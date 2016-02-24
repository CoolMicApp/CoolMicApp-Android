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

/* Please see the corresponding header file for details of this API. */

#include <stdlib.h>
#include <shout/shout.h>
#include <libcoolmic-dsp/shout.h>

struct coolmic_shout {
    size_t refc;
    shout_t *shout;
    coolmic_iohandle_t *in;
};

coolmic_shout_t *coolmic_shout_new(void)
{
    coolmic_shout_t *ret = calloc(1, sizeof(coolmic_shout_t));
    if (!ret)
        return NULL;

    shout_init();

    ret->refc = 1;
    ret->shout = shout_new();
    if (!ret->shout) {
        free(ret);
        shout_shutdown();
        return NULL;
    }

    /* set some stuff that is always the same for all connections */
    shout_set_protocol(ret->shout, SHOUT_PROTOCOL_HTTP);
    shout_set_format(ret->shout, SHOUT_FORMAT_OGG);

    return ret;
}

int              coolmic_shout_set_config(coolmic_shout_t *self, const coolmic_shout_config_t *conf)
{
    if (!self || !conf)
        return -1;

    if (shout_set_host(self->shout, conf->hostname) != SHOUTERR_SUCCESS)
        return -1;

    if (shout_set_port(self->shout, conf->port) != SHOUTERR_SUCCESS)
        return -1;

    if (shout_set_tls(self->shout, conf->tlsmode) != SHOUTERR_SUCCESS)
        return -1;

    if (conf->cadir)
        if (shout_set_ca_directory(self->shout, conf->cadir) != SHOUTERR_SUCCESS)
            return -1;

    if (conf->cafile)
        if (shout_set_ca_file(self->shout, conf->cafile) != SHOUTERR_SUCCESS)
            return -1;

    if (shout_set_mount(self->shout, conf->mount) != SHOUTERR_SUCCESS)
        return -1;

    if (conf->username)
        if (shout_set_user(self->shout, conf->username) != SHOUTERR_SUCCESS)
            return -1;

    if (shout_set_password(self->shout, conf->password) != SHOUTERR_SUCCESS)
        return -1;

    if (conf->client_cert)
        if (shout_set_client_certificate(self->shout, conf->client_cert) != SHOUTERR_SUCCESS)
            return -1;

    return 0;
}

int              coolmic_shout_ref(coolmic_shout_t *self)
{
    if (!self)
        return -1;
    self->refc++;
    return 0;
}

int              coolmic_shout_unref(coolmic_shout_t *self)
{
    if (!self)
        return -1;
    self->refc--;

    if (self->refc)
        return 0;

    shout_close(self->shout);
    shout_free(self->shout);
    coolmic_iohandle_unref(self->in);

    free(self);

    shout_shutdown();

    return 0;
}

int              coolmic_shout_attach_iohandle(coolmic_shout_t *self, coolmic_iohandle_t *handle)
{
    if (!self)
        return -1;
    if (self->in)
        coolmic_iohandle_unref(self->in);
    /* ignore errors here as handle is allowed to be NULL */
    coolmic_iohandle_ref(self->in = handle);
    return 0;
}

int              coolmic_shout_start(coolmic_shout_t *self)
{
    if (!self)
        return -1;

    if (shout_get_connected(self->shout) == SHOUTERR_CONNECTED)
        return 0;

    if (shout_open(self->shout) != SHOUTERR_SUCCESS)
        return -1;

    return 0;
}
int              coolmic_shout_stop(coolmic_shout_t *self)
{
    if (!self)
        return -1;

    if (shout_get_connected(self->shout) == SHOUTERR_UNCONNECTED)
        return 0;

    if (shout_close(self->shout) != SHOUTERR_SUCCESS)
        return -1;

    return 0;
}

int              coolmic_shout_iter(coolmic_shout_t *self)
{
    char buffer[1024];
    ssize_t ret;

    if (!self)
        return -1;

    if (shout_get_connected(self->shout) == SHOUTERR_UNCONNECTED)
        return -1;

    ret = coolmic_iohandle_read(self->in, buffer, sizeof(buffer));
    if (ret > 0)
        shout_send(self->shout, (void*)buffer, ret);

    shout_sync(self->shout);

    return 0;
}
