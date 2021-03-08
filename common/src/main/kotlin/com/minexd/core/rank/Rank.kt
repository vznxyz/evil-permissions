package com.minexd.core.rank

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.minexd.core.CoreXD
import java.lang.reflect.Type
import kotlin.collections.HashSet

class Rank(var id: String) {

    companion object {
        const val COLOR_CHAR = '§'
    }

    var groups: HashSet<String> = hashSetOf("GLOBAL")

    var displayName: String = id
    var gameColor: String = "${COLOR_CHAR}f"
    var siteColor: String = "ffffff"
    var displayOrder: Int = 0

    var prefix: String = ""
    var playerListPrefix: String = ""
    var displayNamePrefix: String = "&f"

    var default: Boolean = false
    var hidden: Boolean = false
    var staff: Boolean = false

    var permissions: HashSet<String> = hashSetOf("GLOBAL")

    @JsonAdapter(RankSetSerializer::class)
    var inheritedRanks: HashSet<Rank> = hashSetOf()

    fun getCompoundedPermissions(): Set<String> {
        return hashSetOf<String>().also { permissions ->
            permissions.addAll(this.permissions)

            for (inheritedRank in inheritedRanks) {
                permissions.addAll(inheritedRank.permissions)
            }
        }
    }

    /**
     * Gets the colored [displayName].
     */
    fun getColoredDisplayName(): String {
        return gameColor.replace('&', '\u00A7') + displayName
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
        val activeGroups = CoreXD.instance.plugin.getActiveGroups()
        for (group in this.groups) {
            if (activeGroups.contains(group)) {
                return true
            }
        }
        return false
    }

    fun getColor(): String {
        return gameColor.replace('&', COLOR_CHAR)
    }

    fun getDisplayColorChar(): String {
        return if (gameColor.isEmpty() || gameColor.isBlank()) {
            'f'.toString()
        } else {
            gameColor.toCharArray()[1].toString()
        }
    }

    fun processPlaceholders(string: String): String {
        return string.replace("{rankDisplayName}", displayName)
            .replace("{rankColor}", getColor())
            .replace("{rankPrefix}", prefix)
            .replace("{rankPlayerListNamePrefix}", playerListPrefix)
            .replace("{rankDisplayNamePrefix}", displayNamePrefix)
    }

    fun copyFrom(otherRank: Rank) {
        this.id = otherRank.id
        this.displayName = otherRank.displayName
        this.gameColor = otherRank.gameColor
        this.siteColor = otherRank.siteColor
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
        return other is Rank
                && this.id == other.id
                && this.default == other.default
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + default.hashCode()
        return result
    }

    class RankSetSerializer : JsonSerializer<Set<Rank>>, JsonDeserializer<Set<Rank>> {
        override fun serialize(set: Set<Rank>, p1: Type, p2: JsonSerializationContext): JsonElement {
            return JsonArray().also { array -> set.forEach { rank -> array.add(JsonPrimitive(rank.id)) } }
        }

        override fun deserialize(json: JsonElement, p1: Type, p2: JsonDeserializationContext): Set<Rank> {
            return json.asJsonArray.mapNotNull { element -> RankHandler.getRankById(element.asString) }.toMutableSet()
        }
    }

    class RankReferenceSerializer : JsonSerializer<Rank>, JsonDeserializer<Rank> {
        override fun serialize(rank: Rank, type: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(rank.id)
        }

        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Rank? {
            return RankHandler.getRankById(json.asString)
        }
    }

}