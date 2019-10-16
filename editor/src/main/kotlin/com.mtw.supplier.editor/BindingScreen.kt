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

class BindingScreen: View("Person Editor") {
    override val root = BorderPane()
    var nameField: TextField by singleAssign()
    var titleField: TextField by singleAssign()
    var personTable: TableView<Person> by singleAssign()

    val persons = listOf(Person("Bob", "Manager"), Person("Larry", "Manager")).observable()

    var prevSelection: Person? = null

    init {
        with(root) {
            center {
                tableview(persons) {
                    personTable = this
                    column("Name", Person::nameProperty)
                    column("Title", Person::titleProperty)

                    selectionModel.selectedItemProperty().onChange {
                        editPerson(it)
                        prevSelection = it
                    }
                }
            }
            right {
                form {
                    fieldset("Edit Person") {
                        field("Name") { textfield { nameField = this } }
                        field("Title") { textfield { titleField = this } }
                        button("Save").action { save() }
                    }
                }
            }
        }
    }

    private fun editPerson(person: Person?) {
        if (person != null) {
            prevSelection?.apply {
                nameProperty.unbindBidirectional(nameField.textProperty())
                titleProperty.unbindBidirectional(titleField.textProperty())
            }
            nameField.bind(person.nameProperty)
            titleField.bind(person.titleProperty)
            prevSelection = person
        }
    }

    private fun save() {
        val person = personTable.selectedItem!!

        println("Saving ${person.name} : ${person.title}")
    }
}