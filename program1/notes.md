# Notes

For now, I will focus on just printing the urls I visit. 

I can approach this problem with recursion or a stack.

To avoid revisitng a url twice, I can use an unordered set.

First, let's implement the program in Java. I can learn C# if I have time.

My first goal is to parse one html page and get all links in it.


## Questions

How to handle relative urls? You don't. Just focus on absolute ones.

Should I print the whole html or just that I visited it? For every html or just the last one? Just print the last onel.

What about <link> tags? Don't worry about it.

What about misformatted tags? You can't do much about it.

What about misformatted url? That would give me 404, so you would treat it as a 404 error.

How to handle redirections? Print the 3xx code only, or print code and go forward through the request? Print that there was a redirect and do the redirection myself.

Ask recommended set of tools. He won't say that.

http version 1.1/2.0? Which exactly?

500, sleep and try again. Try thrice with exponential backof.





## Programming Approach

1. Get the HTML. httpclient or url http://zetcode.com/java/readwebpage/

2. Parse the HTML. htmlunit, htmlcleaner, htmlParser, or regular expression https://stackoverflow.com/questions/5120171/extract-links-from-a-web-page

After that, it is tree traversal. I will use Depth first search. But I have two approaches. I can use recursion or a stack. My concern with recursion is that the program might become difficult to debug. I will use a stack.


## Resources

https://www.w3schools.com/tags/att_a_href.asp

https://www.tutorialspoint.com/javaexamples/extract_content_from_html.htm

http://zetcode.com/java/readwebpage/

http://htmlcleaner.sourceforge.net/index.php

https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html

https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpRequest.html

https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.html

https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html

http://tutorials.jenkov.com/java-regex/index.html

https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html

https://docs.oracle.com/javase/tutorial/essential/regex/quant.html

https://sc1.checkpoint.com/documents/R76/CP_R76_IPS_AdminGuide/12885.htm#:~:text=Various%20metacharacters%20indicate%20how%20many,be%20the%20expression's%20first%20character.&text=a%20single%20character-,a,a%20sub%2Dpattern%20in%20parentheses

http://httpstat.us/


## Test URLS

http://courses.washington.edu/css502/dimpsey/

http://oracle.com

http://ybeltagy.com

http://httpstat.us/

http://google.com