/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/* Please see the corresponding header file for details of this API. */

#include <unistd.h>
#include <stdlib.h>
#include <strings.h>
#include <libcoolmic-dsp/snddev.h>

/* default driver */
#define DEFAULT_DRIVER COOLMIC_DSP_SNDDEV_DRIVER_OSS

/* forward decleration of drivers */
int coolmic_snddev_driver_null_open(coolmic_snddev_driver_t *dev, const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags);
int coolmic_snddev_driver_oss_open(coolmic_snddev_driver_t *dev, const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags);

struct coolmic_snddev {
    /* reference counter */
    size_t refc;
    /* driver */
    coolmic_snddev_driver_t driver;
    /* IO Handles */
    coolmic_iohandle_t *rx; /* Device -data-> Handle */
    coolmic_iohandle_t *tx; /* Handle -data-> Device */
};

static ssize_t __read(void *userdata, void *buffer, size_t len)
{
    coolmic_snddev_t *self = (coolmic_snddev_t*)userdata;
    if (!self->driver.read)
        return -1;
    return self->driver.read(&(self->driver), buffer, len);
}

coolmic_snddev_t   *coolmic_snddev_new(const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags)
{
    coolmic_snddev_t *ret;
    int (*driver_open)(coolmic_snddev_driver_t*, const char*, void*, uint_least32_t, unsigned int, int) = NULL;

    /* check arguments */
    if (!rate || !channels || !flags)
        return NULL;

    if (!driver)
        driver = DEFAULT_DRIVER;

    if (strcasecmp(driver, COOLMIC_DSP_SNDDEV_DRIVER_NULL) == 0) {
        driver_open = coolmic_snddev_driver_null_open;
    } else if (strcasecmp(driver, COOLMIC_DSP_SNDDEV_DRIVER_OSS) == 0) {
        driver_open = coolmic_snddev_driver_oss_open;
    } else {
        /* unknown driver */
        return NULL;
    }

    ret = calloc(1, sizeof(coolmic_snddev_t));
    if (!ret)
        return NULL;

    if (driver_open(&(ret->driver), driver, device, rate, channels, flags) != 0) {
        free(ret);
        return NULL;
    }

    ret->refc = 1;
    if (flags & COOLMIC_DSP_SNDDEV_RX) {
        coolmic_snddev_ref(ret);
        ret->rx = coolmic_iohandle_new(ret, (int (*)(void*))coolmic_snddev_unref, __read, NULL);
    }

    return ret;
}

int                 coolmic_snddev_ref(coolmic_snddev_t *self)
{
    if (!self)
        return -1;
    self->refc++;
    return 0;
}

int                 coolmic_snddev_unref(coolmic_snddev_t *self)
{
    if (!self)
        return -1;
    self->refc--;

    /* FIXME: This does not reflect references hold by self->rx and self->tx */
    if (self->refc)
        return 0;

    coolmic_iohandle_unref(self->rx);
    coolmic_iohandle_unref(self->tx);

    if (self->driver.free)
        self->driver.free(&(self->driver));

    free(self);

    return 0;
}

int                 coolmic_snddev_attach_iohandle(coolmic_snddev_t *self, coolmic_iohandle_t *handle)
{
    if (!self)
        return -1;
    if (self->tx)
        coolmic_iohandle_unref(self->tx);
    /* ignore errors here as handle is allowed to be NULL */
    coolmic_iohandle_ref(self->tx = handle);
    return 0;
}

coolmic_iohandle_t *coolmic_snddev_get_iohandle(coolmic_snddev_t *self)
{
    if (!self)
        return NULL;
    coolmic_iohandle_ref(self->rx);
    return self->rx;
}

int                 coolmic_snddev_iter(coolmic_snddev_t *self)
{
    /* TODO */
    (void)self;
    return -1;
}
