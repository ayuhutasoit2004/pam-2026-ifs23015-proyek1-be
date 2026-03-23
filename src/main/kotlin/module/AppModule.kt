package org.delcom.module

import org.delcom.repositories.*
import org.delcom.services.AuthService
import org.delcom.services.BookService
import org.delcom.services.BorrowingService
import org.delcom.services.UserService
import org.koin.dsl.module

fun appModule(jwtSecret: String) = module {

    // User Repository
    single<IUserRepository> { UserRepository() }

    // User Service
    single { UserService(get(), get()) }

    // Refresh Token Repository
    single<IRefreshTokenRepository> { RefreshTokenRepository() }

    // Auth Service
    single { AuthService(jwtSecret, get(), get()) }

    // Book Repository
    single<IBookRepository> { BookRepository() }

    // Book Service
    single { BookService(get()) }

    // Borrowing Repository
    single<IBorrowingRepository> { BorrowingRepository() }

    // Borrowing Service
    single { BorrowingService(get(), get()) }
}
