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
import java.util.Collections;
import java.util.List;

import org.eclipse.birt.report.model.api.elements.SemanticError;
import org.eclipse.birt.report.model.core.DesignElement;
import org.eclipse.birt.report.model.elements.DataSet;
import org.eclipse.birt.report.model.elements.ReportDesign;
import org.eclipse.birt.report.model.validators.AbstractElementValidator;

/**
 * Validates the result set of the given data set has at least one column.
 * 
 * <h3>Rule</h3>
 * The rule is that the result set of the given data set has at least one
 * column.
 * 
 * <h3>Applicability</h3>
 * This validator is only applied to <code>DataSet</code>.
 *  
 */

public class DataSetResultSetValidator extends AbstractElementValidator
{

	private final static DataSetResultSetValidator instance = new DataSetResultSetValidator( );

	/**
	 * Returns the singleton validator instance.
	 * 
	 * @return the validator instance
	 */

	public static DataSetResultSetValidator getInstance( )
	{
		return instance;
	}

	/**
	 * Validates whether the result set of the given data set has no column
	 * defined.
	 * 
	 * @param design
	 *            the report design
	 * @param element
	 *            the data set to validate
	 * @return error list, each of which is the instance of
	 *         <code>SemanticException</code>.
	 */

	public List validate( ReportDesign design, DesignElement element )
	{
		if ( !( element instanceof DataSet ) )
			return Collections.EMPTY_LIST;

		return doValidate( design, (DataSet) element );
	}

	private List doValidate( ReportDesign design, DataSet toValidate )
	{

		List list = new ArrayList( );

		List columns = (List) toValidate.getProperty( design,
				DataSet.RESULT_SET_PROP );
		if ( columns != null && columns.size( ) == 0 )
		{
			list.add( new SemanticError( toValidate,
					SemanticError.DESIGN_EXCEPTION_AT_LEAST_ONE_COLUMN ) );
		}
		return list;
	}
}