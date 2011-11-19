package com.reflexit.magiccards.core.model.nav;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class LocationPath {
	private IPath path;

	public LocationPath(String path) {
		super();
		this.path = new Path(path);
	}

	public IPath addTrailingSeparator() {
		return path.addTrailingSeparator();
	}

	public LocationPath append(String pat) {
		return new LocationPath(path.append(pat).toPortableString());
	}

	public String getFileExtension() {
		return path.getFileExtension();
	}

	public boolean isAbsolute() {
		return path.isAbsolute();
	}

	public File toFile() {
		return path.toFile();
	}

	@Override
	public String toString() {
		return path.toString();
	}

	public String lastSegment() {
		// TODO Auto-generated method stub
		return path.lastSegment();
	}

	public String toPortableString() {
		// TODO Auto-generated method stub
		return path.toPortableString();
	}

	public String getFirstSegment() {
		String top = path.removeLastSegments(path.segmentCount() - 1).toPortableString();
		return top;
	}

	public LocationPath removeFirstSegments(int i) {
		// TODO Auto-generated method stub
		return new LocationPath(path.removeFirstSegments(i).toPortableString());
	}

	public boolean isEmpty() {
		return path.isEmpty();
	}

	public boolean isRoot() {
		return path.isRoot();
	}

	public IPath getPath() {
		return path;
	}
}
