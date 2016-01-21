package org.impetuouslab.eclipse.filecompletion.impl;



public class FileCompletionSettings {
	
	
	/**
	 * To avoiding too often invocations,
	 * Time in seconds between verification class file
	 */
	public static final long minTimeToVerify=5000;
	
	
	/**
	 * Maximum file size in bytes, which allows open inside eclipse, and if editor is unknown
	 */
	public static final long maxFileSizeToOpen=1000*1000;

}
