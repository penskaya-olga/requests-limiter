# requests-limiter
Create spring-boot application which contains  a controller with one method. 
The method must return empty string with  HTTP status 200. 
Create functionality which would be limit requests count on this method for IP address to 50 requests per minute. 
If requests count is more 50 then the method must returns an error with code 502 until the number of requests 
for the last minute will not be less than 50.  
Solution should reusable and must be applied quickly to new methods and not only to controllers, but also to services. 
Implementation must consider a multi-threaded high-loaded execution environment and consume as few resources as possible. 
Project must build by maven command "mvn clean package" and start up by command "java -jar test-1.jar". 
Application port must be 8080. Use java 8 and maven 3.
