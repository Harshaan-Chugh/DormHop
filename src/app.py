import os
import json
from datetime import datetime, timedelta, timezone
import math

import jwt
from flask import Flask, request
from dotenv import load_dotenv
from google.oauth2 import id_token
from google.auth.transport import requests as google_requests

from db import db, User, Room

# Environment Stuff
load_dotenv()

if not os.environ.get("GOOGLE_CLIENT_ID"):
    raise RuntimeError("GOOGLE_CLIENT_ID missing from .env")


app = Flask(__name__)
app.config["GOOGLE_CLIENT_ID"] = os.environ["GOOGLE_CLIENT_ID"]
JWT_EXP_HOURS = int(os.environ.get("JWT_EXP_HOURS", 24))

# SQLAlchemy Stuff
app.config["SECRET_KEY"] = "mysecretkey"
app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///dormhop.db"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
app.config["SQLALCHEMY_ECHO"] = True

db.init_app(app)
with app.app_context():
    db.create_all()

# JWT helpers
def encode_token(user):
    """
    Return a signed JWT for a user.
    """
    output = {
        "user_id": user.id,
        "email": user.email,
        "exp": datetime.now(timezone.utc) + timedelta(hours=JWT_EXP_HOURS),
    }
    return jwt.encode(output, app.config["SECRET_KEY"], algorithm="HS256")


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

        user = User.query.get(data["user_id"])
        if not user:
            return json.dumps({"error": "User not found"}), 404

        return view_fn(user, *args, **kwargs)

    wrapper.__name__ = view_fn.__name__
    return wrapper

# ID-token verification route
@app.route("/api/auth/verify_id_token", methods=["POST"])
def verify_id_token():
    """
    Android sends {"id_token": "<google-id-token>"}.
    Backend verifies it, then issues its own JWT.
    """
    data = request.get_json(force=True) or {}
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
        user = User(email=email, full_name=full_name, class_year=9999)
        db.session.add(user)
        db.session.commit()
        is_new = True

    token = encode_token(user)
    status = 201 if is_new else 200
    return json.dumps({"token": token, "user": user.serialize()}), status

# Route for testing authentication
@app.route("/api/auth/register", methods=["POST"])
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
@app.route("/api/users/me", methods=["GET"])
@auth_required
def get_profile(current_user):
    """
    Return the current user’s profile, including room information.
    """
    return json.dumps(current_user.serialize()), 200


@app.route("/api/users/me/room", methods=["PATCH"])
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
        r.dorm = data["dorm"]
        r.room_number = data["room_number"]
        r.occupancy = data["occupancy"]
        r.amenities = json.dumps(data.get("amenities", []))
        r.description = data.get("description")
    else:
        data["owner_id"] = current_user.id
        r = Room(**data)
        db.session.add(r)
        current_user.room = r

    current_user.is_room_listed = True
    db.session.commit()

    response = r.serialize()
    response["updated_at"] = datetime.now(timezone.utc).isoformat()
    response["is_room_listed"] = True
    return json.dumps(response), 200


@app.route("/api/users/me/room/visibility", methods=["PATCH"])
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
    return json.dumps(
        {
            "is_room_listed": current_user.is_room_listed,
            "updated_at": datetime.now(timezone.utc).isoformat(),
        }
    ), 200


# Public room feed
@app.route("/api/rooms", methods=["GET"])
@auth_required
def list_rooms(current_user):
    """
    Return all listed rooms except the caller’s own room.
    """
    rooms = (
        Room.query.join(User)
        .filter(User.is_room_listed.is_(True), User.id != current_user.id)
        .all()
    )

    output = []
    for r in rooms:
        d = r.serialize()
        d["owner"] = {"full_name": r.owner.full_name, "class_year": r.owner.class_year}
        output.append(d)

    return json.dumps({"rooms": output, "total": len(output)}), 200


def cosine_similarity(list1, list2):
    """
    Compute cosine similarity between two lists (for amenities).
    """
    set1 = set(list1)
    set2 = set(list2)
    intersection = len(set1.intersection(set2))
    if not set1 or not set2:
        return 0.0
    return intersection / math.sqrt(len(set1) * len(set2))


@app.route("/api/", methods=["GET"])
def hello():
    """
    Health-check endpoint.
    """
    return json.dumps({"message": "DormHop API"}), 200


@app.route("/api/recommendations", methods = ["GET"])
@auth_required
def recommend_rooms(current_user):
    """
    Recommend rooms based on user's current room amenities and occupancy.
    """
    if not current_user.room:
        return json.dumps({"error": "User has no current room"}), 400

    user_amenities = json.loads(current_user.room.amenities)
    user_occupancy = current_user.room.occupancy

    rooms = (Room.query.filter(Room.owner_id != current_user.id).join(User).filter(User.is_room_listed.is_(True)).all())

    scored_rooms = []

    for r in rooms:
        room_amenities = json.loads(r.amenities)
        room_occupancy = r.occupancy

        amenity_score = cosine_similarity(user_amenities, room_amenities)
        occupancy_score = 1.0 if room_occupancy == user_occupancy else 0.5

        total_score = 0.7 * amenity_score + 0.3 * occupancy_score
        scored_rooms.append((r, total_score))

    scored_rooms.sort(key=lambda x: x[1], reverse=True)

    output = []
    for r, score in scored_rooms:
        d = r.serialize()
        d["similarity_score"] = round(score, 2)
        d["owner"] = {"full_name": r.owner.full_name, "class_year": r.owner.class_year}
        output.append(d)

    return json.dumps({"rooms": output, "total": len(output)}), 200

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
