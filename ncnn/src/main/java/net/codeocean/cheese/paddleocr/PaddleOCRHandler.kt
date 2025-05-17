package net.codeocean.cheese.paddleocr

import android.graphics.Bitmap
import com.tencent.paddleocrncnn.PaddleOCRNcnn
import com.tencent.yolov8ncnn.Yolov8Ncnn

object PaddleOCRHandler {

    var paddleOCRNcnn: PaddleOCRNcnn? = null
    var isInit=false

    init {
        paddleOCRNcnn = PaddleOCRNcnn()
    }

    fun init(path: String): Boolean {
        if (isInit) return isInit
        System.out.println(">>>>>加载："+ isInit)
        isInit= paddleOCRNcnn?.InitSd(path)!!

        return isInit
    }

    fun ocr(bitmap: Bitmap): Array<out PaddleOCRNcnn.Obj1>? {
        return paddleOCRNcnn?.ocr(bitmap)
    }


}