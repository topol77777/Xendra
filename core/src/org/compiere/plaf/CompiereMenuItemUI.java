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
package org.compiere.plaf;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

/**
 * 	Menu Item UI
 *	
 *  @author Jorg Janke
 *  @version $Id: CompiereMenuItemUI.java,v 1.1 2007/06/14 23:44:16 sergioaguayo Exp $
 */
public class CompiereMenuItemUI extends BasicMenuItemUI
{
	/**
	 *  Create own instance
	 *  @param c compoment
	 *  @return AdempiereMenuBarUI
	 */
    public static ComponentUI createUI(JComponent c) 
    {
        return new CompiereMenuItemUI();
    }	//	createUI

	/**
	 *  Install UI
	 *  @param c
	 */
	public void installUI (JComponent c)
	{
		super.installUI(c);
		c.setOpaque(false);		//	use MenuBarUI background
	}   //  installUI

	/**
	 * 	Update UI
	 *	@param g graphics
	 *	@param c component
	 */
	public void update (Graphics g, JComponent c)
	{
		if (c.isOpaque())
		{
			CompiereColor bg = CompiereColor.getDefaultBackground();
			bg.paint (g, c);
			//
		//	g.setColor(c.getBackground());
		//	g.fillRect(0,0, c.getWidth(), c.getHeight());
		}
		paint(g,c);
	}	//	update

	/**
	 * 	Paint Background
	 *	@param g graphics
	 *	@param menuItem menu
	 *	@param bgColor bg color
	 */
	protected void paintBackground (Graphics g, JMenuItem menuItem, Color bgColor)
	{
		ButtonModel model = menuItem.getModel();
		if (model.isArmed())
			super.paintBackground (g, menuItem, bgColor);
		else
		{
			CompiereColor bg = CompiereColor.getDefaultBackground();
			bg.paint (g, menuItem);
		}
	}	//	paintBackground
	
}	//	AdempiereMenuItemUI
