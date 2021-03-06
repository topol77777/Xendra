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

import javax.mail.internet.*;
import javax.naming.*;
import javax.naming.directory.*;

import org.compiere.model.persistence.X_AD_User;
import org.compiere.model.persistence.X_AD_UserBPAccess;
import org.compiere.model.persistence.X_C_BPartner;
import org.compiere.model.reference.REF_AD_UserNotificationType;
import org.compiere.util.*;

/**
 *  User Model
 *
 *  @author Jorg Janke
 *  @version $Id: MUser.java 5583 2015-08-05 14:11:58Z xapiens $
 */
public class MUser extends X_AD_User
{
	/**
	 * 	Get active Users of BPartner
	 *	@param ctx context
	 *	@param C_BPartner_ID id
	 *	@return array of users
	 */
	public static MUser[] getOfBPartner (Properties ctx, int C_BPartner_ID)
	{
		ArrayList<MUser> list = new ArrayList<MUser>();
		String sql = "SELECT * FROM AD_User WHERE C_BPartner_ID=? AND IsActive='Y'";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setInt (1, C_BPartner_ID);
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MUser(ctx, rs, null));
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
		
		MUser[] retValue = new MUser[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getOfBPartner

	/**
	 * 	Get Users with Role
	 *	@param role role
	 *	@return array of users
	 */
	public static MUser[] getWithRole (MRole role)
	{
		ArrayList<MUser> list = new ArrayList<MUser>();
		String sql = "SELECT * FROM AD_User u "
			+ "WHERE u.IsActive='Y'"
			+ " AND EXISTS (SELECT * FROM AD_User_Roles ur "
				+ "WHERE ur.AD_User_ID=u.AD_User_ID AND ur.AD_Role_ID=? AND ur.IsActive='Y')";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setInt (1, role.getAD_Role_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add(new MUser(role.getCtx(), rs, null));
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
		
		MUser[] retValue = new MUser[list.size ()];
		list.toArray (retValue);
		return retValue;
	}	//	getWithRole

	/**
	 * 	Get User (cached)
	 * 	Also loads Admninistrator (0)
	 *	@param ctx context
	 *	@param AD_User_ID id
	 *	@return user
	 */
	public static MUser get (Properties ctx, int AD_User_ID)
	{
		Integer key = new Integer(AD_User_ID);
		MUser retValue = (MUser)s_cache.get(key);
		if (retValue == null)
		{
			retValue = new MUser (ctx, AD_User_ID, null);
			if (AD_User_ID == 0)
			{
				String trxName = null;
				retValue.load(trxName);	//	load System Record
			}
			s_cache.put(key, retValue);
		}
		return retValue;
	}	//	get

	/**
	 * 	Get Current User (cached)
	 *	@param ctx context
	 *	@return user
	 */
	public static MUser get (Properties ctx)
	{
		return get(ctx, Env.getAD_User_ID(ctx));
	}	//	get

	/**
	 * 	Get User
	 *	@param ctx context
	 *	@param name name
	 *	@param password password
	 *	@return user or null
	 */
	public static MUser get (Properties ctx, String name, String password)
	{
		if (name == null || name.length() == 0 || password == null || password.length() == 0)
		{
			s_log.warning ("Invalid Name/Password = " + name + "/" + password);
			return null;
		}
		int AD_Client_ID = Env.getAD_Client_ID(ctx);
		
		MUser retValue = null;
		String sql = "SELECT * FROM AD_User "
			+ "WHERE Name=? AND Password=? AND IsActive='Y' AND AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setString (1, name);
			pstmt.setString (2, password);
			pstmt.setInt(3, AD_Client_ID);
			ResultSet rs = pstmt.executeQuery ();
			if (rs.next ())
			{
				retValue = new MUser (ctx, rs, null);
				if (rs.next())
					s_log.warning ("More then one user with Name/Password = " + name);
			}
			else
				s_log.fine("No record");
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
		return retValue;
	}	//	get
	
	/**
	 *  Get Name of AD_User
	 *  @param  AD_User_ID   System User
	 *  @return Name of user or ?
	 */
	public static String getNameOfUser (int AD_User_ID)
	{
		String name = "?";
		//	Get ID
		String sql = "SELECT Name FROM AD_User WHERE AD_User_ID=?";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, AD_User_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				name = rs.getString(1);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		return name;
	}	//	getNameOfUser

	
	/**
	 * 	User is SalesRep
	 *	@param AD_User_ID user
	 *	@return true if sales rep
	 */
	public static boolean isSalesRep (int AD_User_ID)
	{
		if (AD_User_ID == 0)
			return false;
		String sql = "SELECT MAX(AD_User_ID) FROM AD_User u"
			+ " INNER JOIN C_BPartner bp ON (u.C_BPartner_ID=bp.C_BPartner_ID) "
			+ "WHERE bp.IsSalesRep='Y' AND AD_User_ID=?";
		int no = DB.getSQLValue(null, sql, AD_User_ID);
		return no == AD_User_ID;
	}	//	isSalesRep
	
	/**	Cache					*/
	static private CCache<Integer,MUser> s_cache = new CCache<Integer,MUser>("AD_User", 30, 60);
	/**	Static Logger			*/
	private static CLogger	s_log	= CLogger.getCLogger (MUser.class);
	
	
	/**************************************************************************
	 * 	Default Constructor
	 *	@param ctx context
	 *	@param AD_User_ID id
	 * 	@param trxName transaction
	 */
	public MUser (Properties ctx, int AD_User_ID, String trxName)
	{
		super (ctx, AD_User_ID, trxName);	//	0 is also System
		if (AD_User_ID == 0)
		{
			setIsFullBPAccess (true);
			setNotificationType(REF_AD_UserNotificationType.EMail);
		}		
	}	//	MUser

	/**
	 * 	Parent Constructor
	 *	@param partner partner
	 */
	public MUser (X_C_BPartner partner)
	{
		this (partner.getCtx(), 0, partner.get_TrxName());
		setClientOrg(partner);
		setC_BPartner_ID (partner.getC_BPartner_ID());
		setName(partner.getName());
	}	//	MUser

	/**
	 * 	Load Constructor
	 * 	@param ctx context
	 * 	@param rs current row of result set to be loaded
	 * 	@param trxName transaction
	 */
	public MUser (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MUser

	/**	Roles of User with Org	*/
	private MRole[] 			m_roles = null;
	/**	Roles of User with Org	*/
	private int		 			m_rolesAD_Org_ID = -1;
	/** Is Administrator		*/
	private Boolean				m_isAdministrator = null;
	/** User Access Rights				*/
	private X_AD_UserBPAccess[]	m_bpAccess = null;
	
		
	/**
	 * 	Get Value - 7 bit lower case alpha numerics max length 8
	 *	@return value
	 */
	public String getValue()
	{
		String s = super.getValue();
		if (s != null)
			return s;
		setValue(null);
		return super.getValue();
	}	//	getValue

	/**
	 * 	Set Value - 7 bit lower case alpha numerics max length 8
	 *	@param Value
	 */
	public void setValue(String Value)
	{
		if (Value == null || Value.trim().length () == 0)
			Value = getLDAPUser();
		if (Value == null || Value.length () == 0)
			Value = getName();
		if (Value == null || Value.length () == 0)
			Value = "noname";
		//
		String result = cleanValue(Value);
		if (result.length() > 8)
		{
			String first = getName(Value, true);
			String last = getName(Value, false);
			if (last.length() > 0)
			{
				String temp = last;
				if (first.length() > 0)
					temp = first.substring (0, 1) + last;
				result = cleanValue(temp);
			}
			else
				result = cleanValue(first);
		}
		if (result.length() > 8)
			result = result.substring (0, 8);
		super.setValue(result);
	}	//	setValue
	
	/**
	 * 	Clean Value
	 *	@param value value
	 *	@return lower case cleaned value
	 */
	private String cleanValue (String value)
	{
		char[] chars = value.toCharArray();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < chars.length; i++)
		{
			char ch = chars[i];
			ch = Character.toLowerCase (ch);
			if ((ch >= '0' && ch <= '9')		//	digits
				|| (ch >= 'a' && ch <= 'z'))	//	characters
				sb.append(ch);
		}
		return sb.toString ();
	}	//	cleanValue
	
	/**
	 * 	Get First Name
	 *	@return first name
	 */
	public String getFirstName()
	{
		return getName (getName(), true);
	}	//	getFirstName
	
	/**
	 * 	Get Last Name
	 *	@return first name
	 */
	public String getLastName()
	{
		return getName (getName(), false);
	}	//	getLastName

	/**
	 * 	Get First/Last Name
	 *	@param name name
	 *	@param getFirst if true first name is returned
	 *	@return first/last name
	 */
	private String getName (String name, boolean getFirst)
	{
		if (name == null || name.length () == 0)
			return "";
		String first = null;
		String last = null;
		//	Janke, Jorg R - Jorg R Janke
		//	double names not handled gracefully nor titles 
		//	nor (former) aristrocratic world de/la/von 
		boolean lastFirst = name.indexOf(',') != -1;
		StringTokenizer st = null;
		if (lastFirst)
			st = new StringTokenizer(name, ",");
		else
			st = new StringTokenizer(name, " ");
		while (st.hasMoreTokens())
		{
			String s = st.nextToken().trim();
			if (lastFirst)
			{
				if (last == null)
					last = s;
				else if (first == null)
					first = s;
			}
			else
			{
				if (first == null)
					first = s;
				else
					last = s;
			}
		}
		if (getFirst)
		{
			if (first == null)
				return "";
			return first.trim();
		}
		if (last == null)
			return "";
		return last.trim();
	}	//	getName
	
	
	/**
	 * 	Add to Description
	 *	@param description description to be added
	 */
	public void addDescription (String description)
	{
		if (description == null || description.length() == 0)
			return;
		String descr = getDescription();
		if (descr == null || descr.length() == 0)
			setDescription (description);
		else
			setDescription (descr + " - " + description);
	}	//	addDescription
	
	
	/**
	 * 	String Representation
	 *	@return Info
	 */
	public String toString ()
	{
		StringBuffer sb = new StringBuffer ("MUser[")
			.append(get_ID())
			.append(",Name=").append(getName())
			.append(",EMailUserID=").append(getEMailUser())
			.append ("]");
		return sb.toString ();
	}	//	toString

	/**
	 * 	Is it an Online Access User
	 *	@return true if it has an email and password
	 */
	public boolean isOnline ()
	{
		if (getEMail() == null || getPassword() == null)
			return false;
		return true;
	}	//	isOnline

	/**
	 * 	Set EMail - reset validation
	 *	@param EMail email
	 */
	public void setEMail(String EMail)
	{
		super.setEMail (EMail);
		setEMailVerifyDate (null);
	}	//	setEMail
	
	/**
	 * 	Convert EMail
	 *	@return Valid Internet Address
	 */
	public InternetAddress getInternetAddress ()
	{
		String email = getEMail();
		if (email == null || email.length() == 0)
			return null;
		try
		{
			InternetAddress ia = new InternetAddress (email, true);
			if (ia != null)
				ia.validate();	//	throws AddressException
			return ia;
		}
		catch (AddressException ex)
		{
			log.warning(email + " - " + ex.getLocalizedMessage());
		}
		return null;
	}	//	getInternetAddress

	/**
	 * 	Validate Email (does not work).
	 * 	Check DNS MX record
	 * 	@param ia email
	 *	@return error message or ""
	 */
	private String validateEmail (InternetAddress ia)
	{
		if (ia == null)
			return "NoEmail";
                else return ia.getAddress();
		/*
                if (true)
			return null;
		
		Hashtable<String,String> env = new Hashtable<String,String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
	//	env.put(Context.PROVIDER_URL, "dns://admin.xendra.org");
		try
		{
			DirContext ctx = new InitialDirContext(env);
		//	Attributes atts = ctx.getAttributes("admin");
			Attributes atts = ctx.getAttributes("dns://admin.xendra.org", new String[] {"MX"});
			NamingEnumeration en = atts.getAll();
	//		NamingEnumeration en = ctx.list("xendra.org");
			while (en.hasMore())
			{
				System.out.println(en.next());
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return e.getLocalizedMessage();
		}
		return null;
                */
	}	//	validateEmail
	
	/**
	 * 	Is the email valid
	 * 	@return return true if email is valid (artificial check)
	 */
	public boolean isEMailValid()
	{
		return validateEmail(getInternetAddress()) != null;
	}	//	isEMailValid
	
	/**
	 * 	Could we send an email
	 * 	@return true if EMail Uwer/PW exists
	 */
	public boolean isCanSendEMail()
	{
		String s = getEMailUser();
		if (s == null || s.length() == 0)
			return false;
		s = getEMailUserPW();
		return s != null && s.length() > 0;
	}	//	isCanSendEMail

	/**
	 * 	Get EMail Validation Code
	 *	@return code
	 */
	public String getEMailVerifyCode()
	{
		long code = getAD_User_ID() 
			+ getName().hashCode();
		return "C" + String.valueOf(Math.abs(code)) + "C";
	}	//	getEMailValidationCode
	
	/**
	 * 	Check & Set EMail Validation Code.
	 *	@param code code
	 *	@param info info
	 *	@return true if valid
	 */
	public boolean setEMailVerifyCode (String code, String info)
	{
		boolean ok = code != null 
			&& code.equals(getEMailVerifyCode()); 
		if (ok)
			setEMailVerifyDate(new Timestamp(System.currentTimeMillis()));
		else
			setEMailVerifyDate(null);
		setEMailVerify(info);
		return ok;
	}	//	setEMailValidationCode
	
	/**
	 * 	Is EMail Verified by response
	 *	@return true if verified
	 */
	public boolean isEMailVerified()
	{
		//	UPDATE AD_User SET EMailVerifyDate=CURRENT_TIMESTAMP, EMailVerify='Direct' WHERE AD_User_ID=1
		return getEMailVerifyDate() != null
			&& getEMailVerify() != null 
			&& getEMailVerify().length() > 0; 
	}	//	isEMailVerified
	
	/**
	 * 	Get Notification via EMail
	 *	@return true if email
	 */
	public boolean isNotificationEMail()
	{
		String s = getNotificationType();
		return s == null || REF_AD_UserNotificationType.EMail.equals(s);
	}	//	isNotificationEMail
	
	/**
	 * 	Get Notification via Note
	 *	@return true if note
	 */
	public boolean isNotificationNote()
	{
		String s = getNotificationType();
		return s != null && REF_AD_UserNotificationType.Notice.equals(s);
	}	//	isNotificationNote
	
	
	/**************************************************************************
	 * 	Get User Roles for Org
	 * 	@param AD_Org_ID org
	 *	@return array of roles
	 */
	public MRole[] getRoles (int AD_Org_ID)
	{
		if (m_roles != null && m_rolesAD_Org_ID == AD_Org_ID)
			return m_roles;
		
		ArrayList<MRole> list = new ArrayList<MRole>();
		String sql = "SELECT * FROM AD_Role r " 
			+ "WHERE r.IsActive='Y'" 
			+ " AND EXISTS (SELECT * FROM AD_Role_OrgAccess ro"
				+ " WHERE r.AD_Role_ID=ro.AD_Role_ID AND ro.IsActive='Y' AND ro.AD_Org_ID=?)" 
			+ " AND EXISTS (SELECT * FROM AD_User_Roles ur" 
				+ " WHERE r.AD_Role_ID=ur.AD_Role_ID AND ur.IsActive='Y' AND ur.AD_User_ID=?) "
			+ "ORDER BY AD_Role_ID";
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, get_TrxName());
			pstmt.setInt (1, AD_Org_ID);
			pstmt.setInt (2, getAD_User_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
				list.add (new MRole(getCtx(), rs, get_TrxName()));
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
		//
		m_rolesAD_Org_ID = AD_Org_ID;
		m_roles = new MRole[list.size()];
		list.toArray (m_roles);
		return m_roles;
	}	//	getRoles
	
	/**
	 * 	Is User an Administrator?
	 *	@return true id Admin
	 */
	public boolean isAdministrator()
	{
		if (m_isAdministrator == null)
		{
			m_isAdministrator = Boolean.FALSE;
			MRole[] roles = getRoles(0);
			for (int i = 0; i < roles.length; i++)
			{
				if (roles[i].getAD_Role_ID() == 0)
				{
					m_isAdministrator = Boolean.TRUE;
					break;
				}
			}
		}
		return m_isAdministrator.booleanValue();
	}	//	isAdministrator

	/**
	 * 	Has the user Access to BP info and resources
	 *	@param BPAccessType access type
	 *	@param params opt parameter
	 *	@return true if access
	 */
	public boolean hasBPAccess (String BPAccessType, Object[] params)
	{
		if (isFullBPAccess())
			return true;
		getBPAccess(false);
		for (int i = 0; i < m_bpAccess.length; i++)
		{
			if (m_bpAccess[i].getBPAccessType().equals(BPAccessType))
			{
				return true;
			}
		}
		return false;
	}	//	hasBPAccess
	
	/**
	 * 	Get active BP Access records
	 *	@param requery requery
	 *	@return access list
	 */
	public X_AD_UserBPAccess[] getBPAccess (boolean requery)
	{
		if (m_bpAccess != null && !requery)
			return m_bpAccess;
		String sql = "SELECT * FROM AD_UserBPAccess WHERE AD_User_ID=? AND IsActive='Y'";
		ArrayList<X_AD_UserBPAccess> list = new ArrayList<X_AD_UserBPAccess>();
		PreparedStatement pstmt = null;
		try
		{
			pstmt = DB.prepareStatement (sql, null);
			pstmt.setInt (1, getAD_User_ID());
			ResultSet rs = pstmt.executeQuery ();
			while (rs.next ())
			{
				list.add (new X_AD_UserBPAccess (getCtx(), rs, null));
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
		m_bpAccess = new X_AD_UserBPAccess[list.size ()];
		list.toArray (m_bpAccess);
		return m_bpAccess;
	}	//	getBPAccess
	
	
	/**
	 * 	Before Save
	 *	@param newRecord new
	 *	@return true
	 */
	protected boolean beforeSave (boolean newRecord)
	{
		//	New Address invalidates verification
		if (!newRecord && is_ValueChanged("EMail"))
			setEMailVerifyDate(null);
		if (newRecord || super.getValue() == null || is_ValueChanged("Value"))
			setValue(super.getValue());
		return true;
	}	//	beforeSave
	
	
	/**
	 * 	Test
	 *	@param args ignored
	 *
	public static void main (String[] args)
	{
		try
		{
			validateEmail(new InternetAddress("jjanke@adempiere.org"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	//	org.compiere.Xendra.startupClient();
	//	System.out.println ( MUser.get(Env.getCtx(), "SuperUser", "22") );
	}	//	main	/* */
}	//	MUser
