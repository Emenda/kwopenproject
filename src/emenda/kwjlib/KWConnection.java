package emenda.kwjlib;

import java.lang.String;
import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
/*
 * Class KW_Connection represents a connection (including authorization) to
 * a Klocwork server and thereby the Web API. This class can be used to: 
 * 	-	establish the connection between the plug-in and the Klocwork server
 * 	-	send requests in the form of POST HTTP
 * 	-	receive results in the form of JSON records
 */

public class KWConnection {
	//Member variables
	private URL m_kwserver_url;
	private HttpURLConnection m_kwserver_connection;
	private String m_kwserver_ltoken; //Authorization token
	private String m_kwserver_response;
	private String m_s_lastrequest;
	private boolean m_successful_init = false;
	
	/*
	 * Default constructor
	 */
	public KWConnection() {
		//Construct with default values
		KW_Connection_init("localhost", 8080, "");
	}
	
	/*
	 * Argument constructors
	 */
	public KWConnection(String a_KWserver_url, int a_KWserver_port, String a_KWserver_user) {
		KW_Connection_init(a_KWserver_url, a_KWserver_port, a_KWserver_user);
	}
	
	//Initialise function called by constructors
	private void KW_Connection_init(String a_KWserver_url, int a_KWserver_port, String a_KWserver_user) {
		//Set member variable values and test connection to server
		try {
			m_kwserver_url = new URL("http", a_KWserver_url, a_KWserver_port, "/review/api");
		}
		catch (IOException e) {
			KWWebAPIService.appendError("private void KW_Connection_init(String a_KWserver_url, int a_KWserver_port):\n" +
					"\tError initialising connection object; could not create URL object:\n" +
					"\t" + e.getMessage());
			m_successful_init = false;
		}
		
		m_kwserver_connection = null;
		m_kwserver_ltoken = "";
		m_kwserver_response = "";
		m_s_lastrequest = "";
		m_successful_init = getLToken(m_kwserver_url.getHost(), m_kwserver_url.getPort(), a_KWserver_user);
	}
	
	/*
	 * Function to connect to server using member variables URL and connection
	 */
	public boolean connect() {
		try {
			m_kwserver_connection = (HttpURLConnection) m_kwserver_url.openConnection();
			//Settings for the connection
			m_kwserver_connection.setDoOutput(true);
			m_kwserver_connection.setDoInput(true);
			m_kwserver_connection.setInstanceFollowRedirects(false);
			//Set the request method to POST (accepted by KW Web API)
			m_kwserver_connection.setRequestMethod("POST");
			m_kwserver_connection.setUseCaches (false);
		}
		catch(IOException e){
			KWWebAPIService.appendError("public boolean connect():\n" +
					"\tException occured while opening connection:\n" +
					"\t" + e.getMessage());
			m_kwserver_connection = null;
			return false;
		}
		
		return true;
	}
	
	//Disconnects the connection
	public void disconnect() {
		if(m_kwserver_connection != null) {
			m_kwserver_connection.disconnect();
		}
		
		m_kwserver_connection = null;
	}
	
	public boolean isConnected() {
		return (m_kwserver_connection != null);
	}
	
	//Function to send a POST HTTP request to server
	//Argument a_s_request is sent as POST HTTP to server
	//Server response (result) is stored in a_s_response
	//Return value indicates function success or failure
	public boolean sendRequest(String a_request,
							   String a_user) {
		String request = a_request;
		
		//Connect if not connected
		if(!isConnected()) {
			if(!connect()) {
				//Connection failed
				KWWebAPIService.appendError("public boolean sendRequest(String a_project, String a_user, ...):\n" +
						"\tConnection was not open when attempting to send request, and connecting failed.");
				return false;
			}
		}
		
		//Fetch ltoken if we do not have it
		if(m_kwserver_ltoken == null || 
				m_kwserver_ltoken.length() < 1) {
			//Check if token is fetched successfully
			if(!getLToken(m_kwserver_url.getHost(),
							m_kwserver_url.getPort(),
							a_user)) {
				KWWebAPIService.appendError("public boolean sendRequest(String a_project, String a_user, ...):\n" +
						"\tFetching ltoken failed.");
				disconnect();
				return false;
			}
		}
		
		//Append the ltoken value to the request
		request = request.concat("&ltoken=" + m_kwserver_ltoken);
		
		//Set the request parameters of the connection (the length of the request string)
		m_kwserver_connection.setRequestProperty("Content-Length", Integer.toString(request.length()));
		
		//Write the request to the connection
		try {
			DataOutputStream wr = new DataOutputStream(m_kwserver_connection.getOutputStream());
			System.out.println("OutputStream open, writing request: " + request);
			//Store the request as the last sent request
			m_s_lastrequest = request;
			wr.writeBytes(request);
			//Close the streams
			wr.flush();
			wr.close();
			//Read response from server
			InputStream instream = m_kwserver_connection.getInputStream();
			BufferedReader buf = new BufferedReader(new InputStreamReader(instream));
			while(buf.ready()){
				m_kwserver_response = m_kwserver_response.concat(buf.readLine() + "\n");
			}
			//Close the streams
			buf.close();
			instream.close();
		}
		catch(IOException e) {
			System.out.println("Error writing to connection: " + e.getMessage());
			KWWebAPIService.appendError("public boolean sendRequest(String a_project, String a_user, ...):\n" +
					"\tError while writing to/reading from connection:\n" +
					"\t" + e.getMessage());
			disconnect();
			return false;
		}
		
		//Finish by disconnecting
		disconnect();
		
		return true;
	}

	
	public boolean getLToken(String host,
							 int port,
							 String user) {
		m_kwserver_ltoken = KWUtil.fetchLToken(host, port, user);
		return (m_kwserver_ltoken != null);
	}
	
	/*
	 * Accessor function for m_kwserver_response
	 */
	public String getResponse() {
		return m_kwserver_response;
	}
	
	/*
	 * Accessor function for m_successful_init
	 */
	public boolean successfulInitialisation() {
		return m_successful_init;
	}
	
	/*
	 * Clear function for m_kwserver_response
	 */
	public void clearResponse() {
		m_kwserver_response = "";
	}
	
	/*
	 * Accessor function for m_s_lastrequest
	 */
	public String getLastRequest() {
		return m_s_lastrequest;
	}
}
