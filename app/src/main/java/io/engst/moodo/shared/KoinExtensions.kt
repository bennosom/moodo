package io.engst.moodo.shared

import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

inline fun <reified T : Any> koinGet(noinline parameters: ParametersDefinition? = null) =
    GlobalContext.get().get<T>(parameters = parameters)

inline fun <reified T : Any> inject(noinline parameters: ParametersDefinition? = null) =
    lazy(LazyThreadSafetyMode.NONE) { koinGet<T>(parameters) }

inline fun <reified T : Any> Scope.getLogger(tag: String): Logger {
    return get { parametersOf(tag, logPrefix<T>()) }
}

inline fun <reified T : Any> T.injectLogger(
    tag: String? = null,
    prefix: String? = null
): Lazy<Logger> =
    lazy(LazyThreadSafetyMode.NONE) { LogCatLogger.create<T>(tag, prefix) }
