package cn.bluemobi.dylan.step.activity

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.bluemobi.dylan.step.R
import cn.bluemobi.dylan.step.step.utils.SharedPreferencesUtils
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author  Yuan Dl
 * @date 2016/10/18.
 */
class SetPlanActivity : AppCompatActivity(), View.OnClickListener {
    private var sp: SharedPreferencesUtils? = null
    private var layoutTitleBar: LinearLayout? = null
    private var ivLeft: ImageView? = null
    private var ivRight: ImageView? = null
    private var tvStepNumber: EditText? = null
    private var cbRemind: CheckBox? = null
    private var tvRemindTime: TextView? = null
    private var btnSave: Button? = null
    private var walkQty: String? = null
    private var remind: String? = null
    private var achieveTime: String? = null
    private fun assignViews() {
        layoutTitleBar = findViewById(R.id.layout_titlebar)
        ivLeft = findViewById(R.id.iv_left)
        ivRight = findViewById(R.id.iv_right)
        tvStepNumber = findViewById(R.id.tv_step_number)
        cbRemind = findViewById(R.id.cb_remind)
        tvRemindTime = findViewById(R.id.tv_remind_time)
        btnSave = findViewById(R.id.btn_save)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ac_exercise_plan)
        assignViews()
        initData()
        addListener()
    }

    private fun initData() { //获取锻炼计划
        sp = SharedPreferencesUtils(this)
        val planWalkQTY = sp!!.getParam("planWalk_QTY", "7000") as String?
        val remind = sp!!.getParam("remind", "1") as String?
        val achieveTime = sp!!.getParam("achieveTime", "20:00") as String?
        if (planWalkQTY!!.isNotEmpty()) {
            if ("0" == planWalkQTY) {
                tvStepNumber!!.setText(getString(R.string.seven_thousand))
            } else {
                tvStepNumber!!.setText(planWalkQTY)
            }
        }
        if (remind!!.isNotEmpty()) {
            if ("0" == remind) {
                cbRemind!!.isChecked = false
            } else if ("1" == remind) {
                cbRemind!!.isChecked = true
            }
        }
        if (achieveTime!!.isNotEmpty()) {
            tvRemindTime!!.text = achieveTime
        }
    }

    private fun addListener() {
        ivLeft!!.setOnClickListener(this)
        ivRight!!.setOnClickListener(this)
        btnSave!!.setOnClickListener(this)
        tvRemindTime!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_left -> finish()
            R.id.btn_save -> save()
            R.id.tv_remind_time -> showTimeDialog1()
        }
    }

    private fun save() {
        walkQty = tvStepNumber!!.text.toString().trim { it <= ' ' }
        //        remind = "";
        remind = if (cbRemind!!.isChecked) {
            "1"
        } else {
            "0"
        }
        achieveTime = tvRemindTime!!.text.toString().trim { it <= ' ' }
        if (walkQty!!.isEmpty() || "0" == walkQty) {
            sp!!.setParam("planWalk_QTY", "7000")
        } else {
            sp!!.setParam("planWalk_QTY", walkQty!!)
        }
        sp!!.setParam("remind", remind!!)
        if (achieveTime!!.isEmpty()) {
            sp!!.setParam("achieveTime", "21:00")
            achieveTime = "21:00"
        } else {
            sp!!.setParam("achieveTime", achieveTime!!)
        }
        finish()
    }

    private fun showTimeDialog1() {
        val calendar = Calendar.getInstance(Locale.CHINA)
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val minute = calendar[Calendar.MINUTE]
        //        String time = tvRemindTime.getText().toString().trim();
        val df: DateFormat = SimpleDateFormat.getTimeInstance()
        //        Date date = null;
//        try {
//            date = df.parse(time);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        if (null != date) {
//            calendar.setTime(date);
//        }
        TimePickerDialog(this, { _, calendarHourOfDay, calendarMinute ->
            calendar[Calendar.HOUR_OF_DAY] = calendarHourOfDay
            calendar[Calendar.MINUTE] =  calendarMinute
            val reMainTime =
                calendar[Calendar.HOUR_OF_DAY].toString() + ":" + calendar[Calendar.MINUTE]
            var date: Date? = null
            try {
                date = df.parse(reMainTime)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            if (null != date) {
                calendar.time = date
                tvRemindTime!!.text = df.format(date)
            }
        }, hour, minute, true).show()
    }
}