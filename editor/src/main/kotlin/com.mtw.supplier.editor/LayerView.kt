package com.mtw.supplier.editor

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
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
}

class LayerTableView: View("Layer Table View") {
    private val layerController: LayerController by inject()
    private val layerModel: LayerModel by inject()

    override val root = tableview(layerController.getObservableLayers()) {
        column("Name", Layer::nameProperty)
        column("Level", Layer::levelProperty)
        column("Visible", Layer::visibleProperty)
        column("File", Layer::fileProperty)

        bindSelected(layerModel)
    }
}

class LayerEditView: View("Layer Edit View") {
    private val appPath = Paths.get("").toAbsolutePath().toString()
    // TODO: Professionalism
    private val pngFilters = arrayOf(FileChooser.ExtensionFilter("pngs!!! ONLY PNGS", "*png"))

    private val layerModel: LayerModel by inject()

    override val root = form{
        fieldset("Layer Stuff") {
            field("Name") { textfield(layerModel.name) }
            field("Level") { textfield(layerModel.level) }
            field("Visible") { checkbox(null, layerModel.visible) }
            field("File") {
                button(layerModel.fileName()) {
                    action {
                        pickFile()
                    }
                    layerModel.file.addListener { it, _, _ ->
                        this.text = it.value?.name ?: "NO FILE CHOSEN"
                    }
                }
            }

            button("Save") {
                enableWhen(layerModel.dirty)
                action { save() }
            }
            button("Reset").action {
                layerModel.rollback()
            }
        }
    }

    private fun pickFile() {
        val file = chooseFile("Choose the image file",
            pngFilters,
            FileChooserMode.Single,
            op = { initialDirectory = File(appPath) }
        )
        file.map {
            layerModel.file.value = it
        }
    }

    private fun save() {
        layerModel.commit()
    }
}

class LayerView: View("Layer View") {
    override val root = borderpane()

    init {
        with(root) {
            center {
                this += LayerTableView()
            }
           right {
               this += LayerEditView()
           }
        }
    }
}