#ifndef _INCLUDE_IGLOO_CONFIG_H2
#define _INCLUDE_IGLOO_CONFIG_H2 1
 
/* include/igloo/config.h. Generated automatically at end of configure. */
/* config.h.  Generated from config.h.in by configure.  */
/* config.h.in.  Generated from configure.ac by autoheader.  */

/* Define to 1 if your system has a working `chown' function. */
#ifndef HAVE_CHOWN
#define HAVE_CHOWN 1
#endif

/* Define to 1 if you have the <dlfcn.h> header file. */
#ifndef HAVE_DLFCN_H
#define HAVE_DLFCN_H 1
#endif

/* Define to 1 if you have the `endhostent' function. */
//#ifndef HAVE_ENDHOSTENT
//#define HAVE_ENDHOSTENT 1
//#endif

/* Define to 1 if you have the `fork' function. */
#ifndef HAVE_FORK
#define HAVE_FORK 1
#endif

/* Define to 1 if you have the `ftime' function. */
#ifndef HAVE_FTIME
#define HAVE_FTIME 1
#endif

/* Define to 1 if you have the `getaddrinfo' function. */
#ifndef HAVE_GETADDRINFO
#define HAVE_GETADDRINFO 1
#endif

/* Define to 1 if you have the `gethostname' function. */
#ifndef HAVE_GETHOSTNAME
#define HAVE_GETHOSTNAME 1
#endif

/* Define if you have the getnameinfo function */
#ifndef HAVE_GETNAMEINFO
#define HAVE_GETNAMEINFO 1
#endif

/* Define to 1 if you have the `gettimeofday' function. */
#ifndef HAVE_GETTIMEOFDAY
#define HAVE_GETTIMEOFDAY 1
#endif

/* Define to 1 if you have the <grp.h> header file. */
#ifndef HAVE_GRP_H
#define HAVE_GRP_H 1
#endif

/* Define to 1 if you have the `inet_aton' function. */
#ifndef HAVE_INET_ATON
#define HAVE_INET_ATON 1
#endif

/* Define to 1 if you have the <inttypes.h> header file. */
#ifndef HAVE_INTTYPES_H
#define HAVE_INTTYPES_H 1
#endif

/* Define to 1 if you have the <memory.h> header file. */
#ifndef HAVE_MEMORY_H
#define HAVE_MEMORY_H 1
#endif

/* Define if you have nanosleep */
#ifndef HAVE_NANOSLEEP
#define HAVE_NANOSLEEP 1
#endif

/* Define to 1 if you have the `poll' function. */
#ifndef HAVE_POLL
#define HAVE_POLL 1
#endif

/* Define if you have POSIX threads libraries and header files. */
#ifndef HAVE_PTHREAD
#define HAVE_PTHREAD 1
#endif

/* Define to 1 if you have the `pthread_spin_lock' function. */
//#ifndef HAVE_PTHREAD_SPIN_LOCK
//#define HAVE_PTHREAD_SPIN_LOCK 1
//#endif

/* Define to 1 if you have the <pwd.h> header file. */
#ifndef HAVE_PWD_H
#define HAVE_PWD_H 1
#endif

/* Define if you have the sethostent function */
//#ifndef HAVE_SETHOSTENT
//#define HAVE_SETHOSTENT 1
//#endif

/* Define to 1 if you have the <stdint.h> header file. */
#ifndef HAVE_STDINT_H
#define HAVE_STDINT_H 1
#endif

/* Define to 1 if you have the <stdlib.h> header file. */
#ifndef HAVE_STDLIB_H
#define HAVE_STDLIB_H 1
#endif

/* Define to 1 if you have the <strings.h> header file. */
#ifndef HAVE_STRINGS_H
#define HAVE_STRINGS_H 1
#endif

/* Define to 1 if you have the <string.h> header file. */
#ifndef HAVE_STRING_H
#define HAVE_STRING_H 1
#endif

/* Define to 1 if you have the <sys/select.h> header file. */
#ifndef HAVE_SYS_SELECT_H
#define HAVE_SYS_SELECT_H 1
#endif

/* Define to 1 if you have the <sys/socket.h> header file. */
#ifndef HAVE_SYS_SOCKET_H
#define HAVE_SYS_SOCKET_H 1
#endif

/* Define to 1 if you have the <sys/stat.h> header file. */
#ifndef HAVE_SYS_STAT_H
#define HAVE_SYS_STAT_H 1
#endif

/* Define to 1 if you have the <sys/timeb.h> header file. */
//#ifndef HAVE_SYS_TIMEB_H
//#define HAVE_SYS_TIMEB_H 1
//#endif

