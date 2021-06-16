package cn.bluemobi.dylan.step.activity

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.bluemobi.dylan.step.R
import cn.bluemobi.dylan.step.manager.ObjectBoxManager
import cn.bluemobi.dylan.step.step.entity.Step
import com.orhanobut.logger.Logger

/**
 * @author  Yuan Dl
 * @date 2016/10/18.
 */
open class HistoryActivity : AppCompatActivity() {
    private var layoutTitleBar: LinearLayout? = null
    private var ivLeft: ImageView? = null
    private var ivRight: ImageView? = null
    private fun assignViews() {
        layoutTitleBar = findViewById(R.id.layout_titlebar)
        ivLeft = findViewById(R.id.iv_left)
        ivRight = findViewById(R.id.iv_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_history)
        assignViews()
        ivLeft!!.setOnClickListener { finish() }
        initData()
    }

    private fun initData() {
        val stepData = ObjectBoxManager.store.boxFor(Step::class.java).all
        Logger.d("stepData's=$stepData")
    }

    private fun setEmptyView(listView: ListView?): TextView{
        val emptyView = TextView(this)
        emptyView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        emptyView.text = "暂无数据！"
        emptyView.gravity = Gravity.CENTER
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        emptyView.visibility = View.GONE
        (listView!!.parent as ViewGroup).addView(emptyView)
        listView.emptyView = emptyView
        return emptyView
    }
}