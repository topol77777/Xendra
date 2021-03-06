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

import org.compiere.model.persistence.X_C_Commission;
import org.compiere.model.reference.REF_C_CommissionDocBasis;
import org.compiere.model.reference.REF_C_CommissionFrequency;
import org.compiere.util.*;

/**
 *	Model for Commission.
 *	(has Lines)
 *	
 *  @author Jorg Janke
 *  @version $Id: MCommission.java 5583 2015-08-05 14:11:58Z xapiens $
 */
public class MCommission extends X_C_Commission
{
	/**
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param C_Commission_ID id
	 *	@param trxName transaction
	 */
	public MCommission(Properties ctx, int C_Commission_ID, String trxName)
	{
		super(ctx, C_Commission_ID, trxName);
		if (C_Commission_ID == 0)
		{
		//	setName (null);
		//	setC_BPartner_ID (0);
		//	setC_Charge_ID (0);
		//	setC_Commission_ID (0);
		//	setC_Currency_ID (0);
			//
			setDocBasisType (REF_C_CommissionDocBasis.Invoice);	// I
			setFrequencyType (REF_C_CommissionFrequency.Monthly);	// M
			setListDetails (false);
		}
	}	//	MCommission

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MCommission(Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MCommission

	/**
	 * 	Get Lines
	 *	@return array of lines
	 */
	public MCommissionLine[] getLines()
	{
		String sql = "SELECT * FROM C_CommissionLine WHERE C_Commission_ID=? ORDER BY Line";
		ArrayList<MCommissionLine> list = new ArrayList<MCommissionLine>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql, get_TrxName());
			pstmt.setInt(1, getC_Commission_ID());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MCommissionLine(getCtx(), rs, get_TrxName()));
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e); 
		}
		try
		{
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		
		//	Convert
		MCommissionLine[] retValue = new MCommissionLine[list.size()];
		list.toArray(retValue);
		return retValue;
	}	//	getLines

	/**
	 * 	Set Date Last Run
	 *	@param DateLastRun date
	 */
	public void setDateLastRun (Timestamp DateLastRun)
	{
		if (DateLastRun != null)
			super.setDateLastRun(DateLastRun);
	}	//	setDateLastRun

	/**
	 * 	Copy Lines From other Commission
	 *	@param otherCom commission
	 *	@return number of lines copied
	 */
	public int copyLinesFrom (MCommission otherCom)
	{
		if (otherCom == null)
			return 0;
		MCommissionLine[] fromLines = otherCom.getLines ();
		int count = 0;
		for (int i = 0; i < fromLines.length; i++)
		{
			MCommissionLine line = new MCommissionLine (getCtx(), 0, get_TrxName());
			PO.copyValues(fromLines[i], line, getAD_Client_ID(), getAD_Org_ID());
			line.setC_Commission_ID (getC_Commission_ID());
			line.setC_CommissionLine_ID (0);	//	new
			if (line.save())
				count++;
		}
		if (fromLines.length != count)
			log.log(Level.SEVERE, "copyLinesFrom - Line difference - From=" + fromLines.length + " <> Saved=" + count);
		return count;
	}	//	copyLinesFrom

}	//	MCommission
