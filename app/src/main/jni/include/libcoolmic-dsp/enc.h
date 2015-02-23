/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/*
 * This file defines the API for the encoder part of this library.
 *
 * The encoder works by setting it up, attaching a IO handle
 * for the backend and then take the IO handle for the front end
 * and read data off it.
 */

#ifndef __COOLMIC_DSP_ENC_H__
#define __COOLMIC_DSP_ENC_H__

#include <stdint.h>
#include "iohandle.h"

/* forward declare internally used structures */
typedef struct coolmic_enc coolmic_enc_t;

/* Management of the encoder object */
coolmic_enc_t      *coolmic_enc_new(const char *codec, uint_least32_t rate, unsigned int channels);
int                 coolmic_enc_ref(coolmic_enc_t *self);
int                 coolmic_enc_unref(coolmic_enc_t *self);

/* Reset the encoder state */
int                 coolmic_enc_reset(coolmic_enc_t *self);

/* This is to attach the IO Handle of the PCM data stream that is to be passed to the encoder */
int                 coolmic_enc_attach_iohandle(coolmic_enc_t *self, coolmic_iohandle_t *handle);

/* This function is to get the IO Handle to read the encoded data from */
coolmic_iohandle_t *coolmic_enc_get_iohandle(coolmic_enc_t *self);

#endif
