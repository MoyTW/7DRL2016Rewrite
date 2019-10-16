package com.mtw.supplier.editor

import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.BorderPane
import tornadofx.*

class Person(name: String? = null, title: String? = null) {
    val nameProperty = SimpleStringProperty(this, "name", name)
    var name: String? by nameProperty

    val titleProperty = SimpleStringProperty(this, "title", title)
    var title: String? by titleProperty
}

class PersonModel: ItemViewModel<Person>() {
    val name = bind(Person::nameProperty)
    val title = bind(Person::titleProperty)
}

class PersonList: View("Person List") {
    private val persons = listOf(Person("Bob", "Manager"), Person("Larry", "Manager")).observable()
    private val model: PersonModel by inject()

    override val root = tableview(persons) {
        column("Name", Person::nameProperty)
        column("Title", Person::titleProperty)

        bindSelected(model)
    }
}

class EditForm: View("Edit Form") {
    private val model: PersonModel by inject()

    override val root = form {
        fieldset("Edit Person") {
            field("Name") { textfield(model.name) }
            field("Title") { textfield(model.title) }
            button("Save") {
                enableWhen(model.dirty)
                action {
                    save()
                }
            }
            button("Reset") {
                action {
                    model.rollback()
                }
            }
        }
    }

    private fun save() {
        model.commit()

        val person = model.item

        println("Saving ${person.name} : ${person.title}")
    }
}

class BindingScreen: View("Person Editor") {
    override val root = BorderPane()
    private val personList: PersonList by inject()
    private val editForm: EditForm by inject()

    init {
        with(root) {
            center {
                this += personList
            }
            right {
                this += editForm
            }
        }
    }


}