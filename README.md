# DeadLock 

**DeadLock** is an Android privacy permission auditor designed to cure "Permission Blindness." It scans installed applications, analyzes the permissions they hold, and translates complex technical access rights into a human-readable risk score. 

This application was originally developed as a comprehensive college project, complete with business case analysis, Software Requirements Specification (SRS), and UML system design.

## Features

* **Security Dashboard:** A high-level overview of your device's security status, categorizing user apps into High Risk, Medium, and Safe buckets based on their access levels.
* **Risk Scoring Algorithm:** Normalizes app permissions into a 0-10 score. It flags critical access (like reading SMS or tracking fine location) and sensitive access (like reading files) to generate an immediate threat level.
* **Human-Readable Insights:** Translates raw manifest permissions (e.g., `android.permission.RECORD_AUDIO`) into easy-to-understand explanations (e.g., "Can access your microphone").
* **Privacy 101:** An educational module that warns users about the dangers of over-privileged apps and the risks associated with "The Big Three" sensors: Camera, Microphone, and Background Location.
* **Quick Remediation:** Direct deep-linking to the Android system settings so users can instantly revoke invasive permissions.

## Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Package Management:** Standard Android `PackageManager` API for fetching installed packages and requested flags.

##How the Scoring Works

DeadLock uses a weighted scoring system to determine an app's risk level:
1.  **Critical (3 Points):** Access to the camera, microphone, fine location, contacts, call logs, and SMS.
2.  **Sensitive (2 Points):** Access to coarse location, storage/files, and phone state.
3.  **Basic (1 Point):** Internet access and network state viewing.

The total weight is calculated, normalized on a 10-point scale, and assigned a severity level:
* 🔴 **High Risk:** Score 7-10
* 🟡 **Medium Risk:** Score 4-6
* 🔵 **Low Risk/Safe:** Score 0-3

## 🚀 Getting Started

### Prerequisites
* Android Studio (Latest version recommended)
* Minimum SDK: 24 (or as defined in your `build.gradle`)
* Target SDK: 34+

### Installation
1. Clone the repository:
   ```bash
   git clone [https://github.com/yourusername/DeadLock.git](https://github.com/yourusername/DeadLock.git)
