package com.example.queue

class FifoQueue<T> {

    private class Node<T>(
        val value: T,
        var next: Node<T>? = null
    )

    private var head: Node<T>? = null
    private var tail: Node<T>? = null
    private var count: Int = 0

    fun enqueue(value: T) {
        val node = Node(value)
        val currentTail = tail

        if (currentTail == null) {
            head = node
        } else {
            currentTail.next = node
        }

        tail = node
        count++
    }

    fun dequeue(): T? {
        val currentHead = head ?: return null

        head = currentHead.next
        if (head == null) {
            tail = null
        }

        count--
        return currentHead.value
    }

    fun peek(): T? {
        return head?.value
    }

    fun isEmpty(): Boolean {
        return count == 0
    }

    fun size(): Int {
        return count
    }
}
