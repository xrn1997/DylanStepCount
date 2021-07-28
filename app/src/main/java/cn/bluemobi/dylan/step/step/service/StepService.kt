package cn.bluemobi.dylan.step.step.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import cn.bluemobi.dylan.step.R
import cn.bluemobi.dylan.step.activity.HistoryActivity
import cn.bluemobi.dylan.step.activity.MainActivity
import cn.bluemobi.dylan.step.manager.ObjectBoxManager
import cn.bluemobi.dylan.step.step.UpdateUiCallBack
import cn.bluemobi.dylan.step.step.accelerometer.StepCount
import cn.bluemobi.dylan.step.step.accelerometer.StepValuePassListener
import cn.bluemobi.dylan.step.step.entity.Step
import cn.bluemobi.dylan.step.step.entity.Step_
import com.orhanobut.logger.Logger
import java.text.SimpleDateFormat
import java.util.*


class StepService : Service(), SensorEventListener {

    /**
     * 传感器管理对象
     */
    private lateinit var sensorManager: SensorManager

    /**
     * 广播接受者
     */
    private lateinit var mBatInfoReceiver: BroadcastReceiver

    /**
     * 通知管理对象
     */
    private lateinit var mNotificationManager: NotificationManager

    /**
     * 通知构建者
     */
    private var mBuilder: NotificationCompat.Builder? = null

    /**
     * 保存记步计时器
     */
    private var time: TimeCount? = null

    /**
     * 当前所走的步数
     */
    var stepCount = 0
        private set

    /**
     * 每次第一次启动记步服务时是否从系统中获取了已有的步数记录
     */
    private var hasRecord = false

    /**
     * 系统中获取到的已有的步数
     */
    private var hasStepCount = 0

    /**
     * 上一次的步数
     */
    private var previousStepCount = 0


    /**
     * 加速度传感器中获取的步数
     */
    private var mStepCount: StepCount? = null

    /**
     * IBinder对象，向Activity传递数据的桥梁
     */
    private val stepBinder = StepBinder()

    /**
     * ObjectBox step对象 用于操作数据库
     */
    private val stepBox = ObjectBoxManager.store.boxFor(Step::class.java)

    override fun onCreate() {
        super.onCreate()
        initNotification()
        initTodayData()
        initBroadcastReceiver()
        Thread { startStepDetector() }.start()
        startTimeCount()
    }

    /**
     * 获取当天日期
     *
     * @return
     */
    private val todayDate: String
        get() {
            val date = Date(System.currentTimeMillis())
            val sdf = SimpleDateFormat.getDateInstance()
            return sdf.format(date)
        }

    /**
     * 初始化通知栏
     */
    private fun initNotification() {
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("计步器服务", "通知", NotificationManager.IMPORTANCE_HIGH)
            mNotificationManager.createNotificationChannel(channel)
        }
        val fullScreenIntent = Intent(this, HistoryActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, 0)
        mBuilder = NotificationCompat.Builder(this, "计步器服务")
        val notification = mBuilder!!.setContentTitle(resources.getString(R.string.app_name))
            .setContentText("今日步数$stepCount 步")
           // .setContentIntent(pi)
            .setWhen(System.currentTimeMillis()) //通知产生的时间，会在通知信息里显示
            .setAutoCancel(false) //设置这个标志当用户单击面板就可以让通知将自动取消
            .setOngoing(true) //ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
            .setSmallIcon(R.mipmap.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()
        startForeground(notifyIdStep, notification)
    }

    /**
     * 初始化当天的步数
     */
    private fun initTodayData() {
        CURRENT_DATE = todayDate

        //获取当天的数据，用于展示
        val list = stepBox.query()
            .equal(Step_.date, CURRENT_DATE)
            .build()
            .find()
        if (list.size == 0 || list.isEmpty()) {
            stepCount = 0
        } else if (list.size == 1) {
            Log.v(TAG, "Step=" + list[0].toString())
            stepCount = list[0].step!!.toInt()
        } else {
            Log.v(TAG, "出错了！")
        }
        if (mStepCount != null) {
            mStepCount!!.setSteps(stepCount)
        }
        updateNotification()
    }

