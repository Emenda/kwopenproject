package emenda.kwopenproject.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import emenda.kwopenproject.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PreferencesPage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencesPage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
//		addField(new DirectoryFieldEditor(PreferenceConstants.P_PATH, 
//				"&Directory preference:", getFieldEditorParent()));
//		addField(
//			new BooleanFieldEditor(
//				PreferenceConstants.P_BOOLEAN,
//				"&An example of a boolean preference",
//				getFieldEditorParent()));
//
//		addField(new RadioGroupFieldEditor(
//				PreferenceConstants.P_CHOICE,
//			"An example of a multiple-choice preference",
//			1,
//			new String[][] { { "&Choice 1", "choice1" }, {
//				"C&hoice 2", "choice2" }
//		}, getFieldEditorParent()));
		addField(
			new StringFieldEditor(PreferenceConstants.P_KWTD4PROPERTIES, 
					"Relative path to kwtd4.properties file:\n" +
					"(default: \\tmp\\kwtd4.properties)", 
					getFieldEditorParent()));
		addField(
				new StringFieldEditor(PreferenceConstants.P_PROJECTSDIR,
						"Relative path to .projects directory:\n" +
						"(default: \\.metadata\\.plugins\\org.eclipse.core.resources\\.projects)", 
						getFieldEditorParent()));
		addField(
				new StringFieldEditor(PreferenceConstants.P_KWLPPROPERTIES,
						"Relative path to local Klocwork project properties file:\n" +
						"(default: \\com.klocwork.inforceeclipse\\.kwps\\kwps.properties)", 
						getFieldEditorParent()));
		addField(
				new StringFieldEditor(PreferenceConstants.P_PROJECTLOG,
						"Relative path to project log location:\n" +
						"(default: \\tmp\\kwtd4settings.log)", 
						getFieldEditorParent()));
		addField(
				new StringFieldEditor(PreferenceConstants.P_WORKSPACELOG,
						"Relative path to workspace log location:\n" +
						"(default: \\kwopenproject.log)", 
						getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}