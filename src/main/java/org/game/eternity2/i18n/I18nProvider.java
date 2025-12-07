package org.game.eternity2.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Internationalization provider for the Eternity II application.
 * Supports multiple languages via resource bundles.
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
                new Locale("es")
        };
    }
}
