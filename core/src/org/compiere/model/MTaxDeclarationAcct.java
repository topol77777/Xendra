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

import org.compiere.model.persistence.X_C_TaxDeclarationAcct;

/**
 * 	Tax Tax Declaration Accounting Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MTaxDeclarationAcct.java 3654 2011-11-04 01:49:49Z xapiens $
 */
public class MTaxDeclarationAcct extends X_C_TaxDeclarationAcct
{
	/**
	 * 	Standard Constructor
	 *	@param ctx ctx
	 *	@param C_TaxDeclarationAcct_ID id
	 *	@param trxName trc
	 */
	public MTaxDeclarationAcct (Properties ctx, int C_TaxDeclarationAcct_ID, String trxName)
	{
		super (ctx, C_TaxDeclarationAcct_ID, trxName);
	}	//	MTaxDeclarationAcct

	/**
	 * 	Load Constructor
	 *	@param ctx ctx
	 *	@param rs result set
	 *	@param trxName trx
	 */
	public MTaxDeclarationAcct (Properties ctx, ResultSet rs, String trxName)
	{
		super (ctx, rs, trxName);
	}	//	MTaxDeclarationAcct

	/**
	 * 	Parent Constructor
	 *	@param parent parent
	 *	@param fact fact
	 */
	public MTaxDeclarationAcct (MTaxDeclaration parent, MFactAcct fact)
	{
		super (parent.getCtx(), 0, parent.get_TrxName());
		setClientOrg(fact);
		setC_TaxDeclaration_ID(parent.getC_TaxDeclaration_ID());
		//
		setFact_Acct_ID (fact.getFact_Acct_ID());
		setC_AcctSchema_ID (fact.getC_AcctSchema_ID());
	}	//	MTaxDeclarationAcct

}	//	MTaxDeclarationAcct
