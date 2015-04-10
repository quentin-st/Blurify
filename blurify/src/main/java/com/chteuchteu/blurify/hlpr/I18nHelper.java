package com.chteuchteu.blurify.hlpr;

public class I18nHelper {
    public enum AppLanguage {
        EN("en"), FR("fr");

        public String langCode;

        AppLanguage(String langCode) {
            this.langCode = langCode;
        }
        public static AppLanguage defaultLang() { return AppLanguage.EN; }

        /**
         * get AppLanguage enum value from langCode.
         *  If not found, returns the default language (EN)
         */
        public static AppLanguage get(String langCode) {
            for (AppLanguage lang : AppLanguage.values()) {
                if (lang.langCode.equals(langCode))
                    return lang;
            }

            return defaultLang();
        }
    }

    public static boolean isLanguageSupported(String languageCode) {
        for (AppLanguage lang : AppLanguage.values()) {
            if (lang.langCode.equals(languageCode))
                return true;
        }

        return false;
    }
}
