package cn.bluemobi.dylan.step.step.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build

/**
 * 检测手机是否支持计歩
 * Created by dylan on 2016/2/18.
 */
@Suppress("unused")
class StepCountModeDispatcher(private val context: Context) {
    private val hasSensor: Boolean

    @get:TargetApi(Build.VERSION_CODES.KITKAT)
    val isSupportStepCountSensor: Boolean
        get() = context.packageManager
            .hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)

    companion object {
        /**
         * 判断该设备是否支持计歩
         *
         * @param context
         * @return
         */
        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun isSupportStepCountSensor(context: Context): Boolean {
            // 获取传感器管理器的实例
            val sensorManager = context
                .getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            return countSensor != null || detectorSensor != null
        }
    }

    init {
        hasSensor = isSupportStepCountSensor
    }
}