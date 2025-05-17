package net.codeocean.cheese.backend.impl

import net.codeocean.cheese.core.api.Http
import net.codeocean.cheese.core.utils.OkHttpUtils

class HttpImpl:Http {
    override fun builder(): OkHttpUtils? {
        return OkHttpUtils.builder()
    }
}