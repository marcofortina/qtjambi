/****************************************************************************
**
** Copyright (C) 1992-2009 Nokia. All rights reserved.
** Copyright (C) 2009-2022 Dr. Peter Droste, Omix Visualization GmbH & Co. KG. All rights reserved.
**
** This file is part of Qt Jambi.
**
** ** $BEGIN_LICENSE$
** GNU Lesser General Public License Usage
** Alternatively, this file may be used under the terms of the GNU Lesser
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
** $END_LICENSE$

**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
****************************************************************************/

package io.qt.tools.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;

import io.qt.tools.ant.FindCompiler.Compiler;

public class InitializeBuildTask extends AbstractInitializeTask {
	
	private static Properties targetProperties;

	private List<String> androidAbis = new ArrayList<>();
	private String qtVersion;
	private int qtMajorVersion;
	private int qtMinorVersion;
	private int qtPatchlevelVersion;
	private int qtJambiVersion;
	private boolean useQtFramework;
	private String qtVersionSource = "";
	private String versionSuffix; // beta4
	private String libInfix = "";
	private boolean isMinGW;

	private final List<String> generatorPreProcDefinesList = new ArrayList<>();
	private final List<String> generatorStaticLibsList = new ArrayList<>();
	private final Set<String> skippedModules = new HashSet<>();

	static class ModuleInfo {
		ModuleInfo(String preprocdef, String libraryName) {
			this.preprocdef = preprocdef;
			this.libraryName = libraryName;
		}

		final String preprocdef;
		final String libraryName;
	}

	private static Map<String, ModuleInfo> modules = new TreeMap<>();
	static {
		modules.put("gui", new ModuleInfo("QTJAMBI_NO_GUI", "QtGui"));
		modules.put("opengl", new ModuleInfo("QTJAMBI_NO_OPENGL", "QtOpenGL"));
		modules.put("widgets", new ModuleInfo("QTJAMBI_NO_WIDGETS", "QtWidgets"));
		modules.put("openglwidgets", new ModuleInfo("QTJAMBI_NO_OPENGLWIDGETS", "QtOpenGLWidgets"));
		modules.put("qml", new ModuleInfo("QTJAMBI_NO_QML", "QtQml"));
		modules.put("quick", new ModuleInfo("QTJAMBI_NO_QUICK", "QtQuick"));
		modules.put("quicktest", new ModuleInfo("QTJAMBI_NO_QUICKTEST", "QtQuickTest"));
		modules.put("quickcontrols2", new ModuleInfo("QTJAMBI_NO_QUICKCONTROLS2", "QtQuickControls2"));
		modules.put("quickwidgets", new ModuleInfo("QTJAMBI_NO_QUICKWIDGETS", "QtQuickWidgets"));
		modules.put("xml", new ModuleInfo("QTJAMBI_NO_XML", "QtXml"));
		modules.put("network", new ModuleInfo("QTJAMBI_NO_NETWORK", "QtNetwork"));
		modules.put("concurrent", new ModuleInfo("QTJAMBI_NO_CONCURRENT", "QtQuickControls2"));
		modules.put("sql", new ModuleInfo("QTJAMBI_NO_SQL", "QtSql"));
		modules.put("printsupport", new ModuleInfo("QTJAMBI_NO_PRINTSUPPORT", "QtPrintSupport"));
		modules.put("networkauth", new ModuleInfo("QTJAMBI_NO_NETWORKAUTH", "QtNetworkAuth"));
		modules.put("xmlpatterns", new ModuleInfo("QTJAMBI_NO_XMLPATTERNS", "QtXmlPatterns"));
		modules.put("virtualkeyboard", new ModuleInfo("QTJAMBI_NO_VIRTUAL_KEYBOARD", "QtVirtualKeyboard"));
		modules.put("quick3d", new ModuleInfo("QTJAMBI_NO_QUICK3D", "QtQuick3D"));
		modules.put("serialport", new ModuleInfo("QTJAMBI_NO_SERIALPORT", "QtSerialPort"));
		modules.put("serialbus", new ModuleInfo("QTJAMBI_NO_SERIALBUS", "QtSerialBus"));
		modules.put("sensors", new ModuleInfo("QTJAMBI_NO_SENSORS", "QtSensors"));
		modules.put("location", new ModuleInfo("QTJAMBI_NO_LOCATION", "QtLocation"));
		modules.put("positioning", new ModuleInfo("QTJAMBI_NO_POSITIONING", "QtPositioning"));
		modules.put("bluetooth", new ModuleInfo("QTJAMBI_NO_BLUETOOTH", "QtBluetooth"));
		modules.put("script", new ModuleInfo("QTJAMBI_NO_SCRIPT", "QtScript"));
		modules.put("scripttools", new ModuleInfo("QTJAMBI_NO_SCRIPTTOOLS", "QtScriptTools"));
		modules.put("multimedia", new ModuleInfo("QTJAMBI_NO_MULTIMEDIA", "QtMultimedia"));
		modules.put("multimediawidgets", new ModuleInfo("QTJAMBI_NO_MULTIMEDIAWIDGETS", "QtMultimediaWidgets"));
		modules.put("svg", new ModuleInfo("QTJAMBI_NO_SVG", "QtSvg"));
		modules.put("svgwidgets", new ModuleInfo("QTJAMBI_NO_SVGWIDGETS", "QtSvgWidgets"));
		modules.put("dbus", new ModuleInfo("QTJAMBI_NO_DBUS", "QtDBus"));
		modules.put("test", new ModuleInfo("QTJAMBI_NO_TEST", "QtTest"));
		modules.put("help", new ModuleInfo("QTJAMBI_NO_HELP", "QtHelp"));
		modules.put("qt3dcore", new ModuleInfo("QTJAMBI_NO_QT3DCORE", "Qt3DCore"));
		modules.put("qt3drender", new ModuleInfo("QTJAMBI_NO_QT3DRENDER", "Qt3DRender"));
		modules.put("qt3dinput", new ModuleInfo("QTJAMBI_NO_QT3DINPUT", "Qt3DInput"));
		modules.put("qt3dquick", new ModuleInfo("QTJAMBI_NO_QT3DQUICK", "Qt3DQuick"));
		modules.put("qt3dquickrender", new ModuleInfo("QTJAMBI_NO_QT3DQUICKRENDER", "Qt3DQuickRender"));
		modules.put("qt3dquickscene2d", new ModuleInfo("QTJAMBI_NO_QT3DQUICKSCENE2D", "Qt3DQuickScene2D"));
		modules.put("qt3dquickextras", new ModuleInfo("QTJAMBI_NO_QT3DQUICKEXTRAS", "Qt3DQuickExtras"));
		modules.put("qt3dextras", new ModuleInfo("QTJAMBI_NO_QT3DEXTRAS", "Qt3DExtras"));
		modules.put("qt3dlogic", new ModuleInfo("QTJAMBI_NO_QT3DLOGIC", "Qt3DLogic"));
		modules.put("qt3danimation", new ModuleInfo("QTJAMBI_NO_QT3DANIMATION", "Qt3DAnimation"));
		modules.put("websockets", new ModuleInfo("QTJAMBI_NO_WEBSOCKETS", "QtWebSockets"));
		modules.put("webchannel", new ModuleInfo("QTJAMBI_NO_WEBCHANNEL", "QtWebChannel"));
		modules.put("webengine", new ModuleInfo("QTJAMBI_NO_WEBENGINE", "QtWebEngine"));
		modules.put("webenginequick", new ModuleInfo("QTJAMBI_NO_WEBENGINEQUICK", "QtWebEngineQuick"));
		modules.put("webenginecore", new ModuleInfo("QTJAMBI_NO_WEBENGINECORE", "QtWebEngineCore"));
		modules.put("webenginewidgets", new ModuleInfo("QTJAMBI_NO_WEBENGINEWIDGETS", "QtWebEngineWidgets"));
		modules.put("webview", new ModuleInfo("QTJAMBI_NO_WEBVIEW", "QtWebView"));
		modules.put("uitools", new ModuleInfo("QTJAMBI_NO_UITOOLS", "QtUiTools"));
		modules.put("bodymovin", new ModuleInfo("QTJAMBI_NO_LOTTIE", "QtBodymovin"));
		modules.put("datavisualization", new ModuleInfo("QTJAMBI_NO_DATA_VISUALIZATION", "QtDataVisualization"));
		modules.put("charts", new ModuleInfo("QTJAMBI_NO_CHARTS", "QtCharts"));
//		modules.put("uiplugin",new ModuleInfo("QTJAMBI_NO_UIPLUGIN", "QtUiPlugin"));
		modules.put("designer", new ModuleInfo("QTJAMBI_NO_DESIGNER", "QtDesigner"));
		modules.put("remoteobjects", new ModuleInfo("QTJAMBI_NO_REMOTEOBJECTS", "QtRemoteObjects"));
		modules.put("gamepad", new ModuleInfo("QTJAMBI_NO_GAMEPAD", "QtGamepad"));
		modules.put("scxml", new ModuleInfo("QTJAMBI_NO_SCXML", "QtScxml"));
		modules.put("statemachine", new ModuleInfo("QTJAMBI_NO_STATEMACHINE", "QtStateMachine"));
		modules.put("nfc", new ModuleInfo("QTJAMBI_NO_NFC", "QtNfc"));
		modules.put("texttospeech", new ModuleInfo("QTJAMBI_NO_TEXTTOSPEECH", "QtTextToSpeech"));
		modules.put("purchasing", new ModuleInfo("QTJAMBI_NO_PURCHASING", "QtPurchasing"));
		modules.put("xcbqpa", new ModuleInfo(null, "QtXcbQpa"));
		modules.put("winextras", new ModuleInfo("QTJAMBI_NO_WINEXTRAS", "QtWinExtras"));
		modules.put("macextras", new ModuleInfo("QTJAMBI_NO_MACEXTRAS", "QtMacExtras"));
		modules.put("x11extras", new ModuleInfo("QTJAMBI_NO_X11EXTRAS", "QtX11Extras"));
		modules.put("pdf", new ModuleInfo("QTJAMBI_NO_PDF", "QtPdf"));
		modules.put("pdfwidgets", new ModuleInfo("QTJAMBI_NO_PDFWIDGETS", "QtPdfWidgets"));
		modules.put("activex", new ModuleInfo("QTJAMBI_NO_ACTIVEX", "QtAxBase"));
		modules.put("httpserver", new ModuleInfo("QTJAMBI_NO_HTTPSERVER", "QtHttpServer"));
	}

