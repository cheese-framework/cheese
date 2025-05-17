package net.codeocean.cheese.backend.impl

import android.graphics.Bitmap
import coco.cheese.mlkit.MlkitCore

import net.codeocean.cheese.core.api.OCR
import net.codeocean.cheese.core.exception.ScriptInterruptedException
import net.codeocean.cheese.paddleocr.PaddleOCRHandler

object OCRImpl:OCR {
    val CHINESE = 1
    val LATIN = 2

    override fun mlkitOcr(bitmap: Bitmap, recognizer: Int): MlkitCore.ResultType? {
        val tag = when (recognizer) {
            CHINESE -> coco.cheese.mlkit.CHINESE
            LATIN -> coco.cheese.mlkit.LATIN
            else -> return null
        }
        try {
            return MlkitCore().textRecognition(bitmap, tag)
        } catch (e: InterruptedException) {
            throw ScriptInterruptedException()
        }
    }

    override fun paddleOcr(): PaddleOCRHandler {
        return PaddleOCRHandler
    }


}