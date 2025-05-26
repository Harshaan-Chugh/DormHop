import os
import json
import math
from datetime import datetime, timedelta, timezone
from scraper import scrape_community_features

import jwt
from flask import Flask, request
from dotenv import load_dotenv
from google.oauth2 import id_token
from google.auth.transport import requests as google_requests

from db import db, User, Room, Knock

# Environment
load_dotenv()

if not os.environ.get("GOOGLE_CLIENT_ID"):
    raise RuntimeError("GOOGLE_CLIENT_ID missing from .env")
if not os.environ.get("SECRET_KEY"):
    raise RuntimeError("SECRET_KEY missing from .env")

app = Flask(__name__)
app.config["GOOGLE_CLIENT_ID"] = os.environ["GOOGLE_CLIENT_ID"]
app.config["SECRET_KEY"]       = os.environ["SECRET_KEY"]
JWT_EXP_HOURS                  = int(os.environ.get("JWT_EXP_HOURS", 24))

# SQLAlchemy
app.config["SQLALCHEMY_DATABASE_URI"]        = "sqlite:///dormhop.db"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
app.config["SQLALCHEMY_ECHO"]                = True

db.init_app(app)
with app.app_context():
    db.create_all()

# JWT helpers
def encode_token(user):
    """
    Return a signed JWT for a user.
    """
    payload = {
        "user_id": user.id,
        "email": user.email,
        "exp": datetime.now(timezone.utc) + timedelta(hours=JWT_EXP_HOURS),
    }
    return jwt.encode(payload, app.config["SECRET_KEY"], algorithm="HS256")


def decode_token(token):
    """
    Decode JWT or return None if invalid/expired.
    """
    try:
        return jwt.decode(token, app.config["SECRET_KEY"], algorithms=["HS256"])
    except jwt.InvalidTokenError:
        return None

# Decorator
def auth_required(view_fn):
    """
    1. Checks for Bearer <JWT> in Authorization header.
    2. Validates the token and loads the user.
    3. Passes the user as first arg to the wrapped view.
    """
    def wrapper(*args, **kwargs):
        header = request.headers.get("Authorization", "")
        if not header.startswith("Bearer "):
            return json.dumps({"error": "Missing Bearer token"}), 401

        token = header.split(" ", 1)[1]
        data = decode_token(token)
        if not data:
            return json.dumps({"error": "Invalid or expired token"}), 401
        
        if not data["email"].endswith("@cornell.edu"):
            return json.dumps({"error": "Cornell account required"}), 403

        user = User.query.get(data["user_id"])
        if not user:
            return json.dumps({"error": "User not found"}), 404

        return view_fn(user, *args, **kwargs)

    wrapper.__name__ = view_fn.__name__
    return wrapper

# Authentication
@app.route("/api/auth/verify_id_token/", methods=["POST"])
def verify_id_token():
    """
    Android sends {"id_token": "<google-id-token>"}.
    Backend verifies it, then issues its own JWT.
    """
    data         = request.get_json(force=True) or {}
    id_token_str = data.get("id_token")
    if not id_token_str:
        return json.dumps({"error": "id_token required"}), 400

    try:
        info = id_token.verify_oauth2_token(
            id_token_str,
            google_requests.Request(),
            app.config["GOOGLE_CLIENT_ID"],
        )
    except ValueError:
        return json.dumps({"error": "Invalid Google ID token"}), 401

    if info.get("hd") and info["hd"] != "cornell.edu":
        return json.dumps({"error": "Cornell account required"}), 403

    email = info["email"]
    full_name = info.get("name", "")

    user = User.query.filter_by(email=email).first()
    is_new = False
    if not user:
        user = User(email=email, full_name=full_name, class_year=2028)
        db.session.add(user)
        db.session.commit()
        is_new = True

    token = encode_token(user)
    status = 201 if is_new else 200
    return json.dumps({"token": token, "user": user.serialize()}), status


@app.route("/api/auth/register/", methods=["POST"])
def register_user():
    """
    Dev-only registration that skips Google sign-in.
    """
    data = request.get_json(force=True) or {}
    required = {"email", "full_name", "class_year"}
    if not required.issubset(data):
        return json.dumps({"error": "email, full_name, class_year are mandatory"}), 400
    if User.query.filter_by(email=data["email"]).first():
        return json.dumps({"error": "email already registered"}), 400

    user = User(
        email=data["email"],
        full_name=data["full_name"],
        class_year=data["class_year"],
        is_room_listed=data.get("is_room_listed", False),
    )
    db.session.add(user)
    db.session.flush()
    if (room_data := data.get("current_room")):
        room_data["owner_id"] = user.id
        db.session.add(Room(**room_data))
    db.session.commit()

    token = encode_token(user)
    return json.dumps({"token": token, "user": user.serialize()}), 201

