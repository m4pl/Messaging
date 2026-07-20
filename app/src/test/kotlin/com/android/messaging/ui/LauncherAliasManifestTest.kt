package com.android.messaging.ui

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

internal class LauncherAliasManifestTest {

    private val launcherAliasName = ".ui.conversationlist.ConversationListActivity"
    private val hostName = ".ui.MainActivity"

    @Test
    fun launcherAlias_keepsPublicName_andTargetsHost() {
        val alias = requireNotNull(
            aliases().singleOrNull { element ->
                element.getAttribute("android:name") == launcherAliasName
            },
        ) { "Missing launcher alias $launcherAliasName" }

        assertEquals(hostName, alias.getAttribute("android:targetActivity"))
        assertEquals("true", alias.getAttribute("android:exported"))
        assertTrue(alias.declaresLauncherIntentFilter())
    }

    @Test
    fun host_isNotExported_andDeclaresNoIntentFilter() {
        val host = requireNotNull(
            activities().singleOrNull { element ->
                element.getAttribute("android:name") == hostName
            },
        ) { "Missing host activity $hostName" }

        assertEquals("false", host.getAttribute("android:exported"))
        assertTrue(host.getElementsByTagName("intent-filter").length == 0)
    }

    private fun Element.declaresLauncherIntentFilter(): Boolean {
        val actions = childElementValues(tagName = "action")
        val categories = childElementValues(tagName = "category")

        return "android.intent.action.MAIN" in actions &&
            "android.intent.category.LAUNCHER" in categories
    }

    private fun Element.childElementValues(tagName: String): Set<String> {
        val nodes = getElementsByTagName(tagName)

        return (0 until nodes.length)
            .map { index -> nodes.item(index) as Element }
            .map { element -> element.getAttribute("android:name") }
            .toSet()
    }

    private fun aliases(): List<Element> {
        return manifestElements(tagName = "activity-alias")
    }

    private fun activities(): List<Element> {
        return manifestElements(tagName = "activity")
    }

    private fun manifestElements(tagName: String): List<Element> {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(manifestFile())

        val nodes = document.getElementsByTagName(tagName)

        return (0 until nodes.length).map { index -> nodes.item(index) as Element }
    }

    private fun manifestFile(): File {
        val workingDir = requireNotNull(System.getProperty("user.dir"))
        var directory: File? = File(workingDir)

        while (directory != null) {
            val candidate = File(directory, "AndroidManifest.xml")
            if (candidate.exists()) {
                return candidate
            }
            directory = directory.parentFile
        }

        error("Could not locate AndroidManifest.xml from $workingDir")
    }
}
