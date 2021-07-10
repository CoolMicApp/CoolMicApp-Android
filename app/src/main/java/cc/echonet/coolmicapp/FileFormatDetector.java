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

package cc.echonet.coolmicapp;

import cc.echonet.coolmicapp.Configuration.Codec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;

public final class FileFormatDetector {
    private static final byte[] MAGIC_OGG = new byte[]{'O', 'g', 'g', 'S', 0};
    private static final byte[] MAGIC_VORBIS = new byte[]{1, 'v', 'o', 'r', 'b', 'i', 's'};
    private static final byte[] MAGIC_OPUS = new byte[]{'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};
    private static final byte MAGIC_AAC_SYNC_ONE = (byte)0xFF;
    private static final byte MAGIC_AAC_SYNC_TWO = (byte)0xF0;
    private static final byte MAGIC_AAC_SYNC_TWO_MASK = (byte)0xF6;
    private static final byte MAGIC_MP3_SYNC_ONE = (byte)0xFF;
    private static final byte[] MAGIC_MP3_SYNC_TWO = new byte[]{(byte) 0xF2, (byte) 0xF3, (byte) 0xF5};
    private static final byte MAGIC_MP3_SYNC_TWO_MASK = (byte)0xF6;

    @Contract(pure = true)
    private static boolean isSubArray(final byte[] raw, final int offset, final byte[] magic) {
        try {
            for (int i = 0; i < magic.length; i++)
                if (raw[offset + i] != magic[i])
                    return false;
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    @Contract(pure = true)
    private static boolean isPartOf(final byte val, final byte[] magic) {
        for (final byte e : magic) {
            if (val == e)
                return true;
        }

        return false;
    }

    @Contract(pure = true)
    private static boolean isOgg(final byte[] raw) {
        return isSubArray(raw, 0, MAGIC_OGG);
    }

    @Contract(pure = true)
    private static int oggOffsetToBody(final byte[] raw) {
        return 27 + (raw[26] & 0xFF);
    }

    @Contract(pure = true)
    private static boolean isVorbis(final byte[] raw, final int bodyOffset) {
        return isSubArray(raw, bodyOffset, MAGIC_VORBIS);
    }

    @Contract(pure = true)
    private static boolean isOpus(final byte[] raw, final int bodyOffset) {
        return isSubArray(raw, bodyOffset, MAGIC_OPUS);
    }

    @Contract(pure = true)
    private static boolean isAAC(final byte[] raw) {
        try {
            if (raw[0] != MAGIC_AAC_SYNC_ONE)
                return false;

            if ((raw[1] & MAGIC_AAC_SYNC_TWO_MASK) != MAGIC_AAC_SYNC_TWO)
                return false;

            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Contract(pure = true)
    private static boolean isMP3(final byte[] raw) {
        try {
            if (raw[0] != MAGIC_MP3_SYNC_ONE)
                return false;

            if (!isPartOf((byte)(raw[1] & MAGIC_MP3_SYNC_TWO_MASK), MAGIC_MP3_SYNC_TWO))
                return false;

            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static @Nullable String detect(@NotNull InputStream inputStream) {
        final byte[] buffer = new byte[64];
        final int have;

        try {
            have = inputStream.read(buffer);
        } catch (IOException e) {
            return null;
        }

        if (have < 1)
            return null;

        if (isOgg(buffer)) {
            try {
                int bodyOffset = oggOffsetToBody(buffer);

                if (isVorbis(buffer, bodyOffset))
                    return Codec.TYPE_VORBIS;

                if (isOpus(buffer, bodyOffset))
                    return Codec.TYPE_OPUS;

            } catch (Throwable ignored) {
            }
            return Codec.TYPE_OGG;
        }

        if (isAAC(buffer))
            return Codec.TYPE_AAC;

        if (isMP3(buffer))
            return Codec.TYPE_MP3;

        return null;
    }
}
