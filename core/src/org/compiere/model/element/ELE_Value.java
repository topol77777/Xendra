package org.compiere.model.element;


import org.xendra.annotations.*;
public class ELE_Value 
{
 	@XendraElement(AD_Element_ID=620,
		ColumnName="Value",
		EntityType="D",
		Name="Search Key",
		PrintName="Search Key",
		Description="Search key for the record in the format required - must be unique",
		Help="A search key allows you a fast method of finding a particular record. If you leave the search key empty, the system automatically creates a numeric number.  The document sequence used for this fallback number is defined in the 'Maintain Sequence' window with the NAME 'DocumentNo_<TableName>', where TableName is the actual NAME of the table (e.g. C_Order).",
		PO_Name="",
		PO_PrintName="",
		PO_Description="",
		PO_Help="",
		Identifier="e75e100c-9b41-a643-2cb3-46eeced683d8",
		Synchronized="2012-07-11 00:00:00.0")
	public static final String Identifier = "e75e100c-9b41-a643-2cb3-46eeced683d8";

}
