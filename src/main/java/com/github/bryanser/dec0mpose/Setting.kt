package com.github.bryanser.dec0mpose

import Br.API.Calculator
import com.github.bryanser.brapi.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

object Setting {
    lateinit var levelFormat: String
    val color = mutableListOf<Triple<Int, String, Double>>()
    lateinit var formula: (Double, Double) -> Double
    val dItems = mutableListOf<DItem>()


    fun getItemValue(item: ItemStack): Pair<Double,List<Int>>? {
        var lv = -1.0
        var cv = -1.0
        val value = mutableListOf<Int>()
        if (!item.hasItemMeta()) {
            return null
        }
        val im = item.itemMeta
        if (!im.hasLore()) {
            return null
        }

        for (s in im.lore) {
            if (s.contains(levelFormat)) {
                val str = s.split(levelFormat, limit = 2)
                val v = ChatColor.stripColor(str[1].replace(" ", ""))
                lv = v.toDouble()

            } else {
                for((i,t,v) in color){

                    if(s.contains(t)){
                         cv += v
                        value.add(i)
                    }
                }
            }
        }
        if(lv < 0 || cv < 0){
            return null
        }

        return formula(lv,cv) to value
    }


    lateinit var info: ItemStack
    lateinit var result: ItemStack
    lateinit var buttom: ItemStack

    fun loadConfig() {
        val f = File(Dec0mpose.Plugin.dataFolder, "config.yml")
        if (!f.exists()) {
            Dec0mpose.Plugin.saveDefaultConfig()
        }
        val config = YamlConfiguration.loadConfiguration(f)
        levelFormat = ChatColor.translateAlternateColorCodes('&', config.getString("Setting.LevelFormat"))
        color.clear()
        config.getConfigurationSection("Setting.Color").also {
            for (key in it.getKeys(false)) {
                val sub = it.getConfigurationSection(key)
                val i = key.toInt()
                color.add(Triple(i, ChatColor.translateAlternateColorCodes('&', sub.getString("lore")), sub.getDouble("value")))
            }
        }
        formula = config.getString("Setting.Formula").let {
            fun(x: Double, y: Double): Double {
                return Calculator.conversion(it.replace("level", x.toString()).replace("color", y.toString()))
            }
        }
        info = loadItem(config.getConfigurationSection("UI.info"))
        result = loadItem(config.getConfigurationSection("UI.result"))
        buttom = loadItem(config.getConfigurationSection("UI.buttom"))

    }

    fun loadDItem() {
        dItems.clear()
        val f = File(Dec0mpose.Plugin.dataFolder, "ditem.yml")
        if (f.exists()) {
            val config = YamlConfiguration.loadConfiguration(f)
            for (key in config.getKeys(false)) {
                dItems.add(config[key] as DItem)
            }
            dItems.sortByDescending {
                it.cost
            }
        }
    }

    fun saveDItem() {
        dItems.sortByDescending {
            it.cost
        }
        val f = File(Dec0mpose.Plugin.dataFolder, "ditem.yml")
        val config = YamlConfiguration()
        for ((i, d) in dItems.withIndex()) {
            config[i.toString()] = d
        }

        config.save(f)
    }

    fun loadItem(cs: ConfigurationSection): ItemStack {
        return ItemBuilder.createItem(
                Material.getMaterial(cs.getInt("id")),
                durability = cs.getInt("durability", 0)
        ) {
            unbreakable(cs.getBoolean("unbreakable", false))
            if (cs.contains("name")) {
                name(ChatColor.translateAlternateColorCodes('&', cs.getString("name")))
            }
            if (cs.contains("lore")) {
                lore {
                    for (s in cs.getStringList("lore")) {
                        +ChatColor.translateAlternateColorCodes('&', s)
                    }
                }
            }
        }
    }


}