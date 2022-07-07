package cn.bluemobi.dylan.step.step.entity

import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import kotlinx.parcelize.Parcelize

/**
 * @author dylan
 * @author xrn1997
 * @date 2021/6/16
 */
@Parcelize
@Entity
class Step(
    @Id var id: Long = 0,
    var date: String? = null,
    var step: String? = null
) : Parcelable