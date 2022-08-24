package com.mambo.data.tables.tokens

import com.mambo.data.tables.user.UsersTable
import org.jetbrains.exposed.dao.id.UUIDTable

object TokensTable : UUIDTable() {
    val user = reference("user", UsersTable)
    val token = varchar("token", 512)
    val expiration = varchar("expiration", 255)
}