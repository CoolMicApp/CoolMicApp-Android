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

public class Profile extends ProfileBase {
    private static final int DEFAULT_VOLUME = 100;

    public Profile(@NotNull Profile profile) {
        super(profile);
    }

    public Profile(@NotNull Context context, @NotNull String profile) {
        super(context, profile);
    }

    //Stolen from: http://stackoverflow.com/a/23704728 & http://stackoverflow.com/a/18879453 (removed the shortening because it did not work reliably)
    public @NotNull Track getTrack() {
        return new Track(this);
    }

    public @NotNull Station getStation() {
        return new Station(this);
    }

    public @NotNull Server getServer() {
        return new Server(this);
    }

    public @NotNull Audio getAudio() {
        return new Audio(this);
    }

    public @NotNull Codec getCodec() {
        return new Codec(this, getAudio());
    }

    public @NotNull VUMeter getVUMeter() {
        return new VUMeter(this);
    }

    public @NotNull Volume getVolume() {
        return new Volume(this, getAudio());
    }

    public @NotNull DialogState getDialogState(DialogIdentifier dialogIdentifier) {
        return new DialogState(this, dialogIdentifier);
    }
}
