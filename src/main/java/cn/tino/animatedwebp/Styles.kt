package main.java.cn.tino.animatedwebp

import tornadofx.*

/**
 * mailTo:guocheng@xuxu.in
 * Created by tino on 2017 June 17, 18:39.
 */
class Styles : Stylesheet() {
    companion object {
        val mainScreen by cssclass()
    }

    init {
        select(mainScreen) {
            fontSize = 14.pt
            padding = box(20.px)
            vgap = 30.px
            hgap = 60.px
        }
    }
}