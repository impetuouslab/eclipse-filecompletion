package org.impetuouslab.eclipse.filecompletion;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.impetuouslab.eclipse.filecompletion.impl.FileCompletionImpl;

public class FileCompletionProposalComputer implements IJavaCompletionProposalComputer {

    public static IJavaCompletionProposalComputer completionProposalComputer = new FileCompletionImpl();

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(FileCompletionProposalComputer.class.getName());


    public void sessionStarted() {

    }

    public List<ICompletionProposal> computeCompletionProposals(
        ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return completionProposalComputer.computeCompletionProposals(context, monitor);
    }

    public List<IContextInformation> computeContextInformation(
        ContentAssistInvocationContext context, IProgressMonitor monitor) {
        return new ArrayList<IContextInformation>();
    }

    public String getErrorMessage() {
        return null;
    }

    public void sessionEnded() {

    }
}
