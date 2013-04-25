package emenda.kwjlib;

import java.util.*;
import java.io.*;
import java.util.regex.*;

public class KWWebAPIService {
	//Properties are used to store the settings of the KW project
	private Properties m_kw_properties;
	private KWConnection m_kw_connection;
	private static String m_s_error = "";
	private static String m_s_lastrequest = "";
	
	/*
	 * Constructor which takes parameters and stores them in the properties object
	 */
	public KWWebAPIService(String host,
						   String port) {
		m_kw_properties = new Properties();
		m_kw_properties.setProperty("kwwebapihost", host);
		m_kw_properties.setProperty("kwwebapiport", port);
		m_kw_properties.setProperty("kwwebapiuser", "");
	}
	
	/*
	 * Constructor which takes parameters and stores them in the properties object
	 * Also takes user if specifying this is a requirement
	 */
	public KWWebAPIService(String host,
						   String port,
						   String user) {
		m_kw_properties = new Properties();
		m_kw_properties.setProperty("kwwebapihost", host);
		m_kw_properties.setProperty("kwwebapiport", port);
		m_kw_properties.setProperty("kwwebapiuser", user);
	}
	
	/*
	 * Function to create a connection object and connect to the KW Web API
	 */
	public boolean connect() {
		m_kw_connection = new KWConnection(m_kw_properties.getProperty("kwwebapihost"), 
				Integer.valueOf(m_kw_properties.getProperty("kwwebapiport")),
				m_kw_properties.getProperty("kwwebapiuser"));
		if(!m_kw_connection.successfulInitialisation()) {
			KWWebAPIService.appendError("public boolean connect():\n" +
					"\tInitialisation of KWConnection object was unsuccessful.\n");
			return false;
		}
		
		//Attempt to connect
		return m_kw_connection.connect();

	}
	

	
	/*
	 * Function to send request to server
	 * Supports error handling
	 */
	public KWJSONRecord[] sendRequest(String request) {
		//Send the request to the KW server
		//Start by clearing the old response
		m_kw_connection.clearResponse();
		if(m_kw_connection.sendRequest(request, 
					m_kw_properties.getProperty("kwwebapiuser"))) {
			//Successful, so parse the results into a KWIssue array
			m_s_lastrequest = m_kw_connection.getLastRequest();
			return parseServerResponse(m_kw_connection.getResponse());
		}
		KWWebAPIService.appendError("public KWJSONRecord[] sendRequest(String s_projectname):\n" +
				"\tError sending request to server while sending following request:\n" +
				"\t" + request);
		return null;
	}
	
