package com.example.tarea31_metodos_autenticacion

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.tarea31_metodos_autenticacion.ui.theme.Tarea31_Metodos_AutenticacionTheme

/**
 * MainActivity - Clase principal de la aplicación.
 * Gestiona el flujo de navegación entre la pantalla de Login y la pantalla del CRUD.
 * Utiliza Jetpack Compose para la construcción de la interfaz gráfica de usuario.
 */
class MainActivity : ComponentActivity() {
    
    // Controlador que gestiona la lógica de las peticiones HTTP
    private val controller = ProductoController("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplicación del tema personalizado del proyecto
            Tarea31_Metodos_AutenticacionTheme {
                // Estado persistente que indica si el usuario está autenticado
                var isLoggedIn by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Si no está logueado muestra la pantalla de Login, de lo contrario muestra el CRUD
                    if (!isLoggedIn) {
                        LoginScreen(controller) { token ->
                            // Al loguearse correctamente, guardamos el token y cambiamos el estado
                            controller.setToken(token)
                            isLoggedIn = true
                        }
                    } else {
                        ProductScreen(controller) {
                            // Al cerrar sesión volvemos al estado de no autenticado
                            isLoggedIn = false
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable LoginScreen - Pantalla inicial para autenticación.
 * Permite ingresar usuario y contraseña y obtener el Bearer Token.
 */
@Composable
fun LoginScreen(controller: ProductoController, onLoginSuccess: (String) -> Unit) {
    val context = LocalContext.current
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Iniciar Sesión", style = MaterialTheme.typography.headlineLarge)
        Text("Autenticación Bearer Token", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Campo para ingresar el nombre de usuario
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo para ingresar la contraseña con transformación visual de password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón para ejecutar el inicio de sesión
        Button(
            onClick = {
                loading = true
                val request = LoginRequest(usuario, password)
                controller.login(request) { response ->
                    loading = false
                    if (response?.status == "success" && response.token != null) {
                        Toast.makeText(context, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(response.token)
                    } else {
                        val msg = response?.message ?: "Usuario o clave incorrectos"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
            else Text("Entrar")
        }
    }
}

/**
 * Composable ProductScreen - Pantalla principal del CRUD.
 * Permite Listar, Crear, Editar y Eliminar productos si se cuenta con el token.
 */
@Composable
fun ProductScreen(controller: ProductoController, onLogout: () -> Unit) {
    val context = LocalContext.current
    // Estados para almacenar la lista de productos y los valores de los inputs
    var productos by remember { mutableStateOf(emptyList<Producto>()) }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    
    // Variables de control para el modo edición
    var idSeleccionado by remember { mutableStateOf<Int?>(null) }
    var modoEdicion by remember { mutableStateOf(false) }

    // Función interna para refrescar la lista de productos desde la base de datos MySQL
    val refreshList = {
        controller.listarProductos { list ->
            if (list != null) productos = list
        }
    }

    // Efecto que se dispara al cargar la pantalla por primera vez.
    LaunchedEffect(Unit) { refreshList() }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (modoEdicion) "Editar Producto" else "Nuevo Producto", style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = onLogout) { Text("Salir") } // Botón para cerrar sesión
        }

        // Formulario de entrada de datos
        TextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre del Producto") }, modifier = Modifier.fillMaxWidth())
        TextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
        TextField(value = precio, onValueChange = { precio = it }, label = { Text("Precio") }, modifier = Modifier.fillMaxWidth())
        
        Row(modifier = Modifier.padding(vertical = 8.dp)) {
            Button(
                onClick = {
                    val p = Producto(id = idSeleccionado, nombre = nombre, descripcion = descripcion, precio = precio.toDoubleOrNull() ?: 0.0)
                    
                    if (modoEdicion) {
                        // Acción para actualizar un producto existente (PUT)
                        controller.actualizarProducto(p) { success ->
                            if (success) {
                                nombre = ""; descripcion = ""; precio = ""
                                modoEdicion = false; idSeleccionado = null
                                refreshList()
                            }
                        }
                    } else {
                        // Acción para crear un nuevo producto (POST)
                        controller.agregarProducto(p) { success ->
                            if (success) {
                                nombre = ""; descripcion = ""; precio = ""
                                refreshList()
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (modoEdicion) "Guardar Cambios" else "Agregar")
            }

            if (modoEdicion) {
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = {
                    nombre = ""; descripcion = ""; precio = ""; modoEdicion = false; idSeleccionado = null
                }, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Listado scrollable de los productos obtenidos del servidor
        LazyColumn {
            items(productos) { producto ->
                ListItem(
                    headlineContent = { Text(producto.nombre) },
                    supportingContent = { Text("${producto.descripcion} - $${producto.precio}") },
                    trailingContent = {
                        Row {
                            // Acción para preparar la edición (Carga los datos en el formulario)
                            IconButton(onClick = {
                                nombre = producto.nombre
                                descripcion = producto.descripcion
                                precio = producto.precio.toString()
                                idSeleccionado = producto.id
                                modoEdicion = true
                            }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                            
                            // Acción para eliminar definitivamente (DELETE)
                            IconButton(onClick = {
                                producto.id?.let { id ->
                                    controller.eliminarProducto(id) { success ->
                                        if (success) refreshList()
                                    }
                                }
                            }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
                        }
                    }
                )
            }
        }
    }
}
