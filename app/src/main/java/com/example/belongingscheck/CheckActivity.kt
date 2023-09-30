package com.example.belongingscheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.belongingscheck.databinding.ActivityCheckBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CheckActivity : AppCompatActivity() {
    private lateinit var b: ActivityCheckBinding
    private lateinit var adapter: CheckRecyclerViewAdapter
    private var list = mutableListOf<Check>()
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)
        b = ActivityCheckBinding.inflate(layoutInflater)
        setContentView(b.apply {
            var preference = getSharedPreferences("DEFAULT", MODE_PRIVATE)
            var editor = preference.edit()
            var itemID = intent.getIntExtra("ITEM_ID", 0)
            var title = intent.getStringExtra("TITLE")
            var date = intent.getStringExtra("DATE")
            list.addAll(gson.fromJson(preference.getString("CHECKLIST$itemID", "[]"), object :
                TypeToken<List<Check>>() {}.type))
            var comparator = compareBy<Check>() { a -> a.IsCheck }.thenBy { a -> a.Amount }.thenBy { a -> a.Item }
            var tmp = list.sortedWith(comparator)
            list.clear()
            list.addAll(tmp)
            titleTextView.text = title
            dateTextView.text = date
            adapter = CheckRecyclerViewAdapter(this@CheckActivity, R.layout.list_recycler, list)
            recyclerView1.adapter = adapter
            addButton.setOnClickListener {
                var amountList = listOf<String>("1", "2", "3", "4", "5")
                var linearLayout = LinearLayout(this@CheckActivity)
                linearLayout.apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                var itemtext = TextView(this@CheckActivity)
                itemtext.apply {
                    setText(" アイテム: ")
                }
                var itemEditText = EditText(this@CheckActivity)
                itemEditText.apply {
                    width = 400
                    inputType = InputType.TYPE_CLASS_TEXT
                }
                var amountText = TextView(this@CheckActivity)
                amountText.apply {
                    setText(" 数: ")
                }
                var amountSpinner = Spinner(this@CheckActivity)
                amountSpinner.apply {
                    adapter = ArrayAdapter(
                        this@CheckActivity,
                        androidx.constraintlayout.widget.R.layout.support_simple_spinner_dropdown_item,
                        amountList
                    )
                }
                linearLayout.addView(itemtext)
                linearLayout.addView(itemEditText)
                linearLayout.addView(amountText)
                linearLayout.addView(amountSpinner)
                AlertDialog.Builder(this@CheckActivity).apply {
                    setTitle("追加")
                    setMessage("アイテムと数量を入力してください")
                    setView(linearLayout)
                    setPositiveButton("追加") { _, _ ->
                        if (itemEditText.text.isEmpty()) {
                            Toast.makeText(
                                this@CheckActivity,
                                "アイテムを入力してください",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setPositiveButton
                        }
                        list.add(
                            Check(
                                itemID,
                                itemEditText.text.toString(),
                                amountSpinner.selectedItemPosition + 1,
                                false
                            )
                        )
                        var comparator =
                            compareBy<Check>() { a -> a.IsCheck }.thenBy { a -> a.Amount }.thenBy { a -> a.Item }
                        var tmp = list.sortedWith(comparator)
                        list.clear()
                        list.addAll(tmp)
                        editor.putString("CHECKLIST$itemID", gson.toJson(list))
                        editor.commit()
                        adapter.notifyDataSetChanged()
                        Toast.makeText(
                            this@CheckActivity,
                            "${itemEditText.text}を追加しました",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    setNegativeButton("キャンセル") { _, _ ->
                        return@setNegativeButton
                    }
                    create()
                    show()
                }
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    var position = viewHolder.adapterPosition
                    var item = list[position]
                    if (item.IsCheck == false) {
                        AlertDialog.Builder(this@CheckActivity).apply {
                            setTitle(item.Item)
                            setMessage("準備しましたか？")
                            setPositiveButton("はい") { _, _ ->
                                item.IsCheck = true
                                var comparator =
                                    compareBy<Check>() { a -> a.IsCheck }.thenBy { a -> a.Amount }.thenBy { a -> a.Item }
                                var tmp = list.sortedWith(comparator)
                                list.clear()
                                list.addAll(tmp)
                                editor.putString("CHECKLIST$itemID", gson.toJson(list))
                                editor.commit()
                                adapter.notifyDataSetChanged()
                            }
                            setNegativeButton("いいえ") { _, _ ->
                                adapter.notifyDataSetChanged()
                                return@setNegativeButton
                            }
                            create()
                            show()
                        }
                    } else adapter.notifyDataSetChanged()
                }
            }).attachToRecyclerView(recyclerView1)

            clearButton.setOnClickListener {
                if (list.any { a -> a.IsCheck }) {

                    AlertDialog.Builder(this@CheckActivity).apply {
                        setTitle("確認")
                        setMessage("本当にチェックを解除しますか？")
                        setPositiveButton("はい") { _, _ ->
                            for (i in list) {
                                i.IsCheck = false
                            }
                            var comparator =
                                compareBy<Check>() { a -> a.IsCheck }.thenBy { a -> a.Amount }.thenBy { a -> a.Item }
                            var tmp = list.sortedWith(comparator)
                            list.clear()
                            list.addAll(tmp)
                            adapter = CheckRecyclerViewAdapter(
                                this@CheckActivity,
                                R.layout.list_recycler,
                                list
                            )
                            recyclerView1.adapter = adapter
                            editor.putString("CHECKLIST$itemID", gson.toJson(list))
                            editor.commit()
                            adapter.notifyDataSetChanged()
                        }
                        setNegativeButton("いいえ") { _, _ ->
                            adapter.notifyDataSetChanged()
                            return@setNegativeButton
                        }
                        create()
                        show()
                    }
                } else {
                    return@setOnClickListener
                }
            }
        }.root)
    }
}