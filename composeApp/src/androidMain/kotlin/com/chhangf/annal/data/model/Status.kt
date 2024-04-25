package com.chhangf.annal.data.model

/**
 *  状态：待完成/已完成/删除
 */
enum class Status(val displayText: String) {
    PENDING("待完成"),
    IN_PROGRESS("进行中"),
    COMPLETED("已完成"),
    DELETED("已删除");
    override fun toString(): String {
        return displayText
    }
}