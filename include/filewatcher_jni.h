/**
 * @file filewatcher_jni.h
 * @brief Termux FileWatcher JNI Interface
 * 
 * JNI interface definitions for the Termux-compatible FileWatcher library.
 * Provides both stub and real implementations for file system monitoring.
 * 
 * @author yamsergey
 * @version 1.0.0
 * @date 2025-08-14
 * 
 * @copyright MIT License
 */

#ifndef FILEWATCHER_JNI_H
#define FILEWATCHER_JNI_H

#include <jni.h>
#include <pthread.h>

#ifdef REAL_IMPLEMENTATION
#include <sys/inotify.h>
#endif

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @defgroup JNI_Interface JNI Interface
 * @brief Java Native Interface methods for FileWatcher
 * @{
 */

/**
 * @brief Create a new FileWatcher instance
 * @param env JNI environment pointer
 * @param clazz FileWatcher class
 * @return Watcher handle (pointer cast to jlong), 0 on failure
 */
JNIEXPORT jlong JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_create(JNIEnv *env, jclass clazz);

/**
 * @brief Add a path to watch
 * @param env JNI environment pointer
 * @param clazz FileWatcher class
 * @param watcherPtr Watcher handle from create()
 * @param path Java string containing path to watch
 * @return JNI_TRUE on success, JNI_FALSE on failure
 */
JNIEXPORT jboolean JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_watch(JNIEnv *env, jclass clazz, 
                                                          jlong watcherPtr, jstring path);

/**
 * @brief Remove a path from watching
 * @param env JNI environment pointer
 * @param clazz FileWatcher class
 * @param watcherPtr Watcher handle from create()
 * @param path Java string containing path to unwatch
 */
JNIEXPORT void JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_unwatch(JNIEnv *env, jclass clazz,
                                                            jlong watcherPtr, jstring path);

/**
 * @brief Get next file system event (non-blocking)
 * @param env JNI environment pointer
 * @param clazz FileWatcher class
 * @param watcherPtr Watcher handle from create()
 * @return FileWatcher.Event object or NULL if no events available
 */
JNIEXPORT jobject JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_nextEvent(JNIEnv *env, jclass clazz,
                                                              jlong watcherPtr);

/**
 * @brief Close the watcher (stop monitoring)
 * @param env JNI environment pointer
 * @param clazz FileWatcher class
 * @param watcherPtr Watcher handle from create()
 */
JNIEXPORT void JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_close(JNIEnv *env, jclass clazz,
                                                          jlong watcherPtr);

/**
 * @brief Destroy the watcher instance and free resources
 * @param env JNI environment pointer
 * @param clazz FileWatcher class
 * @param watcherPtr Watcher handle from create()
 */
JNIEXPORT void JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_destroy(JNIEnv *env, jclass clazz,
                                                            jlong watcherPtr);

/** @} */

/**
 * @defgroup JNI_Lifecycle JNI Lifecycle
 * @brief JNI library lifecycle management
 * @{
 */

/**
 * @brief Called when library is loaded
 * @param vm Java Virtual Machine pointer
 * @param reserved Reserved parameter (unused)
 * @return JNI version (JNI_VERSION_1_8)
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved);

/**
 * @brief Called when library is unloaded
 * @param vm Java Virtual Machine pointer
 * @param reserved Reserved parameter (unused)
 */
JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved);

/** @} */

#ifdef REAL_IMPLEMENTATION

/**
 * @defgroup Real_Implementation Real Implementation
 * @brief inotify-based real file watching implementation
 * @{
 */

/** Event buffer size for inotify reads */
#define EVENT_SIZE (sizeof(struct inotify_event))
#define BUF_LEN (1024 * (EVENT_SIZE + 16))

/**
 * @brief FileWatcher instance state
 * 
 * Contains all state needed for a file watcher instance including
 * inotify file descriptor, synchronization, and event buffering.
 */
typedef struct {
    int inotify_fd;           /**< inotify file descriptor */
    pthread_mutex_t mutex;    /**< Thread synchronization mutex */
    char event_buffer[BUF_LEN]; /**< Event buffer for inotify reads */
    int buffer_pos;           /**< Current position in buffer */
    int buffer_len;           /**< Current buffer length */
} FileWatcher;

// Internal functions are declared static in the implementation file

/** @} */

#endif // REAL_IMPLEMENTATION

#ifdef __cplusplus
}
#endif

#endif // FILEWATCHER_JNI_H