/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/*
 * This defines the API used to send a stream (IO Handle) to an Icecast server.
 * This works by setting up the object and then passing an IO Handle to it
 * to read the data from.
 * When the setup stage has completed the iteration function is called within
 * the mainloop or a thread of the application. This will let this API read
 * data of the IO Handle and send it to the Icecast server.
 */

#ifndef __COOLMIC_DSP_SHOUT_H__
#define __COOLMIC_DSP_SHOUT_H__

#include "iohandle.h"

/* forward declare internally used structures */
typedef struct coolmic_shout coolmic_shout_t;

/* Management of the encoder object */
coolmic_shout_t *coolmic_shout_new(...);
int              coolmic_shout_ref(coolmic_shout_t *self);
int              coolmic_shout_unref(coolmic_shout_t *self);

/* This is to attach the IO Handle of the Ogg data stream that is to be passed to the Icecast server */
int              coolmic_shout_attach_iohandle(coolmic_shout_t *self, coolmic_iohandle_t *handle);

/* This function is to iterate. It will check internal state and try to send more data
 * to the Icecast server. If needed it will read more encoded data from the IO Handle that was attached.
 */
int              coolmic_shout_iter(coolmic_shout_t *self);

#endif
