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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Station extends ProfileBase {
    private static final @NotNull String[] KEY_LIST = new String[]{"name", "genre", "url", "description", "irc"};

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
}
