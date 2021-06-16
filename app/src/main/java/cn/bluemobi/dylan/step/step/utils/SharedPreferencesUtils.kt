package cn.bluemobi.dylan.step.step.utils

import android.content.Context

/**
 * SharedPreferences的一个工具类，调用setParam就能保存String, Integer, Boolean, Float,
 * Long类型的参数 同样调用getParam就能获取到保存在手机里面的数据
 *
 * @author dylan
 */
@Suppress("unused")
class SharedPreferencesUtils {
    private var context: Context? = null

    /**
     * 保存在手机里面的文件名
     */
    private var fileName = "share_date"

    // public static SharedPreferencesUtils getInstances(String fileName) {
    // FILE_NAME = fileName;
    // if (sharedPreferencesUtils == null) {
    // synchronized (SharedPreferencesUtils.class) {
    // if (sharedPreferencesUtils == null) {
    // sharedPreferencesUtils = new SharedPreferencesUtils();
    // }
    // }
    // }
    // return sharedPreferencesUtils;
    // }
    constructor(FILE_NAME: String) {
        this.fileName = FILE_NAME
    }

    constructor(context: Context?) {
        this.context = context
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param key
     * @param object
     */
    fun setParam(key: String?, `object`: Any) {
        val type = `object`.javaClass.simpleName
        val sp = context!!.getSharedPreferences(
            fileName,
            Context.MODE_MULTI_PROCESS
        )
        val editor = sp.edit()
        when (type) {
            "String" -> {
                editor.putString(key, `object`.toString())
            }
            "Integer" -> {
                editor.putInt(key, (`object` as Int))
            }
            "Boolean" -> {
                editor.putBoolean(key, (`object` as Boolean))
            }
            "Float" -> {
                editor.putFloat(key, (`object` as Float))
            }
            "Long" -> {
                editor.putLong(key, (`object` as Long))
            }
        }
        editor.apply()
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @param defaultObject
     * @return
     */
    fun getParam(key: String?, defaultObject: Any): Any? {
        val type = defaultObject.javaClass.simpleName
        val sp = context!!.getSharedPreferences(
            fileName,
            Context.MODE_PRIVATE
        )
        when (type) {
            "String" -> {
                return sp.getString(key, defaultObject as String)
            }
            "Integer" -> {
                return sp.getInt(key, (defaultObject as Int))
            }
            "Boolean" -> {
                return sp.getBoolean(key, (defaultObject as Boolean))
            }
            "Float" -> {
                return sp.getFloat(key, (defaultObject as Float))
            }
            "Long" -> {
                return sp.getLong(key, (defaultObject as Long))
            }
            else -> return null
        }
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param key
     * @return
     */
    // Delete
    fun remove(key: String?) {
        val sp = context!!.getSharedPreferences(
            fileName,
            Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.remove(key)
        editor.apply()
    }

    fun clear() {
        val sp = context!!.getSharedPreferences(
            fileName,
            Context.MODE_PRIVATE
        )
        val editor = sp.edit()
        editor.clear()
        editor.apply()
    }
}