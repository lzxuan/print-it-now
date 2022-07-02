package com.triplicity.printitnow.entity

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
class Shop {
    var name: String? = null
    var address: String? = null
    var description: String? = null
    var enabled: Boolean? = null
}