package com.mtw.supplier.editor

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.scene.control.TableCell
import javafx.scene.control.TableView
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.nio.file.Paths

class Layer(name: String?, level: Int=-1, visible: Boolean=true, file: File? = null) {
    val nameProperty = SimpleStringProperty(this, "name", name)
    var name by nameProperty

    val levelProperty = SimpleIntegerProperty(this, "level", level)
    var level by levelProperty

    val visibleProperty = SimpleBooleanProperty(this, "visible", visible)
    var visible by visibleProperty

    // Note that for SimpleObjectProperty you declare the T as non-nullable, but since Java it in fact is.
    // This...DOES open you up to full-on NPEs. But tight Java integration already seems to do this...
    val fileProperty = SimpleObjectProperty<File>(this, "file", file)
    var file by fileProperty
}

class LayerModel: ItemViewModel<Layer>() {
    val name = bind(Layer::nameProperty)
    val level = bind(Layer::levelProperty)
    val visible = bind(Layer::visibleProperty)
    val file = bind(Layer::fileProperty)

    fun fileName(): String {
        return file.value?.name ?: "NO FILE CHOSEN"
    }
}

class LayerController: Controller() {
    private val layers = mutableListOf<Layer>().observable()

    fun getObservableLayers(): ObservableList<Layer> {
        return layers
    }

    fun addLayer(layer: Layer) {
        this.layers.add(layer)
    }

    fun removeLayer(layer: Layer) {
        if (layer in layers) {
            layers.remove(layer)
        }
    }
}

class LayerFileCell: TableCell<Layer, File>() {
    private val noFileText = "NO_FILE_SELECTED"
    private val appPath = Paths.get("").toAbsolutePath().toString()
    private val pngFilters = arrayOf(FileChooser.ExtensionFilter("PNG", "*png"))

    init {
        isEditable = true
    }

    // I'm not super confident that this is how startEdit is supposed to be used; I'm preeety sure literally calling
    // the commit/cancel inside this function is not intended. Unfortunately I can't understand ChoiceBoxTableCell
    // so I'm just gonna go off this because it looks like it works at the moment.
    override fun startEdit() {
        super.startEdit()

        val chosenFile = chooseFile("Choose the image file",
            pngFilters,
            FileChooserMode.Single,
            op = { initialDirectory = File(appPath) }
        ).firstOrNull()

        if (chosenFile != null) {
            this.commitEdit(chosenFile)
        } else {
            this.cancelEdit()
        }
    }

    override fun updateItem(item: File?, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty) {
            this.text = null
        } else if (item != null) {
            this.text = item.name
        } else {
            this.text = noFileText
        }
    }
}

class LayerTableView: View("Layer Table View") {
    private val layerController: LayerController by inject()
    private val layerModel: LayerModel by inject()

    private var tableView: TableView<Layer> by singleAssign()

    override val root = borderpane { }

    init {
        with(root) {
            top {
                hbox {
                    button("New Row").action {
                        layerController.addLayer(Layer("New Layer"))
                    }
                    button("Remove Row").action {
                        // I can't figure out how to get it to only be shown if there's a current selection
                        if (tableView.selectedItem != null) {
                            layerController.removeLayer(tableView.selectedItem!!)
                        }
                    }
                }
            }
            center {
                tableView = tableview(layerController.getObservableLayers()) {
                    column("Name", Layer::nameProperty).makeEditable()
                    column("Level", Layer::levelProperty).makeEditable()
                    column("Visible", Layer::visibleProperty).makeEditable()
                    column("File", Layer::fileProperty) {
                        setCellFactory {
                            LayerFileCell()
                        }
                    }

                    bindSelected(layerModel)
                }
            }
        }
    }
}

class LayerView: View("Layer View") {
    override val root = borderpane()

    init {
        with(root) {
            center {
                this += LayerTableView()
            }
        }
    }
}