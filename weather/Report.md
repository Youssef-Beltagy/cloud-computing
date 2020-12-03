# Program 2

This is a Command Line program that tells you the weather of a city and the number of Covid-19 cases in the country of the city.



## Build and Run

I implemented and tested this program in Java 15 on a Windows machine. I also tested it on a Linux machine with Java 14.

I use a jar file for the org.json library. You need to include it in the path of the program.

To build:

```
javac -cp json.jar Weather.java
```

To run:

```
java -cp json.jar Weather.java <city> <optional: country>
```

To run with cities that have spaces in their names:

```
java -cp json.jar Weather.java "new york" <optional: country>
```



## Design Choices

I couldn't find an API that tells the number of Covid-19 cases per city and using something like US Census would restrict the program to the US. To make the program global, I had to sacrifice details and get information about the country as a whole.

To get the weather, I used [https://api.openweathermap.org](https://openweathermap.org/current). This API returns the weather and a two character country code. I use the two character country code to get Covid-19 information from [http://corona-api.com](https://about-corona.net/documentation).

I'm using a pipeline. The user inputs a city name. The city name is passed to the weather API. The weather API returns a country code (ISO 3166-1 alpha-2 format of the country). The country code is passed to the Covid-19 API.

When I get 5xx or 429 as the return code of my http request, I use exponential backoff for my retries.



## Challenges

The weather API is robust and easy to use. But the Covid-19 API was chaotic. Sometimes it would return null; others it would return values. To overcome this issue, I made a method that checks for and handles null cases by printing "No Information." Furthermore, all of the Json Parsing is done in a try-catch block to ensure the program will fail beautifully even if the APIs return invalid formats.

To handle cities like "New York" with spaces in them, I replace spaces with "%20". I replace apostrophes with "%27". But I don't handle other non-alphabet characters because that would complicate the program beyond the extent of this application.



## Limitations

The program can't handle cities with non-alphabet characters in their names (except for spaces and apostrophes).

If you search for Seattle in Egypt by passing "seattle egypt" as program arguments, you will still get Seattle in the US and US Covid-19 information. So if the weather API finds the city, then regardless of whether the country is the correct country or not, the information for that city is printed out (along with the information of its actual country).

Because there is no Graphical Interface and different countries can have cities with the same name, the output can be confusing. Use the optional country parameter to narrow down your search.



## Output

![](D:\Desktop\CSS 436\Programs\program2\output.PNG)