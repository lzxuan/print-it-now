package com.triplicity.printitnow.entity

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
class Print {
    var status: String? = null
    var createdOn: Date? = null
    var price: Double? = null
    var files: List<String>? = null
    var printConfig: HashMap<String, Any>? = null
    var pages: Int? = null
}