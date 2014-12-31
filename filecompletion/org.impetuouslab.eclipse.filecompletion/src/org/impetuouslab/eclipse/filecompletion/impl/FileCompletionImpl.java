package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class FileCompletionImpl implements IJavaCompletionProposalComputer {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(FileCompletionImpl.class.getName());

	public List<ICompletionProposal> computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {

		try {
			LOG.fine("stating proposal");
			int documentOffset = context.getInvocationOffset();
			IWorkbenchPage workbenchPage = PlatformUI.getWorkbench()
					.getWorkbenchWindows()[0].getActivePage();
			IEditorPart compilationUnit = workbenchPage.getActiveEditor();
			IEditorInput editorInput = compilationUnit.getEditorInput();
			Object adapter = editorInput.getAdapter(IJavaElement.class);
			if (adapter == null) {
				LOG.info("adapter is null");
			} else if (adapter instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit) adapter;
				LOG.fine("we found CompilationUnit ");

				try {
					String source = cu.getSource();
					if (isInStringLiteral(source, documentOffset)) {
						StringLiteral fileProposals = findStringLiteral(source,
								documentOffset);
						if (fileProposals == null) {
							LOG.fine("looks like not in string literal");
						} else {

							List<ICompletionProposal> proposals = calculateProposalsAndPrintTime(
									fileProposals, documentOffset);

							return proposals;
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

	public static StringLiteral findStringLiteral(String source,
			final int documentOffset) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		org.eclipse.jdt.core.dom.CompilationUnit astNode = (org.eclipse.jdt.core.dom.CompilationUnit) parser
				.createAST(null);
		FileClassFinder fileClassFinder = new FileClassFinder(false,
				documentOffset);
		astNode.accept(fileClassFinder);
		return fileClassFinder.getFoundedNode();
	}

	static List<ICompletionProposal> calculateProposalsAndPrintTime(final StringLiteral stringLiteral, final int documentOffset) throws IOException{
	    long start = System.currentTimeMillis();
	    List<ICompletionProposal> completionProposals=calculateProposals(stringLiteral, documentOffset);
	    start = System.currentTimeMillis() - start;
        start = start / 1000;
        if (start > 3) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("proposal calculation took ");
            stringBuilder.append(start);
            stringBuilder.append(" sec for ");
            stringBuilder.append(stringLiteral.getLiteralValue());
            LOG.info(stringBuilder.toString());
        }
	    return completionProposals;
	}

	static List<ICompletionProposal> calculateProposals(
			final StringLiteral stringLiteral, final int documentOffset)
			throws IOException {
		LOG.info("literal value = " + stringLiteral.getLiteralValue());
		List<ICompletionProposal> sss = new ArrayList<ICompletionProposal>();
		if (stringLiteral.getLiteralValue().trim().isEmpty()) {
			sss = calculateProposalsForEmptyString(stringLiteral,
					documentOffset);
		} else {
			int fromStart = documentOffset - stringLiteral.getStartPosition();
			String pathFromStartLiteralToCursor = stringLiteral
					.getEscapedValue().substring(1, fromStart);
			pathFromStartLiteralToCursor = pathFromStartLiteralToCursor
					.replace("\"", "").replace("\\\\", "/");
			LOG.info(pathFromStartLiteralToCursor);
			String pathFromDocumentOfficetToEnd = stringLiteral
					.getEscapedValue().substring(fromStart);
			LOG.info("pathFromDocumentOfficetToEnd "
					+ pathFromDocumentOfficetToEnd);
			pathFromDocumentOfficetToEnd = pathFromDocumentOfficetToEnd
					.replace("\"", "").replace("\\\\", "/");
			int nextShalsh = pathFromDocumentOfficetToEnd.indexOf("/");
			File file = new File(pathFromStartLiteralToCursor);
			if (pathFromStartLiteralToCursor.endsWith("/")) {
				if (file.exists() && file.isDirectory()) {
					File[] files = file.listFiles();
					if (files == null) {
						LOG.info("can't list files");
					} else {
						sss = calculateProposalsWithSlash(stringLiteral,
								documentOffset, files, fromStart, nextShalsh);
					}
				}
			} else {
				int lastShalsh = pathFromStartLiteralToCursor.lastIndexOf('/');
				// want to get rid just of string
				if (lastShalsh != -1) {
					String dirrr = pathFromStartLiteralToCursor.substring(0,
							lastShalsh + 1);
					String rest = pathFromStartLiteralToCursor.substring(
							lastShalsh + 1).toLowerCase();
					LOG.info("dirr = " + dirrr);
					LOG.info("rest = " + rest);
					File file2 = new File(dirrr);
					LOG.info("abs path " + file2.getAbsolutePath());
					if (file2.exists() && file2.isDirectory()) {
						sss = calculateProposalsWithNotEndSlash(stringLiteral,
								documentOffset, file2.listFiles(), fromStart,
								nextShalsh, rest);
					} else {
						LOG.info("not exists " + file2);
					}
				}
			}
		}
		LOG.info("proposal count "+sss.size());
		Collections.sort(sss, new Comparator<ICompletionProposal>() {
			public int compare(ICompletionProposal o1, ICompletionProposal o2) {
				return o1.getDisplayString().compareToIgnoreCase(
						o2.getDisplayString());
			}
		});

		return sss;
	}

	static List<ICompletionProposal> calculateProposalsForEmptyString(
			final StringLiteral stringLiteral, final int documentOffset)
			throws IOException {
		List<ICompletionProposal> sss = new ArrayList<ICompletionProposal>();
		File[] listRoots = File.listRoots();
		for (File file2 : listRoots) {
			long startInLoop = System.currentTimeMillis();
			String string = file2.getAbsolutePath();
			string = string.replace("\\", "/");
			sss.add(new CompletionProposal(string, stringLiteral
					.getStartPosition() + 1, stringLiteral.getLiteralValue()
					.length(), string.length()));
			startInLoop = System.currentTimeMillis() - startInLoop;
			startInLoop = startInLoop / 1000;
			if (startInLoop > 2) {
				LOG.info("listing take too much time " + startInLoop + " "
						+ file2.getAbsolutePath());
			}
		}
		LOG.info("returning roots " + sss);
		return sss;
	}

	static List<ICompletionProposal> calculateProposalsWithNotEndSlash(
			final StringLiteral stringLiteral, final int documentOffset,
			File[] files, int fromStart, int nextShalsh, String rest) {
		List<ICompletionProposal> sss = new ArrayList<ICompletionProposal>();
		for (File file3 : files) {
			long startInLoop = System.currentTimeMillis();
			String fileName = file3.getName();
			if (fileName.toLowerCase().startsWith(rest)) {
				if (file3.isDirectory()) {
					fileName += "/";
				}
				int replacementLength;
				if (file3.isFile()) {
					replacementLength = stringLiteral.getLength() - fromStart
							+ rest.length() - 1;
				} else {
					if (nextShalsh == -1) {
						replacementLength = stringLiteral.getLength()
								- fromStart + rest.length() - 1;
					} else {
						replacementLength = rest.length() + nextShalsh + 1;
					}
				}
				LOG.info(stringLiteral.getLength() + " " + replacementLength
						+ " " + rest + " " + fromStart);
				sss.add(new CompletionProposal(fileName, documentOffset
						- rest.length(), replacementLength, fileName.length()));
			}
			startInLoop = System.currentTimeMillis() - startInLoop;
			startInLoop = startInLoop / 1000;
			if (startInLoop > 2) {
				LOG.info("Listing take too much time " + startInLoop + " "
						+ file3.getAbsolutePath());
			}
		}
		return sss;

	}

	static List<ICompletionProposal> calculateProposalsWithSlash(
			final StringLiteral stringLiteral, final int documentOffset,
			File[] files, int fromStart, int nextShalsh) {
		LOG.info(files + "");
		List<ICompletionProposal> sss = new ArrayList<ICompletionProposal>();
		for (File file2 : files) {
			long startInLoop = System.currentTimeMillis();
			String string = file2.getName();
			if (file2.isDirectory()) {
				string += "/";
			}
			int replacementLength;
			if (file2.isFile()) {
				replacementLength = stringLiteral.getLength() - fromStart - 1;
			} else {
				if (nextShalsh == -1) {
					replacementLength = stringLiteral.getLength() - fromStart
							- 1;
				} else {
					replacementLength = nextShalsh + 1;
				}
			}
			sss.add(new CompletionProposal(string, documentOffset,
					replacementLength, string.length()));
			startInLoop = System.currentTimeMillis() - startInLoop;
			startInLoop = startInLoop / 1000;
			if (startInLoop > 2) {
				LOG.info("listing take too much time " + startInLoop + " "
						+ file2.getAbsolutePath());
			}
		}
		return sss;
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
