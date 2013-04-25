package emenda.kwopenproject.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import emenda.kwopenproject.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
//		store.setDefault(PreferenceConstants.P_BOOLEAN, true);
//		store.setDefault(PreferenceConstants.P_CHOICE, "choice2");
		store.setDefault(PreferenceConstants.P_KWTD4PROPERTIES,
				"\\tmp\\kwtd4.properties");
		store.setDefault(PreferenceConstants.P_PROJECTSDIR,
				"\\.metadata\\.plugins\\org.eclipse.core.resources\\.projects");
		store.setDefault(PreferenceConstants.P_KWLPPROPERTIES,
				"\\com.klocwork.inforceeclipse\\.kwps\\kwps.properties");
		store.setDefault(PreferenceConstants.P_PROJECTLOG,
				"\\tmp\\kwtd4settings.log");
		store.setDefault(PreferenceConstants.P_WORKSPACELOG,
				"\\kwopenproject.log");
	}

}
