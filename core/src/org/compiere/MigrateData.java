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
package org.compiere;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

import org.compiere.util.*;
import org.compiere.model.MProductDownload;
import org.compiere.print.*;


/**
 * 	Migrate Data
 *  @author Jorg Janke
 *  @version $Id: MigrateData.java 4604 2013-01-08 22:04:14Z xapiens $
 */
public class MigrateData
{
	/**
	 * 	Migrate Data.
	 * 	Called from DB.afterMigration
	 */
	public MigrateData ()
	{
		//release252c();		
		//	Update existing Print Format
		//PrintFormatUtil pfu = new PrintFormatUtil (Env.getCtx());
		//pfu.addMissingColumns();
	}	//	MigrateData
	
	/**	Logger	*/
	private static CLogger	log	= CLogger.getCLogger (MigrateData.class);
	
	/**
	 * 	Release 252c
	 */
	private void release252c()
	{
		String sql = "SELECT COUNT(*) FROM M_ProductDownload";
		int no = DB.getSQLValue(null, sql);
		if (no > 0)
		{
			log.finer("No Need - Downloads #" + no);
			return;
		}
		//
		int count = 0;
		sql = "SELECT AD_Client_ID, AD_Org_ID, M_Product_ID, Name, DownloadURL "
			+ "FROM M_Product "
			+ "WHERE DownloadURL IS NOT NULL";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				int AD_Client_ID = rs.getInt(1);
				int AD_Org_ID = rs.getInt(2);
				int M_Product_ID = rs.getInt(3);
				String Name = rs.getString(4);
				String DownloadURL = rs.getString(5);
				//
				Properties ctx = new Properties (Env.getCtx());
				Env.setContext(ctx, "#AD_Client_ID", AD_Client_ID);
				Env.setContext(ctx, "AD_Client_ID", AD_Client_ID);
				Env.setContext(ctx, "#AD_Org_ID", AD_Org_ID);
				Env.setContext(ctx, "AD_Org_ID", AD_Org_ID);
				MProductDownload pdl = new MProductDownload(ctx, 0, null);
				pdl.setM_Product_ID(M_Product_ID);
				pdl.setName(Name);
				pdl.setDownloadURL(DownloadURL);
				if (pdl.save())
				{
					count++;
					String sqlUpdate = "UPDATE M_Product SET DownloadURL = NULL WHERE M_Product_ID=" + M_Product_ID;
					int updated = DB.executeUpdate(sqlUpdate, null);
					if (updated != 1)
						log.warning("Product not updated");
				}
				else
					log.warning("Product Download not created M_Product_ID=" + M_Product_ID);
			}
			rs.close ();
			pstmt.close ();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log (Level.SEVERE, sql, e);
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
		log.info("#" + count);
	}	//	release252c
	
	
	/**
	 * 	Migrate Data
	 *	@param args ignored
	 */
	public static void main (String[] args)
	{
		Xendra.startup(true);
		new MigrateData();
	}	//	main
	
}	//	MigrateData
