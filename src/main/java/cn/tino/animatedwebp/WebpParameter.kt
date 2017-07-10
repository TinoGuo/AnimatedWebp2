package main.java.cn.tino.animatedwebp

import tornadofx.getProperty
import tornadofx.property
import java.io.File

/**
 * mailTo:guocheng@xuxu.in
 * Created by tino on 2017 June 17, 18:55.
 */
class WebpParameter {
    var quality by property<String>()
    fun qualityProperty() = getProperty(WebpParameter::quality)

    var speed by property<String>()
    fun speedProperty() = getProperty(WebpParameter::speed)

    var loopInfinity by property<Boolean>()
    fun loopInfinityProperty() = getProperty(WebpParameter::loopInfinity)

    var loopTimes by property<String>()
    fun loopTimesProperty() = getProperty(WebpParameter::loopTimes)

    var fileSelected by property<File>()
    fun fileSelectedProperty() = getProperty(WebpParameter::fileSelected)

    var fileFilter by property<String>(".png")
    fun fileFilterProperty() = getProperty(WebpParameter::fileFilter)
}
