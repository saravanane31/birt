/***********************************************************************
 * Copyright (c) 2009 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Actuate Corporation - initial API and implementation
 ***********************************************************************/
package org.eclipse.birt.report.engine.nLayout.area.impl;

import java.awt.Color;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.content.IContent;
import org.eclipse.birt.report.engine.content.IStyle;
import org.eclipse.birt.report.engine.content.ITextContent;
import org.eclipse.birt.report.engine.layout.pdf.font.FontInfo;
import org.eclipse.birt.report.engine.layout.pdf.util.PropertyUtil;
import org.eclipse.birt.report.engine.nLayout.LayoutContext;
import org.eclipse.birt.report.engine.nLayout.area.ILayout;
import org.eclipse.birt.report.engine.nLayout.area.style.TextStyle;
import org.w3c.dom.css.CSSValue;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;


public class TextAreaLayout implements ILayout
{
	protected static Logger logger = Logger.getLogger( TextAreaLayout.class.getName() );
	/**
	 * the parent Layout manager. LineLM for block text and InlineContainerLM for inline text.
	 */
	private InlineStackingArea parentLM;

	private TextCompositor comp = null;

	private ITextContent textContent = null;
	
	private static HashSet splitChar = new HashSet();
	
	static 
	{
		splitChar.add( new Character( ' ' ) );
		splitChar.add( new Character( '\r') );
		splitChar.add( new Character( '\n') );
	};

	public TextAreaLayout( ContainerArea parent, LayoutContext context,
			IContent content )
	{
		parentLM = (InlineStackingArea) parent;
		ITextContent textContent = (ITextContent) content;
		String text = textContent.getText( );
		if ( text != null && text.length( ) != 0 )
			transform( textContent );
		else
			textContent.setText( " " );
		
		this.textContent = textContent;
		comp = new TextCompositor( textContent, context.getFontManager( ),
				context );
		// checks whether the current line is empty or not.
		ContainerArea ancestor = parentLM;
		do
		{
			if ( null == ancestor )
			{
				// should never reach here.
				comp.setNewLineStatus( true );
				return;
			}
			if ( !ancestor.isEmpty( ) )
			{
				comp.setNewLineStatus( false );
				return;
			}
			if ( ancestor instanceof LineArea )
			{
				comp.setNewLineStatus( ancestor.isEmpty( ) );
				return;
			}
			ancestor = ancestor.getParent( );
		} while ( true );
	}
	
	public 	static TextStyle buildTextStyle( IContent content, FontInfo fontInfo )
	{
		IStyle style = content.getComputedStyle( );
		TextStyle textStyle = new TextStyle( fontInfo );
		CSSValue direction = style.getProperty( IStyle.STYLE_DIRECTION );
		if ( IStyle.RTL_VALUE.equals( direction ) )
		{
			textStyle.setDirection( TextStyle.DIRECTION_RTL );
		}
		textStyle.setFontSize( PropertyUtil.getDimensionValue( style
				.getProperty( IStyle.STYLE_FONT_SIZE ) ) );
		textStyle.setLetterSpacing( PropertyUtil.getDimensionValue( style
				.getProperty( IStyle.STYLE_LETTER_SPACING ) ) );
		textStyle.setWordSpacing( PropertyUtil.getDimensionValue( style
				.getProperty( IStyle.STYLE_WORD_SPACING ) ) );
		textStyle
				.setLineThrough( style
						.getProperty( IStyle.STYLE_TEXT_LINETHROUGH ) == IStyle.LINE_THROUGH_VALUE );
		textStyle
				.setOverLine( style.getProperty( IStyle.STYLE_TEXT_OVERLINE ) == IStyle.OVERLINE_VALUE );
		CSSValue underLine = style.getProperty( IStyle.STYLE_TEXT_UNDERLINE );
		if ( underLine == IStyle.UNDERLINE_VALUE )
		{
			textStyle.setUnderLine( true );
		}
		else
		{
			if ( content.getHyperlinkAction( ) != null )
			{
				textStyle.setUnderLine( true );
			}
		}
		textStyle.setAlign( style.getProperty( IStyle.STYLE_TEXT_ALIGN ) );
		IStyle s = content.getStyle( );
		Color color = PropertyUtil.getColor( s
				.getProperty( IStyle.STYLE_COLOR ) );
		if ( color != null )
		{
			textStyle.setColor( color );
		}
		else
		{
			if ( content.getHyperlinkAction( ) != null )
			{
				textStyle.setColor( Color.BLUE );
			}
			else
			{
				textStyle.setColor( PropertyUtil.getColor( style
						.getProperty( IStyle.STYLE_COLOR ) ) );
			}
		}
		return textStyle;

	}

	public void layout( ) throws BirtException
	{
		layoutChildren();
	}

	protected void layoutChildren( ) throws BirtException
	{
		if ( null == textContent )
			return;
		while ( comp.hasNextArea( ) )
		{
			TextArea area = comp.getNextArea( getFreeSpace( ) );
			//for a textArea which just has a line break. We should not add TextArea into the line.
			if( area != null )
			{
				addTextArea( area );
				comp.setNewLineStatus( false );
				if ( area.isLineBreak( ) )
				{
					newLine();
					comp.setNewLineStatus( true );
				}
			}
		}
	}
	
	protected boolean checkAvailableSpace( )
	{
		return false;
	}

	public void addTextArea( AbstractArea textArea ) throws BirtException
	{
		parentLM.add( textArea );
		parentLM.update( textArea );
	}
	
	/**
	 * true if succeed to new a line.
	 */
	public void newLine( ) throws BirtException
	{
		parentLM.endLine( );
	}

	public int getFreeSpace( )
	{
		return parentLM.getCurrentMaxContentWidth( );
	}


	public void transform( ITextContent textContent )
	{
		String transformType = textContent.getComputedStyle( )
				.getTextTransform( );
		if ( transformType.equalsIgnoreCase( "uppercase" ) ) //$NON-NLS-1$
		{
			textContent.setText( textContent.getText( ).toUpperCase( ) );
		}
		else if ( transformType.equalsIgnoreCase( "lowercase" ) ) //$NON-NLS-1$
		{
			textContent.setText( textContent.getText( ).toLowerCase( ) );
		}
		else if ( transformType.equalsIgnoreCase( "capitalize" ) ) //$NON-NLS-1$
		{
			textContent.setText( capitalize( textContent.getText( ) ) );
		}
		
		ArabicShaping shaping = new ArabicShaping(ArabicShaping.LETTERS_SHAPE);
		try
		{
			String shapingText =  shaping.shape( textContent.getText( ));
			textContent.setText(shapingText);
		}
		catch ( ArabicShapingException e )
		{
			logger.log( Level.WARNING, e.getMessage( ), e );
		}
	}

	private String capitalize( String text )
	{
		boolean capitalizeNextChar = true;
		char[] array = text.toCharArray( );
		for ( int i = 0; i < array.length; i++ )
		{
			Character c = new Character( text.charAt( i ) );
			if ( splitChar.contains( c ) )
				capitalizeNextChar = true;
			else if (capitalizeNextChar)
			{
				array[i] = Character.toUpperCase( array[i] );
				capitalizeNextChar = false;
			}
		}
		return new String(array);
	}

	public void close( )
	{
	}

	public void initialize( )
	{
	}

}
