package com.example.secure

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.SignatureVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Application.configureSecurity() {

    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val myRealm = environment.config.property("jwt.realm").getString()

    install(Authentication) {
        jwt("jwt") {
            realm = myRealm
            verifier(
                    JWT
                        .require(Algorithm.HMAC512(secret))
                        .withAudience(audience)
                        .withIssuer(issuer)
                        .build()
            )

            challenge { _, _ ->
                // get custom error message if error exists
                val header = call.request.headers["Authorization"]
                header?.let {
                    if (it.isNotEmpty()) {
                        try {
                            if ((!it.contains("Bearer", true))) throw JWTDecodeException("")
                            val jwt = it.replace("Bearer ", "")
                            verifier(jwt)
                        } catch (e: TokenExpiredException) {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                "Authentication failed: Access token expired"
                            )
                        } catch (e: SignatureVerificationException) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Authentication failed: Failed to parse Access token"
                            )
                        } catch (e: JWTDecodeException) {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                "Authentication failed: Failed to parse Access token"
                            )
                        }
                    } else call.respond(
                        HttpStatusCode.BadRequest,
                        "Authentication failed: Access token not found"
                    )
                } ?: call.respond(
                    HttpStatusCode.Unauthorized, "Authentication failed: No authorization header found"
                )
                HttpStatusCode.Unauthorized
            }

            validate { credential ->

                if (credential.payload.getClaim("userId").asString() != "" ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
}
