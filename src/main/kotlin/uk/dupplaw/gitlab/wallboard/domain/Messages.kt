package uk.dupplaw.gitlab.wallboard.domain

interface Message

data class HelloMessage(
    val msg: String
) : Message
