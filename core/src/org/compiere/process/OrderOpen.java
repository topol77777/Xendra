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
package org.compiere.process;

import java.util.logging.*;

import org.compiere.model.MOrder;
import org.compiere.model.reference.REF__DocumentStatus;
import org.compiere.util.DisplayType;

import org.xendra.annotations.*; 
/**
 *	Re-Open Order Process (from Closed to Completed)
 *	
 *  @author Jorg Janke
 *  @version $Id: OrderOpen.java 5583 2015-08-05 14:11:58Z xapiens $
 */
@XendraProcess(value="C_Order Open",
name="Reopen Order",
description="Open previously closed Order",
help="",
Identifier="5e1e9f93-0e5d-fb72-28c7-aa5b4137c42c",
classname="org.compiere.process.OrderOpen",
updated="2015-06-20 10:10:12")	
public class OrderOpen extends SvrProcess
{
	/**	The Order				*/
	@XendraProcessParameter(Name="Order",
			                ColumnName="C_Order_ID",
			                Description="Order",
			                Help="The Order is a control document.  The  Order is complete when the quantity ordered is the same as the quantity shipped and invoiced.  When you cloase an order, unshipped (backordered) quantities are cancelled.",
			                AD_Reference_ID=DisplayType.Search,
			                SeqNo=10,
			                ReferenceValueID="",
			                ValRuleID="",
			                FieldLength=0,
			                IsMandatory=true,
			                IsRange=false,
			                DefaultValue="",
			                DefaultValue2="",
			                vFormat="",
			                valueMin="",
			                valueMax="",
			                DisplayLogic="",
			                ReadOnlyLogic="",
			                Identifier="afe7f7f0-9413-3c13-08bc-8cf85e11bb8c")	
	private int		p_C_Order_ID = 0;

	/**
	 *  Prepare - e.g., get Parameters.
	 */
	protected void prepare()
	{
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if (para[i].getParameter() == null)
				;
			else if (name.equals("C_Order_ID"))
				p_C_Order_ID = para[i].getParameterAsInt();
			else
				log.log(Level.SEVERE, "prepare - Unknown Parameter: " + name);
		}
	}	//	prepare

	/**
	 *  Perrform process.
	 *  @return Message
	 *  @throws Exception if not successful
	 */
	protected String doIt() throws Exception
	{
		log.info("doIt - Open C_Order_ID=" + p_C_Order_ID);
		if (p_C_Order_ID == 0)
			throw new IllegalArgumentException("C_Order_ID == 0");
		//
		MOrder order = new MOrder (getCtx(), p_C_Order_ID, get_TrxName());
		if (REF__DocumentStatus.Closed.equals(order.getDocStatus()))
		{
			order.setDocStatus(REF__DocumentStatus.Completed);
			return order.save() ? "@OK@" : "@Error@";
		}
		else
			throw new IllegalStateException("Order is not closed");
	}	//	doIt
	
}	//	OrderOpen
