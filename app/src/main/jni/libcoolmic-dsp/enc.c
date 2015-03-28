/*
 *  Copyright (C) 2015      Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 */

/* Please see the corresponding header file for details of this API. */

#include <strings.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <libcoolmic-dsp/coolmic-dsp.h>
#include <libcoolmic-dsp/enc.h>
#include <vorbis/vorbisenc.h>

typedef enum coolmic_enc_state {
    STATE_NEED_INIT = 0,
    STATE_RUNNING,
    STATE_EOF,
    STATE_NEED_RESET
} coolmic_enc_state_t;

struct coolmic_enc {
    size_t refc;

    /* overall state */
    coolmic_enc_state_t state;

    /* Audio */
    uint_least32_t rate;
    unsigned int channels;

    /* IO Handles */
    coolmic_iohandle_t *in;
    coolmic_iohandle_t *out;

    /* Ogg: */
    ogg_stream_state os; /* take physical pages, weld into a logical
                            stream of packets */
    ogg_page         og; /* one Ogg bitstream page.  Vorbis packets are inside */
    ogg_packet       op; /* one raw packet of data for decode */

    ssize_t offset_in_page;

    /* Vorbis: */
    vorbis_info      vi; /* struct that stores all the static vorbis bitstream
                            settings */
    vorbis_comment   vc; /* struct that stores all the user comments */

    vorbis_dsp_state vd; /* central working state for the packet->PCM decoder */
    vorbis_block     vb; /* local working space for packet->PCM decode */
};

static int __vorbis_start_encoder(coolmic_enc_t *self)
{
    ogg_packet header;
    ogg_packet header_comm;
    ogg_packet header_code;

    if (self->state != STATE_NEED_INIT)
        return -1;

    vorbis_info_init(&(self->vi));
    if (vorbis_encode_init_vbr(&(self->vi), self->channels, self->rate, 0.1) != 0)
        return -1;

    vorbis_comment_init(&(self->vc));
    vorbis_comment_add_tag(&(self->vc), "ENCODER", "libcoolmic-dsp");

    vorbis_analysis_init(&(self->vd), &(self->vi));
    vorbis_block_init(&(self->vd), &(self->vb));

    srand48(time(NULL)); /* TODO FIXME: move this out */
    ogg_stream_init(&(self->os), lrand48());

    vorbis_analysis_headerout(&(self->vd), &(self->vc), &header, &header_comm, &header_code);
    ogg_stream_packetin(&(self->os), &header); /* automatically placed in its own page */
    ogg_stream_packetin(&(self->os), &header_comm);
    ogg_stream_packetin(&(self->os), &header_code);

    self->state = STATE_RUNNING;

    return 0;
}

static int __vorbis_stop_encoder(coolmic_enc_t *self)
{
    ogg_stream_clear(&(self->os));
    vorbis_block_clear(&(self->vb));
    vorbis_dsp_clear(&(self->vd));
    vorbis_comment_clear(&(self->vc));
    vorbis_info_clear(&(self->vi));

    memset(&(self->os), 0, sizeof(self->os));
    memset(&(self->vb), 0, sizeof(self->vb));
    memset(&(self->vd), 0, sizeof(self->vd));
    memset(&(self->vc), 0, sizeof(self->vc));
    memset(&(self->vi), 0, sizeof(self->vi));
    self->state = STATE_NEED_INIT;
    return 0;
}

static int __vorbis_read_data(coolmic_enc_t *self)
{
    char buffer[1024];
    ssize_t ret;
    float **vbuffer;
    const int16_t *in = (int16_t*)buffer;
    unsigned int c;
    size_t i = 0;

    if (self->state == STATE_EOF || self->state == STATE_NEED_RESET) {
        vorbis_analysis_wrote(&(self->vd), 0);
        return 0;
    }

    ret = coolmic_iohandle_read(self->in, buffer, sizeof(buffer));

    if (ret < 1) {
        if (coolmic_iohandle_eof(self->in) == 1) {
            vorbis_analysis_wrote(&(self->vd), 0);
            self->state = STATE_EOF;
        }
        return -1;
    }

    /* Have we got a strange nummber of bytes? */
    if (ret % (2 * self->channels)) {
        self->offset_in_page = -1;
        return -1;
    }

    vbuffer = vorbis_analysis_buffer(&(self->vd), ret / (2 * self->channels));

    while (ret) {
        for (c = 0; c < self->channels; c++)
            vbuffer[c][i] = *(in++) / 32768.f;
        i++;
        ret -= 2 * self->channels;
    }

    vorbis_analysis_wrote(&(self->vd), i);

    return 0;
}

