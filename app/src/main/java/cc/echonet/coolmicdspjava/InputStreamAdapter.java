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

package cc.echonet.coolmicdspjava;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamAdapter {
    private InputStream is;
    private boolean isEOF;

    public InputStreamAdapter(InputStream is) {
        this.is = is;
        this.isEOF = false;
    }

    private int callback(String task, byte[] buffer) {
        try {
            switch (task) {
                case "close":
                    is.close();
                    is = null;
                    break;
                case "read":
                    try {
                        int ret = is.read(buffer);

                        if (ret < buffer.length)
                            isEOF = true;

                        return ret;
                    } catch (EOFException e) {
                        isEOF = true;
                        return -1;
                    }
                case "eof":
                    return isEOF ? 1 : 0;
            }
        } catch (IOException e) {
            return -1;
        }

        return -1;
    }
}
