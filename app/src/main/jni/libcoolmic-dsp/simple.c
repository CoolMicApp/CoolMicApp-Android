/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/* Please see the corresponding header file for details of this API. */

#include <unistd.h>
#include <stdlib.h>
#include <pthread.h>
#include <libcoolmic-dsp/simple.h>
#include <libcoolmic-dsp/iohandle.h>
#include <libcoolmic-dsp/snddev.h>
#include <libcoolmic-dsp/enc.h>
#include <libcoolmic-dsp/shout.h>

struct coolmic_simple {
    size_t refc;
    pthread_mutex_t lock;
    pthread_t thread;
    int running;
    int need_reset;

    coolmic_snddev_t *dev;
    coolmic_enc_t *enc;
    coolmic_shout_t *shout;
    coolmic_iohandle_t *pcm;
    coolmic_iohandle_t *ogg;
};

static void __stop_unlocked(coolmic_simple_t *self)
{
    if (!self->running)
        return;
    self->running = 2;
    pthread_mutex_unlock(&(self->lock));
    pthread_join(self->thread, NULL);
    pthread_mutex_lock(&(self->lock));
}

coolmic_simple_t   *coolmic_simple_new(const char *codec, uint_least32_t rate, unsigned int channels, const coolmic_shout_config_t *conf)
{
    coolmic_simple_t *ret = calloc(1, sizeof(coolmic_simple_t));
    if (!ret)
        return NULL;

    ret->refc = 1;
    pthread_mutex_init(&(ret->lock), NULL);

    do {
        if ((ret->dev = coolmic_snddev_new(COOLMIC_DSP_SNDDEV_DRIVER_AUTO, NULL, rate, channels, COOLMIC_DSP_SNDDEV_RX)) == NULL)
            break;
        if ((ret->enc = coolmic_enc_new(codec, rate, channels)) == NULL)
            break;
        if ((ret->shout = coolmic_shout_new()) == NULL)
            break;
        if ((ret->pcm = coolmic_snddev_get_iohandle(ret->dev)) == NULL)
            break;
        if ((ret->ogg = coolmic_enc_get_iohandle(ret->enc)) == NULL)
            break;
        if (coolmic_enc_attach_iohandle(ret->enc, ret->pcm) != 0)
            break;
        if (coolmic_shout_attach_iohandle(ret->shout, ret->ogg) != 0)
            break;
        if (coolmic_shout_set_config(ret->shout, conf) != 0)
            break;
        return ret;
    } while (0);

    coolmic_simple_unref(ret);
    return NULL;
}

int                 coolmic_simple_ref(coolmic_simple_t *self)
{
    if (!self)
        return -1;
    pthread_mutex_lock(&(self->lock));
    self->refc++;
    pthread_mutex_unlock(&(self->lock));
    return 0;
}

int                 coolmic_simple_unref(coolmic_simple_t *self)
{
    if (!self)
        return -1;

    pthread_mutex_lock(&(self->lock));
    self->refc--;

    if (self->refc) {
        pthread_mutex_unlock(&(self->lock));
        return 0;
    }

    __stop_unlocked(self);

    coolmic_iohandle_unref(self->pcm);
    coolmic_iohandle_unref(self->ogg);
    coolmic_shout_unref(self->shout);
    coolmic_enc_unref(self->enc);
    coolmic_snddev_unref(self->dev);

    pthread_mutex_unlock(&(self->lock));
    pthread_mutex_destroy(&(self->lock));
    free(self);

    return 0;
}

/* reset internal objects */
static inline int __reset(coolmic_simple_t *self)
{
    coolmic_enc_reset(self->enc);
    self->need_reset = 0;
    return 0;
}

/* worker */
static void *__worker(void *userdata)
{
    coolmic_simple_t *self = userdata;
    int running;
    coolmic_shout_t *shout;

    pthread_mutex_lock(&(self->lock));
    if (self->need_reset) {
        if (__reset(self) != 0) {
            self->running = 0;
            pthread_mutex_unlock(&(self->lock));
            return NULL;
        }
    }

    running = self->running;
    coolmic_shout_ref(shout = self->shout);
    coolmic_shout_start(shout);
    pthread_mutex_unlock(&(self->lock));

    while (running == 1) {
        if (coolmic_shout_iter(shout) != 0)
            break;

        pthread_mutex_lock(&(self->lock));
        if (self->need_reset)
            if (__reset(self) != 0)
                self->running = 0;
        running = self->running;
        pthread_mutex_unlock(&(self->lock));
    }

    pthread_mutex_lock(&(self->lock));
    self->running = 0;
    self->need_reset = 1;
    coolmic_shout_stop(shout);
    coolmic_shout_unref(shout);
    pthread_mutex_unlock(&(self->lock));
    return NULL;
}

/* thread control functions */
int                 coolmic_simple_start(coolmic_simple_t *self)
{
    int running;

    if (!self)
        return -1;
    pthread_mutex_lock(&(self->lock));
    if (!self->running)
        if (pthread_create(&(self->thread), NULL, __worker, self) == 0)
            self->running = 1;
    running = self->running;
    pthread_mutex_unlock(&(self->lock));
    return running ? 0 : -1;
}

int                 coolmic_simple_stop(coolmic_simple_t *self)
{
    if (!self)
        return -1;
    pthread_mutex_lock(&(self->lock));
    if (self->running)
        __stop_unlocked(self);
    pthread_mutex_unlock(&(self->lock));
    return 0;
}
