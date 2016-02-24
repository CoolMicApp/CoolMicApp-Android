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
 * This file defines basic interface to access a real (hardware) or
 * virtual (software) sound device.
 * The API is designed so it works with sound devices opned for reading (recording),
 * writing (playback) or both at the same time (full duplex access).
 * To read data from the device use the IO Handle as provided by this API.
 * To write data to the device attach an IO handle and call the iteration function.
 */

#ifndef __COOLMIC_DSP_SNDDEV_H__
#define __COOLMIC_DSP_SNDDEV_H__

#include <stdint.h>
#include "iohandle.h"

/* constants used */
#define COOLMIC_DSP_SNDDEV_DRIVER_AUTO   NULL
#define COOLMIC_DSP_SNDDEV_DRIVER_NULL   "null"
#define COOLMIC_DSP_SNDDEV_DRIVER_OSS    "oss"
#define COOLMIC_DSP_SNDDEV_DRIVER_OPENSL "opensl"

#define COOLMIC_DSP_SNDDEV_RX    0x0001
#define COOLMIC_DSP_SNDDEV_TX    0x0002
#define COOLMIC_DSP_SNDDEV_RXTX  (COOLMIC_DSP_SNDDEV_RX|COOLMIC_DSP_SNDDEV_TX)

/* forward declare internally used structures */
typedef struct coolmic_snddev coolmic_snddev_t;

/* structures exposed to some external components like drivers */
typedef struct coolmic_snddev_driver coolmic_snddev_driver_t;
struct coolmic_snddev_driver {
    /* callbacks */
    /* free device */
    int (*free)(coolmic_snddev_driver_t *dev);
    /* read data off the device (record) */
    ssize_t (*read)(coolmic_snddev_driver_t *dev, void *buffer, size_t len);
    /* write data to the device (playback) */
    ssize_t (*write)(coolmic_snddev_driver_t *dev, const void *buffer, size_t len);

    /* internal storage */
    int userdata_i;
    void *userdata_vp;
};

/* Management of the encoder object */
coolmic_snddev_t   *coolmic_snddev_new(const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags, ssize_t buffer);
int                 coolmic_snddev_ref(coolmic_snddev_t *self);
int                 coolmic_snddev_unref(coolmic_snddev_t *self);

/* This is to attach the IO Handle of the PCM data stream that is to be passed to the sound device */
int                 coolmic_snddev_attach_iohandle(coolmic_snddev_t *self, coolmic_iohandle_t *handle);

/* This function is to get the IO Handle to read data from the sound device */
coolmic_iohandle_t *coolmic_snddev_get_iohandle(coolmic_snddev_t *self);

/* This function is to iterate. It will try to play back the stream
 * as given by the attached IO Handle. If needed it will read more data
 * off that handle.
 */
int                 coolmic_snddev_iter(coolmic_snddev_t *self);

#endif
