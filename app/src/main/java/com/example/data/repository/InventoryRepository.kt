package com.example.data.repository

import com.example.data.dao.InventoryDao
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WarehouseEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val dao: InventoryDao) {

    val allWarehouses: Flow<List<WarehouseEntity>> = dao.getAllWarehouses()
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProducts()
    val allCategories: Flow<List<String>> = dao.getAllCategories()
    val lowStockProducts: Flow<List<ProductEntity>> = dao.getLowStockProducts()
    val lowStockCount: Flow<Int> = dao.getLowStockCount()
    val recentMovements: Flow<List<InventoryMovementEntity>> = dao.getRecentMovements()

    val transferMovements: Flow<List<InventoryMovementEntity>> = dao.getTransferMovements()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = dao.searchProducts(query)

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        dao.getProductsByCategory(category)

    suspend fun getProductByBarcode(barcode: String): ProductEntity? =
        dao.getProductByBarcode(barcode)

    suspend fun getProductById(id: Long): ProductEntity? =
        dao.getProductById(id)

    suspend fun insertProduct(product: ProductEntity): Long =
        dao.insertProduct(product)

    suspend fun updateProduct(product: ProductEntity) =
        dao.updateProduct(product)

    /**
     * بررسی امکان حذف کالا:
     * در صورت وجود فاکتور فعال یا گردش، حذف غیرمجاز است.
     */
    suspend fun canDeleteProduct(productId: Long): Pair<Boolean, String> {
        val invoiceCount = dao.countInvoiceItemsForProduct(productId)
        if (invoiceCount > 0) {
            return Pair(false, "امکان حذف این کالا وجود ندارد زیرا در $invoiceCount فاکتور ثبت شده و گردش مالی دارد.")
        }
        val activeMovements = dao.countActiveMovementsForProduct(productId)
        if (activeMovements > 0) {
            return Pair(false, "امکان حذف این کالا وجود ندارد زیرا دارای $activeMovements گردش انبار (خرید، فروش یا حواله) است.")
        }
        return Pair(true, "")
    }

    suspend fun deleteProductSafely(product: ProductEntity): Pair<Boolean, String> {
        return try {
            val check = canDeleteProduct(product.id)
            if (!check.first) {
                return check
            }
            dao.deleteMovementsForProduct(product.id)
            dao.deleteProduct(product)
            Pair(true, "کالای «${product.name}» با موفقیت حذف گردید.")
        } catch (e: Exception) {
            Pair(false, "امکان حذف این کالا به دلیل داشتن وابستگی در سایر اسناد وجود ندارد: ${e.localizedMessage}")
        }
    }

    suspend fun insertWarehouse(warehouse: WarehouseEntity): Long =
        dao.insertWarehouse(warehouse)

    suspend fun updateWarehouse(warehouse: WarehouseEntity) =
        dao.updateWarehouse(warehouse)

    suspend fun setWarehouseAsDefault(warehouseId: Long) {
        dao.clearDefaultWarehouses()
        dao.setWarehouseAsDefault(warehouseId)
    }

    suspend fun canDeleteWarehouse(warehouseId: Long): Pair<Boolean, String> {
        val totalStock = dao.getTotalStockInWarehouse(warehouseId)
        if (totalStock > 0.001) {
            return Pair(false, "امکان حذف انبار وجود ندارد زیرا دارای موجودی فعال کالا ($totalStock واحد) است. لطفاً ابتدا کالاها را با حواله انتقال به انبار دیگری منتقل نمایید.")
        }
        val movementsCount = dao.countMovementsForWarehouse(warehouseId)
        if (movementsCount > 0) {
            return Pair(false, "امکان حذف این انبار وجود ندارد زیرا دارای سابقه و تاریخچه گردش انبار است.")
        }
        return Pair(true, "")
    }

    suspend fun deleteWarehouseSafely(warehouse: WarehouseEntity): Pair<Boolean, String> {
        val check = canDeleteWarehouse(warehouse.id)
        if (!check.first) {
            return check
        }
        dao.deleteWarehouse(warehouse)
        return Pair(true, "انبار «${warehouse.name}» با موفقیت حذف شد.")
    }

    suspend fun getProductStockInWarehouse(productId: Long, warehouseId: Long): Double =
        dao.getProductStockInWarehouse(productId, warehouseId)

    suspend fun getTotalStockInWarehouse(warehouseId: Long): Double =
        dao.getTotalStockInWarehouse(warehouseId)

    /**
     * حواله انتقال کالا بین دو انبار
     */
    suspend fun transferStockBetweenWarehouses(
        productId: Long,
        fromWarehouseId: Long,
        toWarehouseId: Long,
        quantitySecondary: Double,
        jalaliDate: String,
        referenceNumber: String,
        description: String
    ): Pair<Boolean, String> {
        if (fromWarehouseId == toWarehouseId) {
            return Pair(false, "انبار مبدأ و انبار مقصد نمی‌توانند یکسان باشند.")
        }
        if (quantitySecondary <= 0.0) {
            return Pair(false, "مقدار انتقال باید بیشتر از صفر باشد.")
        }

        val product = dao.getProductById(productId)
            ?: return Pair(false, "کالای انتخاب‌شده یافت نشد.")

        val fromWarehouse = dao.getWarehouseById(fromWarehouseId)
            ?: return Pair(false, "انبار مبدأ یافت نشد.")

        val toWarehouse = dao.getWarehouseById(toWarehouseId)
            ?: return Pair(false, "انبار مقصد یافت نشد.")

        // ثبت سند خروج از انبار مبدأ
        val outMovement = InventoryMovementEntity(
            productId = productId,
            warehouseId = fromWarehouseId,
            jalaliDate = jalaliDate,
            documentType = "TRANSFER_OUT",
            referenceNumber = referenceNumber,
            quantitySecondary = -quantitySecondary,
            unitPrice = product.buyPrice,
            description = "حواله خروج انتقال به انبار ${toWarehouse.name} ($description)"
        )
        dao.insertMovement(outMovement)

        // ثبت سند ورود به انبار مقصد
        val inMovement = InventoryMovementEntity(
            productId = productId,
            warehouseId = toWarehouseId,
            jalaliDate = jalaliDate,
            documentType = "TRANSFER_IN",
            referenceNumber = referenceNumber,
            quantitySecondary = quantitySecondary,
            unitPrice = product.buyPrice,
            description = "حواله ورود انتقال از انبار ${fromWarehouse.name} ($description)"
        )
        dao.insertMovement(inMovement)

        return Pair(true, "انتقال با موفقیت با شماره حواله $referenceNumber ثبت گردید.")
    }

    fun getCardex(productId: Long): Flow<List<InventoryMovementEntity>> =
        dao.getCardexForProduct(productId)

    suspend fun recordMovement(movement: InventoryMovementEntity) {
        dao.insertMovement(movement)
        dao.adjustStock(movement.productId, movement.quantitySecondary)
    }

    suspend fun performStockAdjustment(
        productId: Long,
        warehouseId: Long,
        countedPhysicalStockSecondary: Double,
        reason: String = "انبارگردانی دوره‌ای"
    ): Double {
        val product = dao.getProductById(productId) ?: return 0.0
        val discrepancy = countedPhysicalStockSecondary - product.currentStockInSecondary
        if (discrepancy != 0.0) {
            val movement = InventoryMovementEntity(
                productId = productId,
                warehouseId = warehouseId,
                jalaliDate = com.example.util.JalaliDate.now().toStandardString(),
                documentType = "ADJUSTMENT",
                referenceNumber = "ADJ-${System.currentTimeMillis() % 10000}",
                quantitySecondary = discrepancy,
                unitPrice = product.buyPrice,
                description = "$reason - مغایرت: $discrepancy ${product.secondaryUnit}"
            )
            recordMovement(movement)
        }
        return discrepancy
    }
}
