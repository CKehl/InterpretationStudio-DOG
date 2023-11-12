cmake_minimum_required(VERSION 2.8)
project(InterpretationStudio)
set(CMAKE_MODULE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/CMakeModules ${CMAKE_MODULE_PATH})

find_package(Java REQUIRED)
include(UseJava)
find_package(JNI REQUIRED)
# === SWT integration by default === #
#set(SWT_SEARCH_VERSION_STRING "4.12" CACHE STRING "version string of target SWT library")
set(SWT_ROOT_DIR "${CMAKE_INSTALL_PREFIX}/java" CACHE STRING "Root directory of the SWT bindings.")
find_package(SWT REQUIRED)
set(SWT_JAVA_BINARY_LIB ${ECLIPSE_SWT_LIBRARIES})
set(SWT_JAVA_NATIVE_JAR_LIB "" CACHE FILEPATH "Path to the SWT native version")
#find_package(JOGL REQUIRED)
set(CGEL_ROOT_DIR "${CMAKE_INSTALL_PREFIX}" CACHE STRING "Root directory of the CGEL library")
set(CGEL_JAVA_ROOT_DIR "${CMAKE_INSTALL_PREFIX}/java" CACHE STRING "Root directory of the Java CGEL bindings")
option(WITH_CGEL_JAVA "Use Java bindings of CGEL." ON)
find_package(CGEL REQUIRED)

find_package(VTK REQUIRED)
if(VTK_FOUND)
  include(${VTK_USE_FILE})
  include_directories(${VTK_INCLUDE_DIRS})
  #message(STATUS ${VTK_INSTALL_PREFIX})
  set(VTK_VERSION_STRING "${VTK_MAJOR_VERSION}.${VTK_MINOR_VERSION}")
  set(VTK_JAVA_PLATFORM ${CMAKE_SYSTEM_NAME})
  set(VTK_JAVA_ARCHITECTURE ${CMAKE_HOST_SYSTEM_PROCESSOR})
  mark_as_advanced(VTK_JAVA_PLATFORM VTK_JAVA_ARCHITECTURE)
  set(VTK_JAR_FILENAME "vtk-${VTK_VERSION_STRING}.jar" CACHE STRING "Binary compiled Java library (.jar)")
  set(VTK_NATIVE_JAR_FILENAME "vtk-${VTK_VERSION_STRING}-natives-${VTK_JAVA_PLATFORM}-${VTK_JAVA_ARCHITECTURE}.jar" CACHE STRING "Java-Binary compiled native library (.jar)")
  find_file(VTK_JAVA_NATIVE_JAR_LIB ${VTK_NATIVE_JAR_FILENAME} PATHS ${VTK_INSTALL_PREFIX} PATH_SUFFIXES lib bin)
  find_file(VTK_JAVA_BINARY_LIB ${VTK_JAR_FILENAME} PATHS ${VTK_INSTALL_PREFIX} PATH_SUFFIXES lib bin)
  #set(VTK_JAVA_BINARY_LIB "${VTK_INSTALL_PREFIX}/vtk-${VTK_MAJOR_VERSION}.${VTK_MINOR_VERSION}.jar" CACHE STRING "Binary compiled Java library (.jar)")
endif()

# Add the option for build the JOGL component to VTK.
set(JOGL_JAVA_ROOT_DIR "${CMAKE_INSTALL_PREFIX}/java" CACHE STRING "Root directory of the JOGL bindings")
set(JOGL_VERSION "2.3.2")
#set(JAVA_PLATFORM "linux")
#set(JAVA_ARCHITECTURE "amd64")
set(JAVA_PLATFORM ${CMAKE_SYSTEM_NAME})
set(JAVA_ARCHITECTURE ${CMAKE_HOST_SYSTEM_PROCESSOR})
mark_as_advanced(JAVA_PLATFORM JAVA_ARCHITECTURE)

