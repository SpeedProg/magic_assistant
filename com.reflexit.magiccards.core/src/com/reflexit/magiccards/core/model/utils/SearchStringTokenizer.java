package com.reflexit.magiccards.core.model.utils;

public class SearchStringTokenizer {
	static enum State {
		INIT,
		IN_QUOTE,
		IN_REG,
		IN_ABI
	}

	public static enum TokenType {
		WORD, // i.e. life
		QUOTED, // i.e. "end of turn"
		REGEX, // i.e. m/sacrifice(d)?/
		NOT, // i.e. -gain
		ABI; // mined ability search (i.e. [removal])
	}

	public static class SearchToken {
		public TokenType getType() {
			return type;
		}

		public String getValue() {
			return value;
		}

		private TokenType type;
		private String value;

		SearchToken(TokenType type, String value) {
			this.type = type;
			this.value = value;
		}
	}

	private CharSequence seq;
	private int cur;
	private SearchStringTokenizer.State state;

	public void init(CharSequence seq) {
		this.seq = seq;
		this.cur = 0;
		this.state = State.INIT;
	}

	boolean tokenReady = false;
	StringBuffer str;
	SearchStringTokenizer.SearchToken token = null;

	public SearchStringTokenizer.SearchToken nextToken() {
		tokenReady = false;
		str = new StringBuffer();
		token = null;
		while (tokenReady == false && cur <= seq.length()) {
			char c = cur < seq.length() ? seq.charAt(cur) : 0;
			switch (state) {
				case INIT:
					switch (c) {
						case '"':
							pushToken(TokenType.WORD);
							state = State.IN_QUOTE;
							break;
						case 'm':
							if (cur + 1 < seq.length() && seq.charAt(cur + 1) == '/') {
								pushToken(TokenType.WORD);
								state = State.IN_REG;
								cur++;
							} else {
								str.append(c);
							}
							break;
						case '[':
							pushToken(TokenType.WORD);
							state = State.IN_ABI;
							break;
						case '-':
							pushToken(TokenType.WORD);
							str.append('-');
							pushToken(TokenType.NOT);
							break;
						case ' ':
						case 0:
							pushToken(TokenType.WORD);
							break;
						default:
							str.append(c);
							break;
					}
					break;
				case IN_ABI:
					if (c == ']' || c == 0) {
						pushToken(TokenType.ABI);
						state = State.INIT;
					} else {
						str.append(c);
					}
					break;
				case IN_REG:
					if (c == '/' || c == 0) {
						pushToken(TokenType.REGEX);
						state = State.INIT;
					} else {
						str.append(c);
					}
					break;
				case IN_QUOTE:
					if (c == '"' || c == 0) {
						pushToken(TokenType.QUOTED);
						state = State.INIT;
					} else {
						str.append(c);
					}
					break;
			}
			cur++;
		}
		return token;
	}

	private void pushToken(TokenType type) {
		if (str.length() > 0) {
			token = new SearchToken(type, str.toString());
			str.delete(0, str.length());
			tokenReady = true;
		}
	}
}