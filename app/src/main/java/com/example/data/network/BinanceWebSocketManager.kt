package com.example.data.network

import android.util.Log
import com.example.data.model.MiniTickerWsDto
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

enum class WsConnectionState {
    DISCONNECTED,
    CONNECTING,
    LIVE,
    ERROR
}

class BinanceWebSocketManager {

    private val tag = "BinanceWebSocket"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionState = MutableStateFlow(WsConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WsConnectionState> = _connectionState.asStateFlow()

    private val _tickerUpdates = MutableSharedFlow<List<MiniTickerWsDto>>(extraBufferCapacity = 64)
    val tickerUpdates: SharedFlow<List<MiniTickerWsDto>> = _tickerUpdates.asSharedFlow()

    private var webSocket: WebSocket? = null
    private var isIntentionalClose = false

    private val listType = Types.newParameterizedType(List::class.java, MiniTickerWsDto::class.java)
    private val adapter = NetworkClient.moshi.adapter<List<MiniTickerWsDto>>(listType)

    fun startListening() {
        if (_connectionState.value == WsConnectionState.LIVE || _connectionState.value == WsConnectionState.CONNECTING) {
            return
        }
        isIntentionalClose = false
        connect()
    }

    fun stopListening() {
        isIntentionalClose = true
        webSocket?.close(1000, "App paused or stopped")
        webSocket = null
        _connectionState.value = WsConnectionState.DISCONNECTED
    }

    private fun connect() {
        _connectionState.value = WsConnectionState.CONNECTING
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/!miniTicker@arr")
            .build()

        webSocket = NetworkClient.okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(tag, "WebSocket Connected successfully to Binance stream")
                _connectionState.value = WsConnectionState.LIVE
            }

            override fun onMessage(ws: WebSocket, text: String) {
                try {
                    val tickers = adapter.fromJson(text)
                    if (!tickers.isNullOrEmpty()) {
                        _tickerUpdates.tryEmit(tickers)
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Failed to parse ws mini ticker: ${e.message}")
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "WebSocket Error: ${t.message}")
                _connectionState.value = WsConnectionState.ERROR
                scheduleReconnect()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket Closed: $code / $reason")
                if (!isIntentionalClose) {
                    _connectionState.value = WsConnectionState.DISCONNECTED
                    scheduleReconnect()
                }
            }
        })
    }

    private fun scheduleReconnect() {
        if (isIntentionalClose) return
        scope.launch {
            delay(4000)
            if (!isIntentionalClose && scope.isActive) {
                connect()
            }
        }
    }
}
