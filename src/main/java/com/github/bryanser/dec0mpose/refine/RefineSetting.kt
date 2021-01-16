package com.github.bryanser.dec0mpose.refine

import Br.API.Calculator
import com.github.bryanser.brapi.ItemBuilder
import com.github.bryanser.dec0mpose.Dec0mpose
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.awt.geom.Ellipse2D
import java.io.File
import java.text.DecimalFormat

object RefineSetting {

    val itemList = mutableListOf<Item>()

    fun matchStone(item:ItemStack?):Item?{
        if(item == null){
            return null
        }
        if(!item.hasItemMeta()){
            return null
        }
        val im = item.itemMeta
        if(!im.hasDisplayName()){
            return null
        }
        val name = im.displayName
        for(i in itemList){
            if(i.name == name){
                return i
            }
        }
        return null
    }

    val slotPosition = mutableListOf<Slot>()

    fun matchSlot(lore:String):Pair<Slot?,Double?>?{
        for(slot in slotPosition){
            if(lore == slot.original){
                return slot to null
            }
            if(lore.startsWith(slot.result[0]) && lore.endsWith(slot.result[1])){
                val rem = lore.replace(slot.result[0],"").replace(slot.result[1],"")
                return slot to slot.numberFormat.parse(rem).toDouble()
            }
        }
        return null
    }

    lateinit var info: ItemStack
    lateinit var disable: ItemStack
    lateinit var decoration: ItemStack
    lateinit var able: (String, String?) -> ItemStack
    lateinit var installed: (String, String?) -> ItemStack
    lateinit var other: ItemStack
    lateinit var itemLevel:String

    fun loadConfig() {
        itemList.clear()
        slotPosition.clear()
        val f = File(Dec0mpose.Plugin.dataFolder, "refineConfig.yml")
        if (!f.exists()) {
            Dec0mpose.Plugin.saveResource("refineConfig.yml", false)
        }
        val config = YamlConfiguration.loadConfiguration(f)
        itemLevel = ChatColor.translateAlternateColorCodes('&', config.getString("ItemLevel"))
        config.getConfigurationSection("Items").also {
            for (key in it.getKeys(false)) {
                val sub = it.getConfigurationSection(key)
                val item = Item(sub.getInt("level"), ChatColor.translateAlternateColorCodes('&', sub.getString("name")), sub.getInt("value"))
                itemList.add(item)
            }
        }

        config.getConfigurationSection("SlotPosition").also {
            for (key in it.getKeys(false)) {
                val sub = it.getConfigurationSection(key)

                val slot = Slot(
                        ChatColor.translateAlternateColorCodes('&', sub.getString("text.original")),
                        ChatColor.translateAlternateColorCodes('&', sub.getString("text.result")).split("%var%", limit = 2),
                        sub.getString("expression"),
                        DecimalFormat(sub.getString("format"))
                )
                slotPosition.add(slot)
            }
        }
        config.getConfigurationSection("UI").apply {
            info = loadItem(getConfigurationSection("info"))
            disable = loadItem(getConfigurationSection("disable"))
            decoration = loadItem(getConfigurationSection("decoration"))
            other = loadItem(getConfigurationSection("other"))
            able = loadItemVar(getConfigurationSection("able"))
            installed = loadItemVar(getConfigurationSection("installed"))
        }
    }


    fun getItemValue(item: Item, slot: Slot,lv:Double): Double {
        val exp = slot.expression.replace("value", item.value.toString()).replace("level", item.level.toString()).replace("itemLevel",lv.toString())
        return Calculator.conversion(exp)
    }


    fun loadItemVar(cs: ConfigurationSection): (String, String?) -> ItemStack {
        return { ori, res ->
            ItemBuilder.createItem(
                    Material.getMaterial(cs.getInt("id")),
                    durability = cs.getInt("durability", 0)
            ) {
                unbreakable(cs.getBoolean("unbreakable", false))
                if (cs.contains("name")) {
                    name(ChatColor.translateAlternateColorCodes('&', cs.getString("name"))
                        .replace("%original%", ori).replace("%result%", res ?: "无"))
                }
                if (cs.contains("lore")) {
                    lore {
                        for (s in cs.getStringList("lore")) {
                            +ChatColor.translateAlternateColorCodes('&', s)
                                .replace("%original%", ori).replace("%result%", res ?: "无")
                        }
                    }
                }
            }
        }
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