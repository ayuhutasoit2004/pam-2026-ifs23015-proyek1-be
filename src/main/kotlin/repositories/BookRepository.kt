package org.delcom.repositories

import org.delcom.dao.BookDAO
import org.delcom.entities.Book
import org.delcom.helpers.bookDAOToModel
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.BookTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class BookRepository : IBookRepository {

    override suspend fun getAll(
        search: String,
        genre: String?,
        page: Int,
        perPage: Int
    ): Pair<List<Book>, Long> = suspendTransaction {
        var condition: Op<Boolean> = Op.TRUE

        if (search.isNotBlank()) {
            val keyword = "%${search.lowercase()}%"
            val searchCondition = (BookTable.title.lowerCase() like keyword)
            condition = condition and searchCondition
        }

        if (!genre.isNullOrBlank()) {
            condition = condition and (BookTable.genre eq genre)
        }

        val total = BookDAO.find(condition).count()
        val offset = ((page - 1) * perPage).toLong()
        val books = BookDAO
            .find(condition)
            .orderBy(BookTable.createdAt to SortOrder.DESC)
            .offset(offset)
            .limit(perPage)
            .map(::bookDAOToModel)

        Pair(books, total)
    }

    override suspend fun getById(bookId: String): Book? = suspendTransaction {
        BookDAO
            .find { BookTable.id eq UUID.fromString(bookId) }
            .limit(1)
            .map(::bookDAOToModel)
            .firstOrNull()
    }

    override suspend fun create(book: Book): String = suspendTransaction {
        val bookDAO = BookDAO.new {
            title = book.title
            author = book.author
            genre = book.genre
            stock = book.stock
            cover = book.cover
            createdAt = book.createdAt
            updatedAt = book.updatedAt
        }
        bookDAO.id.value.toString()
    }

    override suspend fun update(bookId: String, newBook: Book): Boolean = suspendTransaction {
        val bookDAO = BookDAO
            .find { BookTable.id eq UUID.fromString(bookId) }
            .limit(1)
            .firstOrNull()

        if (bookDAO != null) {
            bookDAO.title = newBook.title
            bookDAO.author = newBook.author
            bookDAO.genre = newBook.genre
            bookDAO.stock = newBook.stock
            bookDAO.cover = newBook.cover
            bookDAO.updatedAt = newBook.updatedAt
            true
        } else false
    }

    override suspend fun delete(bookId: String): Boolean = suspendTransaction {
        val rowsDeleted = BookTable.deleteWhere {
            BookTable.id eq UUID.fromString(bookId)
        }
        rowsDeleted >= 1
    }

    override suspend fun updateStock(bookId: String, delta: Int): Boolean = suspendTransaction {
        val bookDAO = BookDAO
            .find { BookTable.id eq UUID.fromString(bookId) }
            .limit(1)
            .firstOrNull()

        if (bookDAO != null) {
            bookDAO.stock = bookDAO.stock + delta
            true
        } else false
    }
}
