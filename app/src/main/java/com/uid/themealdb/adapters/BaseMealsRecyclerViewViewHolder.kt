package com.uid.themealdb.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.uid.themealdb.R
import com.uid.themealdb.models.BaseMeal

class BaseMealsRecyclerViewViewHolder (private val view: View, private val type: Int, onItemClicked: (Int) -> Unit) : RecyclerView.ViewHolder ( view )
{
    private var mealNameRef: TextView
    private var mealImageRef: ImageView

    init
    {
        mealNameRef = view.findViewById(R.id.baseMealNameTextView)
        mealImageRef = view.findViewById(R.id.baseMealImageView)

        view.setOnClickListener {
            onItemClicked(adapterPosition)
        }
    }

    fun bindData (baseMeal: BaseMeal)
    {
        mealNameRef.text = baseMeal.name
        Picasso.get().load(baseMeal.thumbUrl).into(mealImageRef);
    }
}