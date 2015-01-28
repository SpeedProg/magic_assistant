package com.reflexit.magiccards.core.exports;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.MagicCardPhysical;
import com.reflexit.magiccards.core.model.abs.ICardField;

public class DeckParser implements Closeable {
	private BufferedReader reader;
	private IImportDelegate delegate;
	private LinkedHashMap<Pattern, Object> patternList = new LinkedHashMap<Pattern, Object>();
	protected String state;
	private ICardField[] reqFields;

	public DeckParser(InputStream st, IImportDelegate delegate) {
		reader = new BufferedReader(new InputStreamReader(st));
		this.delegate = delegate;
	}

	public void addPattern(Pattern p, ICardField fieldsMap[]) {
		patternList.put(p, fieldsMap);
		setRequiredFeilds(fieldsMap);
	}

	public void setRequiredFeilds(ICardField[] fieldsMap) {
		reqFields = fieldsMap;
	}

	public void addPattern(Pattern p, String state) {
		patternList.put(p, state);
	}

	public ICardField[] getFields() {
		return reqFields;
	}

	public String readLine() throws IOException {
		return reader.readLine();
	}

	public MagicCardPhysical readLine(MagicCardPhysical res) throws IOException {
		do {
			String line = reader.readLine();
			if (line == null)
				return null;
			boolean found = parseLine(res, line);
			if (found)
				break;
		} while (true);
		return res;
	}

	public boolean parseLine(MagicCardPhysical res, String line) {
		boolean found = false;
		for (Pattern p : patternList.keySet()) {
			Matcher m = p.matcher(line);
			while (m.find()) {
				found = true;
				Object what = patternList.get(p);
				if (what instanceof ICardField[]) {
					ICardField[] cardFields = (ICardField[]) what;
					for (int i = 0; i < cardFields.length; i++) {
						ICardField cardField = cardFields[i];
						try {
							String group = m.group(i + 1);
							if (group != null) {
								if (delegate != null) {
									delegate.setFieldValue(res, cardField, i, group.trim());
								} else {
									res.set(cardField, group.trim());
								}
							}
						} catch (Exception e) {
							// nothing
						}
					}
					break;
				} else if (what instanceof String) {
					this.state = (String) what;
				} else if (what == null) {
					// skip - comment
				}
			}
			if (found)
				break;
		}
		return found;
	}

	@Override
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
