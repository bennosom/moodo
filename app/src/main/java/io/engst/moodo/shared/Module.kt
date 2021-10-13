package io.engst.moodo.shared

import org.koin.dsl.module

val moduleShared = module {
    factory<Logger> { (tag: String?, prefix: String?) -> LogCatLogger(tag, prefix) }
}