import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Author: Youssef Beltagy (ybeltagy)
 * 
 * This is program 2 of Autumn 2020 CSS 436 (cloud computing) with Robert Dimpsey.
 * 
 * This program prints weather information about a city and prints
 * covid-19 statistics about the country the city is in.
 */

/**
 * The weather class gets the weather of a city and
 * reports the number of COVID-19 cases in the country of the city.
 */
public class Weather {

    //Represents possible data types in the Json response.
    enum ObjectType {
        STRING,
        INT,
        DOUBLE
    }

    // OpenWeahter API KEY.
    // FIXME: put you openweathermap.org API key here
    private static String apiKey = "";

    // HttpClient declared here for reuse
    // Follows redircts
    private static HttpClient client = 
        HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

    // For the exponential backoff.
    private static Random r = new Random(System.currentTimeMillis());

    // Number of attempts at getting a response before the program gives up.
    public static final int MAXRETRIES = 4; 

    // Number of spaces used for prettyprinting
    private static final int SPACES = 20;

    /**
     * Parses arguments and makes to API calls.
     * One API call is to get the weather.
     * The other API case to get Corona Virus information on
     * the country of the city.
     * @param args
     */
    public static void main(String[] args) {

        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage: java Weather <city> <optional: country> ");
            return;
        }

        String location = args[0];
        if (args.length > 1) {
            location += "," + args[1];
        }

        location = preprocessURL(location);

