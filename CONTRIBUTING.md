# Contributing to Termux FileWatcher

Thank you for your interest in contributing! This project helps make Kotlin development on Android/Termux more accessible.

## 🚀 Quick Start

### Development Environment

```bash
# Clone the repository
git clone https://github.com/yamsergey/termux-filewatcher.git
cd termux-filewatcher

# Install dependencies (Termux)
pkg install clang cmake make git openjdk-17

# Build and test
make dev
make test
```

### Project Structure

```
termux-filewatcher/
├── src/
│   ├── common/     # Shared JNI utilities
│   ├── stub/       # Stub implementation
│   └── real/       # Real inotify implementation
├── include/        # Header files
├── test/           # Test suites
│   ├── unit/       # Unit tests (C)
│   ├── integration/# Integration tests (Java)
│   └── manual/     # Manual testing programs
├── examples/       # Usage examples
└── docs/           # Documentation
```

## 🎯 How to Contribute

### Reporting Issues

1. **Search existing issues** to avoid duplicates
2. **Use issue templates** when available
3. **Provide detailed information**:
   - Termux version and device info
   - Steps to reproduce
   - Expected vs actual behavior
   - Relevant logs/error messages

### Submitting Pull Requests

1. **Fork the repository** and create a feature branch
2. **Follow coding standards** (see below)
3. **Add tests** for new functionality
4. **Update documentation** as needed
5. **Test thoroughly** on Termux
6. **Submit PR** with clear description

## 📝 Coding Standards

### C Code Style

Follow Linux kernel coding style:

```c
// Good: Clear function names and documentation
/**
 * @brief Create a new file watcher instance
 * @param env JNI environment pointer
 * @return Watcher handle or 0 on failure
 */
static FileWatcher *create_watcher(JNIEnv *env) {
    FileWatcher *watcher = malloc(sizeof(FileWatcher));
    if (!watcher) {
        error_log("Failed to allocate FileWatcher");
        return NULL;
    }
    
    // Initialize with proper error checking
    watcher->inotify_fd = inotify_init1(IN_NONBLOCK);
    if (watcher->inotify_fd == -1) {
        free(watcher);
        return NULL;
    }
    
    return watcher;
}
```

### Key Principles

- **Memory Safety**: Always check malloc/calloc returns
- **Error Handling**: Check all system calls and JNI operations
- **Documentation**: Document all public APIs with Doxygen
- **Thread Safety**: Protect shared state with mutexes
- **Resource Cleanup**: Pair all allocations with cleanup

### JNI Best Practices

```c
// Good: Proper exception handling
jobject create_java_object(JNIEnv *env, jclass clazz) {
    jobject obj = (*env)->NewObject(env, clazz, constructor);
    if (obj == NULL) {
        if ((*env)->ExceptionCheck(env)) {
            (*env)->ExceptionClear(env);
        }
        error_log("Failed to create Java object");
        return NULL;
    }
    return obj;
}

// Good: Cleanup local references
void process_string(JNIEnv *env, jstring jstr) {
    const char *str = (*env)->GetStringUTFChars(env, jstr, NULL);
    if (str == NULL) return;
    
    // Use string...
    process_path(str);
    
    // Always release
    (*env)->ReleaseStringUTFChars(env, jstr, str);
}
```

## 🧪 Testing

### Test Types

1. **Unit Tests** (`test/unit/`): Test individual functions
2. **Integration Tests** (`test/integration/`): Test JNI interface
3. **Manual Tests** (`test/manual/`): Interactive testing programs
4. **Performance Tests**: Benchmark critical paths

### Writing Tests

```c
// Unit test example
int test_watcher_creation() {
    printf("Testing watcher creation...\n");
    
    // Setup mock JNI environment if needed
    MockJNIEnv env;
    setup_mock_jni(&env);
    
    // Test the function
    jlong handle = Java_com_jetbrains_analyzer_filewatcher_FileWatcher_create(
        &env.jni_env, NULL);
    
    // Verify results
    if (handle == 0) {
        printf("❌ Watcher creation failed\n");
        return 1;
    }
    
    // Cleanup
    Java_com_jetbrains_analyzer_filewatcher_FileWatcher_destroy(
        &env.jni_env, NULL, handle);
    
    printf("✅ Watcher creation test passed\n");
    return 0;
}
```

### Running Tests

```bash
# All tests
make test

# Specific test categories
make test-unit
make test-integration
make test-performance

# Manual testing
make test-manual
./test/manual/test_basic
```

## 📚 Documentation

### API Documentation

Use Doxygen format for all public APIs:

```c
/**
 * @brief Add a directory to file watching
 * @param env JNI environment pointer
 * @param clazz FileWatcher class
 * @param watcherPtr Native watcher handle from create()
 * @param path Java string containing directory path
 * @return JNI_TRUE on success, JNI_FALSE on failure
 * 
 * @note The path must be an existing directory with read permissions
 * @warning This function is not thread-safe, use external synchronization
 * 
 * @see Java_com_jetbrains_analyzer_filewatcher_FileWatcher_unwatch
 * @since 1.0.0
 */
JNIEXPORT jboolean JNICALL
Java_com_jetbrains_analyzer_filewatcher_FileWatcher_watch(
    JNIEnv *env, jclass clazz, jlong watcherPtr, jstring path);
```

### README Updates

Update README.md when adding:
- New features or functionality
- Build requirements or dependencies
- Usage examples
- Performance improvements

## 🔄 Development Workflow

### Branch Strategy

- `main` - Stable releases
- `develop` - Integration branch for features
- `feature/description` - Individual features
- `bugfix/description` - Bug fixes
- `hotfix/description` - Critical fixes for main

### Commit Messages

Use conventional commit format:

```
feat: add recursive directory watching support

- Implement recursive watch addition for directories
- Add configuration option for recursion depth
- Include tests for nested directory structures
- Update documentation with recursion examples

Closes #42
```

Types: `feat`, `fix`, `docs`, `test`, `refactor`, `perf`, `chore`

### Release Process

1. **Feature Complete**: All planned features implemented and tested
2. **Testing**: Full test suite passes on multiple devices
3. **Documentation**: README, API docs, and examples updated
4. **Version Bump**: Update version in CMakeLists.txt and Makefile
5. **Tag Release**: Create git tag with changelog
6. **Package**: Create release artifacts

## 🤝 Community Guidelines

### Code of Conduct

- **Be respectful** and inclusive
- **Help newcomers** learn and contribute
- **Focus on technical merit** in discussions
- **Assume good intentions** in communications

### Getting Help

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: Questions and general discussion
- **Wiki**: Detailed guides and tutorials

## 🎯 Contribution Ideas

### Good First Issues

- Improve error messages and logging
- Add more unit tests for edge cases
- Write usage examples for different scenarios
- Improve documentation with diagrams

### Advanced Contributions

- Cross-platform support (other architectures)
- Performance optimizations
- Advanced inotify features (filters, recursion)
- Integration with other language servers

### Documentation Improvements

- Video tutorials for setup and usage
- Architecture diagrams and flow charts
- Troubleshooting guides
- Performance tuning guides

## 🔍 Code Review Process

### What We Look For

- **Correctness**: Code does what it claims to do
- **Safety**: Proper error handling and resource management
- **Performance**: Efficient algorithms and memory usage
- **Maintainability**: Clear code structure and documentation
- **Testability**: Adequate test coverage

### Review Timeline

- **Initial Review**: Within 48-72 hours
- **Follow-up**: Responses to feedback within 24-48 hours
- **Merge**: After all feedback addressed and CI passes

Thank you for contributing to make Kotlin development on Termux better for everyone! 🚀