package net.codeocean.cheese.core.api

import net.codeocean.cheese.core.utils.OkHttpUtils

interface Http {
    fun builder(): OkHttpUtils?
}