# User endpoints
@app.route("/api/users/me/", methods=["GET"])
@auth_required
def get_profile(current_user):
    """
    Return the current user’s profile, including room information.
    """
    return json.dumps(current_user.serialize()), 200


@app.route("/api/users/me/room/", methods=["PATCH"])
@auth_required
def update_room(current_user):
    """
    Create or update the caller’s room and mark it as listed.
    """
    data = request.get_json(force=True) or {}
    required = {"dorm", "room_number", "occupancy"}
    if not required.issubset(data):
        return json.dumps({"error": "dorm, room_number, occupancy are required"}), 400

    if current_user.room:
        r = current_user.room
        r.dorm        = data["dorm"]
        r.room_number = data["room_number"]
        r.occupancy   = data["occupancy"]
        r.amenities   = json.dumps(data.get("amenities", []))
        r.description = data.get("description")
    else:
        data["owner_id"] = current_user.id
        r = Room(**data)
        db.session.add(r)
        current_user.room = r

    current_user.is_room_listed = True
    db.session.commit()

    resp = r.serialize()
    resp["updated_at"] = datetime.now(timezone.utc).isoformat()
    resp["is_room_listed"] = True
    return json.dumps(resp), 200


@app.route("/api/users/me/room/visibility/", methods=["PATCH"])
@auth_required
def set_visibility(current_user):
    """
    Toggle whether the room appears in the public feed.
    """
    data = request.get_json(force=True) or {}
    if "is_room_listed" not in data:
        return json.dumps({"error": "is_room_listed required"}), 400

    current_user.is_room_listed = bool(data["is_room_listed"])
    db.session.commit()
    return json.dumps({
        "is_room_listed": current_user.is_room_listed,
        "updated_at": datetime.now(timezone.utc).isoformat()
    }), 200

# Room endpoints
@app.route("/api/rooms/<int:room_id>/", methods=["GET"])
@auth_required
def get_room(current_user, room_id):
    """
    Return a single room by its ID.
    Owners always may fetch; others only if listed.
    """
    room = Room.query.get(room_id)
    if not room:
        return json.dumps({"error": "Room not found"}), 404
    if room.owner_id != current_user.id and not room.owner.is_room_listed:
        return json.dumps({"error": "Room not found"}), 404

    data = room.serialize()
    data["owner"] = {
        "full_name": room.owner.full_name,
        "class_year": room.owner.class_year
    }
    return json.dumps(data), 200


@app.route("/api/rooms/", methods=["GET"])
@auth_required
def list_rooms(current_user):
    """
    Return all listed rooms except the caller’s own room.
    """
    rooms = (Room.query
              .join(User)
              .filter(User.is_room_listed.is_(True),
                      User.id != current_user.id)
              .all())

    out = []
    for r in rooms:
        d = r.serialize()
        d["owner"] = {"full_name": r.owner.full_name,
                      "class_year": r.owner.class_year}
        out.append(d)

    return json.dumps({"rooms": out, "total": len(out)}), 200


@app.route("/api/recommendations/", methods=["GET"])
@auth_required
def recommend_rooms(current_user):
    """
    Recommend rooms based on amenities & occupancy similarity.
    """
    if not current_user.room:
        return json.dumps({"error": "User has no current room"}), 400

    user_amen = json.loads(current_user.room.amenities)
    user_occ = current_user.room.occupancy

    rooms = (Room.query
             .filter(Room.owner_id != current_user.id)
             .join(User)
             .filter(User.is_room_listed.is_(True))
             .all())

    scored = []
    for r in rooms:
        a_score = (_cosine := (
            len(set(user_amen).intersection(json.loads(r.amenities)))
            / math.sqrt(len(user_amen) * len(json.loads(r.amenities)))
        ) if user_amen and json.loads(r.amenities) else 0.0)
        o_score = 1.0 if r.occupancy == user_occ else 0.5
        total = 0.7 * a_score + 0.3 * o_score
        scored.append((r, total))

    scored.sort(key=lambda x: x[1], reverse=True)
    out = []
    for r, score in scored:
        d = r.serialize()
        d["similarity_score"] = round(score, 2)
        d["owner"] = {"full_name": r.owner.full_name,
                      "class_year": r.owner.class_year}
        out.append(d)

    return json.dumps({"rooms": out, "total": len(out)}), 200

