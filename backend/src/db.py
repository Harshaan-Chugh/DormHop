from datetime import datetime, timezone
import json

from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()


class Room(db.Model):
    """
    Room model representing a dorm room in the Cornell housing system.
    dorm (str): Name of the dormitory building
    room_number (str): Room identifier within the building
    occupancy (int): Number of students the room can accommodate
    amenities (str): JSON-encoded list of room amenities
    description (str): Optional description of the room
    owner_id (int): Foreign key to the user who owns this room
    """
    __tablename__ = "rooms"

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    dorm = db.Column(db.String, nullable=False)
    room_number = db.Column(db.String, nullable=False)
    occupancy = db.Column(db.Integer, nullable=False)
    amenities = db.Column(db.Text, nullable=False)  # JSON‑encoded list
    description = db.Column(db.String, nullable=True)

    owner_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)

    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(
        db.DateTime, default=datetime.utcnow, onupdate=datetime.utcnow
    )

    def __init__(self, **kwargs):
        self.dorm = kwargs.get("dorm")
        self.room_number = kwargs.get("room_number")
        self.occupancy = kwargs.get("occupancy")
        self.amenities = json.dumps(kwargs.get("amenities", []))
        self.description = kwargs.get("description")
        self.owner_id = kwargs.get("owner_id")

    def serialize(self):
        return {
            "id": self.id,
            "dorm": self.dorm,
            "room_number": self.room_number,
            "occupancy": self.occupancy,
            "amenities": json.loads(self.amenities),
            "description": self.description,
        }

class User(db.Model):
    """
    User model representing a Cornell student in the room swap system.
    email (str): Cornell email address (@cornell.edu)
    full_name (str): User's full name from Google OAuth
    class_year (int): Expected graduation year
    is_room_listed (bool): Whether user's room is available for swaps
    auto_reject_triple (bool): Preference for triple room offers
    room (Room): One-to-one relationship with Room model
    """
    __tablename__ = "users"

    id = db.Column(db.Integer, primary_key=True, autoincrement=True)
    email = db.Column(db.String, nullable=False, unique=True)
    full_name = db.Column(db.String, nullable=False)
    class_year = db.Column(db.Integer, nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    is_room_listed = db.Column(db.Boolean, default=False)
    auto_reject_triple = db.Column(db.Boolean, default=False)

    room = db.relationship(
        "Room", uselist=False, cascade="all, delete-orphan", backref="owner"
    )

    def __init__(self, **kwargs):
        self.email = kwargs.get("email")
        self.full_name = kwargs.get("full_name")
        self.class_year = kwargs.get("class_year")
        self.is_room_listed = kwargs.get("is_room_listed", False)

    def simple_serialize(self):
        return {
            "id": self.id,
            "email": self.email,
            "full_name": self.full_name,
            "class_year": self.class_year,
        }

    def serialize(self):
        data = self.simple_serialize()
        data.update(
            {
                "created_at": self.created_at.isoformat() + "Z",
                "current_room": self.room.serialize() if self.room else None,
                "is_room_listed": self.is_room_listed,
            }
        )
        return data
