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
package org.compiere.util;

/**
 * This RuntimeException is used to pass SQLException up the chain of calling
 * methods to determine what to do where needed.
 * 
 * @author Vincent Harcq
 * @version $Id: DBException.java 508 2007-11-24 23:06:53Z el_man $
 */
public class DBException extends RuntimeException {
	
	/**
	 * Create a new DBException based on a SQLException
	 * @param e Specicy the Exception cause
	 */
	public DBException(Exception e)
	{
		super(e);
	}	//	DBException

	/**
	 * Create a new DBException
	 * @param msg Message
	 */
	public DBException(String msg) 
	{
		super(msg);
	}	//	DBException

}	//	DBException
