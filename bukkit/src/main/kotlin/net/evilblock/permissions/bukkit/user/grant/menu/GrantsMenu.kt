package net.evilblock.permissions.bukkit.user.grant.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.bukkit.user.grant.menu.button.GrantButton
import net.evilblock.permissions.user.User
import net.evilblock.permissions.user.UserHandler
import net.evilblock.permissions.user.grant.Grant
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.lang.Exception
import kotlin.collections.HashMap

class GrantsMenu(private val target: User) : PaginatedMenu() {

    internal data class GrantResourceData(val issuer: User?, val remover: User?)

    private val resources = hashMapOf<Grant, GrantResourceData>()

    init {
        async = true
    }

    override fun asyncLoadResources(callback: (Boolean) -> Unit) {
        try {
            for (grant in target.grants) {
                var issuer: User? = null
                var remover: User? = null

                if (grant.issuedBy != null) {
                    issuer = UserHandler.loadOrCreate(grant.issuedBy!!)
                }

                if (grant.removedBy != null) {
                    remover = UserHandler.loadOrCreate(grant.removedBy!!)
                }

                resources[grant] = GrantResourceData(issuer, remover)
            }

            callback.invoke(true)
        } catch (e: Exception) {
            callback.invoke(false)
        }
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "${ChatColor.RED}Grants - ${target.getUsername()}"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = HashMap<Int, Button>()

        val grants = target.grants.sortedBy { -it.issuedAt }
        for (grant in grants) {
            buttons[buttons.size] = GrantButton(this, target, grant, resources[grant]?.issuer, resources[grant]?.remover)
        }

        return buttons
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 36
    }

}