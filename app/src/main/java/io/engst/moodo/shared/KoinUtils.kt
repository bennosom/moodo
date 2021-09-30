package io.engst.moodo.shared

import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition

inline fun <reified T : Any> koinGet(noinline parameters: ParametersDefinition? = null) =
    GlobalContext.get().get<T>(parameters = parameters)

inline fun <reified T : Any> inject(noinline parameters: ParametersDefinition? = null) =
    lazy(LazyThreadSafetyMode.NONE) { koinGet<T>(parameters) }

inline fun <reified T> T.injectLogger(tag: String? = null): Lazy<Logger> =
    lazy(LazyThreadSafetyMode.NONE) { Logger.create<T>(tag) }
