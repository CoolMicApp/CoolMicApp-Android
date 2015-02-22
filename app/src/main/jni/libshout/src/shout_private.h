/* -*- c-basic-offset: 8; -*- */
/* shout.h: Private libshout data structures and declarations
 *
 *  Copyright (C) 2002-2004 the Icecast team <team@icecast.org>,
 *  Copyright (C) 2012-2015 Philipp "ph3-der-loewe" Schafft <lion@lion.leolix.org>
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id$
 */

#ifndef __LIBSHOUT_SHOUT_PRIVATE_H__
#define __LIBSHOUT_SHOUT_PRIVATE_H__

#ifdef HAVE_CONFIG_H
# include "config.h"
#endif

#include <shout/shout.h>
#include <common/net/sock.h>
#include <common/timing/timing.h>
#include "util.h"

#include <sys/types.h>
#ifdef HAVE_STDINT_H
#  include <stdint.h>
#elif defined (HAVE_INTTYPES_H)
#  include <inttypes.h>
#endif

#ifdef HAVE_OPENSSL
#include <openssl/ssl.h>
#endif

#define LIBSHOUT_DEFAULT_HOST "localhost"
#define LIBSHOUT_DEFAULT_PORT 8000
#define LIBSHOUT_DEFAULT_FORMAT SHOUT_FORMAT_OGG
#define LIBSHOUT_DEFAULT_PROTOCOL SHOUT_PROTOCOL_HTTP
#define LIBSHOUT_DEFAULT_USER "source"
#define LIBSHOUT_DEFAULT_USERAGENT "libshout/" VERSION
#define LIBSHOUT_DEFAULT_ALLOWED_CIPHERS "ALL"

/* server capabilities.
   0x000000XXUL -> Methods.
   0x0000XX00UL -> HTTP Options
   0x000X0000UL -> TLS Related
   0xX0000000UL -> State related
   0x0XX00000UL -> Reserved
 */
#define LIBSHOUT_CAP_SOURCE      0x00000001UL
#define LIBSHOUT_CAP_PUT         0x00000002UL
#define LIBSHOUT_CAP_GET         0x00000004UL
#define LIBSHOUT_CAP_POST        0x00000008UL
#define LIBSHOUT_CAP_CHUNKED     0x00000100UL
#define LIBSHOUT_CAP_100CONTINUE 0x00000200UL
#define LIBSHOUT_CAP_UPGRADETLS  0x00010000UL
#define LIBSHOUT_CAP_GOTCAPS     0x80000000UL

#define LIBSHOUT_MAX_RETRY       2

#define SHOUT_BUFSIZE 4096

typedef struct _shout_tls shout_tls_t;

typedef struct _shout_buf {
	unsigned char data[SHOUT_BUFSIZE];
	unsigned int len;
	unsigned int pos;

	struct _shout_buf *prev;
	struct _shout_buf *next;
} shout_buf_t;

typedef struct {
	shout_buf_t *head;
	size_t len;
} shout_queue_t;

typedef enum {
	SHOUT_STATE_UNCONNECTED = 0,
	SHOUT_STATE_CONNECT_PENDING,
	SHOUT_STATE_TLS_PENDING,
	SHOUT_STATE_REQ_CREATION,
	SHOUT_STATE_REQ_PENDING,
	SHOUT_STATE_RESP_PENDING,
	SHOUT_STATE_CONNECTED,
	SHOUT_STATE_RECONNECT
} shout_state_e;
	
struct shout {
	/* hostname or IP of icecast server */
	char *host;
	/* port of the icecast server */
	int port;
	/* login password for the server */
	char *password;
	/* server protocol to use */
	unsigned int protocol;
	/* type of data being sent */
	unsigned int format;
	/* audio encoding parameters */
	util_dict *audio_info;

	/* user-agent to use when doing HTTP login */
	char *useragent;
	/* mountpoint for this stream */
	char *mount;
	/* all the meta data about the stream */
        util_dict *meta;
	/* icecast 1.x dumpfile */
	char *dumpfile;
	/* username to use for HTTP auth. */
	char *user;
	/* is this stream private? */
	int public;

        /* TLS options */
#ifdef HAVE_OPENSSL
	int upgrade_to_tls;
        int tls_mode;
        char *ca_directory;
        char *ca_file;
        char *allowed_ciphers;
        char *client_certificate;
	shout_tls_t *tls;
#endif

        /* server capabilities (LIBSHOUT_CAP_*) */
        uint32_t server_caps;

        /* Should we retry on error? */
        int retry;

	/* socket the connection is on */
	sock_t socket;
	shout_state_e state;
	int nonblocking;

	void *format_data;
	int (*send)(shout_t* self, const unsigned char* buff, size_t len);
	void (*close)(shout_t* self);

	shout_queue_t rqueue;
	shout_queue_t wqueue;

	/* start of this period's timeclock */
	uint64_t starttime;
	/* amout of data we've sent (in milliseconds) */
	uint64_t senttime;

	int error;
};

int shout_open_ogg(shout_t *self);
int shout_open_mp3(shout_t *self);
int shout_open_webm(shout_t *self);

#ifdef HAVE_OPENSSL
shout_tls_t *shout_tls_new(shout_t *self, sock_t socket);
int shout_tls_try_connect(shout_tls_t *tls);
int shout_tls_close(shout_tls_t *tls);
ssize_t shout_tls_read(shout_tls_t *tls, void *buf, size_t len);
ssize_t shout_tls_write(shout_tls_t *tls, const void *buf, size_t len);
int shout_tls_recoverable(shout_tls_t *tls);
#endif

#endif /* __LIBSHOUT_SHOUT_PRIVATE_H__ */
