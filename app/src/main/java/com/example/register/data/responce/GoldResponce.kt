package com.example.register.data.response

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "ValCurs", strict = false)
data class GoldResponse(
    @field:Element(name = "ValCurs", required = false)
    val valCurs: ValCurs? = null
)

@Root(name = "ValCurs", strict = false)
data class ValCurs(
    @field:ElementList(inline = true, required = false)
    val valute: List<Valute> = emptyList()
)

@Root(name = "Valute", strict = false)
data class Valute(
    @field:Element(name = "CharCode", required = false)
    val charcode: String = "",

    @field:Element(name = "Value", required = false)
    val value: String = "0"
)