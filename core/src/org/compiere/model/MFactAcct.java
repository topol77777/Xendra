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
import java.util.logging.*;

import org.xendra.exceptions.DBException;
import org.compiere.model.persistence.X_Fact_Acct;
import org.compiere.util.*;


/**
 *	Accounting Fact Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MFactAcct.java 4091 2012-04-10 20:33:02Z xapiens $
 */
public class MFactAcct extends X_Fact_Acct
{
	/**
	 * 	Delete Accounting
	 *	@param AD_Table_ID table
	 *	@param Record_ID record
	 *	@param trxName transaction
	 *	@return number of rows or -1 for error
	 */
	public static int delete (int AD_Table_ID, int Record_ID, String trxName)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM Fact_Acct WHERE AD_Table_ID=").append(AD_Table_ID)
			.append(" AND Record_ID=").append(Record_ID);
		int no = DB.executeUpdate(sb.toString(), trxName);
		if (no == -1)
			s_log.log(Level.SEVERE, "failed: AD_Table_ID=" + AD_Table_ID + ", Record_ID" + Record_ID);
		else
			s_log.fine("delete - AD_Table_ID=" + AD_Table_ID 
				+ ", Record_ID=" + Record_ID + " - #" + no);
		return no;
	}	//	delete

	/**	Static Logger	*/
	private static CLogger	s_log	= CLogger.getCLogger (MFactAcct.class);
	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param Fact_Acct_ID id
	 *	@param trxName transaction
	 */
	public MFactAcct (Properties ctx, int Fact_Acct_ID, String trxName)
	{
		super (ctx, Fact_Acct_ID, trxName);
	}	//	MFactAcct

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MFactAcct (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MFactAcct

	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MFactAcct[");
		sb.append(get_ID()).append("-Acct=").append(getAccount_ID())
			.append(",Dr=").append(getAmtSourceDr()).append("|").append(getAmtAcctDr())
			.append(",Cr=").append(getAmtSourceCr()).append("|").append(getAmtAcctCr())
			.append ("]");
		return sb.toString ();
	}	//	toString

	/**
	 * 	Derive MAccount from record
	 *	@return Valid Account Combination
	 */
	public MAccount getMAccount()
	{
		MAccount acct = MAccount.get (getCtx(), getAD_Client_ID(), getAD_Org_ID(),
			getC_AcctSchema_ID(), getAccount_ID(), getC_SubAcct_ID(),
			getM_Product_ID(), getC_BPartner_ID(), getAD_OrgTrx_ID(), 
			getC_LocFrom_ID(), getC_LocTo_ID(), getC_SalesRegion_ID(), 
			getC_Project_ID(), getC_Campaign_ID(), getC_Activity_ID(),
			getUser1_ID(), getUser2_ID(), getUserElement1_ID(), getUserElement2_ID());
		if (acct != null && acct.get_ID() == 0)
			acct.save();
		return acct;
	}	//	getMAccount
	
	/**
	 * Check if a document is already posted
	 * @param AD_Table_ID table
	 * @param Record_ID record
	 * @param trxName transaction
	 * @return boolean indicating if the document has already accounting facts
	 * @throws DBException on database exception
	 */
	public static boolean alreadyPosted(int AD_Table_ID, int Record_ID, String trxName) throws DBException
	{
		final String sql = "SELECT 1 FROM Fact_Acct WHERE AD_Table_ID=? AND Record_ID=?";
		int one = DB.getSQLValue(trxName, sql, new Object[]{AD_Table_ID, Record_ID});
		return (one == 1);
	}

	
}	//	MFactAcct
