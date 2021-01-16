package com.github.bryanser.dec0mpose

import com.github.bryanser.brapi.ItemBuilder
import com.github.bryanser.brapi.Utils
import com.github.bryanser.brapi.kview.KViewContext
import com.github.bryanser.brapi.kview.KViewHandler
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class Dec0mposeViewContext(p: Player) : KViewContext("§6分解") {
    val display = mutableListOf<ItemStack>()
    fun update() {
        _update()
        KViewHandler.updateUI(player)
    }

    private fun _update() {

        display.clear()
        val item = inventory.getItem(SLOT) ?: return
        var (value, list) = Setting.getItemValue(item) ?: return
        for (d in Setting.dItems) {
            if (!d.condition.any { it in list }) {
                continue
            }
            var amount = 0
            while (value >= d.cost) {
                amount++
                value -= d.cost
            }
            if (amount > 0) {
                display.add(d.item.clone().also {
                    it.amount = amount
                })
            }
        }
    }
}

const val SLOT = 10
const val BOTTOM = 16
const val INFO = 26

@Suppress("Unsupported")
val RESULT = [4,5,6,13,14,15,22,23,24]
val Dec0mposeView = KViewHandler.createKView("Dec0mpose View", 3, ::Dec0mposeViewContext) {
    val galss = ItemBuilder.createItem(Material.STAINED_GLASS_PANE,durability = 15) {
        name(" ")
        lore{
            +"§0skill_0"
        }
    }
    onClose {
        Utils.safeGiveItem(player, inventory.getItem(SLOT))
    }
    for (i in 0 until 27) {
        icon(i) {
            initDisplay(galss)
        }
    }
    slotIcon(SLOT) {
        cancelClick {
            if (player.openInventory.cursor == null) {
                Bukkit.getScheduler().runTaskLater(Dec0mpose.Plugin, {
                    update()
                }, 6)
            } else {
                Bukkit.getScheduler().runTaskLater(Dec0mpose.Plugin, {
                    update()
                }, 3)
            }
            false
        }
    }
    icon(11) {
        initDisplay {
            Setting.result
        }
    }
    icon(BOTTOM) {
        initDisplay {
            Setting.buttom
        }
        click {
            val item = inventory.getItem(SLOT)?.clone() ?: return@click
            update()
            if (display.isNotEmpty()) {
                for (i in display) {
                    Utils.safeGiveItem(player, i)
                }
                if (item.amount == 1) {
                    inventory.setItem(SLOT, null)
                } else {
                    item.amount--
                    inventory.setItem(SLOT, item)
                }
                player.playSound(player.location, Sound.BLOCK_PISTON_EXTEND, 1f, 1f)
                update()
            }
        }
    }
    icon(INFO) {
        initDisplay {
            Setting.info
        }
    }
    for ((index, slot) in RESULT.withIndex()) {
        icon(slot) {
            initDisplay {
                display.getOrNull(index)
            }
        }
    }
}