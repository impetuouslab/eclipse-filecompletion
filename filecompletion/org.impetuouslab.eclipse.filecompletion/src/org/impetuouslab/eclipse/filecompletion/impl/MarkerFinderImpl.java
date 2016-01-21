package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.impetuouslab.eclipse.filecompletion.FileCompletionPreferencePage;

/**
 * Marker used to validate all file and pattern constructors
 * 
 */
public class MarkerFinderImpl extends CompilationParticipant {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(FileClassFinder.class.getName());

	// Field value matched with id specified in plugin.xml
	public final static String markerId = "org.impetuouslab.eclipse.filecompletion.FileNotFound";

	private static volatile String lastVerifiedResource;
	private static volatile long lastVerifiedTime;

	@Override
	public void reconcile(final ReconcileContext context) {
		if (FileCompletionPreferencePage.checkDuringTyping) {
			if (context == null) {
				LOG.fine("context is null");
			} else {
				IJavaElementDelta delta = context.getDelta();
				if (delta == null) {
					LOG.fine("delta is null");
				} else {
					reconcileImpl(context, delta);
				}
			}
		}

	}

	public void reconcileImpl(final ReconcileContext context, IJavaElementDelta delta) {
		long reconStartTime = System.currentTimeMillis();
		IResource resource = null;
		try {

			// LOG.info("changed element "+ context.getDelta().getElement().);
			// LOG.info("changed element "+ context.getDelta().getElement());
			final CompilationUnit compilationUnit = delta.getCompilationUnitAST();
			ITypeRoot typeRoot = compilationUnit.getTypeRoot();
			resource = typeRoot.getResource();

			if (lastVerifiedResource != null && lastVerifiedResource.equals(resource.toString())) {
				// avoiding too often invocations
				if (System.currentTimeMillis() - lastVerifiedTime < FileCompletionSettings.minTimeToVerify) {
					return;
				}
			}

			// deleting previously founded markers
			resource.deleteMarkers(markerId, false, IResource.DEPTH_ONE);
			FileClassFinderVerifier fileClassFinderVerifier = new FileClassFinderVerifier();
			compilationUnit.accept(fileClassFinderVerifier);

			List<StringLiteral> foundeFilesNodes = fileClassFinderVerifier.getFoundedFileNodes();

			// validating files
			for (StringLiteral stringLiteral : foundeFilesNodes) {
				String literalValue = stringLiteral.getLiteralValue();
				long startTime = System.currentTimeMillis();
				File file = new File(literalValue);
				LOG.fine("checking file " + file);
				boolean fileExists = file.exists();
				long duration = System.currentTimeMillis() - startTime;
				if (duration > 200) {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("validation file ");
					stringBuilder.append(file.getAbsolutePath());
					stringBuilder.append(" in resource " + resource);
					stringBuilder.append(" took " + duration + " ms");
					LOG.warning(stringBuilder.toString());
				}
				if (!fileExists) {
					createFileMarker(stringLiteral, file, compilationUnit, resource);
				}
			}

			// validating reg exp
			List<StringLiteral> foundedPatternNodes = fileClassFinderVerifier.getFoundedPatternNodes();
			for (StringLiteral stringLiteral : foundedPatternNodes) {
				String literalValue = stringLiteral.getLiteralValue();
				long startTime = System.currentTimeMillis();
				try {
					LOG.info("checking pattern  " + literalValue);
					Pattern.compile(literalValue);
				} catch (Exception e) {
					createPatternMarker(stringLiteral, literalValue, compilationUnit, resource, e.getMessage());
				} finally {
					long duration = System.currentTimeMillis() - startTime;
					if (duration > 200) {
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("validation pattern ");
						stringBuilder.append(literalValue);
						stringBuilder.append(" in resource " + resource);
						stringBuilder.append(" took " + duration + " ms");
						LOG.warning(stringBuilder.toString());
					}
				}

			}
		} catch (Exception e) {
			LOG.log(Level.WARNING, resource + "", e);
		} finally {
			lastVerifiedResource = resource + "";
			lastVerifiedTime = System.currentTimeMillis();
			long duration = System.currentTimeMillis() - reconStartTime;
			if (duration > 200) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append("validation resource ");
				stringBuilder.append(resource);
				stringBuilder.append(" took " + duration + " ms");
				LOG.warning(stringBuilder.toString());
			}
		}
	}

	public void createFileMarker(StringLiteral stringLiteral, File file, CompilationUnit compilationUnit,
			IResource resource) throws CoreException {
		LOG.fine("adding marker to " + resource);
		IMarker marker = resource.createMarker(markerId);
		marker.setAttribute(IMarker.MESSAGE, "File not found " + file.getAbsolutePath());
		marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		marker.setAttribute(IMarker.LINE_NUMBER, compilationUnit.getLineNumber(stringLiteral.getStartPosition()));
		marker.setAttribute(IMarker.CHAR_START, stringLiteral.getStartPosition() + 1);
		marker.setAttribute(IMarker.CHAR_END, stringLiteral.getStartPosition() + stringLiteral.getLength() - 1);
	}

	public void createPatternMarker(StringLiteral stringLiteral, String patteren, CompilationUnit compilationUnit,
			IResource resource, String reasonText) throws CoreException {
		LOG.fine("adding marker to " + resource);
		IMarker marker = resource.createMarker(markerId);
		marker.setAttribute(IMarker.MESSAGE, "Pattern invalid: " + patteren + " , " + reasonText);
		marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		marker.setAttribute(IMarker.LINE_NUMBER, compilationUnit.getLineNumber(stringLiteral.getStartPosition()));
		marker.setAttribute(IMarker.CHAR_START, stringLiteral.getStartPosition() + 1);
		marker.setAttribute(IMarker.CHAR_END, stringLiteral.getStartPosition() + stringLiteral.getLength() - 1);
	}

	@Override
	public boolean isActive(IJavaProject project) {
		return true;
	}

	@Override
	public void buildFinished(IJavaProject project) {
	}

	@Override
	public int aboutToBuild(IJavaProject project) {
		return 1;
	}

	@Override
	public void buildStarting(BuildContext[] files, boolean isBatch) {
	}

	@Override
	public void cleanStarting(IJavaProject project) {
	}

	@Override
	public boolean isAnnotationProcessor() {
		return false;
	}

	@Override
	public void processAnnotations(BuildContext[] files) {
	}

}
