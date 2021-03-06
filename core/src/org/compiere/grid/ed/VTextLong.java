/******************************************************************************
 * Product: Xendra ERP & CRM Smart Business Solution                        *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.grid.ed;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

import org.xendra.plaf.XendraPLAF;
import org.compiere.plaf.*;
import org.compiere.swing.*;
import org.compiere.util.*;

/**
 *	Long Text (CBLOB) Editor 	
 *	
 *  @author Jorg Janke
 *  @version $Id: VTextLong.java 865 2008-03-20 00:23:42Z el_man $
 */
public class VTextLong extends CTextPane
	implements VEditor, KeyListener, ActionListener
{
	/**
	 *	Factory: Start Editor
	 *	@param jc container to get parent frame
	 *	@param header heading
	 *	@param text initial text
	 *	@param editable if false = r/o
	 *	@return edited string
	 */
	public static String startEditor(Container jc, String header, String text, boolean editable)
	{
		//	Find frame
		JFrame frame = Env.getFrame(jc);
		//	Start it
		HTMLEditor ed = new HTMLEditor (frame, header, text, editable);
		String s = ed.getHtmlText();
		ed = null;
		return s;
	}	//	startEditor

	/**
	 *	Standard Constructor
	 *  @param columnName column name
	 *  @param mandatory mandatory
	 *  @param isReadOnly read only
	 *  @param isUpdateable updateable
	 *  @param displayLength display length
	 *  @param fieldLength field length
	 */
	public VTextLong (String columnName, boolean mandatory, boolean isReadOnly, boolean isUpdateable, int displayLength, int fieldLength)
	{
		super ();
		super.setName(columnName);
		LookAndFeel.installBorder(this, "TextField.border");
		setPreferredSize(new Dimension (500,80));

		//  Create Editor
		setForeground(XendraPLAF.getTextColor_Normal());
		setBackground(XendraPLAF.getFieldBackground_Normal());

		setMandatory(mandatory);
		m_columnName = columnName;

		if (isReadOnly || !isUpdateable)
			setReadWrite(false);
		addKeyListener(this);

		//	Popup
		addMouseListener(new VTextLong_mouseAdapter(this));
		menuEditor = new CMenuItem(Msg.getMsg(Env.getCtx(), "Editor"), Env.getImageIcon("Editor16.gif"));
		menuEditor.addActionListener(this);
		popupMenu.add(menuEditor);
	}	//	VText

	/**
	 *  Dispose
	 */
	public void dispose()
	{
	}   //  dispose

	JPopupMenu          		popupMenu = new JPopupMenu();
	private CMenuItem 			menuEditor;

	private String				m_columnName;
	private String				m_oldText;
	private String				m_initialText;
	private volatile boolean	m_setting = false;
	/**	Logger			*/
	private static CLogger log = CLogger.getCLogger(VTextLong.class);

	/**
	 *	Set Editor to value
	 *  @param value value
	 */
	public void setValue(Object value)
	{
		if (value == null)
			m_oldText = "";
		else
			m_oldText = value.toString();
		if (m_setting)
			return;
		super.setValue(m_oldText);
		m_initialText = m_oldText;
		//	Always position Top 
		setCaretPosition(0);
	}	//	setValue

	/**
	 *  Property Change Listener
	 *  @param evt event
	 */
	public void propertyChange (PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals(org.compiere.model.GridField.PROPERTY))
			setValue(evt.getNewValue());
	}   //  propertyChange

	/**
	 *	ActionListener
	 *  @param e event
	 */
	public void actionPerformed(ActionEvent e)
	{
		log.finest( "VTestLong.actionPerformed - " + e.getActionCommand());
		if (e.getSource() == menuEditor)
		{
			menuEditor.setEnabled(false);
			String s = VTextLong.startEditor (this, Msg.translate(Env.getCtx(), m_columnName), getText(), isEditable());
			menuEditor.setEnabled(true);
			//
			//	Data Binding
			try
			{
				fireVetoableChange(m_columnName, m_oldText, s);
			}
			catch (PropertyVetoException pve)	{}
		}
	}	//	actionPerformed

	/**
	 *  Action Listener Interface - NOP
	 *  @param listener listener
	 */
	public void addActionListener(ActionListener listener)
	{
	}   //  addActionListener

	/**************************************************************************
	 *	Key Listener Interface
	 *  @param e event
	 */
	public void keyTyped(KeyEvent e)	{}
	public void keyPressed(KeyEvent e)	{}

	/**
	 * 	Key Released
	 *	if Escape restore old Text.
	 *  @param e event
	 */
	public void keyReleased(KeyEvent e)
	{
		//  ESC
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			setText(m_initialText);
		m_setting = true;
		try
		{
			fireVetoableChange(m_columnName, m_oldText, getText());
		}
		catch (PropertyVetoException pve)	{}
		m_setting = false;
	}	//	keyReleased

	/**
	 *  Set Field/WindowNo for ValuePreference (NOP)
	 *  @param mField field model
	 */
	public void setField (org.compiere.model.GridField mField)
	{
	}   //  setField


}	//	VTextLong

/*****************************************************************************/

/**
 *	Mouse Listener
 */
final class VTextLong_mouseAdapter extends MouseAdapter
{
	/**
	 *	Constructor
	 *  @param adaptee VText
	 */
	VTextLong_mouseAdapter(VTextLong adaptee)
	{
		this.adaptee = adaptee;
	}	//	VText_mouseAdapter

	private VTextLong adaptee;

	/**
	 *	Mouse Listener
	 *  @param e event
	 */
	public void mouseClicked(MouseEvent e)
	{
		//	popup menu
		if (SwingUtilities.isRightMouseButton(e))
			adaptee.popupMenu.show((Component)e.getSource(), e.getX(), e.getY());
	}	//	mouse Clicked

}	//	VTextLong_mouseAdapter
