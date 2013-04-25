package emenda.kwopenproject;

import java.util.Properties;

public class Util {
	private static final String delimiter = ";";
	
	public static String generateUniqueName(String projectname, String buildunit, String mksversion) {
		String name = projectname + delimiter + buildunit + delimiter + mksversion;
		return name;
	}
	
	public static String getDelimiter() {
		return delimiter;
	}
	
	public static String getPreviousVersion(String projectname, String buildunit, String mksversion, Properties prop) {
		String previousversion = "";
		
		//Find the most recent previous version with existing settings in the properties file
		
		return previousversion;
	}
}
