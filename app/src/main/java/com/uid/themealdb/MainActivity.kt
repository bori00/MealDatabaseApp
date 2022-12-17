package com.uid.themealdb

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.uid.themealdb.adapters.BaseMealsRecyclerViewAdapter
import com.uid.themealdb.models.BaseMeal
import com.uid.themealdb.rest_api.MealsAPI
import com.uid.themealdb.rest_api.model.BaseMealsResponseDTO
import com.uid.themealdb.rest_api.model.ErrorDetails
import com.uid.themealdb.rest_api.model.converters.MealConverter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Math.max
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors


class MainActivity : AppCompatActivity() {
    private val mealsAPI : MealsAPI = MealsAPI.create();
    private val context : Context = this;
    private var baseMealsList : ArrayList<BaseMeal> = ArrayList();
    lateinit private var layoutManager : LinearLayoutManager;
    lateinit var adapter: BaseMealsRecyclerViewAdapter;
    lateinit var getDeleteMealCommand : ActivityResultLauncher<Intent>;
    lateinit var loadMealsProgressBar : ProgressBar;
    lateinit var loadingStartTimestamp : LocalDateTime;
    private val MIN_LOADING_TIME_MILISECONDS : Int = 1000;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // search button setup
        val searchMealsButton = findViewById<Button>(R.id.searchMealsButton)
        searchMealsButton.setOnClickListener(SearchMealsButtonOnClickListener(this))

        // results recyclerview setup
        layoutManager = LinearLayoutManager ( this, LinearLayoutManager.VERTICAL, false)
        adapter = BaseMealsRecyclerViewAdapter(this, baseMealsList,
            {baseMeal, position -> onBaseMealClicked(baseMeal, position)})
        val recyclerView = findViewById<RecyclerView>(R.id.mealsRecyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        addMessageDividers(recyclerView, layoutManager)

        // listen to results from DetailedMealActivity
        getDeleteMealCommand = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        {
            result: ActivityResult ->
            onDetailedMealActivityResult(result)
        }

        // handle progress bar
        loadMealsProgressBar = findViewById<ProgressBar>(R.id.mealsLoadingProgressBar)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onDetailedMealActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK && result.data?.hasExtra("delete") == true) {
            val mealIdToDelete = result.data?.extras?.get("mealId")
            baseMealsList.removeIf{baseMeal -> baseMeal.id == mealIdToDelete}
            adapter.notifyDataSetChanged()
        }
    }

    private fun onBaseMealClicked(baseMeal: BaseMeal, position: Int) {
        Log.d("MainActivity","Meal clicked" + baseMeal)

        val intent = Intent(this, DetailedMealActivity::class.java )
        intent.putExtra("mealId", baseMeal.id);

        getDeleteMealCommand.launch(intent)
    }

    inner class SearchMealsButtonOnClickListener(private val myContext : MainActivity) : View.OnClickListener{
        override fun onClick(view: View?) {
            Log.d("MainActivity", "Searching button clicked")

            val ingredientEditText = myContext.findViewById<EditText>(R.id.inhredientEditText)
            val ingredientName = ingredientEditText.text.toString()

            if (validIngredientName(ingredientName)) {
                loadMealsWithIngredient(ingredientName)
            }
        }

        private fun validIngredientName(ingredientName: String) : Boolean{
            if (ingredientName.isEmpty()) {
                Toast.makeText(myContext, getString(R.string.empty_ingredient_name_error), Toast.LENGTH_SHORT).show()
                return false;
            }
            return true;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadMealsWithIngredient(ingredientName: String) {
        Log.d("MainActivity", "Searching for meals: $ingredientName")

        loadingStartTimestamp = LocalDateTime.now()
        loadMealsProgressBar.visibility = View.VISIBLE
        baseMealsList.removeAll(baseMealsList)
        adapter.notifyDataSetChanged()

        mealsAPI.getMealsWithIngredient(ingredientName).enqueue(object : Callback<BaseMealsResponseDTO> {
            override fun onResponse(
                call: Call<BaseMealsResponseDTO>,
                response: Response<BaseMealsResponseDTO>
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
                        Toast.makeText(context, "Error loading the meals. Please contact an admin", Toast.LENGTH_LONG).show()
                    }
                }
                else
                {
                    // received data is available in as a Kotlin object of type User
                    val baseMealsResponseDTO: BaseMealsResponseDTO? = response.body()

                    onMealsLoaded(baseMealsResponseDTO)
                }

            }

            override fun onFailure(call: Call<BaseMealsResponseDTO>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onMealsLoaded(baseMealsResponseDTO: BaseMealsResponseDTO?) {
        Log.d("MainActivity", baseMealsResponseDTO.toString());

        val now = LocalDateTime.now()
        Log.d("MainActivity", "Timestamps: " + loadingStartTimestamp.toString() + "; " + now + ". Difference = " + ChronoUnit.MILLIS.between(loadingStartTimestamp, now))

        val handler = Handler()
        handler.postDelayed(
            {
                loadMealsProgressBar.visibility = View.GONE

                if (baseMealsResponseDTO != null && baseMealsResponseDTO.meals != null) {
                    val newBaseMealsList = baseMealsResponseDTO.meals.stream()
                        .map { baseMealDTO -> MealConverter.constructBaseMeal(baseMealDTO) }
                        .collect(Collectors.toList())
                    baseMealsList.removeAll(baseMealsList)
                    baseMealsList.addAll(newBaseMealsList)
                    Log.d("MainActivity", "New BaseMealList: " + baseMealsList.toString());
                } else {
                    baseMealsList.removeAll(baseMealsList)
                    Toast.makeText(context, "No results found", Toast.LENGTH_LONG).show();
                }
                adapter.notifyDataSetChanged()
            },
            (MIN_LOADING_TIME_MILISECONDS - ChronoUnit.MILLIS.between(loadingStartTimestamp, now)).coerceAtLeast(0)
        )
    }

    private fun addMessageDividers(recyclerView: RecyclerView, linearLayoutManager : LinearLayoutManager) {
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            linearLayoutManager.orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
    }
}