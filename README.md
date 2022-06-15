# Simple Web Server
An assingment of COMP2322 Computer Networking in 2021

The web server uses Java.\
The src folder is the source code file of the web server.\
The htdocs is the test files for the web server to host.


The default server port is 8080. You can edit the port number in WebServer.java.\
PLEASE edit the path of the htdocs folder in the source code according to your environmnet.
You can edit it in the line 17 of Connection.java.


After running the server, you can enter 127.0.0.1:8080 to visit the web server.
It should show a simple web page (index.html) which is about AI and an image.


The log.txt contains the log of the web server. It should be saved in the root of the project folder.
It should look like this:
```
--------Web Server has started--------
Enter "127.0.0.1:8080" in your web browser.


Time: Wed Mar 31 23:36:29 HKT 2021 | IP: 127.0.0.1 | Connection established | Requested file Name: index.html | Respond type: 200 OK

Time: Wed Mar 31 23:36:29 HKT 2021 | IP: 127.0.0.1 | Connection established | Requested file Name: imagemap.png | Respond type: 200 OK
```
