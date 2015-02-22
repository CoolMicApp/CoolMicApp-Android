## Process this with automake to create Makefile.in

AUTOMAKE_OPTIONS = foreign 1.6

if HAVE_THREAD
  MAYBE_THREAD = common/thread
  MAYBE_THREAD_LIB = common/thread/libicethread.la
endif

if HAVE_THEORA
  MAYBE_THEORA = theora.c
endif

if HAVE_SPEEX
  MAYBE_SPEEX = speex.c
endif

if HAVE_TLS
  MAYBE_TLS = tls.c
endif

SUBDIRS = common/avl common/net common/timing common/httpp $(MAYBE_THREAD)

lib_LTLIBRARIES = libshout.la
libshout_la_LDFLAGS = -version-info 5:0:2

EXTRA_DIST = theora.c speex.c tls.c
noinst_HEADERS = shout_ogg.h shout_private.h util.h
libshout_la_SOURCES = shout.c util.c ogg.c vorbis.c mp3.c webm.c opus.c $(MAYBE_THEORA) $(MAYBE_SPEEX) $(MAYBE_TLS)
AM_CFLAGS = @XIPH_CFLAGS@

libshout_la_LIBADD = common/net/libicenet.la common/timing/libicetiming.la common/avl/libiceavl.la\
		common/httpp/libicehttpp.la $(MAYBE_THREAD_LIB) $(THEORA_LIBS) $(VORBIS_LIBS) $(SPEEX_LIBS) @XIPH_LIBS@

INCLUDES = -I$(top_builddir)/include -I./common/

debug:
	$(MAKE) all CFLAGS="@DEBUG@"

profile:
	$(MAKE) all CFLAGS="@PROFILE@"
