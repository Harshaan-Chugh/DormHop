# Cornell DormHop API Specifications

## 0. Status & Error Conventions
| Code | Meaning       | Notes                            |
|------|---------------|----------------------------------|
| 200  | OK            | Successful GET / PATCH / DELETE  |
| 201  | Created       | Successful POST                  |
| 400  | Bad Request   | Missing / malformed input        |
| 401  | Unauthorized  | Bad or expired JWT               |
| 403  | Forbidden     | Authenticated but not allowed    |
| 404  | Not Found     | Resource does not exist          |

Error payload:
```json
{
  "error": "Human-readable message"
}
```

## 1. Authentication

### 1.1 Verify Google ID Token
**POST** `/api/auth/verify_id_token`

The ID token verification endpoint is the entry point for all authentication. When a user signs in with their Cornell Google account, the client obtains a Google ID token. This token is sent to our backend for verification, which checks that it's valid and from a @cornell.edu account. Upon successful verification, we generate our own JWT that the client will use for all subsequent requests.

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
            "id": 123,
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

## 2. Users & Rooms

### 2.1 Get My Profile
**GET** `/api/users/me`

Returns the full user record including current room information.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "id": "uuid",
    "email": "netid@cornell.edu",
    "full_name": "User Name",
    "class_year": 2025,
    "created_at": "2025-04-23T19:15:00Z",
    "current_room": {
        "id": 123,
        "dorm": "Keeton House",
        "room_number": "314",
        "occupancy": 2,
        "amenities": ["private bathroom", "lake view"],
        "description": "Sunny double on 3rd floor"
    },
    "is_room_listed": true
}
```

### 2.2 Create/Update My Room
**PATCH** `/api/users/me/room`

Creates a new room or updates an existing one. Also marks the room as listed.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI...
Content-Type: application/json
```

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
    "id": 123,
    "dorm": "Keeton House",
    "room_number": "314",
    "occupancy": 2,
    "amenities": ["private bathroom", "lake view"],
    "description": "Sunny double on 3rd floor",
    "updated_at": "2025-04-23T19:20:00Z",
    "is_room_listed": true
}
```

Error Response:
```json
<HTTP STATUS CODE 400>
{
    "error": "dorm, room_number, occupancy are required"
}
```

### 2.3 Toggle Room Listing
**PATCH** `/api/users/me/room/visibility`

Controls whether the room appears in public listings.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI...
Content-Type: application/json
```

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

Error Response:
```json
<HTTP STATUS CODE 400>
{
    "error": "is_room_listed required"
}
```

### 2.4 Browse Available Rooms
**GET** `/api/rooms`

Returns all rooms that are marked as listed, except the current user's room.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "rooms": [
        {
            "id": 123,
            "dorm": "Keeton House",
            "room_number": "314",
            "occupancy": 2,
            "amenities": ["private bathroom", "lake view"],
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

### 2.5 Get Room by ID
**GET** `/api/rooms/{room_id}`

Fetches a specific room by ID. The owner can always access their room, but others can only see it if it's publicly listed.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "id": 123,
    "dorm": "Keeton House",
    "room_number": "314",
    "occupancy": 2,
    "amenities": ["private bathroom", "lake view"],
    "description": "Sunny double on 3rd floor",
    "owner": {
        "full_name": "Bob Ross",
        "class_year": 2027
    }
}
```

Error Response:
```json
<HTTP STATUS CODE 404>
{
    "error": "Room not found"
}
```

### 2.6 Get Recommendations
**GET** `/api/recommendations`

Returns room recommendations based on the similarity of amenities and occupancy to the user's room.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "rooms": [
        {
            "id": 123,
            "dorm": "Keeton House",
            "room_number": "314",
            "occupancy": 2,
            "amenities": ["private bathroom", "lake view"],
            "description": "Sunny double on 3rd floor",
            "similarity_score": 0.85,
            "owner": {
                "full_name": "Bob Ross",
                "class_year": 2027
            }
        }
    ],
    "total": 3
}
```

Error Response:
```json
<HTTP STATUS CODE 400>
{
    "error": "User has no current room"
}
```

## 3. Knock Workflow

### 3.1 Send Knock
**POST** `/api/knocks`

Sends a swap request ("knock") to another user's room.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
Content-Type: application/json
```

Request:
```json
{
    "to_room_id": 123
}
```

Response (Standard):
```json
<HTTP STATUS CODE 201>
{
    "id": 42,
    "from_user": {
        "id": 1,
        "email": "alice@cornell.edu",
        "full_name": "Alice Smith",
        "class_year": 2025
    },
    "to_room": {
        "id": 123,
        "dorm": "Keeton House",
        "room_number": "314",
        "occupancy": 2,
        "amenities": ["private bathroom", "lake view"],
        "description": "Sunny double on 3rd floor"
    },
    "status": "pending",
    "created_at": "2025-04-24T02:15:00Z",
    "accepted_at": null
}
```

Response (Auto-Accepted):
```json
<HTTP STATUS CODE 200>
{
    "id": 42,
    "from_user": {
        "id": 1,
        "email": "alice@cornell.edu",
        "full_name": "Alice Smith",
        "class_year": 2025
    },
    "to_room": {
        "id": 123,
        "dorm": "Keeton House",
        "room_number": "314",
        "occupancy": 2,
        "amenities": ["private bathroom", "lake view"],
        "description": "Sunny double on 3rd floor"
    },
    "status": "accepted",
    "created_at": "2025-04-24T02:15:00Z",
    "accepted_at": "2025-04-24T02:15:00Z",
    "contacts": {
        "requester_email": "alice@cornell.edu",
        "owner_email": "bob@cornell.edu"
    }
}
```

Error Responses:
```json
<HTTP STATUS CODE 400>
{
    "error": "to_room_id required"
}

