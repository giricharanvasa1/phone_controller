import socket
import json
import pyautogui
import keyboard

pyautogui.FAILSAFE = False

UDP_IP = "0.0.0.0"
UDP_PORT = 5005

SENSITIVITY = 35
SMOOTHING = 0.7

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((UDP_IP, UDP_PORT))

print("🔥 Server running...")

last_dx, last_dy = 0, 0

while True:
    try:
        data, addr = sock.recvfrom(1024)
        msg = json.loads(data.decode())

        print(msg)  # 🔍 DEBUG (you can remove later)

        # ================= JOYSTICK =================
        if msg["type"] == "joystick":

            # 🎯 RIGHT JOYSTICK (MOUSE AIM)
            if msg["id"] == "right":
                x = msg["x"]
                y = msg["y"]

                dx = x * SENSITIVITY
                dy = y * SENSITIVITY

                # 🔥 SMOOTHING
                dx = last_dx * SMOOTHING + dx * (1 - SMOOTHING)
                dy = last_dy * SMOOTHING + dy * (1 - SMOOTHING)

                last_dx, last_dy = dx, dy

                pyautogui.moveRel(dx, dy, duration=0)

            # 🎮 LEFT JOYSTICK (WASD)
            elif msg["id"] == "left":

                if msg["y"] < -0.2:
                    keyboard.press("w")
                else:
                    keyboard.release("w")

                if msg["y"] > 0.2:
                    keyboard.press("s")
                else:
                    keyboard.release("s")

                if msg["x"] < -0.2:
                    keyboard.press("a")
                else:
                    keyboard.release("a")

                if msg["x"] > 0.2:
                    keyboard.press("d")
                else:
                    keyboard.release("d")

        # ================= BUTTONS =================
        elif msg["type"] == "button":

            bid = msg["id"]
            state = msg["state"]

            # 🔫 L1 → SHOOT
            if bid == "l1":
                if state == "down":
                    pyautogui.mouseDown()
                else:
                    pyautogui.mouseUp()

            # 🎯 R1 → AIM
            elif bid == "r1":
                if state == "down":
                    pyautogui.mouseDown(button="right")
                else:
                    pyautogui.mouseUp(button="right")

            # 🏃 R2 → SPRINT
            elif bid == "r2":
                if state == "down":
                    pyautogui.keyDown("shift")
                else:
                    pyautogui.keyUp("shift")

            # 🧎 L2 → CROUCH
            elif bid == "l2":
                if state == "down":
                    pyautogui.keyDown("ctrl")
                else:
                    pyautogui.keyUp("ctrl")

            # ⬆️⬇️⬅️➡️ ARROWS (FIXED)
            elif bid in ["up", "down", "left", "right"]:
                if state == "down":
                    pyautogui.keyDown(bid)
                else:
                    pyautogui.keyUp(bid)

            # 🎮 FACE BUTTONS
            else:
                key_map = {
                    "triangle": "space",  # jump
                    "circle": "c",        # crouch
                    "square": "r",        # reload
                    "cross": "e"          # interact
                }

                key = key_map.get(bid)
                if key:
                    if state == "down":
                        pyautogui.keyDown(key)
                    else:
                        pyautogui.keyUp(key)

    except Exception as e:
        print("❌ Error:", e)