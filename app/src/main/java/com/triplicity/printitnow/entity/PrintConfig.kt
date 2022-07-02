package com.triplicity.printitnow.entity

class PrintConfig(
    var copies: Int = 1,
    var paperSize: String = "A4",
    var color: Boolean = true,
    var twoSided: Boolean = false
)