package org.delcom.data

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import org.delcom.entities.Borrowing

@Serializable
data class BorrowingRequest(
    var bookId: String = "",
    var borrowerName: String = "",
    var status: String = "borrowed", // borrowed, returned
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "bookId" to bookId,
        "borrowerName" to borrowerName,
        "status" to status,
    )

    fun toEntity(): Borrowing = Borrowing(
        bookId = bookId,
        borrowerName = borrowerName,
        status = status,
        borrowDate = Clock.System.now(),
        updatedAt = Clock.System.now()
    )
}

@Serializable
data class BorrowingResponse(val borrowing: org.delcom.entities.Borrowing)

@Serializable
data class BorrowingIdResponse(val borrowingId: String)

@Serializable
data class BorrowingMeta(
    val total: Long,
    val page: Int,
    val perPage: Int,
    val totalPages: Int
)

@Serializable
data class BorrowingListResponse(
    val borrowings: List<org.delcom.entities.Borrowing>,
    val meta: BorrowingMeta
)
