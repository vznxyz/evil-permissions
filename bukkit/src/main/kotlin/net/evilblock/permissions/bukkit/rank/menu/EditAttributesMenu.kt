package net.evilblock.permissions.bukkit.rank.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.BackButton
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.bukkit.rank.menu.attribute.*
import net.evilblock.permissions.bukkit.rank.menu.attribute.conversation.EditAttributeConversation
import org.bukkit.Material
import org.bukkit.entity.Player

class EditAttributesMenu(private val parent: RanksMenu, val rank: Rank) : Menu("Edit Rank Attributes") {

    init {
        updateAfterClick = true
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        buttons[9] = AttributeConversationButton(this, EditAttributeConversation.Attribute.DISPLAY_NAME, Material.SIGN, rank)
        buttons[10] = DisplayOrderAttributeButton(rank)
        buttons[11] = AttributeConversationButton(this, EditAttributeConversation.Attribute.PREFIX, Material.NAME_TAG, rank)
        buttons[12] = AttributeConversationButton(this, EditAttributeConversation.Attribute.PLAYER_LIST_PREFIX, Material.PAPER, rank)
        buttons[13] = SelectColorMenu.OpenButton(this, rank)
        buttons[14] = EditPermissionsButton(this, rank)
        buttons[15] = EditGroupsButton(this, rank)
        buttons[16] = SelectInheritanceMenu.OpenButton(this, rank)
        buttons[17] = HiddenAttributeButton(rank)
        buttons[18] = DefaultAttributeButton(rank)
        buttons[19] = StaffAttributeButton(rank)

        // back button positioned on right
        buttons[8] = BackButton { player ->
            parent.openMenu(player)
        }

        // toolbar placeholders
        for (i in 0..8) {
            if (!buttons.containsKey(i)) {
                buttons[i] = Button.placeholder(Material.STAINED_GLASS_PANE, 8, " ")
            }
        }

        return buttons
    }

}