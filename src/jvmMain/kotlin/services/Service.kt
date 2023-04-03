package services

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

interface Service {
    val dependencies: List<KClass<out Service>>
        get() = emptyList()

    val scope: CoroutineScope
    var sm: ServiceManager

    fun createScope(ctx: CoroutineContext) { }
    suspend fun initialize() { }
    suspend fun cancelJobs() { }
    suspend fun teardown() { }
}