package org.game.eternity2.i18n;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for I18nProvider.
 * 
 * @author Gemini AI Assistant
 * @author Silvère
 */
class I18nProviderTest {

    @Test
    @DisplayName("Get English message")
    void testEnglishMessage() {
        I18nProvider provider = I18nProvider.getInstance();
        provider.setLocale(Locale.ENGLISH);

        assertEquals("Eternity II Solver", provider.get("app.title"));
    }

    @Test
    @DisplayName("Get French message")
    void testFrenchMessage() {
        I18nProvider provider = I18nProvider.getInstance();
        provider.setLocale(Locale.FRENCH);

        assertEquals("Solveur Eternity II", provider.get("app.title"));
    }

    @Test
    @DisplayName("Get German message")
    void testGermanMessage() {
        I18nProvider provider = I18nProvider.getInstance();
        provider.setLocale(Locale.GERMAN);

        assertEquals("Eternity II Löser", provider.get("app.title"));
    }

    @Test
    @DisplayName("Missing key returns marker")
    void testMissingKey() {
        I18nProvider provider = I18nProvider.getInstance();
        String result = provider.get("nonexistent.key");

        assertTrue(result.startsWith("!"));
        assertTrue(result.endsWith("!"));
    }

    @Test
    @DisplayName("Static shorthand method")
    void testStaticMethod() {
        assertNotNull(I18nProvider.msg("app.title"));
    }

    @Test
    @DisplayName("Supported locales")
    void testSupportedLocales() {
        Locale[] locales = I18nProvider.getSupportedLocales();

        assertEquals(4, locales.length);
        assertEquals(Locale.ENGLISH, locales[0]);
        assertEquals(Locale.FRENCH, locales[1]);
    }
}
