package cn.bluemobi.dylan.step.view

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import cn.bluemobi.dylan.step.R

/**
 * Created by DylanAndroid on 2016/5/26.
 * 显示步数的圆弧
 */
class StepArcView : View {
    /**
     * 圆弧的宽度
     */
    private val borderWidth = dipToPx(14f).toFloat()

    /**
     * 画步数的数值的字体大小
     */
    private var numberTextSize = 0f

    /**
     * 步数
     */
    private var stepNumber = "0"

    /**
     * 开始绘制圆弧的角度
     */
    private val startAngle = 135f

    /**
     * 终点对应的角度和起始点对应的角度的夹角
     */
    private val angleLength = 270f

    /**
     * 所要绘制的当前步数的红色圆弧终点到起点的夹角
     */
    private var currentAngleLength = 0f

    /**
     * 动画时长
     */
    private val animationLength = 3000

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /**中心点的x坐标 */
        val centerX = ((width) / 2).toFloat()
        /**指定圆弧的外轮廓矩形区域 */
        val rectF = RectF(
            0 + borderWidth,
            borderWidth,
            2 * centerX - borderWidth,
            2 * centerX - borderWidth
        )
        /**【第一步】绘制整体的黄色圆弧 */
        drawArcYellow(canvas, rectF)
        /**【第二步】绘制当前进度的红色圆弧 */
        drawArcRed(canvas, rectF)
        /**【第三步】绘制当前进度的红色数字 */
        drawTextNumber(canvas, centerX)
        /**【第四步】绘制"步数"的红色数字 */
        drawTextStepString(canvas, centerX)
    }

    /**
     * 1.绘制总步数的黄色圆弧
     *
     * @param canvas 画笔
     * @param rectF  参考的矩形
     */
    private fun drawArcYellow(canvas: Canvas, rectF: RectF) {
        val paint = Paint()
        /** 默认画笔颜色，黄色  */
        paint.color = resources.getColor(R.color.yellow)
        /** 结合处为圆弧 */
        paint.strokeJoin = Paint.Join.ROUND
        /** 设置画笔的样式 Paint.Cap.Round ,Cap.SQUARE等分别为圆形、方形 */
        paint.strokeCap = Paint.Cap.ROUND
        /** 设置画笔的填充样式 Paint.Style.FILL  :填充内部;Paint.Style.FILL_AND_STROKE  ：填充内部和描边;  Paint.Style.STROKE  ：仅描边 */
        paint.style = Paint.Style.STROKE
        /**抗锯齿功能 */
        paint.isAntiAlias = true
        /**设置画笔宽度 */
        paint.strokeWidth = borderWidth
        /**绘制圆弧的方法
         * drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint)//画弧，
         * 参数一是RectF对象，一个矩形区域椭圆形的界限用于定义在形状、大小、电弧，
         * 参数二是起始角(度)在电弧的开始，圆弧起始角度，单位为度。
         * 参数三圆弧扫过的角度，顺时针方向，单位为度,从右中间开始为零度。
         * 参数四是如果这是true(真)的话,在绘制圆弧时将圆心包括在内，通常用来绘制扇形；如果它是false(假)这将是一个弧线,
         * 参数五是Paint对象；
         */
        canvas.drawArc(rectF, startAngle, angleLength, false, paint)
    }

    /**
     * 2.绘制当前步数的红色圆弧
     */
    private fun drawArcRed(canvas: Canvas, rectF: RectF) {
        val paintCurrent = Paint()
        paintCurrent.strokeJoin = Paint.Join.ROUND
        paintCurrent.strokeCap = Paint.Cap.ROUND //圆角弧度
        paintCurrent.style = Paint.Style.STROKE //设置填充样式
        paintCurrent.isAntiAlias = true //抗锯齿功能
        paintCurrent.strokeWidth = borderWidth //设置画笔宽度
        paintCurrent.color = resources.getColor(R.color.red) //设置画笔颜色
        canvas.drawArc(rectF, startAngle, currentAngleLength, false, paintCurrent)
    }

    /**
     * 3.圆环中心的步数
     */
    private fun drawTextNumber(canvas: Canvas, centerX: Float) {
        val vTextPaint = Paint()
        vTextPaint.textAlign = Paint.Align.CENTER
        vTextPaint.isAntiAlias = true //抗锯齿功能
        vTextPaint.textSize = numberTextSize
        val font = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        vTextPaint.typeface = font //字体风格
        vTextPaint.color = resources.getColor(R.color.red)
        val bounds_Number = Rect()
        vTextPaint.getTextBounds(stepNumber, 0, stepNumber.length, bounds_Number)
        canvas.drawText(
            stepNumber,
            centerX,
            (height / 2 + bounds_Number.height() / 2).toFloat(),
            vTextPaint
        )
    }

    /**
     * 4.圆环中心[步数]的文字
     */
    private fun drawTextStepString(canvas: Canvas, centerX: Float) {
        val vTextPaint = Paint()
        vTextPaint.textSize = dipToPx(16f).toFloat()
        vTextPaint.textAlign = Paint.Align.CENTER
        vTextPaint.isAntiAlias = true //抗锯齿功能
        vTextPaint.color = resources.getColor(R.color.grey)
        val stepString = "步数"
        val bounds = Rect()
        vTextPaint.getTextBounds(stepString, 0, stepString.length, bounds)
        canvas.drawText(
            stepString,
            centerX,
            ((height / 2) + bounds.height() + getFontHeight(numberTextSize)).toFloat(),
            vTextPaint
        )
    }

    /**
     * 获取当前步数的数字的高度
     *
     * @param fontSize 字体大小
     * @return 字体高度
     */
    fun getFontHeight(fontSize: Float): Int {
        val paint = Paint()
        paint.textSize = fontSize
        val bounds_Number = Rect()
        paint.getTextBounds(stepNumber, 0, stepNumber.length, bounds_Number)
        return bounds_Number.height()
    }

    /**
     * dip 转换成px
     *
     * @param dip
     * @return
     */
    private fun dipToPx(dip: Float): Int {
        val density = context.resources.displayMetrics.density
        return (dip * density + 0.5f * (if (dip >= 0) 1 else -1)).toInt()
    }

    /**
     * 所走的步数进度
     *
     * @param totalStepNum  设置的步数
     * @param currentCounts 所走步数
     */
    fun setCurrentCount(totalStepNum: Int, currentCounts: Int) {
        /**如果当前走的步数超过总步数则圆弧还是270度，不能成为园 */
        var currentCounts = currentCounts
        if (currentCounts > totalStepNum) {
            currentCounts = totalStepNum
        }
        /**上次所走步数占用总共步数的百分比 */
        val scalePrevious = Integer.valueOf(stepNumber).toFloat() / totalStepNum
        /**换算成弧度最后要到达的角度的长度-->弧长 */
        val previousAngleLength = scalePrevious * angleLength
        /**所走步数占用总共步数的百分比 */
        val scale = currentCounts.toFloat() / totalStepNum
        /**换算成弧度最后要到达的角度的长度-->弧长 */
        val currentAngleLength = scale * angleLength
        /**开始执行动画 */
        setAnimation(previousAngleLength, currentAngleLength, animationLength)
        stepNumber = currentCounts.toString()
        setTextSize(currentCounts)
    }

    /**
     * 为进度设置动画
     * ValueAnimator是整个属性动画机制当中最核心的一个类，属性动画的运行机制是通过不断地对值进行操作来实现的，
     * 而初始值和结束值之间的动画过渡就是由ValueAnimator这个类来负责计算的。
     * 它的内部使用一种时间循环的机制来计算值与值之间的动画过渡，
     * 我们只需要将初始值和结束值提供给ValueAnimator，并且告诉它动画所需运行的时长，
     * 那么ValueAnimator就会自动帮我们完成从初始值平滑地过渡到结束值这样的效果。
     *
     * @param start   初始值
     * @param current 结束值
     * @param length  动画时长
     */
    private fun setAnimation(start: Float, current: Float, length: Int) {
        val progressAnimator = ValueAnimator.ofFloat(start, current)
        progressAnimator.duration = length.toLong()
        progressAnimator.setTarget(currentAngleLength)
        progressAnimator.addUpdateListener(AnimatorUpdateListener { animation ->
            /**每次在初始值和结束值之间产生的一个平滑过渡的值，逐步去更新进度 */
            /**每次在初始值和结束值之间产生的一个平滑过渡的值，逐步去更新进度 */
            /**每次在初始值和结束值之间产生的一个平滑过渡的值，逐步去更新进度 */

            /**每次在初始值和结束值之间产生的一个平滑过渡的值，逐步去更新进度 */
            currentAngleLength = animation.animatedValue as Float
            invalidate()
        })
        progressAnimator.start()
    }

    /**
     * 设置文本大小,防止步数特别大之后放不下，将字体大小动态设置
     *
     * @param num
     */
    fun setTextSize(num: Int) {
        val s = num.toString()
        val length = s.length
        if (length <= 4) {
            numberTextSize = dipToPx(50f).toFloat()
        } else if (length > 4 && length <= 6) {
            numberTextSize = dipToPx(40f).toFloat()
        } else if (length > 6 && length <= 8) {
            numberTextSize = dipToPx(30f).toFloat()
        } else if (length > 8) {
            numberTextSize = dipToPx(25f).toFloat()
        }
    }
}