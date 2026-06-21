# Android app scaffold

Minimal Android client for the remote MATF reservation and attendance API.

## What it includes

- Login screen
- Secure bearer token storage in the Android Keystore
- Startup session validation
- Logout button
- QR scan flow for logged-in students
- Remote API integration:
  - [Authentication API](API.md#authentication)
  - [Attendance API](API.md#attendance)

## Backend URL

The app currently defaults to:

- `http://10.0.2.2:5000/`

That is the Flask app base URL from the Android emulator.
Set the exact server base URL that serves the API, including any path prefix if your
deployment uses one.

To override it without editing source, set one of these:

- Gradle property: `-PBACKEND_BASE_URL=https://example.com/`
- Environment variable: `BACKEND_BASE_URL=https://example.com/`
- `local.properties`: `serverBaseUrl=https://example.com/`

`local.properties` is the most convenient choice for a physical device because it stays
local to your machine.

## Release Signing

Release builds require a signing key. Put the following values in `app/local.properties`
or provide them as environment variables or Gradle properties:

- `releaseStoreFile=/absolute/path/to/keystore.jks`
- `releaseStorePassword=...`
- `releaseKeyAlias=...`
- `releaseKeyPassword=...`

When these values are present, `./gradlew assembleRelease` uses them automatically.
If they are missing, release tasks fail with a clear error.

## Notes

- Cleartext HTTP is enabled only for development.
- The first version keeps the UI simple and state-driven.
- Use `./gradlew` from the `android/` directory for builds and tasks.
- The wrapper is pinned to Gradle `8.9`.

## API Reference

The Android client talks to a remote Flask API. The remote API consists of:

- Authentication endpoints for session-based login with opaque bearer tokens
- Attendance endpoints for QR challenge retrieval and submission

See [API.md](API.md) for request and response details.
