<<<<<<< HEAD
# Cornell DormHop API
#### Description: A Cornell-specific room-swap platform that lets students list their current dorm rooms—with amenities, and preferences—browse others’ listings, “love” interesting rooms, and send knocks (swap requests) that open a private negotiation workflow while keeping contact details hidden until both sides agree. It streamlines the post-selection housing shuffle by matching students quickly and transparently, replacing ad-hoc group chats and scattered online postings with a structured, searchable market.

## General Information
- **Status Codes**: 200 OK, 201
=======
# Cornell DormHop
#### Description: A Cornell-specific room-swap platform that lets students list their current dorm rooms—with amenities, and preferences—browse others’ listings, “love” interesting rooms, and send knocks (swap requests) that open a private negotiation workflow while keeping contact details hidden until both sides agree. It streamlines the post-selection housing shuffle by matching students quickly and transparently, replacing ad-hoc group chats and scattered online postings with a structured, searchable market.

## General Information
>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
- **Authentication Flow**:
  1. Users sign in through Cornell Google OAuth
  2. After successful OAuth, server generates a JWT for subsequent requests
  3. Client includes JWT in Authorization header for all API calls

- **How JWT works with OAuth**:
  - OAuth handles initial authentication (proving you're a Cornell user)
  - JWTs handles subsequent requests (maintaining session state)
  - Prevents having to re-authenticate with Google for every API call

- **Content-Type**: application/json
- **Base URL**: `/api`
- **Error Responses**: Include message and details fields
- **Token Contents**:
  - User ID
  - Cornell Email
  - Role/permissions
  - Expiration time

- **UUID Usage**:
  - Universally Unique Identifier
  - 32 hexadecimal digits displayed in 5 groups
  - Example: `123e4567-e89b-12d3-a456-426614174000`

- **Authorization Header**:
  - Format: `Authorization: Bearer <jwt_token>`
  - The word "Bearer" indicates that whoever bears (possesses) this token has access
  - Required for all endpoints except `/api/auth/*`
  - Example:
    ```http
    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
    ```
  - The token is obtained after login/registration and must be included in every subsequent request. It tells the server who you are and expires after a set time / on logout

<<<<<<< HEAD
Example Request:
```http
GET /api/users/me HTTP/1.1
Host: api.dormhop.cornell.edu
Authorization: Bearer eyJhbGciOiJIUzI1...
Content-Type: application/json
```

Example:
```
{
  "user_id": "123",
  "email": "netid@cornell.edu",
  "exp": 1682506874
}

Turns into: eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiMTIzIiwiZW1haWwiOiJuZXRpZEBjb3JuZWxsLmVkdSIsImV4cCI6MTY4MjUwNjg3NH0.ABC123signature
```
## 1. Authentication

### 1.1 Verify Google ID Token
**POST** `/api/auth/verify_id_token`

The ID token verification endpoint is the entry point for all authentication. When a user signs in with their Cornell Google account, the client obtains a Google ID token. This token is sent to our backend for verification, which checks that it's valid and from a @cornell.edu account. Upon successful verification, we generate our own JWT that the client will use for all subsequent requests. For new users, we create an account with basic info from Google OAuth. Returning users receive their existing profile data.

Headers:
```http
Content-Type: application/json
```

Request:
```json
{
    "id_token": "ya29.A0ARrdaMxyz...<google-id-token>..."
}
```

Response (New User):
```json
<HTTP STATUS CODE 201>
{
    "token": "<jwt>",
    "user": {
        "id": "uuid",
        "email": "netid@cornell.edu",
        "full_name": "User Name",
        "class_year": 9999,
        "created_at": "2025-04-23T19:15:00Z",
        "current_room": null,
        "is_room_listed": false
    }
}
```

Response (Returning User):
```json
<HTTP STATUS CODE 200>
{
    "token": "<jwt>",
    "user": {
        "id": "uuid",
        "email": "netid@cornell.edu",
        "full_name": "User Name",
        "class_year": 2025,
        "created_at": "2025-04-23T19:15:00Z",
        "current_room": {
            "dorm": "Keeton House",
            "room_number": "314",
            "occupancy": 2,
            "amenities": ["private bathroom", "lake view"],
            "description": "Sunny double on 3rd floor"
        },
        "is_room_listed": false
    }
}
```

Error Responses:
```json
<HTTP STATUS CODE 400>
{
    "error": "id_token required"
}

<HTTP STATUS CODE 401>
{
    "error": "Invalid Google ID token"
}

<HTTP STATUS CODE 403>
{
    "error": "Cornell account required"
}
```

Notes:
- ID token must be from Google Sign-In with Cornell domain
- New users get placeholder class year (9999)
- Token in response is a JWT for subsequent requests
- System determines if user is new based on email

## 2. Users & Rooms

### 2.1 Get Profile with Room
**GET** `/api/users/me`

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "id": "uuid",
    "email": "br11@cornell.edu",
    "full_name": "Bob Ross",
    "class_year": 1950,
    "auto_reject_triple": false,
    "created_at": "2025-04-23T19:15:00Z",
    "current_room": {
        "dorm": "Cascadilla Hall",
        "room_number": "314",
        "occupancy": 2,
        "amenities": ["gorge view"],
        "description": "Top floor single overlooking the gorge"
    },
    "is_room_listed": true
}
```

### 2.2 Update Room Details
**PATCH** `/api/users/me/room`

Request:
```json
{
    "dorm": "Keeton House",
    "room_number": "314",
    "occupancy": 2,
    "amenities": ["private bathroom", "lake view"],
    "description": "Sunny double on 3rd floor"
}
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "dorm": "Keeton House",
    "room_number": "314",
    "occupancy": 2,
    "amenities": ["private bathroom", "lake view"],
    "description": "Sunny double on 3rd floor",
    "updated_at": "2025-04-23T19:20:00Z",
    "is_room_listed": true
}
```

### 2.3 Toggle Room Listing
**PATCH** `/api/users/me/room/visibility`

Request:
```json
{
    "is_room_listed": false
}
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "is_room_listed": false,
    "updated_at": "2025-04-23T19:20:00Z"
}
```

### 2.4 Browse Available Rooms
**GET** `/api/rooms`

Response:
```json
<HTTP STATUS CODE 200>
{
    "rooms": [
        {
            "dorm": "Flora Rose House",
            "room_number": "042",
            "occupancy": 2,
            "amenities": ["grass outside my room"],
            "description": "Sunny double on 3rd floor",
            "owner": {
                "full_name": "Bob Ross",
                "class_year": 2027
            }
        }
    ],
    "total": 87
}
```

Note: Only returns rooms where `is_room_listed` is true

# Retired Routes (No longer in service)

### 1.1 Cornell Authentication
**GET** `/api/auth/cornell`
- Redirects to Cornell Google OAuth consent screen
- Automatically restricts to @cornell.edu domain
- Handles both new and returning users

**POST** `/api/auth/cornell/callback`
Request (Only required for new users):
```json
{
    "class_year": 2028,
    "current_room": {
        "dorm": "Keeton House",
        "room_number": "314",
        "occupancy": 2,
        "amenities": ["private bathroom", "lake view"],
        "description": "Sunny double on 3rd floor"
    },
    "is_room_listed": false
}


