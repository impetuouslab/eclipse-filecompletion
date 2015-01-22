package org.impetuouslab.eclipse.filecompletion.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.*;

/**
 * Aux class to define if cursor now in File constructor and regexp patterns
 * 
 */
final class FileClassFinder extends ASTVisitor {
	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(FileClassFinder.class.getName());

	private final int documentOffset;

	/**
	 * Reference to string object, where now cusror located
	 */
	private StringLiteral foundeNoded;

	/**
	 * Identify if now cursor in file constructor or regexp patern
	 */
	private boolean file = true;

	FileClassFinder( int documentOffset) {
		super(false);
		this.documentOffset = documentOffset;
	}

	@Override
	public boolean visit(StringLiteral node) {
		// Verifying that cursor in this string node
		if (node.getStartPosition() < documentOffset
				&& (node.getLength() + node.getStartPosition()) > documentOffset) {
			LOG.info(" found StringLiteral " + node);
			if (isStringNodeInFileElement(node)) {
				foundeNoded = node;
				file = true;
			} else if (isStringNodeInPatternElement(node)) {
				file = false;
				foundeNoded = node;
			}
		}
		return true;
	}

	public StringLiteral getFoundedNode() {
		return foundeNoded;
	}

	public static boolean isStringNodeInFileElement(StringLiteral node) {
		ASTNode parent = node.getParent();
		if (parent instanceof org.eclipse.jdt.core.dom.ClassInstanceCreation) {
			org.eclipse.jdt.core.dom.ClassInstanceCreation parentNewClassCreation = (org.eclipse.jdt.core.dom.ClassInstanceCreation) parent;
			if (parentNewClassCreation.getType().toString().contains("File")) {
				List arguments = parentNewClassCreation.arguments();
				if (arguments == null) {
					LOG.warning("arguments is null");
					return false;
				}
				if (arguments.size() == 1) {

					return true;
				} else {
					LOG.fine("processing file constructor with only one parameter, got "
							+ arguments.size());
				}
			} else {
				LOG.info("not file " + parent);
			}
		} else {
			LOG.fine("not ClassInstanceCreation " + parent.getClass().getName()
					+ " " + parent);
		}
		return false;
	}

	public static boolean isStringNodeInPatternElement(StringLiteral node) {
		ASTNode parent = node.getParent();
		if (parent instanceof org.eclipse.jdt.core.dom.MethodInvocation) {
			org.eclipse.jdt.core.dom.MethodInvocation parentMethodInvocation = (org.eclipse.jdt.core.dom.MethodInvocation) parent;
			if (parentMethodInvocation.getName().toString().equals("compile")
					&& parentMethodInvocation.getExpression().toString()
							.equals("Pattern")) {
				List arguments = parentMethodInvocation.arguments();
				if (arguments == null) {
					LOG.warning("arguments is null");
					return false;
				}
				if (arguments.size() == 1 || arguments.size() == 2) {

					return true;
				} else {
					LOG.fine("processing file constructor with only one parameter, got "
							+ arguments.size());
				}
			} else {
				LOG.info("not file " + parent);
			}
		} else {
			LOG.fine("not ClassInstanceCreation " + parent.getClass().getName()
					+ " " + parent);
		}
		return false;
	}

	public boolean isFile() {
		return file;
	}

}