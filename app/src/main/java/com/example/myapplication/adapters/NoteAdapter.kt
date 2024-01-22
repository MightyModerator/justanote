package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.myapplication.R
import com.example.myapplication.entities.Note
import com.example.myapplication.util.ImageConverter

class NoteAdapter(context: Context, var notes: List<Note>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private class ViewHolder {
        lateinit var title: TextView
        lateinit var message: TextView
        lateinit var image: ImageView
    }

    override fun getCount(): Int {
        return notes.size
    }

    override fun getItem(position: Int): Any {
        return notes[position]
    }

    override fun getItemId(position: Int): Long {
        return notes[position].id.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.list_item_view, parent, false)

            holder = ViewHolder()
            holder.title = view.findViewById(R.id.tvTitle)
            holder.message = view.findViewById(R.id.tvMessage)
            holder.image = view.findViewById(R.id.itemPreviewImage)

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val tvTitle = holder.title
        val tvMessage = holder.message
        val itemImage = holder.image
        val note = notes[position]
        val bitmap = note?.image?.let { ImageConverter.convertStringToBase64(it) }

        tvTitle.text = note.title
        tvMessage.text = note.message
        itemImage.setImageBitmap(bitmap)

        return view
    }
}