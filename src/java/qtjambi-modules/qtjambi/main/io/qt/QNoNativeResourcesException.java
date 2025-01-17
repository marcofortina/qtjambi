/****************************************************************************
**
** Copyright (C) 1992-2009 Nokia. All rights reserved.
** Copyright (C) 2009-2022 Dr. Peter Droste, Omix Visualization GmbH & Co. KG. All rights reserved.
**
** This file is part of Qt Jambi.
**
** ** $BEGIN_LICENSE$
** GNU Lesser General Public License Usage
** This file may be used under the terms of the GNU Lesser
** General Public License version 2.1 as published by the Free Software
** Foundation and appearing in the file LICENSE.LGPL included in the
** packaging of this file.  Please review the following information to
** ensure the GNU Lesser General Public License version 2.1 requirements
** will be met: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html.
** 
** GNU General Public License Usage
** Alternatively, this file may be used under the terms of the GNU
** General Public License version 3.0 as published by the Free Software
** Foundation and appearing in the file LICENSE.GPL included in the
** packaging of this file.  Please review the following information to
** ensure the GNU General Public License version 3.0 requirements will be
** met: http://www.gnu.org/copyleft/gpl.html.
** $END_LICENSE$

**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
****************************************************************************/
package io.qt;

/**
 * The QNoNativeResourcesException is used to indicate that the C++
 * native resources of a Java object are destroyed while it is still
 * referenced in Java.
 * <p>
 * Most Qt Jambi objects consist of a C++ part, i.e., native C++
 * resources, and a Java peer that wraps the public functions of the
 * native objects. In most cases one can rely on the Java memory
 * model and let objects be garbage collected, in which case this
 * kind of exception will never occur (the garbage collector will
 * only collect unreferenced memory in Java).
 * <p>
 * In some cases, the ownership of an object is transferred to C++,
 * for instance, when QObjects are passed a parent or items are added
 * to a QGraphicsScene.  In these cases the C++ part of the object
 * can be deleted explicitly by, for instance, using the C++ "delete"
 * keyword. When the C++ part of the object has been deleted it can
 * no longer be used and this exception is thrown.
 * <p>
 * If you are encountering this exception, it is likely due to one of
 * the following reasons:
 * </p>
 * <ul>
 * <li>The object you are accessing is owned by Qt Jambi
 * and has been destroyed.</li>
 * <li>The object you are accessing is an object created by Qt Jambi and
 * then passed to you via a call to a non-final method you have overridden (e.g. an
 * event handler.) Many such objects are temporary and you should not retain
 * references to them.</li>
 * <li>You have manually called dispose() or disposeLater() on the object to delete
 * the native resources.</li>
 * </ul>
 * <p>
 * It is possible to check if an object has a valid C++ part or not
 * by checking isDisposed().
 * </p>
 */
public class QNoNativeResourcesException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public QNoNativeResourcesException(String message)
    {
        super(message);
    }
}
