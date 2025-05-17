package net.codeocean.cheese.backend.impl

import net.codeocean.cheese.core.api.Zip
import net.codeocean.cheese.core.utils.ZipUtils

object ZipImpl:Zip {
    override fun compress(srcFilePath: String, destFilePath: String, password: String): Boolean {
       return ZipUtils.compress(srcFilePath,destFilePath,password)
    }

    override fun decompress(zipFilePath: String, destFilePath: String, password: String): Boolean {
        return ZipUtils.decompress(zipFilePath,destFilePath,password)
    }
}