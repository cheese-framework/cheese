package net.codeocean.cheese.core.api

import android.graphics.Bitmap
import com.tencent.yolov8ncnn.Yolov8Ncnn

interface Yolo {
    fun detect(bitmap: Bitmap, path: String, list: ArrayList<Any>, cpugpu: Int): Array<Yolov8Ncnn.Obj?>
    fun getSpeed(): Double
    fun draw(objects: Array<Yolov8Ncnn.Obj>?, b: Bitmap): Bitmap
}