set(MAVEN_REPO "$ENV{HOME}/.m2/repository")
string(TOLOWER ${JAVA_PLATFORM} _java_platform)
string(TOLOWER ${JAVA_ARCHITECTURE} _java_architecture)
find_file(JOGL_LIB jogl-all-${JOGL_VERSION}.jar PATHS ${MAVEN_REPO}/org/jogamp/jogl/jogl-all/${JOGL_VERSION} $ENV{LD_LIBRARY_PATH} ${JOGL_JAVA_ROOT_DIR} ${JOGL_DIR} PATH_SUFFIXES lib bin)
find_file(JOGL_LIB_NATIVE jogl-all-${JOGL_VERSION}-native-${_java_platform}-${_java_architecture}.jar PATHS ${MAVEN_REPO}/org/jogamp/jogl/jogl-all/${JOGL_VERSION}  $ENV{LD_LIBRARY_PATH} ${JOGL_JAVA_ROOT_DIR} ${JOGL_DIR} PATH_SUFFIXES lib bin)
find_file(JOGL_GLUE gluegen-rt-${JOGL_VERSION}.jar PATHS ${MAVEN_REPO}/org/jogamp/gluegen/gluegen-rt/${JOGL_VERSION} $ENV{LD_LIBRARY_PATH} ${JOGL_JAVA_ROOT_DIR} ${JOGL_DIR} PATH_SUFFIXES lib bin)
find_file(JOGL_GLUE_NATVE gluegen-rt-${JOGL_VERSION}-native-${_java_platform}-${_java_architecture}.jar PATHS ${MAVEN_REPO}/org/jogamp/gluegen/gluegen-rt/${JOGL_VERSION} $ENV{LD_LIBRARY_PATH} ${JOGL_JAVA_ROOT_DIR} ${JOGL_DIR} PATH_SUFFIXES lib bin)
mark_as_advanced(JOGL_LIB JOGL_LIB_NATIVE JOGL_GLUE JOGL_GLUE_NATVE)

if(NOT JOGL_LIB)
message(ERROR "CMake can not build Interpretation Studio without JOGL jar file. Please set the JOGL_LIB variable.")
endif()
if(NOT JOGL_GLUE)
message(ERROR "CMake can not build Interpretation Studio without GLUE jar file. Please set the JOGL_GLUE variable.")
endif()
if(NOT ECLIPSE_SWT_LIBRARIES)
message(ERROR "CMake can not build Interpretation Studio without Java Eclipse SWT jar file. Please set the ECLIPSE_SWT_LIBRARIES variable.")
endif()
if(NOT CGEL_JAVA_BINARY_LIB)
message(ERROR "CMake can not build Interpretation Studio without CGEL jar file. Please set the CGEL_JAVA_BINARY_LIB variable.")
endif()

if(WIN32)
  set(JAVA_CLASSPATH_SEPARATOR "\\;")
else()
  set(JAVA_CLASSPATH_SEPARATOR ":")
endif()

# Handle Java class path
set(JAVA_COMPILATION_CLASSPATH ".")
#if(ECLIPSE_SWT_LIBRARIES)
#    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${ECLIPSE_SWT_LIBRARIES}")
#endif()
if(JOGL_LIB)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${JOGL_LIB}")
endif()
if(JOGL_LIB_NATIVE)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${JOGL_LIB_NATIVE}")
endif()
if(JOGL_GLUE)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${JOGL_GLUE}")
endif()
if(JOGL_GLUE_NATIVE)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${JOGL_GLUE_NATIVE}")
endif()
if(VTK_JAVA_BINARY_LIB)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${VTK_JAVA_BINARY_LIB}")
endif()
if(VTK_JAVA_NATIVE_JAR_LIB)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${VTK_JAVA_NATIVE_JAR_LIB}")
endif()
if(CGEL_JAVA_BINARY_LIB)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${CGEL_JAVA_BINARY_LIB}")
endif()
if(CGEL_JAVA_NATIVE_JAR_LIB)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${CGEL_JAVA_NATIVE_JAR_LIB}")
endif()
if(SWT_JAVA_BINARY_LIB)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${SWT_JAVA_BINARY_LIB}")
endif()
if(SWT_JAVA_NATIVE_JAR_LIB)
    set(JAVA_COMPILATION_CLASSPATH "${JAVA_COMPILATION_CLASSPATH}${JAVA_CLASSPATH_SEPARATOR}${SWT_JAVA_NATIVE_JAR_LIB}")
endif()

