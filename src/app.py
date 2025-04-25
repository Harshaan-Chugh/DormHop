import os
import json
from datetime import datetime, timedelta, timezone

import jwt
from flask import Flask, request
from dotenv import load_dotenv

from db import db, User, Room

load_dotenv()

app = Flask(__name__)
app.config["SECRET_KEY"] = os.environ.get("SECRET_KEY")
JWT_EXP_HOURS = int(os.environ.get("JWT_EXP_HOURS", 24))

app.config["SQLALCHEMY_DATABASE_URI"] = "sqlite:///dormhop.db"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False
app.config["SQLALCHEMY_ECHO"] = True

db.init_app(app)
with app.app_context():
    db.create_all()


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
    Decode JWT. Returns None if token is invalid or expired.
    """
    try:
        return jwt.decode(token, app.config["SECRET_KEY"], algorithms=["HS256"])
    except jwt.InvalidTokenError:
        return None


def auth_required(view_fn):
    """
    Wraps functions to add behaviors outlined below:
    1.  Makes sure request carries a valid JWT in its Authorization header.  
    2.  Look up the corresponding user in DB.
    3.  Call the orig view with user obj. as first arg
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


# Auth endpoints
@app.route("/api/auth/cornell", methods=["GET"])
def cornell_auth():
    """
    Placeholder for the Cornell Google OAuth redirect.
    """
    return json.dumps({"message": "Redirect to Cornell OAuth consent screen here"}), 200


@app.route("/api/auth/cornell/callback", methods=["POST"])
def cornell_callback():
    """
    Accept email and full_name from OAuth. Create the user if new,
    otherwise return the existing user.
    """
    data = request.get_json(force=True) or {}
    email, full_name = data.get("email"), data.get("full_name")
    if not email or not full_name:
        return json.dumps({"error": "'email' and 'full_name' are required"}), 400

    user = User.query.filter_by(email=email).first()
    is_new = False

    if not user:
        class_year = data.get("class_year")
        if class_year is None:
            return json.dumps({"error": "New users must supply 'class_year'"}), 400

        user = User(
            email=email,
            full_name=full_name,
            class_year=class_year,
            is_room_listed=data.get("is_room_listed", False),
        )
        db.session.add(user)
        db.session.flush()

        if (room_data := data.get("current_room")):
            room_data["owner_id"] = user.id
            db.session.add(Room(**room_data))

        db.session.commit()
        is_new = True

    token = encode_token(user)
    status = 201 if is_new else 200
    return json.dumps({"token": token, "user": user.serialize()}), status


@app.route("/api/auth/register", methods=["POST"])
def register_user():
    """
    Dev-only registration that skips OAuth.
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


@app.route("/api/", methods=["GET"])
def hello():
    """
    Health-check endpoint.
    """
    return json.dumps({"message": "DormHop API"}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)
