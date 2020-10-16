//TODO: revise logic
//TODO: validate urls
//TODO: refactor
//TODO: test linux
//TODO: README file***4


/**
 * Author: Youssef Beltagy, The Chicken Lord
 * Submission Date: 10/16/2020
 * Description: A web crawler that jumps through numHops pages from
 * the starting page and prints the last visited page.
 * 
 * Implemented for Progrom 1 of CSS 436 in AUT 2020 at UWB.
 */

import java.util.regex.*;           // Used for HTML parsing
import java.util.*;                 // ArrayList and stack
import java.net.URI;                // Needed for HTTP calls
import java.net.http.HttpClient;    // Needed for HTTP calls
import java.net.http.HttpRequest;   // Needed for HTTP calls
import java.net.http.HttpResponse;  // Needed for HTTP calls
import org.apache.commons.validator.routines.UrlValidator;


public class Crawler {

    // Regex patterns for looking for http links.
    private static final Pattern anchorPattern = Pattern.compile("<a[^>]+?href=[\"']?([\"'>]+)[\"']?[^>]*?>(.+?)</a>",  Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    private static final Pattern linkPattern = Pattern.compile("href=[\"'](http|https)://.*?[\"']",  Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
    private static final Pattern httpPattern = Pattern.compile("(http|https)://.+?",  Pattern.CASE_INSENSITIVE|Pattern.DOTALL);

    String[] schemes = {"http","https"};
    UrlValidator urlValidator = new UrlValidator(schemes);


    /**
     * Represents a webpage
     */
    public static class Page
    {
        String url = null;
        int curHop = 0;
        int attempt500 = 0;

        /**
         * Represents a webpage
         * @param url the url of the page
         * @param curHop number of hops from start page
         */
        public Page(String url, int curHop){
            this.url = url;
            this.curHop = curHop;
            attempt500 = 0;
        }
    }


    /**
     * Reads the program arguments and validates them.
     * 
     * calls the crawl method.
     * @param args
     */
    public static void main(String[] args)
    {

        if (args.length != 2){
            System.err.println("Usage: java Crawler <url> <numHops>");
            System.exit(-1);
        }

        int numHops = 0;
        String startingURL = args[0];

        try {
            numHops = Integer.parseInt(args[1]);
        } catch (final NumberFormatException e) {
            System.err.println("numHops must be a number");
            System.exit(-1);
        }

        if(numHops < 0){
            System.err.println("numHops must be bigger than or equal to 0");
            System.exit(-1);
        }

        // The crawl method handles the crawling logic.
        String toPrint = crawl(startingURL, numHops);

        // Print the last page if it can be printed.
        if(toPrint != null){
            System.out.println("\n\nThe last page is printed below:     ------------");
            System.out.println(toPrint);
        }else{
            System.out.println("Can't print response.");
        }

    }

    /**
     * Given an html page as a string, this method 
     * parses the string to get http and https urls in
     * anchor tag.
     * One of the function's limitatins is that it assumes
     * the closing tag of the anchor will always be </a> without
     * line breaks or spaces.
     * @param htmlPage the html page as a string
     * @return An arrayList of strings representing the links
     */
    public static ArrayList<String> getURLS(String htmlPage)
    {

        // Regex Matcher
        Matcher anchorMatcher = anchorPattern.matcher(htmlPage);
        
        ArrayList<String> links = new ArrayList<>();

        // If there is an anchor tag
        while(anchorMatcher.find()){

            Matcher linkMatcher = linkPattern.matcher(anchorMatcher.group());

            // If it has an http or https link
            if(linkMatcher.find()){
                String link = linkMatcher.group();
                link = link.substring(6, link.length()-1);// remove href=" and the trailing "
                links.add(link);
            }
            
        }


        return links;
    }

    /**
     * Given an httpclient and a url, This function sends a GET request and receives
     * its response.
     * @param client the http client
     * @param url the page url
     * @return an HTTPResponse
     */
    public static HttpResponse<String> getPage(HttpClient client, String url)
    {

        // create request
        HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

        try{

            // send the request
            return client.send(request,
                HttpResponse.BodyHandlers.ofString());

            
        }catch(Exception e){

            System.out.println("Error in making GET request");

        }

        return null;
    }

    /**
     * Stacks the links inside urls into st. Sets the hop of the links as curHop.
     * The stack is filled by the end of the list so that it can be popped in
     * the same order as the order of links in the html page.
     * @param st
     * @param urls
     * @param curHop
     */
    public static void stackURLS(Stack<Page> st, List<String> urls, int curHop){

        for(int i = urls.size() -1; i >= 0; i--){
            st.push(new Page(urls.get(i), curHop));
        }

    }

    /**
     * Prints the url of the resource and the status of the response.
     * Uses the curHop of the resource to indent the output.
     * @param page contains information about the page.
     * @param response the httpresponse
     */
    public static void printURL(Page page, HttpResponse<String> response){
        for(int i = 0; i < page.curHop; i++){
            System.out.print("|");
        }
        
        System.out.println("Status: " + response.statusCode() + "| URL:" + response.uri());
    }

    /**
     * Implements the logic of crawling through the web. Downloads web pages
     * and uses regex to search for anchor tags in the pages. Then goes through
     * those pages until the function has made numHops or there are no more valid urls.
     * 
     * Uses a stack of pages and a while loop to backtrack if a deadend is reached.
     * 
     * When adding new pages,
     * If the response is 1xx, no new pages are added.
     * If the response is 2xx, the body of the response is scanned using getURLS
     * If the response is 3xx, the redirection location is checked for the presence of (http|https) and used.
     * If the response is 4xx, no new pages are added.
     * If the response is 5xx, the current page is added again and its attempt500 is incremented to declare that 
     *      a connection attempt has already happend. After a total of three connection attempts, the url is discarded.
     * 
     * For the last page,
     * In case of a 1xx response, the header of httpresponse is printed.
     * In case of a 2xx response, the body is printed.
     * In case of a 3xx response, the header is printed.
     * In case of a 4xx response, the header is printed.
     * In case of a 5xx response, the header is printed.
     * 
     * @param startingURL The starting url
     * @param numHops The allowed number of hops
     * @return The string to be printed
     */
    public static String crawl(String startingURL, int numHops){
        HttpClient client = HttpClient.newHttpClient();

        Stack<Page> st = new Stack<>();
        st.push(new Page(startingURL, 0));

        Page curResource = null;
        String toPrint = null;

        HashSet<String> visitedSet = new HashSet<>();

        while(!st.isEmpty()){
            curResource = st.peek();
            st.pop();
            
            // If the page has alread been visited,
            // then ignore it.
            if(visitedSet.contains(curResource.url)){
                continue;
            }

            visitedSet.add(curResource.url);

            // Will retain the last visited html even if the stack had a few dublicates after it.
            toPrint = null;
            
            // Makes the Get request.
            HttpResponse<String> response = getPage(client, curResource.url);

            // If there was a problem in establishing an http connection,
            // ignore the url. getPage should have alread printed an error message.
            if(response == null){
                continue;
            }
            
            // Print the url in fancy way.
            printURL(curResource, response);

            if(response.statusCode() < 100){
    
                System.out.println("Status code is smaller than 1xx: " + response.statusCode());
                
            }else if (response.statusCode() < 200){
                // informational 1xx
                // TODO: Is this finished

                toPrint = response.headers().toString();
            }else if (response.statusCode() < 300){
                // success 2xx

                // parse the html;

                String htmlPage = response.body();
                toPrint = htmlPage;

                // finish the program if numHops has been reached.
                if(curResource.curHop >= numHops) break;

                // Parse and stack URLS
                ArrayList<String> urls = getURLS(htmlPage);
                if(!urls.isEmpty()){
                    stackURLS(st, urls, curResource.curHop + 1);
                }else{
                    System.out.println("Can't find links");
                    break;
                }

            }else if (response.statusCode() < 400){
                //redirect 3xx 
                // Get the new location from the url.

                toPrint = response.headers().toString();
                List<String> location = response.headers().map().get("location");

                // If there really is a new location and it is an absolute (http|https) URL,
                // then add the url to the stack.
                if(location != null && httpPattern.matcher(location.get(0)).find()){
                    st.push(new Page(location.get(0), curResource.curHop));
                }

            }else if (response.statusCode() < 500){
                // client error 4xx
                // The url will be removed from the stack by defualt.
                // Nothing needs to be done here.
                toPrint = response.headers().toString();

            }else if (response.statusCode() < 600){
                // server error 5xx

                //Visit the url two more times with increasing delay.

                if(curResource.attempt500 < 2){
                    //Visit the link again
                    st.push(curResource);
                    visitedSet.remove(curResource.url);
                    try{
                        Thread.sleep(100 + (long)curResource.attempt500*200);
                    }catch(Exception e){
                        System.out.println("Couldn't sleep");
                    }
                }

                toPrint = response.headers().toString();

                curResource.attempt500++;

            }else {
                System.out.println("Status Code bigger than 5xx: " + response.statusCode());
            }
        }

        return toPrint;
    }

}