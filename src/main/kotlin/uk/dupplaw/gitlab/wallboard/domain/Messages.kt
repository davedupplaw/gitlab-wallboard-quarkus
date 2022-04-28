package uk.dupplaw.gitlab.wallboard.domain

interface Message {
    val type: String
}

data class ProjectInfoMessage(
    val id: String,
    val name: String,
) : Message {
    override val type: String = "project-info"
}
