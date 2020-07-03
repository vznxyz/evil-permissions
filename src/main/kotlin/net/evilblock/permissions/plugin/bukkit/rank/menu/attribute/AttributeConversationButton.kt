package net.evilblock.permissions.plugin.bukkit.rank.menu.attribute

import net.evilblock.cubed.menu.Button
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.plugin.bukkit.rank.menu.EditAttributesMenu
import net.evilblock.permissions.plugin.bukkit.rank.menu.attribute.conversation.EditAttributeConversation
import org.bukkit.Material
import org.bukkit.conversations.ConversationFactory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

class AttributeConversationButton(private val parent: EditAttributesMenu, private val attribute: EditAttributeConversation.Attribute, private val material: Material, private val rank: Rank) : Button() {

    override fun getMaterial(player: Player): Material {
        return material
    }

    override fun getName(player: Player): String {
        return attribute.buttonTitle
    }

    override fun getDescription(player: Player): List<String> {
        return emptyList()
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        player.closeInventory()

        val factory = ConversationFactory(BukkitPlugin.instance)
                .withFirstPrompt(EditAttributeConversation(parent, attribute, rank))
                .withLocalEcho(false)
                .thatExcludesNonPlayersWithMessage("Go away evil console!")

        player.beginConversation(factory.buildConversation(player))
    }

}