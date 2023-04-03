package services

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

interface Event

class EventService: BaseService() {

    val listenerMutex = Mutex()
    val listeners = HashMap<KClass<out Event>, CopyOnWriteArrayList<suspend (Event) -> Unit>>()

    suspend inline fun <reified T: Event> listen(noinline listener: suspend (T) -> Unit): suspend () -> Unit = withContext(scope.coroutineContext) {
        listenerMutex.withLock {
            listeners.getOrPut(T::class, {CopyOnWriteArrayList()})
        }.add(listener as suspend (Event) -> Unit)

        return@withContext {
            listenerMutex.withLock {
                listeners[T::class]?.remove(listener)
            }
        }
    }

    suspend inline fun <reified T: Event> send(event: T) = withContext(scope.coroutineContext) {
        listenerMutex.withLock {
            listeners[T::class]
        }?.forEach({
            it(event)
        })
    }

    suspend inline fun <reified T: Event> observe(noinline listener: (T) -> Unit) = withContext(scope.coroutineContext) {
        val listenerDisposable = listen(listener)

        while(isActive) {
            try {
                delay(100_000)
            } catch(_: CancellationException) { }
        }

        listenerDisposable()
    }
}