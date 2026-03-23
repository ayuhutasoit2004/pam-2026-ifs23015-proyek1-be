package org.delcom.helpers

import kotlinx.coroutines.Dispatchers
import org.delcom.dao.BookDAO
import org.delcom.dao.BorrowingDAO
import org.delcom.dao.RefreshTokenDAO
import org.delcom.dao.UserDAO
import org.delcom.entities.Book
import org.delcom.entities.Borrowing
import org.delcom.entities.RefreshToken
import org.delcom.entities.User
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun userDAOToModel(dao: UserDAO) = User(
    dao.id.value.toString(),
    dao.name,
    dao.username,
    dao.password,
    dao.photo,
    dao.about,
    dao.createdAt,
    dao.updatedAt
)

fun refreshTokenDAOToModel(dao: RefreshTokenDAO) = RefreshToken(
    dao.id.value.toString(),
    dao.userId.toString(),
    dao.refreshToken,
    dao.authToken,
    dao.createdAt,
)

fun bookDAOToModel(dao: BookDAO) = Book(
    id = dao.id.value.toString(),
    title = dao.title,
    author = dao.author,
    genre = dao.genre,
    stock = dao.stock,
    cover = dao.cover,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)

fun borrowingDAOToModel(dao: BorrowingDAO, bookTitle: String = "", bookAuthor: String = "") = Borrowing(
    id = dao.id.value.toString(),
    bookId = dao.bookId.toString(),
    bookTitle = bookTitle,
    bookAuthor = bookAuthor,
    borrowerName = dao.borrowerName,
    borrowDate = dao.borrowDate,
    returnDate = dao.returnDate,
    status = dao.status,
    createdAt = dao.createdAt,
    updatedAt = dao.updatedAt
)
