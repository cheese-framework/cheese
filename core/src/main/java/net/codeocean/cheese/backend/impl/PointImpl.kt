package net.codeocean.cheese.backend.impl

import android.graphics.Path
import android.util.Pair
import cn.vove7.andro_accessibility_api.requireBaseAccessibility
import cn.vove7.auto.core.api.click
import cn.vove7.auto.core.api.longClick
import cn.vove7.auto.core.api.playGestures
import cn.vove7.auto.core.api.swipe
import cn.vove7.auto.core.utils.AutoGestureDescription
import cn.vove7.auto.core.utils.ScreenAdapter
import net.codeocean.cheese.core.Action
import net.codeocean.cheese.core.Action.Companion.runAction
import net.codeocean.cheese.core.api.Point
import net.codeocean.cheese.core.utils.ConvertersUtils
import java.util.LinkedList

object PointImpl : Point {
    infix fun <A, B> A.t(that: B): Pair<A, B> = Pair(this, that)
    private val p = LinkedList<Pair<Int, Int>>()
    class _swipeToPoint : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.size >= 5) { "传入参数数量错误" }
            val (a, b, c, d, e) = parameters
            return try {
                val startX = a.toString().toFloat().toInt()
                val startY = b.toString().toFloat().toInt()
                val endX = c.toString().toFloat().toInt()
                val endY = d.toString().toFloat().toInt()
                val duration = e.toString().toFloat().toInt()
                swipe(startX, startY, endX, endY, duration)
            } catch (e: NumberFormatException) {
                println("类型错误")
                false
            }
        }
    }


    override fun swipeToPoint(sx: Int, sy: Int, ex: Int, ey: Int, dur: Int): Boolean {
        return runAction(_swipeToPoint(), sx, sy, ex, ey, dur) as Boolean
    }
    class _clickPoint : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.size >= 2) { "传入参数数量错误" }
            val (a, b) = parameters
            return try {
                val x = a.toString().toFloat().toInt()
                val y = b.toString().toFloat().toInt()
                click(x, y)
            } catch (e: NumberFormatException) {
                println("类型错误")
                false
            }

        }
    }

    override fun clickPoint(sx: Int, sy: Int): Boolean {
        return runAction(_clickPoint(), sx,sy) as Boolean
    }

    class _longClickPoint : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.size >= 2) { "传入参数数量错误" }
            val (a, b) = parameters
            return try {
                val x = a.toString().toFloat().toInt()
                val y = b.toString().toFloat().toInt()
                longClick(x, y)
            } catch (e: NumberFormatException) {
                println("类型错误")
                false
            }
        }
    }

    override fun longClickPoint(sx: Int, sy: Int): Boolean {
        return runAction(_longClickPoint(), sx,sy) as Boolean
    }

    class _gesture : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.size >= 2) { "传入参数数量错误" }
            val (a, b) = parameters
            return try {
                val x = (a as Number).toLong()
                val y = b as Array<Pair<Int, Int>>
                cn.vove7.auto.core.api.gesture(x, y)
            } catch (e: NumberFormatException) {
                println("类型错误")
                false
            }
        }
    }

    class _gestures : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.size >= 2) { "传入参数数量错误" }
            val (a, b) = parameters
            return try {
                val x = (a as Number).toLong()
                val y = b as Array<Array<Pair<Int, Int>>>
                cn.vove7.auto.core.api.gestures(x, y)
            } catch (e: NumberFormatException) {
                println("类型错误")
                false
            }
        }
    }
    private fun pointsToPath(points: Array<Pair<Int, Int>>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        path.moveTo(ScreenAdapter.scaleX(points[0].first), ScreenAdapter.scaleY(points[0].second))

        for (i in 1 until points.size) {
            path.lineTo(
                ScreenAdapter.scaleX(points[i].first),
                ScreenAdapter.scaleY(points[i].second)
            )
        }
        return path
    }

    class _touchDown : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.size >= 2) { "传入参数数量错误" }
            val (a, b) = parameters
            return try {
                val x = (a as Int).toLong()
                val y = b as Array<Pair<Int, Int>>
                val path = pointsToPath(y)
                playGestures(listOf(AutoGestureDescription.StrokeDescription(path, 0, x, true)))
            } catch (e: NumberFormatException) {
                println("类型错误")
                false
            }
        }
    }

    override fun touchDown(x: Int, y: Int): Boolean {
        val backup: Pair<Int, Int> = x t y
        if (!p.isEmpty()) {
            return false
        }
        p.add(backup)
        return runAction(
            _touchDown(),
            1000,
            ConvertersUtils.pairArray(backup.first, backup.second)
        ) as Boolean
    }

    class _touchMove : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.size >= 2) { "传入参数数量错误" }
            val (a, b) = parameters
            return try {
                val x = (a as Int).toLong()
                val y = b as Array<Pair<Int, Int>>
                val path = pointsToPath(y)
                playGestures(listOf(AutoGestureDescription.StrokeDescription(path, 0, x, true)))
            } catch (e: NumberFormatException) {
                println("类型错误")
                false
            }
        }
    }

    override fun touchMove(x: Int, y: Int): Boolean {
        var backup: Pair<Int, Int> = x t y
        if (!p.isEmpty()) {
            backup = p[0]
            p.clear()
        }
        p.add(x t y)
        return runAction(
            _touchMove(),
            1000,
            ConvertersUtils.pairArray(backup.first, backup.second, x, y)
        ) as Boolean
    }
    class _touchUp : Action() {
        override suspend fun run(vararg parameters: Any): Any {
            requireBaseAccessibility(true)
            require(parameters.size >= 2) { "传入参数数量错误" }
            val (a, b) = parameters
            return try {
                val x = (a as Int).toLong()
                val y = b as Array<Pair<Int, Int>>
                val path = pointsToPath(y)
                playGestures(listOf(AutoGestureDescription.StrokeDescription(path, 0, x, false)))
            } catch (e: NumberFormatException) {
                println("类型错误")
                false
            }
        }
    }


    override fun touchUp(): Boolean {
        if (p.isEmpty()) {
            return false
        }
        val x = p[0].first
        val y = p[0].second
        p.clear()
        return runAction(
            _touchUp(),
            1000,
            ConvertersUtils.pairArray(x, y)
        ) as Boolean
    }

    override fun gesture(int: Int, list: Any): Boolean {
//MutableList<String>

        return runAction(_gesture(),int,list) as Boolean
    }

    override fun gestures(int: Int, list: Any): Boolean {
        return runAction(_gestures(),int,list) as Boolean
    }


}