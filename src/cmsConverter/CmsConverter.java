package cmsConverter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Namespace;

import srw.schema.x1.dcSchema.DcDocument;
import srw.schema.x1.dcSchema.SrwDcType;

import com.exlibris.core.ParseSRWResponse;
import com.exlibris.core.SRWRecord;
import com.exlibris.core.ERESearchResultsSet;
import com.exlibris.core.sdk.exceptions.IEWSException;
import com.exlibris.core.sdk.formatting.DublinCore;
import com.exlibris.core.sdk.formatting.DublinCoreFactory;
import com.exlibris.digitool.exceptions.RepositoryException;
import com.exlibris.repository.eme.EMEResultSet;

import dps.utils.JDBCConnection;

public class CmsConverter {

	private static final String CONFIG_FILE="external_resource_explorer_configuration.xml";
	private static String getcontantById = "SELECT FILE_CONTENT FROM FILE_TABLE WHERE FILE_KEY='com.exlibris.dps.infra.externalresourceexplorer'";
	private static String getJobDetails = "SELECT FILE_CONTENT FROM FILE_TABLE WHERE FILE_KEY='com.exlibris.dps.repository.metadataloadjob'";
	private static String dublincore_configuration = "SELECT FILE_CONTENT FROM FILE_TABLE WHERE FILE_KEY='com.exlibris.dps.webeditor.configuration.dc'";
	private static String is_cms_record_used;
	private static PreparedStatement getKeyIdPS;
	private static PreparedStatement mdJobConfig;
	private static PreparedStatement dcConfiguration;
	private static PreparedStatement cmsRecordCount;
	private static JDBCConnection conn;
	private static String repositoryName;
	private static String filePrefix;
	private static String error_flag="none";
	private static String file_error_flag="none";
	private static String PROCESSED="processed";
	static Logger log= new Logger();
	
