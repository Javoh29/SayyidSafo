package uz.mnsh.sayyidsafo.data.network

import uz.mnsh.sayyidsafo.data.provider.UnitProvider
import okhttp3.Interceptor
import okhttp3.Response
import uz.mnsh.sayyidsafo.utils.NoConnectivityException

class ConnectivityInterceptorImpl(
    private val unitProvider: UnitProvider
) : ConnectivityInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!unitProvider.isOnline())
            throw NoConnectivityException()
        return chain.proceed(chain.request())
    }
}