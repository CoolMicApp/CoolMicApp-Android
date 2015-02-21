/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/*
 * This provides a very simple interface for the encoder framework.
 */

#ifndef __COOLMIC_DSP_SIMPLE_H__
#define __COOLMIC_DSP_SIMPLE_H__

#include <stdint.h>
#include "shout.h"

/* forward declare internally used structures */
typedef struct coolmic_simple coolmic_simple_t;

/* Management of the encoder object */
coolmic_simple_t   *coolmic_simple_new(const char *codec, uint_least32_t rate, unsigned int channels, const coolmic_shout_config_t *conf);
int                 coolmic_simple_ref(coolmic_simple_t *self);
int                 coolmic_simple_unref(coolmic_simple_t *self);

/* thread control functions */
int                 coolmic_simple_start(coolmic_simple_t *self);
int                 coolmic_simple_stop(coolmic_simple_t *self);

#endif
