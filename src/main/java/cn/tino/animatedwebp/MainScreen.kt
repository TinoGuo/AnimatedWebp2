package cn.tino.animatedwebp

import javafx.beans.binding.BooleanBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.stage.FileChooser
import main.java.cn.tino.animatedwebp.Styles.Companion.mainScreen
import main.java.cn.tino.animatedwebp.WebpParameter
import sun.misc.Launcher
import tornadofx.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset


/**
 * mailTo:guocheng@xuxu.in
 * Created by tino on 2017 June 17, 18:08.
 */
class MainScreen : View() {

    companion object {
        val singleFileCmd = " -frame %s +%d+0+0+0-b"
        val encodeWebpCmd = "%s/cwebp -q %d %s -o %s"
        val webpMuxCmd = "%s/webpmux %s -loop %d -bgcolor 0,0,0,0 -o %s.webp"
        val singleWebpCmd = "%s/cwebp -q %d %s -o %s"
        val ERROR = "Error"
    }

    override val root = GridPane()

    val parameter = WebpParameter()
    var projectDir: File by singleAssign()

    var quality: TextField by singleAssign()
    var speed: TextField by singleAssign()
    var loop: CheckBox  by singleAssign()
    var loopTimes: TextField by singleAssign()
    var chooseFile: Button by singleAssign()
    var chooseDir: Button by singleAssign()
    var execute: Button    by singleAssign()
    var fileFilter: Spinner<String> by singleAssign()

    init {
        title = "Animated Webp Tools"

        with(root) {
            maxWidth = 1200.px.value
            addClass(mainScreen)
            row("webp quality(0~100)") {
                quality = textfield() {
                    text = "75"
                    bind(parameter.qualityProperty())
                }
            }
            row("webp speed(frames/s)") {
                speed = textfield() {
                    text = "30"
                    bind(parameter.speedProperty())
                }
            }
            row("infinity loop") {
                loop = checkbox {
                    bind(parameter.loopInfinityProperty())
                }
                label("loop times")
                loopTimes = textfield {
                    disableProperty().bind(parameter.loopInfinityProperty().isEqualTo(true))
                    bind(parameter.loopTimesProperty())
                }
            }

            row {
                chooseFile = button("choose single file") {
                    setOnAction {
                        parameter.fileSelected = tornadofx.chooseFile(null,
                                arrayOf(FileChooser.ExtensionFilter("image", "*.jpg", "*.png", "*.gif")),
                                FileChooserMode.Single,
                                primaryStage,
                                null).let {
                            if (it.isEmpty()) {
                                return@let null
                            }
                            return@let it.first()
                        }
                    }
                }

                chooseDir = button("choose dir") {
                    setOnAction {
                        parameter.fileSelected = chooseDirectory(null,
                                null,
                                primaryStage,
                                null)
                    }
                }

                label("File filter")

                fileFilter = spinner(FXCollections.observableArrayList(".png", ".jpg", ".gif"),
                        false,
                        parameter.fileFilterProperty()) {
                    disableProperty().bind(parameter.fileSelectedProperty().let {
                        return@let object : BooleanBinding() {
                            init {
                                super.bind(it)
                            }

                            override fun getDependencies(): ObservableList<*> {
                                return FXCollections.singletonObservableList(it)
                            }

                            override fun dispose() {
                                super.unbind(it)
                            }

                            override fun computeValue(): Boolean {
                                if (it.get() == null) {
                                    return true
                                }
                                return it.get().isFile
                            }
                        }
                    })
                }
            }

            row("") {
                execute = button("execute") {
                    setOnAction {
                        when (parameter.fileSelected.isDirectory) {
                            true -> processMultiFiles()
                            false -> processSingle()
                        }
                    }

                    disableProperty().bind(parameter.qualityProperty().isNull.or(parameter.speedProperty().isNull)
                            .or(parameter.fileSelectedProperty().isNull))
                }
            }
        }

        quality.text = "75"
        speed.text = "30"
        loop.isSelected = true
        loopTimes.text = "1"

        val file = File(javaClass.protectionDomain.codeSource.location.path)
        if (file.isFile) {
            var preProcess = false
            file.parentFile.listFiles().forEach { it ->
                if (it.name.contains("webpLib")) {
                    preProcess = it.list().findAllLib()
                    return@forEach
                }
            }
            if (!preProcess) {
                alert(Alert.AlertType.ERROR, ERROR, "webpLib directory lose!", ButtonType.OK,
                        actionFn = {
                            buttonType ->
                            if (buttonType.buttonData == ButtonBar.ButtonData.OK_DONE) {
                                System.exit(0)
                            }
                        })
            } else {

                projectDir = file.parentFile.listFiles{pathname -> pathname.endsWith("webpLib")}.first()
            }
        } else {
            val url = Launcher::class.java.getResource("/")
            if (url != null) {
                projectDir = File(File(url.toURI()).parentFile.parentFile.absolutePath + "/webpLib")
            } else {
                alert(Alert.AlertType.ERROR, ERROR, "webpLib directory lose!", ButtonType.OK,
                        actionFn = {
                            buttonType ->
                            if (buttonType.buttonData == ButtonBar.ButtonData.OK_DONE) {
                                System.exit(0)
                            }
                        })
            }
        }
    }

    fun Array<String>.findAllLib(): Boolean {
        val count = mutableMapOf<String, Int>()
        forEach { it ->
            count.put(it, 1)
        }
        return count.size == 3
    }