	// did somebody mention something about methods never being longer than 10
	// lines?
	@SuppressWarnings("unused")
	public void executeInitialize() throws BuildException {
		if(targetProperties!=null) {
			for(Object key : targetProperties.keySet()) {
				if(targetProperties.get(key)!=null) {
					propertyHelper.setProperty(key.toString(), targetProperties.get(key), true);
					getProject().log(this, "--> "+key+"=" + propertyHelper.getProperty(key.toString()), Project.MSG_INFO);
				}
			}
		}
		if(!"true".equals(AntUtil.getPropertyAsString(propertyHelper, "build.properties.loaded"))){
			Properties properties = new Properties();
			try (FileInputStream stream = new FileInputStream(new File("build.properties"))) {
				properties.load(stream);
			} catch (IOException e) {
				getProject().log("reading build.properties", e, Project.MSG_ERR);
			}
			for(Object key : properties.keySet()) {
				propertyHelper.setNewProperty(key.toString(), properties.get(key));
			}
		}
		String skip = AntUtil.getPropertyAsString(propertyHelper, "qtjambi.skipped.modules");
		if (skip != null) {
			for (String s : skip.split(",")) {
				skippedModules.add(s.trim());
//    			System.out.println("skipping module "+s.trim());
			}
		}

		String javaHome = System.getenv("JAVA_HOME");
		if (javaHome == null) {
			javaHome = this.getProject().getProperty("JAVA_HOME");
		} else {
			this.getProject().setNewProperty("JAVA_HOME", javaHome);
		}
		if (javaHome != null) {
			Properties properties = new Properties();
			try (FileInputStream stream = new FileInputStream(new File(new File(javaHome), "release"))) {
				properties.load(stream);
			} catch (IOException e) {
				getProject().log("reading java version", e, Project.MSG_ERR);
			}
			String majorVersion = "11";
			String javaVersion = properties.getProperty("JAVA_VERSION", "\"11\"");
			if (javaVersion != null) {
				if (javaVersion.startsWith("\"") && javaVersion.endsWith("\"")) {
					javaVersion = javaVersion.substring(1, javaVersion.length() - 1);
				}
				majorVersion = "";
				for (int i = 0; i < javaVersion.length(); i++) {
					char c = javaVersion.charAt(i);
					if (c >= '0' && c <= '9') {
						majorVersion += c;
					} else {
						break;
					}
				}
				if (this.getProject().getProperty("target.java.version") == null) {
					PropertyHelper.setNewProperty(getProject(), "target.java.version", "" + majorVersion);
					getProject().log(this, "target.java.version=" + majorVersion, Project.MSG_INFO);
				}
			}
			if (!majorVersion.startsWith("1.")) {
				if (Integer.parseInt(majorVersion) > 9) {
					this.getProject().setProperty("java.module.based", "true");
				}
			}
			String minJavaVersion = this.getProject().getProperty("minimum.java.version");
			if (minJavaVersion == null || minJavaVersion.isEmpty()) {
				PropertyHelper.setNewProperty(getProject(), "minimum.java.version", minJavaVersion = "" + majorVersion);
			}
			getProject().log(this, "minimum.java.version=" + minJavaVersion, Project.MSG_INFO);
			String srcJavaVersion = this.getProject().getProperty("source.java.version");
			if (srcJavaVersion == null || srcJavaVersion.isEmpty()) {
				PropertyHelper.setNewProperty(getProject(), "source.java.version", minJavaVersion);
				getProject().log(this, "source.java.version=" + minJavaVersion, Project.MSG_INFO);
			} else {
				getProject().log(this, "source.java.version=" + srcJavaVersion, Project.MSG_INFO);
			}
		}

		String sourceValue;

		String sep = AntUtil.getPropertyAsString(propertyHelper, "sep");
		if (sep == null || sep.isEmpty()) {
			sep = File.separator;
			sourceValue = " (auto-detect)";
		} else {
			sourceValue = null;
		}
		mySetProperty(-1, "sep", sourceValue, sep, true);

		String psep = AntUtil.getPropertyAsString(propertyHelper, "psep");
		if (psep == null || psep.isEmpty()) {
			psep = File.pathSeparator;
			sourceValue = " (auto-detect)";
		} else {
			sourceValue = null;
		}
		mySetProperty(-1, "psep", sourceValue, psep, true);

		{
			String value = AntUtil.getPropertyAsString(propertyHelper, "qtjambi.autotests.jnicheck"); // used here
			if (value == null || value.isEmpty()) {
				value = System.getenv("QTJAMBI_JNICHECK");
				AntUtil.setProperty(propertyHelper, "qtjambi.autotests.jnicheck", Boolean.toString(value != null));
			}
			value = AntUtil.getPropertyAsString(propertyHelper, "qtjambi.autotests"); // used here
			if (value == null || value.isEmpty()) {
				value = AntUtil.getPropertyAsString(propertyHelper, "QTJAMBI_TEST");
			}
			if (value == null || value.isEmpty()) {
				value = System.getenv("QTJAMBI_TEST");
			}
			if (value != null && !value.isEmpty()) {
				AntUtil.setProperty(propertyHelper, "qtjambi.autotests", value);
				getProject().log(this, "qtjambi.autotests=" + value, Project.MSG_INFO);
			}
		}

		String QTDIR = AntUtil.getPropertyAsString(propertyHelper, "QTDIR"); // used here
		if (QTDIR == null || QTDIR.isEmpty()) {
			QTDIR = AntUtil.getPropertyAsString(propertyHelper, "qtjambi.qtdir");
		}
		if (QTDIR == null || QTDIR.isEmpty()) {
			QTDIR = System.getenv("QTDIR");
		}
		if (QTDIR != null && !QTDIR.isEmpty()) {
			File qtdirDir = new File(QTDIR);
			if (qtdirDir.isDirectory()) {
				QTDIR = qtdirDir.getAbsolutePath();
				getProject().log(this, "QTDIR is set: " + prettyValue(QTDIR), Project.MSG_INFO);
			} else {
				QTDIR = null;
			}
		}
		if (QTDIR == null || QTDIR.isEmpty()) {
			String value = AntUtil.getPropertyAsString(propertyHelper, Constants.BINDIR);
			if (value != null && !value.isEmpty()) {
				File binDir = new File(value);
				if (binDir.isDirectory()) {
					QTDIR = binDir.getParent();
				} else {
					throw new BuildException("No QTDIR specified.");
				}
			} else {
				throw new BuildException("No QTDIR specified.");
			}
		}
		if (QTDIR != null && !QTDIR.isEmpty()) {
			AntUtil.setProperty(propertyHelper, "QTDIR", QTDIR);
			AntUtil.setProperty(propertyHelper, "qtjambi.qtdir", QTDIR);
		}

		final String[] emitA = { Constants.BINDIR, Constants.LIBDIR, Constants.LIBEXECDIR, Constants.INCLUDEDIR,
				Constants.PLUGINSDIR, Constants.IMPORTSDIR, Constants.QMLMODULESDIR, Constants.TRANSLATIONSDIR,
				Constants.RESOURCESDIR };
		for (String emit : emitA) {
			sourceValue = null;
			String value = AntUtil.getPropertyAsString(propertyHelper, emit);
			File dir = null;
			if (value != null && !value.isEmpty()) {
				dir = new File(value);
			} else if ((value == null || value.isEmpty()) && QTDIR != null) {
				dir = resolveDirectoryViaQTDIR(QTDIR, emit);
				if (dir != null) {
					value = dir.getAbsolutePath();
					sourceValue = " (from envvar:QTDIR)";
				}
			}
			if (dir != null) {
				if (dir.isDirectory() == false)
					sourceValue = " (WARNING: path does not exist or is not a directory)";
			}
			mySetProperty(-1, emit, sourceValue, value, true);
		}

		{
			sourceValue = null;
			String qtQmake = AntUtil.getPropertyAsString(propertyHelper, Constants.QMAKE);
			String detectedQmake = QMakeTask.executableName();
			if (qtQmake == null || qtQmake.isEmpty()) {
				qtQmake = detectedQmake;
				sourceValue = " (default)";
			} else if (!qtQmake.equals(detectedQmake)) {
				sourceValue = " (WARNING expected " + detectedQmake + ")";
			}
			mySetProperty(-1, Constants.QMAKE, sourceValue, qtQmake, true);

			String detectedQmakeAbsolutePath = QMakeTask.resolveExecutableAbsolutePath(getProject(), qtQmake);

			String qtQmakeAbspath = AntUtil.getPropertyAsString(propertyHelper, Constants.QMAKE_ABSPATH);
			if (qtQmakeAbspath == null || qtQmakeAbspath.isEmpty()) {
				qtQmakeAbspath = detectedQmakeAbsolutePath;
				if (qtQmakeAbspath != null)
					sourceValue = " (auto-detected)";
				else
					sourceValue = " (WARNING not found)";
			} else {
				if (qtQmakeAbspath.equals(detectedQmakeAbsolutePath) == false)
					sourceValue = " (WARNING detected at " + detectedQmakeAbsolutePath + ")";
			}
			mySetProperty(-1, Constants.QMAKE_ABSPATH, sourceValue, qtQmakeAbspath, true);
		}
		if (!decideQtVersion())
			throw new BuildException("Unable to determine Qt version.");
		if (!decideQMAKE_XSPEC())
			throw new BuildException("Unable to determine QMAKE_XSPEC");
		String s;
		analyzeLibinfix(new File(QTDIR));

		String qtVersionShort = String.format("%1$s.%2$s", qtMajorVersion, qtMinorVersion);
		InputStream inStream = null;
		try {
			inStream = new FileInputStream("releases.properties");
			Properties releases = new Properties();
			releases.load(inStream);
			String value = releases.getProperty(qtVersionShort, "0");
			try {
				qtJambiVersion = Integer.parseInt(value);
			} catch (NumberFormatException e) {
				qtJambiVersion = 0;
			}
		} catch (FileNotFoundException e) {
			// Acceptable failure
		} catch (IOException e) {
			throw new BuildException(e);
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					throw new BuildException(e);
				}
			}
		}
		sourceValue = " (auto-detected)";
		mySetProperty(-1, "qtjambi.patchversion", sourceValue, "" + qtJambiVersion, true); // report value
		String qtjambiFullVersion = qtVersionShort + "." + qtJambiVersion;
		mySetProperty(-1, "qtjambi.jar.version", sourceValue, qtjambiFullVersion, true); // report
																												// value

		FindCompiler finder = new FindCompiler(this.propertyHelper, this);

		String detectedOsname = OSInfo.crossOSArchName();
		String osname = AntUtil.getPropertyAsString(propertyHelper, Constants.OSNAME);
		if (osname == null || osname.isEmpty()) {
			sourceValue = " (auto-detected)";
			osname = detectedOsname;
			if("android".equals(detectedOsname)) {
				String androidSpec = new java.io.File(QTDIR).getName();
				switch(androidSpec) {
				case "android_arm64_v8a":
					osname += "-arm64";
					OSInfo.setCrossOSArchName(osname);
					break;
				case "android_armv7":
					osname += "-arm";
					OSInfo.setCrossOSArchName(osname);
					break;
				case "android_x86":
					osname += "-x86";
					OSInfo.setCrossOSArchName(osname);
					break;
				case "android_x86_64":
					osname += "-x64";
					OSInfo.setCrossOSArchName(osname);
					break;
				}
			}else {
				OSInfo.setCrossOSArchName(null);
			}
		} else {
			if ("help".equals(osname) || "?".equals(osname)) {
				OSInfo.OS[] values = OSInfo.OS.values();
				s = Arrays.toString(values);
				throw new BuildException(Constants.OSNAME + " valid values: " + s);
			}
			sourceValue = " (detected: " + detectedOsname + ")";
		}
		mySetProperty(-1, Constants.OSNAME, sourceValue, osname, true); // report value

		Compiler compiler;
		{
			Compiler detectedCompiler = null;
			String compilerError = null;
			try {
				detectedCompiler = finder.decideCompiler();
			} catch (BuildException e) {
				compilerError = e.getMessage();
			}
			String compilerValue = AntUtil.getPropertyAsString(propertyHelper, Constants.COMPILER);
			if (compilerValue == null || compilerValue.isEmpty()) {
				sourceValue = " (available compilers: " + finder.getAvailableCompilers() + "; auto-detected)";
				compilerValue = (detectedCompiler==null ? "" : detectedCompiler.toString());
				compiler = detectedCompiler;
			} else {
				if ("help".equals(compilerValue) || "?".equals(compilerValue)) {
					Compiler[] values = Compiler.values();
					s = Arrays.toString(values);
					throw new BuildException(Constants.COMPILER + " valid values: " + s);
				}
				compiler = Compiler.resolve(compilerValue);
				sourceValue = " (available compilers: " + finder.getAvailableCompilers() + "; detected: "
						+ (detectedCompiler==null ? "none" : detectedCompiler) + ")";
			}
			isMinGW = compiler==Compiler.MinGW || compiler==Compiler.MinGW_W64;
			mySetProperty(-1, Constants.COMPILER, sourceValue, compilerValue, true); // report value
			if(compilerError!=null)
				mySetProperty(-1, "qtjambi.compiler.error", sourceValue, compilerError, true);
		}

		String vsredistdirValue = AntUtil.getPropertyAsString(propertyHelper, Constants.VSREDISTDIR);
		if (vsredistdirValue != null && !vsredistdirValue.isEmpty()) {
			sourceValue = null; // " (environment variable $" + "VSREDISTDIR" + ")"; // not strictly true it
								// could be set by ant cmdline
			mySetProperty(-1, Constants.VSREDISTDIR_PACKAGE, sourceValue, vsredistdirValue, true); // report value

			String vsredistdirPackageValue = AntUtil.getPropertyAsString(propertyHelper, Constants.VSREDISTDIR_PACKAGE);
			if (vsredistdirPackageValue == null) {
				sourceValue = " (default value; feature need explicit enabling)";
				vsredistdirPackageValue = "false";
			} else {
				sourceValue = null;
			}
			mySetProperty(-1, Constants.VSREDISTDIR_PACKAGE, sourceValue, vsredistdirPackageValue, true); // report
																											// value
		} else {
			mySetProperty(0, Constants.VSREDISTDIR_PACKAGE, null, "false", true); // silently set false
		}

		String CROSS_COMPILE = System.getenv("CROSS_COMPILE"); // used here
		if (CROSS_COMPILE != null && !CROSS_COMPILE.isEmpty())
			getProject().log(this, "CROSS_COMPILE is set: " + prettyValue(CROSS_COMPILE), Project.MSG_INFO);

		s = null;
		switch (OSInfo.os()) {
		case Linux:
			s = OSInfo.K_LINUX;
			break;
		case Windows:
			s = OSInfo.K_WINDOWS;
			break;
		case Android:
			s = OSInfo.K_ANDROID;
			break;
		case MacOS:
			s = OSInfo.K_MACOS;
			mySetProperty(0, Constants.QTJAMBI_CONFIG_ISMACOSX, " (set by init)", "true", true);
			if (qtMajorVersion >= 6 && qtMinorVersion >= 2
					&& AntUtil.getProperty(propertyHelper, "qtjambi.pluginloader.build.from.source") == null) {
				AntUtil.setProperty(propertyHelper, "qtjambi.pluginloader.build.from.source", "true");
			}
			break;
		default:
			break;
		}
		if (s != null)
			AntUtil.setProperty(propertyHelper, Constants.OSPLATFORM, s);

		s = null;

		{
			Properties props = System.getProperties();
			Enumeration<?> e = props.propertyNames();
			while (e.hasMoreElements()) {
				String k = (String) e.nextElement();
				Object v = System.getProperty(k);
				getProject().log("systemProperty[" + k + "] = " + v, Project.MSG_VERBOSE);
			}
		}

		String javaOsArch = System.getProperty("os.arch"); // arm|
		if ("arm".equals(javaOsArch)) {
			// FIXME get LE or BE
			s = "arm";
		} else {
			if (osname.endsWith("64"))
				s = "x86_64";
			else
				s = "i386";
		}
		if (s != null)
			AntUtil.setProperty(propertyHelper, Constants.OSCPU, s);

		finder.checkCompilerDetails();

		mySetProperty(-1, Constants.EXEC_STRIP, null, null, true); // report value

		String javaHomeTarget = decideJavaHomeTarget();
		if (javaHomeTarget == null)
			throw new BuildException(
					"Unable to determine JAVA_HOME_TARGET, setup environment variable JAVA_HOME (or JAVA_HOME_TARGET) or edit build.properties");

		String javaOsarchTarget = decideJavaOsarchTarget();
		if (javaOsarchTarget == null) {
			if (OSInfo.os() != OSInfo.OS.MacOS) // On MacOSX there is no sub-dir inside the JDK include directory that
												// contains jni.h
				throw new BuildException(
						"Unable to determine JAVA_OSARCH_TARGET, setup environment variable JAVA_OSARCH_TARGET or edit build.properties");
		}

		String javaOscpu = decideJavaOscpu();
		if (javaOscpu == null)
			throw new BuildException(
					"Unable to determine JAVA_OSCPU, setup environment variable JAVA_OSCPU or edit build.properties");

		String javaOscpuTarget = decideJavaOscpuTarget();
		if (javaOscpuTarget == null)
			throw new BuildException(
					"Unable to determine JAVA_OSCPU_TARGET, setup environment variable JAVA_OSCPU_TARGET or edit build.properties");

		String generatorQtDir = AntUtil.getPropertyAsString(propertyHelper, "tools.qtdir");
		if (generatorQtDir != null && !generatorQtDir.isEmpty()) {
			final String[] emitB = { Constants.TOOLS_BINDIR, Constants.TOOLS_LIBDIR };
			for (String emit : emitB) {
				sourceValue = null;
				String value = AntUtil.getPropertyAsString(propertyHelper, emit);
				File dir = null;
				if (value != null && !value.isEmpty()) {
					dir = new File(value);
				} else if ((value == null || value.isEmpty()) && generatorQtDir != null) {
					dir = resolveDirectoryViaQTDIR(generatorQtDir, emit);
					if (dir != null) {
						value = dir.getAbsolutePath();
						sourceValue = " (from qtjambi.generator.qtdir)";
					}
				}
				if (dir != null) {
					if (dir.isDirectory() == false)
						sourceValue = " (WARNING: path does not exist or is not a directory)";
				}
				mySetProperty(-1, emit, sourceValue, value, true);
			}
		} else {
			{
				sourceValue = null;
				String toolsBindir = AntUtil.getPropertyAsString(propertyHelper, Constants.TOOLS_BINDIR);
				if (toolsBindir == null || toolsBindir.isEmpty()) {
					String bindir = AntUtil.getPropertyAsString(propertyHelper, Constants.BINDIR);
					if (bindir != null && !bindir.isEmpty()) {
						sourceValue = " (inherited from ${" + Constants.BINDIR + "})";
						toolsBindir = bindir;
					}
				}
				mySetProperty(-1, Constants.TOOLS_BINDIR, sourceValue, toolsBindir, true); // report value
			}
			{
				sourceValue = null;
				String toolsLibdir = AntUtil.getPropertyAsString(propertyHelper, Constants.TOOLS_LIBDIR);
				if (toolsLibdir == null || toolsLibdir.isEmpty()) {
					String libdir = AntUtil.getPropertyAsString(propertyHelper, Constants.LIBDIR);
					if (libdir != null && !libdir.isEmpty()) {
						sourceValue = " (inherited from ${" + Constants.LIBDIR + "})";
						toolsLibdir = libdir;
					}
				}
				mySetProperty(-1, Constants.TOOLS_LIBDIR, sourceValue, toolsLibdir, true); // report value
			}
		}
		{
			sourceValue = null;
			String toolsQmake = AntUtil.getPropertyAsString(propertyHelper, Constants.TOOLS_QMAKE);
			String binDirProp = Constants.TOOLS_BINDIR;
			if (toolsQmake == null || toolsQmake.isEmpty()) {
				String qmake = AntUtil.getPropertyAsString(propertyHelper, Constants.QMAKE);
				binDirProp = Constants.BINDIR;
				sourceValue = " (inherited from ${" + Constants.QMAKE + "})";
				toolsQmake = qmake;
			}
			mySetProperty(-1, Constants.TOOLS_QMAKE, sourceValue, toolsQmake, true); // report value

			// This method needs to look in Constants.TOOLS_BINDIR not Constants.BINDIR

			String toolsQmakeAbspath = AntUtil.getPropertyAsString(propertyHelper, Constants.TOOLS_QMAKE_ABSPATH);
			if (toolsQmakeAbspath == null || toolsQmakeAbspath.isEmpty()) {
				toolsQmakeAbspath = QMakeTask.resolveExecutableAbsolutePath(getProject(), toolsQmake, binDirProp);
				if (toolsQmakeAbspath != null)
					sourceValue = " (auto-detected)";
				else
					sourceValue = " (WARNING not found)";
			} else {
				try {
					String detectedQmakeAbsolutePath = QMakeTask.resolveExecutableAbsolutePath(getProject(), toolsQmake, binDirProp);
					if (!toolsQmakeAbspath.equals(detectedQmakeAbsolutePath))
						sourceValue = " (WARNING detected at " + detectedQmakeAbsolutePath + ")";
				} catch (Throwable e) {
				}
			}
			mySetProperty(-1, Constants.TOOLS_QMAKE_ABSPATH, sourceValue, toolsQmakeAbspath, true);
		}

		List<String> privateModules = new ArrayList<>();
		privateModules.add("QtCore");
		privateModules.add("QtGui");
		privateModules.add("QtDesigner");
		privateModules.add("QtRemoteObjects");
		privateModules.add("QtQuick");
		{
			String pm = AntUtil.getPropertyAsString(propertyHelper, Constants.PRIVATE_MODULES);
			if(pm!=null)
				privateModules.addAll(Arrays.asList(pm.split(",")));
		}
		if (OSInfo.os() == OSInfo.OS.MacOS) {
			String wantedSdk = AntUtil.getPropertyAsString(propertyHelper, Constants.QTJAMBI_MACOSX_MAC_SDK);
			detectMacosxSdk(wantedSdk);
		}
		{
			List<String> privateIncludes = new ArrayList<>();
			String generatorIncludepaths = AntUtil.getPropertyAsString(propertyHelper,
					Constants.GENERATOR_INCLUDEPATHS);
			if (OSInfo.os() == OSInfo.OS.MacOS && useQtFramework) {
				String libdir = AntUtil.getPropertyAsString(propertyHelper, Constants.LIBDIR);
				for (File frameworkDir : new File(libdir).listFiles()) {
					if (frameworkDir.isDirectory() && frameworkDir.getName().endsWith(".framework")) {
						String libName = frameworkDir.getName();
						libName = libName.substring(0, libName.length() - 10);
						File headers = new File(frameworkDir, "/Versions/" + qtMajorVersion + "/Headers");
						if (!headers.isDirectory())
							headers = new File(frameworkDir, "/Versions/A/Headers");
						if (!headers.isDirectory())
							headers = new File(frameworkDir, "/Versions/Current/Headers");
						if (!headers.isDirectory())
							headers = new File(frameworkDir, "/Headers");
						if (privateModules.contains(libName) && !privateIncludes.contains(headers.getAbsolutePath()
								+ "/" + qtMajorVersion + "." + qtMinorVersion + "." + qtPatchlevelVersion)) {
							privateIncludes.add(headers.getAbsolutePath() + "/" + qtMajorVersion + "." + qtMinorVersion
									+ "." + qtPatchlevelVersion);
						}
					}
				}
			} else {
				String includedir = AntUtil.getPropertyAsString(propertyHelper, Constants.INCLUDEDIR);
				for (String privateModule : privateModules) {
					if (!privateIncludes.contains(new File(new File(includedir),
							privateModule + "/" + qtMajorVersion + "." + qtMinorVersion + "." + qtPatchlevelVersion)
									.getAbsolutePath()))
						privateIncludes.add(new File(new File(includedir),
								privateModule + "/" + qtMajorVersion + "." + qtMinorVersion + "." + qtPatchlevelVersion)
										.getAbsolutePath());
				}
			}
			if (generatorIncludepaths != null) {
				for (String path : generatorIncludepaths
						.split("[;" + (File.pathSeparatorChar == ';' ? "" : File.pathSeparator) + "]")) {
					if (!privateIncludes.contains(path))
						privateIncludes.add(path);
				}
			}
			AntUtil.setProperty(propertyHelper, Constants.GENERATOR_INCLUDEPATHS,
					String.join(File.pathSeparator, privateIncludes));
			getProject().log(this,
					Constants.GENERATOR_INCLUDEPATHS + ": " + String.join(File.pathSeparator, privateIncludes),
					Project.MSG_INFO);
		}
		if (qtMajorVersion >= 5) {
			AntUtil.setProperty(propertyHelper, "qt.feature.QT_NO_STYLE_PLASTIQUE", "true");
		}

		s = String.valueOf(qtVersion);
		getProject().log(this, Constants.QT_VERSION + " is " + s + qtVersionSource, Project.MSG_VERBOSE);
		AntUtil.setProperty(propertyHelper, Constants.QT_VERSION, s);
		AntUtil.setProperty(propertyHelper, Constants.VERSION, s); // this won't overwrite existing value
		{
			File tryPath = new File(new File(new File(QTDIR).getParentFile().getParentFile(), "Docs"), "Qt-" + s);
			if(tryPath.isDirectory())
				mySetProperty(-1, Constants.DOCSDIR, " (auto-detected)", tryPath.getAbsolutePath(), true);
		}

		s = String.valueOf(qtMajorVersion);
		getProject().log(this, Constants.QT_VERSION_MAJOR + " is " + s, Project.MSG_VERBOSE);
		AntUtil.setProperty(propertyHelper, "qtjambi.isqt" + s, "true");
		AntUtil.setProperty(propertyHelper, Constants.QT_VERSION_MAJOR, s);
		AntUtil.setProperty(propertyHelper, Constants.QT_VERSION_MAJOR_NEXT, String.valueOf(qtMajorVersion + 1));
		AntUtil.setProperty(propertyHelper, Constants.QT_VERSION_MINOR, String.valueOf(qtMinorVersion));
		AntUtil.setProperty(propertyHelper, Constants.QT_VERSION_MINOR_NEXT, String.valueOf(qtMinorVersion + 1));
		AntUtil.setProperty(propertyHelper, Constants.QT_VERSION_PATCHLEVEL, String.valueOf(qtPatchlevelVersion));
		decideAlternativeJavaHomesTarget();

		versionSuffix = AntUtil.getPropertyAsString(propertyHelper, Constants.SUFFIX_VERSION);
		mySetProperty(-1, Constants.SUFFIX_VERSION, null, null, true); // report

		String canonVersionSuffix;
		if (versionSuffix != null)
			canonVersionSuffix = versionSuffix;
		else
			canonVersionSuffix = "";
		String bundleVersionMode = AntUtil.getPropertyAsString(propertyHelper, Constants.BUNDLE_VERSION_MODE);
		if (bundleVersionMode != null) {
			if (bundleVersionMode.equals("auto-suffix-date")) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				s = qtVersion + "." + sdf.format(new Date()) + canonVersionSuffix;
			}
		} else {
			s = qtVersion + canonVersionSuffix;
		}
		mySetProperty(-1, Constants.BUNDLE_VERSION, null, s, true);

		{ // Need to detect the version of Qt we a reworking with before deciding
			// configuration
			String wantedConfiguration = AntUtil.getPropertyAsString(propertyHelper, Constants.CONFIGURATION);
			detectConfiguration(wantedConfiguration); // all the magic in in here now
		}

		// Moved until after auto-detect of configuration is complete
		s = null;
		if (Constants.CONFIG_RELEASE.equals(getConfiguration()))
			s = ""; // empty
		else if (Constants.CONFIG_DEBUG.equals(getConfiguration()))
			s = "-debug";
		else if (Constants.CONFIG_TEST.equals(getConfiguration()))
			s = "-test";
		else
			s = ""; // empty (debug_and_release)
		if (s != null)
			AntUtil.setProperty(propertyHelper, Constants.CONFIGURATION_DASH, s);
		s = null;
		if (Constants.CONFIG_RELEASE.equals(getConfiguration()))
			s = ""; // empty
		else if (Constants.CONFIG_DEBUG.equals(getConfiguration()))
			s = ".debug";
		else if (Constants.CONFIG_TEST.equals(getConfiguration()))
			s = ".test";
		else
			s = ""; // empty (debug_and_release)
		if (s != null)
			AntUtil.setProperty(propertyHelper, Constants.CONFIGURATION_OSGI, s);

		{
			sourceValue = null;
			String value = AntUtil.getPropertyAsString(propertyHelper, Constants.QTJAMBI_DEBUG_TOOLS);
			if (value == null) {
				if (isConfigurationDebug() || isConfigurationTest())
					value = Boolean.TRUE.toString();
				else
					value = Boolean.FALSE.toString();
				sourceValue = " (auto-configured based on ${" + Constants.CONFIGURATION + "})";
			}
			mySetProperty(-1, Constants.QTJAMBI_DEBUG_TOOLS, sourceValue, value, true);
		}

		{
			sourceValue = null;
			String qmakeTargetDefault = AntUtil.getPropertyAsString(propertyHelper, Constants.QMAKE_TARGET_DEFAULT);
			if (qmakeTargetDefault == null) {
				// We only need to override the default when the Qt SDK is debug_and_release but
				// we are only building the project for one kind.
//              if(Constants.CONFIG_RELEASE.equals(configuration))
//                  qmakeTargetDefault = configuration;
//              else if(Constants.CONFIG_DEBUG.equals(configuration))
//                  qmakeTargetDefault = configuration;
				if (Constants.CONFIG_DEBUG_AND_RELEASE.equals(getConfiguration()))
					qmakeTargetDefault = "all";
				else
					qmakeTargetDefault = ""; // must be set to empty string (avoid ant subst ${qmake.target.default})
				// FIXME: We want ${qtjambi.configuration} to set from QTDIR build kind *.prl
				// data
//                sourceValue = " (set from ${qtjambi.configuration})";
			}
			mySetProperty(-1, Constants.QMAKE_TARGET_DEFAULT, sourceValue, qmakeTargetDefault, true); // report value
		}

		mySetProperty(-1, Constants.QTJAMBI_SONAME_VERSION_MAJOR, " (taken from qt version)", "" + qtMajorVersion,
				true);
		mySetProperty(-1, Constants.QTJAMBI_SONAME_VERSION_MINOR, " (taken from qt version)", "" + qtMinorVersion,
				true);

		String cachekeyVersion = AntUtil.getPropertyAsString(propertyHelper, Constants.CACHEKEY);
		String cachekeyVersionSource = " (already set)";
		if (cachekeyVersion == null || cachekeyVersion.isEmpty()) { // auto-configure
			cachekeyVersionSource = " (set by init)";
			// ${qtjambi.compiler}${qtjambi.configuration.dash}-${DSTAMP}-${TSTAMP}
			cachekeyVersion = propertyHelper.replaceProperties(null,
					"${qtjambi.compiler}${qtjambi.configuration.dash}-${DSTAMP}-${TSTAMP}", null);
		}
		mySetProperty(-1, Constants.CACHEKEY, cachekeyVersionSource, cachekeyVersion, true);

		String qtjambiQtLibdir = AntUtil.getPropertyAsString(propertyHelper, Constants.LIBDIR);
		if (qtjambiQtLibdir != null && !qtjambiQtLibdir.isEmpty()) {
			sourceValue = null;
			s = null;
//            s = AntUtil.getPropertyAsString(propertyHelper, Constants.QTJAMBI_MACOSX_QTMENUNIB_DIR);
			if (OSInfo.os() == OSInfo.OS.MacOS) {
				s = doesQtLibExistDir(qtjambiQtLibdir, "Resources/qt_menu.nib");
				if (s == null)
					s = doesQtLibExistDir(qtjambiQtLibdir, "qt_menu.nib");
				if (s == null)
					s = doesQtLibExistDir(qtjambiQtLibdir, "../src/gui/mac/qt_menu.nib");
				if (s == null)
					s = doesQtLibExistDir(qtjambiQtLibdir, "../qtbase/src/plugins/platforms/cocoa/qt_menu.nib");
				// FIXME: auto-detect, directory from source, directory from QtSDK on MacOSX,
				// directory from framework on MacOSX

				if (s != null)
					sourceValue = " (auto-detected)";
			}
			if (s == null) {
				if (OSInfo.os() == OSInfo.OS.MacOS)
					sourceValue = " (WARNING you should resolve this for targetting MacOSX)";
				else
					sourceValue = " (expected for non-MacOSX platform)";
				s = "";
			}
			mySetProperty(-1, Constants.QTJAMBI_MACOSX_QTMENUNIB_DIR, sourceValue, s, true);
		}
		String qtmenudir = AntUtil.getPropertyAsString(propertyHelper, Constants.QTJAMBI_MACOSX_QTMENUNIB_DIR);
		if (qtmenudir == null || qtmenudir.isEmpty())
			AntUtil.setProperty(propertyHelper, Constants.QTJAMBI_MACOSX_QTMENUNIB_DIR, "", false);

		// The 'qreal' Qt C++ type is 'float' on ARM platforms and 'double' on all
		// others
		String qrealtype = "double";
		String qrealSource = " (the default)";
		if ("arm".equals(javaOsArch)) {
			qrealtype = "float";
			qrealSource = " (${" + Constants.OSCPU + "} is \"arm\")";
		}
		mySetProperty(-1, Constants.QTJAMBI_QREALTYPE, qrealSource, qrealtype, true);

		if (detectQtDsoExistAndSetProperty("qtjambi.core", "QtCore", libInfix, qtMajorVersion, qtMinorVersion, qtPatchlevelVersion, null)) {
			Set<String> availableModules = new TreeSet<>();
			for (String module : modules.keySet()) {
				if (detectQtDsoExistAndSetProperty("qtjambi." + module, modules.get(module).libraryName, libInfix,
						qtMajorVersion, qtMinorVersion, qtPatchlevelVersion, module))
					availableModules.add(module);
			}

			AntUtil.setProperty(propertyHelper, Constants.PACKAGING_DSO_LIBSTDC___6,
					decideQtBinDso(Constants.PACKAGING_DSO_LIBSTDC___6, "libstdc++-6", null, null, null));
			AntUtil.setProperty(propertyHelper, Constants.PACKAGING_DSO_LIBGCC_S_SEH_1,
					decideQtBinDso(Constants.PACKAGING_DSO_LIBGCC_S_SEH_1, "libgcc_s_seh-1", null, null, null));
			AntUtil.setProperty(propertyHelper, Constants.PACKAGING_DSO_WINPTHREAD,
					decideQtBinDso(Constants.PACKAGING_DSO_WINPTHREAD, "libwinpthread-1", null, null, null));

			String packagingDsoLibeay32 = decideQtBinDso(Constants.PACKAGING_DSO_LIBEAY32, "libeay32", null, null,
					false);
			mySetProperty(-1, Constants.PACKAGING_DSO_LIBEAY32, null, packagingDsoLibeay32, true);

			String packagingDsoLibssl32 = decideQtBinDso(Constants.PACKAGING_DSO_LIBSSL32, "libssl32", null, null,
					false);
			String packagingDsoSsleay32 = decideQtBinDso(Constants.PACKAGING_DSO_SSLEAY32, "ssleay32", null, null,
					false);
			// When building QtJambi against the official Nokia Qt SDK they appear to
			// provide duplicate
			// DLLs for the two naming variants libssl32.dll ssleay32.dll so we need to
			// resolve this and
			// omit one.
			String packagingDsoLibssl32Message = "";
			String packagingDsoSsleay32Message = "";
			// "true" or a path, also means true. Only "false" means false.
			if (("false".equals(packagingDsoLibssl32) == false && packagingDsoLibssl32 != null)
					&& ("false".equals(packagingDsoSsleay32) == false && packagingDsoSsleay32 != null)) {
				// FIXME: Compare the files are actually the same
				if (compiler == Compiler.GCC || compiler == Compiler.OldGCC || compiler == Compiler.MinGW
						|| compiler == Compiler.MinGW_W64) {
					packagingDsoSsleay32Message = " (was " + packagingDsoSsleay32 + "; auto-inhibited)";
					packagingDsoSsleay32 = "false"; // favour libssl32.dll
				} else {
					packagingDsoLibssl32Message = " (was " + packagingDsoLibssl32 + "; auto-inhibited)";
					packagingDsoLibssl32 = "false"; // favour ssleay32.dll
				}
			}
			AntUtil.setProperty(propertyHelper, Constants.PACKAGING_DSO_LIBSSL32, packagingDsoLibssl32);
			if ((packagingDsoLibssl32Message.length() > 0
					|| ("false".equals(packagingDsoLibssl32) == false) && packagingDsoLibssl32 != null))
				getProject().log(this,
						Constants.PACKAGING_DSO_LIBSSL32 + ": " + packagingDsoLibssl32 + packagingDsoLibssl32Message,
						Project.MSG_INFO);

			AntUtil.setProperty(propertyHelper, Constants.PACKAGING_DSO_SSLEAY32, packagingDsoSsleay32);
			if ((packagingDsoSsleay32Message.length() > 0
					|| ("false".equals(packagingDsoSsleay32) == false) && packagingDsoSsleay32 != null))
				getProject().log(this,
						Constants.PACKAGING_DSO_SSLEAY32 + ": " + packagingDsoSsleay32 + packagingDsoSsleay32Message,
						Project.MSG_INFO);

			if (OSInfo.os() == OSInfo.OS.Windows) {
				String packagingDsoZlib1 = decideQtBinDso(Constants.PACKAGING_DSO_ZLIB1, "zlib1", null, null, false);
				mySetProperty(-1, Constants.PACKAGING_DSO_ZLIB1, null, packagingDsoZlib1, true);
			} else {
				// If the lib directory contains "libz.so.1" or "libssl.so" or
				// "libcrypto.so.1.0.0"
				sourceValue = null;
				String packagingDsoLibssl = decideQtBinDso(Constants.PACKAGING_DSO_LIBSSL, "ssl", null,
						new int[][] { null, { 1, -1, -1 }, { 0, -1, -1 } }, false);
				if (packagingDsoLibssl != null && QTDIR != null && packagingDsoLibssl.startsWith(QTDIR) == false) {
					sourceValue = " (detected: " + packagingDsoLibssl + "; but inhibited as not inside QTDIR)";
					packagingDsoLibssl = null;
				}
				mySetProperty(-1, Constants.PACKAGING_DSO_LIBSSL, sourceValue, packagingDsoLibssl, true);

				// FIXME: Implement file globs and reverse sort
				sourceValue = null;
				String packagingDsoLibcrypto = decideQtBinDso(Constants.PACKAGING_DSO_LIBCRYPTO, "crypto", null,
						new int[][] { /* "1.0.0h", "1.0.0g", */ { 1, 0, 0 }, { 0, 0, 0 }, null, { 10, -1, -1 } },
						false);
				if (packagingDsoLibcrypto != null && QTDIR != null
						&& packagingDsoLibcrypto.startsWith(QTDIR) == false) {
					sourceValue = " (detected: " + packagingDsoLibcrypto + "; but inhibited as not inside QTDIR)";
					packagingDsoLibcrypto = null;
				}
				mySetProperty(-1, Constants.PACKAGING_DSO_LIBCRYPTO, sourceValue, packagingDsoLibcrypto, true);

				sourceValue = null;
				String packagingDsoLibz = decideQtBinDso(Constants.PACKAGING_DSO_LIBZ, "z", null,
						new int[][] { { 1, -1, -1 }, null }, false);
				if (packagingDsoLibz != null && QTDIR != null && packagingDsoLibz.startsWith(QTDIR) == false) {
					sourceValue = " (detected: " + packagingDsoLibz + "; but inhibited as not inside QTDIR)";
					packagingDsoLibz = null;
				}
				mySetProperty(-1, Constants.PACKAGING_DSO_LIBZ, sourceValue, packagingDsoLibz, true);
			}

			// FIXME: On Macosx when we build and have qtjambi.dbus==true we should WARN
			// when we can not locate libdbus-1.*.dylib
			// FIXME: On Macosx we should also search /usr/local/lib
			String packagingDsoLibdbus = decideQtBinDso(Constants.PACKAGING_DSO_LIBDBUS, "dbus-1", null,
					new int[][] { { 3, -1, -1 }, { 2, -1, -1 }, null }, null);
			AntUtil.setProperty(propertyHelper, Constants.PACKAGING_DSO_LIBDBUS, packagingDsoLibdbus);
		}else {
			String filename;
			if (useQtFramework) {
				filename = CreateNativeDeploymentTask.formatQtNameAsFramework("QtCore", null, false, qtMajorVersion, qtMinorVersion, qtPatchlevelVersion);
			} else {
				filename = CreateNativeDeploymentTask.formatQtName("QtCore", null, false, qtMajorVersion, qtMinorVersion, qtPatchlevelVersion, false);
			}
			throw new BuildException("Unable to detect "+filename+" library.");
		}
		// Other build information sanity testing and warning

		String generatorIncludepaths = AntUtil.getPropertyAsString(propertyHelper, Constants.GENERATOR_INCLUDEPATHS);
		if (generatorIncludepaths != null) {
			// Validate the settings
			sourceValue = null;
			mySetProperty(-1, Constants.GENERATOR_INCLUDEPATHS, sourceValue, generatorIncludepaths, true);

			prettifyPathSeparator(generatorIncludepaths, true);
		}
		// On Windows it is usual to configure the compile include directory via envvar
		String INCLUDE = System.getenv("INCLUDE");
		if (INCLUDE != null) {
			getProject().log(this, "INCLUDE is set: " + INCLUDE, Project.MSG_INFO);
			prettifyPathSeparator(INCLUDE, false);
		}

		switch(OSInfo.os()) {
			case Linux: { // Check we have libQtCore.so.4 in one of the paths in LD_LIBRARY_PATH
				String LD_LIBRARY_PATH = System.getenv("LD_LIBRARY_PATH");
				getProject().log(this,
						"LD_LIBRARY_PATH is set: " + ((LD_LIBRARY_PATH == null) ? "<notset>" : LD_LIBRARY_PATH),
						Project.MSG_INFO);
				if (LD_LIBRARY_PATH != null) {
					String[] sA = LD_LIBRARY_PATH.split(File.pathSeparator); // should regex escape it
					String filename = CreateNativeDeploymentTask.formatQtName("QtCore", libInfix, isConfigurationDebug(), qtMajorVersion,
							qtMinorVersion, qtPatchlevelVersion);
					int found = 0;
					for (String element : sA) {
						File testDir = new File(element);
						if (testDir.isDirectory() == false)
							getProject().log(this, " WARNING: LD_LIBRARY_PATH directory does not exit: " + element,
									Project.MSG_INFO);
						File testFile = new File(element, filename);
						if (testFile.isFile()) {
							getProject().log(this,
									"   FOUND: LD_LIBRARY_PATH directory contains QtCore: " + testFile.getAbsolutePath(),
									Project.MSG_INFO);
							found++;
						}
					}
	
					// FIXME: Refactor this duplicate code later (we look for !debug here but don't
					// WARNING is we dont find it)
					filename = CreateNativeDeploymentTask.formatQtName("QtCore", libInfix, !isConfigurationDebug(), qtMajorVersion,
							qtMinorVersion, qtPatchlevelVersion);
					found = 0;
					for (String element : sA) {
						File testDir = new File(element);
						// we already warned about non-existing directory here
						File testFile = new File(testDir, filename);
						if (testFile.isFile()) {
							getProject().log(this,
									"  XFOUND: LD_LIBRARY_PATH directory contains QtCore: " + testFile.getAbsolutePath(),
									Project.MSG_INFO);
							found++;
						}
					}
				} else { // Maybe we should check to see if (QTDIR != null) before warning
					getProject().log(this,
							" WARNING: LD_LIBRARY_PATH environment variable is not set; this is usually needed to allow 'generator' and 'juic' executables to run during the build",
							Project.MSG_INFO);
				}
			}
			break;
			case Windows: { // Check we have QtCore4.dll/Qt5Core.dll in one of the paths in PATH
				String PATH = System.getenv("PATH");
				getProject().log(this, "PATH is set: " + ((PATH == null) ? "<notset>" : PATH), Project.MSG_INFO);
				if (PATH != null) {
					String[] sA = PATH.split(File.pathSeparator); // should regex escape it
					String filename = CreateNativeDeploymentTask.formatQtName("QtCore", libInfix, isConfigurationDebug(), qtMajorVersion,
							qtMinorVersion, qtPatchlevelVersion);
					int found = 0;
					for (String element : sA) {
						File testDir = new File(element);
						if (testDir.isDirectory() == false)
							getProject().log(this, " WARNING: PATH directory does not exit: " + element, Project.MSG_INFO);
						File testFile = new File(element, filename);
						if (testFile.isFile()) {
							getProject().log(this,
									"   FOUND: PATH directory contains QtCore: " + testFile.getAbsolutePath(),
									Project.MSG_INFO);
							found++;
						}
					}
	
					// FIXME: Refactor this duplicate code later (we look for !debug here but don't
					// WARNING is we dont find it)
					filename = CreateNativeDeploymentTask.formatQtName("QtCore", libInfix, !isConfigurationDebug(), qtMajorVersion,
							qtMinorVersion, qtPatchlevelVersion);
					found = 0;
					for (String element : sA) {
						File testDir = new File(element);
						// we already warned about non-existing directory here
						File testFile = new File(testDir, filename);
						if (testFile.isFile()) {
							getProject().log(this,
									"  XFOUND: PATH directory contains QtCore: " + testFile.getAbsolutePath(),
									Project.MSG_INFO);
							found++;
						}
					}
				}
			}
			break;
			case MacOS: { // Check we have libQtCore.4.dylib in one of the paths in
													// DYLD_LIBRARY_PATH
				String DYLD_LIBRARY_PATH = System.getenv("DYLD_LIBRARY_PATH");
				getProject().log(this,
						"DYLD_LIBRARY_PATH is set: " + ((DYLD_LIBRARY_PATH == null) ? "<notset>" : DYLD_LIBRARY_PATH),
						Project.MSG_INFO);
				if (DYLD_LIBRARY_PATH != null) {
					String[] sA = DYLD_LIBRARY_PATH.split(File.pathSeparator); // should regex escape it
					String filename;
					if (useQtFramework) {
						filename = CreateNativeDeploymentTask.formatQtNameAsFramework("QtCore", libInfix, isConfigurationDebug(),
								qtMajorVersion, qtMinorVersion, qtPatchlevelVersion);
					} else {
						filename = CreateNativeDeploymentTask.formatQtName("QtCore", libInfix, isConfigurationDebug(), qtMajorVersion,
								qtMinorVersion, qtPatchlevelVersion);
					}
					int found = 0;
					for (String element : sA) {
						File testDir = new File(element);
						File testFile = new File(element, filename);
						if (testFile.isFile()) {
							getProject().log(this,
									"   FOUND: DYLD_LIBRARY_PATH directory contains QtCore: " + testFile.getAbsolutePath(),
									Project.MSG_INFO);
							found++;
						}
					}
	
					// FIXME: Refactor this duplicate code later (we look for !debug here but don't
					// WARNING is we dont find it)
					if (useQtFramework) {
						filename = CreateNativeDeploymentTask.formatQtNameAsFramework("QtCore", libInfix, !isConfigurationDebug(),
								qtMajorVersion, qtMinorVersion, qtPatchlevelVersion);
					} else {
						filename = CreateNativeDeploymentTask.formatQtName("QtCore", libInfix, !isConfigurationDebug(), qtMajorVersion,
								qtMinorVersion, qtPatchlevelVersion);
					}
					found = 0;
					for (String element : sA) {
						File testDir = new File(element);
						// we already warned about non-existing directory here
						File testFile = new File(testDir, filename);
						if (testFile.isFile()) {
							getProject().log(this,
									"  XFOUND: DYLD_LIBRARY_PATH directory contains QtCore: " + testFile.getAbsolutePath(),
									Project.MSG_INFO);
							found++;
						}
					}
				}
			}
			break;
			default: break;
		}

		// Setup some properties to be silently set to an empty string for safe ANT
		// substitution
		{
			final String[] emptyStringIfNotSetA = { Constants.QTJAMBI_CONFIG_JUMPTABLE,
					Constants.QTJAMBI_GENERATOR_JUMPTABLE };
			for (String emptyStringIfNotSet : emptyStringIfNotSetA) {
				String value = AntUtil.getPropertyAsString(propertyHelper, emptyStringIfNotSet);
				if (value == null)
					value = ""; // set this empty-string default here
				mySetProperty(0, emptyStringIfNotSet, " (default)", value, true); // silent
			}
		}

		// Sanity checking, the goal here is to abort the build process early
		// on so the developer doesn't waste their time getting an error
		// message later in the build process.
		if (generatorIncludepaths == null) // ${generator.includepaths}
			throw new BuildException("ERROR: " + Constants.GENERATOR_INCLUDEPATHS
					+ " property is not configured, please ensure you read and edit build.properties");
		if (!decideGeneratorPreProc())
			throw new BuildException("Unable to determine generator pre-processor settings");
		s = listToString(generatorPreProcDefinesList);
		getProject().log(this, Constants.GENERATOR_PREPROC_DEFINES + " is " + ((s != null) ? s : "<unset>"),
				Project.MSG_VERBOSE);
		AntUtil.setProperty(propertyHelper, Constants.GENERATOR_PREPROC_DEFINES,
				listJoinToString(generatorPreProcDefinesList, ","));
		s = listToString(generatorStaticLibsList);
		getProject().log(this, Constants.GENERATOR_STATICLIBS + " is " + ((s != null) ? s : "<unset>"),
				Project.MSG_VERBOSE);
		AntUtil.setProperty(propertyHelper, Constants.GENERATOR_STATICLIBS,
				listJoinToString(generatorStaticLibsList, ","));
		
		java.io.File buildDir = new java.io.File(new java.io.File(qtjambiFullVersion), "build");
		mySetProperty(-1, "outputDir", null, buildDir.getAbsolutePath(), true);
		mySetProperty(-1, "deploymentdir", null, new java.io.File(new java.io.File(qtjambiFullVersion), "deployment").getAbsolutePath(), true);
		mySetProperty(-1, "java.outdir", null, new java.io.File(buildDir, "java").getAbsolutePath(), true);
		mySetProperty(-1, "java.outsrcdir", null, new java.io.File(buildDir, "java-src").getAbsolutePath(), true);
		java.io.File platformBuildDir = new java.io.File(buildDir, osname);
		mySetProperty(-1, "qtjambi.builddir", null, new java.io.File(platformBuildDir, "qtjambi").getAbsolutePath(), true);
		mySetProperty(-1, "generator.builddir", null, new java.io.File(platformBuildDir, "generator").getAbsolutePath(), true);
		mySetProperty(-1, "plugins.builddir", null, new java.io.File(platformBuildDir, "plugins").getAbsolutePath(), true);
		mySetProperty(-1, "generator.xmlmerge.outputdir", null, new java.io.File(buildDir, "generator").getAbsolutePath(), true);
		mySetProperty(-1, "generator.outputdir", null, new java.io.File(new java.io.File(buildDir, "generator"), "out").getAbsolutePath(), true);

		int threads = 1;
		{
			String qtjambiJunitThreads = AntUtil.getPropertyAsString(propertyHelper, "qtjambi.junit.threads");
			try {
				threads = Integer.parseInt(qtjambiJunitThreads);
			} catch (Throwable e) {
				if (Boolean.getBoolean("qtjambi.autotests.multithreaded")) {
					AntUtil.setProperty(propertyHelper, "qtjambi.junit.threads",
							"" + (threads = Runtime.getRuntime().availableProcessors()));
				} else {
					AntUtil.setProperty(propertyHelper, "qtjambi.junit.threads", "1");
				}
			}
		}
		{
			String qtjambiJunitThreads = AntUtil.getPropertyAsString(propertyHelper, "qtjambi.junit.timeout");
			try {
				Integer.parseInt(qtjambiJunitThreads);
			} catch (Throwable e) {
				AntUtil.setProperty(propertyHelper, "qtjambi.junit.timeout", "" + (360000 + 60000 * threads));
			}
		}
	}

	public static String listToString(Collection<?> list) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		for (Object o : list) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append("\"");
			sb.append(o.toString());
			sb.append("\"");
		}
		sb.append("]");
		return sb.toString();
	}

	public static String listJoinToString(Collection<?> list, CharSequence delim) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (Object o : list) {
			if (first)
				first = false;
			else
				sb.append(delim);
			sb.append(o.toString());
		}
		return sb.toString();
	}

	private String detectConfiguration(String wantedConfiguration) {
		String dsoName = "QtCore";

		String dsoPath = doesQtLibExist(dsoName, libInfix, qtMajorVersion, qtMinorVersion, qtPatchlevelVersion, null,
				null, false);
		String prlPath = null;
		if (dsoPath != null) // Ok not look for PRL
			prlPath = doesQtLibExist(dsoName, libInfix, qtMajorVersion, qtMinorVersion, qtPatchlevelVersion, null, null,
					true); // prl=true
		if (dsoPath == null) {
			dsoPath = doesQtBinExist(dsoName, libInfix, qtMajorVersion, qtMinorVersion, qtPatchlevelVersion, null, null,
					false);
			if (dsoPath != null) // if not found look in other directory
				prlPath = doesQtBinExist(dsoName, libInfix, qtMajorVersion, qtMinorVersion, qtPatchlevelVersion, null,
						null, true); // prl=true
			if (prlPath == null) // on windows using legacy Qt SDK types the *.dll are in bin/ and the *.prl in
									// lib/
				prlPath = doesQtLibExist(dsoName, libInfix, qtMajorVersion, qtMinorVersion, qtPatchlevelVersion, null,
						null, true); // prl=true
		}

		String thisConfiguration = null;
		String defaultConfiguration = null;
		Set<String> foundSet = new HashSet<String>();
		String sourceValue = null;
		// Check *.prl add (WARNING: no *.prl)
		if (prlPath != null) { // open *.prl and examine fields
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(prlPath));
				String s;
				while ((s = reader.readLine()) != null) {
					String name;
					String value;
					// find first "="
					int indexEquals = s.indexOf("=");
					if (indexEquals < 0)
						continue;
					name = s.substring(0, indexEquals);
					value = s.substring(indexEquals + 1);
					name = Util.safeFirstWord(name); // skip leading and tailing on name
					name = name.toUpperCase();
					if ("QMAKE_PRL_CONFIG".equals(name) == false)
						continue;
					StringTokenizer tokenizer = new StringTokenizer(value, " \t\n");
					while (tokenizer.hasMoreElements()) {
						String t = tokenizer.nextToken();
						if (t.length() == 0)
							continue;
						t = t.toLowerCase();
						if (Constants.CONFIG_DEBUG.equals(t)) {
							foundSet.add(Constants.CONFIG_DEBUG);
						} else if (Constants.CONFIG_RELEASE.equals(t)) {
							foundSet.add(Constants.CONFIG_RELEASE);
						} else if (Constants.CONFIG_DEBUG_AND_RELEASE.equals(t)) {
							foundSet.add(Constants.CONFIG_DEBUG_AND_RELEASE);
						}
					}
				}
				reader.close();
				reader = null;
			} catch (IOException eat) {
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (IOException eat) {
				}
			}
			sourceValue = "available configurations:";
			if (foundSet.contains(Constants.CONFIG_DEBUG_AND_RELEASE)) {
				if (defaultConfiguration == null)
					defaultConfiguration = Constants.CONFIG_DEBUG_AND_RELEASE;
				sourceValue += " " + Constants.CONFIG_DEBUG_AND_RELEASE;
			}
			if (foundSet.contains(Constants.CONFIG_RELEASE)) {
				if (defaultConfiguration == null)
					defaultConfiguration = Constants.CONFIG_RELEASE;
				sourceValue += " " + Constants.CONFIG_RELEASE;
			}
			if (foundSet.contains(Constants.CONFIG_DEBUG)) {
				if (defaultConfiguration == null)
					defaultConfiguration = Constants.CONFIG_DEBUG;
				sourceValue += " " + Constants.CONFIG_DEBUG;
			}
		} else {
			sourceValue = "WARNING: no *.prl found";
		}

		if (thisConfiguration == null) {
			if (wantedConfiguration != null) {
				// if we parsed *.prl we error on invalid configurations against it
				if (foundSet.isEmpty() == false && foundSet.contains(wantedConfiguration)) {
					thisConfiguration = wantedConfiguration;
				} else if (Constants.CONFIG_DEBUG_AND_RELEASE.equals(wantedConfiguration)) {
					thisConfiguration = wantedConfiguration;
				} else if (Constants.CONFIG_RELEASE.equals(wantedConfiguration)) {
					thisConfiguration = wantedConfiguration;
				} else if (Constants.CONFIG_DEBUG.equals(wantedConfiguration)) {
					thisConfiguration = wantedConfiguration;
				} else if (Constants.CONFIG_TEST.equals(wantedConfiguration)) {
					thisConfiguration = wantedConfiguration;
				} else {
					throw new BuildException(Constants.CONFIGURATION + " could not be set to " + wantedConfiguration
							+ " try one of " + Constants.CONFIG_RELEASE + ", " + Constants.CONFIG_DEBUG + ", "
							+ Constants.CONFIG_DEBUG_AND_RELEASE + ", " + Constants.CONFIG_TEST);
				}
				sourceValue += "; default is " + defaultConfiguration;
			} else if (defaultConfiguration != null) {
				thisConfiguration = defaultConfiguration; // this is the default
				sourceValue += "; detected default set";
			} else {
				thisConfiguration = Constants.CONFIG_RELEASE; // this is the configuration of last resort
				sourceValue += "; default of last-resort set";
			}
		}
		setConfiguration(thisConfiguration);
		mySetProperty(-1, Constants.CONFIGURATION, " (" + sourceValue + ")", thisConfiguration, true);
		return thisConfiguration;
	}

	// wantedSdk maybe "MacOSX10.5.sdk" or "/Developer/SDKs/MacOSX10.5.sdk"
	private String detectMacosxSdk(String wantedSdk) {
		if (OSInfo.os() == OSInfo.OS.MacOS == false || qtMajorVersion >= 5)
			return null;

		// QMAKE_MAC_SDK=/Developer/SDKs/MacOSX10.5.sdk
		String sourceValueAvailable = null;
		Map<String, File> foundMap = new HashMap<String, File>();
		List<String> filenameList = new ArrayList<String>();
		File sdkDir = new File("/Developer/SDKs");
		if (sdkDir.isDirectory()) {
			File[] listFiles = sdkDir.listFiles();
			for (File f : listFiles) {
				if (f.isDirectory()) {
					String name = f.getName();
					foundMap.put(name, f);
					filenameList.add(name);
				}
			}
			Collections.sort(filenameList, Collections.reverseOrder());

			// build available string
			StringBuilder sb = new StringBuilder("available: ");
			boolean first = true;
			for (String n : filenameList) {
				if (first)
					first = false;
				else
					sb.append(", ");
				sb.append(n);
			}
			sourceValueAvailable = sb.toString();
		}

		boolean foundValue = false; // null is a valid value
		String sourceValue = "";
		String thisValue = null;
		File wantedSdkFile = null;
		if (wantedSdk != null) {
			if (wantedSdk.length() > 0) {
				wantedSdkFile = new File(wantedSdk); // absolute ?
				if (wantedSdkFile.isDirectory() == false) {
					wantedSdkFile = new File("/Developer/SDKs", wantedSdk); // relative?
					if (wantedSdkFile.isDirectory() == false)
						wantedSdkFile = null;
				}
				if (wantedSdkFile != null) {
					thisValue = wantedSdkFile.getAbsolutePath(); // invalidate it (maybe we should allow it and warn it
																	// does not exist)
					foundValue = true;
				}
			} else {
				if (wantedSdk.length() == 0)
					thisValue = null; // treat empty-string as null
				foundValue = true; // null is the value we want
			}
		}
		if (foundValue == false) {
			if (foundMap.isEmpty())
				throw new BuildException(
						"Directory \"/Developer/SDKs\" does not exist, did you install XCode on your Macosx ?");
			sourceValue = "picked default";
			thisValue = foundMap.get(filenameList.get(0)).getAbsolutePath();
			foundValue = true;
		}

		if (foundValue) {
			if (sourceValueAvailable != null) {
				if (sourceValue.length() > 0) // isEmpty() is Java1.6 :(
					sourceValue += "; ";
				sourceValue += sourceValueAvailable;
			}
			mySetProperty(-1, Constants.QTJAMBI_MACOSX_MAC_SDK, " (" + sourceValue + ")", thisValue, true);
		}

		return thisValue;
	}

	private void prettifyPathSeparator(String generatorIncludepaths, boolean allowRecurse) {
		StringTokenizer tokenizer = new StringTokenizer(generatorIncludepaths, File.pathSeparator);
		while (tokenizer.hasMoreTokens()) {
			String path = tokenizer.nextToken();
			path = propertyHelper.replaceProperties(null, path, null); // one round of substitution
			if (allowRecurse && path.indexOf(File.pathSeparatorChar) >= 0) {
				prettifyPathSeparator(path, false); // this is due to ${env.INCLUDE}
				continue;
			}
			File dir = new File(path);
			String msg = dir.getAbsolutePath();
			if (dir.isDirectory()) {
				getProject().log(this, "   FOUND: " + msg, Project.MSG_INFO);
			} else {
				getProject().log(this, " MISSING: " + msg, Project.MSG_WARN);
			}
		}
	}

	private File resolveDirectoryViaQTDIR(String qtdir, String attrName) {
		File tryPath = null;

		if (attrName.equals(Constants.BINDIR)) {
			tryPath = new File(qtdir, "bin"); // $QTDIR/bin
		} else if (attrName.equals(Constants.LIBDIR)) {
			tryPath = new File(qtdir, "lib"); // $QTDIR/lib
		} else if (attrName.equals(Constants.TOOLS_BINDIR)) {
			tryPath = new File(qtdir, "bin"); // $QTDIR/bin
		} else if (attrName.equals(Constants.TOOLS_LIBDIR)) {
			tryPath = new File(qtdir, "lib"); // $QTDIR/lib
		} else if (attrName.equals(Constants.LIBEXECDIR)) {
			tryPath = new File(qtdir, "libexec"); // $QTDIR/libexec
		} else if (attrName.equals(Constants.INCLUDEDIR)) {
			tryPath = new File(qtdir, "include"); // $QTDIR/include
		} else if (attrName.equals(Constants.PLUGINSDIR)) {
			tryPath = new File(qtdir, "plugins"); // $QTDIR/plugins
		} else if (attrName.equals(Constants.QMLMODULESDIR)) {
			tryPath = new File(qtdir, "qml"); // $QTDIR/qml
		} else if (attrName.equals(Constants.IMPORTSDIR)) {
			tryPath = new File(qtdir, "imports"); // $QTDIR/imports
		} else if (attrName.equals(Constants.TRANSLATIONSDIR)) {
			tryPath = new File(qtdir, "translations"); // $QTDIR/translations
		} else if (attrName.equals(Constants.RESOURCESDIR)) {
			tryPath = new File(qtdir, "resources"); // $QTDIR/resources
		} else if (attrName.equals(Constants.QMAKE)) {
			String qmakeExecName = QMakeTask.executableName();
			tryPath = new File(qtdir, "bin" + File.separator + qmakeExecName); // $QTDIR/bin
			if(!tryPath.isFile() && qmakeExecName.endsWith(".exe")) {
				tryPath = new File(qtdir, "bin" + File.separator + qmakeExecName.replace(".exe", ".bat"));
			}
		}

		if (tryPath.isDirectory())
			return tryPath;
		return null;
	}

	private boolean parseQtVersion(String versionString) {
		if (versionString == null || versionString.isEmpty())
			return false;

		// Remove leading, remove trailing whitespace
		versionString = Util.stripLeadingAndTrailingWhitespace(versionString);

		{
			// Check for valid character set "[0-9\.]+"
			final int len = versionString.length();
			for (int i = 0; i < len; i++) {
				char c = versionString.charAt(i);
				if ((c >= '0' && c <= '9') || c == '.')
					continue;
				return false;
			}
		}

		// Check for non-empty
		final int len = versionString.length();
		if (len == 0)
			return false;

		// Check for [0-9\.] and no double dots, no leading/trailing dots.
		if (versionString.charAt(0) == '.' || versionString.charAt(len - 1) == '.')
			return false;

		if (versionString.indexOf("..") >= 0)
			return false;

		// Split
		String[] versionParts = versionString.split("\\.");

		String tmpQtVersion = null;
		Integer tmpQtMajorVersion = null;
		Integer tmpQtMinorVersion = null;
		Integer tmpQtPatchlevelVersion = null;

		try {
			tmpQtVersion = versionString;
			if (versionParts.length < 1)
				return false;
			tmpQtMajorVersion = Integer.valueOf(versionParts[0]);
			if (versionParts.length < 2)
				return false;
			tmpQtMinorVersion = Integer.valueOf(versionParts[1]);
			if (versionParts.length < 3)
				tmpQtPatchlevelVersion = 0;
			else
				tmpQtPatchlevelVersion = Integer.valueOf(versionParts[2]);
		} catch (NumberFormatException e) {
			return false;
		}

		// Ok we happy
		qtVersion = tmpQtVersion;
		qtMajorVersion = tmpQtMajorVersion.intValue();
		qtMinorVersion = tmpQtMinorVersion.intValue();
		qtPatchlevelVersion = tmpQtPatchlevelVersion.intValue();
		return true;
	}

	private boolean decideQtVersion() {
		boolean versionFound = false;

		String tmpQtVersion = null;

		if (!versionFound) {
			tmpQtVersion = AntUtil.getPropertyAsString(propertyHelper, Constants.QT_VERSION);
			if (parseQtVersion(tmpQtVersion)) {
				versionFound = true;
				qtVersionSource = " (${" + Constants.QT_VERSION + "})";
			}
		}

		if (!versionFound) {
			// Run "qmake -query"
			String qmakeExe = AntUtil.getPropertyAsString(propertyHelper, "qmake.binary");
			qmakeExe = QMakeTask.resolveExecutableAbsolutePath(getProject(), qmakeExe);

			final String K_QT_VERSION = "QT_VERSION";

			List<String> qmakeArgs = new ArrayList<String>();
			qmakeArgs.add(qmakeExe);
			qmakeArgs.add("-query");
			qmakeArgs.add(K_QT_VERSION);

			try {
				File fileDir = new File(".");
				String[] sA = Exec.executeCaptureOutput(this, qmakeArgs, fileDir, getProject(), null, null, false);
				Util.emitDebugLog(getProject(), sA);
				if (sA != null && sA.length == 2 && sA[0] != null)
					tmpQtVersion = sA[0]; // stdout
				// Extract QT_VERSION:4.7.4
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (tmpQtVersion != null) {
				if (tmpQtVersion.startsWith(K_QT_VERSION + ":"))
					tmpQtVersion = tmpQtVersion.substring(K_QT_VERSION.length() + 1);
				tmpQtVersion = Util.stripLeadingAndTrailingWhitespace(tmpQtVersion);

				if (parseQtVersion(tmpQtVersion)) {
					versionFound = true;
					qtVersionSource = " (" + qmakeExe + " -query " + K_QT_VERSION + ")";
				}
			}
		}

		// This is last method as it is the target we are trying to set and also
		// debatable if it should be here at all. Maybe the only use is with maybe
		// supporting really older Qt which does not allow: qmake -query
		if (!versionFound) {
			tmpQtVersion = AntUtil.getPropertyAsString(propertyHelper, Constants.VERSION);
			if (parseQtVersion(tmpQtVersion)) {
				versionFound = true;
				qtVersionSource = " (${" + Constants.VERSION + "})";
			}
		}

		return versionFound;
	}

	private boolean decideQMAKE_XSPEC() {
		boolean specFound = false;

		String qmakeXspecSource = "";
		String qmakeSpec = null;
		String qmakeXSpec = null;

		if (!specFound) {
			qmakeXSpec = AntUtil.getPropertyAsString(propertyHelper, Constants.QT_SPEC);
			if (parseQtVersion(qmakeXSpec)) {
				specFound = true;
				qmakeXspecSource = " (${" + Constants.QT_SPEC + "})";
			}
		}

		if (!specFound) {
			// Run "qmake -query"
			String qmakeExe = AntUtil.getPropertyAsString(propertyHelper, "qmake.binary");
			qmakeExe = QMakeTask.resolveExecutableAbsolutePath(getProject(), qmakeExe);

			final String K_QMAKE_SPEC = "QMAKE_SPEC";
			final String K_QMAKE_XSPEC = "QMAKE_XSPEC";

			List<String> qmakeArgs = new ArrayList<String>();
			qmakeArgs.add(qmakeExe);
			qmakeArgs.add("-query");
			qmakeArgs.add(K_QMAKE_SPEC);

			try {
				File fileDir = new File(".");
				String[] sA = Exec.executeCaptureOutput(this, qmakeArgs, fileDir, getProject(), null, null, false);
				Util.emitDebugLog(getProject(), sA);
				if (sA != null && sA.length == 2 && sA[0] != null)
					qmakeSpec = sA[0]; // stdout
				// Extract QT_VERSION:4.7.4
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (qmakeSpec != null) {
				if (qmakeSpec.startsWith(K_QMAKE_SPEC + ":"))
					qmakeXSpec = qmakeXSpec.substring(K_QMAKE_SPEC.length() + 1);
				qmakeSpec = Util.stripLeadingAndTrailingWhitespace(qmakeSpec);
			}

			qmakeArgs.clear();
			qmakeArgs.add(qmakeExe);
			qmakeArgs.add("-query");
			qmakeArgs.add(K_QMAKE_XSPEC);

			try {
				File fileDir = new File(".");
				String[] sA = Exec.executeCaptureOutput(this, qmakeArgs, fileDir, getProject(), null, null, false);
				Util.emitDebugLog(getProject(), sA);
				if (sA != null && sA.length == 2 && sA[0] != null)
					qmakeXSpec = sA[0]; // stdout
				// Extract QT_VERSION:4.7.4
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (qmakeXSpec != null) {
				if (qmakeXSpec.startsWith(K_QMAKE_XSPEC + ":"))
					qmakeXSpec = qmakeXSpec.substring(K_QMAKE_XSPEC.length() + 1);
				qmakeXSpec = Util.stripLeadingAndTrailingWhitespace(qmakeXSpec);

				specFound = true;
				qmakeXspecSource = " (" + qmakeExe + " -query " + K_QMAKE_XSPEC + ")";
			}
			if (qmakeXSpec != null) {
				mySetProperty(-1, Constants.QT_SPEC, qmakeXspecSource, qmakeXSpec, true);
				if (!qmakeXSpec.equals(qmakeSpec)) {
					OSInfo.setQMakeXSpec(qmakeXSpec);
				}
			}
		}
		return specFound;
	}

	private boolean decideGeneratorPreProc() {
		generatorStaticLibsList.add("QtUIC");
		for (String module : modules.keySet()) {
			if (Boolean.parseBoolean(AntUtil.getPropertyAsString(propertyHelper, "qtjambi." + module + ".any.true"))) {
				if (Boolean.parseBoolean(
						AntUtil.getPropertyAsString(propertyHelper, "qtjambi." + module + ".staticlib"))) {
					generatorStaticLibsList.add(modules.get(module).libraryName);
					if ("activex".equals(module)) {
						generatorStaticLibsList.add("QtActiveX");
					}
				}
			} else if (modules.get(module).preprocdef != null) {
				switch (module) {
				case "macextras":
				case "x11extras":
				case "winextras":
					if (skippedModules.contains(module)) {
						generatorPreProcDefinesList.add(modules.get(module).preprocdef);
					} else {
						String generatorIxtraIncludes = AntUtil.getPropertyAsString(propertyHelper,
								"generator.extra.includes");
						String qtdir = AntUtil.getPropertyAsString(propertyHelper, "QTDIR");
						if (new File(new File(qtdir), "qt" + module).isDirectory() || (new File(new File(qtdir),
								".." + File.separator + "Src" + File.separator + "qt" + module).isDirectory()
								&& new File(new File(qtdir),
										".." + File.separator + "Src" + File.separator + "qt" + module)
												.listFiles().length > 0)) {
							if (generatorIxtraIncludes == null) {
								generatorIxtraIncludes = qtdir + "/qtmacextras/include;" + qtdir + "/../Src/qt" + module
										+ "/include";
							} else {
								generatorIxtraIncludes += ";" + qtdir + "/qtmacextras/include;" + qtdir + "/../Src/qt"
										+ module + "/include";
							}
							AntUtil.setProperty(propertyHelper, "generator.extra.includes", generatorIxtraIncludes,
									false);
						} else {
							generatorPreProcDefinesList.add(modules.get(module).preprocdef);
						}
					}
					break;
				default:
					generatorPreProcDefinesList.add(modules.get(module).preprocdef);
				}
			}
		}

		return true;
	}

	private String doesQtLibExistDir(String librarydir, String name) {
		File dir = new File(librarydir, name);
		if (dir.exists() && dir.isDirectory())
			return dir.getAbsolutePath();
		return null;
	}

	private String doesQtDsoExist(String name, String infix, int majorVersion, int minorVersion, int patchVersion,
			String librarydir, Boolean debugValue, boolean prl, boolean staticLib, String propName) {
		if ((infix == null || infix.isEmpty()) && OSInfo.crossOSArchName().equalsIgnoreCase("android") && !androidAbis.isEmpty()) {
			infix = "_" + androidAbis.get(0);
		}
		StringBuilder path = new StringBuilder();

		if (librarydir != null) {
			path.append(librarydir);
			path.append(File.separator);
		} else if (propName != null) {
			String propValue = AntUtil.getPropertyAsString(propertyHelper, propName);
			if (propValue != null && !propValue.isEmpty())
				path.append(propValue);
			path.append(File.separator);
		}

		boolean thisDebug = isConfigurationDebug();
		if (debugValue != null)
			thisDebug = debugValue.booleanValue();
		String filename;
		if (prl) {
			if (useQtFramework && name.startsWith("Qt")) {
				filename = CreateNativeDeploymentTask.formatQtPrlNameAsFramework(name, infix, thisDebug);
			} else {
				filename = CreateNativeDeploymentTask.formatQtPrlName(name, infix, thisDebug, qtMajorVersion, qtMinorVersion,
						qtPatchlevelVersion);
			}
		} else if (useQtFramework && name.startsWith("Qt") && !staticLib) {
			filename = CreateNativeDeploymentTask.formatQtNameAsFramework(name, infix, thisDebug, majorVersion, minorVersion,
					patchVersion);
		} else {
			filename = CreateNativeDeploymentTask.formatQtName(name, infix, thisDebug, majorVersion, minorVersion, patchVersion,
					staticLib);
		}
		File testForFile = new File(new File(path.toString()), filename);
		getProject().log(this, "Checking " + testForFile + " " + testForFile.exists(), Project.MSG_VERBOSE);
		if (testForFile.exists()) {
			try {
				return testForFile.getCanonicalPath();
			} catch (IOException e) {
				return testForFile.getAbsolutePath();
			}
		}
		return null;
	}

	private String doesQtBinExist(String name, String infix, int majorVersion, int minorVersion, int patchVersion,
			String librarydir, Boolean debugValue, boolean prl) {
		return doesQtDsoExist(name, infix, majorVersion, minorVersion, patchVersion, librarydir, debugValue, prl, false,
				Constants.BINDIR);
	}

	private String doesQtLibExist(String name, String infix, int majorVersion, int minorVersion, int patchVersion,
			String librarydir, Boolean debugValue, boolean prl) {
		return doesQtDsoExist(name, infix, majorVersion, minorVersion, patchVersion, librarydir, debugValue, prl, false,
				Constants.LIBDIR);
	}

	private String doesQtStaticLibExist(String name, String infix, int majorVersion, int minorVersion, int patchVersion,
			String librarydir, Boolean debugValue, boolean prl) {
		return doesQtDsoExist(name, infix, majorVersion, minorVersion, patchVersion, librarydir, debugValue, prl, true,
				Constants.LIBDIR);
	}

	static class LibResult {
		private LibResult(String path, boolean isStatic) {
			this.path = path;
			this.isStatic = isStatic;
		}

		final String path;
		final boolean isStatic;
	}

	private LibResult doesQtLibOrBinExist(String name, String infix, int majorVersion, int minorVersion,
			int patchVersion, Boolean debugValue) {
		boolean isStatic = false;
		String path = doesQtLibExist(name, infix, majorVersion, minorVersion, patchVersion, null, debugValue, false);
		if (path == null)
			path = doesQtBinExist(name, infix, majorVersion, minorVersion, patchVersion, null, debugValue, false);
		if (path == null) {
			path = doesQtStaticLibExist(name, infix, majorVersion, minorVersion, patchVersion, null, debugValue, false);
			isStatic = path != null;
		}
		return path != null ? new LibResult(path, isStatic) : null;
	}

	private boolean detectQtDsoExistAndSetProperty(String propName, String name, String infix, int majorVersion,
			int minorVersion, int patchVersion, String addQtConfig, String altLibdir) {
		if (addQtConfig != null) {
			if (skippedModules.contains(addQtConfig)) {
				return false;
			} else if (addQtConfig.startsWith("3d") && skippedModules.contains("3d")) {
				return false;
			} else if (addQtConfig.startsWith("multimedia") && skippedModules.contains("multimedia")) {
				return false;
			} else if (addQtConfig.startsWith("webengine") && skippedModules.contains("webengine")) {
				return false;
			} else if (addQtConfig.startsWith("pdf") && skippedModules.contains("pdf")) {
				return false;
			} else if ((addQtConfig.equals("opengl") || addQtConfig.startsWith("pdf")) && qtMajorVersion < 6) {
				return false;
			}
		}
		// We detect both non-debug and debug forms manually
		// On Linux the debug and the release are the same DSO name, so we have to look
		// at configuration before detecting which one to detect first
		LibResult dsoPath;
		LibResult dsoDebugPath;
		if (Constants.CONFIG_DEBUG.equals(getConfiguration())) {
			dsoDebugPath = doesQtLibOrBinExist(name, infix, majorVersion, minorVersion, patchVersion, Boolean.TRUE);
			dsoPath = doesQtLibOrBinExist(name, infix, majorVersion, minorVersion, patchVersion, Boolean.FALSE);
			// if(dsoDebugPath != null && dsoDebugPath.equals(dsoPath))
			// dsoPath = null; // same name on linux it can only be one kind
		} else {
			dsoPath = doesQtLibOrBinExist(name, infix, majorVersion, minorVersion, patchVersion, Boolean.FALSE);
			dsoDebugPath = doesQtLibOrBinExist(name, infix, majorVersion, minorVersion, patchVersion, Boolean.TRUE);
			if (dsoDebugPath == null && dsoPath != null && isMinGW) {
				File file = new File(dsoPath.path);
				String _name = file.getName() + ".debug";
				file = new File(file.getParentFile().getParentFile(), "lib");
				file = new File(file, _name);
				if (file.exists()) {
					AntUtil.setProperty(propertyHelper, propName + ".debug.true", "true");
					dsoDebugPath = dsoPath;
				}
			}
			// if(dsoPath != null && dsoPath.equals(dsoDebugPath))
			// dsoDebugPath = null; // same name on linux it can only be one kind
		}

		String dsoDebugFilename = null;
		String sourceValue = null;
		if (dsoDebugPath != null && !dsoDebugPath.isStatic) { // ideally we want to resolve and display the relative
																// path difference
			dsoDebugFilename = new File(dsoDebugPath.path).getName();
			sourceValue = " (found debug DSO: " + dsoDebugFilename + ")";
		}

		mySetProperty(((dsoPath != null || dsoDebugPath == null) ? -1 : 0), propName, sourceValue,
				dsoPath != null ? dsoPath.path : null, true); // silent if null
		if (dsoPath != null) { // we do this because the setting is used for both the Qt library itself and the
								// JNI linkage DSO
			AntUtil.setProperty(propertyHelper, propName + ".true", "true"); // true or not-set
			AntUtil.setProperty(propertyHelper, propName + ".any.true", "true"); // true or not-set
		}

		sourceValue = null;
		if (Constants.CONFIG_DEBUG_AND_RELEASE.equals(getConfiguration()) && dsoPath == null && dsoDebugPath != null
				&& !dsoDebugPath.isStatic)
			sourceValue = " (no release DSO found)";
		mySetProperty(((dsoPath != null || dsoDebugPath == null) ? 0 : -1), propName + ".debug", sourceValue,
				dsoDebugPath != null ? dsoDebugPath.path : null, true); // silent unless dsoPath==null
		if (dsoDebugPath != null) { // we do this because the setting is used for both the Qt library itself and the
									// JNI linkage DSO
			AntUtil.setProperty(propertyHelper, propName + ".debug.true", "true"); // true or not-set
			AntUtil.setProperty(propertyHelper, propName + ".any.true", "true"); // true or not-set
		}
		if ((dsoDebugPath != null && dsoDebugPath.isStatic) || (dsoPath != null && dsoPath.isStatic)) {
			AntUtil.setProperty(propertyHelper, propName + ".staticlib", "true");
			if (dsoPath != null && dsoPath.path != null && dsoDebugPath == null) {
				if (dsoPath.path.endsWith(".a")) {
					Path path = new File(dsoPath.path).toPath();
					File link = new File(dsoPath.path.substring(0, dsoPath.path.length() - 2) + "_debug.a");
					if (!link.exists()) {
						try {
							Files.createLink(link.toPath(), path);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		if (dsoPath != null || dsoDebugPath != null) {
			if (addQtConfig != null)
				addToQtConfig("qtjambi-" + addQtConfig);
		}

		return dsoPath != null || dsoDebugPath != null;
	}

	private boolean detectQtDsoExistAndSetProperty(String propName, String name, String infix, int majorVersion,
			int minorVersion, int patchVersion, String addQtConfig) {
		return detectQtDsoExistAndSetProperty(propName, name, infix, majorVersion, minorVersion, patchVersion,
				addQtConfig, null);
	}

	public static boolean findInString(String haystack, String needle, char delimChar) {
		final int nLen = needle.length();
		final int hLen = haystack.length();
		int o = 0;
		boolean found = false;
		while (o < hLen) {
			int stringOffset = haystack.indexOf(needle, o);
			if (stringOffset < 0)
				break;
			if (stringOffset == 0 || haystack.charAt(stringOffset - 1) == delimChar) {
				if (hLen <= stringOffset + nLen || haystack.charAt(stringOffset + nLen) == delimChar) {
					// found
					found = true;
					break;
				}
			}
			o = stringOffset + nLen;
		}
		return found;
	}

	/**
	 * Adds new library to qtjambi.qtconfig property, which is used to specify
	 * additional qt libraries compiled.
	 * 
	 * @param config Library to add
	 */
	private void addToQtConfig(String config) {
		String oldConfig = AntUtil.getPropertyAsString(propertyHelper, Constants.QTCONFIG);
		String newConfig = null;

		if (oldConfig != null) {
			final char delimChar = ' ';
			// Check it doesn't already exist (and don't add it again), this
			// happens when InitializeTask runs twice due to multiple ant
			// targets being set.
			boolean found = findInString(oldConfig, config, delimChar);
			if (!found) {
				newConfig = oldConfig + delimChar + config;
			}
		} else {
			newConfig = config;
		}

		if (newConfig != null)
			AntUtil.setProperty(propertyHelper, Constants.QTCONFIG, newConfig, false);
	}

	private String decideQtLibDso(String attrName, String name, String infix, int majorVersion, int minorVersion,
			int patchlevelVersion, boolean verboseFlag) {
		String path = doesQtLibExist(name, infix, majorVersion, minorVersion, patchlevelVersion, null, Boolean.FALSE,
				false);
		if (verboseFlag && path != null)
			getProject().log(this, attrName + ": " + path, Project.MSG_INFO);
		return path;
	}

	// unused
	@SuppressWarnings("unused")
	private String decideQtLibDso(String attrName, String name, String infix, int majorVersion, int minorVersion,
			int patchlevelVersion) {
		boolean verbose = getVerboseLevel() > 0;
		return decideQtLibDso(attrName, name, infix, majorVersion, minorVersion, patchlevelVersion, verbose);
	}

	// unused
	@SuppressWarnings("unused")
	private String decideQtLibDso(String attrName, String name, String infix, int[][] tryVersionA,
			Boolean verboseBoolean) {
		boolean thisVerbose = getVerboseLevel() > 0;
		if (verboseBoolean != null)
			thisVerbose = verboseBoolean.booleanValue();

		if (tryVersionA == null)
			return decideQtLibDso(attrName, name, infix, -1, -1, -1, thisVerbose); // run at least once

		String rv = null;
		for (int[] tryVersion : tryVersionA) {
			if (tryVersion != null && tryVersion.length == 3)
				rv = decideQtLibDso(attrName, name, infix, tryVersion[0], tryVersion[1], tryVersion[2], thisVerbose);
			else
				rv = decideQtLibDso(attrName, name, infix, -1, -1, -1, thisVerbose);
			if (rv != null)
				return rv;
		}
		return rv;
	}

	private String decideQtBinDso(String attrName, String name, String infix, int majorVersion, int minorVersion,
			int patchlevelVersion, boolean verboseFlag) {
		String path = doesQtBinExist(name, infix, majorVersion, minorVersion, patchlevelVersion, null, Boolean.FALSE,
				false);
		if (verboseFlag && path != null)
			getProject().log(this, attrName + ": " + path, Project.MSG_INFO);
		return path;
	}

	private String decideQtBinDso(String attrName, String name, String infix, int[][] tryVersionA,
			Boolean verboseBoolean) {
		boolean thisVerbose = getVerboseLevel() > 0;
		if (verboseBoolean != null)
			thisVerbose = verboseBoolean.booleanValue();

		if (tryVersionA == null)
			return decideQtBinDso(attrName, name, infix, -1, -1, -1, thisVerbose); // run at least once

		String rv = null;
		for (int[] tryVersion : tryVersionA) {
			if (tryVersion != null && tryVersion.length == 3)
				rv = decideQtBinDso(attrName, name, infix, tryVersion[0], tryVersion[1], tryVersion[2], thisVerbose);
			else
				rv = decideQtBinDso(attrName, name, infix, -1, -1, -1, thisVerbose);
			if (rv != null)
				return rv;
		}
		return rv;
	}

	private void analyzeLibinfix(File qtdir) {
		if (qtMajorVersion > 4) {
			File libraryPath;
			if (OSInfo.crossOS() == OSInfo.OS.Windows) {
				libraryPath = new File(qtdir, "bin");
			} else {
				libraryPath = new File(qtdir, "lib");
			}

			String libName = "Qt" + qtMajorVersion + "Gui";

			String[] libSuffixes;
			boolean isAndroid = false;
			switch (OSInfo.crossOS()) {
			case Windows:
				libSuffixes = new String[] { "d.dll", ".dll" };
				break;
			case MacOS:
				libSuffixes = new String[] { "_debug." + qtVersion + ".dylib", "." + qtVersion + ".dylib" };
				libName = "lib" + libName;
				break;
			case Android:
				libSuffixes = new String[] { ".so" };
				libName = "lib" + libName;
				isAndroid = true;
				break;
			case IOS:
				libSuffixes = new String[] { ".a" };
				libName = "lib" + libName;
				break;
			case Linux:
			case Unknown:
			default:
				libSuffixes = new String[] { ".so." + qtVersion };
				libName = "lib" + libName;
				break;
			}

			String availableAbis = "";
			loop: for (String libSuffix : libSuffixes) {
				if (!new File(libraryPath, libName + libSuffix).exists()) {
					for (File libfile : libraryPath.listFiles()) {
						String fileName = libfile.getName();
						if (libfile.isFile() && fileName.startsWith(libName) && fileName.endsWith(libSuffix)) {
							fileName = fileName.substring(0, fileName.length() - libSuffix.length());
							fileName = fileName.substring(libName.length());
							if (isAndroid) {
								fileName = fileName.substring(1);
								availableAbis += " " + fileName;
								androidAbis.add(fileName);
							} else {
								libInfix = fileName;
								break loop;
							}
						} else if (OSInfo.os() == OSInfo.OS.MacOS && libfile.isDirectory()
								&& fileName.equals("QtGui.framework")) {
							mySetProperty(-1, Constants.MAC_OS_USE_FRAMEWORK, " (as detected in lib directory)", "true", true);
							useQtFramework = true;
							break loop;
						}
					}
				}
			}
			if (isAndroid) {
				mySetProperty(-1, Constants.QTJAMBI_ABIS, " (taken from library name)", availableAbis.trim(), true);
			}
			mySetProperty(-1, Constants.QTJAMBI_QT_LIBINFIX, " (taken from library name)", libInfix, true);
		}
	}

	public static void setTargetProperties(Properties targetProperties) {
		InitializeBuildTask.targetProperties = targetProperties;
	}
}
