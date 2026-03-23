package org.delcom.entities

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Borrowing(
    var id: String = UUID.randomUUID().toString(),
    var bookId: String,
    var bookTitle: String = "",
    var bookAuthor: String = "",
    var borrowerName: String,
    var borrowDate: @Contextual Instant = Clock.System.now(),
    var returnDate: @Contextual Instant? = null,
    var status: String = "borrowed", // borrowed, returned

    @Contextual
    val createdAt: Instant = Clock.System.now(),
    @Contextual
    var updatedAt: Instant = Clock.System.now(),
)