	private static String ListRecords="";
	public static void main(String[] args) {
			try {
				log.info("Starting to synchronization between CMS Server to Rosetta");
				
				try{
					conn = new JDBCConnection(args[0], "ros",args[2],true);
				}catch(Exception e){
					log.error("failed to build the connection. ");
					log.error(e.getMessage());
					System.exit(2);
				}
				getKeyIdPS = conn.getConnection("ros").prepareStatement(getcontantById);
				mdJobConfig =  conn.getConnection("ros").prepareStatement(getJobDetails);
				String xmlContent=getKeyId(getKeyIdPS);
				String xmlJobContent=getKeyId(mdJobConfig);
				String urlContent="";
				String BaseUrl="";
				String recordSchema="";
				String operation="";
				String mddir=org.apache.commons.lang.StringUtils.substringBetween(xmlJobContent,"<adapter_param key=\"mddir\" value=\"","\">");
				filePrefix = org.apache.commons.lang.StringUtils.substringBetween(xmlJobContent,"<adapter_param key=\"filenameprefix\" value=\"","\">");
				repositoryName ="";
				log.info("mddir:   "+mddir);
				final File folder = new File(args[1]);
				if(!folder.exists()){
					log.error("No such file or directory"+args[1]);
					System.exit(2);
				}
				for (final File fileEntry : folder.listFiles()){
						if(fileEntry.isDirectory())
							continue;
						log.info("Starting to read file: "+fileEntry.getName());
						File file = null;
						try{
							file = new File(args[1]+"/"+fileEntry.getName());	
							FileReader fileReader = new FileReader(file);
							BufferedReader bufferedReader = new BufferedReader(fileReader);
							String cmsId;
							while ((cmsId = bufferedReader.readLine()) != null) {
								log.info("Start to synchronization CMS-record: "+cmsId);

								Pattern p = Pattern.compile("(<RepositoryName" + ".*?" + "</RepositoryName>)",Pattern.DOTALL);
								Matcher m = p.matcher(xmlContent);
								while (m.find()) {
									String externalResource  = m.group(1);
									repositoryName =org.apache.commons.lang.StringUtils.substringBetween(externalResource,"<RepositoryName name=\"","\"");
									is_cms_record_used = "SELECT count(*) FROM HDEMETADATA WHERE EXTERNAL_SYSTEM = '"+repositoryName+"' AND EXTERNAL_SYSTEM_ID = '"+cmsId+"'";
									cmsRecordCount =  conn.getConnection("ros").prepareStatement(is_cms_record_used);
									if(Integer.parseInt(getKeyId(cmsRecordCount))==1){							
										BaseUrl=org.apache.commons.lang.StringUtils.substringBetween(externalResource,"<parm name=\"baseUrl\">","</parm>");
										recordSchema=org.apache.commons.lang.StringUtils.substringBetween(externalResource,"<parm name=\"recordSchema\">","</parm>");
										operation=org.apache.commons.lang.StringUtils.substringBetween(externalResource,"<parm name=\"operation\">","</parm>");
										
										break;
									}
								}
								if(BaseUrl.isEmpty()){
									log.info("Cms record id: "+cmsId+ "  doesn’t exist in the DPS");
									continue;
								}
								String  url=BaseUrl+"?version=1.1&operation="+operation+"&query=rec.id="+cmsId+"&maximumRecords=1&recordSchema="+recordSchema;
								URL connection = new URL(url);
								BufferedReader in = new BufferedReader(new InputStreamReader(connection.openStream())); 
								String inputLine;
						        while ((inputLine = in.readLine()) != null)
						        	urlContent+=inputLine;
						        in.close();				
						        try{
						        //	urlContent=str;
						        	log.info("cms content: \n"+urlContent);
						        	if(urlContent.isEmpty()){
						        		throw new Exception();
						        	}
						    		ERESearchResultsSet searchResults =  new ERESearchResultsSet(urlContent);
						    		if(searchResults != null){
							        	ParseSRWResponse srwParser = new ParseSRWResponse(urlContent);
							        	List<SRWRecord> resultsObjects=searchResults.getResultsObjects();
							        	String record;		
										if (resultsObjects != null && resultsObjects.size() > 0){
											for ( int set = 0; set < resultsObjects.size(); set++){
												SRWRecord srwRecord = resultsObjects.get(set);
												record=converdSRWdctoDPSdc(srwRecord.getData() ,cmsId);
												ListRecords+=AddRecordHeader(record ,repositoryName , cmsId);
											}
										}
						    		}
						        }catch(Exception e){
						        	log.error("coudn't read CMS record "+cmsId+" from the cms server");
						        	log.error(e.getMessage());
						        	file_error_flag="error";
						        	error_flag="error";
								}
						    log.info("finished to synchronization CMS record: "+cmsId);   
						    urlContent="";
							}
							addOAInameSpace();
							if(!createJobFile(mddir ,cmsId)){
								log.error("failed to create the file");
							}
							fileReader.close();
							
					}catch(Exception e){
						log.error(e.getMessage());
						error_flag="error";
						file_error_flag="error";
					}
					if(file_error_flag.equals("error")){
						log.info("finished to read file: "+fileEntry.getName()+" with errors.");
					}else if(file_error_flag.equals("warn")){
						log.info("finished to read file: "+fileEntry.getName()+" with warnings.");
					}else{
						log.info("finished to read file: "+fileEntry.getName());
					}
					moveFileToDir(file , PROCESSED);	
					ListRecords="";
					file_error_flag="none";
				}
			} catch (Exception e) {
					log.error(e.getMessage());
					System.exit(2);
			}
			
		if(error_flag.equals("error")){
			System.exit(2);
		}else if(error_flag.equals("warn")){
			System.exit(3);
		}

	}
	
	private static boolean createJobFile(String dir ,String cmsId) throws FileNotFoundException, UnsupportedEncodingException {
		if (dir == null || ! isValidateDirectory(dir)) {
			log.error("directory name: "+dir+ " is null or invalid");
			error_flag="error";
			return false;
		}
		if(ListRecords.isEmpty())
			return false;
		String timeStamp = getTime(null);
		log.info("creating  file: "+filePrefix+".oai."+ timeStamp+".xml");
		try{
			PrintWriter writer = new PrintWriter(dir+"/"+filePrefix+".oai."+ timeStamp+".xml", "UTF-8");	
			writer.println(ListRecords);
			writer.close();
		}catch(Exception e){
			log.error("An error occurred while creating file: "+filePrefix+".oai."+ timeStamp +".xml");
			error_flag="error";
			return false;
		}
		return true;
		
	}
	private static void addOAInameSpace() {
		if(ListRecords.isEmpty())
			return;
		String OAINameSpace="<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">";
		String OAICloseTag="</OAI-PMH>";
		ListRecords=OAINameSpace+"<ListRecords>"+ListRecords+"</ListRecords>"+OAICloseTag;
	
	}
	private static String AddRecordHeader(String record ,String repositoryName ,String cmsId ) {
		record="<metadata>"+record+"</metadata>";
		String identifier=org.apache.commons.lang.StringUtils.substringBetween(record,"<dc:identifier>","</dc:identifier>");
		identifier="<identifier>"+identifier+"</identifier>";
		String header ="<header>"+identifier+"</header>";
		String complateRecord="<record>"+header+record+"</record>";
		return complateRecord;
		
		
	}
	public static String getKeyId(PreparedStatement preparedStatement) throws SQLException {

		String ret = null;
	//	getKeyIdPS.setString(1, key);
		ResultSet res = preparedStatement.executeQuery();

		if (res.next()) {
			ret = res.getString(1);
		}

		res.close();
		return ret;
	}
	
