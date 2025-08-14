/**
 * Basic usage example for Termux FileWatcher
 * 
 * Demonstrates how to use the FileWatcher library for monitoring
 * file system changes in a Kotlin LSP or standalone Java application.
 * 
 * Compile: javac -cp "path/to/kotlin-lsp/lib/*" basic_usage.java  
 * Run: java -cp ".:path/to/kotlin-lsp/lib/*" BasicUsageExample
 */

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;

public class BasicUsageExample {
    
    public static void main(String[] args) {
        System.out.println("=== Termux FileWatcher Basic Usage ===\n");
        
        if (args.length < 1) {
            System.out.println("Usage: java BasicUsageExample <directory-to-watch>");
            System.out.println("Example: java BasicUsageExample /tmp");
            return;
        }
        
        String watchPath = args[0];
        
        try {
            demonstrateFileWatching(watchPath);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateFileWatching(String watchPath) throws Exception {
        System.out.println("🔍 Starting file monitoring for: " + watchPath);
        
        // Create FileWatcher instance
        FileWatcher watcher = new FileWatcher();
        
        // Add directory to watch
        watcher.watch(watchPath);
        System.out.println("✅ Added watch for directory: " + watchPath);
        
        // Create a test file to trigger events
        Path testFile = Paths.get(watchPath, "filewatcher_test.txt");
        
        // Monitor for events in a separate thread
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        
        executor.scheduleAtFixedRate(() -> {
            try {
                FileWatcher.Event event = watcher.nextEvent();
                if (event != null) {
                    System.out.println("📁 Event: " + event.getKind() + " - " + event.getPath());
                }
            } catch (Exception e) {
                System.err.println("Error checking for events: " + e.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        
        System.out.println("\n⏰ Monitoring events for 10 seconds...");
        System.out.println("💡 Try creating, modifying, or deleting files in: " + watchPath);
        
        // Demonstrate file operations that should trigger events
        Thread.sleep(2000);
        
        // Create file
        System.out.println("\n🔨 Creating test file...");
        Files.write(testFile, "Hello FileWatcher!".getBytes());
        Thread.sleep(1000);
        
        // Modify file  
        System.out.println("✏️ Modifying test file...");
        Files.write(testFile, "\nModified content!".getBytes(), 
                   StandardOpenOption.APPEND);
        Thread.sleep(1000);
        
        // Delete file
        System.out.println("🗑️ Deleting test file...");
        Files.deleteIfExists(testFile);
        Thread.sleep(1000);
        
        // Continue monitoring for user actions
        Thread.sleep(5000);
        
        // Cleanup
        executor.shutdown();
        watcher.stop();
        
        System.out.println("\n✅ Monitoring stopped. FileWatcher cleaned up.");
        System.out.println("\n📊 Summary:");
        System.out.println("  - FileWatcher successfully created and configured");
        System.out.println("  - Directory monitoring active for: " + watchPath);
        System.out.println("  - Events processed in real-time");
        System.out.println("  - Resources properly cleaned up");
    }
}

/**
 * Standalone FileWatcher implementation for testing
 * In production, use the classes from kotlin-lsp JAR
 */
class FileWatcher {
    static {
        // Load native library
        try {
            System.loadLibrary("filewatcher_jni");
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException("Failed to load native library: " + e.getMessage());
        }
    }
    
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
    
    // Native method declarations
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