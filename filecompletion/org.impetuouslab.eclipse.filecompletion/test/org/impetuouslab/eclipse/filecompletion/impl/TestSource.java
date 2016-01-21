package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TestSource {

	
	private void method1() {
		"bad1".replaceAll("needed-leftRegExp1", "test2");
	}
	
	private void method2() throws FileNotFoundException {		
		getClass().getName().concat("bad2").replaceAll("needed-leftRegExp2", "test3");
		new File("needed-file1");
		new FileInputStream("bad3");
		//getClass().getName().concat("bad2").replaceAll("bad3", "test2");
		"bad4".replaceFirst("needed-leftRegExp3", "test4");
		
		"bad5".matches("needed-leftRegExp4");
		
		"bad6".matches("bad7".intern());
		
		new File(
				//
				"needed-file2");
	}
}

