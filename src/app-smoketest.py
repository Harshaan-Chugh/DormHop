import os, json
from datetime import datetime, timezone
from flask import Flask, request
from db import db, User, Room

DB_FILE = "dormhop_smoke.db" # separate DB so you don't pollute real data

app = Flask(__name__)
app.config["SQLALCHEMY_DATABASE_URI"] = f"sqlite:///{DB_FILE}"
app.config["SQLALCHEMY_TRACK_MODIFICATIONS"] = False

# bootstrap DB
db.init_app(app)
with app.app_context():
    db.create_all()
    tester = User.query.filter_by(email="smoke@cornell.edu").first()
    if not tester:
        tester = User(
            email="smoke@cornell.edu",
            full_name="Smoke Tester",
            class_year=9999,
            is_room_listed=False,
        )
        db.session.add(tester)
        db.session.commit()

    # grab the ID while the object is still attached
    SMOKE_USER_ID = tester.id
SMOKE_USER_ID = tester.id

# fake auth decorator
def smoke_user(view_fn):
    def wrapper(*args, **kwargs):
        user = User.query.get(SMOKE_USER_ID)
        return view_fn(user, *args, **kwargs)
    wrapper.__name__ = view_fn.__name__
    return wrapper

# routes (only the ones we want to test)
@app.route("/api/users/me", methods=["GET"])
@smoke_user
def me(user):
    return json.dumps(user.serialize()), 200

@app.route("/api/users/me/room", methods=["PATCH"])
@smoke_user
def upsert_room(user):
    data = request.get_json(force=True) or {}
    if user.room:
        user.room.dorm        = data.get("dorm", user.room.dorm)
        user.room.room_number = data.get("room_number", user.room.room_number)
        user.room.occupancy   = data.get("occupancy", user.room.occupancy)
        user.room.amenities   = json.dumps(data.get("amenities", []))
        user.room.description = data.get("description")
    else:
        data["owner_id"] = user.id
        room = Room(**data)
        db.session.add(room)
        user.room = room
    user.is_room_listed = True
    db.session.commit()
    return json.dumps(user.room.serialize()), 200

@app.route("/api/rooms", methods=["GET"])
@smoke_user
def list_rooms(user):
    rooms = Room.query.filter(Room.owner_id != user.id).all()
    return json.dumps({"rooms": [r.serialize() for r in rooms]}), 200

@app.route("/api/", methods=["GET"])
def ping():
    return json.dumps({"msg": "smoke OK"}), 200

if __name__ == "__main__":
    app.run(port=5001, debug=True)
