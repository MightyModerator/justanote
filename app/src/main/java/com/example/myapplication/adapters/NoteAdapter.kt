package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.entities.Note
import com.example.myapplication.util.ImageConverter

class NoteAdapter(context: Context, var notes: MutableList<Note>) :
    RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        val ivImage: ImageView = view.findViewById(R.id.itemPreviewImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_view,
            parent, false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bitmap = notes.get(position).image?.let { ImageConverter.convertStringToBase64(it) }
        holder.tvTitle.text = notes.get(position).title
        holder.tvMessage.text = notes.get(position).message
        holder.ivImage.setImageBitmap(bitmap)
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return notes[position].id.toLong()
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

}