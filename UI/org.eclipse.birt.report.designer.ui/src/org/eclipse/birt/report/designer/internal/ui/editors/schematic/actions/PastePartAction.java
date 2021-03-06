/*************************************************************************************
 * Copyright (c) 2004 Actuate Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Actuate Corporation - Initial implementation.
 ************************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions;

import org.eclipse.birt.report.designer.internal.ui.views.actions.PasteAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Paste action
 */
public class PastePartAction extends WrapperSelectionAction
{

	/**
	 * Create a new paste action with given selection and default text
	 * 
	 * @param part
	 *  
	 */
	public PastePartAction( IWorkbenchPart part )
	{
		super( part );
		ISharedImages shareImages = PlatformUI.getWorkbench( )
				.getSharedImages( );
		setImageDescriptor( shareImages.getImageDescriptor( ISharedImages.IMG_TOOL_PASTE ) );
		setDisabledImageDescriptor( shareImages.getImageDescriptor( ISharedImages.IMG_TOOL_PASTE_DISABLED ) );
		setAccelerator( SWT.CTRL | 'V' ); //$NON-NLS-1$
	}

	public String getId( )
	{
		return ActionFactory.PASTE.getId( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.report.designer.internal.ui.editors.schematic.actions.WrapperSelectionAction#createActionHandler(org.eclipse.jface.viewers.ISelection)
	 */
	protected IAction createActionHandler( ISelection model )
	{
		Object target = null;
		if ( model instanceof IStructuredSelection )
		{
			target = ( (IStructuredSelection) model ).getFirstElement( );
		}
		return new PasteAction( target );
	}
}