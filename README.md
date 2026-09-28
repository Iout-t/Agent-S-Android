# AgentX

AgentX is a Kotlin-first Android client for autonomous GUI automation. The Android application is implemented in Kotlin, while the Python automation engine remains a separate, protocol-compatible backend.

## Architecture

- **Android client (`app/`)**: Kotlin, Android SDK, coroutines, ViewModel, and a small HTTP client. It owns device permissions, lifecycle, connection state, and automation controls.
- **Python core (`backend/`)**: The existing Python automation architecture remains the source of truth for planning and execution. The included adapter exposes it through a versioned HTTP API without moving core agent logic into the Android process.
- **Transport**: JSON over HTTP today; the protocol is intentionally isolated so WebSocket or ADB transport can be added without changing the UI.

## Build the Android app

Open the repository in Android Studio or run:

```bash
./gradlew assembleDebug
```

The backend URL can be changed in `app/src/main/java/com/agentx/android/data/AgentRepository.kt` or injected through the `AGENTX_BACKEND_URL` build configuration.

## Run the Python backend

```bash
cd backend
python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
python -m agentx_backend
```

The backend listens on `0.0.0.0:8080` and provides `GET /health` and `POST /v1/runs`.

## Migration notes

The Python implementation is deliberately kept behind the backend contract. This replaces the Android/Python application architecture with a native Kotlin Android client without rewriting or coupling the Python automation core to Android UI code.
