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
package org.compiere.model;

import java.sql.*;
import java.util.*;

import org.compiere.model.persistence.X_M_CostType;
import org.compiere.util.*;

/**
 * 	Cost Type Model
 *  @author Jorg Janke
 *  @version $Id: MCostType.java 3654 2011-11-04 01:49:49Z xapiens $
 */
public class MCostType extends X_M_CostType
{
	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param M_CostType_ID id
	 *	@param trxName trx
	 */
	public MCostType (Properties ctx, int M_CostType_ID, String trxName)
	{
		super (ctx, M_CostType_ID, trxName);
	}	//	MCostType

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName trx
	 */
	public MCostType (Properties ctx, ResultSet rs, String trxName)
	{
		super (ctx, rs, trxName);
	}	//	MCostType
	
	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MCostType[");
		sb.append (get_ID()).append ("-").append (getName ()).append ("]");
		return sb.toString ();
	}	//	toString
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		if (getAD_Org_ID() != 0)
			setAD_Org_ID(0);
		return true;
	}	//	beforeSave

	/**
	 * 	Before Delete
	 *	@return true if it can be deleted
	 */
	protected boolean beforeDelete ()
	{
		MAcctSchema[] ass = MAcctSchema.getClientAcctSchema(getCtx(), getAD_Client_ID());
		for (int i = 0; i < ass.length; i++)
		{
			if (ass[i].getM_CostType_ID() == getM_CostType_ID())
			{
				log.saveError("CannotDelete", Msg.getElement(getCtx(), "C_AcctSchema_ID")
					+ " - " + ass[i].getName());
				return false;
			}
		}
		return true;
	}	//	beforeDelete
	
}	//	MCostType
