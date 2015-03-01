package com.reflexit.magiccards.core.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

public class MyXMLStreamWriter {
	private MyOutputStreamWriter out;
	private Stack<String> stack;

	public MyXMLStreamWriter(File file) throws FileNotFoundException {
		this.out = new MyOutputStreamWriter(file);
		this.stack = new Stack<String>();
	}

	public MyXMLStreamWriter(OutputStream st) {
		this.out = new MyOutputStreamWriter(st);
		this.stack = new Stack<String>();
	}

	public void startEl(String string) throws XMLStreamException {
		try {
			nl();
			out.write("<", string, ">");
			stack.push(string);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	/**
	 * End of tag with nl
	 *
	 * @throws XMLStreamException
	 */
	public void endEl() throws XMLStreamException {
		try {
			String string = stack.pop();
			nl();
			out.write("</", string, ">");
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void nl() throws XMLStreamException {
		try {
			switch (stack.size()) {
				case 0:
					out.write('\n');
					break;
				case 1:
					out.write("\n  ");
					break;
				case 2:
					out.write("\n    ");
					break;
				case 3:
					out.write("\n      ");
					break;
				case 4:
					out.write("\n        ");
					break;
				default:
					out.write('\n');
					int indent = stack.size();
					for (int i = 0; i < indent; i++) {
						out.write("  ");
					}
					break;
			}
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void el(String elname, String value) throws XMLStreamException {
		try {
			nl();
			out.write("<", elname, ">");
			data(value);
			// end tag no nl
			out.write("</", elname, ">");
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void ela(String elname, String value, String... attrs) throws XMLStreamException {
		startEl(elname, attrs);
		data(value);
		// end tag no nl
		endEli();
	}

	/**
	 * End of tag, no nl
	 *
	 * @throws XMLStreamException
	 */
	public void endEli() throws XMLStreamException {
		try {
			out.write("</", stack.pop(), ">");
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void startEl(String elname, String var, String value) throws XMLStreamException {
		try {
			nl();
			out.write('<');
			out.write(elname);
			out.write(' ');
			writeAtt(var, value);
			out.write('>');
			stack.push(elname);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void startEl(String elname, String... atts) throws XMLStreamException {
		try {
			nl();
			out.write('<');
			out.write(elname);
			for (int i = 0; i < atts.length; i += 2) {
				String var = atts[i];
				String value = atts[i + 1];
				out.write(' ');
				writeAtt(var, value);
			}
			out.write('>');
			stack.push(elname);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void lineEl(String elname, String... atts) throws XMLStreamException {
		try {
			nl();
			out.write('<');
			out.write(elname);
			for (int i = 0; i < atts.length; i += 2) {
				String var = atts[i];
				String value = atts[i + 1];
				out.write(' ');
				writeAtt(var, value);
			}
			out.write('/');
			out.write('>');
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	private void writeAtt(String var, String value) throws IOException {
		out.write(var);
		out.write('=');
		out.write('"');
		out.write(value);
		out.write('"');
	}

	public void data(String string) throws XMLStreamException {
		if (string == null || string.length() == 0)
			return;
		try {
			int i = 0, a = 0;
			int len = string.length();
			while (i < len) {
				char c = string.charAt(i);
				if (c == '&') {
					out.write(string.substring(a, i));
					out.write("&amp;");
					a = i + 1;
				} else if (c == '<') {
					out.write(string.substring(a, i));
					out.write("&lt;");
					a = i + 1;
				} else if (c == '>') {
					out.write(string.substring(a, i));
					out.write("&gt;");
					a = i + 1;
				}
				i++;
			}
			out.write(string.substring(a, i));
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void writeDirect(String string) throws XMLStreamException {
		try {
			int indent = stack.size();
			for (int i = 0; i < indent; i++) {
				out.write("  ");
			}
			out.write(string);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void write(String string) throws XMLStreamException {
		try {
			out.write(string);
		} catch (IOException e) {
			throw new XMLStreamException(e);
		}
	}

	public void close() {
		try {
			out.close();
			out = null;
			stack = null;
		} catch (IOException e) {
			// ignore
			e.printStackTrace();
		}
	}
}