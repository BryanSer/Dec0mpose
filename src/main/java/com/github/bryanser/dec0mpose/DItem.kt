package com.github.bryanser.dec0mpose

import com.github.bryanser.brapi.util.KConfigurationSerializable
import org.bukkit.inventory.ItemStack

class DItem : KConfigurationSerializable {
    lateinit var item: ItemStack

    @Transient
    val condition = mutableListOf<Int>()
    var _condition: String
        get() = condition.joinToString(",")
        set(value) {
            val list = value.split(",")
            condition.addAll(list.mapNotNull { it.toIntOrNull() })
        }
    var cost = 1.0


    constructor(item: ItemStack, condition:  String, cost: Double) {
        this.item = item
        _condition = (condition)
        this.cost = cost
    }

    constructor(args: Map<String, Any?>) {
        args.deserialize()
    }
}