package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.FileEditorInput;
import org.impetuouslab.eclipse.filecompletion.FileCompletionActivator;

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

	public static void openFile(ExecutionEvent event, boolean inEclipse)
			throws ExecutionException {
		LOG.info("execute " + event);
		LOG.info("execute " + event.getCommand());
		LOG.info("execute " + event.getTrigger());
		LOG.info("execute " + event.getApplicationContext());
		// event.getTrigger().
		try {
			LOG.fine("stating proposal");
			Event event2 = (Event) event.getTrigger();
			LOG.info("widget" + event2.widget);
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench()
					.getWorkbenchWindows()[0].getActivePage();
			IEditorPart compilationUnit = workbenchPage.getActiveEditor();
			IEditorInput editorInput = compilationUnit.getEditorInput();
			Object adapter = editorInput.getAdapter(IJavaElement.class);
			if (workbenchPage.getActiveEditor() instanceof org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor) {
				LOG.info("we are in java editor ");
				org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor cue = (org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor) workbenchPage
						.getActiveEditor();

				int documentOffset = cue.getViewer().getSelectedRange().x;
				if (adapter == null) {
					LOG.info("adapter is null");
				} else if (adapter instanceof ICompilationUnit) {
					ICompilationUnit cu = (ICompilationUnit) adapter;
					LOG.fine("we found CompilationUnit ");

					String source = cu.getSource();
					if (FileCompletionImpl.isInStringLiteral(source,
							documentOffset)) {
						StringLiteral fileProposals = FileCompletionImpl
								.findStringLiteral(source, documentOffset)
								.getFoundedNode();
						if (fileProposals == null) {
							LOG.fine("looks like not in string literal");
						} else {
							String literalValue = fileProposals
									.getLiteralValue();
							literalValue = literalValue.replace('\\', '/');
							File file = new File(literalValue);
							if (file.exists()) {
								if (inEclipse) {
									openFile(file);
								} else {
									openFileInExternalProgram(file);
								}
							} else {
								MessageDialog.openError(
										null,
										"File not found",
										"File not found: "
												+ file.getAbsolutePath());
							}
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
			txtEditorId = PlatformUI.getWorkbench().getEditorRegistry()
					.getDefaultEditor("a.txt").getId();
		}
	}

	public static void openFile(File file) throws Exception {

		LOG.info("opening file " + file);
		LOG.info("opening file " + file);
		LOG.info("open in eclipse " + file);
		IFile findFile = (IFile) findFileResourceInEclipse(file);
		final IEditorDescriptor editorDescriptor = PlatformUI.getWorkbench()
				.getEditorRegistry().getDefaultEditor(file.getAbsolutePath());
		String edId;
		if (editorDescriptor == null) {
			LOG.info("editor desc is null");
			inittt();
			edId = txtEditorId;
		} else {
			edId = editorDescriptor.getId();
		}
		if (findFile == null) {
			LOG.info("can't fine file in workspace " + file);
			openFileInExternalProgram(file);
		} else {
			LOG.info("found " + findFile);
			final FileEditorInput editorInput = new FileEditorInput(findFile);
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench()
					.getWorkbenchWindows()[0].getActivePage();
			workbenchPage.openEditor(editorInput, edId);
		}
	}

	public static IResource findFileResourceInEclipse(final File fileToFind)
			throws Exception {
		LOG.info("1");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ResourceFinder visitor = new ResourceFinder(fileToFind);
		root.accept(visitor, IResource.NONE);
		LOG.info("sinished");
		return visitor.getFoundedResource();
	}

	public static void openFileInExternalProgram(File file) throws IOException {
		String programPathString = FileCompletionActivator.getDefault()
				.getPreferenceStore()
				.getString(FileCompletionActivator.openFileWithExternalProgramPerfId);
		if (programPathString==null||programPathString.trim().length()==0) {
			LOG.info("opening pref dialog");
			PreferenceDialog createPreferenceDialogOn = PreferencesUtil.createPreferenceDialogOn(null, "org.impetuouslab.eclipse.filecompletion.pref", null, null);
			createPreferenceDialogOn.open();
			LOG.info("finishing pref dialog");
			//user should try again after setting external program
			return;
		}
		LOG.info("programPathString = " + programPathString);
		File programFile = new File(programPathString);
		if (!programFile.isFile()) {
			throw new FileNotFoundException(programPathString);
		}
		String[] command = new String[] { programPathString,
				file.getAbsolutePath() };
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
