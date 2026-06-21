# MATF Android App

Android client for the MATF reservation and attendance backend.

## Requirements

- Android Studio Jellyfish or newer
- JDK 17
- Android SDK with compile SDK 36 support
- A running backend instance

## Clone

```bash
git clone https://github.com/filipmaric/matf_app.git
cd matf_app
```

## Build

From the repository root:

```bash
./gradlew assembleDebug
```

To compile only:

```bash
./gradlew :app:compileDebugKotlin
```

To install on a connected device or emulator:

```bash
./gradlew installDebug
```

## Backend URL

The app reads the backend base URL from, in order:

1. Gradle property `BACKEND_BASE_URL`
2. Environment variable `BACKEND_BASE_URL`
3. `app/local.properties` entry `serverBaseUrl`
4. Default emulator URL `http://10.0.2.2:5000/`

Examples:

```properties
# app/local.properties
serverBaseUrl=http://10.0.2.2:5000/
```

```bash
BACKEND_BASE_URL=https://example.com/ ./gradlew assembleDebug
```

Use:
- `http://10.0.2.2:5000/` for a backend running on the same machine as the Android emulator
- your machine IP, for example `http://192.168.1.152:5000/`, when testing on a physical phone on the same network

## Release signing

Release builds are signed when these values are available in `app/local.properties`
or as Gradle properties / environment variables:

- `releaseStoreFile`
- `releaseStorePassword`
- `releaseKeyAlias`
- `releaseKeyPassword`

Example:

```properties
# app/local.properties
releaseStoreFile=/absolute/path/to/keystore.jks
releaseStorePassword=secret
releaseKeyAlias=matf
releaseKeyPassword=secret
```

Then build release APKs with:

```bash
./gradlew assembleRelease
```

## Project structure

- `auth/` login and token storage
- `attendance/history/` attendance summary screen
- `attendance/registration/` QR attendance flow
- `API.md` backend API reference

## Notes

- `local.properties` is intentionally ignored by git.
- The app uses cleartext HTTP only for development targets.
- The current UI is split into small controllers and state objects around the attendance scan flow.
