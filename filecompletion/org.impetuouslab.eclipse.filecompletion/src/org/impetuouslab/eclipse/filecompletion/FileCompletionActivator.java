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
    public static final String openFileWithExternalProgramCmdPerfId="org.impetuouslab.eclipse.filecompletion.openFileWithExternalProgramCmd";
    
    public static final String openFileWithExternalProgramArgsPerfId="org.impetuouslab.eclipse.filecompletion.openFileWithExternalProgramArgs";
    
    public static final String fileCompletionPref="org.impetuouslab.eclipse.filecompletion.pref";

    public static final String checkDuringTypingId="org.impetuouslab.eclipse.filecompletion.checkDuringTyping";

    
    public FileCompletionActivator() {
        plugin = this;
        LOG.info("filecompletion : activated");
    }

    public static FileCompletionActivator getDefault() {
        return plugin;
    }

    @Override
    protected void initializeDefaultPreferences(IPreferenceStore store) {
    }

}