set(APP_NAME InterpretationStudio)
#include_directories( ${OSG_INCLUDE_DIR} ${OSG_GEN_INCLUDE_DIR} )
set(CMAKE_VERBOSE_MAKEFILE ON CACHE STRING "show compilation commands" )
#OPTION(BUILD_SHARED_LIBS "Build shared libraries." ON)
#OPTION(BUILD_STATIC_LIBS "Build static libraries." ON)

################################################################################
# Installation stuff
#SET(CMAKE_BUILD_POSTFIX "" CACHE STRING "final postfix")
SET(CMAKE_DEBUG_POSTFIX "d" CACHE STRING "add a postfix, usually d on windows")
SET(CMAKE_RELEASE_POSTFIX "" CACHE STRING "add a postfix, usually empty on windows")
SET(CMAKE_RELWITHDEBINFO_POSTFIX "rd" CACHE STRING "add a postfix, usually empty on windows")
SET(CMAKE_MINSIZEREL_POSTFIX "s" CACHE STRING "add a postfix, usually empty on windows")
# Correct any incorrect case usage in CMAKE_BUILD_TYPE
IF     (CMAKE_BUILD_TYPE MATCHES "release" OR CMAKE_BUILD_TYPE MATCHES "RELEASE")
    SET(CMAKE_BUILD_TYPE "Release")
ELSEIF (CMAKE_BUILD_TYPE MATCHES "minsizerel" OR CMAKE_BUILD_TYPE MATCHES "MINSIZEREL")
    SET(CMAKE_BUILD_TYPE "MinSizeRel")
ELSEIF (CMAKE_BUILD_TYPE MATCHES "relwithdebinfo" OR CMAKE_BUILD_TYPE MATCHES "RELWITHDEBINFO")
    SET(CMAKE_BUILD_TYPE "RelWithDebInfo")
ELSEIF (CMAKE_BUILD_TYPE MATCHES "debug" OR CMAKE_BUILD_TYPE MATCHES "DEBUG")
    SET(CMAKE_BUILD_TYPE "Debug")
ENDIF()
# Set the build postfix extension according to what configuration is being built.
IF (CMAKE_BUILD_TYPE MATCHES "Release")
    SET(CMAKE_BUILD_POSTFIX "${CMAKE_RELEASE_POSTFIX}")
    SET(CMAKE_EXECUTABLE_SUFFIX "${CMAKE_RELEASE_POSTFIX}")
ELSEIF (CMAKE_BUILD_TYPE MATCHES "MinSizeRel")
    SET(CMAKE_BUILD_POSTFIX "${CMAKE_MINSIZEREL_POSTFIX}")
    SET(CMAKE_EXECUTABLE_SUFFIX "${CMAKE_MINSIZEREL_POSTFIX}")
ELSEIF(CMAKE_BUILD_TYPE MATCHES "RelWithDebInfo")
    SET(CMAKE_BUILD_POSTFIX "${CMAKE_RELWITHDEBINFO_POSTFIX}")
    SET(CMAKE_EXECUTABLE_SUFFIX "${CMAKE_RELWITHDEBINFO_POSTFIX}")
ELSEIF(CMAKE_BUILD_TYPE MATCHES "Debug")
    SET(CMAKE_BUILD_POSTFIX "${CMAKE_DEBUG_POSTFIX}")
    SET(CMAKE_EXECUTABLE_SUFFIX "${CMAKE_DEBUG_POSTFIX}")
ELSE()
    SET(CMAKE_BUILD_POSTFIX "")
    SET(CMAKE_EXECUTABLE_SUFFIX "")
ENDIF()
# end of installation appendix
################################################################################

include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2})

#SET(CMAKE_JAVA_COMPILE_FLAGS "-source" "1.6" "-target" "1.6")
#SET(CMAKE_JAVA_COMPILE_FLAGS "-source" "1.8" "-target" "1.8")
# Set the javac source version
set(ISTUDIO_JAVA_SOURCE_VERSION "1.8" CACHE STRING "javac source version")
mark_as_advanced(ISTUDIO_JAVA_SOURCE_VERSION)
set(ISTUDIO_JAVA_TARGET_VERSION "1.8" CACHE STRING "javac target version")
mark_as_advanced(ISTUDIO_JAVA_TARGET_VERSION)

