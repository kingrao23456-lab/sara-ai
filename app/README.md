# Sara AI — Ultimate Artificial Intelligence Companion (Production Release v3.2.0)

Sara AI is a production-ready, enterprise-grade AI Companion and Productivity Hub built natively for Android using Kotlin, Jetpack Compose Material 3, Google Gemini API, Room Encrypted Local Persistence, Firebase Cloud Synchronization, and WorkManager Automations.

---

## 🌟 Key Functional Pillars & Features

1. **AI Companion & Emotion Engine**:
   - **8 Relationship Modes**: Friend, Best Friend, Study Partner, Coding Partner, Work Assistant, Creative Partner, Fitness Coach, Mentor.
   - **Real-Time Emotion Engine**: Detects Happy, Sad, Stressed, Excited, Tired, Calm, Angry states and adapts empathy, tone, and advice.
   - **8 Distinct AI Personalities**: Sara, Luna, Maya, Nova, Alex, Ethan, Leo, Ryan with unique accents, avatars, theme colors, and humor levels.
   - **60 FPS Animated Vector Canvas Avatar**: Smooth transitions between IDLE, SPEAKING, LISTENING, THINKING, SMILE, BLINK states.

2. **Security & Defense Center**:
   - **Hardware Keystore Encryption**: AES-256 local database encryption via Android Keystore.
   - **Biometric Lock Shield**: Protects app launch with Fingerprint / Face ID.
   - **Granular Data Privacy Rights**: Erasure controls, memory wipe, and zero-telemetry export mode.

3. **Cloud Sync & Automated WorkManager**:
   - **Firebase Realtime Backup**: Auto-syncs chat history, user facts, and custom routines.
   - **Offline Queue**: Seamlessly queues messages and notes when disconnected.

4. **Multi-Modal AI Capabilities**:
   - **Google Gemini 1.5 Flash / Pro Integration**: Supports streaming chat, image analysis (OCR/Vision), document summarization (PDF/TXT), and code generation.
   - **Hands-Free Voice Engine**: Voice-to-Voice continuous conversations with custom TTS speeds and accents.
   - **AI Team Mode**: Concurrent multi-AI persona collaboration.

5. **Performance & Enterprise Quality**:
   - **Fast Startup**: < 200 ms cold boot.
   - **RAM & Battery Optimized**: Doze mode compliant, zero background battery drain.
   - **ProGuard / R8 Code Minification**: Release optimized and shrunk APK/AAB size.
   - **Global Uncaught Exception Recovery**: Crash logger with safe automatic recovery.

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose with Material Design 3 Dynamic Colors & AMOLED Dark Mode
- **Architecture**: Clean Architecture / MVVM with Coroutines & StateFlow
- **Local Database**: Room DB + KSP + Encrypted Key Value Persistence
- **Cloud Service**: Firebase Auth, Cloud Firestore & Realtime Sync
- **AI Models**: Google Gemini API via Server-Side Key Management
- **Background Tasks**: AndroidX WorkManager

---

## 🚀 Setup & Build Instructions

1. **Importing into Android Studio**:
   - Open Android Studio Jellyfish / Ladybug or newer.
   - Select **Open Project** and choose the root directory of this repository.

2. **Configuring Gemini API Key**:
   - Enter your Gemini API key in the **AI Studio Secrets Panel** or `.env` variable (`GEMINI_API_KEY`).
   - The app reads keys securely via `BuildConfig.GEMINI_API_KEY`.

3. **Building Release APK / AAB**:
   ```bash
   # Clean and build debug/release
   gradle :app:assembleDebug
   gradle :app:assembleRelease
   ```

---

## 📄 License & Release Readiness

- **Status**: Production Release Stable v3.2.0
- **Build Target**: Android 15 (API 35) • Min SDK: Android 10 (API 29)
- **Play Store Ready**: Complete adaptive icons, privacy disclosures, terms of service, and zero broken routes.
