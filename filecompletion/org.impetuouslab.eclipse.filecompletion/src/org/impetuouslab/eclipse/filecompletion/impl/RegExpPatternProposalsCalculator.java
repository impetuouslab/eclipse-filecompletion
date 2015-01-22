package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;


public class RegExpPatternProposalsCalculator {

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(RegExpPatternProposalsCalculator.class.getName());

	
	public static List<ICompletionProposal> calculateProposals(
			final StringLiteral stringLiteral, final int documentOffset)
			throws IOException {
		String literalValue = stringLiteral.getLiteralValue();
		LOG.info("literal value = " + literalValue);
		List<ICompletionProposal> sss = new ArrayList<ICompletionProposal>();
		FindReplaceDocumentAdapterContentProposalProvider f=new FindReplaceDocumentAdapterContentProposalProvider(true);
		int fromStart = documentOffset - stringLiteral.getStartPosition()-1;
		if(fromStart>literalValue.length()-1) {
			fromStart=literalValue.length();
		}
		LOG.info("position = "+fromStart);
		IContentProposal[] proposals = f.getProposals(literalValue, fromStart);
		LOG.info("proposals count "+ proposals.length);
		for (IContentProposal iContentProposal : proposals) {
			String content = iContentProposal.getContent();
			if(content.length()==0) {
				continue;
			}
			stringLiteral.setLiteralValue(content);
			String content2=stringLiteral.getEscapedValue();
			content2=content2.substring(1, content2.length()-1);
			int diff=content2.length()-content.length();
			LOG.fine(content2);	
			CompletionProposal completionProposal = new CompletionProposal(content2, documentOffset,
					0, iContentProposal.getCursorPosition()+diff,null,content,null,iContentProposal.getDescription());
			sss.add(completionProposal);
		}
		
		return sss;
	}
}