        String country = weatherAPI(location);
        if (country != null)
            coronaAPI(country);

    }


    /**
     * Prepocess the url to replace spaces with %20
     * and single quotes with %27 for 
     * all the cities like "new york"!
     * 
     * There might be other possible characters,
     * but the space should cover most cases.
     * @param input
     * @return a more valid url for the city name
     */
    private static String preprocessURL(String input){

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < input.length(); i++){
            switch(input.charAt(i)){
                case ' ':
                    sb.append("%20");
                    break;
                case '\'':
                    sb.append("%27");
                    break;
                default:
                    sb.append(input.charAt(i));
            }
        }

        return sb.toString();
    }

    /**
     * Prints a line in a pretty way.
     * 
     * @param key
     * @param val
     * @param unit
     */
    private static void prettyPrintLine(String key, String val, String unit){

        for(int i = 0; i < SPACES - key.length(); i++){
            System.out.print(" ");
        }

        System.out.println(key + ": " + val + " " + unit);

    }

    /**
     * Either prints a the information or prints "No information" if there was no information.
     * @param obj
     * @param key
     * @param printLegend
     * @param type
     * @param unit
     * @return whether there was information or not.
     */
    private static boolean printVal(JSONObject obj, String key, String printLegend, ObjectType type, String unit){

        if(!obj.isNull(key)){
            
            switch(type){
                case STRING:
                    prettyPrintLine(printLegend, obj.getString(key), unit);
                    break;
                case INT:
                    prettyPrintLine(printLegend, Integer.toString(obj.getInt(key)), unit);
                    break;
                case DOUBLE:
                    prettyPrintLine(printLegend, String.format("%.2f", obj.getDouble(key)), unit);
                    break;
            }

            return true;

        }else{

            prettyPrintLine(printLegend, "No Information", "");
            return false;
        }

    }

    /**
     * Gets the weather Information about a city using the api at
     * "https://api.openweathermap.org/data/2.5/weather?"
     * 
     * Prints the information in a pretty way.
     * 
     * @param location
     * @return ISO 3166-1 alpha-2 format of the country the city is in.
     */
    private static String weatherAPI(String location) {

        String url = "https://api.openweathermap.org/data/2.5/weather?" + "q=" + location + "&appid=" + apiKey
            + "&mode=" + "json" + "&units=" + "metric" + "&lang=" + "english";

        HttpResponse<String> response = makeHttpRequest(url);

        if(response == null) return null;

        try {
            JSONObject obj = new JSONObject(response.body());

            //Print city name
            String cityName = obj.getString("name");
            System.out.println("The weather in " + cityName);
            System.out.println();

            //Print Description of the weather
            //I'm guaranteed a weather array.
            JSONArray weatherDescription = obj.getJSONArray("weather");

            //Print the description of the weather
            if(weatherDescription.length() > 0)
                printVal(weatherDescription.getJSONObject(0),
                    "description", "Description", ObjectType.STRING, "");
            

            //I'm guaranteed there is a main object
            JSONObject statistics = obj.getJSONObject("main");
            printVal(statistics,"temp", "Temp", ObjectType.DOUBLE, "Celsius");
            printVal(statistics,"temp_min", "Min Temp", ObjectType.DOUBLE, "Celsius");
            printVal(statistics,"temp_max", "Max Temp", ObjectType.DOUBLE, "Celsius");
            printVal(statistics,"humidity", "Humidity", ObjectType.DOUBLE, "%");

            System.out.println();

            // I'm guaranteed a sys object and a country object.
            return obj.getJSONObject("sys").getString("country");
        } catch (Exception e) {

            System.out.println("Weather information is badly formatted");
            return null;
        }

    } 

    /**
     * Gets the latest Covid-19 information on the country of the city using
     * the api at "http://corona-api.com/countries/""
     * 
     * Prints the information in a pretty way.
     * 
     * @param country
     */
    private static void coronaAPI(String country){

        String url = "http://corona-api.com/countries/" + country;

        HttpResponse<String> response = makeHttpRequest(url);

        if(response == null) return;

        try {

            //I'm guaranteed there will be a "data" object
            JSONObject obj = new JSONObject(response.body());
            JSONObject data = obj.getJSONObject("data");

            //Country name in a human readable format
            String countryFullName = data.getString("name");
            System.out.println("Covid-19 Cases in " + countryFullName);
            System.out.println();

            printVal(data, "population", "Total Population", ObjectType.INT, "");
            printVal(data, "updated_at", "Updated At", ObjectType.STRING, "");

            //I'm guaranteed there will be a "latest data" object
            JSONObject latestData = data.getJSONObject("latest_data");
            printVal(latestData, "confirmed", "Total Confirmed", ObjectType.INT, "");
            printVal(latestData, "recovered", "Total Recovered", ObjectType.INT, "");
            printVal(latestData, "deaths", "Deaths", ObjectType.INT, "");

            //I'm guaranteed there will be a "calculated" object
            JSONObject calculated = latestData.getJSONObject("calculated");
            printVal(calculated, "recovery_rate", "Recovery Rate", ObjectType.DOUBLE, "%");
            printVal(calculated, "death_rate", "Death Rate", ObjectType.DOUBLE, "%");

            System.out.println();
            System.out.println();
            
        } catch (Exception e) {
            System.out.println("Covid-19 information is badly formatted");
        }

    }

    /**
     * Puts the thread to sleep for a 2^powOfTwo/4 to 2^powOfTwo/2 seconds.
     * @param powOfTwo
     */
    private static void exponentialBackof(int powOfTwo){
        System.out.println("Retrying...");
        int waitTime = (1 << powOfTwo)*250;
        waitTime += r.nextInt(waitTime);
        try {

            Thread.sleep(waitTime);// implicitly typecasted to a long

        } catch (Exception e) {

            System.out.println("Interrupted. Please rerun");
            System.exit(-1);
        }

    }

    /**
     * Validates the url.
     * 
     * @param url
     * @return an HttpRequest object if the url is valid, null otherwise.
     */
    private static HttpRequest validateAndReturnRequest(String url){

        try {
            return HttpRequest
                    .newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            System.out.print("Invalid URL");
        }

        return null;

    }

    /**
     * Makes an Http Call to a url address.
     * 
     * Validates the address before calling and handles retry logic if necessary.
     * 
     * @param url
     * @return an HttpResponse<String> if the Http call was successful, null otherwise.
     */
    private static HttpResponse<String> makeHttpRequest(String url){

        //validate url
        HttpRequest request = validateAndReturnRequest(url);

        if(request == null) return null;

        for (int i = 0; i < MAXRETRIES; i++) {

            HttpResponse<String> response = null;

            try{
                response = client.send(request, BodyHandlers.ofString());
            }catch(IOException ioe){
                System.out.println("Network Error");
                return null;
            }catch(InterruptedException ie){
                System.out.println("Program Interrupted. Please try again");
                return null;
            }
            

            if(response.statusCode() < 200){
                //1xx informational

                //It doesn't make sense to enter this logical branch, but just in case.

                System.out.println("Status Code: " + response.statusCode());

                return null;

            }
            else if (response.statusCode() < 300) {
                // Since I'm not 
                // 2xx Success
                return response;

            } else if (response.statusCode() < 500 && response.statusCode() != 429) {
                // 3xx redirects are handled by http client.
                // I also checked that I reach the API, so it wouldn't make sense
                // to get redirects.
                // This should be 4xx.

                System.out.println("Could Not Find Information on the City");

                return null;

            } else {

                // 429 server is overloaded.
                // 5xx server down
                // retry

                // If this was the last call, don't sleep.
                if (i == MAXRETRIES - 1){
                    System.out.println("Server is Down");
                    return null;
                }

                //sleep 2^i/4 to 2^i/2 seconds.
                exponentialBackof(i);

            }

        }

        return null;

    }
    
}
