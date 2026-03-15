# DEFENDER – DefCam Android Camera Node

DefCam is an Android camera streaming application built in Kotlin using CameraX.
It acts as a mobile camera node for the DEFENDER wildlife detection system, capturing images from the device camera and streaming them to the Edge AI inference engine (Raspberry Pi) for real-time animal detection.

---

## 🚀 Project Overview

DefCam allows an Android device to function as a portable camera sensor in the DEFENDER architecture.

Instead of requiring a fixed camera connected to the Raspberry Pi, a smartphone can capture frames and stream them directly to the AI engine over the network.

The AI engine processes the images using YOLOv26 Nano, performs detection, and returns the detection count.

This enables flexible deployment of camera nodes across farms or remote locations.

---

## 🌟 Key Features

✅ **Live Camera Streaming**

Uses Android CameraX API to capture frames.

✅ **Direct Edge AI Integration**

Frames are streamed to the DEFENDER Raspberry Pi inference engine.

✅ **TCP Socket Communication**

Frames transmitted using a lightweight binary protocol.

✅ **Automatic Reconnection**

If connection drops, the client retries automatically.

✅ **Portable Camera Node**

Allows smartphones to act as mobile farm cameras.

---

## 🏗 System Role in DEFENDER Architecture

DefCam acts as a camera node in the overall DEFENDER system pipeline.

```
Android Camera (DefCam)
        ↓
JPEG Frame Capture
        ↓
TCP Socket Transmission
        ↓
Raspberry Pi AI Engine
        ↓
YOLOv26 Nano Detection
        ↓
Detection Metadata Stream
        ↓
Web / Mobile Monitoring Systems
```

This allows multiple camera nodes to feed the same central AI engine.

---

## ⚙️ Technology Stack

- Android Development
- Kotlin
- Android Studio
- Gradle
- CameraX API
- Android Camera Hardware
- TCP Socket Communication
- DataOutputStream / DataInputStream
- JPEG Frame Encoding

---

## 🛠 Prerequisites

Before building the project ensure you have:

- Android Studio (latest stable version)
- Android SDK
- JDK 11+
- A DEFENDER AI engine running on Raspberry Pi

---

## 📁 Project Structure

```
DefCam/
 ├── app/
 │   ├── src/main/
 │   │   ├── java/com/defender/defcam/
 │   │   │   └── MainActivity.kt
 │   │   ├── res/
 │   │   └── AndroidManifest.xml
 │   ├── build.gradle.kts
 │   └── proguard-rules.pro
 │
 ├── build.gradle.kts
 ├── settings.gradle.kts
 ├── gradle/
 └── gradle.properties
```

---

## 📷 Camera Streaming Workflow

The application captures frames and sends them to the AI engine.

### 1️⃣ Camera Initialization

The app requests camera permission and initializes CameraX preview and capture pipeline.

### 2️⃣ Frame Capture

Images are captured using:

`ImageCapture.takePicture()`

Frames are stored temporarily in the app cache.

### 3️⃣ JPEG Encoding

Captured frames are saved as:

`frame.jpg`

This reduces transmission bandwidth.

### 4️⃣ Frame Transmission

Frames are sent via TCP socket to the AI engine.

Protocol format:

```
[Frame Size - 4 bytes]
[JPEG Image Bytes]
```

The frame size is encoded using Little Endian byte order.

### 5️⃣ Detection Response

The AI engine responds with:

**Object Count (integer)**

Example log:

`Detections: 2`

---

## 🔌 Network Configuration

Update the engine IP inside `MainActivity.kt`:

```kotlin
private val TARGET_IP = "100.122.74.118"
private val PORT = 8888
```

This should point to the DEFENDER AI Engine (Raspberry Pi).

---

## 🏗 Frame Streaming Loop

Frames are transmitted periodically using a background loop.

capture frame
      ↓
compress to JPEG
      ↓
send over socket
      ↓
receive detection count
      ↓
repeat

Current capture interval:

`1 frame per second`

---

## 🧪 Running the App

### From Android Studio

- Open the DefCam project
- Connect Android device or start emulator
- Click Run ▶️

### From command line

```bash
./gradlew assembleDebug
```

Install on device:

```bash
./gradlew installDebug
```

---

## ⚠️ Permissions Required

The app requires:

- CAMERA
- INTERNET

Defined in `AndroidManifest.xml`.

These permissions allow the device to:

- capture images
- communicate with the AI server.

---

## 📡 Connection Reliability

The app implements automatic reconnection.

If the socket connection fails:

- Retry connection every 3 seconds

This ensures continuous streaming even in unstable networks.

---

## 🔮 Future Improvements

Potential enhancements include:

- Real-time video streaming (instead of frame capture)
- Adjustable frame rate
- Edge buffering for unstable networks
- Detection overlay preview on the device
- Multi-camera support

---

## 📬 Integration with DEFENDER Platform

DefCam works together with the main DEFENDER system:

```
DefCam (Android Camera Node)
       ↓
Edge AI Engine (Raspberry Pi)
       ↓
Flask Streaming Server
       ↓
Web Dashboard / Mobile App
       ↓
Turret Tracking System
```

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