option(JAVA_MAVEN_SUPPORT "Support setup for Maven integration" OFF)
if(JAVA_MAVEN_SUPPORT)
  if(NOT MAVEN_LOCAL_NATIVE_NAME)
    set(MAVEN_LOCAL_NATIVE_NAME "${CMAKE_SYSTEM_NAME}-${CMAKE_SYSTEM_PROCESSOR}" CACHE STRING "")
  endif()
  if(NOT MAVEN_NATIVE_ARTIFACTS)
    set(MAVEN_NATIVE_ARTIFACTS "${MAVEN_LOCAL_NATIVE_NAME}" CACHE STRING "")
  endif()
  if(NOT MAVEN_LOCAL_REPOSITORY)
    set(MAVEN_LOCAL_REPOSITORY "\${user.home}/cgel-maven-repos" CACHE STRING "")
  endif()
  if(NOT MAVEN_CGEL_GROUP_ID)
    set(MAVEN_ISTUDIO_GROUP_ID "de.ckehl" CACHE STRING "")
  endif()
  mark_as_advanced(MAVEN_LOCAL_REPOSITORY MAVEN_NATIVE_ARTIFACTS MAVEN_LOCAL_NATIVE_NAME MAVEN_CGEL_GROUP_ID)
  set(MAVEN_DEPENDENCY_XML)
else()
  set(MAVEN_LOCAL_NATIVE_NAME "${CMAKE_SYSTEM_NAME}-${CMAKE_SYSTEM_PROCESSOR}" CACHE STRING "Maven-string for local architecture native Java binaries")
endif()

set(JAVA_SOURCE_FILES 
    ckehl/vtk/SurfaceDataInterface.java 
    ckehl/vtk/PointToSurfaceMap.java 
    ckehl/vtk/MousePickInteractor.java 
    ckehl/vtk/PointPickInteractor.java 
    ckehl/vtk/AreaPickInteractor.java 
    ckehl/vtk/LineToSurfaceMap.java 
    ckehl/vtk/LinePickInteractor.java 
    ckehl/vtk/SWT_AWT_VTK_sample.java 
)

# Build Java classes
#FILE(MAKE_DIRECTORY "${CMAKE_CURRENT_BINARY_DIR}/java/bin")
add_custom_target(make_java_target_dir ALL COMMAND "${CMAKE_COMMAND}" -E make_directory "${CMAKE_CURRENT_BINARY_DIR}/java/bin")

SET(class_files)
FOREACH(_java_file ${JAVA_SOURCE_FILES})
    STRING(REGEX REPLACE "\\.java$"
           ".class" _class_file
           "${_java_file}")
    ADD_CUSTOM_COMMAND(
        OUTPUT "${CMAKE_CURRENT_BINARY_DIR}/java/bin/${_class_file}"
        COMMAND
            ${Java_JAVAC_EXECUTABLE}
            ${CMAKE_JAVA_COMPILE_FLAGS}
            -sourcepath "${CMAKE_CURRENT_SOURCE_DIR}/src/"
            -d "${CMAKE_CURRENT_BINARY_DIR}/java/bin"
            -cp ${JAVA_COMPILATION_CLASSPATH}
            "${CMAKE_CURRENT_SOURCE_DIR}/src/${_java_file}"
        DEPENDS "${CMAKE_CURRENT_SOURCE_DIR}/src/${_java_file}" )
    LIST(APPEND class_files "java/bin/${_class_file}")
ENDFOREACH()
ADD_CUSTOM_TARGET(JavaClasses ALL DEPENDS ${class_files})
ADD_DEPENDENCIES(JavaClasses make_java_target_dir)

file (COPY "${CMAKE_CURRENT_SOURCE_DIR}/res/assets"
      DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/java/bin"
      FILES_MATCHING
      PATTERN *.*
)

# copy JARs
#execute_process(COMMAND "${CMAKE_COMMAND}" -E make_directory "${CMAKE_CURRENT_BINARY_DIR}/java/lib")
#file (COPY "${CMAKE_INSTALL_PREFIX}/java/lib/"
#      DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/java/lib/"
#      FILES_MATCHING
#      PATTERN *.jar
#)

