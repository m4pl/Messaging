package messaging.licenses

internal object HtmlRenderer {

    private val headBefore: String
    private val headAfter: String

    init {
        val template = readResource("html/head.html")
        val parts = template.split(TITLE_PLACEHOLDER)

        require(parts.size == 2) {
            "html/head.html must contain exactly one $TITLE_PLACEHOLDER"
        }

        headBefore = parts[0]
        headAfter = parts[1]
    }

    fun render(
        title: String,
        items: List<RenderedLicense>,
    ): String = buildString {
        append(headBefore)
        append(title.htmlEscape())
        append(headAfter)
        for (item in items) {
            appendBlock(
                heading = headingFor(item.record),
                body = bodyFor(item),
            )
        }
        append(TAIL)
    }

    private fun headingFor(record: LicenseRecord): String {
        val coordinates = record.coordinates
        val projectName = record.projectName?.trim().orEmpty()
        val isMissing = projectName.isEmpty()
        val sameAsArtifact = projectName.equals(coordinates.name, ignoreCase = true)

        return when {
            isMissing || sameAsArtifact -> "Notice for $coordinates"
            else -> "Notice for $projectName ($coordinates)"
        }
    }

    private fun bodyFor(item: RenderedLicense): String {
        val url = item.record.projectUrl?.trim().orEmpty()

        return when {
            url.isEmpty() -> item.text
            else -> "Source: $url\n\n${item.text}"
        }
    }

    private fun StringBuilder.appendBlock(
        heading: String,
        body: String,
    ) {
        append("<details>\n")
        append("<summary><span class=\"label\">")
        append(heading.htmlEscape())
        append("</span><span class=\"chev\" aria-hidden=\"true\">+</span></summary>\n")
        append("<pre>\n")
        append(body.htmlEscape())
        append("\n</pre>\n</details>\n\n")
    }

    private fun readResource(path: String): String {
        return javaClass.classLoader.getResource(path)
            ?.readText()
            ?: error("Missing resource: $path")
    }

    private fun String.htmlEscape(): String {
        return replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
    }

    private const val TITLE_PLACEHOLDER = "{title}"
    private const val TAIL = "</body></html>\n"
}
