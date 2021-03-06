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
import java.sql.Date;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.model.persistence.X_C_Conversion_Rate;
import org.compiere.util.*;
import org.xendra.exceptions.ConversionRateException;

/**
 *	Currency Conversion Rate Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MConversionRate.java 4366 2012-09-11 22:22:27Z xapiens $
 */
public class MConversionRate extends X_C_Conversion_Rate
{
	/**	Logger						*/
	private static CLogger		s_log = CLogger.getCLogger (MConversionRate.class);


	/**
	 *	Convert an amount to base Currency
	 *	@param ctx context
	 *  @param CurFrom_ID  The C_Currency_ID FROM
	 *  @param ConvDate conversion date - if null - use current date
	 *  @param C_ConversionType_ID conversion rate type - if 0 - use Default
	 *  @param Amt amount to be converted
	 * 	@param AD_Client_ID client
	 * 	@param AD_Org_ID organization
	 *  @return converted amount
	 */
	public static BigDecimal convertBase (Properties ctx, int C_DocType_ID, 
		BigDecimal Amt, int CurFrom_ID, 
		Timestamp ConvDate, int C_ConversionType_ID, 
		int AD_Client_ID, int AD_Org_ID)
	{
		return convert (ctx, Amt, CurFrom_ID, MClient.get(ctx).getC_Currency_ID(), 
			ConvDate, C_ConversionType_ID, AD_Client_ID, AD_Org_ID);
	}	//	convertBase

	
	/**
	 *  Convert an amount with today's default rate
	 *	@param ctx context
	 *  @param CurFrom_ID  The C_Currency_ID FROM
	 *  @param CurTo_ID    The C_Currency_ID TO
	 *  @param Amt amount to be converted
	 * 	@param AD_Client_ID client
	 * 	@param AD_Org_ID organization
	 *  @return converted amount
	 */
	public static BigDecimal convert (Properties ctx,
		BigDecimal Amt, int CurFrom_ID, int CurTo_ID, 
		int AD_Client_ID, int AD_Org_ID)
	{		
		return convert (ctx, Amt, CurFrom_ID, CurTo_ID, Env.TODAY, 0, AD_Client_ID, AD_Org_ID);
	}   //  convert

	/**
	 *	Convert an amount
	 *	@param ctx context
	 *  @param CurFrom_ID  The C_Currency_ID FROM
	 *  @param CurTo_ID    The C_Currency_ID TO
	 *  @param ConvDate conversion date - if null - use current date
	 *  @param C_ConversionType_ID conversion rate type - if 0 - use Default
	 *  @param Amt amount to be converted
	 * 	@param AD_Client_ID client
	 * 	@param AD_Org_ID organization
	 *  @return converted amount or null if no rate
	 */
	public static BigDecimal convert (Properties ctx,  
		BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, int C_ConversionType_ID, 
		int AD_Client_ID, int AD_Org_ID, Boolean ThrowException)
	{
		if (Amt == null)
			throw new IllegalArgumentException("Required parameter missing - Amt");
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//	Get Rate
		BigDecimal retValue = getRate (CurFrom_ID, CurTo_ID, 
			ConvDate, C_ConversionType_ID,
			AD_Client_ID, AD_Org_ID);
		if (retValue == null)
		{
			if (!ThrowException)
				return null;
			else
				throw new ConversionRateException(Amt, CurFrom_ID, CurTo_ID, C_ConversionType_ID, ConvDate );
		}
			
		//	Get Amount in Currency Precision
		retValue = retValue.multiply(Amt);
		int stdPrecision = MCurrency.getStdPrecision(ctx, CurTo_ID);
		if (retValue.scale() > stdPrecision)
			retValue = retValue.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
			
		return retValue;
	}	//	convert

