package com.seki999.echowordy.ui.navigation

object Routes {
    const val HOME = "home"
    const val LIST_DETAIL = "listDetail/{listId}"
    const val PASTE_CARDS = "pasteCards/{listId}"
    const val REVIEW = "review/{listId}"
    const val SETTINGS = "settings"

    const val ARG_LIST_ID = "listId"

    fun listDetail(listId: Long) = "listDetail/$listId"
    fun pasteCards(listId: Long) = "pasteCards/$listId"
    fun review(listId: Long) = "review/$listId"
}
