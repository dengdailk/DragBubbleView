package com.study.dragbubbleview

import android.content.Context
import android.util.TypedValue

/**
 * @author dengdai
 * @date 2020/3/27.
 * GitHub：
 * email：291996307@qq.com
 * description：
 */
class DensityUtils private constructor() {
    companion object {
        @JvmStatic
        fun dp2px(context: Context, dpVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.resources.displayMetrics
            ).toInt()
        }

        fun sp2px(context: Context, spVal: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                spVal, context.resources.displayMetrics
            ).toInt()
        }

        fun px2dp(context: Context, pxVal: Float): Float {
            val scale = context.resources.displayMetrics.density
            return pxVal / scale
        }

        fun px2sp(context: Context, pxVal: Float): Float {
            return pxVal / context.resources.displayMetrics.scaledDensity
        }
    }

    init { /* cannot be instantiated */
        throw UnsupportedOperationException("cannot be instantiated")
    }
}