@app.route("/api/knocks/", methods=["POST"])
@auth_required
def send_knock(current_user):
    """
    Send a swap request ("knock") to another user's room.
    If the target room's owner has already knocked on the
    current_user's room, auto-accept both knocks and return
    contact info for both parties.
    """
    data = request.get_json(force=True) or {}
    room_id = data.get("to_room_id")
    if not room_id:
        return json.dumps({"error": "to_room_id required"}), 400

    if not current_user.room:
        return json.dumps({"error": "Create/list your room before knocking"}), 400

    # fetch & validate
    room = Room.query.get(room_id)
    if not room or not room.owner.is_room_listed:
        return json.dumps({"error": "Room not found"}), 404
    if room.owner_id == current_user.id:
        return json.dumps({"error": "Cannot knock your own room"}), 400

    # ensure we haven't already knocked
    exists = Knock.query.filter_by(
        from_user_id=current_user.id,
        to_room_id=room_id
    ).first()
    if exists:
        return json.dumps({"error": "Already knocked"}), 400

    # create the knock
    knock = Knock(from_user_id=current_user.id, to_room_id=room_id)
    db.session.add(knock)
    db.session.commit()

    # check for a reciprocal knock
    reciprocal = Knock.query.filter_by(
        from_user_id=room.owner_id,
        to_room_id=current_user.room.id,
        status="pending"
    ).first()

    if reciprocal:
        # auto-accept both
        now = datetime.now(timezone.utc)
        knock.status = "accepted"
        knock.accepted_at = now
        reciprocal.status = "accepted"
        reciprocal.accepted_at = now
        db.session.commit()

        # build a unified response with contacts
        resp = knock.serialize()
        resp["contacts"] = {
            "requester_email": knock.from_user.email,
            "owner_email":     room.owner.email
        }
        return json.dumps(resp), 200

    # if no reciprocal, just return the new knock
    return json.dumps(knock.serialize()), 201

@app.route("/api/knocks/sent/", methods=["GET"])
@auth_required
def list_sent_knocks(current_user):
    """
    List all knocks sent by the current user.
    Returns array of knock objects with room and status info.
    """
    knocks = Knock.query.filter_by(from_user_id=current_user.id).all()
    return json.dumps({"knocks": [k.serialize() for k in knocks]}), 200

@app.route("/api/knocks/received/", methods=["GET"])
@auth_required
def list_received_knocks(current_user):
    """
    List all knocks received on the user's room.
    Returns array of knock objects with sender info.
    """
    knocks = (Knock.query
                   .join(Room, Knock.to_room)
                   .filter(Room.owner_id == current_user.id)
                   .all())
    return json.dumps({"knocks": [k.serialize() for k in knocks]}), 200

@app.route("/api/knocks/<int:knock_id>/", methods=["PATCH"])
@auth_required
def accept_knock(current_user, knock_id):
    """
    Accept a received knock, revealing contact info to both parties.
    
    Request body must contain {"status": "accepted"}.
    Returns 403 if not the room owner.
    Returns 400 if already accepted.
    """
    data = request.get_json(force=True) or {}
    if data.get("status") != "accepted":
        return json.dumps({"error": "Can only set status to 'accepted'"}), 400

    knock = Knock.query.get(knock_id)
    if not knock:
        return json.dumps({"error": "Knock not found"}), 404
    if knock.to_room.owner_id != current_user.id:
        return json.dumps({"error": "Not authorized"}), 403
    if knock.status == "accepted":
        return json.dumps({"error": "Already accepted"}), 400

    # mark accepted
    knock.status = "accepted"
    knock.accepted_at = datetime.now(timezone.utc)
    db.session.commit()

    # response with 
    resp = knock.serialize()
    resp["contacts"] = {
        "requester_email": knock.from_user.email,
        "owner_email":     current_user.email
    }
    return json.dumps(resp), 200

@app.route("/api/knocks/<int:knock_id>/", methods=["DELETE"])
@auth_required
def delete_knock(current_user, knock_id):
    """
    Delete a knock (cancel sent request or reject received request).
    
    Either sender or receiver can delete.
    Returns 403 if not authorized.
    """
    knock = Knock.query.get(knock_id)
    if not knock:
        return json.dumps({"error": "Knock not found"}), 404

    allowed = (
        knock.from_user_id == current_user.id or
        knock.to_room.owner_id == current_user.id
    )
    if not allowed:
        return json.dumps({"error": "Not authorized"}), 403

    db.session.delete(knock)
    db.session.commit()
    return json.dumps({"success": True}), 200

