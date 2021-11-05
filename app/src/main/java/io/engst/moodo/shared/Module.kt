package io.engst.moodo.shared

import org.koin.dsl.module
import java.time.Clock
import java.util.Locale

val moduleShared = module {
    single { Clock.systemDefaultZone() }
    single { Locale.getDefault() }
    factory<Logger> { (tag: String?, prefix: String?) -> LogCatLogger(tag, prefix) }
}