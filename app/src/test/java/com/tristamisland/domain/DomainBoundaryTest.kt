package com.tristamisland.domain

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DomainBoundaryTest {
    @Test
    fun domainSourcesDoNotImportAndroidOrCompose() {
        val domainDir = File("src/main/java/com/tristamisland/domain")
        assertTrue("Expected domain sources at ${domainDir.absolutePath}", domainDir.isDirectory)
        val violations = domainDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    val trimmed = line.trim()
                    val forbidden = trimmed.startsWith("import android.") ||
                        trimmed.startsWith("import androidx.") ||
                        trimmed.contains("@Composable")
                    if (forbidden) "${file.path}:${index + 1}: $trimmed" else null
                }
            }
            .toList()
        assertTrue(violations.joinToString("\n"), violations.isEmpty())
    }
}
