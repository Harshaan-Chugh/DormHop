# Cornell DormHop  
### 🏆 Best Overall App — AppDev Hack Challenge (SP25)

### DormHop is Cornell’s first‑and‑only room‑swap platform: list your dorm, browse and bookmark rooms, and send mutual “knocks” to negotiate swaps once both sides agree. It replaces ad‑hoc group chats with a personalized, searchable marketplace that streamlines the post‑selection housing shuffle.

### Demo Video: https://youtu.be/4a0Fyvim-wQ
## General Information
- **Docker Hub**: https://hub.docker.com/repository/docker/harshaan999/dormhop/tags
- **Authentication Flow**:
  1. Users sign in through Cornell Google OAuth
  2. After successful OAuth, server generates a JWT for subsequent requests
  3. Client includes JWT in Authorization header for all API calls, preventing re-authentication with Google in every API call.

- **Base URL**: `/api`
- **Token Contents**:
  - User ID
  - Cornell Email
  - Role
  - Expiration time

- **Authorization Header**:
  - Format: `Authorization: Bearer <jwt_token>`
  - Whoever bears (possesses) this token has access to all endpoints.
  - Example:
    ```http
    Authorization: Bearer brAinRotTokenGciOiJIUzI1NiIsInT5cCI...
    ```


## 🔐 Authentication (OAuth and JWT)

1. **Client obtains a Google ID-token** from Cornell-restricted OAuth.
2. **Exchange for JWT**

   **POST** `/auth/verify_id_token`

   ```json
   { "id_token": "<google-id-token>" }
   ```

   → **201 Created** (new) | **200 OK** (returning)

   ```json
   { "token": "<jwt>", "user": {/* body */} }
   ```

   *Valid only for `@cornell.edu`.*

After that, include the JWT in every request:

```
Authorization: Bearer <jwt>
```

---

## Models

| Model | Key Attributes / Relations |
|-------|----------------------------|
| **User** | `id`, `email`, `full_name`, `class_year`, `is_room_listed`<br>– 1:1 with **Room**<br>– many:many with **Room** via `saved_rooms`<br>– 1:Many knocks_sent / knocks_received |
| **Room** | `id`, `dorm`, `room_number`, `occupancy`, `amenities[]`, `description`<br>– Foreign Key owner_id → User |
| **Knock** | Swap request: `from_user_id → User`, `to_room_id → Room`, `status`, `accepted_at` |
| **saved_rooms** | Join table for User ↔ Room |

---

## REST Endpoints

| Verb       | Path                              | Purpose                                                          |
| ---------- | --------------------------------- | ---------------------------------------------------------------- |
| **POST**   | `/auth/verify_id_token`           | Exchange Google ID-token → JWT (public)                          |
| **GET**    | `/dorm_features`                  | Fetch each dorm’s “Community Features” list via scraper          |
| **POST**   | `/auth/register`                  | Dev-only fake signup (no OAuth)                                  |
| **GET**    | `/users/me`                       | Current profile (+ room)                                         |
| **PATCH**  | `/users/me/room`                  | Create/update your room (auto-lists)                             |
| **PATCH**  | `/users/me/room/visibility`       | Toggle `is_room_listed`                                          |
| **GET**    | `/rooms`                          | Browse all listed rooms (excluding your own)                     |
| **GET**    | `/rooms/{room_id}`                | Fetch one room (listed or your own)                              |
| **GET**    | `/recommendations`                | Ranked rooms by amenities & occupancy similarity                 |
| **POST**   | `/knocks`                         | Send knock `{ "to_room_id": <id> }` – auto-accepts if reciprocal |
| **GET**    | `/knocks/sent`                    | Knocks you have sent                                             |
| **GET**    | `/knocks/received`                | Knocks on **your** room                                          |
| **PATCH**  | `/knocks/{id}`                    | Accept a knock `{ "status": "accepted" }`                        |
| **DELETE** | `/knocks/{id}`                    | Cancel sent or reject received                                   |
| **POST**   | `/users/me/saved_rooms`           | Save room `{ "room_id": <id> }`                                  |
| **GET**    | `/users/me/saved_rooms`           | List saved rooms                                                 |
| **DELETE** | `/users/me/saved_rooms/{room_id}` | Un-save room                                                     |
> **All routes except** `/auth/*` **and** `/` **require the JWT header.**

## Knock Workflow

1. **Alice** posts `/knocks` → Bob’s room → **pending**
2. **Bob** posts `/knocks` → Alice’s room → both auto-**accepted**, returns `contacts` emails
3. **Eva** (Evesdropper inspired by CS 4820) doesn't see this interaction.
4. Manual accept: room owner `PATCH /knocks/{id}`.