/* Define to 1 if you have the <sys/types.h> header file. */
#ifndef HAVE_SYS_TYPES_H
#define HAVE_SYS_TYPES_H 1
#endif

/* Define to 1 if you have the <sys/uio.h> header file. */
#ifndef HAVE_SYS_UIO_H
#define HAVE_SYS_UIO_H 1
#endif

/* Define to 1 if the system has the `transparent_union' type attribute */
#ifndef HAVE_TYPE_ATTRIBUTE_TRANSPARENT_UNION
#define HAVE_TYPE_ATTRIBUTE_TRANSPARENT_UNION 1
#endif

/* Define to 1 if you have the <unistd.h> header file. */
#ifndef HAVE_UNISTD_H
#define HAVE_UNISTD_H 1
#endif

/* Define to 1 if you have the `vfork' function. */
#ifndef HAVE_VFORK
#define HAVE_VFORK 1
#endif

/* Define to 1 if you have the <vfork.h> header file. */
/* #undef HAVE_VFORK_H */

/* Define if you have winsock2.h on MINGW */
/* #undef HAVE_WINSOCK2_H */

/* Define to 1 if `fork' works. */
#ifndef HAVE_WORKING_FORK
#define HAVE_WORKING_FORK 1
#endif

/* Define to 1 if `vfork' works. */
#ifndef HAVE_WORKING_VFORK
#define HAVE_WORKING_VFORK 1
#endif

/* Define to 1 if you have the `writev' function. */
#ifndef HAVE_WRITEV
#define HAVE_WRITEV 1
#endif

/* Define to the sub-directory in which libtool stores uninstalled libraries.
   */
#ifndef LT_OBJDIR
#define LT_OBJDIR ".libs/"
#endif

/* Define to 1 if assertions should be disabled. */
/* #undef NDEBUG */

/* Name of package */
#ifndef PACKAGE
#define PACKAGE "igloo"
#endif

/* Define to the address where bug reports for this package should be sent. */
#ifndef PACKAGE_BUGREPORT
#define PACKAGE_BUGREPORT "icecast@xiph.org"
#endif

/* Define to the full name of this package. */
#ifndef PACKAGE_NAME
#define PACKAGE_NAME "igloo"
#endif

/* Define to the full name and version of this package. */
#ifndef PACKAGE_STRING
#define PACKAGE_STRING "igloo 0.0.1"
#endif

/* Define to the one symbol short name of this package. */
#ifndef PACKAGE_TARNAME
#define PACKAGE_TARNAME "igloo"
#endif

/* Define to the home page for this package. */
#ifndef PACKAGE_URL
#define PACKAGE_URL ""
#endif

/* Define to the version of this package. */
#ifndef PACKAGE_VERSION
#define PACKAGE_VERSION "0.0.1"
#endif

/* Define to necessary symbol if this constant uses a non-standard name on
   your system. */
/* #undef PTHREAD_CREATE_JOINABLE */

/* Define to 1 if you have the ANSI C header files. */
#ifndef STDC_HEADERS
#define STDC_HEADERS 1
#endif

/* Define to 1 if you can safely include both <sys/time.h> and <time.h>. */
#ifndef TIME_WITH_SYS_TIME
#define TIME_WITH_SYS_TIME 1
#endif

/* Version number of package */
#ifndef VERSION
#define VERSION "0.0.1"
#endif

/* Enable large inode numbers on Mac OS X 10.5.  */
#ifndef _DARWIN_USE_64_BIT_INODE
# define _DARWIN_USE_64_BIT_INODE 1
#endif

/* Number of bits in a file offset, on hosts where this is settable. */
/* #undef _FILE_OFFSET_BITS */

/* Define to include GNU extensions to POSIX */
#ifndef _GNU_SOURCE
#define _GNU_SOURCE 1
#endif

/* Define for large files, on AIX-style hosts. */
/* #undef _LARGE_FILES */

/* Define to `int' if <sys/types.h> doesn't define. */
/* #undef gid_t */

/* Define to `long int' if <sys/types.h> does not define. */
/* #undef off_t */

/* Define to `int' if <sys/types.h> does not define. */
/* #undef pid_t */

/* Define to `unsigned int' if <sys/types.h> does not define. */
/* #undef size_t */

/* Define to `int' if <sys/types.h> does not define. */
/* #undef ssize_t */

/* Define to `int' if <sys/types.h> doesn't define. */
/* #undef uid_t */

/* Define as `fork' if `vfork' does not work. */
/* #undef vfork */
 
/* once: _INCLUDE_IGLOO_CONFIG_H */
#endif
