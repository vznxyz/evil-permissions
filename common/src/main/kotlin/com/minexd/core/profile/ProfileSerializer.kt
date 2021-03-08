package com.minexd.core.profile

import com.google.gson.*
import com.minexd.core.CoreXD
import java.lang.reflect.Type

object ProfileSerializer : JsonSerializer<Profile>, JsonDeserializer<Profile> {

    override fun serialize(profile: Profile, type: Type, context: JsonSerializationContext): JsonElement {
        return context.serialize(profile)
    }

    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Profile {
        return context.deserialize(json, CoreXD.instance.plugin.getProfileType())
    }

}