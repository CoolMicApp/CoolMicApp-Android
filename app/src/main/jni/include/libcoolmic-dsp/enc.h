/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

#ifndef __COOLMIC_DSP_ENC_H__
#define __COOLMIC_DSP_ENC_H__

typedef struct coolmic_enc coolmic_enc_t;

coolmic_enc_t      *coolmic_enc_new(...);
int                 coolmic_enc_ref(coolmic_enc_t *self);
int                 coolmic_enc_unref(coolmic_enc_t *self);

int                 coolmic_enc_attach_iohandle(coolmic_enc_t *self, coolmic_iohandle_t *handle);
coolmic_iohandle_t *coolmic_enc_get_iohandle(coolmic_enc_t *self);

#endif
