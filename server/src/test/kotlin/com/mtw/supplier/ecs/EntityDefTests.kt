package com.mtw.supplier.ecs

import com.mtw.supplier.ecs.components.AIComponent
import com.mtw.supplier.ecs.components.HpComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class EntityDefTests {

    val testComponentModule = SerializersModule {
        polymorphic(Generator::class) {
            FixedIntegerGenerator::class with FixedIntegerGenerator.serializer()
        }
    }

    @Test
    fun testSerializationToJson() {
        val json = Json(JsonConfiguration.Stable, testComponentModule)
        val hpDef = ComponentDef(HpComponent::class.qualifiedName!!, arrayOf(FixedIntegerGenerator(3), FixedIntegerGenerator(9)))
        val aiDef = ComponentDef(AIComponent::class.qualifiedName!!, arrayOf())
        val entityDef = EntityDef(listOf(hpDef, aiDef))
        //val entity = Entity(95, "bob").addComponent(TestComponent("test", 99))

        // serializing objects
        val jsonData = json.stringify(EntityDef.serializer(), entityDef)
        Assert.assertEquals("{\"id\":95,\"name\":\"bob\",\"components\":[{\"type\":\"com.mtw.supplier.ecs.EntityTests.TestComponent\",\"x1\":\"test\",\"x2\":99,\"_parentId\":95}]}", jsonData)
    }

    /*
    @Test
    fun testSerializationFromJson() {
        val jsonString = "{\"id\":95,\"name\":\"bob\",\"components\":[{\"type\":\"com.mtw.supplier.ecs.EntityTests.TestComponent\",\"_parentId\":95,\"x1\":\"test\",\"x2\":99}]}"
        val json = Json(JsonConfiguration.Stable, testComponentModule)
        val entity: Entity = json.parse(Entity.serializer(), jsonString)
        Assert.assertEquals(95, entity.id)
        Assert.assertEquals("bob", entity.name)
        val component = entity.getComponent(TestComponent::class)
        Assert.assertEquals(95, component.parentId)
        Assert.assertEquals("test", component.x1)
        Assert.assertEquals(99, component.x2)
    }
    */
}
