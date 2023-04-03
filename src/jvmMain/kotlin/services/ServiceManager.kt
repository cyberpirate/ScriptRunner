package services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

class ServiceManager(val ctx: CoroutineContext) {

    val unloaded = HashMap<KClass<out Service>, Service>()
    val loaded = HashMap<KClass<out Service>, Service>()
    val loadOrder = ArrayList<KClass<out Service>>()

    suspend fun load(cls: KClass<out Service>) {
        if (loaded.containsKey(cls)) return

        if (!unloaded.containsKey(cls)) {
            throw RuntimeException("${cls.simpleName} does not exist or circular dependency")
        }

        val srv = unloaded.remove(cls)!!

        srv.dependencies.forEach({
            load(it)
        })

        srv.sm = this
        srv.createScope(ctx)
        srv.initialize()

        loadOrder.add(cls)
        loaded[cls] = srv
    }

    inline fun <reified T : Service> add(srv: T) {
        unloaded[T::class] = srv
    }

    inline fun <reified T : Service> get(): T {
        return loaded[T::class] as T
    }

    suspend fun initialize() {
        while (unloaded.isNotEmpty()) {
            load(unloaded.keys.first())
        }
    }

    suspend fun teardown() {
        loadOrder.reversed().forEach({
            val srv = loaded[it]!!
            srv.cancelJobs()
            srv.teardown()
            srv.scope.cancel()
        })

        loadOrder.clear()
        unloaded.putAll(loaded)
        loaded.clear()
    }
}