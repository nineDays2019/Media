package me.juhezi.eternal.model;

import java.io.Serializable;

import io.realm.RealmObject;
import me.juhezi.eternal.global.FunctionsKt;

/**
 * 没什么卵用的记录
 * Created by Juhezi[juhezix@163.com] on 2018/8/23.
 */
public class Record extends RealmObject implements Serializable {

    private String id;
    private String time;

    public String getId() {
        return id;
    }

    public Record setId(String id) {
        this.id = id;
        return this;
    }

    public String getTime() {
        return time;
    }

    public Record setTime(String time) {
        this.time = time;
        return this;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id='" + id + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public static Record generateRecord() {
        return new Record().setId(FunctionsKt.generateRandomID())
                .setTime(System.currentTimeMillis() + "");
    }

}
