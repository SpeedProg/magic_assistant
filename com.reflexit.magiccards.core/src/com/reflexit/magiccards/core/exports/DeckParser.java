package com.reflexit.magiccards.core.exports;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.reflexit.magiccards.core.model.Editions;
import com.reflexit.magiccards.core.model.ICardField;
import com.reflexit.magiccards.core.model.MagicCardField;
import com.reflexit.magiccards.core.model.MagicCardPhysical;

public class DeckParser implements Closeable {
	private BufferedReader reader;
	private String lineSep;

	public DeckParser(InputStream st) {
		reader = new BufferedReader(new InputStreamReader(st));
		lineSep = System.getProperty("line.separator");
	}

	private LinkedHashMap<Pattern, ICardField[]> patternList = new LinkedHashMap<Pattern, ICardField[]>();
	private ICardField[] currentFields;

	public void addPattern(Pattern p, ICardField fieldsMap[]) {
		patternList.put(p, fieldsMap);
	}

	public ICardField[] getCurrentFields() {
		return currentFields;
	};

	public MagicCardPhysical readLine(MagicCardPhysical res) throws IOException {
		nextline: do {
			String line = reader.readLine();
			if (line == null)
				return null;
			boolean found = false;
			for (Pattern p : patternList.keySet()) {
				Matcher m = p.matcher(line);
				while (m.find()) {
					found = true;
					ICardField[] cardFields = patternList.get(p);
					for (int i = 0; i < cardFields.length; i++) {
						ICardField cardField = cardFields[i];
						try {
							String group = m.group(i + 1);
							if (group != null) {
								if (cardField == MagicCardField.SET) {
									String setName = Editions.getInstance().getNameByAbbr(group);
									if (setName == null)
										setName = group;
									group = setName.trim();
								}
								res.setObjectByField(cardField, group.trim());
							}
						} catch (Exception e) {
							// nothing
						}
					}
					currentFields = cardFields;
					break;
				}
				if (found)
					break;
			}
			if (found)
				break;
		} while (true);
		return res;
	}

	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
