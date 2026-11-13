package cn.stu.edu.sicnu.tay.bookmanager

import java.io.Serializable

/**
 * 书籍实体类，包含书名、作者、出版社、价格等属性
 */
data class Book(
    val title: String,
    val author: String,
    val publisher: String,
    val price: Double,
    val isbn: String,
    val publishYear: Int
) : Serializable
