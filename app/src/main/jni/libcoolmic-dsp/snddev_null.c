/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/* Please see the corresponding header file for details of this API. */

#include <string.h>
#include <libcoolmic-dsp/snddev.h>

static ssize_t __read(coolmic_snddev_driver_t *dev, void *buffer, size_t len)
{
    (void)dev;

    memset(buffer, 0, len);
    return len;
}

static ssize_t __write(coolmic_snddev_driver_t *dev, const void *buffer, size_t len)
{
    (void)dev, (void)buffer;
    return len;
}

int coolmic_snddev_driver_null_open(coolmic_snddev_driver_t *dev, const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags)
{
    (void)driver, (void)device, (void)rate, (void)channels, (void)flags;

    dev->read = __read;
    dev->write = __write;

    return 0;
}
