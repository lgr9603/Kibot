package com.takturstudio.kibot.client

import android.annotation.TargetApi
import android.app.AlertDialog
import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import me.aflak.bluetooth.Bluetooth
import me.aflak.bluetooth.DiscoveryCallback


@TargetApi(Build.VERSION_CODES.KITKAT)
class MainActivity : FlutterActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private val channel = "Kibot/client"
    private var bluetooth: Bluetooth = Bluetooth(this)
    private val bluetoothName: String = "KIBOT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.addFlags(524288)
        GeneratedPluginRegistrant.registerWith(this)

        MethodChannel(flutterView, channel).setMethodCallHandler { call, result ->
            when (call.method.toString()) {
                "bluetoothInit" -> {
                    try {
                        Log.d(TAG, "Request: Bluetooth Init")
                        bluetoothInit()
                        result.success(null)
                    } catch (e: Exception) {
                        Log.d(this.channel, e.toString())
                        result.error(e.toString(), null, null)
                    }
                }
                "bluetoothWrite" -> {
                    try {
                        bluetoothWrite(call.arguments.toString())
                        result.success(null)
                    } catch (e: Exception) {
                        Log.d(this.channel, e.toString())
                        result.error(e.toString(), null, null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onUserLeaveHint() {
        // INFO: DO NOT EDIT THIS SECTION
        // startActivity(Intent(this, MainActivity::class.java))
    }

    private fun bluetoothInit() {
        bluetooth.onStart()
        bluetooth.enable()
        bluetooth.setDiscoveryCallback(object : DiscoveryCallback {
            override fun onDiscoveryStarted() {}
            override fun onDiscoveryFinished() {}
            override fun onDeviceFound(device: BluetoothDevice) {
                Log.d(TAG, "Bluetooth Found: name=${device.name} address=${device.address} " +
                        "uuids=${device.uuids}")
            }

            override fun onDevicePaired(device: BluetoothDevice) {
                Log.d(TAG, "Bluetooth Paired: name=${device.name} address=${device.address} " +
                        "uuids=${device.uuids}")
            }

            override fun onDeviceUnpaired(device: BluetoothDevice) {}
            override fun onError(message: String) {}
        })
        Log.d(TAG, "Trying to connect slave-bluetooth($bluetoothName)")
        val devices = bluetooth.pairedDevices
        val device = devices[0]
        Log.d(TAG, "Paired bluetooth devices: $devices\nConnecting to $device...")
        device.createBond()
        device.setPin(byteArrayOf(1234.toByte()))
        bluetooth.connectToDevice(device)
    }

    private fun bluetoothWrite(str: String) {
        Log.d(TAG, "Bluetooth connected:(${bluetooth.isConnected})")
        if (!bluetooth.isConnected) {
            showDialog("현재 Kibot 과 연결할 수 없습니다.")
            return
        }
        try {
            bluetooth.send(str)
        } catch (e: NullPointerException) {
            throw UninitializedPropertyAccessException("Kibot 과 연결하는데 실패했습니다.")
        }
    }

    private fun showDialog(msg: String, title: String = "Kibot client") {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton("OK", null)
        builder.show()
    }
}
