package com.minexd.core.bukkit.profile.punishment.menu

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import com.minexd.core.bukkit.profile.punishment.menu.button.PunishmentButton
import com.minexd.core.profile.punishment.PunishmentQueryResult
import org.bukkit.entity.Player
import java.util.*

class PunishmentsIssuedByMenu(private val issuedBy: UUID, private val punishments: List<PunishmentQueryResult>) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "Staff History of ${Cubed.instance.uuidCache.name(issuedBy)}"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for (queryResult in punishments.sortedBy { -it.punishment.issuedAt }) {
            buttons[buttons.size] = PunishmentButton(this, queryResult.user, queryResult.punishment, true)
        }

        return buttons
    }

}