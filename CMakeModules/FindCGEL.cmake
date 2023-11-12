# Find Computer Graphics Elements Library (CGEL)
#   input: CGEL_ROOT_DIR
#          WITH_CGEL_JAVA
#          CGEL_JAVA_ROOT_DIR
#
# This sets the following variables:
#   CGEL_INCLUDE_DIR, where to find GeometricPrimitives.h, etc.
#   CGEL_LIBRARY, the libraries needed to use CGEL.
#   CGEL_FOUND, If false, do not try to use CGEL.
#   CGEL_LIBRARY_DIR, where to find the CGEL library
#
# to be installed to: ${CMAKE_INSTALL_PREFIX}/share/cmake/CGEL

find_path(CGEL_INCLUDE_DIRECTORY GeometricPrimitives.h
   /usr/include
   /usr/local/include
   ${CGEL_ROOT_DIR}/include/common
   ${CGEL_ROOT_DIR}/include/CGEL/common
)

set(CGEL_INCLUDE_DIR ${CGEL_INCLUDE_DIRECTORY})

set(CGEL_LIBRARY CGEL)
find_library(CGEL_LIBRARY_PATH NAMES ${CGEL_LIBRARY} )

include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(CGEL DEFAULT_MSG CGEL_INCLUDE_DIRECTORY CGEL_LIBRARY_PATH)

SET(CGEL_FOUND FALSE)
IF(CGEL_LIBRARY_PATH AND CGEL_INCLUDE_DIRECTORY)
    SET(CGEL_FOUND TRUE)
ENDIF(CGEL_LIBRARY_PATH AND CGEL_INCLUDE_DIRECTORY)

IF(WITH_CGEL_JAVA)
  set(CGEL_JAVA_PLATFORM ${CMAKE_SYSTEM_NAME})
  set(CGEL_JAVA_ARCHITECTURE ${CMAKE_HOST_SYSTEM_PROCESSOR})
  mark_as_advanced(CGEL_JAVA_PLATFORM CGEL_JAVA_ARCHITECTURE)
  set(CGEL_JAR_FILENAME "CGEL.jar" CACHE STRING "name of CGEL binary compiled Java library (.jar)")
  set(CGEL_NATIVE_JAR_FILENAME "CGEL-natives-${CGEL_JAVA_PLATFORM}-${CGEL_JAVA_ARCHITECTURE}.jar" CACHE STRING "name of CGEL Java-binary compiled native library (.jar)")
  find_file(CGEL_JAVA_NATIVE_JAR_LIB ${CGEL_NATIVE_JAR_FILENAME} PATHS ${CGEL_JAVA_ROOT_DIR} ${CGEL_ROOT_DIR} PATH_SUFFIXES lib bin)
  find_file(CGEL_JAVA_BINARY_LIB ${CGEL_JAR_FILENAME} PATHS ${CGEL_JAVA_ROOT_DIR} ${CGEL_ROOT_DIR} PATH_SUFFIXES lib bin)
ENDIF(WITH_CGEL_JAVA)

if(CGEL_FOUND)
  #message(STATUS "CGEL lib path: ${CGEL_LIBRARY_DIRECTORY}")
  get_filename_component (NATIVE_CGEL_LIB_PATH ${CGEL_LIBRARY_PATH} PATH)
  set(CGEL_LIBRARY_DIR ${NATIVE_CGEL_LIB_PATH})
  #message(STATUS "CGEL library dir: ${CGEL_LIBRARY_DIR}")
endif(CGEL_FOUND)

mark_as_advanced(CGEL_INCLUDE_DIR CGEL_LIBRARY CGEL_LIBRARY_DIR CGEL_JAVA_NATIVE_JAR_LIB CGEL_JAVA_BINARY_LIB)
