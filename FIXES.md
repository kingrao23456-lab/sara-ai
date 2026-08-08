# Sara AI — Fixes Applied

## 1. Text chat model routing (was broken)
- `GeminiApiClient`: removed invalid model id `gemini-3.5-flash` (doesn't exist), default is
  now `gemini-2.5-flash`. Added `normalizeModelId()` so any bad/unknown model id from the UI
  safely falls back instead of breaking the API call.
- `ChatRepository.sendUserMessage()` / `ChatViewModel.sendMessage()`: the model you pick in
  Model Selector / Settings is now actually sent to Gemini (previously it was ignored).
- Fixed the invalid model list in Settings (`gemini-3.1-pro`, `gemini-3.5-flash` → real ids).

## 2. Silent fake responses (was misleading)
- Previously, any API failure (missing key, network error, bad request) silently returned a
  canned "fake AI personality" reply, making it look like the app was working when it wasn't.
- Now: missing key / network error / HTTP error each return a clear `⚠️ ...` message so you can
  actually tell what's wrong.

## 3. Real voice-to-voice (new)
- Added `GeminiLiveClient.kt` — a genuine WebSocket client for the Gemini **Live API**
  (BidiGenerateContent), true real-time speech-to-speech, not STT→text→TTS.
- Added `LiveVoiceManager.kt` — captures mic audio (16-bit PCM, 16kHz) and streams it live,
  plays back model audio (16-bit PCM, 24kHz) as it arrives, handles barge-in/interruption.
- `VoiceViewModel.startLiveCall()` / `stopLiveCall()` wire this into the app.
- In Voice screen, when **Voice-to-Voice** mode is selected, tapping the orb now starts/stops a
  real live call instead of the old turn-based flow. Chat Mode / Hybrid still use the original
  tap-to-talk (STT → Gemini text → TTS) flow.
- ⚠️ The exact Live model id (`gemini-live-2.5-flash-native-audio` in `GeminiLiveClient.kt`)
  can change / vary by API key access. If the call fails immediately, check Google AI Studio
  for which Live model your key supports and update the `LIVE_MODEL` constant.

## 4. Build (Termux can't build this project directly)
- Added `.github/workflows/build-apk.yml`: builds a debug APK on GitHub's servers every time
  you push to `main`, using your `GEMINI_API_KEY` GitHub secret. Download the APK from the
  workflow's "Artifacts" section.

## What you still need to do
1. Create a `.env` file in the project root (same folder as `.env.example`) with:
   `GEMINI_API_KEY=your_real_key_here` — for local/AI Studio use. **Never commit this file.**
2. In your GitHub repo: Settings → Secrets and variables → Actions → New repository secret →
   name `GEMINI_API_KEY`, value = your real key. This is what the CI build uses.
3. Push to `main` → check the "Actions" tab → download the APK from the finished run.
