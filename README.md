# Phone Controller

An Android application that transforms a smartphone into a wireless controller for Windows PCs using Kotlin and Python.

---

##  Features

-  Virtual joystick controls
-  Mouse movement support
   Keyboard input support
-  Wireless communication over UDP
-  Python server for Windows PC
-  Low-latency real-time control

---

##  Technologies Used

- Kotlin
- Android SDK
- Python
- UDP Sockets
- Android Studio

---

##  Project Structure

```
phone_controller/
├── app/
├── gradle/
├── server.py
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---
.
##  Installation
.
### Android App

1. Clone this repository.
2. Open the project in Android Studio.
3. Build and install the application on your Android phone.

### Python Server

1. Install Python 3.
2. Install the required libraries:

```bash
pip install pyautogui keyboard
```

3. Run:

```bash
python server.py
```

4. Connect your phone and PC to the same Wi-Fi network

##  Future Improvements

- Bluetooth support
- Better controller UI
- Gamepad compatibility
- Cross-platform support
- Secure connection

---

##  Author

**Charan Vasa**

GitHub:
https://github.com/giricharanvasa1

---

⭐ If you like this project, consider giving it a star.
