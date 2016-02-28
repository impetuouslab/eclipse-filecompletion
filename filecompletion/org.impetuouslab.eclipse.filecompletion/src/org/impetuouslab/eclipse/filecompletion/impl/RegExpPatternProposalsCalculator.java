package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class RegExpPatternProposalsCalculator {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(RegExpPatternProposalsCalculator.class.getName());

	public static List<ICompletionProposal> calculateProposals(final StringLiteral stringLiteral,
			final int documentOffset) throws IOException {
		String escapedValue = stringLiteral.getEscapedValue();
		String literalValue = stringLiteral.getLiteralValue();
		LOG.info("literal value = " + literalValue);
		List<ICompletionProposal> sss = new ArrayList<ICompletionProposal>();
		int fromStart = documentOffset - stringLiteral.getStartPosition();
		if (fromStart < 2) {
			LOG.info("fromStart negative : " + fromStart);
			// first char is "
			fromStart = 2;
		}
		if (fromStart >= escapedValue.length()) {
			LOG.info("fromStart more then length : " + fromStart + " >= " + escapedValue.length());
			fromStart = escapedValue.length() - 1;
		}
		LOG.info("position = " + fromStart);
		List<IContentProposal> vvv = RegExpAllProposals.getProposals(escapedValue, fromStart);
		Collections.reverse(vvv);
		int i = 0;
		for (final IContentProposal iContentProposal : vvv) {
			String content = iContentProposal.getContent();
			if (content.length() == 0) {
				continue;
			}
			// getting escaped value of content
			stringLiteral.setLiteralValue(content);
			String content2 = stringLiteral.getEscapedValue();
			content2 = content2.substring(1, content2.length() - 1);
			int diff = content2.length() - content.length();
			LOG.fine(content2);

			ICompletionProposal completionProposal = new RegExpCompletionProposal(content2, documentOffset, 0,
					iContentProposal.getCursorPosition() + diff, null,
					content + " - " + iContentProposal.getDescription(), null, iContentProposal.getDescription(), i++);

			sss.add(completionProposal);
			i++;
		}

		return sss;
	}
}
