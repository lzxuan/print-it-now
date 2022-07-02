package com.triplicity.printitnow.ui.prints

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.Group
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.triplicity.printitnow.R
import com.triplicity.printitnow.entity.Print
import com.triplicity.printitnow.ui.prints.PrintAdapter.PrintHolder
import java.text.SimpleDateFormat
import java.util.*

class PrintAdapter (options: FirestoreRecyclerOptions<Print>, private val context: Context) :
    FirestoreRecyclerAdapter<Print, PrintHolder>(options) {

    override fun onBindViewHolder(holder: PrintHolder, position: Int, model: Print) {
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("ms","MY","MY"))

        holder.printsNo.text = this.snapshots.getSnapshot(position).id
        holder.printsStatus.text = model.status
        if (model.status == "Printed") {
            holder.printsStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrinted))
        } else {
            holder.printsStatus.setTextColor(ContextCompat.getColor(context, R.color.colorQueuing))
        }
        holder.printsDate.text = formatter.format(model.createdOn!!)
        holder.printsTotal.text = context.resources.getString(R.string.price, model.price)

        holder.printsFile.removeAllViews()
        model.files?.forEach {
            val file = Chip(context)
            file.text = it
            holder.printsFile.addView(file)
        }

        holder.detailsButton.setOnClickListener { expand(holder) }
        holder.itemView.setOnClickListener { expand(holder) }

        val color = if (model.printConfig?.get("color") as Boolean) "Color" else "Black"
        val copies = model.printConfig?.get("copies")?.toString()
        val paperSize = model.printConfig?.get("paperSize")?.toString()
        val twoSided = if (model.printConfig?.get("twoSided") as Boolean) "Two-Sided" else "One-Sided"
        holder.printConfig.text = context.resources.getString(R.string.print_config, paperSize, color, twoSided, copies, model.pages)
    }

    private fun expand(holder: PrintHolder) {
        if (holder.expandableView.visibility == View.GONE){
            TransitionManager.beginDelayedTransition(holder.cardView, AutoTransition())
            holder.expandableView.visibility = View.VISIBLE
            holder.detailsButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up)
        } else {
            TransitionManager.beginDelayedTransition(holder.cardView, AutoTransition())
            holder.expandableView.visibility = View.GONE
            holder.detailsButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrintHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.card_print,
            parent,
            false
        )
        return PrintHolder(view)
    }

    inner class PrintHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var printsNo: TextView = itemView.findViewById(R.id.printsNo)
        var printsStatus: TextView = itemView.findViewById(R.id.printsStatus)
        var printsDate: TextView = itemView.findViewById(R.id.printsDate)
        var printsTotal: TextView = itemView.findViewById(R.id.printsTotal)

        val cardView: CardView = itemView.findViewById(R.id.cvPrints)
        val detailsButton: ImageButton = itemView.findViewById(R.id.detailsButton)
        val expandableView: Group = itemView.findViewById(R.id.expandableView)
        val printsFile: ChipGroup = itemView.findViewById(R.id.printsFile)
        val printConfig: TextView = itemView.findViewById(R.id.printConfig)
    }
}