	/*
	 * Function to parse the server response into a KWJSONRecord array
	 */
	public KWJSONRecord[] parseServerResponse(String response) {
		//First we must determine how many records have been returned
		Matcher m = Pattern.compile("\n|\r").matcher(response);
		int kwrecord_count = 1;
		int index = 0;
		while (m.find())
		{
			kwrecord_count++;
		}

		KWJSONRecord[] kwrecords = new KWJSONRecord[kwrecord_count];
		//Use a buffered reader to read one line at a time
		InputStream instream = new ByteArrayInputStream(response.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
		
		String currentline = "";
		try {
			while ((currentline = reader.readLine()) != null) {
				kwrecords[index] = new KWJSONRecord(currentline);
				index++;
			}
		}
		catch(IOException e) {
			KWWebAPIService.appendError("public KWJSONRecord[] parseServerResponse(String response):\n" +
					"\tError parsing response from server: " + "\n" +
					"\t" + response +
					"\tException: " + e.getMessage());
			return null;
		}
		
		
		return kwrecords;
	}
	
	
	/*
	 * Function to retrieve error message
	 */
	public static String getError() {
		return m_s_error;
	}
	
	/*
	 * Function to append to static private error string
	 */
	public static void appendError(String errorMsg) {
		m_s_error = m_s_error + "\n" + errorMsg;
	}
	
	/*
	 * Accessor function for m_s_lastrequest
	 */
	public String getLastRequest() {
		return m_s_lastrequest;
	}
		
	/*
	 * builds
	 * Retrieve the list of builds for a project.
	 * Example: curl --data "action=builds&user=myself&project=my_project" http://localhost:8080/review/api
	*/
	public KWJSONRecord[] builds(String project_name) {
		String request = "project=" + project_name +
			 	 "&user=" + KWUtil.user +
			 	 "&action=" + "builds";
		return sendRequest(request);
	}

	/*
	 * create_module
	 * Create a module for a project.
	 * Example: curl --data "action=create_module&user=myself&project=my_project&name=test&allow_all=true&paths="**//*"" http://localhost:8080/review/api
 	*/
	public KWJSONRecord[] create_module(
					String project_name,
					String module_name,
					String allow_all,
					String allow_users,
					String allow_groups,
					String deny_users,
					String deny_groups,
					String paths,
					String tags
				 ) {
		String request = "project=" + project_name +
			 	 "&user=" + KWUtil.user +
			 	 "&action=" + "create_module" + 
			 	 "&name=" + module_name +
			 	 "&allow_all=" + allow_all +
			 	 ((allow_users != "") ? ("&allow_users=" + allow_users) : "") +
			 	 ((allow_groups != "") ? ("&allow_groups=" + allow_groups) : "") +
			 	 ((deny_users != "") ? ("&deny_users=" + deny_users) : "") +
			 	 ((deny_groups != "") ? ("&deny_groups=" + deny_groups) : "") +
			 	 "&paths=" + paths +
			 	 "&tags=" + tags;
		return sendRequest(request);
	}

	/*
	 * create_view
	 * Create a view for a project.
	 * Example: curl --data "action=create_view&user=myself&project=my_project&name=critical&query=severity:1-3" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] create_view(
					String project_name,
					String view_name,
					String query,
					String tags,
					String is_public
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "create_view" + 
				"&name=" + view_name +
				"&query=" + query +
				"&tags=" + tags +
				"&is_public=" + is_public;
		return sendRequest(request);
	}

	/*
	 * delete_build
	 * Delete a build.
	 * Example: curl --data "action=delete_build&user=myself&project=my_project&name=build_1" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] delete_build(
					String project_name,
					String build_name
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "delete_build" + 
				"&name=" + build_name;
		return sendRequest(request);
	}
	
	/*
	 * delete_module
	 * Delete a module.
	 * Example: curl --data "action=delete_module&user=myself&project=my_project&name=my_module" http://localhost:8080/review/api
	 */

	public KWJSONRecord[] delete_module(
					String project_name,
					String module_name
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "delete_module" + 
				"&name=" + module_name;
		return sendRequest(request);
	}

	/*
	 * delete_project
	 * Delete a project.
	 * Example: curl --data "action=delete_project&user=myself&name=my_project" http://localhost:8080/review/api
	*/
	public KWJSONRecord[] delete_project(
					String project_name
				 ) {
		String request = "name=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "delete_project";
		return sendRequest(request);
	}

	/*
	 * delete_view
	 * Delete a view.
	 * Example: curl --data "action=delete_view&user=myself&project=my_project&name=my_view" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] delete_view(
					String project_name,
					String view_name
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "delete_view" +
				"&name=" + view_name;
		return sendRequest(request);
	}	

	/*
	 * fchurns
	 * Generate file churns report.
	 * Example: curl --data "action=fchurns&user=myself&component=Component" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] fchurns(
					String project_name,
					String view_name,
					String view_creator_name,
					String no_of_builds_to_show,
					String root_component
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "fchurns" +
				"&view=" + view_name +
				"&viewCreator=" + view_creator_name +
				"&latestBuilds=" + no_of_builds_to_show +
				"&component=" + root_component;
		return sendRequest(request);
	}

	/*
	 * modules
	 * Retrieve the list of modules for a project.
	 * Example: curl --data "action=modules&user=myself&project=my_project" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] modules(
					String project_name
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "modules";
		return sendRequest(request);
	}

	/*
	 * projects
	 * Retrieve list of projects.
	 * Example: curl --data "action=projects&user=myself&" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] projects() {
		String request = "user=" + m_kw_properties.getProperty("kwwebapiuser") +
				"&action=" + "projects";
		return sendRequest(request);
	}
	
	/*
	 * report
	 * Generate build summary report.
	 * Example: curl --data "action=report&user=myself&project=my_project&build=build_1&x=Category&y=Component" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] report(
					String project_name,
					String build_name,
					String filter_query,
					String view_name,
					String x_axis_value,
					String x_axis_drilldown,
					String y_axis_value,
					String y_axis_drilldown
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "report" +
				"&build=" + build_name +
				((filter_query != "") ? ("&filterQuery=" + filter_query) : "") +
				((view_name != "") ? ("&view=" + view_name) : "") +
				"&x=" + x_axis_value +
				((x_axis_drilldown != "") ? ("&xDrilldown=" + x_axis_drilldown) : "") +
				"&y=" + y_axis_value +
				((y_axis_drilldown != "") ? ("&yDrilldown=" + y_axis_drilldown) : "");
		return sendRequest(request);
	}

	/*
	 * search
	 * Retrieve the list of detected issues.
	 * Example: curl --data "action=search&user=myself&project=my_project&query='file:MyFile.c'" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] search(
					String project_name, 
					String query
					) {
		String request = "project=" + project_name +
			 	 "&user=" + KWUtil.user +
			 	 "&action=" + "search" +
			 	 "&query=" + query;
		return sendRequest(request);
	}	
	
	/*
	 * update_build
	 * Update a build.
	 * Example: curl --data "action=update_build&user=myself&name=build_1&new_name=build_03_11_2011" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] update_build(
					String project_name, 
					String build_name,
					String new_build_name,
					String keepit
					) {
		String request = "project=" + project_name +
			 	 "&user=" + KWUtil.user +
			 	 "&action=" + "update_build" +
			 	 "&name=" + build_name +
			 	 ((new_build_name != "") ? ("&new_name=" + new_build_name) : "") +
			 	 ((keepit != "") ? ("&keepit=" + keepit) : "");
		return sendRequest(request);
	}

	/*
	 * update_module
	 * Update a module for a project.
	 * Example: curl --data "action=update_module&user=myself&project=my_project&name=test&new_name=aux&allow_all=true&paths="**//*,**//*"" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] update_module(
					String project_name,
					String module_name,
					String new_module_name,
					String allow_all,
					String allow_users,
					String allow_groups,
					String deny_users,
					String deny_groups,
					String paths,
					String tags
				 ) {
		String request = "project=" + project_name +
		"&user=" + KWUtil.user +
		"&action=" + "update_module" + 
		"&name=" + module_name +
		((new_module_name != "") ? ("&new_name=" + new_module_name) : "") +
		((allow_all != "") ? ("&allow_all=" + allow_all) : "") +
		((allow_users != "") ? ("&allow_users=" + allow_users) : "") +
		((allow_groups != "") ? ("&allow_groups=" + allow_groups) : "") +
		((deny_users != "") ? ("&deny_users=" + deny_users) : "") +
		((deny_groups != "") ? ("&deny_groups=" + deny_groups) : "") +
		((paths != "") ? ("&paths=" + paths) : "") +
		((tags != "") ? ("&tags=" + tags) : "");
		return sendRequest(request);
	}

	/*
	 * update_project
	 * Update a project.
	 * Example: curl --data "action=update_project&user=myself&name=myproject&new_name=my_project" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] update_project(
					String project_name,
					String new_project_name,
					String new_project_description,
					String tags
				 ) {
		String request = "name=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "update_project" +
				((new_project_name != "") ? ("&new_name=" + new_project_name ) : "") +
				((new_project_description != "") ? ("&description=" + new_project_description ) : "") +
				((tags != "") ? ("&tags=" + tags) : "");
		return sendRequest(request);
	}

	/*
	 * update_view
	 * Update a view.
	 * Example: curl --data "action=update_view&user=myself&project=my_project&tags=c,security&name=my_view" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] update_view(
					String project_name,
					String view_name,
					String new_view_name,
					String query,
					String tags,
					String is_public
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "update_view" +
				"&name=" + view_name +
				((new_view_name != "") ? ("&new_name=" + new_view_name ) : "") +
				((query != "") ? ("&query=" + query ) : "") +
				((tags != "") ? ("&tags=" + tags) : "") +
				((is_public != "") ? ("&is_public=" + is_public ) : "");
		return sendRequest(request);
	}

	/*
	 * views
	 * Retrieve list of views.
	 * Example: curl --data "action=views&user=myself&project=my_project" http://localhost:8080/review/api
	 */
	public KWJSONRecord[] views(
					String project_name
				 ) {
		String request = "project=" + project_name +
				"&user=" + KWUtil.user +
				"&action=" + "views";
		return sendRequest(request);
	}
	
	
}