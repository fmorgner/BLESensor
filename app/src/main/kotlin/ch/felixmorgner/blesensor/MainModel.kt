package ch.felixmorgner.blesensor

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.bluetooth.BluetoothAdapter
import ch.felixmorgner.blesensor.device.Sensor
import ch.felixmorgner.blesensor.support.LiveEvent
import ch.felixmorgner.blesensor.support.unit
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.actor

class MainModel : ViewModel() {

    sealed class Event {
        data class NewSensorFound(val sensor: Sensor) : Event()
        data class RequestEnableBluetooth(val adapter: BluetoothAdapter) : Event()
    }

    private val fSensors = MutableLiveData<List<Sensor>>().apply { value = emptyList() }

    private val fActor = actor<Event>(UI, Channel.CONFLATED) {
        for (event in this) when (event) {
            is Event.NewSensorFound -> event.sensor.let { sensor ->
                fSensors.value = fSensors.value!! + sensor
            }
            is Event.RequestEnableBluetooth -> event.adapter.let { adapter ->
                enableBluetooth.value = adapter
            }
        }
    }

    fun handle(event: Event) = fActor.offer(event).unit

    val sensors: LiveData<List<Sensor>> = fSensors

    val enableBluetooth: LiveEvent<BluetoothAdapter> = LiveEvent<BluetoothAdapter>()
}