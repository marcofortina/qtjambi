/****************************************************************************
**
** Copyright (C) 1992-2009 Nokia. All rights reserved.
** Copyright (C) 2009-2022 Dr. Peter Droste, Omix Visualization GmbH & Co. KG. All rights reserved.
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

#ifndef QDOC_GENERATOR
#define QDOC_GENERATOR

#include "javagenerator.h"
#include "metajava.h"

QT_WARNING_DISABLE_CLANG("-Woverloaded-virtual")
QT_WARNING_DISABLE_GCC("-Woverloaded-virtual")

class QDocGenerator: public JavaGenerator {
    public:
        QDocGenerator();

        void generate() override;
        QString subDirectoryForClass(const AbstractMetaClass *java_class) const override;
        QString fileNameForClass(const AbstractMetaClass *java_class) const override;
        virtual void write(QTextStream &s, const AbstractMetaClass *java_class);
        virtual void write(QTextStream &s, const int size, const AbstractMetaEnumValue *java_enum_value);
        virtual void write(QTextStream &s, const AbstractMetaEnum *java_enum);
        virtual void writeOverload(QTextStream &s, const AbstractMetaFunction *java_function, int arg_count);
        virtual void write(QTextStream &s, const AbstractMetaFunction *java_function);
        virtual void write(QTextStream &s, const AbstractMetaField *java_field);
        virtual void writeSignal(QTextStream &s, const AbstractMetaFunction *java_function);
};

#endif // QDOC_GENERATOR
