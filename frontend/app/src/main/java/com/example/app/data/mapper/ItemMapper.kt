package com.example.app.data.mapper

import com.example.app.data.remote.dto.ItemResponse
import com.example.app.data.remote.dto.WarehouseResponse
import com.example.app.domain.model.Item
import com.example.app.domain.model.ItemImage
import com.example.app.domain.model.Storage
import com.example.app.domain.model.Warehouse

fun ItemResponse.toDomain() = Item(
    id = id,
    title = title,
    ean = ean,
    storageLocations =
        storageLocations.map {
            Storage(
                warehouseId = it.warehouseId,
                warehouseCode = it.warehouseCode,
                warehouseName = it.warehouseName,
                count = it.count
            )
        },
    images = images.map {
        ItemImage(
            id = it.id,
            url = it.url,
            sortOrder = it.sortOrder
        )
    }
)
fun WarehouseResponse.toDomain() =
    Warehouse(
        id = id,
        code = code,
        name = name
    )