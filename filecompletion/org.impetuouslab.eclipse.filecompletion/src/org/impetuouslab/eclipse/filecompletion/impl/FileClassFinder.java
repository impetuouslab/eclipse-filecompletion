package org.impetuouslab.eclipse.filecompletion.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.*;

final class FileClassFinder extends ASTVisitor {
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(FileClassFinder.class.getName());
    private final int documentOffset;
    private StringLiteral foundeNoded;

    FileClassFinder(boolean visitDocTags, int documentOffset) {
        super(visitDocTags);
        this.documentOffset = documentOffset;
    }

    @Override
    public boolean visit(StringLiteral node) {
        if (node.getStartPosition() < documentOffset && (node.getLength() + node.getStartPosition()) > documentOffset) {
            LOG.info(" found StringLiteral " + node);
            if (isStringNodeInFileElement(node)) {
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
        // may be needed for cases like: new File(f + "C:/Users")
//        if (parent instanceof org.eclipse.jdt.core.dom.InfixExpression) {
//            parent = parent.getParent();
//        }
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
                    LOG.fine("processing file constructor with only one parameter, got "+arguments.size());
                }
            } else {
                LOG.info("not file " + parent);
            }
        } else {
            LOG.fine("not ClassInstanceCreation " + parent.getClass().getName() + " " + parent);
        }
        return false;
    }

}