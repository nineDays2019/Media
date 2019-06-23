package me.juhezi.eternal.media.tools


/*
*[0] 0x20
* [1] 0x100
* [2] 0x22
* [3] 0x23
* [4] 0x24
* [5] 0x25
 */

enum class EternalFormat(val id: Int) {

    RAW_SENSOR(0x20),
    JPEG(0x100),
    PRIVATE(0x22),
    YUV_420_888(0x23),
    RAW_PRIVATE(0x24),
    RAW10(0x25)
}