package com.study.dragbubbleview

import android.animation.TypeEvaluator
import android.graphics.PointF

/**
 * @author dengdai
 * @date 2020/3/27.
 * GitHub：
 * email：291996307@qq.com
 * description：
 */
class PointFEvaluator : TypeEvaluator<PointF> {
    override fun evaluate(
        fraction: Float,
        startPointF: PointF,
        endPointF: PointF
    ): PointF {
        val x = startPointF.x + fraction * (endPointF.x - startPointF.x)
        val y = startPointF.y + fraction * (endPointF.y - startPointF.y)
        return PointF(x, y)
    }
}