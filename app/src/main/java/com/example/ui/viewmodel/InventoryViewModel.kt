package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.InventoryMovementEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WarehouseEntity
import com.example.data.repository.InventoryRepository
import com.example.util.JalaliDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    val allWarehouses: StateFlow<List<WarehouseEntity>> = repository.allWarehouses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<String>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transferMovements: StateFlow<List<InventoryMovementEntity>> = repository.transferMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMovements: StateFlow<List<InventoryMovementEntity>> = repository.recentMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        repository.allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { p ->
            val matchesQuery = query.isBlank() ||
                    p.name.contains(query, ignoreCase = true) ||
                    p.code.contains(query, ignoreCase = true) ||
                    p.barcode.contains(query, ignoreCase = true)

            val matchesCategory = category == null || p.category == category

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<ProductEntity>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedProductCardex = MutableStateFlow<ProductEntity?>(null)
    val selectedProductCardex: StateFlow<ProductEntity?> = _selectedProductCardex.asStateFlow()

    val cardexMovements: StateFlow<List<InventoryMovementEntity>> = _selectedProductCardex
        .flatMapLatest { product ->
            if (product != null) {
                repository.getCardex(product.id)
            } else {
                repository.recentMovements
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(cat: String?) {
        _selectedCategory.value = cat
    }

    fun selectProductForCardex(product: ProductEntity?) {
        _selectedProductCardex.value = product
    }

    fun addProduct(
        code: String,
        barcode: String,
        name: String,
        category: String,
        primaryUnit: String,
        secondaryUnit: String,
        conversionRate: Double,
        initialStockSecondary: Double,
        minStock: Double,
        buyPrice: Long,
        retailPrice: Long,
        wholesalePrice: Long,
        partnerPrice: Long,
        description: String,
        warehouseId: Long? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val product = ProductEntity(
                code = code.ifBlank { "PRD-${System.currentTimeMillis() % 10000}" },
                barcode = barcode,
                name = name,
                category = category.ifBlank { "عمومی" },
                primaryUnit = primaryUnit.ifBlank { "کارتن" },
                secondaryUnit = secondaryUnit.ifBlank { "عدد" },
                conversionRate = if (conversionRate <= 0) 1.0 else conversionRate,
                currentStockInSecondary = initialStockSecondary,
                minStockThreshold = minStock,
                buyPrice = buyPrice,
                retailPrice = retailPrice,
                wholesalePrice = wholesalePrice,
                partnerPrice = partnerPrice,
                description = description
            )
            val productId = repository.insertProduct(product)
            if (initialStockSecondary > 0) {
                val targetWhId = warehouseId
                    ?: allWarehouses.value.firstOrNull { it.isDefault }?.id
                    ?: allWarehouses.value.firstOrNull()?.id
                    ?: 1L

                repository.recordMovement(
                    InventoryMovementEntity(
                        productId = productId,
                        warehouseId = targetWhId,
                        jalaliDate = JalaliDate.now().toStandardString(),
                        documentType = "INITIAL_BALANCE",
                        referenceNumber = "INV-INIT",
                        quantitySecondary = initialStockSecondary,
                        unitPrice = buyPrice,
                        description = "موجودی اولیه هنگام تعریف کالا"
                    )
                )
            }
            onSuccess()
        }
    }

    fun updateProduct(product: ProductEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                onSuccess()
            } catch (e: Exception) {
                // Ignore or log
            }
        }
    }

    fun deleteProduct(product: ProductEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.deleteProductSafely(product)
                onResult(res.first, res.second)
            } catch (e: Exception) {
                onResult(false, "امکان حذف این کالا به دلیل داشتن وابستگی در سایر اسناد وجود ندارد.")
            }
        }
    }

    fun addWarehouse(
        code: String,
        name: String,
        manager: String,
        phone: String,
        address: String,
        isDefault: Boolean = false,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val autoCode = code.ifBlank { "W${(allWarehouses.value.size + 101)}" }
            val id = repository.insertWarehouse(
                WarehouseEntity(
                    code = autoCode,
                    name = name,
                    managerName = manager,
                    phone = phone,
                    address = address,
                    isDefault = isDefault
                )
            )
            if (isDefault) {
                repository.setWarehouseAsDefault(id)
            }
            onSuccess()
        }
    }

    fun updateWarehouse(warehouse: WarehouseEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateWarehouse(warehouse)
            if (warehouse.isDefault) {
                repository.setWarehouseAsDefault(warehouse.id)
            }
            onSuccess()
        }
    }

    fun deleteWarehouse(warehouse: WarehouseEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.deleteWarehouseSafely(warehouse)
            onResult(res.first, res.second)
        }
    }

    fun setWarehouseAsDefault(warehouseId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.setWarehouseAsDefault(warehouseId)
            onSuccess()
        }
    }

    fun transferStock(
        productId: Long,
        fromWarehouseId: Long,
        toWarehouseId: Long,
        quantitySecondary: Double,
        description: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val refNo = "TRF-${System.currentTimeMillis() % 100000}"
            val res = repository.transferStockBetweenWarehouses(
                productId = productId,
                fromWarehouseId = fromWarehouseId,
                toWarehouseId = toWarehouseId,
                quantitySecondary = quantitySecondary,
                jalaliDate = JalaliDate.now().toStandardString(),
                referenceNumber = refNo,
                description = description
            )
            onResult(res.first, res.second)
        }
    }

    /**
     * ثبت انبارگردانی و اصلاح موجودی فیزیکی واقعی با ثبت مغایرت در کاردکس
     */
    fun performStockAdjustment(
        productId: Long,
        warehouseId: Long,
        countedPhysicalStockSecondary: Double,
        reason: String,
        onSuccess: (Double) -> Unit
    ) {
        viewModelScope.launch {
            val discrepancy = repository.performStockAdjustment(
                productId = productId,
                warehouseId = warehouseId,
                countedPhysicalStockSecondary = countedPhysicalStockSecondary,
                reason = reason
            )
            onSuccess(discrepancy)
        }
    }
}
