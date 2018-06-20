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

import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class LocalisedString {

    public static final LocalisedString EMPTY = new LocalisedString("", "", "");

    private final String finnish;
    private final String swedish;
    private final String english;

    public static LocalisedString of(final String inFinnish) {
        return new LocalisedString(inFinnish, null, null);
    }

    public static LocalisedString of(final String inFinnish, final String inSwedish) {
        return new LocalisedString(inFinnish, inSwedish, null);
    }

    public static LocalisedString of(final String inFinnish, final String inSwedish, final String inEnglish) {
        return new LocalisedString(inFinnish, inSwedish, inEnglish);
    }

    public static LocalisedString fromMap(@Nonnull final Map<String, String> map) {
        Objects.requireNonNull(map);
        return of(map.get(Locales.FI_LANG), map.get(Locales.SV_LANG), map.get(Locales.EN_LANG));
    }

    public LocalisedString(final String finnish, final String swedish) {
        this.finnish = finnish;
        this.swedish = swedish;
        this.english = null;
    }

    public LocalisedString(final String finnish, final String swedish, final String english) {
        this.finnish = finnish;
        this.swedish = swedish;
        this.english = english;
    }

    public String getFinnish() {
        return finnish;
    }

    public String getSwedish() {
        return swedish;
    }

    public String getEnglish() {
        return english;
    }

    public boolean hasTranslation(final Locale locale) {
        return hasTranslation(locale.getLanguage());
    }

    public boolean hasTranslation(final String lang) {
        if (Locales.FI_LANG.equalsIgnoreCase(lang)) {
            return finnish != null;
        } else if (Locales.SV_LANG.equalsIgnoreCase(lang)) {
            return swedish != null;
        } else if (Locales.EN_LANG.equalsIgnoreCase(lang)) {
            return english != null;
        } else {
            throw new IllegalArgumentException("Unknown lang: " + lang);
        }
    }

    public String getAnyTranslation() {
        if (this.finnish != null) {
            return this.finnish;
        } else if (this.swedish != null) {
            return this.swedish;
        } else if (this.english != null) {
            return this.english;
        } else {
            return null;
        }
    }

    public String getAnyTranslation(final String preferredLang) {
        if (preferredLang == null) {
            return getAnyTranslation();
        }
        final String s = getTranslation(preferredLang);
        if (s != null) {
            return s;
        }
        return getAnyTranslation();
    }

    public String getAnyTranslation(final Locale preferred) {
        if (preferred == null) {
            return getAnyTranslation();
        }
        return getAnyTranslation(preferred.getLanguage());
    }

    public String getTranslation(final Locale locale) {
        return getTranslation(locale.getLanguage());
    }

    public String getTranslation(final String lang) {
        if (Locales.FI_LANG.equals(lang)) {
            return finnish;
        } else if (Locales.SV_LANG.equals(lang)) {
            return swedish;
        } else if (Locales.EN_LANG.equals(lang)) {
            return english;
        } else {
            return null;
        }
    }

    public boolean hasAnyTranslation() {
        return finnish != null || swedish != null || english != null;
    }

    public Map<String, String> asMap() {
        final Map<String, String> map = new LinkedHashMap<>();
        if (finnish != null) {
            map.put(Locales.FI_LANG, finnish);
        }
        if (swedish != null) {
            map.put(Locales.SV_LANG, swedish);
        }
        if (english != null) {
            map.put(Locales.EN_LANG, english);
        }
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LocalisedString that = (LocalisedString) o;

        return Objects.equals(this.finnish, that.finnish)
                && Objects.equals(this.swedish, that.swedish)
                && Objects.equals(this.english, that.english);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.finnish, this.swedish, this.english);
    }

    @Override
    public String toString() {
        final String trimmedFi = Strings.isNullOrEmpty(this.finnish) ? "-" : this.finnish;
        final String trimmedSv = Strings.isNullOrEmpty(this.swedish) ? "-" : this.swedish;
        final String trimmedEn = Strings.isNullOrEmpty(this.english) ? "-" : this.english;
        return String.format("%s / %s / %s", trimmedFi, trimmedSv, trimmedEn);
    }

    public Stream<String> asStream() {
        return Stream.of(finnish, swedish, english).filter(Objects::nonNull);
    }

    public LocalisedString transform(@Nonnull final Function<String, String> fn) {
        Objects.requireNonNull(fn);
        return of(fn.apply(finnish), fn.apply(swedish), fn.apply(english));
    }

}
