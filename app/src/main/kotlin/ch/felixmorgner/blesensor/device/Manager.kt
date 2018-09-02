package ch.felixmorgner.blesensor.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.util.Log
import ch.felixmorgner.blesensor.MainModel
import java.lang.RuntimeException
import java.time.Duration

class Manager(
        private val fContext: Context,
        private val fModel: MainModel,
        private val fScanTimeout: Duration = Duration.ofSeconds(10)
) : ScanCallback() {

    companion object {
        private const val TAG = "Manager"
    }

    private val fEventScheduler = Handler()
    private var fIsScanning = false
    private val fBluetoothAdapter: BluetoothAdapter
    private val fSensors = mutableMapOf<String, Sensor>()

    init {
        val bluetoothManager = fContext.getSystemService(BluetoothManager::class.java)
                ?: throw RuntimeException("Failed to acquire Bluetooth manager service!")
        fBluetoothAdapter = bluetoothManager.adapter
    }

    // ScanCallback implementation

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        Log.i(TAG, "scan result: $result")
        result?.device?.let { device ->
            if (!fSensors.contains(device.address)) {
                Log.i(TAG, "Trying to connect to sensor at address '${device.address}'")
                fSensors[device.address] = Sensor(fContext, device)
            }
        }
    }

    // Public interface implementation

    /**
     * Scan for sensors
     */
    fun scan() {
        if (fIsScanning) return
        if (!fBluetoothAdapter.isEnabled) {
            fModel.handle(MainModel.Event.RequestEnableBluetooth(fBluetoothAdapter))
            return
        }

        fBluetoothAdapter.bluetoothLeScanner.startScan(this)
        fIsScanning = true
        fEventScheduler.postDelayed({
            fBluetoothAdapter.bluetoothLeScanner.stopScan(this)
            fIsScanning = false
        }, fScanTimeout.toMillis())
    }

}