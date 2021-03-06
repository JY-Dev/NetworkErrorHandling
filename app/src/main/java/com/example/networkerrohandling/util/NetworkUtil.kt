package com.example.networkerrohandling.util

import android.util.Log
import com.example.networkerrohandling.data.datasource.JwtRefresh
import com.example.networkerrohandling.data.model.NetworkResult
import com.example.networkerrohandling.data.model.Response
import java.lang.Exception

const val REST_CODE_NO_SEARCH_MOVIE = "C000"
const val REST_SUCCESS = "E000"
const val REST_CODE_ERROR_TOKEN_EXPIRED = "E991"
const val REST_CODE_ERROR_JWT_REFRESH = "E995"

fun <T> Response<T>.toNetworkResult() : NetworkResult<T> {
    return when(code){
        REST_SUCCESS -> NetworkResult.Success(data)
        REST_CODE_ERROR_JWT_REFRESH -> throw JwtRefreshException(message)
        REST_CODE_ERROR_TOKEN_EXPIRED -> throw TokenExpireException(message)
        else -> throw ServerFailException(message)
    }
}

private fun <R> changeNetworkData(replaceData: R): NetworkResult<R> {
    return NetworkResult.Success(replaceData)
}

suspend fun <T,R> NetworkResult<T>.mapNetworkResult(getData : suspend (T) -> R) : NetworkResult<R>{
    return changeNetworkData(getData(toModel()))
}

fun <T> NetworkResult<T>.toModel() : T =
    (this as NetworkResult.Success).data


suspend fun <T,R> NetworkResult<T>.map(getData : suspend (T) -> R) : R{
    val data = toModel()
    return getData(data)
}

suspend fun <T> networkHandling(block : suspend () -> T) : NetworkResult<T> {
    return try {
        NetworkResult.Success(block())
    } catch (e : Exception){
        when(e){
            is JwtRefreshException -> {
                JwtRefresh.isJwtRefresh = true
                Log.d("Network Exception","JwtRefreshException")
                networkHandling { block() }
            }
            is TokenExpireException -> {
                Log.d("Network Exception","TokenExpireException")
                NetworkResult.TokenExpired
            }
            is ServerFailException -> {
                NetworkResult.Fail(e.message?:"")
            }
            else -> {
                Log.d("Network Exception","UnknownException")
                NetworkResult.Exception(e)
            }
        }
    }
}