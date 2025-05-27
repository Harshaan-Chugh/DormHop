import random, json
from datetime import datetime
from db import db, User, Room
from app import app

DORMS = [
    "Barbara McClintock Hall",
    "Balch Hall",
    "Mary Donlon Hall",
    "Mews Hall",
    "Clara Dickson Hall"
]
GENDERS = ["male", "female"]

def seed(n=20):
    with app.app_context():
        for i in range(n):
            # Assign a random gender to each user
            gender = random.choice(GENDERS)
            u = User(
                email=f"cornellian{i}@cornell.edu",
                full_name=f"Test User {i}",
                class_year=random.choice([2025, 2026, 2027, 2028]),
                gender=gender,
                is_room_listed=True
            )
            db.session.add(u)
            db.session.flush()

            # Create a room matching the user's gender
            r = Room(
                owner_id=u.id,
                owner_gender=u.gender,
                dorm=random.choice(DORMS),
                room_number=str(random.randint(100, 499)),
                occupancy=random.choice([1, 2, 3]),
                amenities=random.sample(
                    ["lake view", "gorge view", "private bath", "big closet", "quiet"],
                    k=2
                ),
                description="Auto‑seeded room"
            )
            db.session.add(r)

        db.session.commit()
        print(f"✅ Seeded {n} users + rooms")


if __name__ == "__main__":
    seed()
