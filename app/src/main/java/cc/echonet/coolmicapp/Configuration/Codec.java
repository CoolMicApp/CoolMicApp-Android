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

import cc.echonet.coolmicapp.R;

@SuppressWarnings("HardcodedFileSeparator")
public class Codec extends ProfileBase {
    public static final @NotNull String TYPE_OGG = "application/ogg";
    public static final @NotNull String TYPE_OPUS = "audio/ogg; codec=opus";
    public static final @NotNull String TYPE_VORBIS = "audio/ogg; codec=vorbis";
    public static final @NotNull String TYPE_AAC = "audio/aac";
    public static final @NotNull String TYPE_MP3 = "audio/mpeg";

    private final @NotNull Audio audio;

    Codec(@NotNull ProfileBase profile, @NotNull Audio audio) {
        super(profile);
        this.audio = audio;
    }

    public @NotNull Audio getAudio() {
        return audio;
    }

    public String getType() {
        return getString("audio_codec", R.string.pref_default_audio_codec);
    }

    public void setType(String type) {
        editor.putString("audio_codec", type);

        if (type.equals(TYPE_OPUS)) {
            /* Opus only supports 48kHz */
            audio.setSampleRate(48000);
        }
    }

    public double getQuality() {
        return Double.parseDouble(getString("audio_quality", R.string.pref_default_audio_quality));
    }

    public static boolean isOgg(@Nullable String type) {
        if (type == null)
            return false;

        switch (type) {
            case TYPE_OGG:
            case TYPE_VORBIS:
            case TYPE_OPUS:
                return true;
        }

        return false;
    }
}
