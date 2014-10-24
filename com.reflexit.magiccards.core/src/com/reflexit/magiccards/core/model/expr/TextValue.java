package com.reflexit.magiccards.core.model.expr;

import java.util.regex.Pattern;

public class TextValue extends Value {
	public boolean wordBoundary = true;
	public boolean caseSensitive = false;
	public boolean regex = false;
	public Pattern pattern;

	public TextValue(String name, boolean wordBoundary, boolean caseSensitive, boolean regex) {
		super(name);
		this.wordBoundary = wordBoundary;
		this.caseSensitive = caseSensitive;
		this.regex = regex;
	}

	public TextValue(Pattern pattern) {
		super(pattern.toString());
		this.wordBoundary = false;
		this.caseSensitive = false;
		this.regex = true;
		this.pattern = pattern;
	}

	public void setWordBoundary(boolean b) {
		this.wordBoundary = b;
	}

	public Pattern getPattern() {
		if (pattern == null) {
			pattern = toPattern();
		}
		return pattern;
	}

	private Pattern toPattern() {
		if (regex)
			return Pattern.compile(name());
		int flags = 0;
		if (!caseSensitive)
			flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
		if (wordBoundary)
			return Pattern.compile("\\b\\Q" + name() + "\\E\\b", flags);
		flags |= Pattern.LITERAL;
		return Pattern.compile(name(), flags);
	}

	public String getText() {
		return name();
	}
}