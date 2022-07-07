package cn.bluemobi.dylan.step.pedometer

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.IBinder

class StepsDetectService : Service() {
    private var mSensorManager: SensorManager? = null
    private var mSensor: Sensor? = null
    private var mStepDetector: StepDetector? = null
    override fun onCreate() {
        mStepDetector = StepDetector()
        registerDetector()
        mStepDetector!!.setStepListener(object : StepListener {
            override fun onStep() {
                steps++
                if (mOnStepDetectListener != null) {
                    mOnStepDetectListener!!.onStepDetect(steps)
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        steps = 0
        return super.onStartCommand(intent, flags, startId)
    }

    private fun registerDetector() {
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensorManager!!.registerListener(
            mStepDetector,
            mSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        mOnStepDetectListener = null
        unRegisterDetector()
        steps = 0
    }

    private fun unRegisterDetector() {
        if (mStepDetector != null && mSensorManager != null) {
            mStepDetector!!.setStepListener(null)
            mSensorManager!!.unregisterListener(mStepDetector)
        }
    }

    interface OnStepDetectListener {
        fun onStepDetect(steps: Int)
    }

    @Suppress("unused")
    companion object {
        var steps = 0
        var mOnStepDetectListener: OnStepDetectListener? = null
        fun setOnStepDetectListener(mListener: OnStepDetectListener?) {
            mOnStepDetectListener = mListener
        }
    }
}