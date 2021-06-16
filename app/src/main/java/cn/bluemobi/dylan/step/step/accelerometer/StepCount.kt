package cn.bluemobi.dylan.step.step.accelerometer

/**
 * Created by dylan on 16/9/27.
 */
/*
* 根据StepDetector传入的步点"数"步子
* */
class StepCount : StepCountListener {
    private var count = 0
    private var mCount = 0
    private var mStepValuePassListener: StepValuePassListener? = null
    private var timeOfLastPeak: Long = 0
    private var timeOfThisPeak: Long = 0
    val stepDetector: StepDetector = StepDetector()

    /*
      * 连续走十步才会开始计步
      * 连续走了9步以下,停留超过3秒,则计数清空
      * */
    override fun countStep() {
        timeOfLastPeak = timeOfThisPeak
        timeOfThisPeak = System.currentTimeMillis()
        if (timeOfThisPeak - timeOfLastPeak <= 3000L) {
            when {
                count < 9 -> {
                    count++
                }
                count == 9 -> {
                    count++
                    mCount += count
                    notifyListener()
                }
                else -> {
                    mCount++
                    notifyListener()
                }
            }
        } else { //超时
            count = 1 //为1,不是0
        }
    }

    fun initListener(listener: StepValuePassListener?) {
        mStepValuePassListener = listener
    }

    private fun notifyListener() {
        if (mStepValuePassListener != null) mStepValuePassListener!!.stepChanged(mCount)
    }

    fun setSteps(initValue: Int) {
        mCount = initValue
        count = 0
        timeOfLastPeak = 0
        timeOfThisPeak = 0
        notifyListener()
    }

    init {
        stepDetector.initListener(this)
    }
}