package com.uid.themealdb.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uid.themealdb.R
import com.uid.themealdb.models.BaseMeal

class BaseMealsRecyclerViewAdapter (
    private val context: Context,
    private val dataSource: List<BaseMeal>,
    private val onItemClick: (BaseMeal, Int) -> Unit) : RecyclerView.Adapter<BaseMealsRecyclerViewViewHolder>()
{
    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater


    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int ): BaseMealsRecyclerViewViewHolder
    {
        val view = inflater.inflate ( viewType, parent, false )
        return BaseMealsRecyclerViewViewHolder(view, 1) { it -> onItemClicked(it) }
    }

    override fun onBindViewHolder ( holder: BaseMealsRecyclerViewViewHolder, position: Int )
    {
        holder.bindData ( dataSource.get(position) )
    }

    override fun getItemViewType ( position: Int ) : Int
    {
        return R.layout.base_meal_item
    }

    override fun getItemCount(): Int
    {
        return dataSource.size
    }

    private fun onItemClicked(index : Int) {
        onItemClick(dataSource.get(index), index)
    }
}