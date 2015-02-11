/* util.h: libshout utility/portability functions */

#ifndef __LIBSHOUT_UTIL_H__
#define __LIBSHOUT_UTIL_H__

/* String dictionary type, without support for NULL keys, or multiple
 * instances of the same key */
typedef struct _util_dict {
  char *key;
  char *val;
  struct _util_dict *next;
} util_dict;

char *_shout_util_strdup(const char *s);

util_dict *_shout_util_dict_new(void);
void _shout_util_dict_free(util_dict *dict);
/* dict, key must not be NULL. */
int _shout_util_dict_set(util_dict *dict, const char *key, const char *val);
const char *_shout_util_dict_get(util_dict *dict, const char *key);
char *_shout_util_dict_urlencode(util_dict *dict, char delim);

char *_shout_util_base64_encode(char *data);
char *_shout_util_url_encode(const char *data);
int _shout_util_read_header(int sock, char *buff, unsigned long len);

#endif /* __LIBSHOUT_UTIL_H__ */
