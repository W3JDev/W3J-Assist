# Project Aura

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Status: Implemented](https://img.shields.io/badge/status-implemented-green.svg)](https://github.com/W3JDev/W3J-Assist)

Project Aura is a privacy-first, hyper-efficient AI companion for mobile devices. It acts as your "second brain," providing real-time, context-aware assistance and knowledge during live conversations and interactions without being intrusive or distracting.

## 🚀 Implementation Status: COMPLETE

All core features have been successfully implemented and are ready for APK compilation:

- ✅ **Feature A-01**: Ambient Audio Mode with wake word detection
- ✅ **Feature V-01**: On-Demand Vision OCR with camera integration  
- ✅ **Feature UI-01**: Discreet overlay UI system
- ✅ **Feature S-01**: Local-first ephemeral storage with SQLite

## Core Features

*   **Ambient Audio Mode:** Uses a low-power, on-device wake word (Picovoice Porcupine) to activate. Ask a question, and get a discreet answer delivered privately to your headphones or on-screen overlay.
*   **On-Demand Vision:** Point your camera at a document, slide, or object. Aura uses on-device OCR (Google ML Kit) to understand what you're seeing and answer your questions about it in real time via cloud AI.
*   **Discreet Overlay UI:** All interactions are displayed in a minimal, movable, semi-transparent overlay that floats above your other apps, keeping you in the flow.
*   **Privacy-First by Design:** No raw audio or video is ever stored. Only the final, AI-generated text summaries are saved locally on your device in SQLite, under your complete control.

## How It Works: A Hybrid Architecture

Aura is built on a hybrid architecture designed specifically for the constraints of a mobile device, prioritizing battery life and performance.

1.  **On-Device (Always-On, Low-Power):** A tiny wake-word engine (Picovoice Porcupine) and an OCR engine (Google ML Kit) run locally. Their only job is to listen for a trigger (a keyword or a tap), using minimal power.
2.  **Cloud (Surgical, High-Power):** Once triggered, a small snippet of data (a few seconds of audio or a piece of extracted text) is sent to a powerful cloud AI model (Google Gemini 2.5 Flash) for analysis.
3.  **Local Storage:** The final text result is stored in a local SQLite database with Room ORM on your device. All raw data is immediately discarded.

## Technology Stack

*   **Cloud AI Model:** Google Gemini 2.5 Flash
*   **On-Device Wake Word:** Picovoice Porcupine  
*   **On-Device OCR:** Google ML Kit Text Recognition
*   **On-Device Database:** SQLite with Room ORM
*   **Platform:** Android (Kotlin, Jetpack Compose)
*   **Architecture:** MVVM with LiveData and Coroutines

## Project Structure

```
app/src/main/java/com/aura/app/
├── AuraApplication.kt          # Application class with DI setup
├── MainActivity.kt             # Main UI with permissions & controls
├── OCRActivity.kt              # Camera-based OCR interface
├── database/
│   ├── AuraDatabase.kt         # Room database configuration
│   └── SavedResponseDao.kt     # Database access object
├── models/
│   └── SavedResponse.kt        # Data model for stored responses
├── services/
│   ├── OverlayService.kt       # Floating overlay window service
│   └── WakeWordService.kt      # Background wake word detection
├── ui/
│   ├── theme/                  # Material Design 3 theming
│   └── viewmodels/             # MVVM view models
└── utils/
    ├── GeminiClient.kt         # HTTP client for Gemini API
    └── OCRProcessor.kt         # ML Kit text recognition wrapper
```

## Getting Started

### Prerequisites
- Android SDK 24+ (Android 7.0+)
- Android Studio or Gradle build tools
- Google Gemini API key
- Picovoice access key

### Building the APK

1. **Clone the repository:**
   ```bash
   git clone https://github.com/W3JDev/W3J-Assist.git
   cd W3J-Assist
   ```

2. **Configure API keys:**
   - Add your Gemini API key in `GeminiClient.kt`
   - Add your Picovoice access key in `WakeWordService.kt`

3. **Build the APK:**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Required Permissions

The app requires the following permissions for full functionality:
- 🎙️ `RECORD_AUDIO` - For wake word detection and voice commands
- 📷 `CAMERA` - For OCR text recognition  
- 🌐 `INTERNET` - For cloud AI processing
- 📱 `SYSTEM_ALERT_WINDOW` - For overlay UI
- 🔋 `FOREGROUND_SERVICE` - For background wake word detection

## Documentation

- **[Implementation Report](IMPLEMENTATION_REPORT.md)** - Complete technical documentation
- **[Screenshots](SCREENSHOTS.md)** - Visual documentation of all features  
- **[Build Demo](build_demo.sh)** - Project structure validation script

## Architecture Highlights

### Privacy-First Design
- Raw audio and image data never stored
- Only processed text responses saved locally
- All processing happens on-device or via encrypted cloud APIs
- User maintains complete control over stored data

### Performance Optimized  
- Wake word detection optimized for battery life
- Efficient SQLite queries with Room ORM
- Background services with proper lifecycle management
- Minimal network usage with request batching

### Modern Android Development
- Jetpack Compose for reactive UI development
- Material Design 3 theming and components
- MVVM architecture with dependency injection  
- Coroutines for asynchronous operations
- Proper permission handling and runtime requests

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---
*Privacy-first AI assistant - No user data, extracted text, or context is stored or used outside this session. Confidentiality standards enforced.*
