package emenda.kwopenproject.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IKlocworkWorker extends Remote {
	static final String stubName = "KlocworkWorker";
	boolean createProject(String url,
			 String projectName,
			 String encoding) throws RemoteException;
	boolean duplicateProject(String url,
						 	 String origProjectName,
						 	 String newProjectName,
						 	 String encoding) throws RemoteException;
	boolean pullFromMKS(String mksHost, String mksProject, String mksIteration) throws RemoteException;
	boolean doBuild(String rootDir) throws RemoteException;
	boolean runAnalysis(String kwtablesPath,
			   String compilerOptions,
			   String linkerOptions,
			   String encoding,
			   String url,
			   boolean force,
			   String buildspecPath) throws RemoteException;
	boolean uploadResults(String url,
			 String projectName, 
			 String tablesDirectory,
			 boolean copyTables,
			 boolean force) throws RemoteException;
	String getError() throws RemoteException;
	boolean hasErrors() throws RemoteException;
}
