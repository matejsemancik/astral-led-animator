package dev.matsem.ala.tools.extensions

inline fun <T> Array<T>.modifyIndexed(mapper: (index: Int, current: T) -> T) {
    for (i in 0 until size) {
        this[i] = mapper(i, this[i])
    }
}