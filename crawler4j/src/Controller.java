.import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Set;



//import org.apache.commons.httpclient.HttpStatus;
//import org.apache.http.HttpStatus;

public class Controller {

		public static void main(String[] args) {
			int maxDepthofCrawling = 16;
			int maxPagesToFetch = 20000;
			int politenessDelay = 100;
			String userAgentString = "Homework";
			int numberOfCrawlers = 10;
			int maxDownloadSize = 10485760; // 1048576
			CrawlConfig config = new CrawlConfig();
			 
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd_HH-mm");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String ts=sdf.format(timestamp);
			String crawlStorageFolder = "crawlData_from_bbc"+ts;
			config.setCrawlStorageFolder(crawlStorageFolder);
			 
			 config.setMaxDepthOfCrawling(maxDepthofCrawling);
			 config.setMaxPagesToFetch(maxPagesToFetch);
			 config.setPolitenessDelay(politenessDelay);
			 config.setIncludeBinaryContentInCrawling(true);
			 //config.setProcessBinaryContentInCrawling(true);
			 config.setIncludeHttpsPages(true);
			 config.setMaxDownloadSize(maxDownloadSize);
			 //config.setResumableCrawling(true);
			 config.setUserAgentString(userAgentString);
			 /*
			 * Instantiate the controller for this crawl.
			 */
			 PageFetcher pageFetcher = new PageFetcher(config);
			 RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
			 RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
			 CrawlController controller = null;
				try {
					controller = new CrawlController(config, pageFetcher, robotstxtServer);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//}
			 /*
			 * For each crawl, you need to add some seed urls. These are the first
			 * URLs that are fetched and then the crawler starts following links
			 * which are found in these pages
			 */
			controller.addSeed("https://www.bbc.com/news");
			controller.addSeed("https://www.bbc.com/news/world/us_and_canada");
			/*controller.addSeed("https://www.bbc.com/news");
			controller.addSeed("https://www.nytimes.com/section/technology");
			controller.addSeed("https://www.nytimes.com/section/science");
			controller.addSeed("https://www.nytimes.com/section/health");
			controller.addSeed("https://www.nytimes.com/section/business");
			controller.addSeed("https://www.nytimes.com/section/fashion");
			controller.addSeed("https://www.nytimes.com/section/world");
			controller.addSeed("https://www.nytimes.com/section/us");
			controller.addSeed("https://www.nytimes.com/section/politics");
			*/
				
			 /*
			 * Start the crawl. This is a blocking operation, meaning that your code
			 * will reach the line after this only when crawling is finished.
			 */
			 controller.start(myCrawler.class, numberOfCrawlers);
			 
			 /** Missing from original but in example
			 List<Object> crawlersLocalData = controller.getCrawlersLocalData();
		        long totalLinks = 0;
		        long totalTextSize = 0;
		        int totalProcessedPages = 0;
		        for (Object localData : crawlersLocalData) {
		            CrawlStat stat = (CrawlStat) localData;
		            totalLinks += stat.getTotalLinks();
		            totalTextSize += stat.getTotalTextSize();
		            totalProcessedPages += stat.getTotalProcessedPages();
		            */
			 
			 //SUMMARY
			 System.out.println("fetches attempted:"+myCrawler.attemptedFetch);
			 System.out.println("fetches succeeded:"+myCrawler.successfulFetch);
			 //System.out.println("fetches aborted:"+myCrawler.abortedFetches);
			 System.out.println("fetches failed:"+myCrawler.failedFetches);
			 System.out.println("Total URLs extracted:"+myCrawler.totalUrlList.size());
			 System.out.println("unique URLs extracted:"+myCrawler.uniqueUrlList.size());
			 System.out.println("unique URLs within news site:"+myCrawler.uniqueUrlList_OK.size());
			 System.out.println("unique URLs outside news site:"+myCrawler.uniqueUrlList_NOK.size());
			 //System.out.println("unique URLs outside USC:"+(myCrawler.uniqueUrlList.size()- myCrawler.uniqueURLs_OK.size()));
			 System.out.println("Status Codes" + myCrawler.statusCodeList);
			 System.out.println("File Size" + myCrawler.sizeTypeMap);
			 System.out.println("Content Type" + myCrawler.contentTypeList);
			 
			File crawlSummary= new File("CrawlReport_bbc.txt");
			 
			BufferedWriter summarize;
			try {
				summarize = new BufferedWriter(new FileWriter(crawlStorageFolder+"/"+ crawlSummary));
				summarize.write("Name: Kaixi Wang");
				summarize.newLine();
				summarize.write("USC ID: 3986695328");
				summarize.newLine();
				summarize.write("News site crawled: bbc.com/news");
				summarize.newLine();
				summarize.write("Fetch Statistics \n ================");
				summarize.newLine();
				summarize.write("# fetches attempted: " +myCrawler.attemptedFetch);
				summarize.newLine();
				summarize.write("# fetches succeeded: "+myCrawler.successfulFetch);
				summarize.newLine();
				//summarize.write("# fetches aborted: "+myCrawler.abortedFetches);
				//summarize.newLine();
				summarize.write("# fetches failed: "+myCrawler.failedFetches);
				summarize.newLine();
				summarize.write("Outgoing URLs: \n ==============");
				summarize.newLine();
				summarize.write("Total URLs extracted: "+myCrawler.totalUrlList.size());
				summarize.newLine();
				summarize.write("# unique URLs extracted: "+myCrawler.uniqueUrlList.size());
				summarize.newLine();
				summarize.write("# unique URLs within nytimes: " + myCrawler.uniqueUrlList_OK.size());
				summarize.newLine();
				summarize.write("# unique URLs outside nytimes:"+myCrawler.uniqueUrlList_NOK.size());
				summarize.newLine();
				summarize.write("Status Codes: \n =============");
				summarize.newLine();
				//summarize.write("Code Counts: "+myCrawler.statusCodeList);
				//summarize.write("Code Description"+myCrawler.statusDescriptionList);
				Set<Integer> hcode = myCrawler.statusCodeList.keySet();
				for(Integer h:hcode){
					summarize.write(""+h+" "+ myCrawler.statusDescriptionList.get(h)+": "+myCrawler.statusCodeList.get(h));
					summarize.newLine();
				}
				summarize.newLine();
				summarize.write("File Sizes: \n" + 
						"==============");
				summarize.newLine();
				//summarize.write("Sizes Count: "+myCrawler.sizeTypeMap);
				Set<String> sizeKeys = myCrawler.sizeTypeMap.keySet();
				for(String sk:sizeKeys){
					summarize.write(sk+": "+myCrawler.sizeTypeMap.get(sk));
					summarize.newLine();
				}
				summarize.write("Content Types: \n" + 
						"==============");
				summarize.newLine();
				//summarize.write("Type Count"+myCrawler.contentTypeList);
				Set<String> contentKeys = myCrawler.contentTypeList.keySet();
				for(String ck:contentKeys){
					summarize.write(ck+": "+myCrawler.contentTypeList.get(ck));
					summarize.newLine();
				}
				summarize.close();
				 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
