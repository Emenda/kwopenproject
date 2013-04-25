package emenda.kwopenproject.remote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class KlocworkWorker implements IKlocworkWorker {
	private String errorMessage;
	private boolean errors;
	
	public KlocworkWorker() {
		errorMessage = "";
		errors = false;
	}
	
	/**
	 * Uses kwadmin create-project to create a project
	 */
	public boolean createProject(String url,
								 String projectName,
								 String encoding) {
		boolean result = true;
		try {
			String urlval = (url != null && url.length() > 0) ? " --url " + url : "";
			String encodingval = (encoding != null && encoding.length() > 0) ? " --encoding " + encoding : "";
			Process p = Runtime.getRuntime().exec("kwadmin" + urlval + " create-project " + projectName + encodingval);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			while(line!=null) {
				result = false;
				logError(line);
				line = reader.readLine();
			}
			return result;
		}
		catch(Exception e) {
			logError("Exception while creating project: " + e.getMessage());
			logError(e.getStackTrace().toString());
			return false;
		}
	}
	
	/**
	 * Uses kwadmin duplicate-project to duplicate a project
	 */
	public boolean duplicateProject(String url,
								 	String origProjectName,
								 	String newProjectName,
								 	String encoding) {
		boolean result = true;
		String command = "No value assigned";
		try {
			String urlval = (url != null && url.length() > 0) ? " --url " + url : "";
			String encodingval = (encoding != null && encoding.length() > 0) ? " --encoding " + encoding : "";
			command = "kwadmin" + urlval + " duplicate-project " + origProjectName + " " + newProjectName + encodingval;
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			while(line!=null) {
				result = false;
				logError(line);
				line = reader.readLine();
			}
			return result;
		}
		catch(Exception e) {
			logError("Exception while duplicating project: " + e.getMessage());
			logError("Command sent: " + command);
			logError(e.getStackTrace().toString());
			return false;
		}
	}
	
	/**
	 * Performs a pull from MKS
	 */
	public boolean pullFromMKS(String mksHost, String mksProject, String mksIteration) {
		return false;
	}
	
	/**
	 * Performs a build using TD4
	 */
	public boolean doBuild(String rootDir) {
		return false;
	}
	
	/**
	 * Performs a Klocwork analysis using kwbuildproject
	 */
	public boolean runAnalysis(String kwtablesPath,
							   String compilerOptions,
							   String linkerOptions,
							   String encoding,
							   String url,
							   boolean force,
							   String buildspecPath) {
		boolean result = true;
		try {
			String compilerOptionsVal = (compilerOptions != null && compilerOptions.length() > 0) ? " --add-compiler-options  " + compilerOptions : ""; 
			String linkerOptionsVal = (linkerOptions != null && linkerOptions.length() > 0) ? " --add-linker-options " + linkerOptions : "";
			String encodingVal = (encoding != null && encoding.length() > 0) ? " --encoding " + encoding : "";
			String urlVal = (url != null && url.length() > 0) ? " --url " + url : "";
			String forceVal = force ? " --force " : " ";
			Process p = Runtime.getRuntime().exec("kwbuildproject --tables-directory " + kwtablesPath 
												+ compilerOptionsVal
												+ linkerOptionsVal
												+ encodingVal
												+ urlVal
												+ forceVal
												+ buildspecPath);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			while(line!=null) {
				logError(line);
				line = reader.readLine();
			}
			return result;
		}
		catch(Exception e) {
			logError("Exception while creating project: " + e.getMessage());
			logError(e.getStackTrace().toString());
			return false;
		}
	}
	
	/**
	 * Uploads the results produced by a Klocwork analysis using kwadmin load
	 */
	public boolean uploadResults(String url,
								 String projectName, 
								 String tablesDirectory,
								 boolean copyTables,
								 boolean force) {
		boolean result = true;
		try {
			String urlVal = (url != null && url.length() > 0) ? " --url " + url : "";
			String copyTablesVal = copyTables ? " --copy-tables " : "";
			String forceVal = force ? " --force " : " ";
			Process p = Runtime.getRuntime().exec("kwadmin" + urlVal 
											+ "load " + projectName
											+ tablesDirectory
											+ copyTablesVal
											+ forceVal);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = reader.readLine();
			while(line!=null) {
				logError(line);
				line = reader.readLine();
			}
			return result;
		}
		catch(Exception e) {
			logError("Exception while creating project: " + e.getMessage());
			logError(e.getStackTrace().toString());
			return false;
		}
	}
	
	public String getError() {
		return errorMessage;
	}
	public boolean hasErrors() {
		return errors;
	}
	private void logError(String error) {
		errorMessage = errorMessage + error + "\n";
	}
	
	public static void main(String args[]) {
		//Check if user has provided host argument
		//Else use null (will cause getRegistry to use localhost)
		String host = (args.length < 1) ? null : args[0];
		//Check is port has been provided (Else use 1099 default)
		int port = (args.length < 2) ? 1099 : Integer.valueOf(args[1]);
		try {
			//Create a server instance and generate a stub to be registered in RMI registry
			KlocworkWorker worker = new KlocworkWorker();
			IKlocworkWorker stub = (IKlocworkWorker) UnicastRemoteObject.exportObject(worker, 0);
			
			//Register the stub in the registry
			Registry registry = LocateRegistry.getRegistry(host, port);
			registry.rebind(stubName, stub);
		}
		catch(Exception e) {
			System.out.println("kwopenproject listener server exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
