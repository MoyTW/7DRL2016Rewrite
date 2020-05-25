package com.mtw.supplier.client

import com.mtw.supplier.engine.Serializers
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.state.EncounterState
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.asString
import okhttp3.Response
import org.slf4j.LoggerFactory


class NetworkClient(
    private val SERVER_PORT: Int = 8080
) {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun postAction(action: Action): String? {
        val response: Response = httpPost {
            host = "localhost"
            port = SERVER_PORT
            path = "/game/player/action"
            body {
                string(Serializers.stringify(action))
            }
        }
        return response.use {
            response.asString()
        }
    }

    fun refreshEncounterState(): EncounterState? {
        val response: Response = httpGet {
            host = "localhost"
            port = SERVER_PORT
            path = "/game/state"
        }
        response.use {
            val body = response.asString()
            return if (body != null) {
                Serializers.parse(body)
            } else {
                null
            }
        }
    }

    fun resetGame(): EncounterState? {
        val response: Response = httpPost {
            host = "localhost"
            port = SERVER_PORT
            path = "/game/reset"
        }
        response.use {
            val body = response.asString()
            return if (body != null) {
                Serializers.parse(body)
            } else {
                null
            }
        }
    }
}
