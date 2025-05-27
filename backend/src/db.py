from datetime import datetime, timezone
import json

from flask_sqlalchemy import SQLAlchemy

db = SQLAlchemy()


# Gender helpers
VALID_GENDERS = {"male", "female"}

def _validate_gender(value: str) -> str:
    value = (value or "").lower()
    if value not in VALID_GENDERS:
        raise ValueError("gender must be 'male' or 'female'")
    return value

# Saved‐rooms many-to-many table
saved_rooms = db.Table(
    "saved_rooms",
    db.Column("user_id", db.Integer, db.ForeignKey("users.id"), primary_key=True),
    db.Column("room_id", db.Integer, db.ForeignKey("rooms.id"), primary_key=True),
)

class Room(db.Model):
    __tablename__ = "rooms"
    id              = db.Column(db.Integer, primary_key=True, autoincrement=True)
    dorm            = db.Column(db.String, nullable=False)
    room_number     = db.Column(db.String, nullable=False)
    occupancy       = db.Column(db.Integer, nullable=False)
    amenities       = db.Column(db.Text, nullable=False)  # JSON list
    description     = db.Column(db.String, nullable=True)
    owner_id        = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    gender          = db.Column(db.String, nullable=False)

    created_at      = db.Column(db.DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at      = db.Column(db.DateTime, 
                          default=lambda: datetime.now(timezone.utc),
                          onupdate=lambda: datetime.now(timezone.utc))

    def __init__(self, **kwargs):
        """
        Accepts:
            dorm, room_number, occupancy, amenities (list|None),
            description, owner_id, gender (optional)
        """
        self.dorm         = kwargs["dorm"]
        self.room_number  = kwargs["room_number"]
        self.occupancy    = kwargs["occupancy"]
        self.amenities    = json.dumps(kwargs.get("amenities", []))
        self.description  = kwargs.get("description")
        self.owner_id     = kwargs["owner_id"]

        self.gender       = _validate_gender(
            kwargs.get("gender") or kwargs.get("owner_gender")
        )

    def serialize(self):
        return {
            "id": self.id,
            "dorm": self.dorm,
            "room_number": self.room_number,
            "occupancy": self.occupancy,
            "amenities": json.loads(self.amenities),
            "description": self.description,
            "gender": self.gender,
        }

class User(db.Model):
    __tablename__ = "users"
    id         = db.Column(db.Integer, primary_key=True, autoincrement=True)
    email      = db.Column(db.String, nullable=False, unique=True)
    full_name  = db.Column(db.String, nullable=False)
    class_year = db.Column(db.Integer, nullable=False)
    gender     = db.Column(db.String, nullable=False)        # NEW
    created_at = db.Column(db.DateTime, default=lambda: datetime.now(timezone.utc))
    is_room_listed   = db.Column(db.Boolean, default=False)
    auto_reject_triple = db.Column(db.Boolean, default=False)

    room = db.relationship("Room", uselist=False, cascade="all, delete-orphan", backref="owner")

    # bookmarks
    saved_rooms = db.relationship(
        "Room", secondary=saved_rooms,
        backref="saved_by"
    )

    def __init__(self, **kwargs):
        self.email = kwargs.get("email")
        self.full_name = kwargs.get("full_name")
        self.class_year = kwargs.get("class_year")
        self.gender     = _validate_gender(kwargs.get("gender"))
        self.is_room_listed = kwargs.get("is_room_listed", False)

    def simple_serialize(self) -> dict:
        return {
            "id": self.id,
            "email": self.email,
            "full_name": self.full_name,
            "class_year": self.class_year,
            "gender": self.gender,
        }

    def serialize(self) -> dict:
        data = self.simple_serialize()
        data.update(
            {
                "created_at": self.created_at.isoformat() + "Z",
                "current_room": self.room.serialize() if self.room else None,
                "is_room_listed": self.is_room_listed,
            }
        )
        return data

class Knock(db.Model):
    __tablename__ = "knocks"
    id              = db.Column(db.Integer, primary_key=True, autoincrement=True)
    from_user_id    = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    to_room_id      = db.Column(db.Integer, db.ForeignKey("rooms.id"), nullable=False)
    status          = db.Column(db.String,  nullable=False, default="pending")
    created_at      = db.Column(db.DateTime, default=lambda: datetime.now(timezone.utc))
    accepted_at     = db.Column(db.DateTime, nullable=True)

    from_user = db.relationship("User", backref="knocks_sent", foreign_keys=[from_user_id])
    to_room   = db.relationship("Room", backref="knocks_received", foreign_keys=[to_room_id])

    def serialize(self):
        return {
            "id": self.id,
            "from_user": self.from_user.simple_serialize(),
            "to_room": self.to_room.serialize(),
            "status": self.status,
            "created_at": self.created_at.isoformat() + "Z",
            "accepted_at": self.accepted_at.isoformat() + "Z" if self.accepted_at else None
        }