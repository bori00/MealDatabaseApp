package com.uid.themealdb

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.uid.themealdb.models.DetailedMeal
import com.uid.themealdb.rest_api.MealsAPI
import com.uid.themealdb.rest_api.model.BaseMealsResponseDTO
import com.uid.themealdb.rest_api.model.DetailedMealsResponseDTO
import com.uid.themealdb.rest_api.model.ErrorDetails
import com.uid.themealdb.rest_api.model.converters.MealConverter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors

class DetailedMealActivity : AppCompatActivity() {
    private lateinit var mealId : String;
    private lateinit var meal : DetailedMeal;
    private val mealsAPI : MealsAPI = MealsAPI.create();
    private lateinit var context : Context;
    private val DELETE_RESULT = 1;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_meal)

        context = this;

        val extras = intent.extras
        if (extras != null) {
            mealId = extras.getString("mealId")!!
            loadMeal()
            //The key argument here must match that used in the other activity
        }
    }

    fun onMealLoaded(detailedMealsResponseDTO: DetailedMealsResponseDTO?) {
        Log.d("DetailedMealActivity", detailedMealsResponseDTO.toString());

        if (detailedMealsResponseDTO != null && detailedMealsResponseDTO.meals != null) {
            meal = MealConverter.constructDetailedMeal(detailedMealsResponseDTO.meals.get(0))
            Log.d("DetailedMealActivity", "New Detailed Meal: " + meal.toString());
            updateMealDataInUI()
        } else {
            Toast.makeText(context, "Error loading data", Toast.LENGTH_SHORT).show();
        }
    }

    fun updateMealDataInUI() {
        val mealNameTextView = findViewById<TextView>(R.id.detailedMealNameTextView)
        mealNameTextView.text = meal.name

        val mealCategoryTextView = findViewById<TextView>(R.id.detailedMealCategoryTextView)
        mealCategoryTextView.text = meal.category

        val mealAreaTextView = findViewById<TextView>(R.id.detailedMealAreaTextView)
        mealAreaTextView.text = meal.area

        val mealImageView = findViewById<ImageView>(R.id.detailedMealImageView)
        Picasso.get().load(meal.imageUrl).into(mealImageView);

        val mealInstructionsTextView = findViewById<TextView>(R.id.detailedMealInstructionsImageView)
        mealInstructionsTextView.text = meal.instructions
    }

    fun loadMeal() {
        Log.d("DetailedMealActivity", "Searching for detailed meal: $mealId")

        mealsAPI.getMealDetails(mealId).enqueue(object :
            Callback<DetailedMealsResponseDTO> {
            override fun onResponse(
                call: Call<DetailedMealsResponseDTO>,
                response: Response<DetailedMealsResponseDTO>
            ) {
                if ( !response.isSuccessful )
                {
                    // verify if the server has sent more details about the error
                    var errorDetails : ErrorDetails?
                    try
                    {
                        // in case of error, the data received is not converted to a Kotlin object
                        // => we nedd to deal with the raw, plain text answer
                        val errorInfo = response.errorBody()!!.string()
                        // after retrieving the information in string format, we will try to convert it
                        // from JSON to a Kotlin object – specific to ChatAPI
                        val parser = Gson()
                        errorDetails = parser.fromJson ( errorInfo, ErrorDetails::class.java )
                    }
                    catch (e: Exception)
                    {
                        errorDetails = ErrorDetails ( "Error message cannot be obtained!" )
                    }
                    if (errorDetails != null) {
                        Log.e("MainActivity", errorDetails.message)
                        Toast.makeText(context, "Error loading the meal's details. Please contact an admin", Toast.LENGTH_LONG).show()
                    }
                }
                else
                {
                    // received data is available in as a Kotlin object of type User
                    val detailedMealsResponseDTO: DetailedMealsResponseDTO? = response.body()

                    onMealLoaded(detailedMealsResponseDTO)
                }

            }

            override fun onFailure(call: Call<DetailedMealsResponseDTO>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.detailed_meal_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.deleteMeal) {
            Log.d("DetailedMealActivity", "Delete Meal: " + mealId)
            onFinishActivityWithDeleteMeal()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onFinishActivityWithDeleteMeal() {
        setResult(Activity.RESULT_OK, Intent().putExtra("delete", true).putExtra("mealId", mealId));
        finish();
    }
}