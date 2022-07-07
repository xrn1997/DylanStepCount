package cn.bluemobi.dylan.step.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import cn.bluemobi.dylan.step.manager.ObjectBoxManager

/**
 * @author  Yuan Dl
 * @date 2016/10/18.
 */
class MyApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        ObjectBoxManager.init(context)
    }
}