    fun String?.isDigitsOnly(): Boolean {
        val len = this?.length ?: 0
        var cp: Int
        var i = 0
        while (i < len) {
            cp = Character.codePointAt(this, i)
            if (!Character.isDigit(cp)) {
                return false
            }
            i += Character.charCount(cp)
        }
        return true
    }

    fun InputStream.convertString(): String {
        val outStream = ByteArrayOutputStream(1024)
        val data: ByteArray = ByteArray(1024)
        var count = this.read(data, 0, 1024)
        while (count != -1) {
            outStream.write(data, 0, 1024)
            count = this.read(data, 0, 1024)
        }

        return String(outStream.toByteArray(), Charset.defaultCharset())
    }

    fun Runtime.openFileBrowser(path: String) {
        val os = System.getProperty("os.name").toLowerCase()
        if (os.startsWith("win")) {
            this.exec("explorer.exe /select,$path")
        } else {
            this.exec("open $path")
        }
    }

    fun preProcessParameter(): Boolean {
        if (parameter.quality.isEmpty() || parameter.speed.isEmpty() || (!parameter.loopInfinity && parameter.loopTimes.isEmpty())) {
            alert(Alert.AlertType.ERROR, ERROR, "content must not empty!", ButtonType.OK)
            return false
        }
        if (!parameter.quality.isDigitsOnly()) {
            alert(Alert.AlertType.ERROR, ERROR, "quality must be digits!", ButtonType.OK)
            return false
        }
        if (parameter.quality.toInt() in 0..100) {
            alert(Alert.AlertType.ERROR, ERROR, "quality must in the range(0,100]!", ButtonType.OK)
            return false
        }
        if (!parameter.speed.isDigitsOnly()) {
            alert(Alert.AlertType.ERROR, "", "speed must be digits!", ButtonType.OK)
            return false
        }
        if (!parameter.loopInfinity && !parameter.loopTimes.isDigitsOnly()) {
            alert(Alert.AlertType.ERROR, "", "loop times must be digits!", ButtonType.OK)
            return false
        }
        if (parameter.loopTimes.toInt() <= 0) {
            alert(Alert.AlertType.ERROR, ERROR, "loop time must greater than 0!", ButtonType.OK)
            return false
        }
        return true
    }

    fun processSingle() {
        if (preProcessParameter()) {
            val runTime = Runtime.getRuntime()
            val tmpName = parameter.fileSelected.absolutePath.replaceAfter('.', "webp")
            runAsync {
                runTime.exec(String.format(singleWebpCmd,
                        projectDir,
                        parameter.quality.toInt(),
                        parameter.fileSelected.absolutePath,
                        tmpName)).apply { this.waitFor() }
            } ui { process ->
                when (process.exitValue()) {
                    0 -> alert(Alert.AlertType.INFORMATION, "", tmpName, ButtonType.OK)
                    else -> runAsync {
                        process.errorStream.convertString()
                    } ui { str ->
                        alert(Alert.AlertType.ERROR, ERROR, str, ButtonType.OK)
                    }
                }
            }
        }
    }

    fun processMultiFiles() {
        runAsync {
            val runTime = Runtime.getRuntime()
            var files = parameter.fileSelected.listFiles { pathname ->
                pathname!!.name.endsWith(parameter.fileFilter)
            }
            if (files.isEmpty()) {
                return@runAsync null
            }
            files.forEach { it ->
                val name = it.name.replace(parameter.fileFilter, "")

                runTime.exec(String.format(encodeWebpCmd,
                        projectDir.absolutePath,
                        parameter.quality.toInt(),
                        it.absolutePath,
                        it.parent + "/" + name + ".webp"))
                        .waitFor()
            }

            val sb = StringBuilder()
            files = parameter.fileSelected.listFiles { pathname -> pathname!!.name.endsWith(".webp") }
            if (files.isEmpty()) {
                return@runAsync null
            }
            files.sort()
            for (file in files) {
                sb.append(String.format(singleFileCmd, file.absolutePath, 1000 / parameter.speed.toInt()))
            }

            val loopCount = if (parameter.loopInfinity) 0 else parameter.loopTimes.toInt()

            runTime.exec(String.format(webpMuxCmd,
                    projectDir.absolutePath,
                    sb.toString(),
                    loopCount,
                    parameter.fileSelected.parent + "/" + parameter.fileSelected.name))
                    .apply {
                        this.waitFor()
                        files.forEach { it.delete() }
                    }

        } ui { process ->
            if (process == null) {
                alert(Alert.AlertType.ERROR, ERROR, "selected files is empty", ButtonType.OK)
            }
            when (process?.exitValue()) {
                0 -> alert(Alert.AlertType.INFORMATION,
                        "",
                        "output file is: ${parameter.fileSelected.absolutePath}.webp\nOpen the directory now?",
                        ButtonType.OK,
                        ButtonType.CANCEL,
                        actionFn = {
                            btnType ->
                            if (btnType.buttonData == ButtonBar.ButtonData.OK_DONE) {
                                Runtime.getRuntime().openFileBrowser(parameter.fileSelected.parentFile.absolutePath)
                            }
                        })
                else -> runAsync {
                    process?.errorStream?.convertString() ?: "EXECUTE FAIL!"
                } ui { str ->
                    alert(Alert.AlertType.ERROR, ERROR, str, ButtonType.OK)
                }
            }

        }
    }


}
