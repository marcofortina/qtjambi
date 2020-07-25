QTJAMBILIB = QtJambi3DQuickExtras
TARGET = $$QTJAMBILIB

VERSION = $$QT_VERSION

include(../../qtjambi/qtjambi_include.pri)
include(../$$QTJAMBI_CPP/$$QTJAMBILIB/generated.pri)
DESTDIR = ../../lib
DLLDESTDIR = ../../bin

QT += core gui qml 3dcore 3dinput 3dquick 3dlogic 3dquickextras