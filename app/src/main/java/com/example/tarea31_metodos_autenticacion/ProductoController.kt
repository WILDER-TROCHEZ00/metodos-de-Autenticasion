package com.example.tarea31_metodos_autenticacion

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * ProductoController - Clase que actúa como el Controlador en el patrón MVC.
 * Se encarga de gestionar todas las peticiones de red entre la aplicación y el servidor PHP.
 * Maneja la lógica de autenticación (Login) y las operaciones CRUD de productos.
 */
class ProductoController(private var token: String) {

    // Configuración de Retrofit para realizar las peticiones HTTP
    private val retrofit = Retrofit.Builder()
        // URL base que apunta al servidor local de XAMPP (IP 10.0.2.2 es usada para el emulador)
        .baseUrl("http://10.0.2.2/auth_api/")
        // Conversor para transformar automáticamente el JSON del servidor a objetos de Kotlin
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Instancia de la interfaz ApiService definida con las rutas del servidor
    private val apiService = retrofit.create(ApiService::class.java)

    /**
     * Actualiza el token de acceso guardado en el controlador.
     * Se utiliza después de un inicio de sesión exitoso.
     */
    fun setToken(newToken: String) {
        this.token = newToken
    }

    /**
     * Realiza la petición de inicio de sesión enviando usuario y contraseña.
     * El servidor responde con un Bearer Token dinámico si las credenciales son válidas.
     */
    fun login(request: LoginRequest, onResult: (LoginResponse?) -> Unit) {
        apiService.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                // Retorna el cuerpo de la respuesta (status y token) al llamador
                onResult(response.body())
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Retorna null si hubo un error de conexión
                onResult(null)
            }
        })
    }

    /**
     * Obtiene la lista de todos los productos desde la base de datos MySQL.
     * Requiere el encabezado "Authorization" con el prefijo "Bearer" seguido del token.
     */
    fun listarProductos(onResult: (List<Producto>?) -> Unit) {
        apiService.getProductos("Bearer $token").enqueue(object : Callback<List<Producto>> {
            override fun onResponse(call: Call<List<Producto>>, response: Response<List<Producto>>) {
                onResult(response.body())
            }
            override fun onFailure(call: Call<List<Producto>>, t: Throwable) {
                onResult(null)
            }
        })
    }

    /**
     * Envía un nuevo producto al servidor para ser guardado en la base de datos.
     * Utiliza el método POST y requiere autenticación Bearer Token.
     */
    fun agregarProducto(producto: Producto, onResult: (Boolean) -> Unit) {
        apiService.crearProducto("Bearer $token", producto).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                // Retorna true si el servidor respondió con un código de éxito (2xx)
                onResult(response.isSuccessful)
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false)
            }
        })
    }

    /**
     * Envía un producto existente con sus nuevos datos para actualizarlo en la BD.
     * Utiliza el método PUT y requiere autenticación Bearer Token.
     */
    fun actualizarProducto(producto: Producto, onResult: (Boolean) -> Unit) {
        apiService.actualizarProducto("Bearer $token", producto).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                onResult(response.isSuccessful)
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false)
            }
        })
    }

   /**
     * Solicita la eliminación de un producto específico mediante su ID.
     * Utiliza el método DELETE y requiere autenticación Bearer Token.
     */
    fun eliminarProducto(id: Int, onResult: (Boolean) -> Unit) {
        apiService.eliminarProducto("Bearer $token", id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                onResult(response.isSuccessful)
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                onResult(false)
            }
        })
    }
}
