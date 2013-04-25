package emenda.kwopenproject;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import emenda.kwjlib.*;
import emenda.kwopenproject.preferences.PreferenceConstants;
import emenda.kwopenproject.remote.KlocworkCaller;

import org.json.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.lang.Long;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "kwopenproject"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	//Strings for use in property files
	final static String PROP_TEMP_FILEPATH = "kwtd4.filepath";
	final static String PROP_PROJECT_VERSION = "kwtd4.version";
	final static String PROP_PROJECT_CONFIGPATH = "kwtd4.server.configpath";
	final static String PROP_PROJECT_PROJECTNAME = "kwtd4.project";
	final static String PROP_PROJECT_BUILDUNIT = "kwtd4.buildunit";
	final static String PROP_PROJECT_MKSVERSION = "kwtd4.mksversion";
	final static String PROP_PROJECT_MKSHISTORYFILE = "kwtd4.mkshistoryfile";
	final static String PROP_PROJECT_SERVER = ".server";
	final static String PROP_PROJECT_BUILDSPEC = ".buildspec";
	final static String PROP_PROJECT_KWHOST = ".server.host";
	final static String PROP_PROJECT_KWPORT = ".server.port";
//	final static String PROP_PROJECT_KWUSER = ".server.user";
	final static String PROP_PROJECT_LICHOST = ".license.host";
	final static String PROP_PROJECT_LICPORT = ".license.port";
	final static String PROP_PROJECT_SSL = ".ssl";
	final static String PROP_PROJECT_KWOPENPROJECT_PORT = ".kwopenproject.port";
	final static String PROP_KW_HOST = "klocwork.host";
	final static String PROP_KW_PORT = "klocwork.port";
	final static String PROP_KW_LICHOST = "license.host";
	final static String PROP_KW_LICPORT = "license.port";
	final static String PROP_KW_PROJECT = "klocwork.project";
	final static String PROP_KW_SSL = "klocwork.ssl";
	final static String KLOCWORK_PROPERTIES_FILE = "\\klocwork.properties";
	
	final static boolean PROMPT_USER = true;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		//After starting plug-in, load plug-in preferences and set up logging
		store = Activator.getDefault().getPreferenceStore();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		logging = new Logging(workspace.getRoot().getLocation().toOSString() + store.getString(PreferenceConstants.P_WORKSPACELOG));
		//scan each project, then register listener
		logging.writeLog("Plug-in startup. Scanning projects...");
		if(scanProjects() == true)
			logging.writeLog("Plug-in startup. Scanning projects... Success");
		else
			logging.writeLog("Plug-in startup. Scanning projects... Failure");
		logging.writeLog("Registering listener with workspace...");
		if(registerListener())
			logging.writeLog("Registering listener with workspace... Success");
		else
			logging.writeLog("Registering listener with workspace... Failure");
