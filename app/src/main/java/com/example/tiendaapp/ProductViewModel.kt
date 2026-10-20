package com.example.tiendaapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    var productos by mutableStateOf<List<Product>>(emptyList())
        private set
    var productoSeleccionado by mutableStateOf<Product?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                productos = RetrofitClient.api.getProducts(30).products
            } catch (e: Exception) {
                error = "No se pudieron cargar los productos"
            }
            cargando = false
        }
    }

    fun buscar(texto: String) {
        if (texto.isBlank()) {
            cargarProductos()
            return
        }
        viewModelScope.launch {
            cargando = true
            error = null
            try {
                productos = RetrofitClient.api.searchProducts(texto).products
            } catch (e: Exception) {
                error = "No se pudo realizar la búsqueda"
            }
            cargando = false
        }
    }

    fun cargarProducto(id: Int) {
        viewModelScope.launch {
            productoSeleccionado = null
            error = null
            try {
                productoSeleccionado = RetrofitClient.api.getProductById(id)
            } catch (e: Exception) {
                error = "No se pudo cargar el producto"
            }
        }
    }
}