# Project Aura

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Status: In Development](https://img.shields.io/badge/status-in_development-orange.svg)](https://github.com/your-username/aura)

Project Aura is a privacy-first, hyper-efficient AI companion for mobile devices. It acts as your "second brain," providing real-time, context-aware assistance and knowledge during live conversations and interactions without being intrusive or distracting.

## Core Features

*   **Ambient Audio Mode:** Uses a low-power, on-device wake word to activate. Ask a question, and get a discreet answer delivered privately to your headphones or on-screen.
*   **On-Demand Vision:** Point your camera at a document, slide, or object. Aura uses on-device OCR to understand what you're seeing and answer your questions about it in real time.
*   **Discreet Overlay UI:** All interactions are displayed in a minimal, movable, semi-transparent overlay that floats above your other apps, keeping you in the flow.
*   **Privacy-First by Design:** No raw audio or video is ever stored. Only the final, AI-generated text summaries are saved locally on your device, under your complete control.

## How It Works: A Hybrid Architecture

Aura is built on a hybrid architecture designed specifically for the constraints of a mobile device, prioritizing battery life and performance.

1.  **On-Device (Always-On, Low-Power):** A tiny wake-word engine and an OCR engine run locally. Their only job is to listen for a trigger (a keyword or a tap), using minimal power.
2.  **Cloud (Surgical, High-Power):** Once triggered, a small snippet of data (a few seconds of audio or a piece of extracted text) is sent to a powerful cloud AI model for analysis.
3.  **Local Storage:** The final text result is stored in a local, open-source database on your device. All raw data is immediately discarded.

## Technology Stack

*   **Cloud AI Model:** Google Gemini 2.5 Flash
*   **On-Device Wake Word:** Picovoice Porcupine
*   **On-Device OCR:** Google ML Kit Text Recognition
*   **On-Device Database:** SQLite
*   **Platform (Initial):** Android (Kotlin, Jetpack Compose)

## Getting Started

> This section will be updated with build and installation instructions once the initial development is complete.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---
*No user data, extracted text, or context is stored or used outside this session. Confidentiality standards enforced.*
