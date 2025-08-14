/**
 * @file jni_helpers.c
 * @brief Common JNI helper functions
 * 
 * Shared JNI utilities used by both stub and real implementations.
 * Provides error handling, logging, and common operations.
 */

#include "filewatcher_jni.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>

/**
 * @brief Debug logging flag (set via FILEWATCHER_DEBUG env var)
 */
static int debug_enabled = -1;

/**
 * @brief Check if debug logging is enabled
 * @return 1 if enabled, 0 if disabled
 */
int is_debug_enabled(void) {
    if (debug_enabled == -1) {
        const char *debug_env = getenv("FILEWATCHER_DEBUG");
        debug_enabled = (debug_env != NULL && *debug_env != '0') ? 1 : 0;
    }
    return debug_enabled;
}

/**
 * @brief Log debug message
 * @param format Printf-style format string
 * @param ... Arguments for format string
 */
void debug_log(const char *format, ...) {
    if (!is_debug_enabled()) return;
    
    va_list args;
    va_start(args, format);
    fprintf(stderr, "[FileWatcher DEBUG] ");
    vfprintf(stderr, format, args);
    fprintf(stderr, "\n");
    va_end(args);
}

/**
 * @brief Log error message
 * @param format Printf-style format string
 * @param ... Arguments for format string
 */
void error_log(const char *format, ...) {
    va_list args;
    va_start(args, format);
    fprintf(stderr, "[FileWatcher ERROR] ");
    vfprintf(stderr, format, args);
    fprintf(stderr, "\n");
    va_end(args);
}

/**
 * @brief Check for JNI exceptions and clear them
 * @param env JNI environment pointer
 * @param context Context string for error message
 * @return 1 if exception occurred, 0 if none
 */
int check_jni_exception(JNIEnv *env, const char *context) {
    if ((*env)->ExceptionCheck(env)) {
        error_log("JNI exception in %s", context);
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
        return 1;
    }
    return 0;
}