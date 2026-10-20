package com.example.tiendaapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val ColorPrincipal = Color(0xFF6C4DFF)
private val ColorFondo = Color(0xFFF3F0FF)
private val ColorDescuento = Color(0xFFE53935)

@Composable
fun ListaProductosScreen(viewModel: ProductViewModel, onProductoClick: (Int) -> Unit) {
    var texto by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(ColorFondo)
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            placeholder = { Text("Buscar productos...") },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            trailingIcon = {
                IconButton(onClick = { viewModel.buscar(texto) }) {
                    Icon(Icons.Outlined.Search, contentDescription = "Buscar")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.buscar(texto) })
        )

        when {
            viewModel.cargando -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ColorPrincipal)
                }
            }
            viewModel.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.error ?: "", color = ColorDescuento)
                }
            }
            viewModel.productos.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron productos")
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(viewModel.productos) { producto ->
                        ProductoCard(producto) { onProductoClick(producto.id) }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoCard(producto: Product, onClick: () -> Unit) {
    val precioFinal = producto.price * (1 - producto.discountPercentage / 100)

    Card(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = producto.thumbnail,
                contentDescription = producto.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = "-${producto.discountPercentage.toInt()}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(ColorDescuento, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Column(Modifier.padding(10.dp)) {
            Text(
                text = producto.title,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = producto.category, color = Color.Gray, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(16.dp)
                )
                Text(text = " ${"%.1f".format(producto.rating)}", fontSize = 13.sp)
            }
            Text(
                text = "US$ ${"%.2f".format(producto.price)}",
                color = Color.Gray,
                fontSize = 12.sp,
                textDecoration = TextDecoration.LineThrough
            )
            Text(
                text = "US$ ${"%.2f".format(precioFinal)}",
                color = ColorPrincipal,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun DetalleProductoScreen(viewModel: ProductViewModel, id: Int) {
    LaunchedEffect(id) { viewModel.cargarProducto(id) }
    val producto = viewModel.productoSeleccionado

    if (producto == null || producto.id != id) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (viewModel.error != null) {
                Text(viewModel.error ?: "", color = ColorDescuento)
            } else {
                CircularProgressIndicator(color = ColorPrincipal)
            }
        }
        return
    }

    val precioFinal = producto.price * (1 - producto.discountPercentage / 100)

    Column(
        Modifier
            .fillMaxSize()
            .background(ColorFondo)
            .verticalScroll(rememberScrollState())
    ) {
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(producto.images) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = producto.title,
                    modifier = Modifier
                        .width(300.dp)
                        .height(260.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Column(Modifier.padding(16.dp)) {
            Text(text = producto.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${producto.category} · ${producto.brand ?: "Sin marca"}",
                color = Color.Gray
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "US$ ${"%.2f".format(precioFinal)}",
                    color = ColorPrincipal,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "US$ ${"%.2f".format(producto.price)}",
                    color = Color.Gray,
                    textDecoration = TextDecoration.LineThrough
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "-${producto.discountPercentage.toInt()}%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(ColorDescuento, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFB300)
                )
                Text(text = " ${"%.1f".format(producto.rating)}  ·  Stock: ${producto.stock}")
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "Descripción", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(text = producto.description)
        }
    }
}