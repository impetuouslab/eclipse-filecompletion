package org.impetuouslab.eclipse.filecompletion.impl;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.StringLiteral;

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
        if (node.getStartPosition() < documentOffset
            && (node.getLength() + node.getStartPosition()) > documentOffset) {
            LOG.info(" found StringLiteral " + node);
            ASTNode parent = node.getParent();
            if (parent instanceof org.eclipse.jdt.core.dom.InfixExpression) {
                parent = parent.getParent();
            }
            if (parent instanceof org.eclipse.jdt.core.dom.ClassInstanceCreation) {
                org.eclipse.jdt.core.dom.ClassInstanceCreation new_name = (org.eclipse.jdt.core.dom.ClassInstanceCreation) parent;
                if (new_name.getType().toString().contains("File")) {
                    foundeNoded = node;
                } else {
                    LOG.info("not file " + parent);
                }
            } else {
                LOG.info("not ClassInstanceCreation "
                    + parent.getClass().getName() + " "
                    + parent);
            }
        }
        return true;

    }

    public StringLiteral getFoundedNode() {
        return foundeNoded;
    }
}