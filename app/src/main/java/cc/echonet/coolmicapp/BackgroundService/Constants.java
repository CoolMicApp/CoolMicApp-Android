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

package cc.echonet.coolmicapp.BackgroundService;

public class Constants {
    /* client-to-server */
    public static final int C2S_MSG_STATE = 1;
    public static final int C2S_MSG_STREAM_ACTION = 2;
    public static final int C2S_MSG_STREAM_RELOAD = 3;
    public static final int C2S_MSG_GAIN = 4;
    public static final int C2S_MSG_STREAM_STOP = 5;
    public static final int C2S_MSG_NEXT_SEGMENT = 6;

    /* server-to-client */
    public static final int S2C_MSG_STATE_REPLY = 52;
    public static final int S2C_MSG_STREAM_STOP_REPLY = 53;
    public static final int S2C_MSG_STREAM_START_REPLY = 54;
    public static final int S2C_MSG_PERMISSIONS_MISSING = 55;
    public static final int S2C_MSG_CONNECTION_UNSET = 56;
    public static final int S2C_MSG_CMTS_TOS = 57;
    public static final int S2C_MSG_VUMETER = 58;
    public static final int S2C_MSG_ERROR = 59;
    public static final int S2C_MSG_GAIN = 60;

    /* ???-to-server */
    public static final int H2S_MSG_TIMER = 100;

    /* any-to-any */
    public static final int A2A_MSG_NONE = 1000;

    public static final int NEXTSEGMENT_REQUEST_CODE = 33;

    public static final int NOTIFICATION_ID_LED = 0;

    public enum CONTROL_UI {
        CONTROL_UI_CONNECTING,
        CONTROL_UI_CONNECTED,
        CONTROL_UI_DISCONNECTED
    }
}
