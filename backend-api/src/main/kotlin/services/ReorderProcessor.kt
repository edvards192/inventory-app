package com.example.services

import com.example.queue.FifoQueue
import com.example.repositories.ReorderRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ReorderProcessor(
    private val repository: ReorderRepository
) {

    private val queue = FifoQueue<Int>()
    private val mutex = Mutex()

    private val processingTimeMs = 30_000L
    private val idleCheckMs = 1_000L

    fun start(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            var restored = false

            while (isActive) {
                try {
                    if (!restored) {
                        val activeIds = repository.getActiveReorderIds()
                        mutex.withLock {
                            activeIds.forEach { queue.enqueue(it) }
                        }
                        restored = true
                    }

                    val reorderId = mutex.withLock { queue.peek() }

                    if (reorderId == null) {
                        delay(idleCheckMs)
                        continue
                    }

                    repository.markProcessing(reorderId)
                    delay(processingTimeMs)
                    repository.markCompleted(reorderId)

                    mutex.withLock { queue.dequeue() }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    delay(idleCheckMs)
                }
            }
        }
    }

    suspend fun enqueue(reorderId: Int) {
        mutex.withLock {
            queue.enqueue(reorderId)
        }
    }
}
