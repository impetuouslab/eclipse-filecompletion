package org.impetuouslab.eclipse.filecompletion.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

/**
 * Class calculate proposals
 * 
 */
public class FileCompletionImpl implements IJavaCompletionProposalComputer {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(FileCompletionImpl.class.getName());

	public List<ICompletionProposal> computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {

		try {
			LOG.fine("starting proposal");
			int documentOffset = context.getInvocationOffset();
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench()
					.getWorkbenchWindows()[0].getActivePage();
			IEditorPart editorPart = workbenchPage.getActiveEditor();
			IEditorInput editorInput = editorPart.getEditorInput();
			Object adapter = editorInput.getAdapter(IJavaElement.class);
			if (adapter == null) {
				LOG.info("adapter is null");
			} else if (adapter instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit) adapter;
				
				LOG.fine("we found CompilationUnit ");
				try {
					String source = cu.getSource();
					// quick check if cursor now in string, needed to avoid performance impact 
					if (isInStringLiteral(source, documentOffset)) {
						FileClassFinder fileProposals = findStringLiteral(
								source, documentOffset);
						if (fileProposals.getFoundedNode() == null) {
							LOG.fine("looks like not in string literal");
						} else {
							if (fileProposals.isFile()) {
								return FileProposalCalculator.calculateProposalsAndPrintTime(
										fileProposals.getFoundedNode(),
										documentOffset);
							} else {
								return RegExpPatternProposalsCalculator
										.calculateProposals(
												fileProposals.getFoundedNode(),
												documentOffset);
							}
						}
					} else {
						LOG.info("not in string literal");
					}

				} catch (Exception e) {
					LOG.log(Level.SEVERE, null, e);
				}
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, null, e);
		}
		return new ArrayList<ICompletionProposal>();
	}

	/**
	 * Quick detector if cursor now in string
	 */
	public static boolean isInStringLiteral(String source,
			final int documentOffset) throws JavaModelException {
		int endOfLine = source.indexOf('\r', documentOffset);
		endOfLine = Math.min(endOfLine, source.indexOf('\n', documentOffset));
		String s;
		if (endOfLine == -1) {
			s = source.substring(documentOffset);
		} else {
			s = source.substring(documentOffset, endOfLine);
		}
		s = s.replace("\\\\", "/").replace("\\\"", "");
		// if we in string literal, count of " should be odd
		boolean b = false;
		char[] charArray = s.toCharArray();
		for (char c : charArray) {
			if (c == '"') {
				b = !b;
			}
		}
		return b;
	}

	/**
	 * Try to find AST string object, where nor cursor located
	 */
	public static FileClassFinder findStringLiteral(String source,
			final int documentOffset) throws Exception {
		// TODO pass AST correspondent for project
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		org.eclipse.jdt.core.dom.ASTNode astNode = parser.createAST(null);
		FileClassFinder fileClassFinder = new FileClassFinder(
				documentOffset);
		astNode.accept(fileClassFinder);
		return fileClassFinder;
	}

	

	public List<IContextInformation> computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return null;
	}

	public String getErrorMessage() {
		return null;
	}

	public void sessionEnded() {

	}

	public void sessionStarted() {

	}

}
