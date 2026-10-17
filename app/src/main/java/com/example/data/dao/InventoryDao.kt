package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WarehouseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    // Warehouses
    @Query("SELECT * FROM warehouses ORDER BY isDefault DESC, id ASC")
    fun getAllWarehouses(): Flow<List<WarehouseEntity>>

    @Query("SELECT * FROM warehouses WHERE id = :id LIMIT 1")
    suspend fun getWarehouseById(id: Long): WarehouseEntity?

    @Query("SELECT * FROM warehouses WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultWarehouse(): WarehouseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: WarehouseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouses(warehouses: List<WarehouseEntity>)

    @Update
    suspend fun updateWarehouse(warehouse: WarehouseEntity)

    @Delete
    suspend fun deleteWarehouse(warehouse: WarehouseEntity)

    @Query("UPDATE warehouses SET isDefault = 0")
    suspend fun clearDefaultWarehouses()

    @Query("UPDATE warehouses SET isDefault = 1 WHERE id = :warehouseId")
    suspend fun setWarehouseAsDefault(warehouseId: Long)

    @Query("SELECT COUNT(*) FROM inventory_movements WHERE warehouseId = :warehouseId")
    suspend fun countMovementsForWarehouse(warehouseId: Long): Int

    @Query("SELECT COALESCE(SUM(quantitySecondary), 0.0) FROM inventory_movements WHERE warehouseId = :warehouseId")
    suspend fun getTotalStockInWarehouse(warehouseId: Long): Double

    @Query("SELECT COALESCE(SUM(quantitySecondary), 0.0) FROM inventory_movements WHERE productId = :productId AND warehouseId = :warehouseId")
    suspend fun getProductStockInWarehouse(productId: Long, warehouseId: Long): Double

    // Products
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE currentStockInSecondary <= minStockThreshold")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE currentStockInSecondary <= minStockThreshold")
    fun getLowStockCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("UPDATE products SET currentStockInSecondary = currentStockInSecondary + :delta WHERE id = :productId")
    suspend fun adjustStock(productId: Long, delta: Double)

    // Stock Movements & Cardex
    @Query("SELECT * FROM inventory_movements WHERE productId = :productId ORDER BY timestamp DESC")
    fun getCardexForProduct(productId: Long): Flow<List<InventoryMovementEntity>>

    @Query("SELECT * FROM inventory_movements ORDER BY timestamp DESC LIMIT 100")
    fun getRecentMovements(): Flow<List<InventoryMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovement(movement: InventoryMovementEntity): Long

    @Query("SELECT COUNT(*) FROM invoice_items WHERE productId = :productId")
    suspend fun countInvoiceItemsForProduct(productId: Long): Int

    @Query("SELECT COUNT(*) FROM inventory_movements WHERE productId = :productId AND documentType != 'INITIAL_BALANCE'")
    suspend fun countActiveMovementsForProduct(productId: Long): Int

    @Query("SELECT * FROM inventory_movements WHERE documentType IN ('TRANSFER', 'TRANSFER_IN', 'TRANSFER_OUT') ORDER BY timestamp DESC")
    fun getTransferMovements(): Flow<List<InventoryMovementEntity>>

    @Query("DELETE FROM inventory_movements WHERE productId = :productId")
    suspend fun deleteMovementsForProduct(productId: Long)
}
