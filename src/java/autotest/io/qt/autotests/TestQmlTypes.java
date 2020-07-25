/****************************************************************************
**
** Copyright (C) 2009-2020 Dr. Peter Droste, Omix Visualization GmbH & Co. KG. All rights reserved.
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
import org.junit.Test;

import io.qt.core.QByteArray;
import io.qt.core.QDir;
import io.qt.core.QMetaObject;
import io.qt.core.QMetaProperty;
import io.qt.core.QObject;
import io.qt.qml.QQmlComponent;
import io.qt.qml.QQmlEngine;
import io.qt.qml.QtQml;
import io.qt.qml.util.QmlTypes;

public class TestQmlTypes extends QApplicationTest{
	
	@Test
    public void run_testQmlTypes() {
		QtQml.qmlClearTypeRegistrations();
		QmlTypes.registerPackage("io.qt.autotests.qmltypes");
		QByteArray data = new QByteArray("import io.qt.autotests.qmltypes 5.0; Car {}");
		QQmlEngine qmlengine = new QQmlEngine();
		String prefix = io.qt.QtUtilities.qtPrefix();
        if(new QDir(prefix+"/qml").exists()) {
        	qmlengine.addImportPath(prefix+"/qml");
        }
		QQmlComponent component = new QQmlComponent(qmlengine);
		component.setData(data, null);
		QObject car = component.create();
		Assert.assertFalse(component.errorString(), component.isError());
		Assert.assertTrue("car is null", car!=null);
		QMetaObject carType = car.metaObject();
		QMetaProperty engineProp = carType.property("engine");
		Assert.assertTrue("engine property not available", engineProp!=null);
		QMetaProperty wheelsProp = carType.property("wheels");
		Assert.assertTrue("wheels property not available", wheelsProp!=null);
		Object engine = car.property("engine");
		Object wheels = car.property("wheels");
		Assert.assertTrue("engine is null", engine!=null);
		Assert.assertTrue("wheels is null", wheels!=null);
		Assert.assertTrue("engine has wrong type", engine instanceof io.qt.autotests.qmltypes.Engine);
		Assert.assertEquals(150, ((io.qt.autotests.qmltypes.Engine)engine).getPerformance());
		Assert.assertTrue("wheels has wrong type", wheels instanceof io.qt.qml.QQmlListProperty);
		io.qt.qml.QQmlListProperty<?> wheelsList = (io.qt.qml.QQmlListProperty<?>)wheels;
		Assert.assertEquals(4, wheelsList.count());
		for (int i = 0; i < wheelsList.count(); i++) {
			QObject wheel = wheelsList.at(i);
			Assert.assertEquals(52., wheel.property("radius"));
		}
		car.setProperty("wheelRadius", 53);
		for (int i = 0; i < wheelsList.count(); i++) {
			QObject wheel = wheelsList.at(i);
			Assert.assertEquals(53., wheel.property("radius"));
		}
	}
}