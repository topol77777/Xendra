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
package org.compiere.apps.graph;

import java.awt.*;
import java.awt.event.*;

import org.compiere.*;
import org.compiere.apps.*;
import org.compiere.swing.*;
import org.compiere.model.MGoal;


/**
 * 	Performance Detail Frame.
 * 	BarPanel for Drill-Down
 *	
 *  @author Jorg Janke
 *  @version $Id: PerformanceDetail.java 508 2007-11-24 23:06:53Z el_man $
 */
public class PerformanceDetail extends CFrame
	implements ActionListener
{
	/**
	 * 	Constructor.
	 * 	Called from PAPanel, ViewPI (Performance Indicator)
	 *	@param goal goal
	 */
	public PerformanceDetail (MGoal goal)
	{
		super (goal.getName());
		setIconImage(Xendra.getImage16());
		barPanel = new BarGraph(goal);
		init();
		AEnv.showCenterScreen(this);
	}	//	PerformanceDetail

	BarGraph barPanel = null;
	ConfirmPanel confirmPanel = new ConfirmPanel();

	/**
	 * 	Static init
	 */
	private void init()
	{
		getContentPane().add(barPanel, BorderLayout.NORTH);
		
		getContentPane().add(confirmPanel, BorderLayout.SOUTH);
		confirmPanel.addActionListener(this);
	}	//	init
	
	/**
	 * 	Action Listener
	 *	@param e event
	 */
	public void actionPerformed (ActionEvent e)
	{
		if (e.getActionCommand().equals(ConfirmPanel.A_OK))
			dispose();
	}	//	actionPerformed
	
}	//	PerformanceDetail
