/* %%%COPYRIGHT%%% */

/* %%%LICENSE%%% */

/* Please see the corresponding header file for details of this API. */

#include <stdlib.h>
#include <libcoolmic-dsp/iohandle.h>

struct coolmic_iohandle {
    size_t   refc;
    void    *userdata;
    int     (*free)(void *userdata);
    ssize_t (*read)(void *userdata, void *buffer, size_t len);
    int     (*eof )(void *userdata);
};

coolmic_iohandle_t *coolmic_iohandle_new(void *userdata, int(*free)(void*), ssize_t(*read)(void*,void*,size_t), int(*eof)(void*))
{
    coolmic_iohandle_t *ret;

    /* we should at least have a read function for this to make sense */
    if (!read)
        return NULL;

    ret = calloc(1, sizeof(coolmic_iohandle_t));
    if (!ret)
        return NULL;

    ret->refc = 1;
    ret->userdata = userdata;
    ret->free = free;
    ret->read = read;
    ret->eof = eof;

    return ret;
}

int                 coolmic_iohandle_ref(coolmic_iohandle_t *self)
{
    if (!self)
        return -1;
    self->refc++;
    return 0;
}

int                 coolmic_iohandle_unref(coolmic_iohandle_t *self)
{
    if (!self)
        return -1;
    self->refc--;
    if (self->refc)
        return 0;

    if (self->free) {
        if (self->free(self->userdata) != 0) {
            self->refc++;
            return -1;
        }
    }

    free(self);

    return 0;
}

ssize_t             coolmic_iohandle_read(coolmic_iohandle_t *self, void *buffer, size_t len)
{
    if (!self || !buffer)
        return -1;
    if (!len)
        return 0;
    return self->read(self->userdata, buffer, len);
}

int                 coolmic_iohandle_eof(coolmic_iohandle_t *self)
{
    if (!self)
        return -1;
    if (self->eof)
        return self->eof(self->userdata);
    return 0;
}
