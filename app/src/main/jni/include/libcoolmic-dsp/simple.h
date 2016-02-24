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

/*
 * This provides a very simple interface for the encoder framework.
 */

#ifndef __COOLMIC_DSP_SIMPLE_H__
#define __COOLMIC_DSP_SIMPLE_H__

#include <stdint.h>
#include "shout.h"

/* forward declare internally used structures */
typedef struct coolmic_simple coolmic_simple_t;
typedef enum coolmic_simple_event {
 /* some invalid event
  * arg0 and arg1 are undefined.
  */
 COOLMIC_SIMPLE_EVENT_INVALID           = -1,
 /* no event happend.
  * arg0 and arg1 are undefined.
  */
 COOLMIC_SIMPLE_EVENT_NONE              =  0,
 /* an error happend.
  * arg0 points to an const int containing the error value or NULL.
  * YOU MUST NOT ALTER THIS VALUE.
  * arg0 and arg1 are undefined.
  */
 COOLMIC_SIMPLE_EVENT_ERROR             =  1,
 /* a thread got started.
  * arg1 is undefined.
  */
 COOLMIC_SIMPLE_EVENT_THREAD_START      =  2,
 /* as COOLMIC_SIMPLE_EVENT_THREAD_START but ran inside the thread. */
 COOLMIC_SIMPLE_EVENT_THREAD_POST_START =  3,
 /* a thread is stopped.
  * arg0 and arg1 are undefined.
  */
 COOLMIC_SIMPLE_EVENT_THREAD_STOP       =  4,
 /* as COOLMIC_SIMPLE_EVENT_THREAD_STOP but ran inside the thread. */
 COOLMIC_SIMPLE_EVENT_THREAD_PRE_STOP   =  5
} coolmic_simple_event_t;

/* Generic callback for events.
 * inst is the coolmic_simple_t instance calling.
 * userdata is the userdata pointer as passed on setting the callback function.
 * event is the event code as defined in coolmic_simple_event_t.
 * thread is set to a unique pointer to some OS specific structure.
 * This pointer can be used to identify the thread.
 * arg0's and arg1's usage is up to the specific event.
 * The callback is expected to return 0 in case of success.
 * The callback is expected to never fail.
 */
typedef int (*coolmic_simple_callback_t)(coolmic_simple_t *inst, void *userdata, coolmic_simple_event_t event, void *thread, void *arg0, void *arg1);

/* Management of the encoder object */
coolmic_simple_t   *coolmic_simple_new(const char *codec, uint_least32_t rate, unsigned int channels, ssize_t buffer, const coolmic_shout_config_t *conf);
int                 coolmic_simple_ref(coolmic_simple_t *self);
int                 coolmic_simple_unref(coolmic_simple_t *self);

/* thread control functions */
int                 coolmic_simple_start(coolmic_simple_t *self);
int                 coolmic_simple_stop(coolmic_simple_t *self);

/* status callbacks */
int                 coolmic_simple_set_callback(coolmic_simple_t *self, coolmic_simple_callback_t callback, void *userdata);

#endif
