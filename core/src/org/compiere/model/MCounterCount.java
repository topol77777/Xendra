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

import java.util.*;
import java.sql.*;

import org.compiere.model.persistence.X_W_CounterCount;

/**
 * 	Web Counter (header)
 *
 *  @author Jorg Janke
 *  @version $Id: MCounterCount.java 3654 2011-11-04 01:49:49Z xapiens $
 */
public class MCounterCount extends X_W_CounterCount
{
	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param W_CounterCount_ID id
	 *	@param trxName transaction
	 */
	public MCounterCount (Properties ctx, int W_CounterCount_ID, String trxName)
	{
		super (ctx, W_CounterCount_ID, trxName);
		if (W_CounterCount_ID == 0)
		{
			setCounter (0);
		//	setName (null);
		//	setPageURL (null);
		}
	}	//	MCounterCount

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MCounterCount (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MCounterCount

}	//	MCounterCount
