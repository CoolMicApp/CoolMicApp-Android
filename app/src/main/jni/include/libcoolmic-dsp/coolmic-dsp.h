/*
 *      Copyright (C) Jordan Erickson                     - 2014-2016,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2016
 *       on behalf of Jordan Erickson.
 */

/*
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
 */

/*
 * This file is for libcoolmic-dsp overall API constants and calls.
 */

#ifndef __COOLMIC_DSP_COOLMIC_DSP_H__
#define __COOLMIC_DSP_COOLMIC_DSP_H__

/* Codec definitions */
#define COOLMIC_DSP_CODEC_VORBIS "audio/ogg; codec=vorbis" /* Ogg/Vorbis */
#define COOLMIC_DSP_CODEC_OPUS   "audio/ogg; codec=opus"   /* Ogg/Opus */

/* Errors */
#define COOLMIC_ERROR_NONE              (  0) /* No error */
#define COOLMIC_ERROR_GENERIC           ( -1) /* Generic, unknown error */
#define COOLMIC_ERROR_NOSYS             ( -8) /* Function not implemented */
#define COOLMIC_ERROR_FAULT             ( -9) /* Bad address */
#define COOLMIC_ERROR_INVAL             (-10) /* Invalid argument */
#define COOLMIC_ERROR_NOMEM             (-11) /* Not enough space */
#define COOLMIC_ERROR_BUSY              (-12) /* Device or resource busy */
#define COOLMIC_ERROR_PERM              (-13) /* Operation not permitted */
#define COOLMIC_ERROR_CONNREFUSED       (-14) /* Connection refused */
#define COOLMIC_ERROR_CONNECTED         (-15) /* Connected. */
#define COOLMIC_ERROR_UNCONNECTED       (-16) /* Unconnected. */
#define COOLMIC_ERROR_NOTLS             (-17) /* TLS requested but not supported by peer */
#define COOLMIC_ERROR_TLSBADCERT        (-18) /* TLS connection can not be established because of bad certificate */

#endif
