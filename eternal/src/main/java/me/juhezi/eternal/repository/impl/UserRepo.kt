package me.juhezi.eternal.repository.impl

import me.juhezi.eternal.global.Fail
import me.juhezi.eternal.global.Success
import me.juhezi.eternal.model.User
import me.juhezi.eternal.repository.interfaces.IUserRepo
import me.juhezi.eternal.repository.local.LocalUserRepo
import me.juhezi.eternal.repository.remote.RemoteUserRepo

/**
 * 暂时由本地操作代理
 */
class UserRepo(var localRepo: IUserRepo = LocalUserRepo(),
               var remoteRepo: IUserRepo = RemoteUserRepo()) : IUserRepo by remoteRepo {
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