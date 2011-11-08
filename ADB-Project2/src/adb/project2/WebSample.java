package adb.project2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//Live Search API 2.0 code sample demonstrating the use of the
//Web SourceType over the XML Protocol.
class WebSample 
{
	static XPathFactory factory = null;
	static XPath xpath = null;
	static XPathExpression expr = null;
	StringBuffer builder = new StringBuffer();
	static BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
	static String userinput = null;

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, 
	XPathExpressionException
	{
		// Build the request.
		System.out.println("Enter the Database URL you want to crawl :");

		userinput = br.readLine();
		if(userinput.startsWith("http://") || userinput.startsWith("https://"))
		{
			userinput=userinput.substring(userinput.indexOf("://")+3);
		}
		String dbURL=userinput;


		System.out.println("Search URL : "+userinput);
		FileReader fileReader = new FileReader("rules.txt");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		List<String> rules = new ArrayList<String>();
		String rule = null;
		while ((rule = bufferedReader.readLine()) != null) {
			rules.add(rule);
		}
		bufferedReader.close();

		String requestURL = BuildRequest(dbURL,rules.get(0));

		// Send the request to the Live Search Service and get the response.
		Document doc = GetResponse(requestURL);

		if(doc != null)
		{
			// Display the response obtained from the Live Search Service.
			DisplayResponse(doc);
		}

	}
	private static String BuildRequest(String dbURL,String searchStr)
	{
		// Replace the following string with the AppId you received from the
		// Live Search Developer Center.
		String AppId = "C1E6E5443C0C93B26B467E3BB8F36D014DE50A97";
		String requestString = "http://api.search.live.net/xml.aspx?"

				// Common request fields (required)
				+ "AppId=" + AppId
				+ "&Query="+searchStr+"%20(site:"+dbURL+")"
				+ "&Sources=Web"

			// Common request fields (optional)
			+ "&Version=2.0"
			+ "&Market=en-us"
			+ "&Adult=Moderate"

			// Web-specific request fields (optional)
			+ "&Web.Count=10"
			+ "&Web.Offset=0";
		/*	+ "&Web.FileType=DOC"
			+ "&Web.Options=DisableHostCollapsing+DisableQueryAlterations";*/

		return requestString;
	}

	private static Document GetResponse(String requestURL) throws ParserConfigurationException, SAXException, 

	IOException 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = null;
		DocumentBuilder db = dbf.newDocumentBuilder();

		if (db != null)
		{              
			doc = db.parse(requestURL);
		}

		return doc;
	}

	private static void DisplayResponse(Document doc) throws XPathExpressionException
	{
		factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		xpath.setNamespaceContext(new APINameSpaceContext());
		NodeList errors = (NodeList) xpath.evaluate("//api:Error",doc,XPathConstants.NODESET);

		if(errors != null && errors.getLength() > 0 )
		{
			// There are errors in the response. Display error details.
			DisplayErrors(errors);
		}
		else
		{
			DisplayResults(doc);
		}
	}

	private static void DisplayResults(Document doc) throws XPathExpressionException 
	{
		String version = (String)xpath.evaluate("//@Version",doc,XPathConstants.STRING);
		String searchTerms = (String)xpath.evaluate("//api:SearchTerms",doc,XPathConstants.STRING);
		int total = Integer.parseInt((String)xpath.evaluate("//web:Web/web:Total",doc,XPathConstants.STRING));
		int offset = Integer.parseInt((String)xpath.evaluate("//web:Web/web:Offset",doc,

				XPathConstants.STRING));
		NodeList results = (NodeList)xpath.evaluate("//web:Web/web:Results/web:WebResult",doc,XPathConstants.NODESET); 

		// Display the results header.
		System.out.println("Live Search API Version " + version);
		System.out.println("Web results for " + searchTerms);
		System.out.println(results.getLength());
		
		/*System.out.println("Displaying " + (offset+1) + " to " + (offset + 

				results.getLength()) + " of " + total + " results ");
		System.out.println();

		// Display the Web results.
		StringBuilder builder = new StringBuilder();

		for(int i = 0 ; i < results.getLength(); i++)
		{
			NodeList childNodes = results.item(i).getChildNodes();

			for (int j = 0; j < childNodes.getLength(); j++) 
			{
				if(!childNodes.item(j).getLocalName().equalsIgnoreCase("DisplayUrl"))
				{
					String fieldName = childNodes.item(j).getLocalName();

					if(fieldName.equalsIgnoreCase("DateTime"))
					{
						fieldName = "Last Crawled";
					}

					builder.append(fieldName + ":" + childNodes.item(j).getTextContent());
					builder.append("\n");
				}
			}

			builder.append("\n");
		}

		System.out.println(builder.toString());*/
	}

	private static void DisplayErrors(NodeList errors) 
	{
		System.out.println("Live Search API Errors:");
		System.out.println();

		for (int i = 0; i < errors.getLength(); i++) 
		{
			NodeList childNodes = errors.item(i).getChildNodes();

			for (int j = 0; j < childNodes.getLength(); j++) 
			{
				System.out.println(childNodes.item(j).getLocalName() + ":" + childNodes.item(j).getTextContent());
			}

			System.out.println();
		}
	}
}
