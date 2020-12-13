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

const val SLOT = 4
const val BOTTOM = 16
const val INFO = 44

@Suppress("Unsupported")
val RESULT = [30, 31, 32, 39, 40, 41, 48, 49, 50]
val Dec0mposeView = KViewHandler.createKView("Dec0mpose View", 6, ::Dec0mposeViewContext) {
    val galss = ItemBuilder.createItem(Material.STAINED_GLASS_PANE) {
        name(" ")
    }
    onClose {
        Utils.safeGiveItem(player, inventory.getItem(SLOT))
    }
    for (i in 0 until 54) {
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
    icon(13) {
        initDisplay {
            Setting.result
        }
    }
    icon(22) {
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