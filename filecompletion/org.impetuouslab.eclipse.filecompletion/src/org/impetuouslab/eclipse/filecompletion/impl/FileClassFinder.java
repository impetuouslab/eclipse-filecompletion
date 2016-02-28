package org.impetuouslab.eclipse.filecompletion.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

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

	FileClassFinder(int documentOffset) {
		super(false);
		this.documentOffset = documentOffset;
	}

	@Override
	public boolean visit(StringLiteral node) {
		// Verifying that cursor in this string node
		if (node.getStartPosition() < documentOffset && (node.getLength() + node.getStartPosition()) > documentOffset) {
			LOG.info(" found StringLiteral " + node);
			if (isStringNodeInFileElement(node)) {
				foundeNoded = node;
				file = true;
			} else if (isStringNodeInPatternElement(node)) {
				file = false;
				foundeNoded = node;
			}else {
				//LOG.info("seems not a file");
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
			if (parentNewClassCreation.getType().toString().endsWith("File")) {
				List arguments = parentNewClassCreation.arguments();
				if (arguments == null) {
					LOG.warning("arguments is null");
					return false;
				}
				if (arguments.size() == 1) {

					return true;
				} else {
					LOG.fine("processing file constructor with only one parameter, got " + arguments.size());
				}
			} else {
				LOG.fine("not file " + parent);
			}
		} else {
			LOG.fine("not ClassInstanceCreation " + parent.getClass().getName() + " " + parent);
		}
		return false;
	}

	public static boolean isStringNodeInPatternElement(StringLiteral node) {
		ASTNode parent = node.getParent();
		StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
		if (locationInParent.isChildProperty() && !locationInParent.isChildListProperty()) {
			return false;
		}
		if (parent instanceof MethodInvocation) {
			MethodInvocation parentMethodInvocation = (MethodInvocation) parent;
			if (isPatternPart(parentMethodInvocation)) {
				return true;
			} else if (isStringPattern(parentMethodInvocation)) {
				return true;
			} else {

			}
		} else {
			LOG.fine("not ClassInstanceCreation " + parent.getClass().getName() + " " + parent);
		}
		return false;
	}

	/**
	 * Check if it is : Pattern.compile("Regexp");
	 */
	public static boolean isPatternPart(MethodInvocation parentMethodInvocation) {
		if (parentMethodInvocation.getName().toString().equals("compile")
				&& parentMethodInvocation.getExpression().toString().equals("Pattern")) {
			List arguments = parentMethodInvocation.arguments();
			if (arguments == null) {
				LOG.warning("arguments is null");
				return false;
			}
			if (arguments.size() == 1 || arguments.size() == 2) {
				return true;
			} else {
				LOG.fine("processing file constructor with only one parameter, got " + arguments.size());
			}
		} else {
			LOG.fine("not Pattetn regexp " + parentMethodInvocation);
		}
		return false;
	}

	/**
	 * Check if it is string methods : {@link String#matches},
	 * {@link String#replaceFirst(String, String)} ,
	 * {@link String#replaceAll(String, String)}
	 */
	public static boolean isStringPattern(MethodInvocation parentMethodInvocation) {
		String methodName = parentMethodInvocation.getName().toString();
		if (methodName.equals("replaceAll") || methodName.equals("replaceFirst")) {
			List arguments = parentMethodInvocation.arguments();
			if (arguments == null) {
				LOG.warning("arguments is null");
				return false;
			}
			if (arguments.size() == 2) {
				return true;
			} else {
				LOG.fine("processing file constructor with only one parameter, got " + arguments.size());
			}
		} else if (methodName.equals("matches")) {
			List arguments = parentMethodInvocation.arguments();
			if (arguments == null) {
				LOG.warning("arguments is null");
				return false;
			}
			if (arguments.size() == 1) {
				return true;
			}
		} else {
			LOG.fine("not String regexp " + parentMethodInvocation);
		}
		return false;
	}

	public boolean isFile() {
		return file;
	}

}