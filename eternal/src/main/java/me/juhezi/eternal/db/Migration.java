package me.juhezi.eternal.db;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            schema.create("Record")
                    .addField("id", String.class)
                    .addField("time", String.class);
            oldVersion++;
        }
    }
}
