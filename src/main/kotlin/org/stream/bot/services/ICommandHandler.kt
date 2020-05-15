package org.stream.bot.services

import org.telegram.telegrambots.meta.api.objects.Update

const val MARKDOWN_ENABLED = false

interface ICommandHandler {

    fun answer(update: Update)

    fun firstReply(update: Update)

    fun secondReply(update: Update)

    fun thirdReply(update: Update)

    fun String?.bookText(): String{
        return "*$this*"
    }

    //TODO: Make markdown which can stable escape all characters
    // Text that cannot be escaped: You've send me EF3e_int_filetest_01a.pdf.\nI wish you an exciting read.😌
    fun String?.botText(): String?{
        //return "_${this}_"
        return this
    }

    fun String?.escapeCharacters(): String{
        if (this!=null)
        return this.replace("_","\\_")
                .replace("*","\\*")
                .replace("`","\\`")
                .replace("[","\\[")
                .replace("]","\\]")
                .replace("(","\\(")
                .replace(")","\\)")
                .replace("~","\\~")
                .replace(">","\\>")
                .replace("#","\\#")
                .replace("+","\\+")
                .replace("-","\\-")
                .replace("=","\\=")
                .replace("|","\\|")
                .replace("{","\\{")
                .replace("}","\\}")
                .replace(".","\\.")
                .replace("!","\\!")
                .replace("\\","\\\\")

        return ""
    }
}