set(ISTUDIO_MANIFEST_PATH "PATH_UNSPECIFIED" CACHE FILEPATH "Manifest file to use for packaging")
if(WIN32)
  set(ISTUDIO_MANIFEST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/src/manifest_win32-x86_64.mf" CACHE FILEPATH "Manifest file to use for packaging")
endif()
if(UNIX)
  set(ISTUDIO_MANIFEST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/src/manifest_unix-x86_64.mf" CACHE FILEPATH "Manifest file to use for packaging")
endif()
if(APPLE)
  set(ISTUDIO_MANIFEST_PATH "${CMAKE_CURRENT_SOURCE_DIR}/src/manifest_darwin-x86_64.mf" CACHE FILEPATH "Manifest file to use for packaging")
endif()


# Create the natives directory
set(NATIVE_FOLDER_NAME InterpretationStudio_natives)
add_custom_target(make_native_library_dir ALL COMMAND "${CMAKE_COMMAND}" -E make_directory "${CMAKE_INSTALL_PREFIX}/${NATIVE_FOLDER_NAME}")

# Only for windows but no harm to do it always
file (COPY "${CMAKE_INSTALL_PREFIX}/bin"
      DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/${NATIVE_FOLDER_NAME}"
      FILES_MATCHING
      PATTERN *.dll
      PATTERN *.jnilib
)
file (COPY "${CMAKE_INSTALL_PREFIX}/java/bin"
      DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/${NATIVE_FOLDER_NAME}"
      FILES_MATCHING
      PATTERN *.dll
      PATTERN *.jnilib
)
file (COPY "${CMAKE_INSTALL_PREFIX}/java/lib"
      DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/${NATIVE_FOLDER_NAME}"
      FILES_MATCHING
      PATTERN *.dll
      PATTERN *.jnilib
)

# For Apple and Linux
file (COPY "${CMAKE_INSTALL_PREFIX}/lib"
      DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/${NATIVE_FOLDER_NAME}"
      FILES_MATCHING
      PATTERN *.so*
      PATTERN *.dylib
      PATTERN *.jnilib
)
file (COPY "${CMAKE_INSTALL_PREFIX}/java/lib"
      DESTINATION "${CMAKE_CURRENT_BINARY_DIR}/${NATIVE_FOLDER_NAME}"
      FILES_MATCHING
      PATTERN *.so*
      PATTERN *.dylib
      PATTERN *.jnilib
)

# Build the JAR archive
ADD_CUSTOM_COMMAND(
  COMMAND ${JAVA_ARCHIVE} -cfm "${CMAKE_CURRENT_BINARY_DIR}/java/InterpretationStudio${CMAKE_BUILD_POSTFIX}.jar" "${ISTUDIO_MANIFEST_PATH}" -C "${CMAKE_CURRENT_BINARY_DIR}/${NATIVE_FOLDER_NAME}/bin" . -C "${CMAKE_CURRENT_BINARY_DIR}/${NATIVE_FOLDER_NAME}/lib" . -C "${CMAKE_CURRENT_BINARY_DIR}/java/bin" .
  DEPENDS ${class_files}
  OUTPUT "${CMAKE_CURRENT_BINARY_DIR}/java/InterpretationStudio${CMAKE_BUILD_POSTFIX}.jar"
  COMMENT "Java Archive Runnable"
)
ADD_CUSTOM_TARGET(JavaJarFile ALL DEPENDS "${CMAKE_CURRENT_BINARY_DIR}/java/InterpretationStudio${CMAKE_BUILD_POSTFIX}.jar")
ADD_DEPENDENCIES(JavaJarFile JavaClasses)

set_property(DIRECTORY APPEND PROPERTY ADDITIONAL_MAKE_CLEAN_FILES "${CMAKE_CURRENT_BINARY_DIR}/${NATIVE_FOLDER_NAME}" "${CMAKE_CURRENT_BINARY_DIR}/java")


install(FILES "${CMAKE_CURRENT_BINARY_DIR}/java/InterpretationStudio${CMAKE_BUILD_POSTFIX}.jar" PERMISSIONS OWNER_READ GROUP_READ OWNER_WRITE GROUP_WRITE OWNER_EXECUTE GROUP_EXECUTE DESTINATION java/bin)
