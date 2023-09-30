package com.example.belongingscheck

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable.Orientation
import android.icu.lang.UCharacter.VerticalOrientation
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.belongingscheck.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    private lateinit var adapter: ListViewAdapter
    private var list = mutableListOf<Item>()
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.apply {
            var preference = getSharedPreferences("DEFAULT", MODE_PRIVATE)
            var editor = preference.edit()
            var idNumber = preference.getInt("ID", 1)
            list.addAll(gson.fromJson(preference.getString("DATA", "[]"), object :
                TypeToken<List<Item>>() {}.type))
            var comparator =
                compareByDescending<Item> { a -> a.IsFavorite }.thenBy { a -> a.getDate() }
                    .thenBy { a -> a.ID }
            var tmp = list.sortedWith(comparator)
            list.clear()
            list.addAll(tmp)
            adapter = ListViewAdapter(this@MainActivity, R.layout.list_listing, list)
            listView1.adapter = adapter
            addButton.setOnClickListener {
                var linearLayout = LinearLayout(this@MainActivity)
                linearLayout.apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                var text = TextView(this@MainActivity)
                text.apply {
                    setText(" 件名: ")
                }
                var titleText = EditText(this@MainActivity)
                titleText.apply {
                    width = 400
                    titleText.inputType = InputType.TYPE_CLASS_TEXT
                }

                var dateTextView = TextView(this@MainActivity)
                dateTextView.apply {
                    setText(" 日付: ")
                }
                var dateEditText = EditText(this@MainActivity)
                dateEditText.apply {
                    width = 350
                    inputType = InputType.TYPE_CLASS_DATETIME
                    isFocusable = false
                    gravity = Gravity.CENTER
                }
                linearLayout.addView(text)
                linearLayout.addView(titleText)
                linearLayout.addView(dateTextView)
                linearLayout.addView(dateEditText)
                var now = LocalDate.now()
                dateEditText.setOnClickListener {
                    DatePickerDialog(
                        this@MainActivity, { _, i, i2, i3 ->
                            var today = String.format("%04d-%02d-%02d", i, i2 + 1, i3)
                            if (LocalDate.parse(today) < LocalDate.now()) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "今日以降を選択してください",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@DatePickerDialog
                            }
                            dateEditText.setText(today)
                        },
                        now.year,
                        now.monthValue - 1,
                        now.dayOfMonth
                    ).show()
                }
                AlertDialog.Builder(this@MainActivity).apply {
                    setTitle("追加")
                    setMessage("件名と日付を入力してください\n※日付は空欄で期限なし")
                    setView(linearLayout)
                    setPositiveButton("追加") { _, _ ->
                        if (titleText.text.isEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "件名を入力してください",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setPositiveButton
                        }
                        var date = if (dateEditText.text.isEmpty()) "期限なし"
                        else dateEditText.text.toString()
                        list.add(Item(idNumber++, titleText.text.toString(), date, false))
                        var comparator =
                            compareByDescending<Item> { a -> a.IsFavorite }.thenBy { a -> a.getDate() }
                                .thenBy { a -> a.ID }
                        var tmp = list.sortedWith(comparator)
                        list.clear()
                        list.addAll(tmp)
                        editor.putString("DATA", gson.toJson(list))
                        editor.putInt("ID", idNumber)
                        editor.commit()
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this@MainActivity,"${titleText.text}を追加しました",Toast.LENGTH_SHORT).show()
                    }
                    setNegativeButton("キャンセル") { _, _ ->
                        return@setNegativeButton
                    }
                    create()
                    show()
                }
            }
        }.root)
    }
}