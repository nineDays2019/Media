package me.juhezi.eternal.db

import io.realm.Realm
import me.juhezi.eternal.base.BaseApplication

class DB {
    companion object {
        @JvmStatic
        fun init() {
            Realm.init(BaseApplication.getApplicationContext())
            DBConfig.applyDefaultDBConfig()
        }
    }
}