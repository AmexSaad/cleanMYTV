package tv.cleanyour.app

import android.app.Application
import android.icu.text.MessageFormat
import androidx.test.core.app.ApplicationProvider
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CleanLocaleTest {
    @Test fun languageChoiceChangesFeatureAndEngineCopyWithoutReplacingTheApplication() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        try {
            for (language in listOf("ms", "en")) {
                CleanLocale.select(app, language)
                val localized = CleanLocale.wrap(app)
                assertEquals(language, localized.resources.configuration.locales[0].language)
                assertEquals(if (language == "ms") "Sokong cleanMYTV" else "Support cleanMYTV", localized.getString(R.string.clean_support))
            }
        } finally {
            app.getSharedPreferences("cleanmytv-ui", 0).edit().clear().commit()
        }
    }

    @Test fun everyMalayMessageKeepsItsArgumentsAndHasValidIcuSyntax() {
        val root = File(System.getProperty("cleanyourtv.repoRoot") ?: error("no repo root"))
        val modules = listOf("app", "ui/design", "feature/connect", "feature/setup", "feature/manage", "feature/launcher", "feature/modules", "feature/backup", "tv/curtain")
        fun strings(file: File): Map<String, String> {
            val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
            return (0 until nodes.length).associate { index ->
                val node = nodes.item(index) as org.w3c.dom.Element
                node.getAttribute("name") to node.textContent.replace("\\'", "'")
            }
        }
        var checked = 0
        for (module in modules) {
            val english = strings(File(root, "$module/src/main/res/values/strings.xml"))
            val malay = strings(File(root, "$module/src/main/res/values-ms/strings.xml"))
            for ((key, value) in malay) {
                assertTrue(key in english, "$module/$key has no source")
                assertEquals(MessageFormat(english.getValue(key), Locale.ENGLISH).argumentNames,
                    MessageFormat(value, Locale.forLanguageTag("ms")).argumentNames, "$module/$key")
                checked++
            }
        }
        assertTrue(checked > 1000, "Expected all feature copy to be covered")
    }
}