	private static String converdSRWdctoDPSdc(String xml, String cmsId)throws Exception  {

		//final String dcNamespace="xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:marcrel=\"http://www.loc.gov/loc.terms/relators/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:mods=\"http://www.loc.gov/mods/v3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
		final String dcRecordClose ="dc:record";
		final String OAIprefix="dc:oai_dc";
		//final String OAInamespace=" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\"";
		final String OAIschemaLocation=" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/http://www.openarchives.org/OAI/2.0/oai_dc.xsd\" ";
		Namespace oai_dc= new Namespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
		//String defaultNameSpace = "";
		DcDocument dcDoc;
		log.info("Starting to parse cmsId: "+cmsId+"to dc format");
		try {
			dcDoc = DcDocument.Factory.parse(xml);
			SrwDcType srwdc = dcDoc.getDc();
		} catch (XmlException e1) {
			log.error("coudn't parse CMS-record: "+cmsId+" to dc format");
			log.error(e1.getMessage());
			throw new IEWSException(e1);
		}
		
		// parse
		try {
			String oldNamespace=org.apache.commons.lang.StringUtils.substringBetween(xml,"<" ,">");
			String oldPrefixNamespace =org.apache.commons.lang.StringUtils.substringBetween(xml,"<" ," ");
			if(oldNamespace.contains("xsi:schemaLocation")){
				Pattern p = Pattern.compile("(xsi:schemaLocation=\"" + ".*?" + "\" )",Pattern.DOTALL);
				Matcher m = p.matcher(oldNamespace);
				if(m.find()) {
					String schemaLocation  = m.group(1);
					xml=xml.replaceFirst(schemaLocation, " ");
				}
			}
			//defaultNameSpace =org.apache.commons.lang.StringUtils.substringBetween(xml,"xmlns=\"","\"");
			if(oldNamespace.contains("xmlns:dc")){
				xml=xml.replaceFirst(oldPrefixNamespace, dcRecordClose  + OAIschemaLocation);
			}else{
				xml=xml.replaceFirst(oldPrefixNamespace, dcRecordClose  + OAIschemaLocation+" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"");
			}
			
			String oldClose=xml.substring(org.apache.commons.lang.StringUtils.lastIndexOf(xml, "<")+2, org.apache.commons.lang.StringUtils.lastIndexOf(xml, ">"));
			xml= xml.replaceAll(oldClose, dcRecordClose);
			//create empty DublinCore document
			DublinCore dc = DublinCoreFactory.getInstance().createDocument();
			dc.addNamespace(oai_dc);
			try {
				xml=parseXml(xml, dc);
			} catch (IOException e) {
				throw new Exception(e);
			}
			xml=xml.replaceAll(dcRecordClose, OAIprefix);			
			
		//	validateXml(dc,record);

		}catch (IEWSException e){
			throw new IEWSException(e);

		}
		log.info("finished to parse cmsId: "+cmsId+"to dc format");
		return xml;
	}
private static String parseXml(String xml ,DublinCore dc)throws RepositoryException ,IEWSException, IOException, SQLException{
		
		DublinCore dcTemp =null;
		try {
			dcTemp = DublinCoreFactory.getInstance().createDocument(xml);
		} catch (DocumentException e) {
			log.error("cannot parse cms xml");
			throw new IEWSException("cannot parse cms xml");
		}
		//String defaultNameSpace=org.apache.commons.lang.StringUtils.substringBetween(xml," xmlns=\"","\"");
		Document m_document = dcTemp.getDocument();
		org.dom4j.Element root = m_document.getRootElement();
		// iterate through child elements of root
        for ( Iterator i = root.elementIterator(); i.hasNext(); ) {
        	org.dom4j.Element element = (org.dom4j.Element) i.next();
        	if(validateElement(element)){
        		setDcData(element, dc);
        	}
        	
        }
    	xml=dc.toXml().substring(dc.toXml().indexOf(">")+1,dc.toXml().length());
    	return xml;

	}
	private static boolean validateElement(org.dom4j.Element element) throws SQLException {
		dcConfiguration =  conn.getConnection("ros").prepareStatement(dublincore_configuration);
		String xmlDcContent=getKeyId(dcConfiguration);
		String elem=createDcNode(element);
		if(elem==null){
			log.warn("dublincore_configuration file does not have "+ element.getName()+" element");
			if(error_flag.equals("none")){
				error_flag="warn";
    		}
			return false;
		}
    	if (!xmlDcContent.contains(elem)){
    		log.warn("dublincore_configuration file does not have "+elem+" element");
    		if(error_flag.equals("none")){
				error_flag="warn";
    		}
    		return false;
    	}
        return true;
	}
	private static void setDcData(org.dom4j.Element element, DublinCore dc)throws RepositoryException ,IEWSException {
		if (element == null)
			return;
		Map<String,Integer> allPrefix = PrefixMap();
		Map<String,Integer> allNameSpace = NameSpaceMap();
		String namespacePrefix =element.getNamespacePrefix();
		String namespaceURI	=element.getNamespaceURI();
		int NameSpace=10;
		String tag =element.getName();
		if(namespacePrefix!=""){
			if(allPrefix.get(namespacePrefix.toLowerCase())==null){
				return;
			}
			NameSpace=allPrefix.get(namespacePrefix.toLowerCase());
		}
		else if(namespaceURI!=""){
			if(allNameSpace.get(namespaceURI.toLowerCase())==null){
				return;
			}
			NameSpace=allNameSpace.get(namespaceURI.toLowerCase());
		}
		else{
			throw new IEWSException("cannot parse cms xml");
		}
		//add the element and xsi:type attribute (if defined) to the dc
		if (element.attributes().size()==0){
		    dc.addElement(NameSpace, tag, element.getText());
		}else{
			dc.addElement(NameSpace, tag, element,
					element.getText(),false);
		}
	}
	private static String createDcNode(org.dom4j.Element element){
		Map<String,String> allNameSpace = NameSpacePrfixMap();
		String namespacePrefix =element.getNamespacePrefix();
		String namespaceURI	=element.getNamespaceURI();
		String tag =element.getName();
		if(namespacePrefix!=""){
				return (namespacePrefix + ":" +  element.getName());
		}
		else if(namespaceURI!=""){
			if(allNameSpace.get(namespaceURI.toLowerCase())!=null){
				 return (allNameSpace.get(namespaceURI.toLowerCase()) + ":" +  element.getName());
			}
		}
		else{
			return null;
		}
		return null;
	}
	private final static boolean isValidateDirectory(String directoryname) {

		if (directoryname == null || directoryname.length() == 0)
			return false;

		File directory = new File(directoryname);
		if ((! directory.isDirectory()) || (! directory.exists()))
			return false;

		return true;
	}
	/* Moves file to a specific directory */
	private static void moveFileToDir(File file, String dirName){
		boolean success = true;
		String dirPath = file.getParent()+file.separatorChar+dirName;
		File directory = new File(dirPath);
		// creates dest directory if needed
        if (!directory.exists()) {
        	success= directory.mkdir();
            directory.setWritable(true);
        }
        // adds a random number to the name to avoid files with the same name in the dest directory
		String destPath = dirPath+file.separatorChar+file.getName();
		String time = new Timestamp(System.currentTimeMillis()).toString().replace('.', '_').replace(':', '_').replace(" ", "_");
		destPath+=("_"+time);
		success = file.renameTo(new File(destPath));
		if(success){
	    	log.info("File is moved successful!");
	    }else{
	    	log.info("File is failed to move to processed folder!");
	    }
	}
	
	private final static String getTime(String format)  {

    	if (format == null)
    		format = "yyyyMMddhhmmssSSS";

        DateFormat timeformat	= new SimpleDateFormat(format);
        Calendar now			= Calendar.getInstance();
        return timeformat.format(now.getTime());
    }
	private static Map<String,Integer> PrefixMap() {
		Map<String,Integer> allPids = new HashMap<String, Integer>();
		allPids.put("dc",10);
		allPids.put("dcterms",20);
		allPids.put("mods",30);
		allPids.put("marcrel",40);
		return allPids;
	}
	private static Map<String,Integer> NameSpaceMap() {
		Map<String,Integer> allPids = new HashMap<String, Integer>();
		allPids.put("http://purl.org/dc/elements/1.1/",10);
		allPids.put("http://purl.org/dc/terms/",20);
		allPids.put("http://www.loc.gov/mods/v3",30);
		allPids.put("http://www.loc.gov/marc/relators/",40);
		return allPids;
	}
	private static Map<String,String> NameSpacePrfixMap() {
		Map<String,String> allPids = new HashMap<String, String>();
		allPids.put("http://purl.org/dc/elements/1.1/","dc");
		allPids.put("http://purl.org/dc/terms/","dcterms");
		allPids.put("http://www.loc.gov/mods/v3","mods");
		allPids.put("http://www.loc.gov/marc/relators/","marcrel");
		return allPids;
	}

}
