package ch.felixmorgner.blesensor.device

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import ch.felixmorgner.blesensor.support.unit
import java.util.*

class Sensor(fContext: Context, private val fDevice: BluetoothDevice) : BluetoothGattCallback() {

    companion object {
        private const val TAG = "Sensor"

        private val RECEPTION_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    }

    private val fGattConnection = fDevice.connectGatt(fContext, true, this)

    // BluetoothGattCallback implementation

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) = when (newState) {
        BluetoothProfile.STATE_CONNECTED -> {
            Log.i(TAG, "Connected to device")
            fGattConnection.discoverServices().unit
        }
        BluetoothProfile.STATE_DISCONNECTED -> Log.i(TAG, "Disconnected from device").unit
        else -> Unit
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) = when (status) {
        BluetoothGatt.GATT_SUCCESS -> {
            val rxService = fGattConnection.getService(RECEPTION_SERVICE_UUID)
            if (rxService != null) {
                Log.i(TAG, "Found RX service: $rxService")
            } else {
                Log.i(TAG, "No RX service found!")
            }
            fGattConnection.disconnect()
        }
        else -> Unit
    }

    // Public interface implementation

    /**
     * Access the sensor's name
     */
    val name get() = fDevice.name

}