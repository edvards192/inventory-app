package com.example.database

import org.jetbrains.exposed.v1.core.Table

object ItemsTable : Table("items") {

    val id = integer("id").autoIncrement()

    val title = varchar("title", 255)

    val ean = char("ean", 13)

    override val primaryKey = PrimaryKey(id)
}

object StorageTable : Table("storage") {

    val id = integer("id").autoIncrement()

    val itemId = integer("item_id")
        .references(ItemsTable.id)

    val warehouseId = integer("warehouse_id")
        .references(WarehousesTable.id)

    val count = integer("count")

    override val primaryKey = PrimaryKey(id)
}

object WarehousesTable : Table("warehouses") {

    val id = integer("id").autoIncrement()

    val warehouseCode = varchar("warehouse_code", 20)

    val warehouseName = varchar("warehouse_name", 255)

    override val primaryKey = PrimaryKey(id)
}

object ItemImagesTable : Table("item_images") {

    val id = integer("id").autoIncrement()

    val itemId = integer("item_id") references ItemsTable.id

    val url = text("url")

    val sortOrder = integer("sort_order")

    override val primaryKey = PrimaryKey(id)
}
