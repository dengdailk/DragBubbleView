package com.study.dragbubbleview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.study.dragbubbleview.DensityUtils.Companion.dp2px
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * @author dengdai
 * @date 2020/3/27.
 * GitHub：
 * email：291996307@qq.com
 * description：
 */
class DragBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val mBubblePaint: Paint
    private val mBezierPath: Path
    private val mTextPaint: Paint
    private val mTextRect: Rect
    private val mExplosionPaint: Paint
    private val mExplosionRect: Rect
    /* 黏连小圆的半径 */
    private var mCircleRadius: Float
    /* 手指拖拽气泡的半径 */
    private val mBubbleRadius: Float
    /* 气泡的颜色 */
    private val mBubbleColor: Int
    /* 气泡消息的文本 */
    private var mText: String?
    /* 气泡消息文本的字体大小 */
    private val mTextSize: Float
    /* 气泡消息文本的颜色 */
    private val mTextColor: Int
    /* 黏连小圆的圆心横坐标 */
    private var mCircleCenterX = 0f
    /* 黏连小圆的圆心纵坐标 */
    private var mCircleCenterY = 0f
    /* 手指拖拽气泡的圆心横坐标 */
    private var mBubbleCenterX = 0f
    /* 手指拖拽气泡的圆心纵坐标 */
    private var mBubbleCenterY = 0f
    /* 两圆圆心的间距 */
    private var d = 0f
    /* 两圆圆心间距的最大距离，超出此值黏连小圆消失 */
    private val maxD: Float
    /* 黏连小圆的贝塞尔曲线起点横坐标 */
    private var mCircleStartX = 0f
    /* 黏连小圆的贝塞尔曲线起点纵坐标 */
    private var mCircleStartY = 0f
    /* 手指拖拽气泡的贝塞尔曲线终点横坐标 */
    private var mBubbleEndX = 0f
    /* 手指拖拽气泡的贝塞尔曲线终点纵坐标 */
    private var mBubbleEndY = 0f
    /* 手指拖拽气泡的贝塞尔曲线起点横坐标 */
    private var mBubbleStartX = 0f
    /* 手指拖拽气泡的贝塞尔曲线起点纵坐标 */
    private var mBubbleStartY = 0f
    /* 黏连小圆的贝塞尔曲线终点横坐标 */
    private var mCircleEndX = 0f
    /* 黏连小圆的贝塞尔曲线终点纵坐标 */
    private var mCircleEndY = 0f
    /* 贝塞尔曲线控制点横坐标 */
    private var mControlX = 0f
    /* 贝塞尔曲线控制点纵坐标 */
    private var mControlY = 0f
    /* 气泡爆炸的图片id数组 */
    private val mExplosionDrawables = intArrayOf(
        R.drawable.explosion_one, R.drawable.explosion_two
        , R.drawable.explosion_three, R.drawable.explosion_four, R.drawable.explosion_five
    )
    /* 气泡爆炸的bitmap数组 */
    private val mExplosionBitmaps: Array<Bitmap?>
    /* 气泡爆炸当前进行到第几张 */
    private var mCurExplosionIndex = 0
    /* 气泡爆炸动画是否开始 */
    private var mIsExplosionAnimStart = false
    /* 气泡的状态 */
    private var mState: Int
    /* 气泡状态的监听 */
    private var mOnBubbleStateListener: OnBubbleStateListener? = null
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            measuredDimension(widthMeasureSpec),
            measuredDimension(heightMeasureSpec)
        )
    }

    private fun measuredDimension(measureSpec: Int): Int {
        var result: Int
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        if (mode == MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = (2 * mBubbleRadius).toInt()
            if (mode == MeasureSpec.AT_MOST) {
                result = min(result, size)
            }
        }
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initData(w, h)
    }

    private fun initData(w: Int, h: Int) { //设置圆心坐标
        mBubbleCenterX = w / 2.toFloat()
        mBubbleCenterY = h / 2.toFloat()
        mCircleCenterX = mBubbleCenterX
        mCircleCenterY = mBubbleCenterY
        mState = STATE_DEFAULT
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画拖拽气泡
        if (mState != STATE_DISMISS) {
            canvas.drawCircle(mBubbleCenterX, mBubbleCenterY, mBubbleRadius, mBubblePaint)
        }
        if (mState == STATE_DRAG && d < maxD - maxD / 4) { //画黏连小圆
            canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mBubblePaint)
            //计算控制点坐标，为两圆圆心连线的中点
            mControlX = (mBubbleCenterX + mCircleCenterX) / 2
            mControlY = (mBubbleCenterY + mCircleCenterY) / 2
            //计算两条二阶贝塞尔曲线的起点和终点
            val sin = (mBubbleCenterY - mCircleCenterY) / d
            val cos = (mBubbleCenterX - mCircleCenterX) / d
            mCircleStartX = mCircleCenterX - mCircleRadius * sin
            mCircleStartY = mCircleCenterY + mCircleRadius * cos
            mBubbleEndX = mBubbleCenterX - mBubbleRadius * sin
            mBubbleEndY = mBubbleCenterY + mBubbleRadius * cos
            mBubbleStartX = mBubbleCenterX + mBubbleRadius * sin
            mBubbleStartY = mBubbleCenterY - mBubbleRadius * cos
            mCircleEndX = mCircleCenterX + mCircleRadius * sin
            mCircleEndY = mCircleCenterY - mCircleRadius * cos
            //画二阶贝赛尔曲线
            mBezierPath.reset()
            mBezierPath.moveTo(mCircleStartX, mCircleStartY)
            mBezierPath.quadTo(mControlX, mControlY, mBubbleEndX, mBubbleEndY)
            mBezierPath.lineTo(mBubbleStartX, mBubbleStartY)
            mBezierPath.quadTo(mControlX, mControlY, mCircleEndX, mCircleEndY)
            mBezierPath.close()
            canvas.drawPath(mBezierPath, mBubblePaint)
        }
        //画消息个数的文本
        if (mState != STATE_DISMISS && !TextUtils.isEmpty(mText)) {
            mTextPaint.getTextBounds(mText, 0, mText!!.length, mTextRect)
            canvas.drawText(
                mText!!,
                mBubbleCenterX - mTextRect.width() / 2,
                mBubbleCenterY + mTextRect.height() / 2,
                mTextPaint
            )
        }
        if (mIsExplosionAnimStart && mCurExplosionIndex < mExplosionDrawables.size) { //设置气泡爆炸图片的位置
            mExplosionRect[(mBubbleCenterX - mBubbleRadius).toInt(), (mBubbleCenterY - mBubbleRadius).toInt(), (mBubbleCenterX + mBubbleRadius).toInt()] =
                (mBubbleCenterY + mBubbleRadius).toInt()
            //根据当前进行到爆炸气泡的位置index来绘制爆炸气泡bitmap
            canvas.drawBitmap(
                mExplosionBitmaps[mCurExplosionIndex]!!,
                null,
                mExplosionRect,
                mExplosionPaint
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (mState != STATE_DISMISS) {
                parent.requestDisallowInterceptTouchEvent(true)
                d = hypot(
                    event.x - mBubbleCenterX.toDouble(),
                    event.y - mBubbleCenterY.toDouble()
                ).toFloat()
                mState = if (d < mBubbleRadius + maxD / 4) { //当指尖坐标在圆内的时候，才认为是可拖拽的
//一般气泡比较小，增加(maxD/4)像素是为了更轻松的拖拽
                    STATE_DRAG
                } else {
                    STATE_DEFAULT
                }
            }
            MotionEvent.ACTION_MOVE -> if (mState != STATE_DEFAULT) {
                parent.requestDisallowInterceptTouchEvent(true)
                mBubbleCenterX = event.x
                mBubbleCenterY = event.y
                //计算气泡圆心与黏连小球圆心的间距
                d = hypot(
                    mBubbleCenterX - mCircleCenterX.toDouble(),
                    mBubbleCenterY - mCircleCenterY.toDouble()
                ).toFloat()
                //                float d = (float) Math.sqrt(Math.pow(mBubbleCenterX - mCircleCenterX, 2) + Math.pow(mBubbleCenterY - mCircleCenterY, 2));
                if (mState == STATE_DRAG) { //如果可拖拽
//间距小于可黏连的最大距离
                    if (d < maxD - maxD / 4) { //减去(maxD/4)的像素大小，是为了让黏连小球半径到一个较小值快消失时直接消失
                        mCircleRadius = mBubbleRadius - d / 8 //使黏连小球半径渐渐变小
                        if (mOnBubbleStateListener != null) {
                            mOnBubbleStateListener!!.onDrag()
                        }
                    } else { //间距大于于可黏连的最大距离
                        mState = STATE_MOVE //改为移动状态
                        if (mOnBubbleStateListener != null) {
                            mOnBubbleStateListener!!.onMove()
                        }
                    }
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
                if (mState == STATE_DRAG) { //正在拖拽时松开手指，气泡恢复原来位置并颤动一下
                    setBubbleRestoreAnim()
                } else if (mState == STATE_MOVE) { //正在移动时松开手指
//如果在移动状态下间距回到两倍半径之内，我们认为用户不想取消该气泡
                    if (d < 2 * mBubbleRadius) { //那么气泡恢复原来位置并颤动一下
                        setBubbleRestoreAnim()
                    } else { //气泡消失
                        setBubbleDismissAnim()
                    }
                }
            }
        }
        return true
    }

    /**
     * 设置气泡复原的动画
     */
    private fun setBubbleRestoreAnim() {
        val anim = ValueAnimator.ofObject(
            PointFEvaluator(),
            PointF(mBubbleCenterX, mBubbleCenterY),
            PointF(mCircleCenterX, mCircleCenterY)
        )
        anim.duration = 500
        //自定义Interpolator差值器达到颤动效果
        anim.interpolator = TimeInterpolator { input ->
            //http://inloop.github.io/interpolator/
            val f = 0.571429f
            (2.0.pow(-4 * input.toDouble()) * sin((input - f / 4) * (2 * Math.PI) / f) + 1).toFloat()
        }
        anim.addUpdateListener { animation ->
            val curPoint = animation.animatedValue as PointF
            mBubbleCenterX = curPoint.x
            mBubbleCenterY = curPoint.y
            invalidate()
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) { //动画结束后状态改为默认
                mState = STATE_DEFAULT
                if (mOnBubbleStateListener != null) {
                    mOnBubbleStateListener!!.onRestore()
                }
            }
        })
        anim.start()
    }

    /**
     * 设置气泡消失的动画
     */
    private fun setBubbleDismissAnim() {
        mState = STATE_DISMISS //气泡改为消失状态
        mIsExplosionAnimStart = true
        if (mOnBubbleStateListener != null) {
            mOnBubbleStateListener!!.onDismiss()
        }
        //做一个int型属性动画，从0开始，到气泡爆炸图片数组个数结束
        val anim = ValueAnimator.ofInt(0, mExplosionDrawables.size)
        anim.interpolator = LinearInterpolator()
        anim.duration = 500
        anim.addUpdateListener { animation ->
            //拿到当前的值并重绘
            mCurExplosionIndex = animation.animatedValue as Int
            invalidate()
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) { //动画结束后改变状态
                mIsExplosionAnimStart = false
            }
        })
        anim.start()
    }

    /**
     * 气泡状态的监听器
     */
    interface OnBubbleStateListener {
        /**
         * 拖拽气泡
         */
        fun onDrag()

        /**
         * 移动气泡
         */
        fun onMove()

        /**
         * 气泡恢复原来位置
         */
        fun onRestore()

        /**
         * 气泡消失
         */
        fun onDismiss()
    }

    /**
     * 设置气泡状态的监听器
     */
    fun setOnBubbleStateListener(onBubbleStateListener: OnBubbleStateListener?) {
        mOnBubbleStateListener = onBubbleStateListener
    }

    /**
     * 设置气泡消息个数文本
     *
     * @param text 消息个数文本
     */
    fun setText(text: String?) {
        mText = text
        invalidate()
    }

    /**
     * 重新生成气泡
     */
    fun reCreate() {
        initData(width, height)
        invalidate()
    }

    companion object {
        /* 默认，无法拖拽 */
        private const val STATE_DEFAULT = 0x00
        /* 拖拽 */
        private const val STATE_DRAG = 0x01
        /* 移动 */
        private const val STATE_MOVE = 0x02
        /* 消失 */
        private const val STATE_DISMISS = 0x03
    }

    init {
        val ta =
            context.obtainStyledAttributes(attrs, R.styleable.DragBubbleView, defStyleAttr, 0)
        mBubbleRadius = ta.getDimension(
            R.styleable.DragBubbleView_bubbleRadius,
            dp2px(context, 12f).toFloat()
        )
        mBubbleColor =
            ta.getColor(R.styleable.DragBubbleView_bubbleColor, Color.RED)
        mText = ta.getString(R.styleable.DragBubbleView_text)
        mTextSize = ta.getDimension(
            R.styleable.DragBubbleView_textSize,
            dp2px(context, 12f).toFloat()
        )
        mTextColor = ta.getColor(R.styleable.DragBubbleView_textColor, Color.WHITE)
        mState = STATE_DEFAULT
        mCircleRadius = mBubbleRadius
        maxD = 8 * mBubbleRadius
        ta.recycle()
        mBubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBubblePaint.color = mBubbleColor
        mBubblePaint.style = Paint.Style.FILL
        mBezierPath = Path()
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint.color = mTextColor
        mTextPaint.textSize = mTextSize
        mTextRect = Rect()
        mExplosionPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mExplosionPaint.isFilterBitmap = true
        mExplosionRect = Rect()
        mExplosionBitmaps = arrayOfNulls(mExplosionDrawables.size)
        for (i in mExplosionDrawables.indices) { //将气泡爆炸的drawable转为bitmap
            val bitmap =
                BitmapFactory.decodeResource(resources, mExplosionDrawables[i])
            mExplosionBitmaps[i] = bitmap
        }
    }
}