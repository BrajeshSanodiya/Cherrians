package com.jans.imageload


sealed class Result<out T : Any> {

    class Success<out T : Any>(val data: T) : Result<T>()

    class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "An unknown error occurred!"
    ) : Result<Nothing>()

    class Progress(val isLoading: Boolean) : Result<Nothing>()

}

/*
* R :- if success then value will be returned
*       if value null then NULL Pointer Exception
*       else throw appropriate error
**/
inline fun <R : Any> tryCatching(block: () -> R): Result<R> {
    return try {
        Result.Success(block())
    } catch (e: Throwable) {
        Result.Error(e)
    }
}