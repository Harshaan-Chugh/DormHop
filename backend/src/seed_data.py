import random, json
from datetime import datetime
from db import db, User, Room
from app import app

DORMS = ["Barbara McClintock Hall","Balch Hall","Mary Donlon Hall","Mews Hall","Clara Dickson Hall"]

def seed(n=20):
    with app.app_context():
        for i in range(n):
            u = User(
                email=f"cornellian{i}@cornell.edu",
                full_name=f"Test User {i}",
                class_year=random.choice([2025,2026,2027,2028]),
                is_room_listed=True
            )
            db.session.add(u)
            db.session.flush()
            r = Room(
                owner_id=u.id,
                dorm=random.choice(DORMS),
                room_number=str(random.randint(100,499)),
                occupancy=random.choice([1,2,3]),
                amenities=random.sample(
                  ["lake view","gorge view","private bath","big closet","quiet"],
                  k=2
                ),
                description="Auto‐seeded room"
            )
            db.session.add(r)
        db.session.commit()
        print(f"✅ Seeded {n} users + rooms")

if __name__=="__main__":
    seed()