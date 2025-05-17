package net.codeocean.cheese

import net.codeocean.cheese.core.Logger
import net.codeocean.cheese.screen.MainViewModel

// app 模块中
class ViewModelLogger(private val viewModel: MainViewModel) : Logger {
    override fun addLog(message: String) {
        viewModel.addLog(message)
    }
}