```

Response (New User):
```json
<HTTP STATUS CODE 201>
{
    "token": "<jwt>",
    "user": {
        "id": "uuid",
        "email": "bc44@cornell.edu",
        "full_name": "Bombardino Crocodilo",  // from Google OAuth
        "class_year": 2028,
        "created_at": "2025-04-23T19:15:00Z",
        "current_room": {
            "dorm": "Keeton House",
            "room_number": "314",
            "occupancy": 2,
            "amenities": ["private bathroom", "lake view"],
            "description": "Sunny double on 3rd floor"
        },
        "is_room_listed": false
    }
}
```

Response (Returning User):
```json
<HTTP STATUS CODE 200>
{
    "token": "<jwt>",
    "user": {
        "id": "uuid",
        "email": "bc44@cornell.edu",
        "full_name": "Bombardino Crocodilo",
        "class_year": 2025,
        "current_room": {
            "dorm": "Keeton House",
            "room_number": "314",
            "occupancy": 2,
            "amenities": ["private bathroom", "lake view"],
            "description": "Sunny double on 3rd floor"
        },
        "is_room_listed": false
    }
}
```

Note: System determines if user is new based on Cornell email from OAuth. New users must provide additional information in callback request.

### 1.2 Register User
**POST** `/api/auth/register`

Request:
```json
{
    "email": "tt6699@cornell.edu",
    "full_name": "Tralalero Tralala",
    "class_year": 2028,
    "current_room": {
        "dorm": "Keeton House",
        "room_number": "314",
        "occupancy": 2,
        "amenities": ["private bathroom", "lake view"],
        "description": "Sunny double on 3rd floor"
    },
    "is_room_listed": false
}
```

Response:
```json
<HTTP STATUS CODE 201>
{
    "token": "<jwt>",
    "user": {
        "id": "uuid",
        "email": "tt6699@cornell.edu",
        "full_name": "Tralalero Tralala",
        "class_year": 2027,
        "created_at": "2025-04-23T19:15:00Z",
        "current_room": {
            "dorm": "Keeton House",
            "room_number": "314",
            "occupancy": 2,
            "amenities": ["private bathroom", "lake view"],
            "description": "Sunny double on 3rd floor"
        },
        "is_room_listed": false
    }
}
```
=======

>>>>>>> b415288ab12c93f049217ef3a1cbb40ddf906c9c
