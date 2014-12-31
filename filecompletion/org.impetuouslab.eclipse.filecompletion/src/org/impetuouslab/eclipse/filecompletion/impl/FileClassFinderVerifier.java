package org.impetuouslab.eclipse.filecompletion.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

final class FileClassFinderVerifier extends ASTVisitor {
    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(FileClassFinderVerifier.class.getName());
    private List<StringLiteral> foundeNodes=new ArrayList<StringLiteral>();

    FileClassFinderVerifier() {
        super(false);
    }

    @Override
    public boolean visit(StringLiteral node) {
        if (FileClassFinder.isStringNodeInFileElement(node)) {
            foundeNodes.add(node);
        }
        return true;
    }

    public List<StringLiteral> getFoundeNodes() {
        return foundeNodes;
    }

}