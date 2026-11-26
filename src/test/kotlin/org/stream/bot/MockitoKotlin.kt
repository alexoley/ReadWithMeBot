package org.stream.bot

import org.mockito.ArgumentCaptor
import org.mockito.Mockito

/*
 * Mockito matchers return null, which Kotlin rejects for non-null parameter types.
 * These helpers register the matcher and hand Kotlin an unchecked null instead.
 */

@Suppress("UNCHECKED_CAST")
private fun <T> nullAs(): T = null as T

fun <T> anyK(type: Class<T>): T {
    Mockito.any(type)
    return nullAs()
}

fun <T> anyK(): T {
    Mockito.any<T>()
    return nullAs()
}

fun <T : Any> ArgumentCaptor<T>.captureK(): T {
    capture()
    return nullAs()
}
