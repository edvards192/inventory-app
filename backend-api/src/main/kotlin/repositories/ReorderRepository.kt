package com.example.repositories

import com.example.database.ItemsTable
import com.example.database.ReordersTable
import com.example.models.ReorderStatus
import com.example.models.requests.CreateReorderRequest
import com.example.models.responses.ReorderListResponse
import com.example.models.responses.ReorderResponse
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDateTime

class ReorderRepository {

    fun itemExists(itemId: Int): Boolean {
        return transaction {
            ItemsTable
                .selectAll()
                .where { ItemsTable.id eq itemId }
                .limit(1)
                .any()
        }
    }

    fun createReorder(request: CreateReorderRequest): Int? {
        return transaction {
            if (request.quantity <= 0) {
                return@transaction null
            }

            val itemExists = ItemsTable
                .selectAll()
                .where { ItemsTable.id eq request.itemId }
                .limit(1)
                .any()

            if (!itemExists) {
                return@transaction null
            }

            ReordersTable.insert {
                it[ReordersTable.itemId] = request.itemId
                it[ReordersTable.quantity] = request.quantity
                it[ReordersTable.status] = ReorderStatus.WAITING
            }[ReordersTable.id]
        }
    }

    fun getReorderById(reorderId: Int): ReorderResponse? {
        return transaction {
            joinedReorders()
                .where { ReordersTable.id eq reorderId }
                .singleOrNull()
                ?.toReorderResponse()
        }
    }

    fun getReorders(): ReorderListResponse {
        return transaction {
            val processing = activeReorders(ReorderStatus.PROCESSING)
            val waiting = activeReorders(ReorderStatus.WAITING)
            val history = joinedReorders()
                .where { ReordersTable.status eq ReorderStatus.COMPLETED }
                .orderBy(
                    ReordersTable.completedAt to SortOrder.DESC,
                    ReordersTable.id to SortOrder.DESC
                )
                .map { it.toReorderResponse() }

            ReorderListResponse(
                queue = processing + waiting,
                history = history
            )
        }
    }

    fun getActiveReorderIds(): List<Int> {
        return transaction {
            val processing = ReordersTable
                .selectAll()
                .where { ReordersTable.status eq ReorderStatus.PROCESSING }
                .orderBy(
                    ReordersTable.createdAt to SortOrder.ASC,
                    ReordersTable.id to SortOrder.ASC
                )
                .map { it[ReordersTable.id] }

            val waiting = ReordersTable
                .selectAll()
                .where { ReordersTable.status eq ReorderStatus.WAITING }
                .orderBy(
                    ReordersTable.createdAt to SortOrder.ASC,
                    ReordersTable.id to SortOrder.ASC
                )
                .map { it[ReordersTable.id] }

            processing + waiting
        }
    }

    fun getStatus(reorderId: Int): ReorderStatus? {
        return transaction {
            ReordersTable
                .selectAll()
                .where { ReordersTable.id eq reorderId }
                .singleOrNull()
                ?.get(ReordersTable.status)
        }
    }

    fun markProcessing(reorderId: Int): Boolean {
        return transaction {
            ReordersTable.update(
                {
                    (ReordersTable.id eq reorderId) and
                        (ReordersTable.status eq ReorderStatus.WAITING)
                }
            ) {
                it[ReordersTable.status] = ReorderStatus.PROCESSING
            } > 0
        }
    }

    fun markCompleted(reorderId: Int): Boolean {
        return transaction {
            ReordersTable.update(
                {
                    (ReordersTable.id eq reorderId) and
                        (ReordersTable.status eq ReorderStatus.PROCESSING)
                }
            ) {
                it[ReordersTable.status] = ReorderStatus.COMPLETED
                it[ReordersTable.completedAt] = LocalDateTime.now()
            } > 0
        }
    }

    fun hasReordersForItem(itemId: Int): Boolean {
        return transaction {
            ReordersTable
                .selectAll()
                .where { ReordersTable.itemId eq itemId }
                .limit(1)
                .any()
        }
    }

    private fun activeReorders(status: ReorderStatus): List<ReorderResponse> {
        return joinedReorders()
            .where { ReordersTable.status eq status }
            .orderBy(
                ReordersTable.createdAt to SortOrder.ASC,
                ReordersTable.id to SortOrder.ASC
            )
            .map { it.toReorderResponse() }
    }

    private fun joinedReorders() = ReordersTable
        .join(
            ItemsTable,
            joinType = JoinType.INNER,
            onColumn = ReordersTable.itemId,
            otherColumn = ItemsTable.id
        )
        .selectAll()

    private fun ResultRow.toReorderResponse(): ReorderResponse {
        return ReorderResponse(
            id = this[ReordersTable.id],
            itemId = this[ReordersTable.itemId],
            itemTitle = this[ItemsTable.title],
            quantity = this[ReordersTable.quantity],
            status = this[ReordersTable.status],
            createdAt = this[ReordersTable.createdAt].toString(),
            completedAt = this[ReordersTable.completedAt]?.toString()
        )
    }
}
