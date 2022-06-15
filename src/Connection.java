import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.nio.file.Files;
import java.util.Date;
import java.util.StringTokenizer;

public class Connection extends Thread {
	Socket socket;
	PrintWriter print_writer;
	BufferedReader buffered_reader;
	SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",java.util.Locale.ENGLISH);
	String filename;
	String response;
	String source = "C:\\Users\\Sky\\IdeaProjects\\untitled\\htdocs";

	// constructor
	Connection(Socket socket) throws Exception {
		this.socket = socket;
		InputStreamReader input_stream_reader = new InputStreamReader(this.socket.getInputStream());
		buffered_reader = new BufferedReader(input_stream_reader);
		print_writer = new PrintWriter(this.socket.getOutputStream());
	}

	public void run() {
		try {
			String request_frame = "";
			while(buffered_reader.ready() || request_frame.length() == 0) {
				request_frame = request_frame + (char)buffered_reader.read();
			}

			// print the timestamp and client ip of each request
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.print("\nTime: " + sdf.format(timestamp) + " | " + "IP: " + socket.getInetAddress().getHostAddress() + " | Connection established | ");

			HTTPResponse(request_frame);

			print_writer.write(response.toCharArray());
			print_writer.close();
			buffered_reader.close();
			socket.close();	

		} catch(Exception e) {}
	}

