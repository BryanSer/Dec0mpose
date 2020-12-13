package com.github.bryanser.dec0mpose

import com.github.bryanser.dec0mpose.refine.RefineSetting
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class Dec0mpose : JavaPlugin() {
    override fun onLoad() {
        Plugin = this
        ConfigurationSerialization.registerClass(DItem::class.java)
    }

    override fun onEnable() {
        Setting.loadConfig()
        Setting.loadDItem()
        RefineSetting.loadConfig()
    }

    override fun onDisable() {

    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            if (sender is Player) {
                Dec0mposeView.openView(sender)
                return true
            }
            return !sender.isOp
        }
        if (!sender.isOp) {
            if (sender is Player) {
                Dec0mposeView.openView(sender)
            }
            return true
        }
        if (args[0].equals("open", true) && args.size >= 2) {
            val target = Bukkit.getPlayerExact(args[1]) ?: run{
                sender.sendMessage("§c找不到玩家${args[1]}")
                return true
            }
            Dec0mposeView.openView(target)
            return true
        }
        if (args[0].equals("reload", true)) {
            Setting.loadConfig()
            Setting.loadDItem()

            RefineSetting.loadConfig()

            sender.sendMessage("§6重载完成")
            return true
        }
        if (args[0].equals("add", true) && args.size >= 3 && sender is Player) {
            val value = args[1].toDoubleOrNull() ?: run {
                sender.sendMessage("§6你输入的第二个参数不是浮点数")
                return true
            }
            val item = sender.inventory.itemInMainHand ?: run {
                sender.sendMessage("§6你手上什么都没有")
                return true
            }
            val ditem = DItem(item.clone(), args[2], value)
            Setting.dItems.add(ditem)
            Setting.saveDItem()
            sender.sendMessage("§6添加成功")
            return true
        }

        return false
    }

    companion object {
        lateinit var Plugin: Dec0mpose
    }
}