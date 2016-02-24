/*
 *      Copyright (C) Jordan Erickson                     - 2014-2016,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2016
 *       on behalf of Jordan Erickson.
 */

/*
 * This file is part of Cool Mic.
 * 
 * Cool Mic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Cool Mic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Cool Mic.  If not, see <http://www.gnu.org/licenses/>.
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

int coolmic_snddev_driver_oss_open(coolmic_snddev_driver_t *dev, const char *driver, void *device, uint_least32_t rate, unsigned int channels, int flags, ssize_t buffer)
{
    int mode;
    int req;

    (void)driver;
    (void)buffer; /* TODO: implement this. */

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
