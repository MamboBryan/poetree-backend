package com.mambobryan.utils

const val PAGE_SIZE = 20

data class PageData<T>(
    val next: Int?, val previous: Int?, val list: List<T>?
)

fun getLimitAndOffset(page: Int): Pair<Int, Long> {
    return when (page == 0) {
        true -> Pair(PAGE_SIZE, 0L)
        false -> {
            val offset = page.times(PAGE_SIZE).minus(PAGE_SIZE).toLong()
            Pair(PAGE_SIZE, offset)
        }
    }
}

fun <T> getPagedData(page: Int, list: List<T>): PageData<T> {

    val previousPage = if (page == 1) null else page - 1
    val nextPage = page + 1

    return PageData(next = nextPage, previous = previousPage, list = list)
}

