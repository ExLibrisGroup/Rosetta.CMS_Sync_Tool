package com.exlibris.core;

public class EREConstants {

	public static final String CONF_FILE 		= "com.exlibris.dps.infra.externalresourceexplorer";
	public static final String EXPALIN_FILE 	= "com.exlibris.dps.delivery.sru.explainproperties";
	public static final String URLS_FILE		= "com.exlibris.dps.viewers.digitalentityurls";
	public static final String DC 				= "dc";
	public static final String ANY 				= "ANY";
	public static final String DPS 				= "DPS";
	public static final String UTF8 			= "UTF-8";
	public static final int TIMEOUT 			= 30000; // 30 sec

	public static final String COLON 			= "\\:";
	public static final String QUESTIONMARK 	= "?";
	public static final String EQUAL 			= "=";
	public static final String QUOTE 			= "\"";
	public static final String SPACE			= " ";
	public static final String URLSPACE			= "%20";
	public static final String AMPERSAND 		= "&";
	public static final String SLASH			= "/";
	public static final String DOT				= ".";

	public static final String SYNCHUPDATE 		= "synchUpdate";
	public static final String DETACHURL 		= "detachUrl";
	public static final String UPDATEURL 		= "updateUrl";
	public static final String UPDATEURNURL 	= "updateURNUrl";
	public static final String BASEURL 			= "baseUrl";
	public static final String INDEXNAME 		= "indexName";
	public static final String INDEXNAME2 		= "indexName2";
	public static final String INDEXNAME3 		= "indexName3";
	public static final String ANDQUERYEQUAL 	= "&query=";

	public static final String VERSION 			= "version";
	public static final String VERSION1_1 		= "1.1";
	public static final String VERSION1_2 		= "1.2";
	public static final String QUERY			= "query";
	public static final String RECORDSCHEMA 	= "recordSchema";
	public static final String RECORDSCHEMADC 	= "dc";
	public static final String STARTRECORD		= "startRecord";
	public static final String MAXIMUMRECORDS 	= "maximumRecords";
	public static final String RECORDPACKING 	= "recordPacking";
	public static final String RECORDPACKINGXML = "xml";
	public static final String OPERATION		= "operation";
	public static final String SEARCHRETRIEVE	= "searchRetrieve";
	public static final String EXPLAIN			= "explain";

	public static final String DEFAULTMDTYPE 	= "CMS:dc";
	public static final String DCTITLE 			= "dc:title";
	public static final String DCCREATOR 		= "dc:creator";
	public static final String DCSUBJECT 		= "dc:subject";
	public static final String DCDESCRIPTION 	= "dc:description";
	public static final String DCPUBLISHER 		= "dc:publisher";
	public static final String DCCONTRIBUTOR 	= "dc:contributor";
	public static final String DCDATE 			= "dc:date";
	public static final String DCTYPE 			= "dc:type";
	public static final String DCFORMAT 		= "dc:format";
	public static final String DCIDENTIFIER 	= "dc:identifier";
	public static final String DCSOURCE 		= "dc:source";
	public static final String DCLANGUAGE 		= "dc:language";
	public static final String DCRELATION 		= "dc:relation";
	public static final String DCCOVERAGE 		= "dc:coverage";
	public static final String DCRIGHTS 		= "dc:rights";

	public static final String TITLE 			= "title";
	public static final String CREATOR 			= "creator";
	public static final String SUBJECT 			= "subject";
	public static final String DESCRIPTION 		= "description";
	public static final String PUBLISHER 		= "publisher";
	public static final String CONTRIBUTOR 		= "contributor";
	public static final String DATE 			= "date";
	public static final String TYPE 			= "type";
	public static final String FORMAT 			= "format";
	public static final String IDENTIFIER 		= "identifier";
	public static final String SOURCE 			= "source";
	public static final String LANGUAGE 		= "language";
	public static final String RELATION 		= "relation";
	public static final String COVERAGE 		= "coverage";
	public static final String RIGHTS 			= "rights";


	public static final String XMLDECLUTF8 		= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	public static final String DCSCHEMA 		= "info:srw/schema/1/dc-v1.1";
	public static final String EXPLAINSCHEMA 	= "http://explain.z3950.org/dtd/2.1/";
	public static final String XSINAMESPACE		= "http://www.w3.org/2001/XMLSchema-instance";
	public static final String DUBLINCORE 		= "Dublin Core";
	public static final String ENGLISH 			= "en";
	public static final String PID 				= "PID";
	public static final String OBJECTTYPE 		= "objectType";


	// explain constants
	public static final String HOST 			= "host";
	public static final String PORT 			= "port";
	public static final String DATABASE			= "database";
	public static final String DEFAULT 			= "default";
	public static final String FALSE			= "false";
	public static final String TRUE				= "true";
	public static final String DATABASEINFO		= "dbInfo";
	public static final String PRIMARY			= "primary";
	public static final String NUMBEROFRECORDS 	= "numberOfRecords";
	public static final String EXTERNALLIB 		= "md.ie.externalLibrary";
	public static final String EXTERNALLIBID 	= "md.ie.externalLibraryId";
	public static final String CMSRECORDID      = "CMS Record ID";
	public static final String RECORDID      	= "recordId";
	public static final String CMSSYSTEM     	= "CMS System";
	public static final String SYSTEM     		= "system";

	// types
	public static final String ALEPH     		= "ALEPH";
	public static final String VOYAGER     		= "VOYAGER";


}
