package com.github.bryanser.dec0mpose.refine

import com.github.bryanser.brapi.Utils
import com.github.bryanser.brapi.kview.KViewContext
import com.github.bryanser.brapi.kview.KViewHandler
import com.github.bryanser.dec0mpose.Dec0mpose
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack


class RefineViewContext(p: Player) : KViewContext("§6精炼") {
    fun update() {
        for (s in slots) {
            s.update(null to null, 0)
        }
        _update()
        KViewHandler.updateUI(player)
    }

    val slots = Array(5) {
        SlotState(it)
    }

    private fun _update() {
        val item = inventory.getItem(SLOT) ?: return
        if (!item.hasItemMeta()) {
            return
        }
        val im = item.itemMeta
        if (!im.hasLore()) {
            return
        }
        val lore = im.lore

        var lv = -1.0
        for(s in lore){
            if(s.contains(RefineSetting.itemLevel)){
                val str = s.split(RefineSetting.itemLevel)
                val v = ChatColor.stripColor(str[1])
                lv = v.toDoubleOrNull() ?: continue
                break
            }
        }
        if(lv < 0){
            return
        }
        var curr = 0
        for ((i, s) in lore.withIndex()) {
            if (curr >= 5) {
                break
            }
            val slot = RefineSetting.matchSlot(s) ?: continue
            slots[curr++].update(slot, i)
        }
    }

    inner class SlotState(
            private val index: Int
    ) {
        var slot: Slot? = null
        var value: Double? = null
        var loreIndex: Int = 0
        fun update(pair: Pair<Slot?, Double?>, loreIndex: Int) {
            slot = pair.first
            value = pair.second
            this.loreIndex = loreIndex
            removed = false
        }

        fun display(): ItemStack {
            slot?.also {
                value?.also { v ->
                    return RefineSetting.installed(it.original, it.makeLore(v))
                }
                return RefineSetting.able(it.original, null)
            }
            return RefineSetting.disable
        }

        fun cancel(): Boolean {
            if (slot == null) {
                return true
            }
            if (value == null) {
                val c = player.openInventory.cursor
                val item = RefineSetting.matchStone(c) ?: return true
                if (c.amount > 1) {
                    player.sendMessage("§c一次只能放入一个物品哦")
                    return true
                }
                install(item)
                player.openInventory.cursor = null
                return true
            }
            return true
        }

        fun install(item: Item) {
            val stack = inventory.getItem(SLOT)?.clone() ?: return
            val im = stack.itemMeta
            val lore = im.lore
            var lv = -1.0
            for(s in lore){
                if(s.contains(RefineSetting.itemLevel)){
                    val str = s.split(RefineSetting.itemLevel)
                    val v = ChatColor.stripColor(str[1]).replace("[^0-9.]".toRegex(),"")
                    lv = v.toDoubleOrNull() ?: continue
                    break
                }
            }
            if(lv < 0){
                return
            }
            val value = RefineSetting.getItemValue(item, slot ?: return,lv)
            lore[loreIndex] = slot!!.makeLore(value)
            im.lore = lore
            stack.itemMeta = im
            inventory.setItem(SLOT, stack)
            update()
        }

        var removed = false

        fun onShiftClick() {
            if(slot == null || removed){
                return
            }
            val stack = inventory.getItem(SLOT)?.clone() ?: return
            val im = stack.itemMeta
            val lore = im.lore
            lore[loreIndex] = slot!!.original
            im.lore = lore
            stack.itemMeta = im
            inventory.setItem(SLOT, stack)
            update()
            removed = true
            player.sendMessage("§6移除成功")
        }

    }
}

const val SLOT = 10
const val INFO = 26

@Suppress("Unsupported")
val SLOTINDEX = [12, 13, 14, 15, 16]

val RefineView = KViewHandler.createKView("Refine View", 3, ::RefineViewContext) {
    onClose {
        Utils.safeGiveItem(player, inventory.getItem(SLOT))
    }
    for (i in 0 until 27) {
        icon(i) {
            initDisplay {
                RefineSetting.decoration
            }
        }
    }
    slotIcon(SLOT) {
        cancelClick {
            Bukkit.getScheduler().runTaskLater(Dec0mpose.Plugin, {
                update()
            }, 3)
            false
        }
    }
    icon(11) {
        initDisplay {
            RefineSetting.info
        }
    }
    for ((index, i) in SLOTINDEX.withIndex()) {
        icon(i) {
            initDisplay {
                slots[index].display()
            }
            cancelClick {
                slots[index].cancel()
            }
            click(ClickType.SHIFT_RIGHT) {
                slots[index].onShiftClick()
            }
        }
    }
    icon(INFO) {
        initDisplay {
            RefineSetting.other
        }
    }
}

