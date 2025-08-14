# Termux FileWatcher JNI Library
# Build system for both stub and real implementations

# Project configuration
PROJECT_NAME = libfilewatcher_jni
VERSION = 1.0.0
PREFIX = /data/data/com.termux/files/usr

# Toolchain
CC = clang
CFLAGS = -Wall -Wextra -Wpedantic -O2 -fPIC -shared
LDFLAGS = -shared
JAVA_HOME ?= $(PREFIX)/opt/openjdk

# Directories
SRC_DIR = src
INCLUDE_DIR = include
TEST_DIR = test
DIST_DIR = dist
BUILD_DIR = build

# Source files
COMMON_SOURCES = $(SRC_DIR)/common/jni_helpers.c
STUB_SOURCES = $(SRC_DIR)/stub/stub_filewatcher.c $(COMMON_SOURCES)
REAL_SOURCES = $(SRC_DIR)/real/real_filewatcher.c $(COMMON_SOURCES)

# Output files
STUB_TARGET = $(DIST_DIR)/$(PROJECT_NAME)_stub.so
REAL_TARGET = $(DIST_DIR)/$(PROJECT_NAME).so

# Include paths
INCLUDES = -I$(INCLUDE_DIR) -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

# Libraries
LIBS_STUB = 
LIBS_REAL = -lpthread

# Default target
.PHONY: all
all: real

# Create directories
$(DIST_DIR) $(BUILD_DIR):
	mkdir -p $@

# Real implementation (default)
.PHONY: real
real: $(REAL_TARGET)

$(REAL_TARGET): $(REAL_SOURCES) | $(DIST_DIR)
	$(CC) $(CFLAGS) $(INCLUDES) -o $@ $(REAL_SOURCES) $(LIBS_REAL)
	@echo "✅ Built real implementation: $@"

# Stub implementation
.PHONY: stub
stub: $(STUB_TARGET)

$(STUB_TARGET): $(STUB_SOURCES) | $(DIST_DIR)
	$(CC) $(CFLAGS) $(INCLUDES) -o $@ $(STUB_SOURCES) $(LIBS_STUB)
	@echo "✅ Built stub implementation: $@"

# Build both implementations
.PHONY: both
both: real stub

# Debug builds
.PHONY: debug debug-real debug-stub
debug: CFLAGS += -g -DDEBUG -O0
debug: debug-real

debug-real: CFLAGS += -g -DDEBUG -O0
debug-real: $(REAL_TARGET)

debug-stub: CFLAGS += -g -DDEBUG -O0
debug-stub: $(STUB_TARGET)

# Development build with extra warnings
.PHONY: dev
dev: CFLAGS += -g -DDEBUG -O0 -Weverything -Wno-padded -Wno-unused-macros
dev: real

# Testing
.PHONY: test test-unit test-integration test-manual test-performance
test: test-unit test-integration

test-unit: real
	@echo "🧪 Running unit tests..."
	$(MAKE) -C $(TEST_DIR)/unit

test-integration: real  
	@echo "🧪 Running integration tests..."
	$(MAKE) -C $(TEST_DIR)/integration

test-manual: real
	@echo "🧪 Building manual tests..."
	$(MAKE) -C $(TEST_DIR)/manual

test-performance: real
	@echo "📊 Running performance tests..."
	$(MAKE) -C $(TEST_DIR)/performance

# Installation
KOTLIN_LSP_PATH ?= /data/data/com.termux/files/home/work/opt/kotlin-lsp
NATIVE_LIB_DIR = $(KOTLIN_LSP_PATH)/native/Linux-AArch64
TARGET_LIB = $(NATIVE_LIB_DIR)/libfilewatcher_jni.so

.PHONY: install install-stub install-real uninstall backup restore
install: install-real

install-real: real backup
	@echo "📦 Installing real implementation to Kotlin LSP..."
	cp $(REAL_TARGET) $(TARGET_LIB)
	@echo "✅ Installed: $(TARGET_LIB)"