    /**
     * 注册广播
     */
    private fun initBroadcastReceiver() {
        val filter = IntentFilter()
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN)
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON)
        // 屏幕解锁广播
//        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        //监听日期变化
        filter.addAction(Intent.ACTION_DATE_CHANGED)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIME_TICK)
        mBatInfoReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                when {
                    Intent.ACTION_SCREEN_ON == action -> {
                        Log.d(TAG, "screen on")
                    }
                    Intent.ACTION_SCREEN_OFF == action -> {
                        Log.d(TAG, "screen off")
                        //改为60秒一存储
                        duration = 60000
                    }
                    Intent.ACTION_USER_PRESENT == action -> {
                        Log.d(TAG, "screen unlock")
                        //                    save();
                        //改为30秒一存储
                        duration = 30000
                    }
                    Intent.ACTION_CLOSE_SYSTEM_DIALOGS == intent.action -> {
                        Log.i(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS")
                        //保存一次
                        save()
                    }
                    Intent.ACTION_SHUTDOWN == intent.action -> {
                        Log.i(TAG, " receive ACTION_SHUTDOWN")
                        save()
                    }
                    Intent.ACTION_DATE_CHANGED == action -> { //日期变化步数重置为0
                        //                    Logger.d("重置步数" + StepDetector.CURRENT_STEP);
                        save()
                        isNewDay
                    }
                    Intent.ACTION_TIME_CHANGED == action -> {
                        //时间变化步数重置为0
                        isCall
                        save()
                        isNewDay
                    }
                    Intent.ACTION_TIME_TICK == action -> { //日期变化步数重置为0
                        isCall
                        //                    Logger.d("重置步数" + StepDetector.CURRENT_STEP);
                        save()
                        isNewDay
                    }
                }
            }
        }
        registerReceiver(mBatInfoReceiver, filter)
    }

    /**
     * 监听晚上0点变化初始化数据
     */
    private val isNewDay: Unit
        get() {
            val time = "00:00"
            if (time == SimpleDateFormat.getTimeInstance()
                    .format(Date()) || CURRENT_DATE != todayDate
            ) {
                initTodayData()
            }
        }

    /**
     * 监听时间变化提醒用户锻炼
     */
    private val isCall: Unit
        get() {
            val time = getSharedPreferences("share_date", MODE_MULTI_PROCESS).getString(
                "achieveTime",
                "21:00"
            )
            val plan = getSharedPreferences("share_date", MODE_MULTI_PROCESS).getString(
                "planWalk_QTY",
                "7000"
            )
            val remind =
                getSharedPreferences("share_date", MODE_MULTI_PROCESS).getString("remind", "1")
            Logger.d(
                """
    time=$time
    new SimpleDateFormat("HH: mm").format(new Date()))=${
                    SimpleDateFormat.getTimeInstance().format(Date())
                }
    """.trimIndent()
            )
            if ("1" == remind &&
                stepCount < plan!!.toInt() &&
                time == SimpleDateFormat.getTimeInstance().format(Date())
            ) {
                remindNotify()
            }
        }

    /**
     * 开始保存记步数据
     */
    private fun startTimeCount() {
        if (time == null) {
            time = TimeCount(duration.toLong(), 1000)
        }
        time!!.start()
        Log.d(TAG, "startTimeCount")
    }

    /**
     * 更新步数通知
     */
    private fun updateNotification() {
        //设置点击跳转
        val hangIntent = Intent(this, MainActivity::class.java)
        val hangPendingIntent =
            PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notification = mBuilder!!
            .setContentTitle(resources.getString(R.string.app_name))
            .setContentText("今日步数$stepCount 步")
            .setWhen(System.currentTimeMillis()) //通知产生的时间，会在通知信息里显示
            .setContentIntent(hangPendingIntent)
            .build()
        mNotificationManager.notify(notifyIdStep, notification)
        if (mCallback != null) {
            mCallback!!.updateUi(stepCount)
        }
        Log.d(TAG, "updateNotification()")
    }

    /**
     * UI监听器对象
     */
    private var mCallback: UpdateUiCallBack? = null

    /**
     * 注册UI更新监听
     *
     * @param paramICallback
     */
    fun registerCallback(paramICallback: UpdateUiCallBack?) {
        mCallback = paramICallback
    }

    /**
     * 记步Notification的ID
     */
    private var notifyIdStep = 1150

    /**
     * 提醒锻炼的Notification的ID
     */
    private var notifyRemindId = 2000

    /**
     * 提醒锻炼通知栏
     */
    private fun remindNotify() {
        //设置点击跳转
        val hangIntent = Intent(this, MainActivity::class.java)
        val hangPendingIntent =
            PendingIntent.getActivity(this, 0, hangIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val plan =
            getSharedPreferences("share_date", MODE_MULTI_PROCESS).getString("planWalk_QTY", "7000")
        mBuilder!!.setContentTitle("今日步数$stepCount 步")
            .setContentText("距离目标还差" + (Integer.valueOf(plan!!) - stepCount) + "步，加油！")
            .setContentIntent(hangPendingIntent)
            .setTicker(resources.getString(R.string.app_name) + "提醒您开始锻炼了") //通知首次出现在通知栏，带上升动画效果的
            .setWhen(System.currentTimeMillis()) //通知产生的时间，会在通知信息里显示
            // .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
            .setAutoCancel(true) //设置这个标志当用户单击面板就可以让通知将自动取消
            .setOngoing(false) //ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
            .setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND) //向通知添加声音、闪灯和振动效果的最简单、最一致的方式是使用当前的用户默认设置，使用defaults属性，可以组合：
            //Notification.DEFAULT_ALL  Notification.DEFAULT_SOUND 添加声音 // requires VIBRATE permission
            .setSmallIcon(R.mipmap.logo)
        mNotificationManager.notify(notifyRemindId, mBuilder!!.build())
    }


    override fun onBind(intent: Intent): IBinder {
        return stepBinder
    }

    /**
     * 向Activity传递数据的纽带
     */
    inner class StepBinder : Binder() {
        /**
         * 获取当前service对象
         *
         * @return StepService
         */
        val service: StepService
            get() = this@StepService
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * 获取传感器实例
     */
    private fun startStepDetector() {
        // 获取传感器管理器的实例
        sensorManager = this
            .getSystemService(SENSOR_SERVICE) as SensorManager
        addCountStepListener()
    }

    /**
     * 添加传感器监听
     * 1. TYPE_STEP_COUNTER API的解释说返回从开机被激活后统计的步数，当重启手机后该数据归零，
     * 该传感器是一个硬件传感器所以它是低功耗的。
     * 为了能持续的计步，请不要反注册事件，就算手机处于休眠状态它依然会计步。
     * 当激活的时候依然会上报步数。该sensor适合在长时间的计步需求。
     *
     *
     * 2.TYPE_STEP_DETECTOR翻译过来就是走路检测，
     * API文档也确实是这样说的，该sensor只用来监监测走步，每次返回数字1.0。
     * 如果需要长事件的计步请使用TYPE_STEP_COUNTER。
     */
    private fun addCountStepListener() {
        val countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        when {
            countSensor != null -> {
                stepSensorType = Sensor.TYPE_STEP_COUNTER
                Log.v(TAG, "Sensor.TYPE_STEP_COUNTER")
                sensorManager.registerListener(
                    this@StepService,
                    countSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            detectorSensor != null -> {
                stepSensorType = Sensor.TYPE_STEP_DETECTOR
                Log.v(TAG, "Sensor.TYPE_STEP_DETECTOR")
                sensorManager.registerListener(
                    this@StepService,
                    detectorSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
            else -> {
                Log.v(TAG, "Count sensor not available!")
                addBasePedometerListener()
            }
        }
    }

    /**
     * 传感器监听回调
     * 记步的关键代码
     * 1. TYPE_STEP_COUNTER API的解释说返回从开机被激活后统计的步数，当重启手机后该数据归零，
     * 该传感器是一个硬件传感器所以它是低功耗的。
     * 为了能持续的计步，请不要反注册事件，就算手机处于休眠状态它依然会计步。
     * 当激活的时候依然会上报步数。该sensor适合在长时间的计步需求。
     *
     *
     * 2.TYPE_STEP_DETECTOR翻译过来就是走路检测，
     * API文档也确实是这样说的，该sensor只用来监监测走步，每次返回数字1.0。
     * 如果需要长事件的计步请使用TYPE_STEP_COUNTER。
     *
     * @param event
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (stepSensorType == Sensor.TYPE_STEP_COUNTER) {
            //获取当前传感器返回的临时步数
            val tempStep = event.values[0].toInt()
            //首次如果没有获取手机系统中已有的步数则获取一次系统中APP还未开始记步的步数
            if (!hasRecord) {
                hasRecord = true
                hasStepCount = tempStep
            } else {
                //获取APP打开到现在的总步数=本次系统回调的总步数-APP打开之前已有的步数
                val thisStepCount = tempStep - hasStepCount
                //本次有效步数=（APP打开后所记录的总步数-上一次APP打开后所记录的总步数）
                val thisStep = thisStepCount - previousStepCount
                //总步数=现有的步数+本次有效步数
                stepCount += thisStep
                //记录最后一次APP打开到现在的总步数
                previousStepCount = thisStepCount
            }
            Logger.d("tempStep$tempStep")
        } else if (stepSensorType == Sensor.TYPE_STEP_DETECTOR) {
            if (event.values[0] == 1.0f) {
                stepCount++
            }
        }
        updateNotification()
    }

    /**
     * 通过加速度传感器来记步
     */
    private fun addBasePedometerListener() {
        mStepCount = StepCount()
        mStepCount!!.setSteps(stepCount)
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val isAvailable = sensorManager.registerListener(
            mStepCount!!.stepDetector, sensor,
            SensorManager.SENSOR_DELAY_UI
        )
        mStepCount!!.initListener(object : StepValuePassListener {
            override fun stepChanged(steps: Int) {
                stepCount = steps
                updateNotification()
            }
        })
        if (isAvailable) {
            Log.v(TAG, "加速度传感器可以使用")
        } else {
            Log.v(TAG, "加速度传感器无法使用")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    /**
     * 保存记步数据
     */
    internal inner class TimeCount(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onFinish() {
            // 如果计时器正常结束，则开始计步
            time!!.cancel()
            save()
            startTimeCount()
        }

        override fun onTick(millisUntilFinished: Long) {}
    }

    /**
     * 保存记步数据
     */
    private fun save() {
        val tempStep = stepCount
        val list = stepBox.query()
            .equal(Step_.date, CURRENT_DATE)
            .build()
            .find()
        if (list.size == 0 || list.isEmpty()) {
            val data = Step()
            data.date = CURRENT_DATE
            data.step = tempStep.toString() + ""
            stepBox.put(data)
        } else if (list.size == 1) {
            val data = list[0]
            data.step = tempStep.toString() + ""
            stepBox.put(data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //取消前台进程
        stopForeground(true)
        unregisterReceiver(mBatInfoReceiver)
        Logger.d("stepService关闭")
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    companion object {
        /**
         * 默认为30秒进行一次存储
         */
        private var duration = 30 * 1000

        /**
         * 当前的日期
         */
        private var CURRENT_DATE = ""

        /**
         * 计步传感器类型  Sensor.TYPE_STEP_COUNTER或者Sensor.TYPE_STEP_DETECTOR
         */
        private var stepSensorType = -1

        private const val TAG = "StepService"
    }
}