package org.impetuouslab.eclipse.filecompletion.impl;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

public class OpenFileInExternalTool implements IHandler {
	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(FileClassFinder.class.getName());

	public void addHandlerListener(IHandlerListener handlerListener) {
		LOG.info("addHandlerListener " + handlerListener);

	}

	public void dispose() {
		LOG.info("dispose");

	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		OpenFileInEclipseImpl.openFile(event, false);
		return null;
	
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
