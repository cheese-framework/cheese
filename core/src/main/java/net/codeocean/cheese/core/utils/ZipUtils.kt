package net.codeocean.cheese.core.utils


import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.util.Zip4jConstants
import java.io.File
import java.lang.ref.WeakReference

object ZipUtils {

    /**
     * 使用zip4j库压缩一个文件或者文件夹
     * @param srcFilePath 需要压缩的文件或者文件夹的路径
     * @param destFilePath 压缩后的zip文件的路径
     * @param password 加密密码，如果不加密可以为null
     * @throws ZipException
     */
    fun compress(srcFilePath: String, destFilePath: String, password: String): Boolean {


        return try {
            val parameters = ZipParameters()
            parameters.compressionMethod = Zip4jConstants.COMP_DEFLATE
            parameters.compressionLevel = Zip4jConstants.DEFLATE_LEVEL_NORMAL
            parameters.isEncryptFiles = true
            parameters.encryptionMethod = Zip4jConstants.ENC_METHOD_STANDARD
            parameters.setPassword(password)
            val zipFile = ZipFile(destFilePath)
            val file = File(srcFilePath)
            if (file.isDirectory()) {
                zipFile.addFolder(file, parameters)
            } else if (file.isFile()) {
                zipFile.addFile(file, parameters)
            }
            true
        } catch (e: ZipException) {
            false
        }


    }

    /**
     * 使用zip4j库解压一个文件或者文件夹
     * @param zipFilePath 需要解压的zip文件的路径
     * @param destFilePath 解压后的文件或者文件夹的路径
     * @param password 解压密码，如果不加密可以为null
     * @throws ZipException
     */
    fun decompress(zipFilePath: String, destFilePath: String, password: String): Boolean {
        return try {
            val zipFile = ZipFile(zipFilePath)
            if (zipFile.isEncrypted) {
                zipFile.setPassword(password)
            }
            zipFile.extractAll(destFilePath)
            true
        } catch (e: ZipException) {
            e.printStackTrace()
            false
        }


    }

}