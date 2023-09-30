package com.example.belongingscheck

import java.lang.Exception
import java.time.LocalDate

data class Item(
    var ID: Int = 0,
    var Title: String = "",
    var Date: String = "",
    var IsFavorite:Boolean=false
) {
    fun getDate(): LocalDate {
        if(Date == "期限なし") return LocalDate.parse("1111-11-11")
        return LocalDate.parse(Date)
    }
}

data class Check(
    var ID: Int = 0,
    var Item: String = "",
    var Amount: Int = 0,
    var IsCheck: Boolean = false
)