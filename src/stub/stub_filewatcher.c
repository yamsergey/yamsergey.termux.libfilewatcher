/**
 * @file stub_filewatcher.c
 * @brief Stub implementation of FileWatcher JNI interface
 * 
 * Minimal implementation that satisfies JNI loading requirements but provides
 * no actual file watching functionality. Use when you only need the LSP to
 * start without native library errors.
 * 
 * @author yamsergey
 * @version 1.0.0
 * @date 2025-08-14
 */

#include "filewatcher_jni.h"
#include <stdlib.h>
#include <string.h>

// Create a dummy watcher pointer
JNIEXPORT jlong JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_create(JNIEnv *env, jclass clazz) {
    // Return a dummy pointer (non-zero to indicate success)
    return (jlong)1;
}

// Stub watch method - always returns true
JNIEXPORT jboolean JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_watch(JNIEnv *env, jclass clazz, jlong watcherPtr, jstring path) {
    return JNI_TRUE;
}

// Stub unwatch method - does nothing
JNIEXPORT void JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_unwatch(JNIEnv *env, jclass clazz, jlong watcherPtr, jstring path) {
    // No-op
}

// Stub nextEvent method - returns null (no events)
JNIEXPORT jobject JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_nextEvent(JNIEnv *env, jclass clazz, jlong watcherPtr) {
    // Return null to indicate no events
    return NULL;
}

// Stub close method - does nothing
JNIEXPORT void JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_close(JNIEnv *env, jclass clazz, jlong watcherPtr) {
    // No-op
}

// Stub destroy method - does nothing
JNIEXPORT void JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_destroy(JNIEnv *env, jclass clazz, jlong watcherPtr) {
    // No-op
}

// JNI_OnLoad - called when library is loaded
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    return JNI_VERSION_1_8;
}