# Cornell DormHop
#### Description: A Cornell-specific room-swap platform that lets students list their current dorm rooms—with amenities, and preferences—browse others’ listings, “love” interesting rooms, and send knocks (swap requests) that open a private negotiation workflow while keeping contact details hidden until both sides agree. It streamlines the post-selection housing shuffle by matching students quickly and transparently, replacing ad-hoc group chats and scattered online postings with a structured, searchable market.

## General Information
- **Status Codes**: 200 OK, 201
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
