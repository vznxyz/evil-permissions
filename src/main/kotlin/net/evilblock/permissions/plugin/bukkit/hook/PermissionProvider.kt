package net.evilblock.permissions.plugin.bukkit.hook

import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.plugin.bukkit.BukkitPlugin
import net.evilblock.permissions.plugin.bukkit.user.grant.event.GrantCreateEvent
import net.evilblock.permissions.user.grant.Grant
import net.milkbowl.vault.permission.Permission
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.ServicePriority
import java.util.*

object PermissionProvider : Permission() {

    @JvmStatic
    fun hook() {
        Bukkit.getServicesManager().register(Permission::class.java, this, BukkitPlugin.instance, ServicePriority.Highest)
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun hasSuperPermsCompat(): Boolean {
        return true
    }

    override fun hasGroupSupport(): Boolean {
        return true
    }

    override fun getGroups(): Array<String> {
        return EvilPermissions.instance.rankHandler.getRanks().map { it.id }.toTypedArray()
    }

    override fun getPlayerGroups(player: Player): Array<String> {
        val groups = arrayListOf<String>()
        val user = EvilPermissions.instance.userHandler.getByUniqueId(player.uniqueId)!!

        user.grants
            .filter { grant -> grant.isActive() }
            .forEach { groups.add(it.rank.id) }

        return groups.toTypedArray()
    }

    // TODO: ASYNC OFFLINE SUPPORT
    override fun getPlayerGroups(world: String, player: OfflinePlayer): Array<String> {
        return emptyArray()
    }

    override fun getPlayerGroups(world: String, playerName: String): Array<String> {
        val uuid = UUID.fromString(playerName) ?: return emptyArray()
        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        return getPlayerGroups(world, offlinePlayer)
    }

    override fun getPrimaryGroup(player: Player): String {
        val user = EvilPermissions.instance.userHandler.getByUniqueId(player.uniqueId)!!
        return user.getBestDisplayRank().id
    }

    // TODO: ASYNC OFFLINE SUPPORT
    override fun getPrimaryGroup(world: String?, player: OfflinePlayer): String {
        return EvilPermissions.instance.rankHandler.getDefaultRank().id
    }

    override fun getPrimaryGroup(world: String?, playerName: String): String? {
        val uuid = UUID.fromString(playerName) ?: return null
        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        return getPrimaryGroup(world, offlinePlayer)
    }

    override fun groupAdd(world: World, group: String, permission: String): Boolean {
        val rank = EvilPermissions.instance.rankHandler.getRankById(group)

        return if (rank != null) {
            val success = rank.permissions.add(permission)
            if (success) {
                Tasks.async {
                    EvilPermissions.instance.database.saveRank(rank)
                }
            }
            return success
        } else {
            false
        }
    }

    override fun groupHas(world: World, group: String, permission: String): Boolean {
        val rank = EvilPermissions.instance.rankHandler.getRankById(group)
        return rank?.permissions?.contains(permission) ?: false
    }

    override fun groupRemove(world: World, group: String, permission: String): Boolean {
        val rank = EvilPermissions.instance.rankHandler.getRankById(group)

        return if (rank != null) {
            val success = rank.permissions.remove(permission)
            if (success) {
                Tasks.async {
                    EvilPermissions.instance.database.saveRank(rank)
                }
            }
            return success
        } else {
            false
        }
    }

    override fun playerAddGroup(player: Player, group: String): Boolean {
        val rank = EvilPermissions.instance.rankHandler.getRankById(group)
        if (rank == null) {
            BukkitPlugin.instance.logger.severe("[Vault Hook] Couldn't add group \"$group\" to player because it doesn't exist.")
            return false
        }

        val user = EvilPermissions.instance.userHandler.getByUniqueId(player.uniqueId)
        if (user != null) {
            // TODO: MAKE VAULT GRANT'S DEFAULT DURATION CONFIGURABLE
            val grant = Grant()
            grant.rank = rank
            grant.reason = "VaultAPI Hook"
            grant.issuedAt = System.currentTimeMillis()

            user.grants.add(grant)
            user.requiresSave = true
            user.requiresApply = true

            Bukkit.getServer().pluginManager.callEvent(GrantCreateEvent(player, grant))

            return true
        }

        return false
    }

    override fun playerAddGroup(world: String?, player: String, group: String): Boolean {
        val bukkitPlayer = Bukkit.getPlayer(player)
        if (bukkitPlayer != null) {
            return this.playerAddGroup(bukkitPlayer, group)
        }

        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            val rank = EvilPermissions.instance.rankHandler.getRankById(group)
            if (rank == null) {
                BukkitPlugin.instance.logger.severe("[Vault Hook] Couldn't add group \"$group\" to player because it doesn't exist.")
                return@runTaskAsynchronously
            }

            val user = EvilPermissions.instance.userHandler.loadOrCreateByUsername(player)
            if (user != null) {
                // TODO: MAKE VAULT GRANT'S DEFAULT DURATION CONFIGURABLE
                val grant = Grant()
                grant.rank = rank
                grant.reason = "VaultAPI Hook"
                grant.issuedAt = System.currentTimeMillis()

                user.grants.add(grant)
                user.requiresSave = true
                user.requiresApply = true

                return@runTaskAsynchronously
            }
        }

        return true
    }

