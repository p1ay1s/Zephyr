package com.p1ay1s.dev.base.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.p1ay1s.extensions.appContext
import com.p1ay1s.extensions.log.logE
import java.net.Inet4Address
import java.net.NetworkInterface

var appIpAddress = ""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object IPSetter {
    private val TAG = this::class.simpleName!!

    init {
        registerReceiver()
    }

    fun setIp() {
        appIpAddress = getIp()
    }

    /**
     * 注册网络连接状态的广播接收器
     */
    private fun registerReceiver() = with(IntentFilter()) {
        addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        com.p1ay1s.extensions.appContext.registerReceiver(
            NetworkConnectChangedReceiver(), this,
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    /**
     * 根据 wifi / 流量获取 ip
     */
    private fun getIp(): String {
        var ip = "0.0.0.0"

        val wifiManager: WifiManager =
            com.p1ay1s.extensions.appContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager: ConnectivityManager =
            com.p1ay1s.extensions.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        try {
            if (wifiManager.isWifiEnabled) {
                val network = connectivityManager.activeNetwork ?: return ip
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ip
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    val wifiInfo = wifiManager.connectionInfo
                    val ipAddress = wifiInfo.ipAddress

                    ip = (ipAddress and 0xFF).toString() + "." +
                            ((ipAddress shr 8) and 0xFF) + "." +
                            ((ipAddress shr 16) and 0xFF) + "." +
                            (ipAddress shr 24 and 0xFF)
                }
            } else {
                NetworkInterface.getNetworkInterfaces().toList().forEach { networkInterface ->
                    networkInterface.inetAddresses.toList().forEach {
                        it.run {
                            if (!isLoopbackAddress && this is Inet4Address) {
                                if (!hostAddress.isNullOrBlank())
                                    ip = hostAddress!!
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            com.p1ay1s.extensions.log.logE(TAG, "failed to get ip address")
        }
        return ip
    }

    /**
     * 监听网络变化并设置 ip 地址
     */
    class NetworkConnectChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                with(intent.getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_NETWORK_INFO)) {
                    if (this != null && isConnected) setIp()
                }
            }
        }
    }
}