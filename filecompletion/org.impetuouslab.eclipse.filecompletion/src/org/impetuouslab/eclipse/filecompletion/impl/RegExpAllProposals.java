package org.impetuouslab.eclipse.filecompletion.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;

public class RegExpAllProposals {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(RegExpAllProposals.class.getName());

	public static final Map<String, IContentProposal> allProposals = new TreeMap();

	public static final Map<Character, List<IContentProposal>> proposalsByFirstChar = new TreeMap();

	static {
		final IContentProposal[] contentProposals = new FindReplaceDocumentAdapterContentProposalProvider(true)
				.getProposals("", 0);
		for (final IContentProposal proposal : contentProposals) {
			String content = proposal.getContent();
			if (content != null && content.length() != 0) {
				allProposals.put(content, proposal);
			}
		}
		for (final Entry<String, IContentProposal> entry : allProposals.entrySet()) {
			final Character c = entry.getKey().charAt(0);
			List<IContentProposal> set = proposalsByFirstChar.get(c);
			if (set == null) {
				set = new ArrayList<IContentProposal>();
				proposalsByFirstChar.put(c, set);
			}
			set.add(entry.getValue());
		}
		LOG.fine("init done : " + allProposals);
		LOG.fine("init done : " + proposalsByFirstChar.keySet());
	}

	/**
	 * Get proposal for i-1 value. For string "1*la123" with fromStart = 3
	 * returns proposals *
	 */
	public static List<IContentProposal> getProposals(String literalValue, int fromStart) {
		Character charr;
		if (literalValue.length() == 0) {
			charr = null;
		} else if (fromStart == 0) {
			charr = literalValue.charAt(1);
		} else {
			charr = literalValue.charAt(fromStart - 1);
		}
		boolean foundProposalForChar = false;
		ArrayList<IContentProposal> e = new ArrayList<IContentProposal>(allProposals.values());
		List<IContentProposal> vvv = new ArrayList<IContentProposal>();
		if (charr != null) {
			List<IContentProposal> list = proposalsByFirstChar.get(charr);
			if (list != null) {
				vvv.addAll(list);
				e.removeAll(list);
				foundProposalForChar = true;
			}
		}
		if (foundProposalForChar) {
			LOG.fine("found proposals for : " + charr);
		} else {
			LOG.fine("not found proposals for : " + charr);
		}
		vvv.addAll(e);
		return vvv;
	}

}
