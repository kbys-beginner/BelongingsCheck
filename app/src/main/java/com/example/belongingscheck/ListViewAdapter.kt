package com.example.belongingscheck

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.icu.text.CaseMap.Title
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.belongingscheck.databinding.ListListingBinding
import com.google.gson.Gson

class ListViewAdapter(
    private val context: Context,
    private val resourceID: Int,
    private val items: MutableList<Item>
) : BaseAdapter() {
    var preference = context.getSharedPreferences("DEFAULT", MODE_PRIVATE)
    var editor = preference.edit()
    val gson = Gson()
    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position] as Any
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return when (resourceID) {
            R.layout.list_listing -> ListListingBinding.inflate(LayoutInflater.from(context))
                .apply {
                    var item = items[position] as Item
                    titleTextView.text = item.Title
                    dateTextView.text = item.Date
                    trashButton.setOnClickListener {
                        AlertDialog.Builder(context).apply {
                            setTitle(item.Title)
                            setMessage("本当に削除しますか？")
                            setPositiveButton("はい") { _, _ ->
                                items.remove(item)
                                var comparator =
                                    compareByDescending<Item> { a -> a.IsFavorite }.thenBy { a -> a.getDate() }
                                        .thenBy { a -> a.ID }
                                var tmp = items.sortedWith(comparator)
                                items.clear()
                                items.addAll(tmp)
                                notifyDataSetChanged()
                                editor.putString("DATA", gson.toJson(items))
                                editor.commit()
                                Toast.makeText(
                                    context,
                                    "${item.Title}を削除しました",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            setNegativeButton("いいえ") { _, _ ->
                                return@setNegativeButton
                            }
                            create()
                            show()
                        }
                    }
                    linearLayout1.setOnClickListener {
                        var item = items[position] as Item
                        context.startActivity(Intent(context, CheckActivity::class.java).apply {
                            putExtra("ITEM_ID", item.ID)
                            putExtra("TITLE", item.Title)
                            putExtra("DATE", item.Date)
                        })
                    }
                    if (item.IsFavorite) favoriteImage.setImageResource(R.drawable.greenstar_foreground)
                    else favoriteImage.setImageResource(R.drawable.blackstar_foreground)

                    favoriteImage.setOnClickListener {
                        if (item.IsFavorite) {
                            item.IsFavorite = false
                            var comparator =
                                compareByDescending<Item> { a -> a.IsFavorite }.thenBy { a -> a.getDate() }
                                    .thenBy { a -> a.ID }
                            var tmp = items.sortedWith(comparator)
                            items.clear()
                            items.addAll(tmp)
                            notifyDataSetChanged()
                            editor.putString("DATA", gson.toJson(items))
                            editor.commit()
                            Toast.makeText(
                                context,
                                "${item.Title}をお気に入りから削除しました",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            item.IsFavorite = true
                            var comparator =
                                compareByDescending<Item> { a -> a.IsFavorite }.thenBy { a -> a.getDate() }
                                    .thenBy { a -> a.ID }
                            var tmp = items.sortedWith(comparator)
                            items.clear()
                            items.addAll(tmp)
                            notifyDataSetChanged()
                            editor.putString("DATA", gson.toJson(items))
                            editor.commit()
                            Toast.makeText(
                                context,
                                "${item.Title}をお気に入りに追加しました",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.root

            else -> View(context)
        }
    }
}