package com.example.phonecontroller

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    // ✅ FIXED IP (NO EXTRA DOT)
    private val PC_IP = "192.168.1.4"
    private val PC_PORT = 5005

    private lateinit var udpSocket: DatagramSocket
    private lateinit var pcAddress: InetAddress
    private val executor = Executors.newSingleThreadExecutor()

    private lateinit var leftJoystick: View
    private lateinit var leftKnob: View
    private lateinit var rightJoystick: View
    private lateinit var rightKnob: View

    private var lastSendTime = 0L
    private val SEND_INTERVAL = 5   // 🔥 smoother

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        hideSystemUI()
        setContentView(R.layout.activity_main)

        try {
            pcAddress = InetAddress.getByName(PC_IP)
            udpSocket = DatagramSocket()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        initJoysticks()
        initButtons()
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    // ================= JOYSTICKS =================

    private fun initJoysticks() {
        leftJoystick = findViewById(R.id.leftJoystick)
        leftKnob = findViewById(R.id.leftKnob)
        rightJoystick = findViewById(R.id.rightJoystick)
        rightKnob = findViewById(R.id.rightKnob)

        setupJoystick(leftJoystick, leftKnob, "left")
        setupJoystick(rightJoystick, rightKnob, "right")
    }

    private fun setupJoystick(base: View, knob: View, idName: String) {
        base.setOnTouchListener { _, event ->

            val cx = base.width / 2f
            val cy = base.height / 2f
            val maxRadius = cx - knob.width / 2f

            when (event.action) {

                MotionEvent.ACTION_DOWN,
                MotionEvent.ACTION_MOVE -> {

                    val dx = event.x - cx
                    val dy = event.y - cy

                    val distance = sqrt(dx * dx + dy * dy)
                    val clamped = min(distance, maxRadius)
                    val angle = atan2(dy, dx)

                    val finalX = clamped * cos(angle)
                    val finalY = clamped * sin(angle)

                    knob.translationX = finalX
                    knob.translationY = finalY

                    val normX = finalX / maxRadius
                    val normY = finalY / maxRadius

                    val now = System.currentTimeMillis()
                    if (now - lastSendTime > SEND_INTERVAL) {
                        sendUdp("""{"type":"joystick","id":"$idName","x":$normX,"y":$normY}""")
                        lastSendTime = now
                    }
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    knob.translationX = 0f
                    knob.translationY = 0f
                    sendUdp("""{"type":"joystick","id":"$idName","x":0,"y":0}""")
                }
            }
            true
        }
    }

    // ================= BUTTONS =================

    private fun initButtons() {

        val buttonMap = mapOf(
            R.id.btnTriangle to "triangle",
            R.id.btnCircle to "circle",
            R.id.btnCross to "cross",
            R.id.btnSquare to "square",
            R.id.btnLUp to "up",
            R.id.btnLDown to "down",
            R.id.btnLLeft to "left",
            R.id.btnLRight to "right",
            R.id.btnL1 to "l1",
            R.id.btnL2 to "l2",
            R.id.btnR1 to "r1",
            R.id.btnR2 to "r2"
        )

        for ((id, name) in buttonMap) {
            val v = findViewById<View>(id)

            v.setOnTouchListener { view, event ->
                when (event.action) {

                    MotionEvent.ACTION_DOWN -> {
                        view.alpha = 0.5f
                        sendUdp("""{"type":"button","id":"$name","state":"down"}""")
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        view.alpha = 1f
                        sendUdp("""{"type":"button","id":"$name","state":"up"}""")
                    }
                }
                true
            }
        }
    }

    private fun sendUdp(message: String) {
        executor.execute {
            try {
                val data = message.toByteArray()
                val packet = DatagramPacket(data, data.size, pcAddress, PC_PORT)
                udpSocket.send(packet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        udpSocket.close()
        executor.shutdown()
    }
}