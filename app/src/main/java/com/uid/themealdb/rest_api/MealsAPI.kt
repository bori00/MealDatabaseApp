package com.uid.themealdb.rest_api

import com.uid.themealdb.rest_api.model.BaseMealsResponseDTO
import com.uid.themealdb.rest_api.model.DetailedMealsResponseDTO
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface MealsAPI {
    @GET( "filter.php" )
    @Headers( "Content-Type: application/json" )
    fun getMealsWithIngredient(@Query("i") ingredientName: String): Call<BaseMealsResponseDTO>

    @GET( "lookup.php" )
    @Headers( "Content-Type: application/json" )
    fun getMealDetails(@Query("i") ingredientId: String): Call<DetailedMealsResponseDTO>

    companion object {
        private val httpInterceptor = HttpLoggingInterceptor().apply {
            // there are different logging levels that provide a various amount of detail
            // we will use the most detailed one
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        private val httpClient = OkHttpClient.Builder().apply{
            // add the interceptor to the newly created HTTP client
            this.addInterceptor ( httpInterceptor )
        }.build()

        fun create(): MealsAPI {
            val retrofit = Retrofit.Builder()
                .baseUrl ("https://www.themealdb.com/api/json/v1/1/")
                .addConverterFactory (GsonConverterFactory.create() )
                .client(httpClient)
                .build()

            return retrofit.create(MealsAPI::class.java)
        }
    }
}