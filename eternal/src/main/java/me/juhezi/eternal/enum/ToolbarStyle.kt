package me.juhezi.eternal.enum

enum class ToolbarStyle(val key: Int) {

    ICON(0x100),   // 图标

    TEXT(0x101),   // 文本

    NONE(0x102),   // 没有控件

    ICON_AND_TEXT(0x103),     // 图标与文本同时存在
}