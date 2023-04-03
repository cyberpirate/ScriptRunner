package services

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseService: Service {

    final override lateinit var scope: CoroutineScope
        protected set

    override lateinit var sm: ServiceManager

    private val jobs = ArrayList<Job>()

    override fun createScope(ctx: CoroutineContext) {
        scope = CoroutineScope(ctx + Dispatchers.Default)
    }

    protected fun addJob(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) {
        jobs.add(scope.launch(
            context = context,
            start = start,
            block = block
        ))
    }

    override suspend fun cancelJobs() {
        jobs.reversed().forEach({
            it.cancelAndJoin()
        })
        jobs.clear()
    }
}