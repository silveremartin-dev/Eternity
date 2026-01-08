/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.game.eternity2.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Internationalization provider for the Eternity II application.
 * Supports multiple languages via resource bundles.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class I18nProvider {

    private static I18nProvider instance;
    private static final String BUNDLE_BASE = "i18n.messages";

    private Locale currentLocale;
    private ResourceBundle bundle;

    private I18nProvider() {
        String lang = System.getenv().getOrDefault("ETERNITY_LANG", "en");
        setLocale(Locale.forLanguageTag(lang));
    }

    public static synchronized I18nProvider getInstance() {
        if (instance == null) {
            instance = new I18nProvider();
        }
        return instance;
    }

    /**
     * Set the current locale.
     */
    public void setLocale(Locale locale) {
        this.currentLocale = locale;
        try {
            this.bundle = ResourceBundle.getBundle(BUNDLE_BASE, locale);
        } catch (MissingResourceException e) {
            // Fallback to English
            this.bundle = ResourceBundle.getBundle(BUNDLE_BASE, Locale.ENGLISH);
        }
    }

    /**
     * Get the current locale.
     */
    public Locale getLocale() {
        return currentLocale;
    }

    /**
     * Get a localized message by key.
     */
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }

    /**
     * Get a localized message with parameters.
     */
    public String get(String key, Object... params) {
        String pattern = get(key);
        if (pattern.startsWith("!")) {
            return pattern;
        }
        return MessageFormat.format(pattern, params);
    }

    /**
     * Static shorthand for getting messages.
     */
    public static String msg(String key) {
        return getInstance().get(key);
    }

    /**
     * Static shorthand for getting messages with params.
     */
    public static String msg(String key, Object... params) {
        return getInstance().get(key, params);
    }

    /**
     * Get supported locales.
     */
    public static Locale[] getSupportedLocales() {
        return new Locale[] {
                Locale.ENGLISH,
                Locale.FRENCH,
                Locale.GERMAN,
                Locale.forLanguageTag("es")
        };
    }
}
