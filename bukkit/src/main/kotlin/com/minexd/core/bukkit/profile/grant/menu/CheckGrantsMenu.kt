package com.minexd.core.bukkit.profile.grant.menu

import net.evilblock.cubed.Cubed
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import com.minexd.core.bukkit.profile.grant.menu.button.GrantButton
import com.minexd.core.profile.Profile
import com.minexd.core.profile.ProfileHandler
import com.minexd.core.profile.grant.Grant
import com.minexd.core.profile.grant.GrantQueryResult
import org.bukkit.entity.Player
import java.util.*

class CheckGrantsMenu(private val issuedBy: UUID, private val grants: List<GrantQueryResult>) : PaginatedMenu() {

    internal data class GrantResourceData(val issuer: Profile?, val remover: Profile?)

    private val resources = hashMapOf<Grant, GrantResourceData>()

    init {
        async = true
    }

    override fun asyncLoadResources(callback: (Boolean) -> Unit) {
        try {
            for (queryResult in grants) {
                var issuer: Profile? = null
                var remover: Profile? = null

                if (queryResult.grant.issuedBy != null) {
                    issuer = try {
                        ProfileHandler.getOrFetchProfile(queryResult.grant.issuedBy!!)
                    } catch (e: Exception) {
                        null
                    }
                }

                if (queryResult.grant.removed) {
                    remover = try {
                        ProfileHandler.getOrFetchProfile(queryResult.grant.removedBy!!)
                    } catch (e: Exception) {
                        null
                    }
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
                    profile = queryResult.profile,
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