package com.uma.musicvk.remote

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.uma.musicvk.aidl.IBoomExportedActionCallback
import com.uma.musicvk.aidl.IExportedActionService
import musicvk.com.uma.remote.R

class MainActivity : AppCompatActivity() {

    private lateinit var inputView: EditText
    private lateinit var buttonView: Button
    private lateinit var resultView: TextView

    private var remoteService: IExportedActionService? = null
    private var serviceConnection: ServiceConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputView = findViewById(R.id.track_id_input)
        buttonView = findViewById(R.id.track_load_button)
        resultView = findViewById(R.id.track_load_result)

        buttonView.setOnClickListener { downloadTrack() }

        disableInput()
        connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    private fun connect() {
        val intent = Intent()
        intent.component = ComponentName(getString(R.string.boom_aidl_package_name), getString(R.string.boom_aidl_service_name))
        serviceConnection = Connection()
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun disconnect() {
        serviceConnection?.let { unbindService(it) }
    }

    private fun enableInput() {
        inputView.text.clear()
        inputView.isEnabled = true
        buttonView.isEnabled = true
    }

    private fun disableInput() {
        inputView.setText("Waiting for connection")
        inputView.isEnabled = false
        buttonView.isEnabled = false
    }

    private fun downloadTrack() {
        val trackId = inputView.text.toString()
        remoteService?.downloadTrack(0, trackId, Callback())
    }

    private inner class Connection : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            remoteService = IExportedActionService.Stub.asInterface(service)
            enableInput()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            remoteService = null
            disableInput()
        }
    }

    private inner class Callback : IBoomExportedActionCallback.Stub() {

        override fun onSuccess() {
            resultView.text = "Success"
        }

        override fun onError(errorCode: Int) {
            resultView.text = "Fail $errorCode"
        }
    }
}
