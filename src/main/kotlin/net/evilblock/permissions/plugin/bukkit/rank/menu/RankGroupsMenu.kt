package net.evilblock.permissions.plugin.bukkit.rank.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.plugin.bukkit.rank.conversation.RankCreationPrompt
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

class RankGroupsMenu : Menu() {

    init {
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String {
        return "Rank Groups"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()
        buttons[0] = CreateRankButton()

        for (group in EvilPermissions.instance.plugin.getActiveGroups()) {
            buttons[buttons.size] = RankGroupButton(group)
        }

        return buttons
    }

    private class CreateRankButton : Button() {
        override fun getMaterial(player: Player): Material {
            return Material.NETHER_STAR
        }

        override fun getName(player: Player): String {
            return "${ChatColor.YELLOW}${ChatColor.BOLD}Create New Rank"
        }

        override fun getDescription(player: Player): List<String> {
            return emptyList()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            player.closeInventory()

            val factory = ConversationFactory(BukkitPlugin.instance)
                .withFirstPrompt(RankCreationPrompt())
                .withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage("Go away evil console!")

            player.beginConversation(factory.buildConversation(player))
        }
    }

    private class RankGroupButton(private val group: String) : Button() {
        override fun getName(player: Player): String {
            val active = if (EvilPermissions.instance.plugin.getActiveGroups().contains(group)) {
                "${ChatColor.GREEN}${ChatColor.BOLD}(Active)"
            } else {
                "${ChatColor.RED}${ChatColor.RED}(Inactive)"
            }

            return "${ChatColor.BLUE}${ChatColor.BOLD}$group $active"
        }

        override fun getMaterial(player: Player): Material {
            return Material.HOPPER
        }

        override fun applyMetadata(player: Player, itemMeta: ItemMeta): ItemMeta? {
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true) // b = ignoreLevelRestrictions (same as addUnsafeEnchantment)
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            return itemMeta
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            RanksMenu(group).openMenu(player)
        }
    }

}