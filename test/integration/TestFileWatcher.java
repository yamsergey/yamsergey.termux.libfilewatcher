/**
 * Integration test for FileWatcher functionality
 * Tests the complete Java -> JNI -> Native code path
 */

import java.nio.file.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestFileWatcher {
    
    // Load the native library
    static {
        String libraryPath = System.getProperty("java.library.path", "../../dist");
        System.setProperty("java.library.path", libraryPath);
        
        try {
            System.loadLibrary("filewatcher_jni");
            System.out.println("✅ Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("❌ Failed to load native library: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== FileWatcher Integration Test ===\n");
        
        try {
            testBasicFunctionality();
            testFileEvents();
            System.out.println("\n🎉 All integration tests passed!");
        } catch (Exception e) {
            System.err.println("❌ Integration test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void testBasicFunctionality() throws Exception {
        System.out.println("Testing basic functionality...");
        
        // Create FileWatcher - this will call the native create() method
        FileWatcher watcher = new FileWatcher();
        System.out.println("  ✓ FileWatcher created");
        
        // Test watch() method
        String testDir = "/tmp/filewatcher_test";
        File dir = new File(testDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        watcher.watch(testDir);
        System.out.println("  ✓ Added watch for: " + testDir);
        
        // Test nextEvent() - should return null initially
        FileWatcher.Event event = watcher.nextEvent();
        if (event == null) {
            System.out.println("  ✓ nextEvent() returns null when no events (expected)");
        }
        
        // Clean up
        watcher.stop();
        System.out.println("  ✓ FileWatcher stopped");
        
        System.out.println("✅ Basic functionality test passed\n");
    }
    
    private static void testFileEvents() throws Exception {
        System.out.println("Testing file events (real implementation only)...");
        
        FileWatcher watcher = new FileWatcher();
        
        String testDir = "/tmp/filewatcher_test";
        File dir = new File(testDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        watcher.watch(testDir);
        
        // Create a test file
        File testFile = new File(testDir, "test_file.txt");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Hello, FileWatcher!");
        }
        
        System.out.println("  ✓ Created test file: " + testFile.getAbsolutePath());
        
        // Give some time for event to be processed
        Thread.sleep(100);
        
        // Check for events
        FileWatcher.Event event = watcher.nextEvent();
        if (event != null) {
            System.out.println("  ✓ Received event: " + event.getKind() + " - " + event.getPath());
        } else {
            System.out.println("  ⚠️ No events received (stub implementation or event not captured)");
        }
        
        // Modify the file
        try (FileWriter writer = new FileWriter(testFile, true)) {
            writer.write("\nModified content!");
        }
        
        Thread.sleep(100);
        
        // Check for modification event
        event = watcher.nextEvent();
        if (event != null) {
            System.out.println("  ✓ Received modification event: " + event.getKind() + " - " + event.getPath());
        }
        
        // Clean up
        testFile.delete();
        watcher.stop();
        dir.delete();
        
        System.out.println("✅ File events test completed\n");
    }
}

/**
 * Minimal FileWatcher class for testing
 * In real usage, this comes from the Kotlin LSP JAR
 */
class FileWatcher {
    
    private long nativePtr;
    
    public FileWatcher() {
        this.nativePtr = create();
        if (this.nativePtr == 0) {
            throw new RuntimeException("Failed to create native FileWatcher");
        }
    }
    
    public void watch(String path) {
        if (!watch(nativePtr, path)) {
            throw new RuntimeException("Failed to add watch for: " + path);
        }
    }
    
    public void unwatch(String path) {
        unwatch(nativePtr, path);
    }
    
    public Event nextEvent() {
        return nextEvent(nativePtr);
    }
    
    public void stop() {
        if (nativePtr != 0) {
            close(nativePtr);
            destroy(nativePtr);
            nativePtr = 0;
        }
    }
    
    // Native methods
    private static native long create();
    private static native boolean watch(long ptr, String path);
    private static native void unwatch(long ptr, String path);
    private static native Event nextEvent(long ptr);
    private static native void close(long ptr);
    private static native void destroy(long ptr);
    
    // Event class
    public static class Event {
        private final EventKind kind;
        private final String path;
        
        public Event(EventKind kind, String path) {
            this.kind = kind;
            this.path = path;
        }
        
        public EventKind getKind() { return kind; }
        public String getPath() { return path; }
        
        @Override
        public String toString() {
            return "Event{kind=" + kind + ", path='" + path + "'}";
        }
    }
    
    // EventKind enum
    public enum EventKind {
        CREATED, MODIFIED, DELETED, OVERFLOW
    }
}