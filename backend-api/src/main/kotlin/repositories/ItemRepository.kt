package com.example.repositories

import com.example.database.ItemsTable
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import com.example.models.responses.ItemResponse
import com.example.database.StorageTable
import com.example.models.responses.StorageResponse
import com.example.database.ItemImagesTable
import com.example.models.responses.ItemImageResponse
import org.jetbrains.exposed.v1.core.eq
import com.example.models.requests.CreateItemRequest
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update
import com.example.services.FileStorageService
import com.example.database.WarehousesTable
import com.example.models.responses.WarehouseResponse
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.JoinType
import com.example.models.requests.UpdateStorageRequest
import com.example.models.requests.ReplaceStorageRequest
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import java.time.LocalDateTime
import org.jetbrains.exposed.v1.core.SortOrder


class ItemRepository {

    private val fileStorageService = FileStorageService()

    private fun getStorageByItemId(itemId: Int): List<StorageResponse> {
        return StorageTable
            .join(
                WarehousesTable,
                joinType = JoinType.INNER,
                onColumn = StorageTable.warehouseId,
                otherColumn = WarehousesTable.id
            )
            .selectAll()
            .where {
                StorageTable.itemId eq itemId
            }
            .map { row ->
                StorageResponse(
                    warehouseId = row[WarehousesTable.id],
                    warehouseCode = row[WarehousesTable.warehouseCode],
                    warehouseName = row[WarehousesTable.warehouseName],
                    count = row[StorageTable.count] 
                )
            }
    }

    fun getAllItems(
        search: String = "",
        sort: String = "updatedDesc",
        stockFilter: String = "all",
        warehouseIds: List<Int> = emptyList(),
        minQuantity: Int? = null,
        maxQuantity: Int? = null,
        page: Int = 1,
        limit: Int = 25
        ): List<ItemResponse> {
        return transaction {

            var query = ItemsTable.selectAll()

            val safePage = page.coerceAtLeast(1)
            val safeLimit = limit.coerceIn(1, 100)
            val offset = (safePage - 1) * safeLimit

            if (search.isNotBlank()) {
                query = query.where {
                    ItemsTable.title.lowerCase() like "%${search.lowercase()}%"
                }
            }
            query = when (sort) {
                "nameAsc" -> query.orderBy(ItemsTable.title,SortOrder.ASC)
                "nameDesc" -> query.orderBy(ItemsTable.title,SortOrder.DESC)
                else -> query.orderBy(ItemsTable.updatedAt,SortOrder.DESC)
            }
            val filtered =
            query.map { row -> val id = row[ItemsTable.id]
                ItemResponse(
                    id = id,
                    title = row[ItemsTable.title],
                    ean = row[ItemsTable.ean],
                    createdAt = row[ItemsTable.createdAt].toString(),
                    updatedAt = row[ItemsTable.updatedAt].toString(),
                    storageLocations = getStorageByItemId(id),
                    images = getImagesByItemId(id).take(1)
                )
            }
            .filter { item -> warehouseIds.isEmpty() || item.storageLocations.any {
                    it.warehouseId in warehouseIds
                }
            }
            .filter { item -> val total =item.storageLocations.sumOf { it.count }
                (minQuantity == null || total >= minQuantity) && (maxQuantity == null || total <= maxQuantity)
            }
            .filter { item -> val total = item.storageLocations.sumOf { it.count }
                when (stockFilter) {
                    "inStock" ->
                        total > 0
                    "outOfStock" ->
                        total == 0
                    else ->
                        true
                }
            }
            filtered
                .drop(offset)
                .take(safeLimit)
        }
    }
    private fun getImagesByItemId(itemId: Int): List<ItemImageResponse> {
        return ItemImagesTable
            .selectAll()
            .where { ItemImagesTable.itemId eq itemId }
            .orderBy(ItemImagesTable.sortOrder)
            .map { row ->
                ItemImageResponse(
                    id = row[ItemImagesTable.id],
                    url = row[ItemImagesTable.url],
                    sortOrder = row[ItemImagesTable.sortOrder]
                )
            }
    }

