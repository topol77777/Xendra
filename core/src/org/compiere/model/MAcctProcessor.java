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

import org.compiere.model.persistence.X_A_Machine;
import org.compiere.model.persistence.X_C_AcctProcessor;
import org.compiere.model.reference.REF__FrequencyType;
import org.compiere.util.*;


/**
 *	Accounting Processor Model
 *	
 *  @author Jorg Janke
 *  @version $Id: MAcctProcessor.java 5822 2016-06-15 22:56:06Z xapiens $
 */
public class MAcctProcessor extends X_C_AcctProcessor
	implements XendraProcessor
{
	/**
	 * 	Get Active
	 *	@param ctx context
	 *	@return active processors
	 */
	public static MAcctProcessor[] getActive (Properties ctx)
	{
		MAcctProcessor[] retValue = null;
		List<MAcctProcessor> list = new ArrayList<MAcctProcessor>();
		X_A_Machine machine = new Query(Env.getCtx(), X_A_Machine.Table_Name, "Mac_Address = ? AND IsActive='Y'", null)
							.setParameters(Util.getLocalMacAddress()).first();
		if (machine != null)
		{
			list = new Query(Env.getCtx(), X_C_AcctProcessor.Table_Name, "IsActive='Y' AND A_Machine_ID = ?", null)
			.setParameters(machine.getA_Machine_ID())
			.list();
		}
		retValue = new MAcctProcessor[list.size ()];
		list.toArray (retValue);		
		return retValue;
	}	//	getActive

	/**	Static Logger	*/
	private static CLogger	s_log	= CLogger.getCLogger (MAcctProcessor.class);

	
	/**
	 * 	Standard Construvtor
	 *	@param ctx context
	 *	@param C_AcctProcessor_ID id
	 *	@param trxName transaction
	 */
	public MAcctProcessor (Properties ctx, int C_AcctProcessor_ID, String trxName)
	{
		super (ctx, C_AcctProcessor_ID, trxName);
		if (C_AcctProcessor_ID == 0)
		{
		//	setName (null);
		//	setSupervisor_ID (0);
			setFrequencyType (REF__FrequencyType.Hour);
			setFrequency (1);
			setKeepLogDays (7);	// 7
		}	
	}	//	MAcctProcessor

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public MAcctProcessor (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MAcctProcessor

	/**
	 * 	Parent Constructor
	 *	@param client parent
	 *	@param Supervisor_ID admin
	 */
	public MAcctProcessor (MClient client, int Supervisor_ID)
	{
		this (client.getCtx(), 0, client.get_TrxName());
		setClientOrg(client);
		setName (client.getName() + " - " 
			+ Msg.translate(getCtx(), "C_AcctProcessor_ID"));
		setSupervisor_ID (Supervisor_ID);
	}	//	MAcctProcessor
	
	
	
	/**
	 * 	Get Server ID
	 *	@return id
	 */
	public String getServerID ()
	{
		return "AcctProcessor" + get_ID();
	}	//	getServerID

	/**
	 * 	Get Date Next Run
	 *	@param requery requery
	 *	@return date next run
	 */
	public Timestamp getDateNextRun (boolean requery)
	{
		if (requery)
			load(get_TrxName());
		return getDateNextRun();
	}	//	getDateNextRun

	/**
	 * 	Get Logs
	 *	@return logs
	 */
	public XendraProcessorLog[] getLogs ()
	{
		ArrayList<MAcctProcessorLog> list = new ArrayList<MAcctProcessorLog>();
		String sql = "SELECT * "
			+ "FROM C_AcctProcessorLog "
			+ "WHERE C_AcctProcessor_ID=? " 
			+ "ORDER BY Created DESC";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setInt (1, getC_AcctProcessor_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MAcctProcessorLog (getCtx(), rs, get_TrxName()));
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
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
		MAcctProcessorLog[] retValue = new MAcctProcessorLog[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getLogs

	/**
	 * 	Delete old Request Log
	 *	@return number of records
	 */
	public int deleteLog()
	{
		if (getKeepLogDays() < 1)
			return 0;
		String sql = "DELETE FROM C_AcctProcessorLog "
			+ "WHERE C_AcctProcessor_ID=" + getC_AcctProcessor_ID() 
			+ " AND (Created+ cast('1 day' as interval) * "+ getKeepLogDays() + ") < CURRENT_TIMESTAMP";
		int no = DB.executeUpdate(sql, get_TrxName());
		return no;
	}	//	deleteLog

}	//	MAcctProcessor
