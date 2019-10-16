package com.mtw.supplier.editor

import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import tornadofx.*

class Person(name: String? = null, title: String? = null) {
    val nameProperty = SimpleStringProperty(this, "name", name)
    var name by nameProperty

    val titleProperty = SimpleStringProperty(this, "title", title)
    var title by titleProperty
}

class PersonModel: ItemViewModel<Person>() {
    val name = bind(Person::nameProperty)
    val title = bind(Person::titleProperty)
}

class BindingScreen: View("Person Editor") {
    override val root = BorderPane()
    private val persons = listOf(Person("Bob", "Manager"), Person("Larry", "Manager")).observable()
    private val model = PersonModel()

    init {
        with(root) {
            center {
                tableview(persons) {
                    column("Name", Person::nameProperty)
                    column("Title", Person::titleProperty)

                    bindSelected(model)
                }
            }
            right {
                form {
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
            }
        }
    }

    private fun save() {
        model.commit()

        val person = model.item

        println("Saving ${person.name} : ${person.title}")
    }
}