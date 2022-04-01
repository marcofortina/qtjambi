/****************************************************************************
**
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
package io.qt.autotests;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.qt.core.QTimer;
import io.qt.internal.QtJambiInternal;
import io.qt.pdf.QPdfDocument;
import io.qt.pdf.QPdfSelection;
import io.qt.pdf.widgets.QPdfView;
import io.qt.widgets.QApplication;

public class TestQPdfWidgets extends ApplicationInitializer {
	
	@BeforeClass
    public static void testInitialize() throws Exception {
    	ApplicationInitializer.testInitializeWithWidgets();
    }
	
    @Test
    public void test() {
    	QPdfDocument document = new QPdfDocument();
    	QPdfDocument.DocumentError error = document.load("classpath:io/qt/autotests/test.pdf");
    	Assert.assertEquals(QPdfDocument.DocumentError.NoError, error);
    	Assert.assertEquals(QPdfDocument.Status.Ready, document.status());
        QPdfSelection allText = document.getSelectionAtIndex(0, 9, 7);
        Assert.assertEquals("Ownership of QPdfSelection", QtJambiInternal.Ownership.Java, QtJambiInternal.ownership(allText));
        Assert.assertEquals("Ownership of QPdfSelection's Clone", QtJambiInternal.Ownership.Java, QtJambiInternal.ownership(allText.clone()));
        Assert.assertEquals("Text expected from selection", "QtJambi", allText.text());
    	QPdfView view = new QPdfView();
    	view.setDocument(document);
    	view.show();
    	QTimer.singleShot(1000, QApplication::quit);
    	QApplication.exec();
    }
    
}