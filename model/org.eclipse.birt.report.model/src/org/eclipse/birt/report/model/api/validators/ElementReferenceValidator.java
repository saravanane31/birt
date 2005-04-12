/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.model.api.validators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.report.model.api.elements.SemanticError;
import org.eclipse.birt.report.model.api.util.StringUtil;
import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.birt.report.model.elements.ReportDesign;
import org.eclipse.birt.report.model.metadata.ElementPropertyDefn;
import org.eclipse.birt.report.model.metadata.ElementRefValue;
import org.eclipse.birt.report.model.metadata.PropertyType;
import org.eclipse.birt.report.model.validators.AbstractPropertyValidator;

/**
 * Validates the property whose type is element reference. If the value can
 * refer to an actual element, it will be resolved after validation.
 * <h3>Rule</h3>
 * The rule is that the element reference value should refer to an actual
 * element in the same report.
 * <h3>Applicability</h3>
 * This validator is only applied to the element reference properties of
 * <code>DesignElement</code>, except <code>StyledElement.STYLE_PROP</code>.
 * The <code>StyledElement.STYLE_PROP</code> value should be validated with
 * {@link StyleReferenceValidator}.
 */

public class ElementReferenceValidator extends AbstractPropertyValidator
{

	/**
	 * Name of this validator.
	 */

	public final static String NAME = "ElementReferenceValidator"; //$NON-NLS-1$

	private final static ElementReferenceValidator instance = new ElementReferenceValidator( );

	/**
	 * Returns the singleton validator instance.
	 * 
	 * @return the validator instance
	 */

	public static ElementReferenceValidator getInstance( )
	{
		return instance;
	}

	/**
	 * Validates the element reference value can refer to an actual element.
	 * 
	 * @param design
	 *            the report design
	 * @param element
	 *            the element holding the element reference property
	 * @param propName
	 *            the name of the element reference property
	 * @return error list, each of which is the instance of
	 *         <code>SemanticException</code>.
	 */

	public List validate( ReportDesign design, DesignElement element,
			String propName )
	{
		List list = new ArrayList( );

		if ( !checkElementReference( design, element, propName ) )
		{
			Object value = element.getLocalProperty( design, propName );

			list.add( new SemanticError( element, new String[]{propName,
					( (ElementRefValue) value ).getName( )},
					SemanticError.DESIGN_EXCEPTION_INVALID_ELEMENT_REF ) );
		}

		return list;
	}

	/**
	 * Attempts to resolve an element reference property. If the property is
	 * empty, or the reference is already resolved, return true. If the
	 * reference is not resolved, attempt to resolve it. If it cannot be
	 * resolved, return false.
	 * 
	 * @param design
	 *            the report design
	 * @param element
	 *            the element holding this element reference property
	 * @param propName
	 *            the name of the property
	 * @return <code>true</code> if the property is resolved;
	 *         <code>false</code> otherwise.
	 */

	private boolean checkElementReference( ReportDesign design,
			DesignElement element, String propName )
	{
		assert !StringUtil.isBlank( propName );

		// Is the value set?

		Object value = element.getLocalProperty( design, propName );
		if ( value == null )
			return true;

		// This must be an element reference property.

		ElementPropertyDefn prop = element.getPropertyDefn( propName );
		assert PropertyType.ELEMENT_REF_TYPE == prop.getTypeCode( );

		// Attempt to resolve the reference.

		ElementRefValue ref = (ElementRefValue) value;
		element.resolveElementReference( design, prop, ref );
		return ref.isResolved( );
	}

}
