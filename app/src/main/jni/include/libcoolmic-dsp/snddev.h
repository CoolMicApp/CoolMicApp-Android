/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
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

/* forward declare internally used structures */
typedef struct coolmic_snddev coolmic_snddev_t;

/* Management of the encoder object */
coolmic_snddev_t   *coolmic_snddev_new(...);
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
int                 coolmic_snddev_iter(coolmic_shout_t *self);

#endif