	/**
	 *	Convert an amount
	 *	@param ctx context
	 *  @param CurFrom_ID  The C_Currency_ID FROM
	 *  @param CurTo_ID    The C_Currency_ID TO
	 *  @param ConvDate conversion date - if null - use current date
	 *  @param C_ConversionType_ID conversion rate type - if 0 - use Default
	 *  @param Amt amount to be converted
	 * 	@param AD_Client_ID client
	 * 	@param AD_Org_ID organization
	 *  @return converted amount or null if no rate
	 */
	public static BigDecimal convert (Properties ctx, int C_DocType_ID, 
		BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate,Timestamp payDate,  int C_ConversionType_ID, 
		int AD_Client_ID, int AD_Org_ID, Boolean ThrowException)
	{
		if (Amt == null)
			throw new IllegalArgumentException("Required parameter missing - Amt");
		if (CurFrom_ID == CurTo_ID || Amt.equals(Env.ZERO))
			return Amt;
		//	Get Rate
		BigDecimal retValue = getRate (C_DocType_ID, CurFrom_ID, CurTo_ID, 
			ConvDate, payDate, C_ConversionType_ID,
			AD_Client_ID, AD_Org_ID);
		if (retValue == null)
		{
			if (!ThrowException)
				return null;
			else
				throw new ConversionRateException(Amt, CurFrom_ID, CurTo_ID, C_ConversionType_ID, ConvDate );
		}
			
		//	Get Amount in Currency Precision
		retValue = retValue.multiply(Amt);
		int stdPrecision = MCurrency.getStdPrecision(ctx, CurTo_ID);
		if (retValue.scale() > stdPrecision)
			retValue = retValue.setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);
			
		return retValue;
	}	//	convert
	
	/**
	 *	Convert an amount
	 *	@param ctx context
	 *  @param CurFrom_ID  The C_Currency_ID FROM
	 *  @param CurTo_ID    The C_Currency_ID TO
	 *  @param ConvDate conversion date - if null - use current date
	 *  @param C_ConversionType_ID conversion rate type - if 0 - use Default
	 *  @param Amt amount to be converted
	 * 	@param AD_Client_ID client
	 * 	@param AD_Org_ID organization
	 *  @return converted amount or null if no rate
	 */
	public static BigDecimal convert (Properties ctx,
		BigDecimal Amt, int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, int C_ConversionType_ID, 
		int AD_Client_ID, int AD_Org_ID)
	{
		return convert(ctx, Amt, CurFrom_ID, CurTo_ID, ConvDate, C_ConversionType_ID, AD_Client_ID, AD_Org_ID, false);
	}	//	convert
	
	/**
	 *	Get Currency Conversion Rate
	 *  @param  CurFrom_ID  The C_Currency_ID FROM
	 *  @param  CurTo_ID    The C_Currency_ID TO
	 *  @param  ConvDate    The Conversion date - if null - use current date
	 *  @param  ConversionType_ID Conversion rate type - if 0 - use Default
	 * 	@param	AD_Client_ID client
	 * 	@param	AD_Org_ID	organization
	 *  @return currency Rate or null
	 */
	public static BigDecimal getRate ( int CurFrom_ID, int CurTo_ID,
		Timestamp ConvDate, int ConversionType_ID, int AD_Client_ID, int AD_Org_ID)
	{
		BigDecimal retValue = null;
		try
		{
//			CREATE OR REPLACE FUNCTION currencyrate(p_doctype_id numeric, 
//						p_curfrom_id numeric, 
//						p_curto_id numeric, 
//						p_dateorigin timestamp with time zone, 
//						p_datepay timestamp with time zone, 
//						p_conversiontype_id numeric, p_client_id numeric, p_org_id numeric)
			// Identifier=5c3d0ae1-8b25-e641-c7e0-ef59338e2567
			retValue = DB.getSQLValueBD(null, "SELECT xendra.currencyrate(?,?,?,?,?,?)", CurFrom_ID, CurTo_ID, ConvDate, ConversionType_ID, AD_Client_ID, AD_Org_ID);
			if (retValue == null)
			{
				retValue = DB.getSQLValueBD(null, "SELECT xendra.currencyrate(?,?,?,?,?,?)", CurFrom_ID, CurTo_ID, ConvDate, ConversionType_ID, AD_Client_ID, 0);
			}
		}
		catch (Exception e)
		{
			s_log.log(Level.SEVERE, "getRate", e);
		}
//		if (CurFrom_ID == CurTo_ID)
//			return Env.ONE;
//		//	Conversion Type
//		int C_ConversionType_ID = ConversionType_ID;
//		if (C_ConversionType_ID == 0)
//			return null;
//			//C_ConversionType_ID = MConversionType.getDefault(AD_Client_ID);
//		//	Conversion Date
//		if (ConvDate == null)
//			ConvDate = new Timestamp (System.currentTimeMillis());
//
//		//	Get Rate
//		String sql = "SELECT MultiplyRate "
//			+ "FROM C_Conversion_Rate "
//			+ "WHERE C_Currency_ID=?"					//	#1
//			+ " AND C_Currency_ID_To=?"					//	#2
//			+ " AND	C_ConversionType_ID=?"				//	#3
//			+ " AND	? BETWEEN ValidFrom AND ValidTo"	//	#4	TRUNC (?) ORA-00932: inconsistent datatypes: expected NUMBER got TIMESTAMP
//			+ " AND AD_Client_ID IN (0,?)"				//	#5
//			+ " AND AD_Org_ID IN (0,?) "				//	#6
//			+ "ORDER BY AD_Client_ID DESC, AD_Org_ID DESC, ValidFrom DESC";
//		BigDecimal retValue = null;
//		PreparedStatement pstmt = null;
//		try
//		{
//			pstmt = DB.prepareStatement(sql, null);
//			pstmt.setInt(1, CurFrom_ID);
//			pstmt.setInt(2, CurTo_ID);
//			pstmt.setInt(3, C_ConversionType_ID);
//			//pstmt.setTimestamp(4, ConvDate);
//			pstmt.setDate(4, new Date(ConvDate.getTime()));
//			pstmt.setInt(5, AD_Client_ID);
//			pstmt.setInt(6, AD_Org_ID);
//			ResultSet rs = pstmt.executeQuery();
//			if (rs.next())
//				retValue = rs.getBigDecimal(1);
//			rs.close();
//			pstmt.close();
//			pstmt = null;
//		}
//		catch (Exception e)
//		{
//			s_log.log(Level.SEVERE, "getRate", e);
//		}
//		try
//		{
//			if (pstmt != null)
//				pstmt.close();
//			pstmt = null;
//		}
//		catch (Exception e)
//		{
//			pstmt = null;
//		}			
		if (retValue == null)
			s_log.info ("getRate - not found - CurFrom=" + CurFrom_ID 
			  + ", CurTo=" + CurTo_ID
			  + ", " + ConvDate 
			  + ", Type=" + ConversionType_ID  
			  + ", Client=" + AD_Client_ID 
			  + ", Org=" + AD_Org_ID);
		return retValue;
	}	//	getRate
	
