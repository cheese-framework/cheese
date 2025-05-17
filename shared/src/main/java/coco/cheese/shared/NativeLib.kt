package coco.cheese.shared

class NativeLib {

    /**
     * A native method that is implemented by the 'shared' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'shared' library on application startup.
        init {
            System.loadLibrary("shared")
        }
    }
}