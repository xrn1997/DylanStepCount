package cn.bluemobi.dylan.step.pedometer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

/*
*  Pedometer - Android App
*  Copyright (C) 2009 Levente Bag i
*
*  This program is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*  
*/ /**
 * Detects steps and notifies all listeners (that implement StepListener).
 * @author Levente Bag i
 * @todo REFACTOR: SensorListener is deprecated
 */
class StepDetector : SensorEventListener {
    private var mLimit = 10f
    private val mLastValues = FloatArray(3 * 2)
    private val mScale = FloatArray(2)
    private val mYOffset: Float
    private val mLastDirections = FloatArray(3 * 2)
    private val mLastExtremes = arrayOf(FloatArray(3 * 2), FloatArray(3 * 2))
    private val mLastDiff = FloatArray(3 * 2)
    private var mLastMatch = -1
    private var mListener: StepListener? = null

    fun setStepListener(sl: StepListener?) {
        mListener = sl
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensor = event.sensor
        synchronized(this) {
            val j = if (sensor.type == Sensor.TYPE_ACCELEROMETER) 1 else 0
            if (j == 1) {
                var vSum = 0f
                for (i in 0..2) {
                    val v = mYOffset + event.values[i] * mScale[j]
                    vSum += v
                }
                val k = 0
                val v = vSum / 3
                val direction =
                    (if (v > mLastValues[k]) 1 else if (v < mLastValues[k]) -1 else 0).toFloat()
                if (direction == -mLastDirections[k]) {
                    val extType = if (direction > 0) 0 else 1
                    mLastExtremes[extType][k] = mLastValues[k]
                    val diff =
                        abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k])
                    if (diff > mLimit) {
                        val isAlmostAsLargeAsPrevious = diff > mLastDiff[k] * 2 / 3
                        val isPreviousLargeEnough = mLastDiff[k] > diff / 3
                        val isNotContra = mLastMatch != 1 - extType
                        mLastMatch =
                            if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                                if (mListener != null) {
                                    mListener!!.onStep()
                                }
                                extType
                            } else {
                                -1
                            }
                    }
                    mLastDiff[k] = diff
                }
                mLastDirections[k] = direction
                mLastValues[k] = v
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    init {
        val h = 480
        mYOffset = h * 0.5f
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)))
        mScale[1] = -(h * 0.5f * (1.0f / SensorManager.MAGNETIC_FIELD_EARTH_MAX))
    }
}