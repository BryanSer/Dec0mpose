package com.github.bryanser.dec0mpose.refine

import java.text.NumberFormat

data class Slot(
    val original:String,
    val result:List<String>,
    val expression: String,
    val numberFormat:NumberFormat
){
    fun makeLore(value:Double):String{
        return result[0] + numberFormat.format(value) + result[1]
    }
}
