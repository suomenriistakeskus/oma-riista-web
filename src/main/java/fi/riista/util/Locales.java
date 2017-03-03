/*
 Copyright 2017 Finnish Wildlife Agency - Suomen Riistakeskus
 Copyright 2014 Ministry of Justice, Finland

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 NOTE: File contains modification to the original source.
*/
package fi.riista.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;

public final class Locales {

    public static final String FI_LANG = "fi";
    public static final String SV_LANG = "sv";
    public static final String EN_LANG = "en";

    private static final String FINLAND_COUNTRY_CODE = "FI";

    public static final Locale FI = new Locale(FI_LANG, FINLAND_COUNTRY_CODE);
    public static final Locale SV = new Locale(SV_LANG, FINLAND_COUNTRY_CODE);
    public static final Locale EN = new Locale(EN_LANG, FINLAND_COUNTRY_CODE);

    public static boolean isSwedish(@Nullable final Locale locale) {
        return locale != null && SV_LANG.equals(locale.getLanguage());
    }

    @Nonnull
    public static Locale getLocaleByLanguageCode(@Nullable final String languageCode) {
        return getLocaleByLanguageCode(languageCode, FI);
    }

    @Nonnull
    private static Locale getLocaleByLanguageCode(
            @Nullable final String languageCode, @Nonnull final Locale defaultLocale) {

        if (languageCode == null) {
            return defaultLocale;
        }

        switch (languageCode.trim().toLowerCase()) {
            case "fi":
                return FI;
            case "sv":
                return SV;
            case "en":
                return EN;
            default:
                return defaultLocale;
        }
    }

    private Locales() {
        throw new AssertionError();
    }

}