# Saved‑rooms endpoints
@app.route("/api/users/me/saved_rooms/", methods=["POST"])
@auth_required
def save_room(current_user):
    """
    Save a room to the user's favorites list.
    
    Request body must contain room_id.
    Returns 400 if room_id missing or already saved.
    Returns 404 if room not found or not listed.
    """
    data = request.get_json(force=True) or {}
    room_id = data.get("room_id")
    if not room_id:
        return json.dumps({"error": "room_id required"}), 400

    room = Room.query.get(room_id)
    if not room or not room.owner.is_room_listed:
        return json.dumps({"error": "Room not found"}), 404
    if room in current_user.saved_rooms:
        return json.dumps({"error": "Already saved"}), 400

    current_user.saved_rooms.append(room)
    db.session.commit()
    return json.dumps({"success": True}), 201

@app.route("/api/users/me/saved_rooms/", methods=["GET"])
@auth_required
def list_saved_rooms(current_user):
    """
    Return all rooms saved by the current user.
    
    Each room includes basic owner information.
    Returns empty list if no rooms are saved.
    """
    output = []
    for room in current_user.saved_rooms:
        data = room.serialize()
        data["owner"] = {
            "full_name": room.owner.full_name,
            "class_year": room.owner.class_year
        }
        output.append(data)
    return json.dumps({"saved_rooms": output}), 200

@app.route("/api/users/me/saved_rooms/<int:room_id>/", methods=["DELETE"])
@auth_required
def unsave_room(current_user, room_id):
    """
    Remove a room from user's saved list.
    """
    room = Room.query.get(room_id)
    if not room:
        return json.dumps({"error": "Room not found"}), 404
    if room not in current_user.saved_rooms:
        return json.dumps({"error": "Not in saved list"}), 400

    current_user.saved_rooms.remove(room)
    db.session.commit()
    return json.dumps({"success": True}), 200

DORM_URLS: dict[str, str] = {
    "Alice Cook House":         "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/alice-cook-house",
    "Balch Hall":               "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/balch-hall",
    "Barbara McClintock Hall":  "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/barbara-mcclintock-hall",
    "Carl Becker House":        "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/carl-becker-house",
    "Cascadilla Hall":          "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/residence-halls/cascadilla-hall",
    "Clara Dickson Hall":       "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/clara-dickson-hall",
    "Court–Kay–Bauer Hall":     "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/court-kay-bauer-hallhall",
    "Flora Rose House":         "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/flora-rose-house",
    "Hans Bethe House":         "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/hans-bethe-house",
    "High Rise 5":              "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/high-rise-5",
    "Hu Shih Hall":             "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/hu-shih-hall",
    "Jameson Hall":             "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/jameson-hall",
    "Low Rise 6":               "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/low-rise-6",
    "Low Rise 7":               "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/low-rise-7",
    "Mary Donlon Hall":         "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/mary-donlon-hall",
    "Mews Hall":                "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/mews-hall",
    "Ruth Bader Ginsburg Hall": "https://scl.cornell.edu/residential-life/housing/campus-housing/first-year-undergraduates/residence-halls/ruth-bader-ginsburg-hall",
    "William Keeton House":     "https://scl.cornell.edu/residential-life/housing/campus-housing/upper-level-undergraduates/west-campus-house-system/william-keeton-house",
}

_FEATURE_CACHE: dict[str, list[str]] = {}
@app.route("/api/dorm_features/", methods=["GET"])
@auth_required
def dorm_features(current_user):
    """
    Return a JSON object mapping dorm‑slug → list[feature].

    Example response:
    {
        "Mews Hall": ["Open to first‑year students", "260+ residents", ...],
        "Alice Cook House": [...],
    }
    """
    result = {}

    for slug, url in DORM_URLS.items():
        if slug in _FEATURE_CACHE:
            result[slug] = _FEATURE_CACHE[slug]
            continue

        try:
            feats = scrape_community_features(url)
        except Exception as exc:
            feats = []
        _FEATURE_CACHE[slug] = feats
        result[slug] = feats

    return json.dumps(result), 200

# Dummy Route
@app.route("/api/", methods=["GET"])
def hello():
    return json.dumps({"message": "Welcome to the DormHop API"}), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)