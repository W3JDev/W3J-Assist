# Project Aura

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Status: In Development](https://img.shields.io/badge/status-in_development-orange.svg)](https://github.com/W3JDev/W3J-Assist)

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

## Core Features

### 🎧 Ambient Audio Mode
Uses a low-power, on-device wake word to activate. Ask a question, and get a discreet answer delivered privately to your headphones or on-screen. Perfect for meetings, lectures, or any situation where you need quick information without interruption.

### 📷 On-Demand Vision
Point your camera at a document, slide, or object. Aura uses on-device OCR to understand what you're seeing and answer your questions about it in real time. Ideal for reading assistance, document analysis, and visual information processing.

### 🎯 Discreet Overlay UI
All interactions are displayed in a minimal, movable, semi-transparent overlay that floats above your other apps, keeping you in the flow. The interface adapts to your workflow without demanding your full attention.

### 🔒 Privacy-First by Design
No raw audio or video is ever stored. Only the final, AI-generated text summaries are saved locally on your device, under your complete control. Your privacy is protected by design, not by policy.

## How It Works: A Hybrid Architecture

Aura is built on a hybrid architecture designed specifically for the constraints of a mobile device, prioritizing battery life and performance.

1.  **On-Device (Always-On, Low-Power):** A tiny wake-word engine and an OCR engine run locally. Their only job is to listen for a trigger (a keyword or a tap), using minimal power.
2.  **Cloud (Surgical, High-Power):** Once triggered, a small snippet of data (a few seconds of audio or a piece of extracted text) is sent to a powerful cloud AI model for analysis.
3.  **Local Storage:** The final text result is stored in a local, open-source database on your device. All raw data is immediately discarded.

## Technology Stack

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

> **Note:** Project Aura is currently in active development. The following instructions will be updated with complete build and installation procedures once the initial development phase is complete.

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

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---
*No user data, extracted text, or context is stored or used outside this session. Confidentiality standards enforced.*
