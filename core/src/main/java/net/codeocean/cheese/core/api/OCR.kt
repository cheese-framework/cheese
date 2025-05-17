package net.codeocean.cheese.core.api

import android.graphics.Bitmap
import coco.cheese.mlkit.MlkitCore

import net.codeocean.cheese.paddleocr.PaddleOCRHandler

interface OCR {
    fun mlkitOcr(
        bitmap: Bitmap,
        recognizer: Int,
    ): MlkitCore.ResultType?
    fun paddleOcr(
    ): PaddleOCRHandler
}