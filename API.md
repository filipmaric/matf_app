# API Reference

This Android client talks to a remote Flask app.

Base URL:

- Emulator default: `http://10.0.2.2:5000/`
- Override with `BACKEND_BASE_URL` or `local.properties` as described in `README.md`

## Authentication

### `POST /auth/login`

Request JSON:

- `username`
- `password`
- `device_id`
- `device_name`

Success response:

- `token`
- `token_type`
- `expires_at`
- `user`
- `session`

### `GET /auth/me`

Headers:

- `Authorization: Bearer <token>`

Success response:

- `user`
- `session`

### `GET /attendance/locations`

Headers:

- `Authorization: Bearer <token>`

Success response:

- `attendance_locations`

### `POST /auth/logout`

Headers:

- `Authorization: Bearer <token>`

Success response:

- `ok: true`

## Attendance

The scan flow uses the bearer token from authentication plus the scanned QR token.

### `GET /attendance/<kind>/<id>/<date>/challenge?join_token=...`

Headers:

- `Authorization: Bearer <token>`

Success response:

- `event`
- `challenge`

### `POST /attendance/<kind>/<id>/<date>/join`

Headers:

- `Authorization: Bearer <token>`

Request JSON:

- `join_token`
- `selected_code`

Success response:

- `success`
- `username`

Error responses of interest:

- `401` for invalid or expired bearer token
- `403` when attendance is outside the allowed class window or the session is blocked
- `409` when the selected challenge number is wrong and the app must wait for a new round
