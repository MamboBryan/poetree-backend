package com.mambobryan.data

import com.mambobryan.data.tables.topic.TopicsTable
import com.mambobryan.data.tables.user.UsersTable
import com.mambobryan.data.tables.comment.CommentLikesTable
import com.mambobryan.data.tables.comment.CommentsTable
import com.mambobryan.data.tables.poem.BookmarksTable
import com.mambobryan.data.tables.poem.PoemLikesTable
import com.mambobryan.data.tables.poem.PoemsTable
import com.mambobryan.data.tables.poem.ReadsTable
import com.mambobryan.data.tables.poem.relations.CompletePoemEntity
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionScope

object Database {

    fun init() {
        Database.connect(hikariConfiguration())

        transactionScope {
            SchemaUtils.createDatabase("poetree")
        }

        transaction {
            // tables
            SchemaUtils.create(
                UsersTable,
                TopicsTable,
                CommentsTable,
                CommentLikesTable,
                PoemsTable,
                PoemLikesTable,
                ReadsTable,
                BookmarksTable
            )
        }
    }

}

private fun hikariConfiguration(): HikariDataSource {
    val config = HikariConfig()

    config.apply {
        driverClassName = System.getenv("JDBC_DRIVER")
        jdbcUrl = System.getenv("JDBC_DATABASE_URL")
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }

    config.validate()
    return HikariDataSource(config)
}

suspend fun <T> query(block: () -> T): T = withContext(Dispatchers.IO) {
    transaction {
        block.invoke()
    }
}