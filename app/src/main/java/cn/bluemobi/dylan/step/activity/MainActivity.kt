package cn.bluemobi.dylan.step.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.bluemobi.dylan.step.R
import cn.bluemobi.dylan.step.step.UpdateUiCallBack
import cn.bluemobi.dylan.step.step.service.StepService
import cn.bluemobi.dylan.step.step.utils.SharedPreferencesUtils
import cn.bluemobi.dylan.step.view.StepArcView

/**
 * 记步主页
 */
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var tvData: TextView? = null
    private var cc: StepArcView? = null
    private var tvSet: TextView? = null
    private var tvIsSupport: TextView? = null
    private var sp: SharedPreferencesUtils? = null
    private fun assignViews() {
        tvData = findViewById(R.id.tv_data)
        cc = findViewById(R.id.cc)
        tvSet = findViewById(R.id.tv_set)
        tvIsSupport = findViewById(R.id.tv_isSupport)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        assignViews()
        initData()
        addListener()
    }

    private fun addListener() {
        tvSet!!.setOnClickListener(this)
        tvData!!.setOnClickListener(this)
    }

    private fun initData() {
        sp = SharedPreferencesUtils(this)
        //获取用户设置的计划锻炼步数，没有设置过的话默认7000
        val planWalkQTY = sp!!.getParam("planWalk_QTY", "7000") as String?
        //设置当前步数为0
        cc!!.setCurrentCount(planWalkQTY!!.toInt(), 0)
        tvIsSupport!!.text = "计步中..."
        setupService()
    }

    private var isBind = false

    /**
     * 开启计步服务
     */
    private fun setupService() {
        val intent = Intent(this, StepService::class.java)
        isBind = bindService(intent, conn, BIND_AUTO_CREATE)
            startService(intent);
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private var conn: ServiceConnection = object : ServiceConnection {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val stepService = (service as StepService.StepBinder).service
            //设置初始化数据
            val planWalkQTY = sp!!.getParam("planWalk_QTY", "7000") as String?
            cc!!.setCurrentCount(planWalkQTY!!.toInt(), stepService.stepCount)

            //设置步数监听回调
            stepService.registerCallback(object : UpdateUiCallBack {
                override fun updateUi(stepCount: Int) {
                    val temp = sp!!.getParam("planWalk_QTY", "7000") as String?
                    cc!!.setCurrentCount(temp!!.toInt(), stepCount)
                }
            })
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        override fun onServiceDisconnected(name: ComponentName) {}
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_set -> startActivity(Intent(this, SetPlanActivity::class.java))
            R.id.tv_data -> startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (isBind) {
            unbindService(conn)
        }
    }
}