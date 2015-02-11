/* -*- c-basic-offset: 8; -*- */
/* shout.h: Private libshout data structures and declarations
 *
 * $Id: shout_private.h 18174 2012-02-02 00:16:36Z giles $
 */

#ifndef __LIBSHOUT_SHOUT_PRIVATE_H__
#define __LIBSHOUT_SHOUT_PRIVATE_H__

#ifdef HAVE_CONFIG_H
# include "config.h"
#endif

#include <shout/shout.h>
#include <net/sock.h>
#include <timing/timing.h>
#include "util.h"

#include <sys/types.h>
#ifdef HAVE_STDINT_H
#  include <stdint.h>
#elif defined (HAVE_INTTYPES_H)
#  include <inttypes.h>
#endif

#define LIBSHOUT_DEFAULT_HOST "localhost"
#define LIBSHOUT_DEFAULT_PORT 8000
#define LIBSHOUT_DEFAULT_FORMAT SHOUT_FORMAT_OGG
#define LIBSHOUT_DEFAULT_PROTOCOL SHOUT_PROTOCOL_HTTP
#define LIBSHOUT_DEFAULT_USER "source"
#define LIBSHOUT_DEFAULT_USERAGENT "libshout/" VERSION

#define SHOUT_BUFSIZE 4096

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
	SHOUT_STATE_REQ_PENDING,
	SHOUT_STATE_RESP_PENDING,
	SHOUT_STATE_CONNECTED
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
	/* name of the stream */
	char *name;
	/* homepage of the stream */
	char *url;
	/* genre of the stream */
	char *genre;
	/* description of the stream */
	char *description;
	/* icecast 1.x dumpfile */
	char *dumpfile;
	/* username to use for HTTP auth. */
	char *user;
	/* is this stream private? */
	int public;

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

#endif /* __LIBSHOUT_SHOUT_PRIVATE_H__ */
