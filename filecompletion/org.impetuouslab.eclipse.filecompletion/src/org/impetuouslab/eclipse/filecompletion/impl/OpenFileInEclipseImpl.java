package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.impetuouslab.eclipse.filecompletion.FileCompletionActivator;
import org.impetuouslab.eclipse.filecompletion.FileCompletionPreferencePage;

public class OpenFileInEclipseImpl implements IHandler {
	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(FileClassFinder.class.getName());

	public void addHandlerListener(IHandlerListener handlerListener) {
		LOG.info("addHandlerListener " + handlerListener);

	}

	public void dispose() {
		LOG.info("dispose");

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		openFile(event, true);
		return null;
	}

	public static void openFile(ExecutionEvent event, boolean inEclipse) throws ExecutionException {
		LOG.info("execute " + event);
		LOG.info("execute " + event.getCommand());
		LOG.info("execute " + event.getTrigger());
		LOG.info("execute " + event.getApplicationContext());
		// event.getTrigger().
		try {
			LOG.fine("stating proposal");
			Event event2 = (Event) event.getTrigger();
			LOG.info("widget" + event2.widget);
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
			IEditorPart compilationUnit = workbenchPage.getActiveEditor();
			IEditorInput editorInput = compilationUnit.getEditorInput();
			Object adapter = editorInput.getAdapter(IJavaElement.class);
			IEditorPart activeEditor = workbenchPage.getActiveEditor();
			if (activeEditor instanceof org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor) {
				LOG.info("we are in java editor ");
				org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor cue = (org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor) activeEditor;
				int documentOffset = cue.getViewer().getSelectedRange().x;
				if (adapter == null) {
					LOG.info("adapter is null");
				} else if (adapter instanceof ICompilationUnit) {
					ICompilationUnit cu = (ICompilationUnit) adapter;
					LOG.fine("we found CompilationUnit");
					String source = cu.getSource();
					if (FileCompletionImpl.isInStringLiteral(source, documentOffset)) {
						FileClassFinder findStringLiteral = FileCompletionImpl.findStringLiteral(source,
								documentOffset);
						if (findStringLiteral.isFile()) {
							StringLiteral fileProposals = findStringLiteral.getFoundedNode();
							if (fileProposals == null) {
								LOG.info("looks like not in string literal");
							} else {
								String literalValue = fileProposals.getLiteralValue();
								literalValue = literalValue.replace('\\', '/');
								File file = new File(literalValue);
								if (file.exists()) {
									if (inEclipse) {
										openFileInEclipse(file);
									} else {
										openFileInExternalProgram(file);
									}
								} else {
									MessageDialog.openError(null, "File not found",
											"File not found: " + file.getAbsolutePath());
								}
							}
						} else {
							LOG.info("found but not it is not file");
						}
					} else {
						LOG.info("not in string literal");
					}

				}
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "error during opening file", e);
			MessageDialog.openError(null, "File open error", e + "");
		}
	}

	public static String txtEditorId;

	public static void inittt() {
		if (txtEditorId == null) {
			txtEditorId = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor("a.txt").getId();
		}
	}

	public static void openFileInEclipse(File file) throws Exception {
		LOG.info("opening file " + file);
		IFile findFile = (IFile) findFileResourceInEclipse(file);
		final IEditorDescriptor editorDescriptor = PlatformUI.getWorkbench().getEditorRegistry()
				.getDefaultEditor(file.getAbsolutePath());
		String edId;
		if (editorDescriptor == null) {
			LOG.info("editor desc is null");
			if (file.isFile() && file.length() > FileCompletionSettings.maxFileSizeToOpen) {
				MessageDialog.openError(null, "File open error", "File too big : " + file.length());
				return;
			}
			inittt();
			edId = txtEditorId;
		} else {
			edId = editorDescriptor.getId();
		}
		if (findFile == null) {
			LOG.info("can't find file in workspace, opening in external editor : " + file);
			openFileInExternalProgram(file);
		} else {
			LOG.info("found file in workspace : " + findFile);
			final FileEditorInput editorInput = new FileEditorInput(findFile);
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getActivePage();
			workbenchPage.openEditor(editorInput, edId);
		}
	}

	public static IResource findFileResourceInEclipse(final File fileToFind) throws Exception {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ResourceFinder visitor = new ResourceFinder(fileToFind);
		root.accept(visitor, IResource.NONE);
		LOG.info("finished");
		return visitor.getFoundedResource();
	}

	public static void openFileInExternalProgram(File file) throws IOException {
		LOG.info("opening file : " + file);
		String programPathString = FileCompletionActivator.getDefault().getPreferenceStore()
				.getString(FileCompletionActivator.openFileWithExternalProgramCmdPerfId);
		if (programPathString == null || programPathString.trim().length() == 0) {
			LOG.info("opening pref dialog");
			PreferenceDialog createPreferenceDialogOn = PreferencesUtil.createPreferenceDialogOn(null,
					FileCompletionActivator.fileCompletionPref, null, null);
			createPreferenceDialogOn.open();
			LOG.info("finishing pref dialog");
			// user should try again after setting external program
			return;
		}
		LOG.info("programPathString = " + programPathString);
		File programFile = new File(programPathString);
		if (!programFile.isFile()) {
			throw new FileNotFoundException(programPathString);
		}
		ArrayList<String> cmd = new ArrayList<String>();
		cmd.add(programPathString);
		String args = FileCompletionActivator.getDefault().getPreferenceStore()
				.getString(FileCompletionActivator.openFileWithExternalProgramArgsPerfId);
		if (args != null && args.length() > 0) {
			cmd.addAll(Arrays.asList(args.split(" ")));
		}
		cmd.add(file.getAbsolutePath());
		LOG.info("command to run : " + cmd);
		String[] command = cmd.toArray(new String[0]);
		Runtime.getRuntime().exec(command);
	}

	public boolean isEnabled() {
		LOG.info("enabled ");
		return true;
	}

	public boolean isHandled() {
		LOG.info("handled");
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		LOG.info("removeHandlerListener " + handlerListener);

	}
}
