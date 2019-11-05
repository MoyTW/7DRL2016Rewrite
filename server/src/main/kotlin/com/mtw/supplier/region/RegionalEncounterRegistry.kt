package com.mtw.supplier.region

import com.mtw.supplier.ecs.defs.EntityDef
import kotlinx.serialization.Serializable
import java.lang.IndexOutOfBoundsException

@Serializable
class RegionalEncounter constructor(
    val id: Int,
    val name: String,
    val entityDefs: List<EntityDef>
)

@Serializable
class RegionalEncounterRegistry(
    private val regionalEncounters: MutableList<RegionalEncounter> = mutableListOf()
) {
    fun addRegionalEncounter(name: String, entityDefs: List<EntityDef>) {
        this.regionalEncounters.add(RegionalEncounter(this.regionalEncounters.size + 1, name, entityDefs))
    }

    fun getRegionalEncounterById(regionalEnconterId: Int): RegionalEncounter {
        try {
            return this.regionalEncounters[regionalEnconterId]
        } catch (e: IndexOutOfBoundsException) {
            throw RegionalEncounterIdNotFound(regionalEnconterId)
        }
    }

    class RegionalEncounterIdNotFound(id: Int): Exception("Regional encounter id $id does not exist!")
}