<HTTP STATUS CODE 400>
{
    "error": "Create/list your room before knocking"
}

<HTTP STATUS CODE 400>
{
    "error": "Cannot knock your own room"
}

<HTTP STATUS CODE 400>
{
    "error": "Already knocked"
}

<HTTP STATUS CODE 404>
{
    "error": "Room not found"
}
```

### 3.2 List Sent Knocks
**GET** `/api/knocks/sent`

Returns all knocks sent by the current user.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "knocks": [
        {
            "id": 42,
            "from_user": {
                "id": 1,
                "email": "alice@cornell.edu",
                "full_name": "Alice Smith",
                "class_year": 2025
            },
            "to_room": {
                "id": 123,
                "dorm": "Keeton House",
                "room_number": "314",
                "occupancy": 2,
                "amenities": ["private bathroom", "lake view"],
                "description": "Sunny double on 3rd floor"
            },
            "status": "pending",
            "created_at": "2025-04-24T02:15:00Z",
            "accepted_at": null
        }
    ]
}
```

### 3.3 List Received Knocks
**GET** `/api/knocks/received`

Returns all knocks received by the current user's room.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "knocks": [
        {
            "id": 42,
            "from_user": {
                "id": 1,
                "email": "alice@cornell.edu",
                "full_name": "Alice Smith",
                "class_year": 2025
            },
            "to_room": {
                "id": 123,
                "dorm": "Keeton House",
                "room_number": "314",
                "occupancy": 2,
                "amenities": ["private bathroom", "lake view"],
                "description": "Sunny double on 3rd floor"
            },
            "status": "pending",
            "created_at": "2025-04-24T02:15:00Z",
            "accepted_at": null
        }
    ]
}
```

### 3.4 Accept Knock
**PATCH** `/api/knocks/{knock_id}`

Accepts a received knock request, revealing contact information to both parties.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
Content-Type: application/json
```

Request:
```json
{
    "status": "accepted"
}
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "id": 42,
    "from_user": {
        "id": 1,
        "email": "alice@cornell.edu",
        "full_name": "Alice Smith",
        "class_year": 2025
    },
    "to_room": {
        "id": 123,
        "dorm": "Keeton House",
        "room_number": "314",
        "occupancy": 2,
        "amenities": ["private bathroom", "lake view"],
        "description": "Sunny double on 3rd floor"
    },
    "status": "accepted",
    "created_at": "2025-04-24T02:15:00Z",
    "accepted_at": "2025-04-24T02:16:30Z",
    "contacts": {
        "requester_email": "alice@cornell.edu",
        "owner_email": "bob@cornell.edu"
    }
}
```

Error Responses:
```json
<HTTP STATUS CODE 400>
{
    "error": "Can only set status to 'accepted'"
}

<HTTP STATUS CODE 400>
{
    "error": "Already accepted"
}

<HTTP STATUS CODE 403>
{
    "error": "Not authorized"
}

<HTTP STATUS CODE 404>
{
    "error": "Knock not found"
}
```

### 3.5 Delete/Cancel/Reject Knock
**DELETE** `/api/knocks/{knock_id}`

Deletes a knock. The sender can cancel their request, or the receiver can reject it.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "success": true
}
```

Error Responses:
```json
<HTTP STATUS CODE 403>
{
    "error": "Not authorized"
}

<HTTP STATUS CODE 404>
{
    "error": "Knock not found"
}
```

## 4. Saved Rooms

### 4.1 Save Room
**POST** `/api/users/me/saved_rooms`

Adds a room to the user's saved/favorites list.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
Content-Type: application/json
```

Request:
```json
{
    "room_id": 123
}
```

Response:
```json
<HTTP STATUS CODE 201>
{
    "success": true
}
```

Error Responses:
```json
<HTTP STATUS CODE 400>
{
    "error": "room_id required"
}

<HTTP STATUS CODE 400>
{
    "error": "Already saved"
}

<HTTP STATUS CODE 404>
{
    "error": "Room not found"
}
```

### 4.2 List Saved Rooms
**GET** `/api/users/me/saved_rooms`

Returns all rooms saved by the current user.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "saved_rooms": [
        {
            "id": 123,
            "dorm": "Keeton House",
            "room_number": "314",
            "occupancy": 2,
            "amenities": ["private bathroom", "lake view"],
            "description": "Sunny double on 3rd floor",
            "owner": {
                "full_name": "Bob Ross",
                "class_year": 2027
            }
        }
    ]
}
```

### 4.3 Unsave Room
**DELETE** `/api/users/me/saved_rooms/{room_id}`

Removes a room from the user's saved list.

Headers:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInT5cCI...
```

Response:
```json
<HTTP STATUS CODE 200>
{
    "success": true
}
```

Error Responses:
```json
<HTTP STATUS CODE 400>
{
    "error": "Not in saved list"
}

<HTTP STATUS CODE 404>
{
    "error": "Room not found"
}
```

## 5. Health Check
**GET** `/api`

Simple health check endpoint to verify API is up and running.

Response:
```json
<HTTP STATUS CODE 200>
{
    "message": "Welcome to the DormHop API"
}
```

## Appendix - Deprecated Routes

### A.1 Register User (Dev-only)
**POST** `/api/auth/register`

This endpoint is for development purposes only and should be removed in production.

### A.2 Cornell OAuth Flow
**GET** `/api/auth/cornell`
**POST** `/api/auth/cornell/callback`

These endpoints were part of an earlier authentication flow and are maintained for backward compatibility only.
