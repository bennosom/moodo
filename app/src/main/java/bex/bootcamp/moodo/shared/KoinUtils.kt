package bex.bootcamp.moodo.shared

import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition

inline fun <reified T : Any> koinGet(noinline parameters: ParametersDefinition? = null) =
    GlobalContext.get().get<T>(parameters = parameters)

inline fun <reified T : Any> inject(noinline parameters: ParametersDefinition? = null) =
    lazy(LazyThreadSafetyMode.NONE) { koinGet<T>(parameters) }

inline fun <reified T> T.injectLogger(): Lazy<Logger> =
    lazy(LazyThreadSafetyMode.NONE) { createLogger<T>() }
