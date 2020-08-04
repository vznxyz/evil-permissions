package net.evilblock.permissions.rank

import com.google.gson.annotations.JsonAdapter
import net.evilblock.permissions.EvilPermissions
import net.evilblock.permissions.rank.serialize.RankSetSerializer
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class Rank(var id: String,
           var displayName: String,
           private var displayColor: String,
           var displayOrder: Int,
           /**
            * The prefix that can be used in chat formatting.
            */
           var prefix: String,
           /**
            * The prefix that gets prepended to [org.bukkit.entity.Player.getPlayerListName].
            */
           var playerListPrefix: String,
           /**
            * The prefix that gets prepended to [org.bukkit.entity.Player.getDisplayName].
            */
           var displayNamePrefix: String,
           var permissions: HashSet<String>,
           @JsonAdapter(RankSetSerializer::class)
           var inheritedRanks: HashSet<Rank>,
           var default: Boolean,
           var hidden: Boolean,
           var groups: HashSet<String>) {

    companion object {
        val COLOR_CHAR = '§'
    }

    constructor(name: String) : this(
        id = name,
        displayName = name,
        displayColor = "${COLOR_CHAR}f",
        displayOrder = 999,
        prefix = "",
        playerListPrefix = "",
        displayNamePrefix = "&f",
        permissions = hashSetOf<String>(),
        inheritedRanks = hashSetOf(),
        default = false,
        hidden = false,
        groups = hashSetOf("GLOBAL")
    )

    /**
     * Recursively gets all permissions of this rank and the ranks it inherits.
     */
    fun getCompoundedPermissions(): List<String> {
        val toReturn = ArrayList<String>()
        toReturn.addAll(permissions)

        for (inheritedRank in inheritedRanks) {
            toReturn.addAll(inheritedRank.getCompoundedPermissions())
        }

        return toReturn
    }

    /**
     * Recursively gets all permissions of this rank and the ranks it inherits.
     */
    fun getMappedCompoundedPermissions(map: HashMap<Rank, HashSet<String>>, list: ArrayList<Rank> = arrayListOf()): Map<Rank, HashSet<String>> {
        map.putIfAbsent(this, hashSetOf())
        map[this]!!.addAll(permissions)

        for (inheritedRank in inheritedRanks) {
            if (list.contains(inheritedRank)) {
                continue
            }

            list.add(inheritedRank)
            inheritedRank.getMappedCompoundedPermissions(map, list)
        }

        return map
    }

    /**
     * Gets the colored [displayName].
     */
    fun getColoredDisplayName(): String {
        return displayColor.replace('&', '\u00A7') + displayName
    }

    fun getChatPrefix(): String {
        return prefix.replace('&', '\u00A7')
    }

    fun setGlobal(global: Boolean) {
        if (global) {
            groups.add("GLOBAL")
        } else {
            groups.remove("GLOBAL")
        }
    }

    fun isGlobal(): Boolean {
        return groups.contains("GLOBAL")
    }

    fun isHidden(): Boolean {
        return hidden || getCompoundedPermissions().contains("rank.hidden")
    }

    fun isActiveOnServer(): Boolean {
        val activeGroups = EvilPermissions.instance.plugin.getActiveGroups()
        for (group in this.groups) {
            if (activeGroups.contains(group)) {
                return true
            }
        }
        return false
    }

    fun findDependencyLock(rank: Rank): Rank? {
        if (this == rank) {
            return rank
        }

        if (rank.inheritedRanks.contains(this)) {
            return rank
        }

        for (inheritedRank in inheritedRanks) {
            if (rank.inheritedRanks.contains(inheritedRank)) {
                return rank
            }

            val rankDependencyLock = rank.findDependencyLock(inheritedRank)
            if (rankDependencyLock != null) {
                return rankDependencyLock
            }

            if (inheritedRank.inheritedRanks.contains(rank)) {
                return inheritedRank
            }

            val inheritedRankDependencyLock = inheritedRank.findDependencyLock(rank)
            if (inheritedRankDependencyLock != null) {
                return inheritedRankDependencyLock
            }
        }

        return null
    }

    fun getDisplayColor(): String {
        return displayColor.replace('&', COLOR_CHAR)
    }

    fun setDisplayColor(color: String) {
        displayColor = color
    }

    fun getDisplayColorChar(): String {
        return displayColor.toCharArray()[1].toString()
    }

    fun processPlaceholders(string: String): String {
        return string.replace("{rankDisplayName}", displayName)
            .replace("{rankColor}", displayColor)
            .replace("{rankPrefix}", prefix)
            .replace("{rankPlayerListNamePrefix}", playerListPrefix)
            .replace("{rankDisplayNamePrefix}", displayNamePrefix)
    }

    fun copyFrom(otherRank: Rank) {
        this.id = otherRank.id
        this.displayName = otherRank.displayName
        this.displayColor = otherRank.displayColor
        this.displayOrder = otherRank.displayOrder
        this.permissions = otherRank.permissions
        this.prefix = otherRank.prefix
        this.playerListPrefix = otherRank.playerListPrefix
        this.displayNamePrefix = otherRank.displayNamePrefix
        this.default = otherRank.default
        this.hidden = otherRank.hidden
        this.groups = otherRank.groups
    }

    override fun equals(other: Any?): Boolean {
        return other is Rank && this.id == other.id
    }

    /**
     * Fixes missing/renamed fields to fix version compatibility.
     */
    fun runCompatibilityFix() {
        if (displayColor == null) {
            displayColor = "${COLOR_CHAR}f"
        }
        if (displayNamePrefix == null) {
            displayNamePrefix = ""
        }
    }

}