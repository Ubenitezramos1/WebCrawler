//Import necessary libraries
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Define the WebCrawler class
public class WebCrawler {

    //Set to store visited URLs
    private Set<String> visitedUrls = new HashSet<>();
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    //Method to start crawling from a given URL and return all found links
    public Set<String> getLinks(String initialUrl) {
        Set<String> links = new HashSet<>();
        crawl(initialUrl, links);
        return links;
    }

    //Recursive method to crawl a given URL and extract links
    private void crawl(String url, Set<String> links) {
        //If the URL has been visited before, return
        if (visitedUrls.contains(url)) {
            return;
        }

        //Print the URL being crawled
        System.out.println("Crawling: " + url);

        try {
            //Connect to the URL and parse the HTML document
            Document document = Jsoup.connect(url).get();

            // Extract links from the page
            Elements pageLinks = document.select("a[href]");
            for (Element pageLink : pageLinks) {
                //Resolve relative URLs to absolute URLs
                String nextUrl = resolveUrl(url, pageLink.attr("href"));
                //If the next URL has not been visited and is different from the current URL, add it to the set
                if (!visitedUrls.contains(nextUrl) && !nextUrl.equals(url)) {
                    links.add(nextUrl);
                    // Submit the next URL for crawling in a separate thread
                    submitTaskForCrawling(nextUrl, links);
                }
            }
            //Mark the current URL as visited
            visitedUrls.add(url);
        } catch (HttpStatusException httpStatusException) {
            // Handle HTTP status exceptions (e.g., 403 Forbidden)
            System.err.println("HTTP error crawling: " + url + ". Status=" + httpStatusException.getStatusCode());
            // You can choose to log the error or take other appropriate actions
        } catch (IOException e) {
            //Handle IO exceptions while crawling
            System.err.println("Error crawling: " + url);
            e.printStackTrace();
        }
    }

    //Method to resolve a relative URL against a base URL
    private String resolveUrl(String baseUrl, String link) {
        try {
            //Create URL objects for the base and resolved URLs
            URL base = new URL(baseUrl);
            URL resolved = new URL(base, link);
            return resolved.toString();
        } catch (MalformedURLException e) {
            // Handle malformed URLs
            System.err.println("Malformed URL: " + baseUrl + ", " + link);
            return link; // Return the link as is if there's an issue with resolving
        }
    }

    //Method to submit a URL for crawling in a seperate thread
    private void submitTaskForCrawling(String url, Set<String> links) {
        //Submit a task to crawl the URL in a seperate thread
        executorService.submit(() -> crawl(url, links));
    }

    //Main method to start the web crawler
   public static void main(String[] args) {
    //Use a try-with-resources statement to automatically close the scanner
    try (Scanner scan = new Scanner(System.in)) {
        //Prompt the user to enter the initial URL to start crawling
        System.out.println("Enter a Url to start crawling: ");
        //Read the user input
        String initialUrl = scan.nextLine();
        //Remove whitespaces from the input URL
        initialUrl = initialUrl.replaceAll("\\s+", "");

        //Create a WebCrawler instance
        WebCrawler webCrawler = new WebCrawler();
        //Get all URls by crawling the initial  URL
        Set<String> allUrls =  new HashSet<>(webCrawler.getLinks(initialUrl));

        // Shutdown the executor service after all tasks are completed
        webCrawler.awaitTermination();

        //Print all found URLs
        System.out.println("All URLs found:");
        for (String url : allUrls) {
            System.out.println(url);
        }
    }
}

 // Method to wait for all tasks to complete before shutting down the executor service
private void awaitTermination() {
    try {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, java.util.concurrent.TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

	
}

