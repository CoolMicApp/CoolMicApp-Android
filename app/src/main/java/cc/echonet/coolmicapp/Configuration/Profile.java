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

public class Profile extends ProfileBase {
    private static final int DEFAULT_VOLUME = 100;

    public Profile(Profile profile) {
        super(profile);
    }

    public Profile(Context context, String profile) {
        super(context, profile);
    }

    //Stolen from: http://stackoverflow.com/a/23704728 & http://stackoverflow.com/a/18879453 (removed the shortening because it did not work reliably)
    public Track getTrack() {
        return new Track(this);
    }

    public Server getServer() {
        return new Server(this);
    }

    public Audio getAudio() {
        return new Audio(this);
    }

    public Codec getCodec() {
        return new Codec(this, getAudio());
    }

    public VUMeter getVUMeter() {
        return new VUMeter(this);
    }

    public Volume getVolume() {
        return new Volume(this, getAudio());
    }
}
