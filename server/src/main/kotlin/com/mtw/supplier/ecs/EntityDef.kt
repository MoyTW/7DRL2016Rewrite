package com.mtw.supplier.ecs

import com.mtw.supplier.ecs.components.HpComponent
import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.internal.impl.resolve.jvm.JvmClassName

@Serializable
abstract class Generator {
    abstract fun generate(): Any
}

@Serializable
class FixedIntegerGenerator(val value: Int): Generator() {
    override fun generate(): Int {
        return value
    }
}

@Serializable
class ComponentDef(
    val qualifiedClassName: String,
    val generators: Array<Generator>
) {
    @Transient
    private val componentClass: KClass<*> = Class.forName(qualifiedClassName).kotlin
    @Transient
    private val matchingConstuctor =  componentClass.constructors.find { constructorMatches(it.parameters) }
        ?: throw InvalidComponentDefException()

    private fun constructorMatches(params: List<KParameter>): Boolean {
        if (params.any{ it.name == "seen1" }) return false
        if (params.size < generators.size) return false
        for (i in params.indices) {
            val param = params[i]
            if (!(param.isOptional && generators.getOrNull(i) == null) &&
                param.type != generators[i]::class.declaredFunctions.find { it.name == "generate" }!!.returnType)
            {
                return false
            }
        }
        return true
    }

    fun buildComponent(): Component {
        val generated = generators.map { it.generate() }.toTypedArray()
        return matchingConstuctor.call(*generated) as Component
    }

    class InvalidComponentDefException: Exception("something went wrong loading rip you")
}

@Serializable
class EntityDef(
    val componentDefs: List<ComponentDef>
) {
    fun buildEntity(id: Int?, name: String): Entity {
        if (id == null) {
            TODO("Auto-increment entity IDs aren't a thing yet")
        }
        val entity = Entity(id, name)
        for (componentDef in componentDefs) {
            entity.addComponent(componentDef.buildComponent())
        }
        return entity
    }
}