	// respond the http request from the client
	public void HTTPResponse(String request) throws IOException {
		String ifModSince = null;
		Date ifModifiedSince = null;
		Date lastModifiedDate;
		String CRLF = "\r\n";
		String lines[] = request.split("\n"); // get the request line from the request header
		filename = lines[0].split(" ")[1]; // get the requested file name

		// get the "If-Modified-Since" from the request header if it contains
		for(int i = 0; lines[i].length() > 1; i++) {
			if(lines[i].startsWith("If-Modified-Since")) {
				int index = lines[i].indexOf(':');
				ifModSince = lines[i].substring(index + 2);
			}
		}

		// if the client does not request any file, redirect to index.html
		if(lines[0].split(" ")[1].endsWith("/")){
			filename = "/index.html";
		}

		// output the requested file name to log
		String logFilename = filename.split("/")[1];
		System.out.print("Requested file Name: " + logFilename);

		File file = new File(source + filename);
		DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

		// tokenize the string to separate the 3 components of the request line
		StringTokenizer tokens = new StringTokenizer(lines[0]);

		// HTTP 400 Bad Request response
		if(tokens.countTokens() != 3 || !lines[0].split(" ")[2].endsWith("HTTP/1.1\r")){
			System.out.println(" | Respond type: 400 Bad Request");
			// send the response header to client
			outputStream.writeBytes("HTTP/1.1 400 Bad Request" + CRLF);
			outputStream.writeBytes("Content-Type: text/html" + CRLF);
			outputStream.writeBytes("Server: SimpleWebServer/1.0" + CRLF);
			outputStream.writeBytes("Connection: keep-alive" + CRLF);
			outputStream.writeBytes(CRLF);
			// send the response html to client
			String http400Response =
					"<!DOCTYPE html>\n" +
					"<html>\n" +
					"<head>\n" +
					"    <title>400 Bad Request</title>\n" +
					"</head>\n" +
					"<body>\n" +
					"<h1>Error 400: Bad Request</h1>\n" +
					"<p>Your browser sent a request that this server could not understand.<br /></p>\n" +
					"</body>\n" +
					"</html>";
			outputStream.write(http400Response.getBytes("UTF-8"));
		}
		// HTTP 404 file not found response
		else if (!file.exists()) {
			System.out.println(" | Respond type: 404 File Not Found");
			// send the response header to client
			outputStream.writeBytes("HTTP/1.1 404 Not Found" + CRLF);
			outputStream.writeBytes("Content-Type: text/html" + CRLF);
			outputStream.writeBytes("Server: SimpleWebServer/1.0" + CRLF);
			outputStream.writeBytes("Connection: keep-alive" + CRLF);
			outputStream.writeBytes(CRLF);
			// send the response html to client
			String http404Response =
					"<!DOCTYPE html>\n" +
					"<html>\n" +
					"<head>\n" +
					"    <title>404 Not Found</title>\n" +
					"</head>\n" +
					"<body>\n" +
					"<h1>Error 404: File Not Found</h1>\n" +
					"<p>The requested URL was not found on this server.<br /></p>\n" +
					"</body>\n" +
					"</html>";
			outputStream.write(http404Response.getBytes("UTF-8"));
		}
		// handle Last-Modified field and If-Modified-Since header
		else if(ifModSince != null) {
			// convert string to date
			try {
				ifModifiedSince = sdf.parse(ifModSince);
			}
			catch(ParseException e) {
				e.printStackTrace();
			}

			// get the last modified date of the file
			lastModifiedDate = new Date(file.lastModified());
			String lastMod = sdf.format(lastModifiedDate);
			lastModifiedDate = new Date();

			// convert string to date
			try {
				lastModifiedDate = sdf.parse(lastMod);
			}
			catch (ParseException e) {
				e.printStackTrace();
			}

			// if the last modified date is before the "if-modified-since" date,
			// there is no need to retransmit the requested resources
			if((ifModifiedSince != null) && (lastModifiedDate.compareTo(ifModifiedSince) <= 0))
			{
				// send the response header to client
				System.out.println(" | Respond type: 304 Not Modified");
				outputStream.writeBytes("HTTP/1.1 304 Not Modified" + CRLF);
				outputStream.writeBytes("Date: " + sdf.format(new Date()) + CRLF);
				outputStream.writeBytes("Server: SimpleWebServer/1.0" + CRLF);
				outputStream.writeBytes("Last-Modified: " + lastModifiedDate + CRLF);
				outputStream.writeBytes("Content-Length: " + (int)file.length() + CRLF);
				outputStream.writeBytes(CRLF);
			}
			// HTTP 200 OK response
			else {
				System.out.println(" | Respond type: 200 OK");
				// read data from the file
				FileInputStream fileStream = new FileInputStream(file);
				// Get file type of the requested file
				String contentType = Files.probeContentType(file.toPath());

				BufferedInputStream bufInputStream = new BufferedInputStream(fileStream);
				byte[] bytes = new byte[(int) file.length()];

				// send the response header to client
				outputStream.writeBytes("HTTP/1.1 200 OK"+CRLF);
				outputStream.writeBytes("Content-Type: " + contentType +CRLF);
				outputStream.writeBytes("Date: " + sdf.format(new Date())+CRLF);
				outputStream.writeBytes("Server: SimpleWebServer/1.0"+CRLF);
				outputStream.writeBytes("Connection: keep-alive"+CRLF);
				outputStream.writeBytes("Last-Modified: " + sdf.format(file.lastModified())+CRLF);
				outputStream.writeBytes("Content-Length: " + file.length() +CRLF);
				outputStream.writeBytes(CRLF);

				bufInputStream.read(bytes);
				// send the data of the requested file from the bytes array to client and flush the output stream
				outputStream.write(bytes);
				outputStream.flush();

				bufInputStream.close();
			}
		}
		// HTTP 200 OK response
		else {
			System.out.println(" | Respond type: 200 OK");
			// read data from the file
			FileInputStream fileStream = new FileInputStream(file);
			// Get file type of the requested file
			String contentType = Files.probeContentType(file.toPath());

			BufferedInputStream bufInputStream = new BufferedInputStream(fileStream);
			byte[] bytes = new byte[(int) file.length()];

			// send the response header to client
			outputStream.writeBytes("HTTP/1.1 200 OK"+CRLF);
			outputStream.writeBytes("Content-Type: " + contentType +CRLF);
			outputStream.writeBytes("Date: " + sdf.format(new Date())+CRLF);
			outputStream.writeBytes("Server: SimpleWebServer/1.0"+CRLF);
			outputStream.writeBytes("Connection: keep-alive"+CRLF);
			outputStream.writeBytes("Last-Modified: " + sdf.format(file.lastModified())+CRLF);
			outputStream.writeBytes("Content-Length: " + file.length() +CRLF);
			outputStream.writeBytes(CRLF);

			bufInputStream.read(bytes);
			// send the data of the requested file from the bytes array to client and flush the output stream
			outputStream.write(bytes);
			outputStream.flush();

			bufInputStream.close();
		}
		outputStream.close();
	}
}
