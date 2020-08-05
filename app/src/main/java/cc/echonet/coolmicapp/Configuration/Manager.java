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

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Manager {
    static final String DEFAULT_PROFILE = "default";

    private final @NotNull Context context;
    private final @NotNull GlobalConfiguration globalConfiguration;

    public Manager(@NotNull Context context) {
        this.context = context;
        this.globalConfiguration = new GlobalConfiguration(context);
    }

    public List<String> getProfileNames() {
        // TODO: Implement actual reading of the list...
        ArrayList<String> list = new ArrayList<>();
        list.add(DEFAULT_PROFILE);
        return list;
    }

    public Profile getDefaultProfile() {
        return getProfile(DEFAULT_PROFILE);
    }

    public Profile getProfile(String name) {
        return new Profile(context, name);
    }

    public Profile getCurrentProfile() {
        return getProfile(globalConfiguration.getCurrentProfileName());
    }

    public @NotNull GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }
}
