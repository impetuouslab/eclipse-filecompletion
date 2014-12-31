package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.StringLiteral;

public class MarkerFinderImpl extends CompilationParticipant {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(FileClassFinder.class.getName());

    // Field value matched with id specified in plugin.xml
    public final static String markerId = "org.impetuouslab.eclipse.filecompletion.FileNotFound";

    @Override
    public void reconcile(final ReconcileContext context) {
        IResource resource=null;
        try {

            final CompilationUnit compilationUnit =context.getDelta().getCompilationUnitAST();
            ITypeRoot typeRoot = compilationUnit.getTypeRoot();
            resource= typeRoot.getResource();
            // deleting previously founded markers
            resource.deleteMarkers(markerId, false, IResource.DEPTH_ONE);
            FileClassFinderVerifier fileClassFinderVerifier = new FileClassFinderVerifier();
            compilationUnit.accept(fileClassFinderVerifier);
            List<StringLiteral> foundeNodes = fileClassFinderVerifier.getFoundeNodes();
            for (StringLiteral stringLiteral : foundeNodes) {
                String literalValue = stringLiteral.getLiteralValue();
                long startTime=System.currentTimeMillis();
                File file = new File(literalValue);
                LOG.fine("checking file " + file);
                boolean fileExists=file.exists();
                long duration=System.currentTimeMillis()-startTime;
                if(duration>200) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("validation file ");
                    stringBuilder.append(file.getAbsolutePath());
                    stringBuilder.append(" in resource "+resource);
                    stringBuilder.append(" took "+duration+" ms");
                    LOG.warning(stringBuilder.toString());
                }
                if (!fileExists) {
                    createMarker(stringLiteral, file, compilationUnit, resource);
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING,resource +"", e );
        }
    }


    public void createMarker(StringLiteral stringLiteral, File file, CompilationUnit compilationUnit,IResource resource) throws CoreException {
        LOG.fine("adding marker to "+resource);
        IMarker marker = resource.createMarker(markerId);
        marker.setAttribute(IMarker.MESSAGE, "File not found " + file.getAbsolutePath());
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

    public int aboutToBuild(IJavaProject project) {
        return 1;
    }

    public void buildStarting(BuildContext[] files, boolean isBatch) {
    }

    public void cleanStarting(IJavaProject project) {
    }

    public boolean isAnnotationProcessor() {
        return false;
    }

    public void processAnnotations(BuildContext[] files) {
    }

}
