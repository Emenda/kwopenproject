package emenda.kwopenproject.remote;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class KlocworkCaller {
	IKlocworkWorker stub;
	private String errorMessage;
	
	public KlocworkCaller() {
		errorMessage = "";
	}
	
	/**
	 * Creates a new Klocwork project on the server
	 * @param projectName - The name to be given to the new project
	 * @return true if successful, else false
	 */
	public boolean createProject(String url,
			 					 String projectName,
			 					 String encoding) {
		//Check if we have a server stub
		if(stub == null) {
			logError("Stub has not been initialised, have you connected to the server?");
			return false;
		}
		
		try {
			//Attempt to create project and report error upon failure
			if(stub.createProject(url, projectName, encoding)) {
				return true;
			}
			else {
				logError("Error on server while creating Klocwork project with name: " + projectName);
				logError("Error reported by server: " + stub.getError());
				return false;
			}
		}
		catch(Exception e) {
			logError("Exception while creating project: " + e.getMessage());
			logError(e.getStackTrace().toString());
			return false;
		}
	}
	
	/**
	 * Creates a new Klocwork project on the server
	 * @param projectName - The name to be given to the new project
	 * @return true if successful, else false
	 */
	public boolean duplicateProject(String url,
								 	String origProjectName,
								 	String newProjectName,
								 	String encoding) {
		//Check if we have a server stub
		if(stub == null) {
			logError("Stub has not been initialised, have you connected to the server?");
			return false;
		}
		
		try {
			//Attempt to create project and report error upon failure
			if(stub.duplicateProject(url, origProjectName, newProjectName, encoding)) {
				return true;
			}
			else {
				logError("Error on server while duplicating Klocwork project " + origProjectName + " to create new project with name: " + newProjectName);
				logError("Error reported by server: " + stub.getError());
				return false;
			}
		}
		catch(Exception e) {
			logError("Exception while creating project: " + e.getMessage());
			logError(e.getStackTrace().toString());
			return false;
		}
	}
	
	public boolean pullFromMKS(String mksHost, String mksProject, String mksIteration) {
		return false;
	}
	public boolean doBuild(String rootDir) {
		return false;
	}
	public boolean runAnalysis(String kwtablesPath, String buildspecPath) {
		return false;
	}
	public boolean uploadResults(String projectName, String resultsPath) {
		return false;
	} 
	
	/**
	 * Attempts to fetch the stub for the worker instance from the RMI registry
	 * @param host - The host on which the server is listening for connections
	 * @param port - The port on which the server is listening for connections
	 * @return true if successful, else false
	 */
	public boolean connectToServer(String host, int port) {
		try {
			Registry registry = LocateRegistry.getRegistry(host, port);
			if(registry == null) {
				logError("Error connecting to registry on port: " + port);
				logError("Please check port number and that server service is running.");
				return false;
			}
			
			stub = (IKlocworkWorker) registry.lookup(IKlocworkWorker.stubName);
			if(stub == null) {
				logError("Error looking up KlocworkWorker stub with name: " + IKlocworkWorker.stubName);
				logError("Please ensure service is running and listening on port: " + port);
				return false;
			}
			
			return !stub.hasErrors();
		}
		catch(Exception e) {
			logError("Exception while connecting to server: " + e.getMessage());
			logError(e.getStackTrace().toString());
			return false;
		}
	}
	
	public String getError() {
		return errorMessage;
	}
	
	public String getServerError() {
		//Check if we have a server stub
		if(stub == null) {
			logError("Stub has not been initialised, have you connected to the server?");
			return "Server not connected, error could not be retrieved.";
		}
		
		try {
			return stub.getError();
		}
		catch(Exception e) {
			logError("Exception while fetching server error: " + e.getMessage());
			logError(e.getStackTrace().toString());
			return "!Exception while fetching server error string!";
		}
	}
	
	private void logError(String error) {
		errorMessage = errorMessage + error + "\n";
	}
}
