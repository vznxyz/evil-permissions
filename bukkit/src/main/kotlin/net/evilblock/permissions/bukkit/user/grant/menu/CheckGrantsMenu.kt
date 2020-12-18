package net.evilblock.permissions.bukkit.user.grant.menu

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.database.result.IssuedByQueryResult
import net.evilblock.permissions.bukkit.user.grant.menu.button.GrantButton
import net.evilblock.permissions.user.User
import net.evilblock.permissions.user.UserHandler
import net.evilblock.permissions.user.grant.Grant
import org.bukkit.entity.Player
import java.lang.Exception
import java.util.*

class CheckGrantsMenu(private val issuedBy: UUID, private val grants: List<IssuedByQueryResult>) : PaginatedMenu() {

    internal data class GrantResourceData(val issuer: User?, val remover: User?)

    private val resources = hashMapOf<Grant, GrantResourceData>()

    init {
        async = true
    }

    override fun asyncLoadResources(callback: (Boolean) -> Unit) {
        try {
            for (queryResult in grants) {
                var issuer: User? = null
                var remover: User? = null

                if (queryResult.grant.issuedBy != null) {
                    issuer = UserHandler.loadOrCreate(queryResult.grant.issuedBy!!)
                }

                if (queryResult.grant.removedBy != null) {
                    remover = UserHandler.loadOrCreate(queryResult.grant.removedBy!!)
                }

                resources[queryResult.grant] = GrantResourceData(issuer, remover)
            }

            callback.invoke(true)
        } catch (e: Exception) {
            callback.invoke(false)
        }
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Staff History of ${Cubed.instance.uuidCache.name(issuedBy)}"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        for (queryResult in grants.sortedBy { -it.grant.issuedAt }) {
            buttons[buttons.size] = GrantButton(
                returnTo = this,
                user = queryResult.user,
                grant = queryResult.grant,
                issuedBy = resources[queryResult.grant]?.issuer,
                removedBy = resources[queryResult.grant]?.remover,
                appendIssuedTo = true
            )
        }

        return buttons
    }

    override fun getMaxItemsPerPage(player: Player): Int {
        return 36
    }

}