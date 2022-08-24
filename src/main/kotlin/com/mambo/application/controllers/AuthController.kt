package com.mambo.application.controllers

import com.mambo.application.utils.*
import com.mambo.data.dao.TokensDao
import com.mambo.data.helpers.TokenProvider
import com.mambo.data.models.ServerResponse
import com.mambo.data.requests.RefreshTokenRequest
import io.ktor.server.application.*
import io.ktor.server.request.*

class AuthController {

    private val tokensDao = TokensDao()

    suspend fun refreshToken(
        call: ApplicationCall, issuer: String, audience: String
    ): ServerResponse<Any?> = safeTransaction(
        error = "failed getting refresh token"
    ) {

        val request = call.receive<RefreshTokenRequest>()
        val token = request.token

        if (token.isNullOrBlank()) return@safeTransaction defaultBadRequestResponse(message = "Invalid refresh token")

        val id = TokenProvider.verify(issuer = issuer, audience = audience, token = token)
            ?: return@safeTransaction defaultBadRequestResponse(message = "Invalid refresh token")

        val userId = id.asUUID() ?: return@safeTransaction defaultBadRequestResponse(message = "Invalid refresh token")

        val isNotRefreshToken = TokenProvider.getTokenType(issuer, audience, token) != "refresh"

        if (isNotRefreshToken) return@safeTransaction defaultBadRequestResponse(message = "Invalid refresh token")

        if (tokensDao.exists(userId = userId, token = token).not())
            return@safeTransaction defaultBadRequestResponse(message = "Refresh token has expired sing in to continue")

        tokensDao.delete(token)

        tokensDao.deleteExpiredTokens(userId = userId)

        val tokens = TokenProvider.generateTokens(issuer = issuer, audience = audience, userId = id)

        val expiry = TokenProvider.getTokenExpiryDate(issuer = issuer, audience = audience, token = tokens.refreshToken)

        tokensDao.save(token = tokens.refreshToken, userId = userId, expiry = expiry.toDateTimeString()!!)

        defaultOkResponse(message = "tokens successfully regenerated", tokens)

    }

}