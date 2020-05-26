/****************************************************************************
**
** Copyright (C) 2009-2020 Dr. Peter Droste, Omix Visualization GmbH & Co. KG. All rights reserved.
**
** This file is part of Qt Jambi.
**
** ** $BEGIN_LICENSE$
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
** $END_LICENSE$
**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
****************************************************************************/


#ifndef QTMATRIXES_H
#define QTMATRIXES_H

#include <QtGui/QtGui>
#include <qtjambi_gui/qtjambi_gui_qhashes.h>

#define QTJAMBIMATRIX(M,N) QtJambiMatrix##M##x##N
#define QMATRIX(M,N) QMatrix##M##x##N

#define QTJAMBI_MATRIX_CONTENT(M,N)\
    public:\
        QTJAMBIMATRIX(M,N)();\
        QTJAMBIMATRIX(M,N)(const QMATRIX(M,N)& other);\
        explicit QTJAMBIMATRIX(M,N)(const float *values);\
        QMATRIX(M,N)& operator/=(float divisor);

#define QTJAMBI_MATRIX_EXTRACONTENT(M,N)\
    float& operator ()(int row, int column);\
    const float& operator ()(int row, int column) const;\
    bool isIdentity() const;\
    void setToIdentity();\
    void fill(float value);\
    QMATRIX(M,N)& operator+=(const QMATRIX(M,N)& other);\
    QMATRIX(M,N)& operator-=(const QMATRIX(M,N)& other);\
    QMATRIX(M,N)& operator*=(float factor);\
    bool operator==(const QMATRIX(M,N)& other) const;\
    QMATRIX(N,M) transposed() const;\
    void copyDataTo(float *values) const;\
    float *data();\
    const float *data() const;\
    const float *constData() const;\


#ifdef QT_JAMBI_RUN
#define QTJAMBI_MATRIX(M,N)\
    class QTJAMBIMATRIX(M,N){\
    QTJAMBI_MATRIX_CONTENT(M,N)\
    QTJAMBI_MATRIX_EXTRACONTENT(M,N)\
    };\
    uint qHash(const QTJAMBIMATRIX(M,N) &);
#else
#define QTJAMBI_MATRIX(M,N)\
    typedef QMATRIX(M,N) QTJAMBIMATRIX(M,N);
#endif

QTJAMBI_MATRIX(2,2)
QTJAMBI_MATRIX(2,3)
QTJAMBI_MATRIX(2,4)
QTJAMBI_MATRIX(3,2)
QTJAMBI_MATRIX(3,3)
QTJAMBI_MATRIX(3,4)
QTJAMBI_MATRIX(4,2)
QTJAMBI_MATRIX(4,3)

#endif // QTMATRIXES_H
