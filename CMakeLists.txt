cmake_minimum_required(VERSION 3.16)
project(termux_filewatcher VERSION 1.0.0 LANGUAGES C)

# Project configuration
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# Build options
option(BUILD_STUB "Build stub implementation instead of real" OFF)
option(BUILD_TESTING "Build test suite" ON)
option(BUILD_EXAMPLES "Build example programs" ON)

# Compiler flags
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -g -DDEBUG -O0 -Wall -Wextra -Wpedantic")
else()
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O2 -DNDEBUG -Wall -Wextra -Wpedantic")
endif()

# Find Java
find_package(JNI REQUIRED)
if(NOT JNI_FOUND)
    message(FATAL_ERROR "JNI not found. Please set JAVA_HOME environment variable.")
endif()

# Include directories
include_directories(
    include
    ${JNI_INCLUDE_DIRS}
)

# Source files
set(COMMON_SOURCES
    src/common/jni_helpers.c
)

set(STUB_SOURCES
    src/stub/stub_filewatcher.c
    ${COMMON_SOURCES}
)

set(REAL_SOURCES
    src/real/real_filewatcher.c
    ${COMMON_SOURCES}
)

# Main library target
if(BUILD_STUB)
    add_library(filewatcher_jni SHARED ${STUB_SOURCES})
    set_target_properties(filewatcher_jni PROPERTIES OUTPUT_NAME "libfilewatcher_jni_stub")
    message(STATUS "Building stub implementation")
else()
    add_library(filewatcher_jni SHARED ${REAL_SOURCES})
    target_link_libraries(filewatcher_jni pthread)
    set_target_properties(filewatcher_jni PROPERTIES OUTPUT_NAME "libfilewatcher_jni")
    message(STATUS "Building real implementation")
endif()

# Library properties
set_target_properties(filewatcher_jni PROPERTIES
    POSITION_INDEPENDENT_CODE ON
    PREFIX ""
    SUFFIX ".so"
    VERSION ${PROJECT_VERSION}
    SOVERSION ${PROJECT_VERSION_MAJOR}
)

# Installation
install(TARGETS filewatcher_jni
    LIBRARY DESTINATION lib
    RUNTIME DESTINATION bin
)

# Testing
if(BUILD_TESTING)
    enable_testing()
    add_subdirectory(test)
endif()

# Examples
if(BUILD_EXAMPLES)
    add_subdirectory(examples)
endif()

# Packaging
set(CPACK_GENERATOR "TGZ")
set(CPACK_PACKAGE_NAME "termux-filewatcher")
set(CPACK_PACKAGE_VERSION ${PROJECT_VERSION})
set(CPACK_PACKAGE_DESCRIPTION_SUMMARY "Termux-compatible FileWatcher JNI library")
set(CPACK_PACKAGE_VENDOR "yamsergey")
set(CPACK_PACKAGE_CONTACT "https://github.com/yamsergey/termux-filewatcher")
include(CPack)

# Custom targets
add_custom_target(stub
    COMMAND ${CMAKE_COMMAND} -DBUILD_STUB=ON -B ${CMAKE_BINARY_DIR}_stub ${CMAKE_SOURCE_DIR}
    COMMAND ${CMAKE_COMMAND} --build ${CMAKE_BINARY_DIR}_stub
    COMMENT "Building stub implementation"
)

add_custom_target(both
    COMMAND ${CMAKE_COMMAND} --build ${CMAKE_BINARY_DIR}
    COMMAND ${CMAKE_COMMAND} -DBUILD_STUB=ON -B ${CMAKE_BINARY_DIR}_stub ${CMAKE_SOURCE_DIR}  
    COMMAND ${CMAKE_COMMAND} --build ${CMAKE_BINARY_DIR}_stub
    COMMENT "Building both implementations"
)

# Validation target
add_custom_target(validate
    COMMAND ${CMAKE_CURRENT_SOURCE_DIR}/scripts/validate.sh
    COMMENT "Validating installation"
)

# Print build configuration
message(STATUS "Build type: ${CMAKE_BUILD_TYPE}")
message(STATUS "C compiler: ${CMAKE_C_COMPILER}")
message(STATUS "Java found: ${JNI_FOUND}")
message(STATUS "JNI include dirs: ${JNI_INCLUDE_DIRS}")
message(STATUS "Build stub: ${BUILD_STUB}")
message(STATUS "Build testing: ${BUILD_TESTING}")
message(STATUS "Build examples: ${BUILD_EXAMPLES}")