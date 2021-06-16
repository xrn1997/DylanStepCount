package cn.bluemobi.dylan.step.manager

import android.content.Context
import cn.bluemobi.dylan.step.step.entity.MyObjectBox
import io.objectbox.BoxStore

/**
 * @author xrn1997
 * @date 2021/6/14
 */
object ObjectBoxManager {
    lateinit var  store:BoxStore
        private set

    fun init(context: Context){
        store= MyObjectBox.builder()
            .androidContext(context.applicationContext)
            .build()
    }
}