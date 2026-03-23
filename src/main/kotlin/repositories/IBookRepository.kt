package org.delcom.repositories

import org.delcom.entities.Book

interface IBookRepository {
    suspend fun getAll(
        search: String,
        genre: String?,
        page: Int,
        perPage: Int
    ): Pair<List<Book>, Long>

    suspend fun getById(bookId: String): Book?
    suspend fun create(book: Book): String
    suspend fun update(bookId: String, newBook: Book): Boolean
    suspend fun delete(bookId: String): Boolean
    suspend fun updateStock(bookId: String, delta: Int): Boolean
}
