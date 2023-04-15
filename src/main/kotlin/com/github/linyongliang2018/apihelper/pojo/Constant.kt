package com.github.linyongliang2018.apihelper.pojo

import org.springframework.web.bind.annotation.RequestBody
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * 在Kotlin中，const val要求值必须是一个编译时常量表达式，这意味着它的值必须在编译时就已知。
 * 然而，RequestBody::class.java.name这个表达式的值是在运行时计算得到的，因此它不能用于初始化一个const val。
 */
val NOT_NULL = NotNull::class.java.name

val NOT_EMPTY = NotBlank::class.java.name

val REQUEST_BODY = RequestBody::class.java.name