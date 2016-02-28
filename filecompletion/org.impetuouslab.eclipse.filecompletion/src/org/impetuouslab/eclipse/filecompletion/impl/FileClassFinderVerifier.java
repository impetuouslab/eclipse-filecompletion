package org.impetuouslab.eclipse.filecompletion.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * Aux class for finding all file constructors and reg exp paterns
 *
 */
final class FileClassFinderVerifier extends ASTVisitor {
	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(FileClassFinderVerifier.class.getName());
	private List<StringLiteral> foundedFileNodes = new ArrayList<StringLiteral>();
	private List<StringLiteral> foundedPatternNodes = new ArrayList<StringLiteral>();

	FileClassFinderVerifier() {
		super(false);
	}

	@Override
	public boolean visit(StringLiteral node) {
		if (FileClassFinder.isStringNodeInFileElement(node)) {
			foundedFileNodes.add(node);
		} else if (FileClassFinder.isStringNodeInPatternElement(node)) {
			foundedPatternNodes.add(node);
		}
		return true;
	}

	public List<StringLiteral> getFoundedFileNodes() {
		return foundedFileNodes;
	}

	public List<StringLiteral> getFoundedPatternNodes() {
		return foundedPatternNodes;
	}

}