    override fun playerAddGroup(world: World?, player: String, group: String): Boolean {
        BukkitPlugin.instance.server.scheduler.runTaskAsynchronously(BukkitPlugin.instance) {
            val rank = EvilPermissions.instance.rankHandler.getRankById(group)
            if (rank == null) {
                BukkitPlugin.instance.logger.severe("[Vault Hook] Couldn't add group \"$group\" to player because it doesn't exist.")
                return@runTaskAsynchronously
            }

            val user = EvilPermissions.instance.userHandler.loadOrCreateByUsername(player)
            if (user != null) {
                // TODO: MAKE VAULT GRANT'S DEFAULT DURATION CONFIGURABLE
                val grant = Grant()
                grant.rank = rank
                grant.reason = "VaultAPI Hook"
                grant.issuedAt = System.currentTimeMillis()

                user.grants.add(grant)
                user.requiresSave = true
                user.requiresApply = true
            }
        }

        return true
    }

    override fun playerRemoveGroup(player: Player, group: String): Boolean {
        val user = EvilPermissions.instance.userHandler.getByUniqueId(player.uniqueId)
        if (user != null) {
            var removed = false
            for (grant in user.grants.filter { it.isActive() }) {
                if (grant.rank.id.equals(group, ignoreCase = true)) {
                    grant.removedAt = System.currentTimeMillis()
                    grant.removalReason = "VaultAPI Hook"

                    user.requiresSave = true
                    user.requiresApply = true

                    removed = true
                }
            }

            return removed
        }

        return false
    }

    override fun playerRemoveGroup(world: String?, player: OfflinePlayer, group: String): Boolean {
        Tasks.async {
            val user = EvilPermissions.instance.userHandler.getByUniqueId(player.uniqueId)
            if (user != null) {
                for (grant in user.grants.filter { it.isActive() }) {
                    if (grant.rank.id.equals(group, ignoreCase = true)) {
                        grant.removedAt = System.currentTimeMillis()
                        grant.removalReason = "VaultAPI Hook"

                        user.requiresSave = true
                        user.requiresApply = true
                    }
                }
            }
        }

        return true
    }

    override fun playerRemoveGroup(world: String, player: String, group: String): Boolean {
        val bukkitPlayer = Bukkit.getPlayer(player)
        if (bukkitPlayer != null) {
            return this.playerRemoveGroup(bukkitPlayer, group)
        }

        Tasks.async {
            val user = EvilPermissions.instance.userHandler.loadOrCreateByUsername(player)
            if (user != null) {
                for (grant in user.grants.filter { it.isActive() }) {
                    if (grant.rank.id.equals(group, ignoreCase = true)) {
                        grant.removedAt = System.currentTimeMillis()
                        grant.removalReason = "VaultAPI Hook"

                        user.requiresSave = true
                        user.requiresApply = true
                    }
                }
            }
        }

        return true
    }

