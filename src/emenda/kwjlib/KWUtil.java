package emenda.kwjlib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

public class KWUtil {
	
	public static String user = "NOT SET. ltoken has not been validated.";

	/*
	 * Function to retrieve user's ltoken from file (created by kwauth)
	 * This token is used to confirm authentication with the KW web API
	 */
	public static String fetchLToken(String a_s_host,
							   int a_n_port,
							   String a_s_user) {
		String s_ltoken_file_line = "";
		String s_ltoken = null;
		//The ltoken is stored by Klocwork in the user's home directory
		//under a hidden folder called .klocwork.
		String s_user_home = System.getProperty("user.home");
		//Operating system will determine which path format we need to use
		File f_ltoken_file;
		if(System.getProperty("os.name").toLowerCase().startsWith("windows")) {
			//Windows path format
			f_ltoken_file = new File(s_user_home + "\\.klocwork\\ltoken");
		}
		else {
			//Unix/Linux path format
			f_ltoken_file = new File(s_user_home + "/.klocwork/ltoken");
		}
		
		if(!(f_ltoken_file.exists())){
			KWWebAPIService.appendError("public boolean fetchLToken(String a_s_host, int a_n_port, String a_s_user)\n" +
					"\tltoken file does not exist:\n" +
					"\t" + f_ltoken_file.getAbsolutePath());
			return null;
		}
		
		//Try to read from the file
		try {
			FileReader filereader = new FileReader(f_ltoken_file);
			BufferedReader reader = new BufferedReader(filereader);
			
			//Each line in the token file represents one token entry
			//Process each one until we reach EOF or find a match
			s_ltoken_file_line = reader.readLine();
			while(s_ltoken_file_line != null) {
				s_ltoken = validateLToken(s_ltoken_file_line, a_s_host, a_n_port, a_s_user);
				if(s_ltoken != null) {
					break;
				}
				s_ltoken_file_line = reader.readLine();
			}
			
			//Check if we found a match, report error if not
			if(s_ltoken == null) {
				KWWebAPIService.appendError("public boolean fetchLToken(String a_s_host, int a_n_port, String a_s_user)\n" +
						"\tNo matching token entry was found in ltoken file. Please ensure parameters for connection\n" +
						"\tare correct, and that you are authorised (using kwauth).");
				return null;
			}
			
			//Close the readers
			reader.close();
			filereader.close();
		}
		catch(IOException e) {
			KWWebAPIService.appendError("public boolean fetchLToken(String a_s_host, int a_n_port, String a_s_user)\n" +
					"\tError reading from ltoken file:\n" +
					"\tFile: " + f_ltoken_file.getAbsolutePath() + "\n" +
					"\tError: " + e.getMessage());
			return null;
		}
		
		return s_ltoken;
	}
	
	/*
	 * Function to validate an ltoken
	 * Checks:
	 * 1. Token entry contains 4 elements (host, port, user, ltoken)
	 * 2. Host, port and user (optional) match request
	 */
	public static String validateLToken(String a_ltoken_string,
								  String a_s_host,
								  int a_n_port,
								  String a_s_user) {
		//Split the string into delimited tokens
		String s_ltokens[];
		String s_kwserver_ltoken = "";
		s_ltokens = a_ltoken_string.split(";");
		//Check we have enough tokens
		if(s_ltokens.length < 4) {
			return null;
		}
		//Check the token port agrees with request port
		if(s_ltokens[1].equals(Integer.toString(a_n_port))) {
			//Check user
			if(s_ltokens[2].equals(a_s_user) || a_s_user.equals("")) {
				//Now check host
				if(s_ltokens[0].equals(a_s_host) ||
				   verifyURLEquivalence(s_ltokens[0], a_s_host)) {
					//Set ltoken and user
					s_kwserver_ltoken = s_ltokens[3];
					user = s_ltokens[2];
					return s_kwserver_ltoken;
				}
			}
		}
		
		return null;		
	}
	
	/*
	 * Function to determine if two URLs are equivalent by resolving their absolute addresses
	 */
	public static boolean verifyURLEquivalence(String a_s_url1, String a_s_url2) {
		try {
			InetAddress ip1 = null;
			InetAddress ip2 = null;
			
			//First we consider the case of "localhost" or other loopback addresses
			//These must be resolved manually
			if(a_s_url1.toLowerCase().startsWith("localhost") || 
			   a_s_url1.toLowerCase().startsWith("127.") ) {
				ip1 = InetAddress.getLocalHost();
			}
			if(a_s_url2.toLowerCase().startsWith("localhost") || 
			   a_s_url2.toLowerCase().startsWith("127.") ) {
				ip2 = InetAddress.getLocalHost();
			}
			
			//Set any address variables not initialised by getting the address by name
			if(ip1 == null) {
				ip1 = InetAddress.getByName(a_s_url1);
			}
			if(ip2 == null) {
				ip2 = InetAddress.getByName(a_s_url2);
			}
			
			//Comparison now made using addresses, rather than names
			if(ip1.getHostAddress().equals(ip2.getHostAddress())) {
				return true;
			}
		}
		catch(Exception e) {
			KWWebAPIService.appendError("public boolean verifyURLEquivalence(String a_s_url1, String a_s_url2):\n" +
					"\tError resolving URLs to determine equivalence when verifying ltoken value.\n" +
					"\tURL1: " + a_s_url1 +
					"\tURL2: " + a_s_url2 +
					"\tException message: " + e.getMessage());
			return false;
		}
		return false;
	}
	
}
