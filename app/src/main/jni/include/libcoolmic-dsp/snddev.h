/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

#ifndef __COOLMIC_DSP_SNDDEV_H__
#define __COOLMIC_DSP_SNDDEV_H__

typedef struct coolmic_snddev coolmic_snddev_t;

coolmic_snddev_t   *coolmic_snddev_new(...);
int                 coolmic_snddev_ref(coolmic_snddev_t *self);
int                 coolmic_snddev_unref(coolmic_snddev_t *self);

int                 coolmic_snddev_attach_iohandle(coolmic_snddev_t *self, coolmic_iohandle_t *handle);
coolmic_iohandle_t *coolmic_snddev_get_iohandle(coolmic_snddev_t *self);

int                 coolmic_snddev_iter(coolmic_shout_t *self);

#endif
