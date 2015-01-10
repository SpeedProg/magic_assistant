package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class MyOutputStreamWriter {
	private final static Charset UTF_8 = Charset.forName("utf-8");
	private OutputStream st;
	private final int bufSize = 256 * 1024;
	private StringBuilder builder = new StringBuilder(bufSize);

	public MyOutputStreamWriter(File file) throws FileNotFoundException {
		st = new FileOutputStream(file);
	}

	public MyOutputStreamWriter(OutputStream st) {
		this.st = st;
	}

	public void write(String string) throws IOException {
		builder.append(string);
		if (builder.length() > bufSize)
			flush();
	}

	public void write(String a, String b, String c) throws IOException {
		builder.append(a);
		builder.append(b);
		builder.append(c);
		if (builder.length() > bufSize)
			flush();
	}

	public void flush() throws IOException {
		st.write(builder.toString().getBytes(UTF_8));
		builder.delete(0, builder.length());
	}

	public void write(char c) throws IOException {
		builder.append(c);
		if (builder.length() > bufSize)
			flush();
	}

	public void close() throws IOException {
		flush();
		st.close();
	}
}