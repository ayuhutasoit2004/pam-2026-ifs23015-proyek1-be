package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Book

@Serializable
data class BookRequest(
    var title: String = "",
    var author: String = "",
    var genre: String = "",
    var stock: Int = 0,
    var cover: String? = null,
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "author" to author,
        "genre" to genre,
        "stock" to stock,
    )

    fun toEntity(): Book = Book(
        title = title,
        author = author,
        genre = genre,
        stock = stock,
        cover = cover,
        updatedAt = Clock.System.now()
    )
}

@Serializable
data class BookResponse(val book: org.delcom.entities.Book)

@Serializable
data class BookIdResponse(val bookId: String)

@Serializable
data class BookMeta(
    val total: Long,
    val page: Int,
    val perPage: Int,
    val totalPages: Int
)

@Serializable
data class BookListResponse(
    val books: List<org.delcom.entities.Book>,
    val meta: BookMeta
)
