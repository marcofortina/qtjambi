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

package generator;

import io.qt.*;
import io.qt.QtJambiObject.QPrivateConstructor;
import io.qt.core.QCoreApplication;
import io.qt.core.QPoint;
import io.qt.core.QPointF;
import io.qt.gui.*;

class QHelpContentItem___ extends QHelpContentItem {

    @io.qt.QtUninvokable
    public final int childPosition(QHelpContentItem child)    {
        long nativeId = io.qt.internal.QtJambiInternal.nativeId(this);
        if (nativeId == 0)
            throw new QNoNativeResourcesException("Function call on incomplete object of type: " +getClass().getName());
        return __qt_QHelpContentItem_childPosition_nativepointer(nativeId, child == null ? null : QNativePointer.fromObject(child));
    }

    @io.qt.QtUninvokable
    public final QHelpContentItem child(int row)    {
        return __child(row).object(QHelpContentItem.class);
    }

    @io.qt.QtUninvokable
    public final QHelpContentItem parent()    {
        return __parent().object(QHelpContentItem.class);
    }

}// class