    fun getItemById(id: Int): ItemResponse? {
        return transaction {

            ItemsTable
                .selectAll()
                .where { ItemsTable.id eq id }
                .singleOrNull()
                ?.let { row ->

                    ItemResponse(
                        id = row[ItemsTable.id],
                        title = row[ItemsTable.title],
                        ean = row[ItemsTable.ean],
                        createdAt = row[ItemsTable.createdAt].toString(),
                        updatedAt = row[ItemsTable.updatedAt].toString(),
                        storageLocations = getStorageByItemId(id),
                        images = getImagesByItemId(id)
                    )
                }
        }
    }
    fun getItemByEan(ean: String): ItemResponse? {
        return transaction {
            ItemsTable
                .selectAll()
                .where {
                    ItemsTable.ean eq ean
                }
                .singleOrNull()
                ?.let { row ->

                    val id = row[ItemsTable.id]

                    ItemResponse(
                        id = id,
                        title = row[ItemsTable.title],
                        ean = row[ItemsTable.ean],
                        createdAt = row[ItemsTable.createdAt].toString(),
                        updatedAt = row[ItemsTable.updatedAt].toString(),
                        storageLocations = getStorageByItemId(id),
                        images = getImagesByItemId(id)
                    )
                }
        }
    }
    fun createItem(request: CreateItemRequest): Int {
        return transaction {

            ItemsTable.insert { row ->

                row[ItemsTable.title] = request.title
                row[ItemsTable.ean] = request.ean
                row[ItemsTable.createdAt] = LocalDateTime.now()
                row[ItemsTable.updatedAt] = LocalDateTime.now()
            }[ItemsTable.id]
        }
    } 
    fun updateItem(id: Int, request: CreateItemRequest): Boolean {

        return transaction {

            val updatedRows = ItemsTable.update({ ItemsTable.id eq id }) { row ->

                row[ItemsTable.title] = request.title
                row[ItemsTable.ean] = request.ean
                row[ItemsTable.updatedAt] = LocalDateTime.now()
            }

            updatedRows > 0
        }
    }
    
    fun deleteItem(id: Int): Boolean {
        

    return transaction {
        val imageUrls =
            ItemImagesTable
                .selectAll()
                .where { ItemImagesTable.itemId eq id }
                .map { it[ItemImagesTable.url] }

            imageUrls.forEach { url ->
                fileStorageService.deleteFile(url)
            }

            ItemImagesTable.deleteWhere {
                ItemImagesTable.itemId eq id
            }

            StorageTable.deleteWhere {
                StorageTable.itemId eq id
            }

            ItemsTable.deleteWhere {
                ItemsTable.id eq id
            } > 0
        }
    }
    fun addImage(
        itemId: Int,
        imageUrl: String,
        sortOrder: Int
        ) {
        transaction {
            ItemImagesTable.insert {

                it[ItemImagesTable.itemId] = itemId
                it[url] = imageUrl
                it[ItemImagesTable.sortOrder] = sortOrder
            }
            ItemsTable.update({ ItemsTable.id eq itemId }) {
                it[ItemsTable.updatedAt] = LocalDateTime.now()
            }
        }
    }
    fun deleteImage(imageId: Int): Boolean {

    return transaction {

        val image =
            ItemImagesTable
                .selectAll()
                .where {
                    ItemImagesTable.id eq imageId
                }
                .singleOrNull()

        if (image == null) {
            return@transaction false
        }
        val imageUrl = image[ItemImagesTable.url]
        val itemId = image[ItemImagesTable.itemId]
        fileStorageService.deleteFile(imageUrl)
        val deleted = ItemImagesTable.deleteWhere {
                ItemImagesTable.id eq imageId
            } > 0
        if (deleted) {
            ItemsTable.update(
                { ItemsTable.id eq itemId }
            ) {
                it[ItemsTable.updatedAt] = LocalDateTime.now()
            }
        }
        deleted
    }
    }

    fun getWarehouses(): List<WarehouseResponse> {
    return transaction {

        WarehousesTable
            .selectAll()
            .map { row ->

                WarehouseResponse(
                    id = row[WarehousesTable.id],
                    code = row[WarehousesTable.warehouseCode],
                    name = row[WarehousesTable.warehouseName]
                )
            }
        }
    } 
    fun addOrUpdateStorage(itemId: Int,warehouseId: Int,count: Int) {
        transaction {
            if (count == 0) {
                StorageTable.deleteWhere {
                    (StorageTable.itemId eq itemId) and
                    (StorageTable.warehouseId eq warehouseId)
                }
                return@transaction
            }
            val existing =
                StorageTable
                    .selectAll()
                    .where {
                        (StorageTable.itemId eq itemId) and
                        (StorageTable.warehouseId eq warehouseId)
                    }
                    .singleOrNull()
            if (existing == null) {
                StorageTable.insert {
                    it[StorageTable.itemId] = itemId
                    it[StorageTable.warehouseId] = warehouseId
                    it[StorageTable.count] = count
                }
            } else {
                StorageTable.update(
                    {
                        (StorageTable.itemId eq itemId) and
                        (StorageTable.warehouseId eq warehouseId)
                    }
                ) {
                    it[StorageTable.count] = count
                }
            }
        }
    }
    fun replaceStorage(itemId: Int,request: ReplaceStorageRequest) {
        transaction {
            StorageTable.deleteWhere {
                StorageTable.itemId eq itemId
            }
            request.storageLocations
                .filter {
                    it.count > 0
                }
                .forEach { storage ->
                    StorageTable.insert {
                        it[StorageTable.itemId] = itemId
                        it[warehouseId] = storage.warehouseId
                        it[count] = storage.count
                    }
                }
                ItemsTable.update({ ItemsTable.id eq itemId }) {
                    it[ItemsTable.updatedAt] = LocalDateTime.now()
                }
        }
    }
}
