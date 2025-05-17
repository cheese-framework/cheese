package net.codeocean.cheese.backend.impl

import android.graphics.Bitmap
import com.tencent.yolov8ncnn.Yolov8Ncnn
import net.codeocean.cheese.core.api.Yolo
import net.codeocean.cheese.core.utils.ConvertersUtils
import net.codeocean.cheese.yolo.YoloHandler

object YoloImpl:Yolo {
    override fun detect(
        bitmap: Bitmap,
        path: String,
        list:  ArrayList<Any>,
        cpugpu: Int
    ): Array<Yolov8Ncnn.Obj?> {
        return YoloHandler.detect(bitmap,path,(ConvertersUtils.arrayToArrayList(list).filterIsInstance<String>().toTypedArray()),cpugpu)
    }

    override fun getSpeed(): Double {
        return  YoloHandler.getSpeed()
    }

    override fun draw(objects: Array<Yolov8Ncnn.Obj>?, b: Bitmap): Bitmap {
        return   YoloHandler.draw(objects,b)
    }
}