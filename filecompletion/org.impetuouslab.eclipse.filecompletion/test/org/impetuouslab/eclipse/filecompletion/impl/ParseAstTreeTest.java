package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.StringLiteral;

import junit.framework.TestCase;
import net.sf.jremoterun.JrrUtils;
import net.sf.jremoterun.utilities.JrrClassUtils;

public class ParseAstTreeTest extends TestCase {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(JrrClassUtils.getCurrentClass().getName());

	public void test1() throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		File file = new File("test/org/impetuouslab/eclipse/filecompletion/impl/TestSource.java");
		FileInputStream fis = new FileInputStream(file);
		parser.setSource(new String(JrrUtils.readAllBytesFromInpustStream(fis)).toCharArray());
		fis.close();
		org.eclipse.jdt.core.dom.ASTNode astNode = parser.createAST(null);
		FileClassFinderVerifier fileClassFinder = new FileClassFinderVerifier();
		astNode.accept(fileClassFinder);
		{
			String[] needFind = { "needed-leftRegExp1", "needed-leftRegExp2","needed-leftRegExp3","needed-leftRegExp4" };
			Set<String> needFind2 = new HashSet<String>(Arrays.asList(needFind));

			Set<String> founded = new HashSet<String>();
			List<StringLiteral> foundedPatternNodes = fileClassFinder.getFoundedPatternNodes();
			LOG.info("foundedPatternNodes " + foundedPatternNodes.size());
			for (StringLiteral stringLiteral : foundedPatternNodes) {
				String literalValue = stringLiteral.getLiteralValue();
				if (literalValue.startsWith("bad")) {
					fail("Found unwanted : " + literalValue);
				}
				// check don't found element twice
				assertTrue(literalValue, founded.add(literalValue));
			}
			needFind2.removeAll(founded);
			assertSame(0, needFind2.size());
		}
		{
			String[] needFindFiles = { "needed-file1","needed-file2" };
			Set<String> needFindFiles2 = new HashSet<String>(Arrays.asList(needFindFiles));
			Set<String> founded = new HashSet<String>();
			List<StringLiteral> foundedPatternNodes = fileClassFinder.getFoundedFileNodes();
			LOG.info("foundedPatternNodes " + foundedPatternNodes.size());
			for (StringLiteral stringLiteral : foundedPatternNodes) {
				String literalValue = stringLiteral.getLiteralValue();
				if (literalValue.startsWith("bad")) {
					fail("Found unwanted : " + literalValue);
				}
				// check don't found element twice
				assertTrue(literalValue, founded.add(literalValue));
			}
			needFindFiles2.removeAll(founded);
			assertSame(0, needFindFiles2.size());
		}
	}
}
