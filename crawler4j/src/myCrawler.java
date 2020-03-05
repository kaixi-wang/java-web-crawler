import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.crawler.exceptions.ContentFetchException;
import edu.uci.ics.crawler4j.crawler.exceptions.PageBiggerThanMaxSizeException;
import edu.uci.ics.crawler4j.crawler.exceptions.ParseException;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;



import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpStatus;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.frontier.DocIDServer;
import edu.uci.ics.crawler4j.frontier.Frontier;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.parser.Parser;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;


//only visits HTML, doc, pdf and different image format URLs 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

//import org.apache.http.HttpStatus;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.NotAllowedContentException;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;


class FileWriter{
	String fileName;
	private PrintWriter pwriter;
	
	
	FileWriter(String name)
	{
		
		
		// comment out synchronized(this) if not resuming/causing problems
		synchronized(this)
		{
			//File f = new File(name);
			Path path=Paths.get(name);
			if (!Files.exists(path)) {
		        try {
		            Files.createDirectories(path.getParent());
		        } catch (IOException e) {
		            //fail to create directory
		            e.printStackTrace();
		            return;
		        }
			}
			//}
			//File f = new File(name);
			 // if(f.exists()) {
			//	fileName="/
			//}
			
		
			fileName = name;
			try {
				pwriter = new PrintWriter(new FileOutputStream(fileName));
				System.out.printf("%s created \n", fileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	
	
  public  void writeData(String data)
  {   
  	synchronized(this){
  		//System.out.println("PRINT" + data);
	    	pwriter.println(data);    	
	    	pwriter.flush();
  	}
  }
	
  public void closeWriter()
  {
  	pwriter.close();
  }
	
}

//Object to hold data from crawler
class Locker
{
	static Object lock =  new Object();
}

public class myCrawler extends WebCrawler {
	

	 static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd_HH-mm");
	 static Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	 static String ts=sdf.format(timestamp);
		
		//static String runFolder= "/"+ts;
	static String crawlStorageFolder = "crawlData_from_"+ts;
	//static String crawlStorageFolder = "crawlData";
	private final static Pattern keepFILTERS = Pattern.compile(".*(\\.(html|htm|pdf|doc|jpg|gif|png))$");
	//private static final Pattern jsonfilter=Pattern.compile(".*((\.\?-"
	private static final Pattern bodyFILTERS = Pattern.compile(".*[^a-z]?(css|js|json|xml|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v)[^a-z]?.*"); 
  private static final Pattern extFILTERS = Pattern.compile(".*(\\.rm|smil|wmv|swf|wma|zip|rar|gz|asc|ico)$");
          //".*(\\.(css|js|json|xml|mid|mp2|mp3|mp4|wav|avi|mov|mpeg|ram|m4v" +
          //"|rm|smil|wmv|swf|wma|zip|rar|gz))$");
  //only images allowed: image/gif, image/jpeg, image/png
  private static final Pattern imgFILTERS = Pattern.compile(".*[^a-z]?(gif|jpe?g|png)[^a-z]?.*");
  private static final Pattern jsFILTERS = Pattern.compile(".+(embedded\\.html)$");
  
	static FileWriter fetchWriter = new FileWriter(crawlStorageFolder+"/fetch.csv");
	static FileWriter visitWriter = new FileWriter(crawlStorageFolder+"/visit.csv");
	static FileWriter contentWriter = new FileWriter(crawlStorageFolder+"/content.csv");
	static FileWriter nullWriter = new FileWriter(crawlStorageFolder+"/nullcontent.csv");
	static FileWriter urlsWriter = new FileWriter(crawlStorageFolder+"/urls.csv");
	static FileWriter errorWriter = new FileWriter(crawlStorageFolder+"/errors.csv");
	static int attemptedFetch = 0;
	static int successfulFetch = 0;
	static int failedFetches = 0;
	//static int abortedFetches = 0;
	static ArrayList<String>totalUrlList = new ArrayList<String>();
	static ArrayList<String>visitUrlList = new ArrayList<String>();
	static ArrayList<String>contentList = new ArrayList<String>();
	static LinkedHashSet<String>uniqueUrlList = new LinkedHashSet<String>();
	static LinkedHashSet<String>uniqueUrlList_OK = new LinkedHashSet<String>();
	static LinkedHashSet<String>uniqueUrlList_NOK = new LinkedHashSet<String>();
	
	static LinkedHashMap <Integer,Integer> statusCodeList = new LinkedHashMap<Integer,Integer>();
	static LinkedHashMap <Integer,String> statusDescriptionList = new LinkedHashMap<Integer,String>();
	static LinkedHashMap <String,Integer> contentTypeList = new LinkedHashMap<String,Integer>();
	static LinkedHashMap <String,Integer> sizeTypeMap = new LinkedHashMap<String,Integer>();
	
	
	
	public void addtoList(Set t,String data)
	{
		synchronized(Locker.lock)
		{
			t.add(data);
		}
	}
	
	public void addSize(int size)
	{
		synchronized(Locker.lock)
		{
			Map<String, Integer> m = sizeTypeMap;
			if(size < 1)
			{
				Integer val = m.get("< 1KB");
				if(val == null)
					m.put("< 1KB",1);
				else
					m.put("< 1KB",val+1);			
			}
			else if(size < 10)
			{
				Integer val = m.get("1KB ~ <10KB");
				if(val == null)
					m.put("1KB ~ <10KB",1);
				else
					m.put("1KB ~ <10KB",val+1);
			}
			else if(size < 100)
			{
				Integer val = m.get("10KB ~ <100KB");
				if(val == null)
					m.put("10KB ~ <100KB",1);
				else
					m.put("10KB ~ <100KB",val+1);
			}
			else if(size < 1024)
			{
				Integer val = m.get("100KB ~ <1MB");
				if(val == null)
					m.put("100KB ~ <1MB",1);
				else
					m.put("100KB ~ <1MB",val+1);
			}
			else
			{
				Integer val = m.get(">= 1MB");
				if(val == null)
					m.put(">= 1MB",1);
				else
					m.put(">= 1MB",val+1);
			}
		}
	}
	@Override
	/*catch (ContentFetchException | SocketTimeoutException cfe) {
        onContentFetchError(curURL);
        onContentFetchError(page);
        */
	protected void onContentFetchError(Page page) {
		String urlStr = (page.getWebURL() == null ? "NULL" : page.getWebURL().getURL());
        System.out.println("Can't fetch content of: "+ urlStr);
		synchronized(Locker.lock)
		{
	        //contentErrorList.add(urlStr);
			errorWriter.writeData(urlStr.replace(",", "-")+",ContentFetchError");
	        
		}
	        
	}
	 /**@Override
	    /**
	     * Determine whether links found at the given URL should be added to the queue for crawling.
	     * By default this method returns true always, but classes that extend WebCrawler can
	     * override it in order to implement particular policies about which pages should be
	     * mined for outgoing links and which should not.
	     *
	     * If links from the URL are not being followed, then we are not operating as
	     * a web crawler and need not check robots.txt before fetching the single URL.
	     * (see definition at http://www.robotstxt.org/faq/what.html).  Thus URLs that
	     * return false from this method will not be subject to robots.txt filtering.
	     *
	     * @param url the URL of the page under consideration
	     * @return true if outgoing links from this page should be added to the queue.
	     
	    protected boolean shouldFollowLinksIn(WebURL url) {
	        return true;
	    }
	*/

	  private String getHeaderContent(WebURL curURL) {
		  String contentType= "";
		  URL vurl=null;
		try {
			vurl = new URL(curURL.getURL());
			HttpURLConnection connection;
			connection = (HttpURLConnection)  vurl.openConnection();
			connection.setRequestMethod("HEAD");
			connection.connect();
			String[] contentTypelist = connection.getContentType().split("\n", 2);
			contentType=contentTypelist[0];
			//System.out.println(vurl.toString());
			//System.out.println("CONTENT TYPE: " +contentType);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("isValidContent error");
		}
		  StringBuilder contentS = new StringBuilder();
		  //Boolean isValid = null; 
		  if (vurl!=null) {
			  contentS.append(vurl.toString().replaceAll(",", "-")+",");
			  contentS.append(contentType+",");
			  contentWriter.writeData(contentS+",");
		  }
		  
		  return contentType;
	  }
		  //System.out.println(contentS);
		  //Page opage = new Page(curURL);
		//String contentType = opage.getContentType();
		/*contentS.append(contentType+",");
		contentWriter.writeData(contentS+",");
		if (contentType.indexOf("json")!=-1) {
			isValid=false;
		}
		else if (contentType.indexOf("image")!=-1 & imgFILTERS.matcher(contentType).matches() ){
			isValid=true;
		}
		else if (contentType.indexOf("application")!=-1 & contentType.indexOf("pdf")!=-1) {
			isValid=false;
		}
		else if (contentType.indexOf("text")!=-1 & contentType.indexOf("html")!=-1) {
			isValid=f;
		}
		else {
			isValid=false;
		}
		  
		  contentWriter.writeData(contentS+",");
		  return isValid;
	  }*/

	/**
	 * This method receives two parameters. The first parameter is the page
	 * in which we have discovered this new url and the second parameter is
	 * the new url. You should implement this function to specify whether
	 * the given url should be crawled or not (based on your crawling logic).
	 * In this example, we are instructing the crawler to ignore urls that
	 * have css, js, git, ... extensions and to only accept urls that start
	 * with "http://www.viterbi.usc.edu/". In this case, we didn't need the
	 * referringPage parameter to make the decision.
	 */
	@Override
	 public boolean shouldVisit(Page referencedPage,WebURL url) {
			 String href = url.getURL().toLowerCase();
			 String hrefC = url.getURL();
			 Boolean inDomain;
			 Boolean validType;
			 Boolean validated=null;
			 
			 synchronized(Locker.lock)
			{
				 totalUrlList.add(hrefC);
			}
			 addtoList(uniqueUrlList, hrefC);
			 if (totalUrlList.size()%10000==0) {
				 System.out.println("Total Urls so far: "+ totalUrlList.size());
				 System.out.println("# Success Fetched: "+ successfulFetch);
				 System.out.println("Visit Urls so far: "+ visitUrlList.size());
				 }
			
	
			 String urlStatus = null;
			 /*if(href.startsWith("https://www.nytimes.com") | href.startsWith("https://nytimes.com"))*/
			 
			 if(href.startsWith("https://www.bbc.com/news") | href.startsWith("https://bbc.com/news"))
			 {
				 
				 urlStatus = "OK";
				 addtoList(uniqueUrlList_OK, hrefC);	
				 inDomain = true;
				 
			 }
			 else
			 {
				 addtoList(uniqueUrlList_NOK, hrefC);
				 urlStatus = "N_OK";
				 inDomain = false;
			 }
			 //collecting all urls
			 urlsWriter.writeData(hrefC.replace(",", "-")+","+urlStatus);
			 
			 //parsing url for valid filetypes
			 if (bodyFILTERS.matcher(href).matches() | extFILTERS.matcher(href).matches()) {
				 validType=false;
		        }
			 else if (keepFILTERS.matcher(href).matches() & jsFILTERS.matcher(href).matches() ) {
				validType=false;
			 }
			 else {
				 validType=true;
			 }
			 /*
			 if (validType==true) {
				 String contentType = getHeaderContent(url);
				 //System.out.println(contentType);
				 if (contentType==null) {
					 System.out.println("null content type: " + hrefC);
					 nullWriter.writeData(hrefC);
					 validated=true;
				 }
				 else if (contentType.indexOf("json")!=-1) {
						validated=false;
				}
				else if (contentType.indexOf("image")!=-1 & imgFILTERS.matcher(contentType).matches() ){
					validated=true;
				}
				else if (contentType.indexOf("application")!=-1 & contentType.indexOf("pdf")!=-1) {
					validated=false;
				}
				else if (contentType.indexOf("text")!=-1 & contentType.indexOf("html")!=-1) {
					validated=false;
				}
				else {
					validated=true;
				}
				 if (validType!=validated) {
				 		errorWriter.writeData(hrefC.replace(",", "-")+",mismatch,"+urlStatus+","+contentType);
			 }
			 else {
				 validated=true;
			 }
			//else {
			//	logger.warn("in ShouldVisit: url not validated for content");
			//	validated= false;
			//}
		 	//if (validType!=validated) {
		 	//	errorWriter.writeData(hrefC.replace(",", "-")+",mismatch,"+urlStatus+","+contentType);
		 		//System.out.println("MISMATCH: validType!=validated \n " + hrefC);
		 	}
			 boolean retStatus = inDomain & validType & validated;*/
			 boolean retStatus = inDomain & validType;
			 
			 return retStatus; 
		 }

		 /**
		  * This function is called when a page is fetched and ready
		  * to be processed by your program.
		  * 
		  * visit.csv = the files it successfully downloads
		  * 
		  * public void visit(Page page) {
			  String url = page.getWebURL().getURL();
			  String contentType = new String();
			  
			  try {
			  URL url1 = new URL(url);
			  HttpURLConnection connection = (HttpURLConnection)  url1.openConnection();
			  connection.setRequestMethod("HEAD");
			  connection.connect();
			  contentType = connection.getContentType();
			  }
			  catch (MalformedURLException e) {
		            e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		  */
		  @Override
		  public void visit(Page page) {			  
			  StringBuilder dataS = new StringBuilder();
			  String url = page.getWebURL().getURL();
			  String surl =url.toLowerCase();
			  String url1=url.replace(",", "-");
			  dataS.append(url1+",");
			  /*
			  String contentType = new String();
			  
			  try {
			  URL url1 = new URL(url);
			  HttpURLConnection connection = (HttpURLConnection)  url1.openConnection();
			  connection.setRequestMethod("HEAD");
			  connection.connect();
			  httpcontentType = connection.getContentType();
			  }
			  catch (MalformedURLException e) {
		            e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}*/
			  synchronized(Locker.lock)
				{
				  visitUrlList.add(url1);
				}
				  //if (httpcontentType) {
				  //visitURLWriter.writeData(surl+","+contentType);
				  //}
				  
				  //visitURLWriter.writeData(url1);
				  String detailedContentType = page.getContentType().toLowerCase();
				  
				  if (detailedContentType==null) {
					  errorWriter.writeData(surl.replace(",", "-")+",NULL,contentType");
					  dataS.append("null,");
					  dataS.append("null,");
					  dataS.append("null");
				  }
				  else {
					  String contentType=detailedContentType.split(";")[0];
					  
					  if(contentTypeList.get(contentType) == null)
					 {
						 contentTypeList.put(contentType,1);
					 }
					 else
					 {
						 contentTypeList.put(contentType,contentTypeList.get(contentType)+1);
					 }
			
					  int sizeKB = page.getContentData().length/1024;
					  addSize(sizeKB);
				 //System.out.println("Visiting : "+url);
				 //synchronized(Locker.lock)
				//	{
					  ParseData  pd = page.getParseData();	
					  
					  if (pd != null) {
						  Set<WebURL>olinks = pd.getOutgoingUrls();
						  dataS.append(page.getContentData().length+",");
						  dataS.append(olinks.size()+",");
						  dataS.append(contentType);
						}
					  
						else {
								  dataS.append("NULL,");
								  dataS.append("NULL"+",");
								  dataS.append("NULL");
						}
				  //dataS.append(page.getContentType());
				//	}
				 }
				  visitWriter.writeData(dataS.toString());
			         
			  }
			  
		  
	  
	  @Override
	  protected void onUnhandledException(WebURL webUrl, Throwable e) {
	        String urlStr = (webUrl == null ? "NULL" : webUrl.getURL());
	        logger.warn("Unhandled exception while fetching {}: {}", urlStr, e.getMessage());
	        errorWriter.writeData(urlStr.replace(",", "-")+",UnhandledExceptionError");
	        
	    }
	  @Override
	  protected void onUnexpectedStatusCode(String urlStr, int statusCode, String contentType,
	          String description) {
		  logger.warn("onUnexpectedStatusCode: URL: {}, StatusCode: {}, {}, {}", urlStr, statusCode, contentType, description);
		  errorWriter.writeData(urlStr.replace(",", "-")+",UnexpectedStatusCode"+","+ statusCode+","+ contentType+","+ description);
		  
	  }
	  @Override
	    /**
	     * This function is called if there has been an error in parsing the content.
	     *
	     * @param webUrl URL which failed on parsing
	     */
	    protected void onParseError(WebURL webUrl) {
	        logger.warn("Parsing error of: {}", webUrl.getURL());
	        errorWriter.writeData(webUrl.getURL().replace(",", "-")+",ParseError");
	    }
	  
	  

		
	  

	  @Override
	  protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		  //fetch.csv=the URLs it attempts to fetch (<=20,000)
		  //System.out.println("Fetch "+webUrl.getURL()+","+statusCode);

		  fetchWriter.writeData(webUrl.getURL().replace(",", "-")+","+statusCode);
		  synchronized(Locker.lock)
		  {

			  attemptedFetch++;
			  //if(statusCode>=300 && statusCode < 400)
				//  abortedFetches++;
			  //if(statusCode >= 400)
				  //failedFetches++;
			  if(statusCode >= 300)
				  failedFetches++;
			  if(statusCode >=200 && statusCode < 300)
				  successfulFetch++;
			  
			  if(statusCodeList.get(statusCode)  == null) {
				  statusCodeList.put(statusCode, 1);
			  	  statusDescriptionList.put(statusCode, statusDescription);
			  }
			  else
				  statusCodeList.put(statusCode, statusCodeList.get(statusCode)+1);
				  
		  }
	  }
}
	  /*
	  private void processPage(WebURL curURL) {
	        PageFetchResult fetchResult = null;
	        Page page = new Page(curURL);
	        try {
	            if (curURL == null) {
	                return;
	            }

	            fetchResult = pageFetcher.fetchPage(curURL);
	            int statusCode = fetchResult.getStatusCode();
	            handlePageStatusCode(curURL, statusCode,
	                                 EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode,
	                                                                               Locale.ENGLISH));
	            // Finds the status reason for all known statuses

	            page.setFetchResponseHeaders(fetchResult.getResponseHeaders());
	            page.setStatusCode(statusCode);
	            if (statusCode < 200 ||
	                statusCode > 299) { // Not 2XX: 2XX status codes indicate success
	                if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
	                    statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
	                    statusCode == HttpStatus.SC_MULTIPLE_CHOICES ||
	                    statusCode == HttpStatus.SC_SEE_OTHER ||
	                    statusCode == HttpStatus.SC_TEMPORARY_REDIRECT ||
	                    statusCode == 308) { // is 3xx  todo
	                    // follow https://issues.apache.org/jira/browse/HTTPCORE-389

	                    page.setRedirect(true);

	                    String movedToUrl = fetchResult.getMovedToUrl();
	                    if (movedToUrl == null) {
	                        logger.warn("Unexpected error, URL: {} is redirected to NOTHING",
	                                    curURL);
	                        return;
	                    }
	                    page.setRedirectedToUrl(movedToUrl);
	                    onRedirectedStatusCode(page);

	                    if (myController.getConfig().isFollowRedirects()) {
	                        int newDocId = docIdServer.getDocId(movedToUrl);
	                        if (newDocId > 0) {
	                            logger.debug("Redirect page: {} is already seen", curURL);
	                            return;
	                        }

	                        WebURL webURL = new WebURL();
	                        webURL.setURL(movedToUrl);
	                        webURL.setParentDocid(curURL.getParentDocid());
	                        webURL.setParentUrl(curURL.getParentUrl());
	                        webURL.setDepth(curURL.getDepth());
	                        webURL.setDocid(-1);
	                        webURL.setAnchor(curURL.getAnchor());
	                        if (shouldVisit(page, webURL)) {
	                            if (!shouldFollowLinksIn(webURL) || robotstxtServer.allows(webURL)) {
	                                webURL.setDocid(docIdServer.getNewDocID(movedToUrl));
	                                frontier.schedule(webURL);
	                            } else {
	                                logger.debug(
	                                    "Not visiting: {} as per the server's \"robots.txt\" policy",
	                                    webURL.getURL());
	                            }
	                        } else {
	                            logger.debug("Not visiting: {} as per your \"shouldVisit\" policy",
	                                         webURL.getURL());
	                        }
	                    }
	                } else { // All other http codes other than 3xx & 200
	                    String description =
	                        EnglishReasonPhraseCatalog.INSTANCE.getReason(fetchResult.getStatusCode(),
	                                                                      Locale.ENGLISH); // Finds
	                    // the status reason for all known statuses
	                    String contentType = fetchResult.getEntity() == null ? "" :
	                                         fetchResult.getEntity().getContentType() == null ? "" :
	                                         fetchResult.getEntity().getContentType().getValue();
	                    onUnexpectedStatusCode(curURL.getURL(), fetchResult.getStatusCode(),
	                                           contentType, description);
	                }

	            } else { // if status code is 200
	                if (!curURL.getURL().equals(fetchResult.getFetchedUrl())) {
	                    if (docIdServer.isSeenBefore(fetchResult.getFetchedUrl())) {
	                        logger.debug("Redirect page: {} has already been seen", curURL);
	                        return;
	                    }
	                    curURL.setURL(fetchResult.getFetchedUrl());
	                    curURL.setDocid(docIdServer.getNewDocID(fetchResult.getFetchedUrl()));
	                }

	                if (!fetchResult.fetchContent(page,
	                                              myController.getConfig().getMaxDownloadSize())) {
	                	logger.
	                    throw new ContentFetchException();
	                }

	                if (page.isTruncated()) {
	                    logger.warn(
	                        "Warning: unknown page size exceeded max-download-size, truncated to: " +
	                        "({}), at URL: {}",
	                        myController.getConfig().getMaxDownloadSize(), curURL.getURL());
	                }

	                parser.parse(page, curURL.getURL());

	                if (shouldFollowLinksIn(page.getWebURL())) {
	                    ParseData parseData = page.getParseData();
	                    List<WebURL> toSchedule = new ArrayList<>();
	                    int maxCrawlDepth = myController.getConfig().getMaxDepthOfCrawling();
	                    for (WebURL webURL : parseData.getOutgoingUrls()) {
	                        webURL.setParentDocid(curURL.getDocid());
	                        webURL.setParentUrl(curURL.getURL());
	                        int newdocid = docIdServer.getDocId(webURL.getURL());
	                        if (newdocid > 0) {
	                            // This is not the first time that this Url is visited. So, we set the
	                            // depth to a negative number.
	                            webURL.setDepth((short) -1);
	                            webURL.setDocid(newdocid);
	                        } else {
	                            webURL.setDocid(-1);
	                            webURL.setDepth((short) (curURL.getDepth() + 1));
	                            if ((maxCrawlDepth == -1) || (curURL.getDepth() < maxCrawlDepth)) {
	                                if (shouldVisit(page, webURL)) {
	                                    if (robotstxtServer.allows(webURL)) {
	                                        webURL.setDocid(docIdServer.getNewDocID(webURL.getURL()));
	                                        toSchedule.add(webURL);
	                                    } else {
	                                        logger.debug(
	                                            "Not visiting: {} as per the server's \"robots.txt\" " +
	                                            "policy", webURL.getURL());
	                                    }
	                                } else {
	                                    logger.debug(
	                                        "Not visiting: {} as per your \"shouldVisit\" policy",
	                                        webURL.getURL());
	                                }
	                            }
	                        }
	                    }
	                    frontier.scheduleAll(toSchedule);
	                } else {
	                    logger.debug("Not looking for links in page {}, "
	                                 + "as per your \"shouldFollowLinksInPage\" policy",
	                                 page.getWebURL().getURL());
	                }

	                boolean noIndex = myController.getConfig().isRespectNoIndex() &&
	                    page.getContentType() != null &&
	                    page.getContentType().contains("html") &&
	                    ((HtmlParseData)page.getParseData())
	                        .getMetaTagValue("robots").
	                        contains("noindex");

	                if (!noIndex) {
	                    visit(page);
	                }
	            }
	        } catch (PageBiggerThanMaxSizeException e) {
	            onPageBiggerThanMaxSize(curURL.getURL(), e.getPageSize());
	        } catch (ParseException pe) {
	            onParseError(curURL);
	        } catch (ContentFetchException | SocketTimeoutException cfe) {
	        
	            onContentFetchError(page);
	        } catch (NotAllowedContentException nace) {
	            logger.debug(
	                "Skipping: {} as it contains binary content which you configured not to crawl",
	                curURL.getURL());
	        } catch (Exception e) {
	            onUnhandledException(curURL, e);
	        } finally {
	            if (fetchResult != null) {
	                fetchResult.discardContentIfNotConsumed();
	            }
	        }
	    }
	  
}
		  
	
/**
* This function is called before processing of the page's URL
* It can be overridden by subclasses for tweaking of the url before processing it.
* For example, http://abc.com/def?a=123 - http://abc.com/def
*
* @param curURL current URL which can be tweaked before processing
* @return tweaked WebURL
*/
/*
	  @Override
	  protected WebURL handleUrlBeforeProcess(WebURL curURL) {
		  
		  
	  
	        return curURL;
	  }
	  */
	  