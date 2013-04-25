package emenda.kwjlib;

import org.json.*;

public class KWJSONRecord {
	private JSONObject m_json_obj;
	
	public KWJSONRecord(String a_record_string) {
		try {
			m_json_obj = new JSONObject(a_record_string);
		}
		catch(JSONException e) {
			KWWebAPIService.appendError("public KWJSONRecord(String a_record_string):\n" +
					"\tError while parsing JSON record: \n" + a_record_string + "\n" +
					"\tException: " + e.getMessage());
		}
	}
	
	public String getValue(String a_key) {
		try {
			return m_json_obj.get(a_key).toString();
		}
		catch(JSONException e) {
			KWWebAPIService.appendError("public String getValue(String key):\n" +
					"\tError fetching value from JSON record: \n" + a_key + "\n" +
					"\tException: " + e.getMessage());
			return "";
		}
	}
	
	public String toString() {
		return m_json_obj.toString();
	}
}