//		org.eclipse.swt.widgets.Shell parent = getWorkbench().getModalDialogShellProvider().getShell();
//		MessageBox dialog = new MessageBox(parent, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
//		dialog.setText("Klocwork Project Activation");
//		dialog.setMessage("Klocwork has noticed that you have opened a new project and will now attempt to load settings automatically. Do you wish to continue?");
//		dialog.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/*
	 * Non-inherited variables and functions
	 */
	
	private IResourceChangeListener listener;
	private HashMap<String, Long> lastModifiedMap;
	private final String versionNumber = "1.0";
	private Logging logging;
	private IPreferenceStore store;
	
	//Utility function to load properties
	public Properties loadProperties(String propertyPath) {
		logging.writeLog("Loading properties file: " + propertyPath + "...");
		//Initialise and load properties object from file
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(propertyPath));
			prop.put(PROP_TEMP_FILEPATH, propertyPath);
		}
		catch(Exception e) {
			logging.writeLog("Exception while reading properties file: " + propertyPath + "\nException message: " + e.getMessage());
			return null;
		}
		logging.writeLog("Loading properties file: " + propertyPath + "... Success");
		return prop;
	}
	
	//Utility function to store properties
	public boolean storeProperties(Properties prop) {
		if(prop != null) {
			//Remove temporary filepath value from file before storing
			String filepath = prop.getProperty(PROP_TEMP_FILEPATH);
			prop.remove(PROP_TEMP_FILEPATH);
			try {
				logging.writeLog("Storing properties file: " + filepath + "...");
				prop.store(new FileOutputStream(filepath), null);
				logging.writeLog("Storing properties file: " + filepath + "... SUCCESS");
			}
			catch(Exception e) {
				logging.writeLog("Exception while writing Klocwork Sync properties to file: " + prop.getProperty(PROP_TEMP_FILEPATH) + "\nMessage: " + e.getMessage());
				return false;
			}
		}
		
		return true;
	}
	
	//Function to register the listener
	public boolean registerListener() {
		logging.writeLog("Registering listener...");
		//Declare the listener
	    listener = new IResourceChangeListener() {
	      public void resourceChanged(IResourceChangeEvent event) {
	    	  handleEvent(event);
	      }
	   };
	   
	   //Register the listener with the workspace
	   IWorkspace workspace = ResourcesPlugin.getWorkspace();
	   workspace.addResourceChangeListener(listener);
	   logging.writeLog("Successfully registed listener");
	   return true;
	}
	
	//Function called by listener upon event
	//Check for a POST_CHANGE event then scan for a recently opened project
	private void handleEvent(IResourceChangeEvent event) {
		if(event.getType() == IResourceChangeEvent.POST_CHANGE) {
			logging.writeLog("Handling event of type POST_CHANGE...");
			IResourceDelta rootDelta = event.getDelta();
			//Scan the delta tree for a newly opened project with function getOpenedProject
			logging.writeLog("Fetching activated project (if any)...");
			IResourceDelta projectDelta = getActivatedProject(rootDelta);
			//Have we found a project?
			if(projectDelta != null) {
				logging.writeLog("Project found! Processing...");
				//Handle the project resource
				IResource projectResource = projectDelta.getResource();
				handleResource(projectResource);
				logging.writeLog("Project found! Processing... Complete");
			}
		}
	}
	
	//Function to retrieve the opened project (if there is one) from
	// the project list of the workspace (represented by rootDelta)
	private IResourceDelta getActivatedProject(IResourceDelta rootDelta) {
		logging.writeLog("Fetching project resource objects...");
		IResourceDelta children[] = rootDelta.getAffectedChildren();
		
		if(children == null) {
			logging.writeLog("No project resource objects could be retrieved. Bailing out.");
			return null;
		}
		
		logging.writeLog("Number of project resource objects found: " + children.length);
		
		for(int i = 0; i < children.length; i++) {
			logging.writeLog("Processing project resource object " + i + " of " + children.length);
			
			if(children[i] == null)
				continue;

			//No need to check "closed" projects
			IResource resource = children[i].getResource();
			logging.writeLog("Project name: " + resource.getName());
			if(resource.isAccessible() == true) {
				String filepath = resource.getLocation().toOSString() + store.getString(PreferenceConstants.P_KWTD4PROPERTIES);
				File file = new File(filepath);
				if(file.exists() != true) {
					logging.writeLog("No kwtd4 properties file found for this project! Continuing to next project...");
					continue;
				}
				
				long lastModified = lastModifiedMap.get(resource.getName());
				logging.writeLog("Kwtd4 properties file found. Last modified: " + file.lastModified() 
						+ ". Comparing this date to " + lastModified);
				if(file.lastModified() > lastModified) {
					//The config file of this project has been modified since last scan,
					//so this is the project to process
					logging.writeLog("Properties file has been modified, this is the project which has been activated.");
					lastModifiedMap.put(resource.getName(), file.lastModified());
					return children[i];
				}
			}
//			//Check if an "open project" event has occurred
//			if((children[i].getFlags() & IResourceDelta.OPEN) != 0) {
//				IResource resource = children[i].getResource();
//				if(resource == null)
//					continue;
//				
//	    		//IResourceDelta.OPEN flag is used for both opening and closing
//	    		//project, so we must check it is accessible (i.e. open)
//				if(resource.getType() == IResource.PROJECT &&		
//				   resource.isAccessible() == true) {
//					//This is the project which has been opened!
//					return children[i];
//				}
//			}
		}
		
		return null;
	}
	
	//Function to handle a project resource.
	// Attempts to set values in the Klocwork plug-in synchronisation tab
	// Fetches list of Klocwork projects from Klocwork server
	// If no matching project exists on server, makes calls to create one
	// Sets values in synchronisation tab accordingly
	private boolean handleResource(IResource projectResource) {
		
		String projectname;
		String uniqueName;
		String tempresult;
		
		String host;
		String port;
		String user;
		
		String workspacePath;
		String klocworkPropertiesFilePath;
		
		Properties klocworkProperties;
		Properties projectProperties;
		Properties kwtd4Properties;
		
		IPath projectPath;
		
//		//Display messagebox, asking user if they wish to continue
//		org.eclipse.swt.widgets.Shell parent = getWorkbench().getModalDialogShellProvider().getShell();
//		MessageBox dialog = new MessageBox(parent, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
//		dialog.setText("Klocwork Project Activation");
//		dialog.setMessage("Klocwork has noticed that you have opened a new project and will now attempt to load settings automatically. Do you wish to continue?");
		
		
		//Begin by fetching project path and loading properties file
		projectPath = projectResource.getLocation();
		workspacePath = projectResource.getWorkspace().getRoot().getLocation().toOSString();
		klocworkPropertiesFilePath = workspacePath + store.getString(PreferenceConstants.P_PROJECTSDIR) + 
				"\\" + projectResource.getName() + store.getString(PreferenceConstants.P_KWLPPROPERTIES);
		klocworkProperties = loadProperties(klocworkPropertiesFilePath);
		projectProperties = loadProperties(projectPath.toOSString() + store.getString(PreferenceConstants.P_KWTD4PROPERTIES));
		kwtd4Properties = loadProperties(projectProperties.getProperty(PROP_PROJECT_CONFIGPATH) + KLOCWORK_PROPERTIES_FILE);
		
		//Logging
		logging.writeLog("Now processing project " + projectResource.getName() 
				+ ". Please see log file " + projectPath.toOSString() + store.getString(PreferenceConstants.P_PROJECTLOG) + " for more information."); 
		Logging projectLog = new Logging(projectPath.toOSString() + store.getString(PreferenceConstants.P_PROJECTLOG));
		projectLog.writeLog("Processing project " + projectResource.getName());
				
		//Check version is correct
		if(!(projectProperties.get(PROP_PROJECT_VERSION).equals(versionNumber))) {
			projectLog.writeLog("Incorrect version number in properties file: " + projectProperties.getProperty(PROP_TEMP_FILEPATH) 
					+ " \nProgram version: " + versionNumber + ". Properties file version: " +projectProperties.get(PROP_PROJECT_VERSION)); 
		}
		
		
//		//1. Retrieve plugin sync settings, if they are already set then return true,
//		// we do not need to change them
//		projectLog.writeLog("Fetching existing Klocwork project setting...");
//		String kwsyncname = klocworkProperties.getProperty(PROP_KW_PROJECT);
//		if(kwsyncname != null && kwsyncname.length() > 0) {
//			projectLog.writeLog("Setting already exists, value is: " + kwsyncname);
//			projectLog.writeLog("No further processing of project.");
//			return true;
//		}
//		projectLog.writeLog("Setting does not exist/is incorrect, fetching project unique name from properties file: " + projectProperties.getProperty(PROP_TEMP_FILEPATH));
		
		//1. Fetch project & iteration unique name
		// Return false on failure
		projectLog.writeLog("Generating project unique name from values fetched from properties file: " + projectProperties.getProperty(PROP_TEMP_FILEPATH));
		uniqueName = Util.generateUniqueName(projectProperties.getProperty(PROP_PROJECT_PROJECTNAME),
													projectProperties.getProperty(PROP_PROJECT_BUILDUNIT),
													projectProperties.getProperty(PROP_PROJECT_MKSVERSION));
		if(uniqueName.length() < 1) {
			projectLog.writeLog("Generated project unique name is blank. Please correct settings in kwtd4settings.properties file: " + projectProperties.getProperty(PROP_TEMP_FILEPATH));
			return false;
		}
		projectLog.writeLog("Generated project unique name is: " + uniqueName);
		//We can now read various values from kwtd4 project-specific properties
		host = kwtd4Properties.getProperty(uniqueName + PROP_PROJECT_KWHOST);
		port = kwtd4Properties.getProperty(uniqueName + PROP_PROJECT_KWPORT);
		user = System.getProperty("user.name");
//		user = kwtd4Properties.getProperty(uniqueName + PROP_PROJECT_KWUSER);
		//Check that we have generated the correct unique name by attempting to retrieve the Klocwork server host value
		// for the project in question. If the retrieval fails then no settings for this project exist and it must be created.
		tempresult = kwtd4Properties.getProperty(uniqueName + PROP_PROJECT_KWHOST);
		if(tempresult != null && tempresult.length() > 0) {
			//2. Set values of Klocwork synchronisation tab using values from properties file
			klocworkProperties.put(PROP_KW_HOST, 	host);
			klocworkProperties.put(PROP_KW_PORT, 	port);
			klocworkProperties.put(PROP_KW_LICHOST, kwtd4Properties.getProperty(uniqueName + PROP_PROJECT_LICHOST));
			klocworkProperties.put(PROP_KW_LICPORT, kwtd4Properties.getProperty(uniqueName + PROP_PROJECT_LICPORT));
			klocworkProperties.put(PROP_KW_SSL, 	kwtd4Properties.getProperty(uniqueName + PROP_PROJECT_SSL));
		}
		else { //Copy settings from an earlier version
			tempresult = Util.getPreviousVersion(projectProperties.getProperty(PROP_PROJECT_PROJECTNAME), 
												 projectProperties.getProperty(PROP_PROJECT_BUILDUNIT), 
												 projectProperties.getProperty(PROP_PROJECT_MKSVERSION), kwtd4Properties);
			if(tempresult == null || tempresult.length() < 1) {
				//Now we have a problem, what do we do? <-- Could happen very easily
				projectLog.writeLog("Could not find an earlier version of " + uniqueName + ". Unable to continue as Klocwork host and port are required.");
				projectLog.writeLog("Please correct this by entering the following information manually in file " + kwtd4Properties.getProperty(PROP_TEMP_FILEPATH) + ":");
				projectLog.writeLog(" 1. " + uniqueName + PROP_PROJECT_KWHOST);
				projectLog.writeLog(" 2. " + uniqueName + PROP_PROJECT_KWPORT);
				projectLog.writeLog(" 3. " + uniqueName + PROP_PROJECT_LICHOST);
				projectLog.writeLog(" 4. " + uniqueName + PROP_PROJECT_LICPORT);
				return false;
			}
			
		}
		
		
		//3. Fetch list of projects from server
		// Try to find a project on server which matches the unique name
		// If a match is found, set this in the properties of the project
		projectname = "";
		projectLog.writeLog("Connecting to KW Review Web API to retrieve list of existing projects...");
		projectLog.writeLog("host: " + host + " port: " + port + " user: " + user);
		KWWebAPIService service = new KWWebAPIService(host, port, user);
		if(service.connect()) {
			KWJSONRecord projects[] = service.projects();
			if(projects == null) {
				projectLog.writeLog("No projects returned. Error message (if any): " + KWWebAPIService.getError());
			}
			else {
				//See if we have found a match
				for(int i = 0; i < projects.length; i++) {
					if(projects[i].getValue("name").equalsIgnoreCase(uniqueName)) {
						projectLog.writeLog("Match found, project " + projects[i].getValue("name"));
						projectname = uniqueName;
						break;
					}
				}
			}
		}
		else {
			projectLog.writeLog("Connection failed. Error message returned: " + KWWebAPIService.getError());
		}
		
		//4. If all else fails, the project must be created on the KW server
		// This is achieved using Java RMI with a listener on the server
		// Once the project has been created, load the source from MKS
		// Then run an analysis and upload the results
		// Always duplicate the project to ensure history and settings are copied
		if(projectname.length() < 1) {
			projectLog.writeLog("Attempting to connect to KlocworkWorker server (RMI) service");
			KlocworkCaller caller = new KlocworkCaller();
			//Connect to RMI listener (KlocworkWorker) using caller (KlocworkCaller)
			if(caller.connectToServer(host, Integer.parseInt(kwtd4Properties.getProperty(uniqueName + PROP_PROJECT_KWOPENPROJECT_PORT)))) {
				projectLog.writeLog("Connected to server successfully!");
			}
			else {
				projectLog.writeLog("ERROR: Connection failed.");
				projectLog.writeLog("Server error (if any): " + caller.getServerError());
				projectLog.writeLog("Caller error (if any): " + caller.getError());
				return false;
			}
			//Attempt to create project
			if(caller.createProject("http://" + host + ":" + port, uniqueName, "")) {
				projectLog.writeLog("Created project successfully!");
			}
			else {
				projectLog.writeLog("ERROR: Project creation failed.");
				projectLog.writeLog("Server error (if any): " + caller.getServerError());
				projectLog.writeLog("Caller error (if any): " + caller.getError());
				return false;
			}
			//TODO: Use TD4 to fetch source and perform build
			//TODO: Run analysis
			//TODO: Upload results
			projectname = "something";
		}
		
		klocworkProperties.put(PROP_KW_PROJECT, projectname);
		return storeProperties(klocworkProperties);
	}
	
	private boolean scanProjects() {
		//Initialise hashmap if not initialised
		logging.writeLog("Initialising hashmap...");
		if(lastModifiedMap == null) {
			lastModifiedMap = new HashMap<String, Long>();
		}
		//Get the workspace and iterate through projects
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject projects[] = workspace.getRoot().getProjects();
		if(projects != null) {
			logging.writeLog("Number of projects found in scan: " + projects.length);
			for(int i = 0; i < projects.length; i++) {
				String filepath = projects[i].getLocation().toOSString() + store.getString(PreferenceConstants.P_KWTD4PROPERTIES);
				File file = new File(filepath);
				//Store the last modified date of the config file in the hashmap
				if(file.exists()) {
					lastModifiedMap.put(projects[i].getName(), file.lastModified());
				}
				else {
					lastModifiedMap.put(projects[i].getName(), (long)0);
				}
				logging.writeLog("Last modified date of file " + filepath
						+ " stored in hashmap as " + lastModifiedMap.get(projects[i].getName()));
			}
			return true;
		}
		logging.writeLog("No projects were returned when scanning. Empty workspace?");
		return false;
	}
}
