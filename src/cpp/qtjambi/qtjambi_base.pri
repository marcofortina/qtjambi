!android:{
    !exists($$(JAVA_HOME_TARGET)) {
        # Ant/Maven should set this up (this can't be a symlink)
        exists($$(JAVA_HOME)) {
            JAVA_HOME_TARGET = $$(JAVA_HOME)
        } else {
            error("Please set your JAVA_HOME_TARGET or JAVA_HOME environment variable to point to the directory of your Java SDK. Current JAVA_HOME_TARGET: $$(JAVA_HOME_TARGET) JAVA_HOME: $$(JAVA_HOME)")
        }
    } else {
        # This has the effect of making $$JAVA_HOME_TARGET work in this file as used below
        JAVA_HOME_TARGET = $$(JAVA_HOME_TARGET)
    }
}

VER_MAJ = $$section(VERSION, ., 0, 0)
VER_MIN = $$section(VERSION, ., 1, 1)
VER_PAT = $$section(VERSION, ., 2, 2)

isEmpty(TARGET) {
    error("Please specify TARGET name before including qtjambi_base.pri");
}

TEMPLATE = lib
isEmpty(DESTDIR) {
    DESTDIR = ../lib
    DLLDESTDIR = ../bin
}

CONFIG(debug, debug|release) {
    win32:{
        TARGET = $$member(TARGET, 0)d
    }else{
        macx:{
            TARGET = $$member(TARGET, 0)_debug
        }else{
            TARGET = $$member(TARGET, 0)_debug
        }
    }
}

INCLUDEPATH += $$PWD/..
DEPENDPATH += $$PWD/..

macx:{
    # if(!defined(QMAKE_MAC_SDK)) { }
    #QMAKE_MAC_SDK=/Developer/SDKs/MacOSX10.6.sdk
    #QMAKE_MACOSX_DEPLOYMENT_TARGET = 10.4
    #LIBS += -framework JavaVM
    INCLUDEPATH += $$JAVA_HOME_TARGET/include
    INCLUDEPATH += $$JAVA_HOME_TARGET/include/darwin
    QMAKE_EXTENSION_SHLIB = jnilib
	# this option is necessary to properly compile on mountain lion because of std::string to char* casts
	QMAKE_CXXFLAGS += -fpermissive 
} else {
    !android:INCLUDEPATH += $$quote($$JAVA_HOME_TARGET/include)
    win32 {
        INCLUDEPATH += $$quote($$JAVA_HOME_TARGET/include/win32)
    }
    solaris-g++ | solaris-cc {
        INCLUDEPATH += $$quote($$JAVA_HOME_TARGET/include/solaris)
    }
    linux-g++* {
        INCLUDEPATH += $$quote($$JAVA_HOME_TARGET/include/linux)
    }
    freebsd-g++* {
        INCLUDEPATH += $$quote($$JAVA_HOME_TARGET/include/freebsd)
    }
}

contains(QT_CONFIG, release):contains(QT_CONFIG, debug) {
    # Qt was configued with both debug and release libs
    CONFIG += debug_and_release build_all
}

# make install related...
!isEmpty(INSTALL_PREFIX) {
    target.path = $$INSTALL_PREFIX
    INSTALLS = target
}

win32-msvc* {
    CONFIG += precompile_header
}

android:CONFIG += rtti
android:CONFIG += exceptions
android:QMAKE_CXXFLAGS_EXCEPTIONS_ON += -fexceptions
android:QMAKE_CXXFLAGS += -fexceptions -frtti
android:QMAKE_LFLAGS += -Wl,--export-dynamic -Wl,--exclude-libs,libgcc_real.a -Wl,--exclude-libs,libunwind.a -Wl,--exclude-libs,libgcc.a -lunwind
android:QMAKE_CXXFLAGS += -funwind-tables

macx {
    contains(QT_CONFIG, x86):CONFIG += x86
    contains(QT_CONFIG, ppc):CONFIG += ppc
    contains(QT_CONFIG, x86_64):CONFIG += x86_64
    contains(QT_CONFIG, ppc64):CONFIG += ppc64
    contains(QT_CONFIG, arm64):CONFIG += arm64
    CONFIG -= precompile_header
    QMAKE_CXXFLAGS += -Wc++14-extensions
    QMAKE_CXXFLAGS_WARN_OFF += -Wdollar-in-identifier-extension -Woverloaded-virtual
    greaterThan(QT_MAJOR_VERSION, 5):{
        greaterThan(QT_MAJOR_VERSION, 6) | greaterThan(QT_MINOR_VERSION, 1):{
            QMAKE_APPLE_DEVICE_ARCHS = x86_64 arm64
        }
    }
}

# gcc reports some functions as unused when they are not.
linux-g++* | freebsd-g++* | win32-g++* {
    QMAKE_CXXFLAGS_WARN_OFF += -Wdollar-in-identifier-extension -Woverloaded-virtual
    QMAKE_CXXFLAGS_WARN_ON += -Wno-unused-function
    QMAKE_LFLAGS_NOUNDEF   += -Wl,--no-undefined
    QMAKE_LFLAGS += $$QMAKE_LFLAGS_NOUNDEF
}

linux-g++* : lessThan(QT_MAJOR_VERSION, 6):{
    QMAKE_CXXFLAGS += -fno-sized-deallocation
}

# Extra options to be set when using jump tables...
jumptable{
    CONFIG += hide_symbols

    # Tell the linker to strip the binaries..
    macx:QMAKE_LFLAGS += -Wl,-x
}

#NOTE: this is just to test, uncomment if after all RPATHs are wanted.
#linux-g++* | freebsd-g++* {
    #QMAKE_LFLAGS += -Wl,--rpath,\\\$\$ORIGIN/../lib
#}
