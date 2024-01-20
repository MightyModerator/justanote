package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.myapplication.entities.Note

class NoteAdapter(var context: Context, var notes: List<Note>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private class ViewHolder {
        lateinit var title: TextView
        lateinit var message: TextView
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

            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as ViewHolder
        }

        val tvTitle = holder.title
        val tvMessage = holder.message
        val note = notes[position]

        tvTitle.text = note.title
        tvMessage.text = note.message

        return view
    }
}