package com.uid.themealdb.rest_api.model.converters

import com.uid.themealdb.models.BaseMeal
import com.uid.themealdb.models.DetailedMeal
import com.uid.themealdb.rest_api.model.BaseMealDTO
import com.uid.themealdb.rest_api.model.DetailedMealDTO

class MealConverter {
    companion object {
        fun constructBaseMeal(baseMealDTO: BaseMealDTO) : BaseMeal {
            return BaseMeal(baseMealDTO.strMeal, baseMealDTO.strMealThumb, baseMealDTO.idMeal)
        }

        fun constructDetailedMeal(detailedMealDTO: DetailedMealDTO) : DetailedMeal {
            return DetailedMeal(detailedMealDTO.idMeal,
                detailedMealDTO.strMeal,
                detailedMealDTO.strCategory,
                detailedMealDTO.strArea,
                detailedMealDTO.strMealThumb,
                detailedMealDTO.strInstructions
            )
        }
    }
}