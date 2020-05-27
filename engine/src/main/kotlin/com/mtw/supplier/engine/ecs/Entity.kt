package com.mtw.supplier.engine.ecs

import com.mtw.supplier.engine.utils.SeededRand
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.reflect.KClass

@Serializable
class Entity internal constructor(
    val id: String,
    val name: String
) {
    constructor(name: String, seededRand: SeededRand) : this(
        id = seededRand.generateUUID(),
        name = name
    )

    private val components: MutableList<Component> = mutableListOf()

    fun addComponent(component: Component): Entity {
        this.components.add(component)
        component.notifyAdded(this.id)
        return this
    }

    fun removeComponent(component: Component) {
        if (component !in components) {
            throw ComponentNotFoundException("Could not find component of type ${component::class} in entity [$name,$id]")
        }
        this.components.remove(component)
        component.notifyRemoved()
    }

    fun <T: Component> removeComponent(clazz: KClass<T>) {
        this.removeComponent(this.getComponent(clazz))
    }

    fun hasComponent(componentClass: KClass<*>): Boolean {
        return components.any { componentClass.isInstance(it) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(clazz: KClass<T>): T {
        val first = components.firstOrNull { clazz.isInstance(it) }
            ?: throw ComponentNotFoundException("Could not find component of type $clazz in entity [$name,$id]")
        return first as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponentOrNull(clazz: KClass<T>): T? {
        return components.firstOrNull { clazz.isInstance(it) } as T?
    }

    class ComponentNotFoundException(message: String): Exception(message)
}