/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

#ifndef __COOLMIC_DSP_SHOUT_H__
#define __COOLMIC_DSP_SHOUT_H__

#include "iohandle.h"

typedef struct coolmic_shout coolmic_shout_t;

coolmic_shout_t *coolmic_shout_new(...);
int              coolmic_shout_ref(coolmic_shout_t *self);
int              coolmic_shout_unref(coolmic_shout_t *self);

int              coolmic_shout_attach_iohandle(coolmic_shout_t *self, coolmic_iohandle_t *handle);

int              coolmic_shout_iter(coolmic_shout_t *self);

#endif
