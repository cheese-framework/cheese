package net.codeocean.cheese.core.api

interface Point {
    fun swipeToPoint(sx: Int, sy: Int, ex: Int, ey: Int, dur: Int):Boolean
    fun clickPoint(sx: Int, sy: Int):Boolean
    fun longClickPoint(sx: Int, sy: Int):Boolean
    fun touchDown(x: Int, y: Int):Boolean
    fun touchMove(x: Int, y: Int):Boolean
    fun touchUp():Boolean
    fun gesture(int: Int,list: Any):Boolean
    fun gestures(int: Int,list: Any):Boolean

}