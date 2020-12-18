package net.evilblock.permissions.rank.serialize

import com.google.gson.*
import net.evilblock.permissions.rank.Rank
import net.evilblock.permissions.rank.RankHandler
import java.lang.reflect.Type

class RankSetSerializer : JsonSerializer<Set<Rank>>, JsonDeserializer<Set<Rank>> {

    override fun serialize(set: Set<Rank>, p1: Type, p2: JsonSerializationContext): JsonElement {
        return JsonArray().also { array -> set.forEach { rank -> array.add(JsonPrimitive(rank.id)) } }
    }

    override fun deserialize(json: JsonElement, p1: Type, p2: JsonDeserializationContext): Set<Rank> {
        return json.asJsonArray.mapNotNull { element -> RankHandler.getRankById(element.asString) }.toMutableSet()
    }

}