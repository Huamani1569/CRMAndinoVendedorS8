package com.crmandino.vendedor.data.remote

import com.crmandino.vendedor.data.local.SesionLocal
import com.crmandino.vendedor.data.remote.api.ApiService
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Cliente Retrofit. Un interceptor agrega a cada petición:
//  - la dirección del servidor configurada en el Login (se puede cambiar sin recompilar)
//  - el token JWT de la sesión ("Authorization: Bearer ...")
object RetrofitClient {

    fun crear(sesion: SesionLocal): ApiService {
        val interceptor = Interceptor { chain ->
            val original = chain.request()
            val servidor = sesion.baseUrl.toHttpUrlOrNull()
            val url = if (servidor != null) {
                original.url.newBuilder()
                    .scheme(servidor.scheme)
                    .host(servidor.host)
                    .port(servidor.port)
                    .build()
            } else {
                original.url
            }
            val peticion = original.newBuilder().url(url)
            sesion.token?.let { peticion.header("Authorization", "Bearer $it") }
            chain.proceed(peticion.build())
        }

        // Tiempos de espera cortos: si el servidor no responde, la operación queda PENDIENTE.
        val cliente = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(SesionLocal.URL_POR_DEFECTO)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
