package emenda.kwopenproject;

import java.io.File;
import java.io.FileOutputStream;

import java.util.Calendar;
import java.text.SimpleDateFormat;

public class Logging {

	private File file;
	private final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private Calendar cal;
	private SimpleDateFormat sdf;
	
	public Logging(String path) {
		
		cal = Calendar.getInstance();
		sdf = new SimpleDateFormat(DATE_FORMAT);
		
		file = new File(path);
		System.out.println("Attempting to create log file: " + path);
		//Create the file if it does not exist
		try {
			file.createNewFile();
			file.setWritable(true);
		}
		catch(Exception e) {
			System.out.println("Could not create log file.");
		}
	}
	
	public boolean writeLog(String message) {
		if(file != null && file.canWrite()) {
			try {
				String output = (now() + message + "\r\n");
				FileOutputStream outstream = new FileOutputStream(file, true);
				outstream.write(output.getBytes());
				outstream.flush();
				outstream.close();
			}
			catch(Exception e) {
				System.out.println("Failed to write to file: " + file.getAbsolutePath());
			}
			return true;
		}
		
		System.out.println("Cannot write to log file.");
		return false;
	}
	
	private String now() {
		return (sdf.format(cal.getTime()) + " - ");
	}
	
}
