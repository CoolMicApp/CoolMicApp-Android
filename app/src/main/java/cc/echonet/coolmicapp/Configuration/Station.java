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

import cc.echonet.coolmicapp.Utils;
import org.jetbrains.annotations.*;

import java.util.*;

public class Station extends ProfileBase {
    private static final @NotNull String[] KEY_LIST = new String[]{"name", "genre", "url", "description", "irc"};
    public static final @NotNull @UnmodifiableView List<@NotNull String> STANDARD_KEYS = Collections.unmodifiableList(Arrays.asList(KEY_LIST));

    private static void assertValidKey(@NotNull String key) {
        for (final @NotNull String validKey : KEY_LIST) {
            if (key.equals(validKey)) {
                return;
            }
        }

        throw new IllegalArgumentException("Invalid key: " + key);
    }

    public static @NotNull String getKeyDisplayName(@NotNull String key) {
        return Utils.toUpperFirst(key);
    }

    Station(@NotNull ProfileBase profile) {
        super(profile);
    }

    @Unmodifiable
    public @NotNull Map<@NotNull String, @NotNull String> getMetadata() {
        final @NotNull Map<@NotNull String, @NotNull String> ret = new HashMap<>();

        for (final @NotNull String key : KEY_LIST) {
            final @Nullable String value = getString("station_" + key);
            if (value != null && !value.isEmpty())
                ret.put(key, value);
        }

        return Collections.unmodifiableMap(ret);
    }

    @Contract("_, !null -> !null; _, null -> _")
    public @Nullable String getValue(@NotNull String key, @Nullable String def) {
        assertValidKey(key);
        return getString("station_" + key, def);
    }

    public void setValue(@NotNull String key, @Nullable String value) {
        assertValidKey(key);
        editor.putString("station_" + key, value);
    }
}
