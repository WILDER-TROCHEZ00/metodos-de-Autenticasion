package com.example.tarea31_metodos_autenticacion

import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("api.php")
    fun getProductos(@Header("Authorization") auth: String): Call<List<Producto>>

    @POST("api.php")
    fun crearProducto(@Header("Authorization") auth: String, @Body producto: Producto): Call<Void>

    @PUT("api.php")
    fun actualizarProducto(@Header("Authorization") auth: String, @Body producto: Producto): Call<Void>

    @DELETE("api.php")
    fun eliminarProducto(@Header("Authorization") auth: String, @Query("id") id: Int): Call<Void>
}
