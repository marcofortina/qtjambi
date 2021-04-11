/****************************************************************************
**
** Copyright (C) 2009-2021 Dr. Peter Droste, Omix Visualization GmbH & Co. KG. All rights reserved.
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
package io.qt.core;

import static io.qt.internal.QtJambiInternal.nativeId;

import io.qt.QtPrimitiveType;
import io.qt.QtUninvokable;

/**
 * QDoubleBindable is primitive-typed version of QBindable&lt;Double&gt;.
 * @see QBindable
 */
public final class QDoubleBindable extends QUntypedBindable {

	private QDoubleBindable(QPrivateConstructor p) {
		super(p);
	}

	/**
	 * Creates a new <code>double</code>-typed invalid bindable.
	 */
	public QDoubleBindable() {
		super(null, null, true);
	}
	
	/**
	 * Creates a copy of the provided bindable. 
	 * If the type of the other bindable is not <code>double</code> it remains invalid.
	 * @param other
	 */
	public QDoubleBindable(QUntypedBindable other) {
		super(other.data(), other.iface(), true);
		check();
	}
	
	private void check() {
		QBindableInterface iface = iface();
		if(iface!=null) {
			QMetaType metaType = iface.metaType();
			if(metaType==null 
					|| metaType.sizeOf()!=8
					|| (metaType.id()!=QMetaType.Type.Double.value()
						&& metaType.id()!=QMetaType.Type.Float.value())) {
				setIface(null);
				setData(null);
			}
		}
	}

	QDoubleBindable(QUntypedPropertyData d, QBindableInterface i) {
		super(d, i, true);
	}
	
	/**
	 * Creates a new bindable from provided property.
	 * @param property
	 */
	public QDoubleBindable(QDoubleProperty property) {
		super(property, bindableInterface(0), true);
	}
	
	/**
	 * Creates a new bindable from provided property.
	 * @param property
	 */
	public QDoubleBindable(QObject.QDoubleProperty property) {
		super(property, bindableInterface(1), true);
	}
	
	/**
	 * Creates a new bindable from provided property.
	 * @param property
	 */
	public QDoubleBindable(QObject.QComputedDoubleProperty property) {
		super(property, bindableInterface(2), true);
	}
	
	/**
	 * Creates a new bindable from provided property.
	 * If the type of the property is not <code>double</code> an {@link IllegalArgumentException} is thrown.
	 * @param property
	 */
	public QDoubleBindable(QProperty<@QtPrimitiveType Double> property) {
		super(property, QBindable.bindableInterface(property.valueMetaType().id(), 0), true);
		check();
		if(iface()==null)
			throw new IllegalArgumentException("Given QProperty is not of double type.");
	}
	
	/**
	 * Creates a new bindable from provided property.
	 * If the type of the property is not <code>double</code> an {@link IllegalArgumentException} is thrown.
	 * @param property
	 */
	public QDoubleBindable(QObject.QProperty<@QtPrimitiveType Double> property) {
		super(property, QBindable.bindableInterface(property.valueMetaType().id(), 1), true);
		check();
		if(iface()==null)
			throw new IllegalArgumentException("Given QProperty is not of double type.");
	}
	
	/**
	 * Creates a new bindable from provided property.
	 * If the type of the property is not <code>double</code> an {@link IllegalArgumentException} is thrown.
	 * @param property
	 */
	public QDoubleBindable(QObject.QComputedProperty<@QtPrimitiveType Double> property) {
		super(property, bindableInterface(2), true);
		check();
		if(iface()==null)
			throw new IllegalArgumentException("Given QProperty is not of double type.");
	}
	
	static native QBindableInterface bindableInterface(int propertyType);

	/**
	 * Creates a binding to the underlying property.
	 */
	@QtUninvokable
	@Override
	public QDoublePropertyBinding makeBinding()
    {
		QUntypedPropertyBinding binding = super.makeBinding();
		if(binding instanceof QDoublePropertyBinding) {
			return (QDoublePropertyBinding)binding;
		}else {
			return new QDoublePropertyBinding(binding);
		}
    }
	
    /**
     * Returns the binding expression that is associated with the underlying property. 
     * A default constructed {@link QDoublePropertyBinding} will be returned if no such association exists.
     * @return binding
     */
	@QtUninvokable
	@Override
    public QDoublePropertyBinding binding()
    {
		QUntypedPropertyBinding binding = super.binding();
		if(binding instanceof QDoublePropertyBinding) {
			return (QDoublePropertyBinding)binding;
		}else {
			return new QDoublePropertyBinding(binding);
		}
    }
    
	@QtUninvokable
	public QDoublePropertyBinding setBinding(QDoublePropertyBinding binding)
    {
		QUntypedPropertyBinding oldBinding = super.overrideBinding(binding);
		if(oldBinding instanceof QDoublePropertyBinding) {
			return (QDoublePropertyBinding)oldBinding;
		}else {
			return new QDoublePropertyBinding(oldBinding);
		}
    }
	
	@QtUninvokable
	public QDoublePropertyBinding setBinding(QPropertyBinding<@QtPrimitiveType Double> binding)
    {
		if(binding!=null && !QDoubleProperty.checkType(binding.valueMetaType()))
			return new QDoublePropertyBinding();
		QUntypedPropertyBinding oldBinding = super.overrideBinding(binding);
		if(oldBinding instanceof QDoublePropertyBinding) {
			return (QDoublePropertyBinding)oldBinding;
		}else {
			return new QDoublePropertyBinding(oldBinding);
		}
    }
	
	@QtUninvokable
	public QDoublePropertyBinding setBinding(java.util.function.DoubleSupplier functor)
    {
		return setBinding(new QDoublePropertyBinding(functor));
    }
	
	/**
	 * <p>Disassociates the binding expression from this property and returns it.</p>
	 * <p>After calling this function, the value of the property will only change if you assign a new value to it, or when a new binding is set.</p>
	 * @return the removed binding
	 */
	@QtUninvokable
    public QDoublePropertyBinding takeBinding()
    {
        return setBinding(new QDoublePropertyBinding());
    }
    
	/**
	 * Returns the value of the underlying property. This may evaluate a binding expression that is tied to the property, before returning the value.
	 * @return value
	 */
    @io.qt.QtUninvokable
    public final double value() {
        return value(nativeId(this.iface()), nativeId(this.data()));
    }
    
	/**
	 * <p>Assigns newValue to the underlying property and removes the property's associated binding, if present.</p>
	 * @param newValue
	 */
    @io.qt.QtUninvokable
    public final void setValue(double value) {
        setValue(nativeId(this.iface()), nativeId(this.data()), value);
    }
    
    @io.qt.QtUninvokable
    static native double value(long ifaceId, long dataId);
    
    @io.qt.QtUninvokable
    static native void setValue(long ifaceId, long dataId, double value);
}