# Project Aura

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
 copilot/fix-3
[![Status: Implemented](https://img.shields.io/badge/status-implemented-green.svg)](https://github.com/W3JDev/W3J-Assist)

[![Status: In Development](https://img.shields.io/badge/status-in_development-orange.svg)](https://github.com/W3JDev/W3J-Assist)
 Lets-Coin

## Table of Contents

- [Project Overview & Vision](#project-overview--vision)
- [Core Features](#core-features)
- [How It Works: A Hybrid Architecture](#how-it-works-a-hybrid-architecture)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Privacy & Security](#privacy--security)
- [License](#license)

## Project Overview & Vision

Project Aura is a privacy-first, hyper-efficient AI companion for mobile devices that revolutionizes how we interact with artificial intelligence in our daily lives. It acts as your intelligent "second brain," providing real-time, context-aware assistance and knowledge during live conversations and interactions without being intrusive or distracting.

**Our Vision:** To create an AI assistant that seamlessly integrates into your workflow, respects your privacy, and enhances your productivity without demanding your full attention or compromising your personal data.

## 🚀 Implementation Status: COMPLETE

All core features have been successfully implemented and are ready for APK compilation:

- ✅ **Feature A-01**: Ambient Audio Mode with wake word detection
- ✅ **Feature V-01**: On-Demand Vision OCR with camera integration  
- ✅ **Feature UI-01**: Discreet overlay UI system
- ✅ **Feature S-01**: Local-first ephemeral storage with SQLite

## Core Features

 copilot/fix-3
*   **Ambient Audio Mode:** Uses a low-power, on-device wake word (Picovoice Porcupine) to activate. Ask a question, and get a discreet answer delivered privately to your headphones or on-screen overlay.
*   **On-Demand Vision:** Point your camera at a document, slide, or object. Aura uses on-device OCR (Google ML Kit) to understand what you're seeing and answer your questions about it in real time via cloud AI.
*   **Discreet Overlay UI:** All interactions are displayed in a minimal, movable, semi-transparent overlay that floats above your other apps, keeping you in the flow.
*   **Privacy-First by Design:** No raw audio or video is ever stored. Only the final, AI-generated text summaries are saved locally on your device in SQLite, under your complete control.

### 🎧 Ambient Audio Mode
Uses a low-power, on-device wake word to activate. Ask a question, and get a discreet answer delivered privately to your headphones or on-screen. Perfect for meetings, lectures, or any situation where you need quick information without interruption.

### 📷 On-Demand Vision
Point your camera at a document, slide, or object. Aura uses on-device OCR to understand what you're seeing and answer your questions about it in real time. Ideal for reading assistance, document analysis, and visual information processing.

### 🎯 Discreet Overlay UI
All interactions are displayed in a minimal, movable, semi-transparent overlay that floats above your other apps, keeping you in the flow. The interface adapts to your workflow without demanding your full attention.

### 🔒 Privacy-First by Design
No raw audio or video is ever stored. Only the final, AI-generated text summaries are saved locally on your device, under your complete control. Your privacy is protected by design, not by policy.
 Lets-Coin

## How It Works: A Hybrid Architecture

Aura is built on a hybrid architecture designed specifically for the constraints of a mobile device, prioritizing battery life and performance.

1.  **On-Device (Always-On, Low-Power):** A tiny wake-word engine (Picovoice Porcupine) and an OCR engine (Google ML Kit) run locally. Their only job is to listen for a trigger (a keyword or a tap), using minimal power.
2.  **Cloud (Surgical, High-Power):** Once triggered, a small snippet of data (a few seconds of audio or a piece of extracted text) is sent to a powerful cloud AI model (Google Gemini 2.5 Flash) for analysis.
3.  **Local Storage:** The final text result is stored in a local SQLite database with Room ORM on your device. All raw data is immediately discarded.

## Technology Stack

 copilot/fix-3
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

Our technology choices prioritize privacy, efficiency, and seamless user experience:

### AI & Machine Learning
*   **Cloud AI Model:** Google Gemini 2.5 Flash - For high-accuracy natural language processing and reasoning
*   **On-Device Wake Word:** Picovoice Porcupine - Ultra-low power wake word detection
*   **On-Device OCR:** Google ML Kit Text Recognition - Offline text extraction from images

### Data & Storage
*   **On-Device Database:** SQLite - Local, secure storage of processed results
*   **No Cloud Storage:** Raw audio/video data is never stored or transmitted

### Platform & Development
*   **Platform (Initial):** Android (API 24+)
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Architecture:** MVVM with Clean Architecture principles

## Getting Started

 **Note:** Project Aura is currently in active development. The following instructions will be updated with complete build and installation procedures once the initial development phase is complete.

### Prerequisites
- Android device (API level 24+)
- Android Studio Arctic Fox or later
- Kotlin 1.8+
- Google Cloud account (for Gemini AI integration)
- Picovoice account (for wake word detection)

### Development Setup
1. Clone the repository
2. Set up API keys for Google Gemini and Picovoice
3. Build and install on your Android device

### Current Development Status
- ✅ Architecture design completed
- ✅ Technology stack selected
- 🔄 Core implementation in progress
- ⏳ Initial prototype development
- ⏳ Testing and optimization phase

*Detailed setup instructions, API documentation, and contribution guidelines will be added as development progresses.*

## Privacy & Security

Privacy is at the core of Project Aura's design philosophy:

### Data Protection
- **No Raw Data Storage:** Audio recordings and camera captures are processed in real-time and immediately discarded
- **Local Processing First:** OCR and wake word detection happen entirely on your device
- **Minimal Cloud Interaction:** Only processed text snippets are sent to cloud AI, never raw audio or images
- **User Control:** All generated summaries are stored locally in an open database under your complete control

### Security Measures
- **Open Source Database:** Uses SQLite for transparent, auditable local storage
- **Encrypted Transmission:** All cloud communications use industry-standard encryption
- **No Persistent Sessions:** Each interaction is independent with no cross-session data retention
- **Device-First Architecture:** Critical functions remain operational even without internet connectivity

### Compliance
- Designed with privacy regulations in mind (GDPR, CCPA)
- No user profiling or behavioral tracking
- Complete data portability and deletion capabilities
 Lets-Coin

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---
*Privacy-first AI assistant - No user data, extracted text, or context is stored or used outside this session. Confidentiality standards enforced.*
