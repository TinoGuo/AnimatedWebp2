package cn.tino.animatedwebp

import javafx.application.Application
import main.java.cn.tino.animatedwebp.Styles
import tornadofx.*

/**
 * mailTo:guocheng@xuxu.in
 * Created by tino on 2017 June 17, 18:00.
 */
class AnimatedWebpKt : App(MainScreen::class) {

    init {
        importStylesheet(Styles::class)
    }
}

fun main(args: Array<String>) {
    Application.launch(AnimatedWebpKt::class.java, *args)
}