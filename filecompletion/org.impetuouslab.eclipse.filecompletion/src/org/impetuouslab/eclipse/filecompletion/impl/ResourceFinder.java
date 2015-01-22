package org.impetuouslab.eclipse.filecompletion.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ResourceFinder implements IResourceProxyVisitor {

	private static final java.util.logging.Logger LOG = java.util.logging.Logger
			.getLogger(ResourceFinder.class.getName());

	private IResource foundedResource;

	private final File fileToFind;

	private final String absPath;

	public ResourceFinder(File fileToFind) throws IOException {
		super();
		this.fileToFind = fileToFind;
		absPath = getFilePathABS(fileToFind);
	}

	public boolean visit(IResourceProxy proxy) throws CoreException {
		if (foundedResource != null) {
			return false;
		}
		IResource reseource = proxy.requestResource();
		File file;// = resource.getFullPath().toFile();
		IPath location = reseource.getLocation();
		if (location == null) {
			// LOG.info("bad loacation ");
			return true;
		}
		file = location.toFile();
		// .toFile();
		if (file == null) {
			LOG.info("file is null " + reseource);
		} else {
			if (file.getName().equals(fileToFind.getName())) {
				if (file.isFile()) {
					String path;
					try {
						path = getFilePathABS(file);
						LOG.info(absPath);
						if (absPath.equals(path)) {
							LOG.info("found");
							this.foundedResource = reseource;
						}
						LOG.info(path);
					} catch (IOException e) {
						LOG.log(Level.SEVERE, file + "", e);
					}
				} else {
					LOG.info("not a file " + file);
				}
			}
		}
		if (reseource.getType() == IResource.FILE) {
			return false;
		}
		return true;
	}

	private static String getFilePathABS(File fileToFind) throws IOException {
		return fileToFind.getCanonicalPath().replace('\\', '/');
	}

	public IResource getFoundedResource() {
		return foundedResource;
	}
	
}
