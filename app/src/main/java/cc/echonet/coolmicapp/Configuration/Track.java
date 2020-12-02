/*
 *      Copyright (C) Jordan Erickson                     - 2014-2020,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2020
 *       on behalf of Jordan Erickson.
 *
 * This file is part of Cool Mic.
 *
 * Cool Mic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cool Mic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cool Mic.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package cc.echonet.coolmicapp.Configuration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Track extends ProfileBase {
    public static final @NotNull @UnmodifiableView List<@NotNull String> STANDARD_KEYS =
            Collections.unmodifiableList(
                    Arrays.asList("title", "version", "album", "artist", "performer", "copyright", "license", "organization", "description", "genre", "location", "contact"));
    private static final @NotNull String PREF_PREFIX = "trackmetadata_key_";
    private static final @NotNull String PREF_PREFIX_LEGACY = "general_";

    Track(@NotNull ProfileBase profile) {
        super(profile);
    }

    private static boolean isLegacyKey(@NotNull String key) {
        key = normalizeKey(key);

        return key.equals("artist") || key.equals("title");
    }

    @Contract("_ -> new")
    private static @NotNull String toUpperFirst(@NotNull String str) {
        if (str.isEmpty()) {
            return "";
        } else {
            char[] chars = str.toCharArray();

            chars[0] = Character.toUpperCase(chars[0]);

            return new String(chars);
        }
    }

    public static @NotNull String getKeyDisplayName(@NotNull String key) {
        key = normalizeKey(key);

        if (key.contains("_"))
            return key.toUpperCase(Locale.ROOT);

        return toUpperFirst(key);
    }

    public static @NotNull String normalizeKey(@NotNull String key) {
        return key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
    }

    public @NotNull List<String> getKeys() {
        final @NotNull List<String> ret = new ArrayList<>();

        for (final @NotNull Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (entry.getValue() instanceof String) {
                final @NotNull String key = entry.getKey();
                final @NotNull String value = (String) entry.getValue();

                if (key.startsWith(PREF_PREFIX) && !value.isEmpty()) {
                    ret.add(key.substring(PREF_PREFIX.length()));
                }
            }
        }

        for (final @NotNull String key : new String[]{"artist", "title"}) {
            if (!ret.contains(key))
                if (getValue(key, null) != null)
                    ret.add(key);
        }

        Collections.sort(ret);

        return ret;
    }

    @Contract("_, !null -> !null; _, null -> _")
    public @Nullable String getValue(@NotNull String key, @Nullable String def) {
        final @Nullable String ret;

        key = normalizeKey(key);

        if (isLegacyKey(key))
            def = getString(PREF_PREFIX_LEGACY + key, def);

        ret = getString(PREF_PREFIX + key, def);

        if (ret == null || ret.isEmpty())
            return def;

        return ret;
    }

    public void setValue(@NotNull String key, @Nullable String value) {
        key = normalizeKey(key);

        if (isLegacyKey(key))
            editor.remove(PREF_PREFIX_LEGACY + key);

        key = PREF_PREFIX + key;

        if (value == null) {
            editor.remove(key);
        } else {
            editor.putString(key, value);
        }
    }
}
