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

import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.model.persistence.X_C_InvoiceTax;
import org.compiere.util.*;

/**
 *	Invoice Tax Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MInvoiceTax.java 3654 2011-11-04 01:49:49Z xapiens $
 */
public class MInvoiceTax extends X_C_InvoiceTax
{
	/**
	 * 	Get Tax Line for Invoice Line
	 *	@param line invoice line
	 *	@param precision currency precision
	 *	@param oldTax if true old tax is returned
	 *	@param trxName transaction name
	 *	@return existing or new tax
	 */
	public static MInvoiceTax get (MInvoiceLine line, int precision, 
		boolean oldTax, String trxName)
	{
		MInvoiceTax retValue = null;
		if (line == null || line.getC_Invoice_ID() == 0)
			return null;
		int C_Tax_ID = line.getC_Tax_ID();
		if (oldTax && line.is_ValueChanged("C_Tax_ID"))
		{
			Object old = line.get_ValueOld("C_Tax_ID");
			if (old == null)
				return null;
			C_Tax_ID = ((Integer)old).intValue();
		}
		if (C_Tax_ID == 0)
		{
			s_log.warning("C_Tax_ID=0");
			return null;
		}
		
		String sql = "SELECT * FROM C_InvoiceTax WHERE C_Invoice_ID=? AND C_Tax_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, trxName);
			pstmt.setInt (1, line.getC_Invoice_ID());
			pstmt.setInt (2, C_Tax_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				retValue = new MInvoiceTax (line.getCtx(), rs, trxName);
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, sql, e); 
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		if (retValue != null)
		{
			retValue.set_TrxName(trxName);
			retValue.setPrecision(precision);
			s_log.fine("(old=" + oldTax + ") " + retValue);
			return retValue;
		}
		
		//	Create New
		retValue = new MInvoiceTax(line.getCtx(), 0, trxName);
		retValue.set_TrxName(trxName);
		retValue.setClientOrg(line);
		retValue.setC_Invoice_ID(line.getC_Invoice_ID());
		retValue.setC_Tax_ID(line.getC_Tax_ID());
		retValue.setPrecision(precision);
		retValue.setIsTaxIncluded(line.isTaxIncluded());
		s_log.fine("(new) " + retValue);
		return retValue;
	}	//	get
	
	/**	Static Logger	*/
	private static CLogger	s_log	= CLogger.getCLogger (MInvoiceTax.class);
	
	
	/**************************************************************************
	 * 	Persistency Constructor
	 *	@param ctx context
	 *	@param ignored ignored
	 *	@param trxName transaction
	 */
	public MInvoiceTax (Properties ctx, int ignored, String trxName)
	{
		super(ctx, 0, trxName);
		if (ignored != 0)
			throw new IllegalArgumentException("Multi-Key");
		setTaxAmt (Env.ZERO);
		setTaxBaseAmt (Env.ZERO);
		setIsTaxIncluded(false);
	}	//	MInvoiceTax

	/**
	 * 	Load Constructor.
	 * 	Set Precision and TaxIncluded for tax calculations!
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MInvoiceTax (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MInvoiceTax
	
	/** Tax							*/
	private MTax 		m_tax = null;
	/** Cached Precision			*/
	private Integer		m_precision = null;
	
	
	/**
	 * 	Get Precision
	 * 	@return Returns the precision or 2
	 */
	private int getPrecision ()
	{
		if (m_precision == null)
			return 2;
		return m_precision.intValue();
	}	//	getPrecision

	/**
	 * 	Set Precision
	 *	@param precision The precision to set.
	 */
	protected void setPrecision (int precision)
	{
		m_precision = new Integer(precision);
	}	//	setPrecision

	/**
	 * 	Get Tax
	 *	@return tax
	 */
	protected MTax getTax()
	{
		if (m_tax == null)
			m_tax = MTax.get(getCtx(), getC_Tax_ID());
		return m_tax;
	}	//	getTax
	
	
	/**************************************************************************
	 * 	Calculate/Set Tax Base Amt from Invoice Lines
	 * 	@return true if tax calculated
	 */
	public boolean calculateTaxFromLines ()
	{
		BigDecimal taxBaseAmt = Env.ZERO;
		BigDecimal taxAmt = Env.ZERO;
		//
		boolean documentLevel = getTax().isDocumentLevel();
		MTax tax = getTax();
		//
		String sql = "SELECT il.LineNetAmt, COALESCE(il.TaxAmt,0), i.IsSOTrx "
			+ "FROM C_InvoiceLine il"
			+ " INNER JOIN C_Invoice i ON (il.C_Invoice_ID=i.C_Invoice_ID) "
			+ "WHERE il.C_Invoice_ID=? AND il.C_Tax_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setInt (1, getC_Invoice_ID());
			pstmt.setInt (2, getC_Tax_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				//	BaseAmt
				BigDecimal baseAmt = rs.getBigDecimal(1);
				taxBaseAmt = taxBaseAmt.add(baseAmt);
				//	TaxAmt
				BigDecimal amt = rs.getBigDecimal(2);
				if (amt == null)
					amt = Env.ZERO;
				boolean isSOTrx = "Y".equals(rs.getString(3));
				//
				if (documentLevel || baseAmt.signum() == 0)
					amt = Env.ZERO;
				else if (amt.signum() != 0 && !isSOTrx)	//	manually entered
					;
				else	// calculate line tax
					amt = tax.calculateTax(baseAmt, isTaxIncluded(), getPrecision());
				//
				if ( tax.isSubstract() )
					taxAmt = taxAmt.subtract(amt);
				else
					taxAmt = taxAmt.add(amt);
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "setTaxBaseAmt", e);
			taxBaseAmt = null;
		}
		try
		{
			if (pstmt != null)
				pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			pstmt = null;
		}
		if (taxBaseAmt == null)
			return false;
		
		//	Calculate Tax
		if (documentLevel || taxAmt.signum() == 0)
			taxAmt = tax.calculateTax(taxBaseAmt, isTaxIncluded(), getPrecision());
		setTaxAmt(taxAmt);

		//	Set Base
		if (isTaxIncluded())
			setTaxBaseAmt (taxBaseAmt.subtract(taxAmt));
		else
			setTaxBaseAmt (taxBaseAmt);
		return true;
	}	//	calculateTaxFromLines

	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MInvoiceTax[");
		sb.append("C_Invoice_ID=").append(getC_Invoice_ID())
			.append(",C_Tax_ID=").append(getC_Tax_ID())
			.append(", Base=").append(getTaxBaseAmt()).append(",Tax=").append(getTaxAmt())
			.append ("]");
		return sb.toString ();
	}	//	toString

}	//	MInvoiceTax
