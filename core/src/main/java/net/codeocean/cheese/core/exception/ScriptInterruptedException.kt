package net.codeocean.cheese.core.exception

class ScriptInterruptedException : RuntimeException {
    constructor(cause: Throwable) : super(cause)
    constructor() : super("ScriptInterruptedException")
    companion object {
        fun causedByInterrupted(e: Throwable?): Boolean {
            var current = e
            while (current != null) {
                if (current is ScriptInterruptedException || current is InterruptedException) {
                    return true
                }
                current = current.cause
            }
            return false
        }
    }
}
