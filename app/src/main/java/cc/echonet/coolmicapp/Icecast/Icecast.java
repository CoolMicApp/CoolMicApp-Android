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

package cc.echonet.coolmicapp.Icecast;

import cc.echonet.coolmicapp.Icecast.Request.Stats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class Icecast implements Closeable {
    private final @NotNull String protocol;
    private final @NotNull String host;
    private final int port;
    private String username;
    private String password;

    public Icecast(@NotNull String protocol, @NotNull String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @SuppressWarnings("HardcodedFileSeparator")
    URL getCommandURL(@NotNull Command command, @Nullable String mount) throws MalformedURLException {
        String url;

        // TODO: Improve this mess...
        if (mount == null) {
            url = String.format(Locale.ENGLISH, "%s://%s:%s@%s:%d/admin/%s", protocol, username, password, host, port, command.getEndpoint());
        } else {
            url = String.format(Locale.ENGLISH, "%s://%s:%s@%s:%d/admin/%s?mount=/%s", protocol, username, password, host, port, command.getEndpoint(), mount);
        }

        return new URL(url);
    }

    public Stats getStats(@Nullable String mount) throws IOException {
        return new Stats(getCommandURL(Command.STATS, mount));
    }

    @Override
    public void close() {
        // no-op.
    }
}
