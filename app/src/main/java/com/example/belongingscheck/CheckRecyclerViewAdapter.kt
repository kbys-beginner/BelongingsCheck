package com.example.belongingscheck

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import org.w3c.dom.Text

class CheckRecyclerViewAdapter(
    private val context: Context,
    private val resourceID: Int,
    private val items: MutableList<Check>
) : RecyclerView.Adapter<CheckRecyclerViewAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText = itemView.findViewById<TextView>(R.id.titleTextView)
        var amountText = itemView.findViewById<TextView>(R.id.amountTextView)
        var checkImage = itemView.findViewById<ImageView>(R.id.checkImageView)
        var trashButton = itemView.findViewById<ImageButton>(R.id.trashButton)
    }

    var preference = context.getSharedPreferences("DEFAULT", Context.MODE_PRIVATE)
    var editor = preference.edit()
    val gson = Gson()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(resourceID, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        var item = items[position] as Check
        holder.titleText.text = "・${item.Item}"
        holder.amountText.text = "数量: ${item.Amount}"
        if(item.IsCheck)holder.checkImage.setImageResource(R.drawable.check)
        holder.trashButton.setOnClickListener {
                AlertDialog.Builder(context).apply {
                    setTitle(item.Item)
                    setMessage("本当に削除しますか？")
                    setPositiveButton("はい"){_,_->
                        items.remove(item)
                        var comparator =
                            compareBy<Check>() { a -> a.IsCheck }.thenBy { a -> a.Amount }.thenBy { a -> a.Item }
                        var tmp = items.sortedWith(comparator)
                        items.clear()
                        items.addAll(tmp)
                        notifyDataSetChanged()
                        editor.putString("CHECKLIST${item.ID}",gson.toJson(items))
                        editor.commit()
                        Toast.makeText(context,"${item.Item}を削除しました", Toast.LENGTH_SHORT).show()
                    }
                    setNegativeButton("いいえ"){_,_->
                        return@setNegativeButton
                    }
                    create()
                    show()
                }
        }
    }
}