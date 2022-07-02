package com.triplicity.printitnow.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.triplicity.printitnow.entity.User

class ProfileViewModel : ViewModel() {

    var uid = ""

    private var _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    fun setUser(user: User) {
        _user.value = user
    }

}