package org.impetuouslab.eclipse.filecompletion;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Activator used to receive preference store 
 *
 */
public class FileCompletionActivator extends AbstractUIPlugin {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(FileCompletionPreferencePage.class.getName());

    private static FileCompletionActivator plugin;


    /**
     * This id is used to store external program path
     */
    public static final String openFileWithExternalProgramPerfId="org.impetuouslab.eclipse.filecompletion.openFileWithExternalProgram";

    public FileCompletionActivator() {
        plugin = this;
    }

    public static FileCompletionActivator getDefault() {
        return plugin;
    }

    @Override
    protected void initializeDefaultPreferences(IPreferenceStore store) {
    }

}
