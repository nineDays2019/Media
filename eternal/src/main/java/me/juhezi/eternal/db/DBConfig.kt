package me.juhezi.eternal.db

import io.realm.Realm
import io.realm.RealmConfiguration
import me.juhezi.eternal.global.DB_VERSION

class DBConfig {

    companion object {

        @JvmStatic
        fun defaultDBConfig(): RealmConfiguration {
            return RealmConfiguration.Builder()
                    .schemaVersion(DB_VERSION)
                    .migration(Migration())
                    .build()
        }

        @JvmStatic
        fun applyDefaultDBConfig() {
            Realm.setDefaultConfiguration(defaultDBConfig())
        }
    }

}