package WebCrawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WebCrawler {

    private Set<String> visitedUrls = new HashSet<>();

    public Set<String> getLinks(String initialUrl) {
        Set<String> links = new HashSet<>();
        crawl(initialUrl, links);
        return links;
    }

    private void crawl(String url, Set<String> links) {
        if (visitedUrls.contains(url)) {
            return;
        }

        System.out.println("Crawling: " + url);

        try {
            Document document = Jsoup.connect(url).get();

            // Extract links from the page
            Elements pageLinks = document.select("a[href]");
            for (Element pageLink : pageLinks) {
                String nextUrl = resolveUrl(url, pageLink.attr("href"));
                if (!visitedUrls.contains(nextUrl) && !nextUrl.equals(url)) {
                    links.add(nextUrl);
                    // Submit the next URL for crawling in a separate thread
                    submitTaskForCrawling(nextUrl, links);
                }
            }

            visitedUrls.add(url);
        } catch (IOException e) {
            System.err.println("Error crawling: " + url);
            e.printStackTrace();
        }
    }

    private String resolveUrl(String baseUrl, String link) {
        try {
            URL base = new URL(baseUrl);
            URL resolved = new URL(base, link);
            return resolved.toString();
        } catch (MalformedURLException e) {
            // Handle malformed URLs
            System.err.println("Malformed URL: " + baseUrl + ", " + link);
            return link; // Return the link as is if there's an issue with resolving
        }
    }

    private void submitTaskForCrawling(String url, Set<String> links) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        executorService.submit(() -> crawl(url, links));
        executorService.shutdown();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java MultiThreadedWebCrawler <initialUrl>");
            System.exit(1);
        }

        String initialUrl = args[0];

        WebCrawler webCrawler = new WebCrawler();
        Set<String> allUrls = webCrawler.getLinks(initialUrl);

        System.out.println("All URLs found:");
        for (String url : allUrls) {
            System.out.println(url);
        }
    }

	public Set<String> startCrawling(String initialUrl) {
		// TODO Auto-generated method stub
		return null;
	}
}

