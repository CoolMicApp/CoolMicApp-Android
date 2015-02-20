/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/* This is a OSS (Open Sound System) driver.
 * This driver is meant for testing this library on random POSIX
 * systems and to help developer to work with this code.
 */

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/soundcard.h>
#include <sys/ioctl.h>
#include <libcoolmic-dsp/snddev.h>

/* default device */
#define DEFAULT_DEVICE "/dev/audio"

static int __free(coolmic_snddev_driver_t *dev)
{
    return close(dev->userdata_i);
}

static ssize_t __read(coolmic_snddev_driver_t *dev, void *buffer, size_t len)
{
    return read(dev->userdata_i, buffer, len);
}

static ssize_t __write(coolmic_snddev_driver_t *dev, const void *buffer, size_t len)
{
    return write(dev->userdata_i, buffer, len);
}

int coolmic_snddev_driver_oss_open(coolmic_snddev_driver_t *dev, const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags)
{
    int mode;
    int req;

    (void)driver;

    if (!device)
        device = DEFAULT_DEVICE;

    switch (flags & COOLMIC_DSP_SNDDEV_RXTX) {
        case COOLMIC_DSP_SNDDEV_RX:
            mode = O_RDONLY;
        break;
        case COOLMIC_DSP_SNDDEV_TX:
            mode = O_WRONLY;
        break;
        case COOLMIC_DSP_SNDDEV_RXTX:
            mode = O_RDWR;
        break;
        default:
            return -1;
        break;
    }

    do {
        dev->userdata_i = open(device, mode, 0);
        if (dev->userdata_i == -1)
            break;

        req = channels;
        if (ioctl(dev->userdata_i, SNDCTL_DSP_CHANNELS, &req) != 0)
            break;
        if (req != (int)channels)
            break;

        req = AFMT_S16_LE;
        if (ioctl(dev->userdata_i, SNDCTL_DSP_SETFMT, &req) != 0)
            break;
        if (req != AFMT_S16_LE)
            break;


        req = rate;
        if (ioctl(dev->userdata_i, SNDCTL_DSP_SPEED, &req) != 0)
            break;
        if (req != (int)rate)
            break;

        dev->free = __free;
        dev->read = __read;
        dev->write = __write;

        return 0;
    } while (0);

    if (dev->userdata_i != -1)
        __free(dev);
    return -1;
}