static int __vorbis_process_flush(coolmic_enc_t *self)
{
    int ret = 0;

    while (vorbis_bitrate_flushpacket(&(self->vd), &(self->op))){
        ogg_stream_packetin(&(self->os), &(self->op));
        ret = 1;
    }

    return ret;
}

static int __vorbis_process(coolmic_enc_t *self)
{
    if (__vorbis_process_flush(self))
        return 0;

    while (vorbis_analysis_blockout(&(self->vd), &(self->vb)) != 1) {
        if (__vorbis_read_data(self) != 0) {
            return -1;
        }
    }

    vorbis_analysis(&(self->vb), NULL);
    vorbis_bitrate_addblock(&(self->vb));

    __vorbis_process_flush(self);
    return 0;
}

static int __need_new_page(coolmic_enc_t *self)
{
    while (ogg_stream_pageout(&(self->os), &(self->og)) == 0) {
        if (__vorbis_process(self) != 0) {
            self->offset_in_page = -1;
            return -1;
        }
        if (self->state == STATE_NEED_RESET && ogg_page_eos(&(self->og))) {
            __vorbis_stop_encoder(self);
            __vorbis_start_encoder(self);
        }
    }

    self->offset_in_page = 0;
    return 0;
}

static ssize_t __read(void *userdata, void *buffer, size_t len)
{
    coolmic_enc_t *self = userdata;
    size_t offset;
    size_t max_len;

    if (self->offset_in_page == -1)
        return -1;

    if (self->offset_in_page == (self->og.header_len + self->og.body_len))
        if (__need_new_page(self) == -1)
            return -1;

    if (self->offset_in_page < self->og.header_len) {
        max_len = self->og.header_len - self->offset_in_page;
        len = (len > max_len) ? max_len : len;
        memcpy(buffer, self->og.header + self->offset_in_page, len);
        self->offset_in_page += len;
        return len;
    }

    offset = self->offset_in_page - self->og.header_len;
    max_len = self->og.body_len - offset;
    len = (len > max_len) ? max_len : len;
    memcpy(buffer, self->og.body + offset, len);
    self->offset_in_page += len;
    return len;
}

static int __eof(void *userdata)
{
    coolmic_enc_t *self = userdata;

    if (self->offset_in_page == (self->og.header_len + self->og.body_len) && self->state == STATE_EOF)
        return 1;

    return 0;
}

coolmic_enc_t      *coolmic_enc_new(const char *codec, uint_least32_t rate, unsigned int channels)
{
    coolmic_enc_t *ret;

    if (!rate || !channels)
        return NULL;

    /* for now we only support Ogg/Vorbis */
    if (strcasecmp(codec, COOLMIC_DSP_CODEC_VORBIS) != 0)
        return NULL;

    ret = calloc(1, sizeof(coolmic_enc_t));
    if (!ret)
        return NULL;

    ret->refc = 1;
    ret->state = STATE_NEED_INIT;
    ret->rate = rate;
    ret->channels = channels;

    __vorbis_start_encoder(ret);

    coolmic_enc_ref(ret);
    ret->out = coolmic_iohandle_new(ret, (int (*)(void*))coolmic_enc_unref, __read, __eof);

    return ret;
}

int                 coolmic_enc_ref(coolmic_enc_t *self)
{
    if (!self)
        return -1;
    self->refc++;
    return 0;
}

int                 coolmic_enc_unref(coolmic_enc_t *self)
{
    if (!self)
        return -1;
    self->refc--;
    if (self->refc != 1) /* 1 = reference in self->out */
        return 0;

    __vorbis_stop_encoder(self);

    coolmic_iohandle_unref(self->in);
    coolmic_iohandle_unref(self->out);
    free(self);

    return 0;
}

int                 coolmic_enc_reset(coolmic_enc_t *self)
{
    if (!self)
        return -1;
    if (self->state != STATE_RUNNING && self->state != STATE_EOF)
        return -1;

    /* send EOF event */
    self->state = STATE_EOF;

    /* no process to EOS page and then reset the encoder */
    while (__need_new_page(self) == 0)
        if (ogg_page_eos(&(self->og)))
            break;

    self->state = STATE_NEED_RESET;
    return 0;
}

int                 coolmic_enc_attach_iohandle(coolmic_enc_t *self, coolmic_iohandle_t *handle)
{
    if (!self)
        return -1;
    if (self->in)
        coolmic_iohandle_unref(self->in);
    /* ignore errors here as handle is allowed to be NULL */
    coolmic_iohandle_ref(self->in = handle);
    return 0;
}

coolmic_iohandle_t *coolmic_enc_get_iohandle(coolmic_enc_t *self)
{
    if (!self)
        return NULL;
    coolmic_iohandle_ref(self->out);
    return self->out;
}
