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

import java.math.*;
import java.sql.*;

import org.compiere.model.MConversionRate;
import org.compiere.model.persistence.X_I_Conversion_Rate;

import java.util.logging.*;
import org.compiere.util.*;

import org.xendra.annotations.XendraProcess;
import org.xendra.annotations.XendraProcessParameter;

/**
 *	Import Currency Conversion Rates 
 *	
 *  @author Jorg Janke
 *  @version $Id: ImportConversionRate.java 5583 2015-08-05 14:11:58Z xapiens $
 */
@XendraProcess(value="Import_ConversionRate",
name="Import Conversion Rate",
description="Import Currency Conversion Rate",
help="",
Identifier="3c05dbc0-c245-594c-a89d-62bdd49ce0b2",
classname="org.compiere.process.ImportConversionRate",
updated="2015-06-20 10:10:12")	
public class ImportConversionRate extends SvrProcess
{
	
	/**	Client to be imported to			*/
	@XendraProcessParameter(Name="Client",
			                ColumnName="AD_Client_ID",
			                Description="Client/Tenant for this installation.",
			                Help="A Client is a company or a legal entity. You cannot share data between Clients. Tenant is a synonym for Client.",
			                AD_Reference_ID=DisplayType.TableDir,
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
			                Identifier="dfb95f2c-2c30-57a2-6e0f-75af8d319c22")	
	private int				p_AD_Client_ID = 0;
	/**	Organization to be imported to		*/
	@XendraProcessParameter(Name="Organization",
                ColumnName="AD_Org_ID",
                Description="Organizational entity within client",
                Help="An organization is a unit of your client or legal entity - examples are store, department. You can share data between organizations.",
                AD_Reference_ID=DisplayType.TableDir,
                SeqNo=20,
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
                Identifier="aea166ba-cda8-93be-7086-f01e8c9fe567")
	private int				p_AD_Org_ID = 0;
	/**	Conversion Type to be imported to	*/
	@XendraProcessParameter(Name="Currency Type",
			                ColumnName="C_ConversionType_ID",
			                Description="Currency Conversion Rate Type",
			                Help="The Currency Conversion Rate Type lets you define different type of rates, e.g. Spot, Corporate and/or Sell/Buy rates.",
			                AD_Reference_ID=DisplayType.TableDir,
			                SeqNo=30,
			                ReferenceValueID="",
			                ValRuleID="",
			                FieldLength=0,
			                IsMandatory=false,
			                IsRange=false,
			                DefaultValue="",
			                DefaultValue2="",
			                vFormat="",
			                valueMin="",
			                valueMax="",
			                DisplayLogic="",
			                ReadOnlyLogic="",
			                Identifier="f656277b-fbb7-4078-efc1-4836759d4e45")	
	private int				p_C_ConversionType_ID = 0;
	/**	Default Date					*/
	@XendraProcessParameter(Name="Valid from",
			                ColumnName="ValidFrom",
			                Description="Valid from including this date (first day)",
			                Help="The Valid From date indicates the first day of a date range",
			                AD_Reference_ID=DisplayType.Date,
			                SeqNo=40,
			                ReferenceValueID="",
			                ValRuleID="",
			                FieldLength=0,
			                IsMandatory=false,
			                IsRange=false,
			                DefaultValue="",
			                DefaultValue2="",
			                vFormat="",
			                valueMin="",
			                valueMax="",
			                DisplayLogic="",

			                ReadOnlyLogic="",
			                Identifier="e21bcfdc-c042-0402-66cc-b9750a4922d5")	
	private Timestamp		p_ValidFrom = null;
	/** Default Reciprocal				*/
	@XendraProcessParameter(Name="Create Reciprocal Rate",
                ColumnName="CreateReciprocalRate",
                Description="Create Reciprocal Rate from current information",
                Help="If selected, the imported USD->EUR rate is used to create/calculate the reciprocal rate EUR->USD.",
                AD_Reference_ID=DisplayType.YesNo,
                SeqNo=50,
                ReferenceValueID="",
                ValRuleID="",
                FieldLength=0,
                IsMandatory=false,
                IsRange=false,
                DefaultValue="",
                DefaultValue2="",
                vFormat="",
                valueMin="",
                valueMax="",
                DisplayLogic="",
                ReadOnlyLogic="",
                Identifier="84558d37-7a21-c2b9-7ca0-7dd45750966a")
	private boolean 		p_CreateReciprocalRate = false;
	/**	Delete old Imported				*/
	@XendraProcessParameter(Name="Delete old imported records",
			                ColumnName="DeleteOldImported",
			                Description="Before processing delete old imported records in the import table",
			                Help="",
			                AD_Reference_ID=DisplayType.YesNo,
			                SeqNo=60,
			                ReferenceValueID="",
			                ValRuleID="",
			                FieldLength=0,
			                IsMandatory=false,
			                IsRange=false,
			                DefaultValue="Y",
			                DefaultValue2="",
			                vFormat="",
			                valueMin="",
			                valueMax="",
			                DisplayLogic="",
			                ReadOnlyLogic="",
			                Identifier="f28f023f-bd4e-1ecf-c61b-317413b28bbf")	
	private boolean			p_DeleteOldImported = false;

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
			else if (name.equals("AD_Client_ID"))
				p_AD_Client_ID = ((BigDecimal)para[i].getParameter()).intValue();
			else if (name.equals("AD_Org_ID"))
				p_AD_Org_ID = ((BigDecimal)para[i].getParameter()).intValue();
			else if (name.equals("C_ConversionType_ID"))
				p_C_ConversionType_ID = ((BigDecimal)para[i].getParameter()).intValue();
			else if (name.equals("ValidFrom"))
				p_ValidFrom = (Timestamp)para[i].getParameter();
			else if (name.equals("CreateReciprocalRate"))
				p_CreateReciprocalRate = "Y".equals(para[i].getParameter());
			else if (name.equals("DeleteOldImported"))
				p_DeleteOldImported = "Y".equals(para[i].getParameter());
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}	//	prepare


	/**
	 *  Perrform process.
	 *  @return Message
	 *  @throws Exception
	 */
	protected String doIt() throws Exception
	{
		log.info("doIt - AD_Client_ID=" + p_AD_Client_ID
			+ ",AD_Org_ID=" + p_AD_Org_ID
			+ ",C_ConversionType_ID=" + p_C_ConversionType_ID
			+ ",ValidFrom=" + p_ValidFrom
			+ ",CreateReciprocalRate=" + p_CreateReciprocalRate);
		//
		StringBuffer sql = null;
		int no = 0;
		String clientCheck = " AND AD_Client_ID=" + p_AD_Client_ID;
		//	****	Prepare	****

		//	Delete Old Imported
		if (p_DeleteOldImported)
		{
			sql = new StringBuffer ("DELETE FROM I_Conversion_Rate "
				  + "WHERE I_IsImported='Y'").append (clientCheck);
			no = DB.executeUpdate(sql.toString(), get_TrxName());
			log.fine("Delete Old Impored =" + no);
		}

		//	Set Client, Org, Location, IsActive, Created/Updated
		sql = new StringBuffer ("UPDATE I_Conversion_Rate "
			  + "SET AD_Client_ID = COALESCE (AD_Client_ID,").append (p_AD_Client_ID).append ("),"
			  + " AD_Org_ID = COALESCE (AD_Org_ID,").append (p_AD_Org_ID).append ("),");
		if (p_C_ConversionType_ID != 0)
			sql.append(" C_ConversionType_ID = COALESCE (C_ConversionType_ID,").append (p_C_ConversionType_ID).append ("),");
		if (p_ValidFrom != null)
			sql.append(" ValidFrom = COALESCE (ValidFrom,").append (DB.TO_DATE(p_ValidFrom)).append ("),");
		else
			sql.append(" ValidFrom = COALESCE (ValidFrom,CURRENT_TIMESTAMP),");
		sql.append(" CreateReciprocalRate = COALESCE (CreateReciprocalRate,'").append (p_CreateReciprocalRate ? "Y" : "N").append ("'),"
			+ " IsActive = COALESCE (IsActive, 'Y'),"
			+ " Created = COALESCE (Created, CURRENT_TIMESTAMP),"
			+ " CreatedBy = COALESCE (CreatedBy, 0),"
			+ " Updated = COALESCE (Updated, CURRENT_TIMESTAMP),"
			+ " UpdatedBy = ").append(getAD_User_ID()).append(","
			+ " I_ErrorMsg = ' ',"
			+ " Processed = 'N',"	
			+ " I_IsImported = 'N' "
			+ "WHERE I_IsImported<>'Y' OR I_IsImported IS NULL");
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		log.info ("Reset =" + no);

		//	Org
		sql = new StringBuffer ("UPDATE I_Conversion_Rate o "
			+ "SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid Org, '"
			+ "WHERE (AD_Org_ID IS NULL"
			+ " OR EXISTS (SELECT * FROM AD_Org oo WHERE o.AD_Org_ID=oo.AD_Org_ID AND (oo.IsSummary='Y' OR oo.IsActive='N')))"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Org =" + no);
			
		//	Conversion Type
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET C_ConversionType_ID = (SELECT C_ConversionType_ID FROM C_ConversionType c"
			+ " WHERE c.Value=i.ConversionTypeValue AND c.AD_Client_ID IN (0,i.AD_Client_ID) AND c.IsActive='Y') "
			+ "WHERE C_ConversionType_ID IS NULL AND ConversionTypeValue IS NOT NULL"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no > 0)
			log.fine("Set ConversionType =" + no);
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid ConversionType, ' "
			+ "WHERE (C_ConversionType_ID IS NULL"
			+ " OR NOT EXISTS (SELECT * FROM C_ConversionType c "
				+ "WHERE i.C_ConversionType_ID=c.C_ConversionType_ID AND c.IsActive='Y'"
				+ " AND c.AD_Client_ID IN (0,i.AD_Client_ID)))"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid ConversionType =" + no);
		
		//	Currency
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET C_Currency_ID = (SELECT C_Currency_ID FROM C_Currency c"
			+ "	WHERE c.ISO_Code=i.ISO_Code AND c.AD_Client_ID IN (0,i.AD_Client_ID) AND c.IsActive='Y') "
			+ "WHERE C_Currency_ID IS NULL AND ISO_Code IS NOT NULL"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no > 0)
			log.fine("Set Currency =" + no);
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid Currency, ' "
			+ "WHERE (C_Currency_ID IS NULL"
			+ " OR NOT EXISTS (SELECT * FROM C_Currency c "
				+ "WHERE i.C_Currency_ID=c.C_Currency_ID AND c.IsActive='Y'"
				+ " AND c.AD_Client_ID IN (0,i.AD_Client_ID)))"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Currency =" + no);

		//	Currency To
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET C_Currency_ID_To = (SELECT C_Currency_ID FROM C_Currency c"
			+ "	WHERE c.ISO_Code=i.ISO_Code_To AND c.AD_Client_ID IN (0,i.AD_Client_ID) AND c.IsActive='Y') "
			+ "WHERE C_Currency_ID_To IS NULL AND ISO_Code_To IS NOT NULL"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no > 0)
			log.fine("Set Currency To =" + no);
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid Currency To, ' "
			+ "WHERE (C_Currency_ID_To IS NULL"
			+ " OR NOT EXISTS (SELECT * FROM C_Currency c "
				+ "WHERE i.C_Currency_ID_To=c.C_Currency_ID AND c.IsActive='Y'"
				+ " AND c.AD_Client_ID IN (0,i.AD_Client_ID)))"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Currency To =" + no);
			
		//	Rates
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET MultiplyRate = 1 / DivideRate "
			+ "WHERE (MultiplyRate IS NULL OR MultiplyRate = 0) AND DivideRate IS NOT NULL AND DivideRate<>0"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no > 0)
			log.fine("Set MultiplyRate =" + no);
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET DivideRate = 1 / MultiplyRate "
			+ "WHERE (DivideRate IS NULL OR DivideRate = 0) AND MultiplyRate IS NOT NULL AND MultiplyRate<>0"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no > 0)
			log.fine("Set DivideRate =" + no);
		sql = new StringBuffer ("UPDATE I_Conversion_Rate i "
			+ "SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Invalid Rates, ' "
			+ "WHERE (MultiplyRate IS NULL OR MultiplyRate = 0 OR DivideRate IS NULL OR DivideRate = 0)"
			+ " AND I_IsImported<>'Y'").append (clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (no != 0)
			log.warning ("Invalid Rates =" + no);
	//	sql = new StringBuffer ("UPDATE I_Conversion_Rate i "	//	Rate diff > 10%
	//		+ "SET I_IsImported='E', I_ErrorMsg=I_ErrorMsg||'ERR=Inconsistent Rates='||(MultiplyRate - (1/DivideRate)) "
	//		+ "WHERE ((MultiplyRate - (1/DivideRate)) > (MultiplyRate * .1))"
	//		+ " AND I_IsImported<>'Y'").append (clientCheck);
	//	no = DB.executeUpdate(sql.toString(), get_TrxName());
	//	if (no != 0)
	//		log.warn ("Inconsistent Rates =" + no);
		
		commit();
		/*********************************************************************/

		int noInsert = 0;
		sql = new StringBuffer ("SELECT * FROM I_Conversion_Rate "
			+ "WHERE I_IsImported='N'").append (clientCheck)
			.append(" ORDER BY C_Currency_ID, C_Currency_ID_To, ValidFrom");
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
			{
				X_I_Conversion_Rate imp = new X_I_Conversion_Rate (getCtx(), rs, get_TrxName());
				MConversionRate rate = new MConversionRate (imp, 
					imp.getC_ConversionType_ID(), 
					imp.getC_Currency_ID(), imp.getC_Currency_ID_To(),
					imp.getMultiplyRate(), imp.getValidFrom());
				if (imp.getValidTo() != null)
					rate.setValidTo(imp.getValidTo());
				if (rate.save())
				{
					imp.setC_Conversion_Rate_ID(rate.getC_Conversion_Rate_ID());
					imp.setI_IsImported(true);
					imp.setProcessed(true);
					imp.save();
					noInsert++;
					//
					if (imp.isCreateReciprocalRate())
					{
						rate = new MConversionRate (imp, 
							imp.getC_ConversionType_ID(), 
							imp.getC_Currency_ID_To(), imp.getC_Currency_ID(),
							imp.getDivideRate(), imp.getValidFrom());
						if (imp.getValidTo() != null)
							rate.setValidTo(imp.getValidTo());
						if (rate.save())					
							noInsert++;
					}
				}
			}
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql.toString(), e);
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

		//	Set Error to indicator to not imported
		sql = new StringBuffer ("UPDATE I_Conversion_Rate "
			+ "SET I_IsImported='N', Updated=CURRENT_TIMESTAMP "
			+ "WHERE I_IsImported<>'Y'").append(clientCheck);
		no = DB.executeUpdate(sql.toString(), get_TrxName());
		addLog (0, null, new BigDecimal (no), "@Errors@");
		//
		addLog (0, null, new BigDecimal (noInsert), "@C_Conversion_Rate_ID@: @Inserted@");
		return "";
	}	//	doIt

}	//	ImportConversionRate
