/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

#ifndef __COOLMIC_DSP_IOHANDLE_H__
#define __COOLMIC_DSP_IOHANDLE_H__

#include <unistd.h>

typedef struct coolmic_iohandle coolmic_iohandle_t;

coolmic_iohandle_t *coolmic_iohandle_new(void *userdata, int(*free)(void*), ssize_t(*read)(void*,void*,size_t), int(*eof)(void*));
int                 coolmic_iohandle_ref(coolmic_iohandle_t *self);
int                 coolmic_iohandle_unref(coolmic_iohandle_t *self);

ssize_t             coolmic_iohandle_read(coolmic_iohandle_t *self, void *buffer, size_t len);
int                 coolmic_iohandle_eof(coolmic_iohandle_t *self);

#endif
