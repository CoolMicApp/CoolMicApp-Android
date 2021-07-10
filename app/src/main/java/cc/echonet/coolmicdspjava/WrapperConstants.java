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

/**
 * Created by stephanj on 17.02.2018.
 */

public class WrapperConstants {
    public enum WrapperInitializationStatus {WRAPPER_UNINITIALIZED, WRAPPER_INITIALIZATION_ERROR, WRAPPER_INTITIALIZED}

    public enum WrapperCallbackEvents {THREAD_POST_START, THREAD_PRE_STOP, THREAD_POST_STOP, THREAD_STOP, ERROR, STREAMSTATE, RECONNECT, SEGMENT_CONNECT, SEGMENT_DISCONNECT}
}
