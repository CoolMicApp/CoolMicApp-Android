/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/*
 * This file defines the basic IO Handle API as used by libcoolmic-dsp.
 * A handle is a generic object with some kind of backend connected on
 * creation and offering a frontend (the handle) that is to be passed
 * to a part that needs to read data.
 */

#ifndef __COOLMIC_DSP_IOHANDLE_H__
#define __COOLMIC_DSP_IOHANDLE_H__

#include <unistd.h>

/* forward declare internally used structures */
typedef struct coolmic_iohandle coolmic_iohandle_t;

/* Management of the IO Handle object */
/* The constructor takes the following arguments:
 *
 * The userdata is an object passed to the callbacks to store private data.
 * The free function pointer is called when this object is destroyed and should
 * be used to clean up the backend state.
 * The read function pointer is used to call the backend read function.
 * This function should return -1 in case of error, 0 in case no data can be read
 * by now and a positive number up to the size argument that tells how many data
 * was read into the buffer passed to it.
 * The eof function pointer is called to tell if the reader (caller) reached EOF.
 * This function pointer may be NULL to signal endless streams.
 * This function should return -1 for error, 1 when EOF has been reached and 0 otherwise.
 */
coolmic_iohandle_t *coolmic_iohandle_new(void *userdata, int(*free)(void*), ssize_t(*read)(void*,void*,size_t), int(*eof)(void*));
int                 coolmic_iohandle_ref(coolmic_iohandle_t *self);
int                 coolmic_iohandle_unref(coolmic_iohandle_t *self);

/* This function is to read data from the IO Handle.
 * Short reads can occur while reading data.
 * If zero is returned this does not always mean that EOF was reached. See below.
 */
ssize_t             coolmic_iohandle_read(coolmic_iohandle_t *self, void *buffer, size_t len);

/* This function is to test if we hit EOF while reading.
 * This is to test for EOF as the read function may return zero in some cases
 * such as non-blocking operation that is not to signal EOF.
 */
int                 coolmic_iohandle_eof(coolmic_iohandle_t *self);

#endif
