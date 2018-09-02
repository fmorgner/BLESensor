package ch.felixmorgner.blesensor

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import ch.felixmorgner.blesensor.R.layout.activity_main
import ch.felixmorgner.blesensor.device.Manager

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BLESensor"

        private const val COARSE_LOCATION_REQUEST_CODE = 0;
        private const val REQUEST_BLUETOOTH_ENABLE_CODE = 1;
    }

    private lateinit var fSensorManager: Manager
    private lateinit var fModel: MainModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        fModel = ViewModelProviders.of(this).get(MainModel::class.java)
        fModel.sensors.observe(this, Observer { Log.i(TAG, "sensors changed: ${it}") })
        fModel.enableBluetooth.observe(this, Observer {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, REQUEST_BLUETOOTH_ENABLE_CODE)
        })

        fSensorManager = Manager(this, fModel)

        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), COARSE_LOCATION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) = when (requestCode) {
        COARSE_LOCATION_REQUEST_CODE -> fSensorManager.scan()
        else -> Unit
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = when (requestCode) {
        REQUEST_BLUETOOTH_ENABLE_CODE -> {
            fSensorManager.scan()
        }
        else -> Unit
    }

}
