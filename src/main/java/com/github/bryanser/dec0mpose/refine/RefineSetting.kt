package com.github.bryanser.dec0mpose.refine

import Br.API.Calculator
import com.github.bryanser.dec0mpose.Dec0mpose
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

object RefineSetting {

    var itemList = mutableListOf<Item>()

    var SlotPosition = mutableListOf<Slot>()


    fun loadConfig() {
        val f = File(Dec0mpose.Plugin.dataFolder, "refineConfig.yml")
        if (!f.exists()) {
            Dec0mpose.Plugin.saveDefaultConfig()
        }
        val config = YamlConfiguration.loadConfiguration(f)
        config.getConfigurationSection("Items").also {
            for (key in it.getKeys(false)) {
                val sub = it.getConfigurationSection(key)
                val item = Item(sub.getInt("level"), sub.getString("name"), sub.getInt("value"))
                itemList.add(item)
            }
        }

        config.getConfigurationSection("SlotPosition").also {
            for (key in it.getKeys(false)) {
                val sub = it.getConfigurationSection(key)

                val cs = sub.getConfigurationSection("text")
                val text = mutableMapOf<Int, String>()
                for (t in cs.getKeys(false)) {
                    val i = t.toInt()
                    val str = cs.getString(t)
                    text[i] = str
                }

                val slot = Slot(text, sub.getString("formula"))
                SlotPosition.add(slot)
            }
        }
    }


    fun getItemValue(item: Item, slot: Slot): Double {
        return Calculator.conversion(slot.formula.replace("value", item.value.toString()))
    }



}