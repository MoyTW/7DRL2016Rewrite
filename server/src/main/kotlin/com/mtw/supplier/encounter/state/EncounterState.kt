package com.mtw.supplier.encounter.state

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.EncounterLocationComponent


class EncounterState(
    private var _currentTime: Int = 1,
    private var _completed: Boolean = false
) {
    val currentTime: Int
        get() = this._currentTime

    val completed: Boolean
        get() = this._completed

    private val nodes: MutableMap<Int, EncounterNode> = mutableMapOf()

    fun advanceTime(timeDiff: Int = 1) {
        this._currentTime += timeDiff
    }

    fun completeEncounter() {
        if (this._completed) {
            throw EncounterCannotBeCompletedTwiceException()
        }
        this._completed = true
    }
    class EncounterCannotBeCompletedTwiceException(): Exception("Encounter cannot be completed twice!")

    internal fun addNode(node: EncounterNode): EncounterState {
        if (node.id in this.nodes) {
            throw java.lang.IllegalArgumentException("Specified node id $node.id was already found in the map's nodes!")
        }
        nodes[node.id] = node
        return this
    }

    // TODO: Possibly maintain internal list
    fun getEntities(): List<Entity> {
        return nodes.flatMap { n -> n.value.entities }
    }

    fun getEntity(entityId: Int): Entity {
        return getEntities().firstOrNull { it.id == entityId } ?: throw EntityIdNotFoundException(entityId)
    }
    class EntityIdNotFoundException(entityId: Int): Exception("Entity id $entityId could not be found!")

    //<editor-fold desc="Node functions">
    fun getNodeName(nodeId: Int): String {
        return this.getNode(nodeId).name
    }

    fun getNodeOccupiedSize(nodeId: Int): Int {
        return this.getNode(nodeId).getOccupiedSize()
    }

    fun getNodeMaxSize(nodeId: Int): Int {
        return this.getNode(nodeId).size
    }

    // TODO: Entity sizing & ZOC Rules
    fun getNodeHasRoom(entity: Entity, nodeId: Int): Boolean {
        return this.getNodeOccupiedSize(nodeId) < this.getNodeMaxSize(nodeId)
    }

    fun getNodeDirectlyConnected(startNodeId: Int, endNodeId: Int): Boolean {
        return this.getNode(startNodeId).exits.any { it.id == endNodeId }
    }

    fun getDirectlyConnectedNodes(nodeId: Int): List<Int> {
        return this.getNode(nodeId).exits.map { it.id }
    }

    private fun getNode(nodeId: Int): EncounterNode {
        return this.nodes[nodeId] ?: throw NoSuchNodeException("Node id=$nodeId is not a node in this map!")
    }
    class NoSuchNodeException(message: String): Exception(message)
    //</editor-fold>

    //<editor-fold desc="Entity location functions">
    /**
     * @throws EntityAlreadyHasLocation when a node already has a location
     * @throws NodeHasInsufficientSpaceException when node cannot find space for the entity
     */
    fun placeEntity(entity: Entity, nodeId: Int): EncounterState {
        if (entity.hasComponent(EncounterLocationComponent::class)) {
            throw EntityAlreadyHasLocation("Specified entity ${entity.name} already has a location, cannot be placed!")
        } else if (!this.getNodeHasRoom(entity, nodeId)) {
            throw NodeHasInsufficientSpaceException("Node $nodeId is full, cannot place ${entity.name}")
        }

        this.getNode(nodeId).entities.add(entity)
        entity.addComponent(EncounterLocationComponent(nodeId))
        return this
    }
    class EntityAlreadyHasLocation(message: String): Exception(message)
    class NodeHasInsufficientSpaceException(message: String): Exception(message)

    fun removeEntity(entity: Entity): EncounterState {
        if (!entity.hasComponent(EncounterLocationComponent::class)) {
            throw EntityHasNoLocation("Specified entity ${entity.name} has no location, cannot remove!")
        }

        val locationComponent = entity.getComponent(EncounterLocationComponent::class)
        this.getNode(locationComponent.locationNodeId).entities.remove(entity)
        entity.removeComponent(locationComponent)
        return this
    }
    class EntityHasNoLocation(message: String): Exception(message)

    fun relocateEntity(entity: Entity, targetNodeId: Int) {
        this.removeEntity(entity)
        this.placeEntity(entity, targetNodeId)
    }
    //</editor-fold>
}

