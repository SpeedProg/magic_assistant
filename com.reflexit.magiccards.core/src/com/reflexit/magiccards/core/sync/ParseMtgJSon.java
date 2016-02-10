package com.reflexit.magiccards.core.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.core.model.Edition;
import com.reflexit.magiccards.core.model.Editions;

public class ParseMtgJSon {
	private String setUrl = "http://mtgjson.com/json/SetList.json";

	public void parseSets() {
		Editions editions = Editions.getInstance();
		try (InputStream st = WebUtils.openUrl(new URL(setUrl))) {
			BufferedReader br = new BufferedReader(new InputStreamReader(st, FileUtils.CHARSET_UTF_8));
			Object object = new JSONParser().parse(br);
			if (object instanceof JSONArray) {
				JSONArray arr = (JSONArray) object;
				for (Object seto : arr) {
					JSONObject set = (JSONObject) seto;
					String name = (String) set.get("name");
					Edition ed = editions.getEditionByName(name);
					if (ed == null)
						System.err.println("Set not found " + seto);
					else {
						String code = (String) set.get("code");
						String reldate = (String) set.get("releaseDate");
						if (!ed.getMainAbbreviation().equals(code))
							System.err.println("Code not matching " + ed.getMainAbbreviation() + " " + set);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ParseMtgJSon().parseSets();
	}
}