    override fun playerRemoveGroup(world: World?, player: String?, group: String?): Boolean {
        if (player == null || group == null) {
            return false
        }

        val bukkitPlayer = Bukkit.getPlayer(player)
        if (bukkitPlayer != null) {
            return this.playerRemoveGroup(bukkitPlayer, group)
        }

        Tasks.async {
            val user = EvilPermissions.instance.userHandler.loadOrCreateByUsername(player)
            if (user != null) {
                for (grant in user.grants.filter { it.isActive() }) {
                    if (grant.rank.id.equals(group, ignoreCase = true)) {
                        grant.removedAt = System.currentTimeMillis()
                        grant.removalReason = "VaultAPI Hook"

                        user.requiresSave = true
                        user.requiresApply = true
                    }
                }
            }
        }

        return true
    }

    override fun playerHas(world: String?, player: String, permission: String): Boolean {
        val bukkitPlayer = Bukkit.getPlayer(player)
        if (bukkitPlayer != null) {
            return this.playerHas(bukkitPlayer, permission)
        }

        return false
    }

    override fun playerHas(player: Player, permission: String): Boolean {
        val user = EvilPermissions.instance.userHandler.getByUniqueId(player.uniqueId)
        if (user != null) {
            return user.permissions.contains(permission)
        }

        return false
    }

    override fun playerAdd(world: String?, player: String, permission: String): Boolean {
        val bukkitPlayer = Bukkit.getPlayer(player)
        if (bukkitPlayer != null) {
            return this.playerAdd(bukkitPlayer, permission)
        }

        return false
    }

    override fun playerAdd(player: Player, permission: String): Boolean {
        val user = EvilPermissions.instance.userHandler.loadOrCreate(player.uniqueId)
        if (user != null) {
            val added = user.permissions.add(permission)
            if (added) {
                user.requiresSave = true
            }
            return added
        }

        return false
    }

    override fun playerRemove(world: String?, player: String, permission: String): Boolean {
        val bukkitPlayer = Bukkit.getPlayer(player)
        if (bukkitPlayer != null) {
            return this.playerRemove(bukkitPlayer, permission)
        }

        return false
    }

    override fun playerRemove(player: Player, permission: String): Boolean {
        val user = EvilPermissions.instance.userHandler.loadOrCreate(player.uniqueId)
        if (user != null) {
            val removed = user.permissions.remove(permission)
            if (removed) {
                user.requiresSave = true
            }
            return removed
        }

        return false
    }

    override fun playerInGroup(player: Player, group: String): Boolean {
        val user = EvilPermissions.instance.userHandler.getByUniqueId(player.uniqueId)

        user?.grants
            ?.filter { it.isActive() }
            ?.forEach { grant ->
                if (grant.rank.id.equals(group, ignoreCase = true)) {
                    return true
                }
            }

        return false
    }

    // TODO: ASYNC OFFLINE SUPPORT
    override fun playerInGroup(world: String, player: OfflinePlayer, group: String): Boolean {
        return super.playerInGroup(world, player, group)
    }

    override fun playerInGroup(p0: String?, p1: String?, p2: String?): Boolean {
        throw UnsupportedOperationException("EvilPermissions does not support deprecated VaultAPI methods")
    }

    override fun playerInGroup(world: World?, player: String?, group: String?): Boolean {
        throw UnsupportedOperationException("EvilPermissions does not support deprecated VaultAPI methods")
    }

    override fun getName(): String {
        return "EvilPermissions"
    }

    override fun groupAdd(world: String?, group: String, permission: String): Boolean {
        val rank = EvilPermissions.instance.rankHandler.getRankById(group)
        if (rank != null) {
            return rank.permissions.add(permission)
        }
        return false
    }

    override fun groupHas(world: String?, group: String, permission: String): Boolean {
        val rank = EvilPermissions.instance.rankHandler.getRankById(group)
        if (rank != null) {
            return rank.permissions.contains(permission)
        }
        return false
    }

    override fun groupRemove(world: String?, group: String, permission: String): Boolean {
        val rank = EvilPermissions.instance.rankHandler.getRankById(group)
        if (rank != null) {
            return rank.permissions.remove(permission)
        }
        return false
    }

}