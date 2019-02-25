package me.juhezi.media.commen.config;

/**
 * 缩放模式
 * 视频、图片显示会用到
 */
public enum ScaleMode {

    NONE("直接拉伸至 View 的大小，会发生变形"),
    FILL_X("按比例拉伸，x 填满"),
    FILL_Y("按比例拉伸，y 填满"),
    AUTO_FIT("按比例拉伸，最大边不超过 View 的大小"),
    AUTO_FILL("按比例拉伸，最小边填满 View，相当于自适应 FILL_X 或 FILL_Y");

    public final String desc;   // 描述

    ScaleMode(String desc) {
        this.desc = desc;
    }

}