/**
 *	Get Currency Conversion Rate
 *  @param  CurFrom_ID  The C_Currency_ID FROM
 *  @param  CurTo_ID    The C_Currency_ID TO
 *  @param  ConvDate    The Conversion date - if null - use current date
 *  @param  ConversionType_ID Conversion rate type - if 0 - use Default
 * 	@param	AD_Client_ID client
 * 	@param	AD_Org_ID	organization
 *  @return currency Rate or null
 */
public static BigDecimal getRate ( int C_DocType_ID, int CurFrom_ID, int CurTo_ID,
	Timestamp ConvDate, Timestamp PayDate, int ConversionType_ID, int AD_Client_ID, int AD_Org_ID)
{
	BigDecimal retValue = null;
	try
	{
		retValue = DB.getSQLValueBD(null, "SELECT xendra.currencyrate(?,?,?,?,?,?,?,?)", C_DocType_ID, CurFrom_ID, CurTo_ID, ConvDate, PayDate, ConversionType_ID, AD_Client_ID, AD_Org_ID);
		if (retValue == null)
			retValue = DB.getSQLValueBD(null, "SELECT xendra.currencyrate(?,?,?,?,?,?,?,?)", C_DocType_ID, CurFrom_ID, CurTo_ID, ConvDate, PayDate, ConversionType_ID, AD_Client_ID, 0);
		
	}
	catch (Exception e)
	{
		s_log.log(Level.SEVERE, "getRate", e);
	}
	if (retValue == null)
		s_log.info ("getRate - not found - CurFrom=" + CurFrom_ID 
		  + ", CurTo=" + CurTo_ID
		  + ", " + ConvDate 
		  + ", Type=" + ConversionType_ID  
		  + ", Client=" + AD_Client_ID 
		  + ", Org=" + AD_Org_ID);
	return retValue;
}	//	getRate

	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param C_Conversion_Rate_ID id
	 *	@param trxName transaction
	 */
	public MConversionRate (Properties ctx, int C_Conversion_Rate_ID, String trxName)
	{
		super(ctx, C_Conversion_Rate_ID, trxName);
		if (C_Conversion_Rate_ID == 0)
		{
		//	setC_Conversion_Rate_ID (0);
		//	setC_Currency_ID (0);
		//	setC_Currency_ID_To (null);
			super.setDivideRate (Env.ZERO);
			super.setMultiplyRate (Env.ZERO);
			setValidFrom (new Timestamp(System.currentTimeMillis()));
		}
	}	//	MConversionRate

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MConversionRate (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MConversionRate

	/**
	 * 	New Constructor
	 *	@param po parent
	 *	@param C_ConversionType_ID conversion type
	 *	@param C_Currency_ID currency
	 *	@param C_Currency_ID_To currency to
	 *	@param MultiplyRate multiply rate
	 *	@param ValidFrom valid from
	 */
	public MConversionRate (PO po, 
		int C_ConversionType_ID, 
		int C_Currency_ID, int C_Currency_ID_To, 
		BigDecimal MultiplyRate, Timestamp ValidFrom)
	{
		this (po.getCtx(), 0, po.get_TrxName());
		setClientOrg(po);
		setC_ConversionType_ID (C_ConversionType_ID);
		setC_Currency_ID (C_Currency_ID);
		setC_Currency_ID_To (C_Currency_ID_To);
		//
		setMultiplyRate (MultiplyRate);
		setValidFrom(ValidFrom);
	}	//	MConversionRate

	/**
	 * 	Set Multiply Rate
	 * 	Sets also Divide Rate
	 *	@param MultiplyRate multiply rate
	 */
	public void setMultiplyRate (BigDecimal MultiplyRate)
	{
		if (MultiplyRate == null 
			|| MultiplyRate.compareTo(Env.ZERO) == 0 
			|| MultiplyRate.compareTo(Env.ONE) == 0)
		{
			super.setDivideRate(Env.ONE);
			super.setMultiplyRate(Env.ONE);
		}
		else
		{
			super.setMultiplyRate(MultiplyRate);
			double dd = 1 / MultiplyRate.doubleValue();
			super.setDivideRate(new BigDecimal(dd));
		}
	}	//	setMultiplyRate

	/**
	 *	Set Divide Rate.
	 *	Sets also Multiply Rate
	 *	@param	DivideRate divide rate
	 */
	public void setDivideRate (BigDecimal DivideRate)
	{
		if (DivideRate == null 
			|| DivideRate.compareTo(Env.ZERO) == 0 
			|| DivideRate.compareTo(Env.ONE) == 0)
		{
			super.setDivideRate(Env.ONE);
			super.setMultiplyRate(Env.ONE);
		}
		else
		{
			super.setDivideRate(DivideRate);
			double dd = 1 / DivideRate.doubleValue();
			super.setMultiplyRate(new BigDecimal(dd));
		}
	}	//	setDivideRate

	/**
	 * 	String Representation
	 *	@return info
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer("MConversionRate[");
		sb.append(get_ID())
			.append(",Currency=").append(getC_Currency_ID())
			.append(",To=").append(getC_Currency_ID_To())
			.append(", Multiply=").append(getMultiplyRate())
			.append(",Divide=").append(getDivideRate())
			.append(", ValidFrom=").append(getValidFrom());
		sb.append("]");
		return sb.toString();
	}	//	toString

	
	/**
	 * 	Before Save.
	 * 	- Same Currency
	 * 	- Date Range Check
	 * 	- Set To date to 2056
	 *	@param newRecord new
	 *	@return true if OK to save
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		//	From - To is the same
		if (getC_Currency_ID() == getC_Currency_ID_To())
		{
			log.saveError("Error", Msg.parseTranslation(getCtx(), "@C_Currency_ID@ = @C_Currency_ID@"));
			return false;
		}
		//	Nothing to convert
		if (getMultiplyRate().compareTo(Env.ZERO) <= 0)
		{
			log.saveError("Error", Msg.parseTranslation(getCtx(), "@MultiplyRate@ <= 0"));
			return false;
		}

		//	Date Range Check
		Timestamp from = getValidFrom();
		if (getValidTo() == null)
			setValidTo (TimeUtil.getDay(2056, 1, 29));	//	 no exchange rates after my 100th birthday
		Timestamp to = getValidTo();
		
		if (to.before(from))
		{
			SimpleDateFormat df = DisplayType.getDateFormat(DisplayType.Date);
			log.saveError("Error", df.format(to) + " < " + df.format(from));
			return false;
		}
		
		return true;
	}	//	beforeSave
	
}	//	MConversionRate
