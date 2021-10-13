package io.engst.moodo.shared

import java.time.LocalDateTime
import java.time.ZoneId

val LocalDateTime.msecsSinceEpoch: Long
    get() = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()