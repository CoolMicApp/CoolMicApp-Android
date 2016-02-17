/* %%%COPYRIGHT%%% */

/* %%%LICENSE%%% */

/* Please see the corresponding header file for details of this API. */

#include <unistd.h>
#include <stdlib.h>
#include <strings.h>
#include <libcoolmic-dsp/snddev.h>

/* default driver */
#ifndef DEFAULT_SNDDRV_DRIVER
# ifdef HAVE_SNDDRV_DRIVER_OPENSL
#  define DEFAULT_SNDDRV_DRIVER COOLMIC_DSP_SNDDEV_DRIVER_OPENSL
# elif defined(HAVE_SNDDRV_DRIVER_OSS)
#  define DEFAULT_SNDDRV_DRIVER COOLMIC_DSP_SNDDEV_DRIVER_OSS
# else
#  define DEFAULT_SNDDRV_DRIVER COOLMIC_DSP_SNDDEV_DRIVER_NULL
# endif
#endif

/* forward decleration of drivers */
int coolmic_snddev_driver_null_open(coolmic_snddev_driver_t *dev, const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags, ssize_t buffer);
#ifdef HAVE_SNDDRV_DRIVER_OSS
int coolmic_snddev_driver_oss_open(coolmic_snddev_driver_t *dev, const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags, ssize_t buffer);
#endif
#ifdef HAVE_SNDDRV_DRIVER_OPENSL
int coolmic_snddev_driver_opensl_open(coolmic_snddev_driver_t *dev, const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags, ssize_t buffer);
#endif

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

coolmic_snddev_t   *coolmic_snddev_new(const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags, ssize_t buffer)
{
    coolmic_snddev_t *ret;
    int (*driver_open)(coolmic_snddev_driver_t*, const char*, void*, uint_least32_t, unsigned int, int, ssize_t) = NULL;

    /* check arguments */
    if (!rate || !channels || !flags)
        return NULL;

    if (!driver)
        driver = DEFAULT_SNDDRV_DRIVER;

    if (strcasecmp(driver, COOLMIC_DSP_SNDDEV_DRIVER_NULL) == 0) {
        driver_open = coolmic_snddev_driver_null_open;
#ifdef HAVE_SNDDRV_DRIVER_OSS
    } else if (strcasecmp(driver, COOLMIC_DSP_SNDDEV_DRIVER_OSS) == 0) {
        driver_open = coolmic_snddev_driver_oss_open;
#endif
#ifdef HAVE_SNDDRV_DRIVER_OPENSL
    } else if (strcasecmp(driver, COOLMIC_DSP_SNDDEV_DRIVER_OPENSL) == 0) {
        driver_open = coolmic_snddev_driver_opensl_open;
#endif
    } else {
        /* unknown driver */
        return NULL;
    }

    ret = calloc(1, sizeof(coolmic_snddev_t));
    if (!ret)
        return NULL;

    if (driver_open(&(ret->driver), driver, device, rate, channels, flags, buffer) != 0) {
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

    if (self->refc != 1) /* 1=reference in self->rx */
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
