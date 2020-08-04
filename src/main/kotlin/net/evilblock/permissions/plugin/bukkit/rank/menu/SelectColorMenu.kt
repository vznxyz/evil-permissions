package net.evilblock.permissions.plugin.bukkit.rank.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.bukkit.enchantment.GlowEnchantment
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.plugin.bukkit.util.ColorMap
import org.apache.commons.lang.WordUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class SelectColorMenu(private val parent: EditAttributesMenu, private val rank: Rank) : Menu("Select Color") {

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for ((color, damage) in ColorMap.woolMap) {
            buttons[buttons.size] = ColorButton(parent, rank, color, damage)
        }

        return buttons
    }

    class OpenButton(private val parent: EditAttributesMenu, private val rank: Rank) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            val itemStack = super.getButtonItem(player)

            if (rank.getDisplayColorChar() == "4") {
                GlowEnchantment.addGlow(itemStack)
            }

            return itemStack
        }

        override fun getMaterial(player: Player): Material {
            return Material.WOOL
        }

        override fun getDamageValue(player: Player): Byte {
            val colorChar = if (rank.getDisplayColor().isEmpty()) 'f' else rank.getDisplayColorChar()[0]
            return (ColorMap.woolMap[ChatColor.getByChar(colorChar)] ?: 15).toByte()
        }

        override fun getName(player: Player): String {
            return "${ChatColor.AQUA}Display Color"
        }

        override fun getDescription(player: Player): List<String> {
            return emptyList()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            SelectColorMenu(parent, rank).openMenu(player)
        }
    }

    class ColorButton(private val parent: EditAttributesMenu, private val rank: Rank, private val color: ChatColor, private val damage: Int) : Button() {

        override fun getMaterial(player: Player): Material {
            return Material.WOOL
        }

        override fun getDamageValue(player: Player): Byte {
            return damage.toByte()
        }

        override fun getName(player: Player): String {
            return color.toString() + WordUtils.capitalizeFully(color.name.replace("_", " ").toLowerCase())
        }

        override fun getDescription(player: Player): List<String> {
            return emptyList()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            player.closeInventory()

            rank.setDisplayColor(color.toString())

            BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
                EvilPermissions.instance.database.saveRank(rank)
            }

            parent.openMenu(player)
        }

    }

}