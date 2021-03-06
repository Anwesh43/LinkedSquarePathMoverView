package com.anwesh.uiprojects.squarepathmoverview

/**
 * Created by anweshmishra on 14/08/18.
 */

import android.app.Activity
import android.view.View
import android.view.MotionEvent
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.util.Log

val nodes : Int = 5

fun Canvas.drawSPMNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val sc1 : Float = Math.min(0.5f, scale) * 2
    val sc2 : Float = Math.min(0.5f, Math.max(0f, scale - 0.5f)) * 2
    val gap : Float = w / nodes
    val size : Float = gap / 3
    val x1 = gap * sc2
    val x2 = gap * sc1
    paint.color = Color.parseColor("#66BB6A")
    save()
    translate(i * gap + gap/2, h/2)
    drawRect(RectF(-size/2 + x1, -size/2, size/2 + x2, size/2), paint)
    restore()
}

class SquarePathMoverView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            this.scale += 0.05f * this.dir
            Log.d("scale is ", "$scale")
            if (Math.abs(this.scale - this.prevScale) > 1) {
                this.scale = this.prevScale + this.dir
                this.dir = 0f
                this.prevScale = this.scale
                cb(this.prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (this.dir == 0f) {
                this.dir = 1 - 2 * this.prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SPMNode(var i : Int, val state : State = State()) {

        var next : SPMNode? = null
        var prev : SPMNode? = null

        fun update(cb : (Int, Float) -> Unit) {
            this.state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            this.state.startUpdating(cb)
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SPMNode(i + 1)
                next?.prev = this
            }
        }

        init {
            addNeighbor()
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSPMNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SPMNode {
            var curr : SPMNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class LinkedSPM(var i : Int) {

        private var curr : SPMNode = SPMNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                Log.d("dir", "${dir}")
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : SquarePathMoverView) {

        private val lspm : LinkedSPM = LinkedSPM(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            lspm?.draw(canvas, paint)
            animator.animate {
                lspm.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            lspm.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : SquarePathMoverView {
            val view : SquarePathMoverView = SquarePathMoverView(activity)
            activity.setContentView(view)
            return view
        }
    }
}