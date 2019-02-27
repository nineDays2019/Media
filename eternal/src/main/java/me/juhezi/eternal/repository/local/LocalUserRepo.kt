package me.juhezi.eternal.repository.local

import me.juhezi.eternal.global.Fail
import me.juhezi.eternal.global.Success
import me.juhezi.eternal.model.User
import me.juhezi.eternal.repository.interfaces.IUserRepo

class LocalUserRepo : IUserRepo {
    override fun add(t: User, success: Success<User>?, fail: Fail?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update(t: User, success: Success<User>?, fail: Fail?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(id: String, success: Success<User>?, fail: Fail?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun query(id: String, success: Success<User>?, fail: Fail?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun queryAll(success: Success<List<User>>?, fail: Fail?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}