/****************************************************************************
**
** Copyright (C) 1992-2009 Nokia. All rights reserved.
**
** This file is part of Qt Jambi.
**
** ** $BEGIN_LICENSE$
** Commercial Usage
** Licensees holding valid Qt Commercial licenses may use this file in
** accordance with the Qt Commercial License Agreement provided with the
** Software or, alternatively, in accordance with the terms contained in
** a written agreement between you and Nokia.
**
** GNU Lesser General Public License Usage
** This file may be used under the terms of the GNU Lesser
** General Public License version 2.1 as published by the Free Software
** Foundation and appearing in the file LICENSE.LGPL included in the
** packaging of this file.  Please review the following information to
** ensure the GNU Lesser General Public License version 2.1 requirements
** will be met: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html.
**
** In addition, as a special exception, Nokia gives you certain
** additional rights. These rights are described in the Nokia Qt LGPL
** Exception version 1.0, included in the file LGPL_EXCEPTION.txt in this
** package.
**
** GNU General Public License Usage
** Alternatively, this file may be used under the terms of the GNU
** General Public License version 3.0 as published by the Free Software
** Foundation and appearing in the file LICENSE.GPL included in the
** packaging of this file.  Please review the following information to
** ensure the GNU General Public License version 3.0 requirements will be
** met: http://www.gnu.org/copyleft/gpl.html.
**
** If you are unsure which license is appropriate for your use, please
** contact the sales department at qt-sales@nokia.com.
** $END_LICENSE$

**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
****************************************************************************/

#include "generator_predefinitions.h"

#define QTJAMBI_CORE_H
#define QQMLPRIVATE_H
#define QML_GETTYPENAMES

#if QT_VERSION>=0x050000
    #include <QtCore/private/qabstractfileengine_p.h>
    #include <QtCore/private/qfsfileengine_p.h>
    #include <QtCore/private/qmetaobject_p.h>
#endif

#define QSETTINGS_H
#include <QtCore/QtCore>
#undef QSETTINGS_H
#define Q_OS_WIN
#include <QtCore/QSettings>
#undef Q_OS_WIN
#include <qtjambi_core/qtjambiqfuture.h>
#include <qtjambi_core/qtjambi_core_qhashes.h>

#define QACCESSIBLE_H
#include <QtGui/QtGui>
#include <QtGui/QOpenGLFunctions_1_0>
#include <QtGui/QOpenGLFunctions_1_1>
#include <QtGui/QOpenGLFunctions_1_2>
#include <QtGui/QOpenGLFunctions_1_3>
#include <QtGui/QOpenGLFunctions_1_4>
#include <QtGui/QOpenGLFunctions_1_5>
#include <QtGui/QOpenGLFunctions_2_0>
#include <QtGui/QOpenGLFunctions_2_1>
#include <QtGui/QOpenGLFunctions_3_0>
#include <QtGui/QOpenGLFunctions_3_1>
#include <QtGui/QOpenGLFunctions_3_2_Compatibility>
#include <QtGui/QOpenGLFunctions_3_2_Core>
#include <QtGui/QOpenGLFunctions_3_3_Compatibility>
#include <QtGui/QOpenGLFunctions_3_3_Core>
#include <QtGui/QOpenGLFunctions_4_0_Compatibility>
#include <QtGui/QOpenGLFunctions_4_0_Core>
#include <QtGui/QOpenGLFunctions_4_1_Compatibility>
#include <QtGui/QOpenGLFunctions_4_1_Core>
#include <QtGui/QOpenGLFunctions_4_2_Compatibility>
#include <QtGui/QOpenGLFunctions_4_2_Core>
#include <QtGui/QOpenGLFunctions_4_3_Compatibility>
#include <QtGui/QOpenGLFunctions_4_3_Core>
#include <QtGui/QOpenGLFunctions_4_4_Compatibility>
#include <QtGui/QOpenGLFunctions_4_4_Core>
#include <QtGui/QOpenGLFunctions_4_5_Compatibility>
#include <QtGui/QOpenGLFunctions_4_5_Core>

#ifndef QT_OPENGL_ES_2
#define QT_OPENGL_ES_2
#include <QtGui/QOpenGLFunctions_ES2>
#undef QT_OPENGL_ES_2
#else
#include <QtGui/QOpenGLFunctions_ES2>
#endif

#undef QACCESSIBLE_H
#define quint64 bool
#include <QtGui/qaccessible.h>
#undef quint64

#include <QtPlatformHeaders/QtPlatformHeaders>
#include <qtjambi_gui/qtjambi_gui.h>
#include <qtjambi_gui/qtjambi_gui_qhashes.h>
#include <qtjambi_gui/qtmatrixes.h>


#if QT_VERSION>=0x050000
    #include <QtWidgets/QtWidgets>
    #include <qtjambi_widgets/qtjambi_widgets_core.h>
    #ifndef QT_NO_CONCURRENT
        #include <QtConcurrent/QtConcurrent>
    #endif
    #include <QtQml/QtQml>
    #include <qtjambi_qml/qqmllistproperty.h>
#endif