install-stub: stub backup  
	@echo "📦 Installing stub implementation to Kotlin LSP..."
	cp $(STUB_TARGET) $(TARGET_LIB)
	@echo "✅ Installed: $(TARGET_LIB)"

backup:
	@if [ -f "$(TARGET_LIB)" ] && [ ! -f "$(TARGET_LIB).backup" ]; then \
		echo "💾 Backing up original library..."; \
		cp "$(TARGET_LIB)" "$(TARGET_LIB).backup"; \
	fi

restore:
	@if [ -f "$(TARGET_LIB).backup" ]; then \
		echo "🔄 Restoring original library..."; \
		cp "$(TARGET_LIB).backup" "$(TARGET_LIB)"; \
		echo "✅ Restored original library"; \
	else \
		echo "❌ No backup found at $(TARGET_LIB).backup"; \
	fi

uninstall: restore

# Validation
.PHONY: validate validate-kotlin-lsp
validate: validate-kotlin-lsp

validate-kotlin-lsp:
	@echo "🔍 Validating Kotlin LSP integration..."
	@if [ ! -d "$(KOTLIN_LSP_PATH)" ]; then \
		echo "❌ Kotlin LSP not found at $(KOTLIN_LSP_PATH)"; \
		echo "   Set KOTLIN_LSP_PATH=/path/to/kotlin-lsp"; \
		exit 1; \
	fi
	@cd "$(KOTLIN_LSP_PATH)" && ./kotlin-lsp.sh.orig --help >/dev/null 2>&1 && \
		echo "✅ Kotlin LSP integration successful" || \
		echo "❌ Kotlin LSP integration failed"

# Documentation
.PHONY: docs
docs:
	@echo "📚 Generating documentation..."
	doxygen docs/Doxyfile 2>/dev/null || echo "⚠️  Doxygen not available, skipping API docs"

# Packaging
.PHONY: package
package: both
	@echo "📦 Creating release package..."
	mkdir -p $(BUILD_DIR)/release
	cp $(DIST_DIR)/* $(BUILD_DIR)/release/
	cp README.md LICENSE CONTRIBUTING.md $(BUILD_DIR)/release/
	tar -czf $(BUILD_DIR)/termux-filewatcher-$(VERSION).tar.gz -C $(BUILD_DIR)/release .
	@echo "✅ Package created: $(BUILD_DIR)/termux-filewatcher-$(VERSION).tar.gz"

# Docker testing (if available)
.PHONY: docker-test
docker-test:
	@if command -v docker >/dev/null 2>&1; then \
		echo "🐳 Running tests in Docker container..."; \
		docker build -t termux-filewatcher-test -f test/Dockerfile .; \
		docker run --rm termux-filewatcher-test; \
	else \
		echo "⚠️  Docker not available, skipping containerized tests"; \
	fi

# Cleaning
.PHONY: clean clean-dist clean-build clean-all
clean: clean-dist clean-build

clean-dist:
	rm -rf $(DIST_DIR)

clean-build:
	rm -rf $(BUILD_DIR)

clean-all: clean
	$(MAKE) -C $(TEST_DIR) clean 2>/dev/null || true

# Information
.PHONY: info
info:
	@echo "📋 Project Information"
	@echo "  Project: $(PROJECT_NAME) v$(VERSION)"
	@echo "  CC: $(CC) $(CFLAGS)"
	@echo "  Java: $(JAVA_HOME)"
	@echo "  Kotlin LSP: $(KOTLIN_LSP_PATH)"
	@echo ""
	@echo "🎯 Available Targets:"
	@echo "  real          - Build real implementation (default)"
	@echo "  stub          - Build stub implementation"  
	@echo "  both          - Build both implementations"
	@echo "  test          - Run test suite"
	@echo "  install       - Install to Kotlin LSP (real)"
	@echo "  install-stub  - Install stub to Kotlin LSP"
	@echo "  validate      - Test Kotlin LSP integration"
	@echo "  package       - Create release package"
	@echo "  clean         - Clean build artifacts"

# Default help
.PHONY: help
help: info

# Prevent deletion of intermediate files
.PRECIOUS: $(BUILD_DIR)/%.o