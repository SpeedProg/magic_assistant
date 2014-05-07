package com.reflexit.magiccards.core.locale;

import java.lang.reflect.Field;
import java.util.Locale;

import com.reflexit.magiccards.core.model.Languages;

public abstract class LocalizedText {
	public static final Locale RUSSIAN = new Locale("ru");
	public static final Locale ENGLISH = Locale.ENGLISH;
	public static final Locale FRENCH = Locale.FRENCH;
	public static final Locale SPANISH = new Locale("es");
	public static final Locale CHINESE_SIMPLIFIED = Locale.SIMPLIFIED_CHINESE;
	public static final Locale CHINESE_TRADITIONAL = Locale.TRADITIONAL_CHINESE;
	public static final Locale PORTUGUESE = new Locale("pt");
	public static final Locale GERMAN = new Locale("de");
	public static final Locale ITALIAN = new Locale("it");
	public static final Locale JAPANESE = Locale.JAPANESE;
	public static final Locale KOREAN = Locale.KOREAN;

	protected LocalizedText(Locale locale) {
		NLSLocal.initializeMessages(getBundleName(), this, locale);
	}

	public abstract String getBundleName();

	public String translate(String fromValue, Locale locale) {
		return translate(fromValue, CardTextLocal.getCardText(locale));
	}

	public String translate(String fromValue, LocalizedText toLocale) {
		Field field = getField(fromValue);
		if (field != null)
			return toLocale.getFieldValue(field.getName());
		return null;
	}

	public Field getField(String fieldValue) {
		Field[] declaredFields = this.getClass().getDeclaredFields();
		for (int i = 0; i < declaredFields.length; i++) {
			Field field = declaredFields[i];
			Object value;
			try {
				value = field.get(this);
			} catch (Exception e) {
				continue;
			}
			if (value instanceof String) {
				String text = (String) value;
				if (text.equals(fieldValue)) {
					return field;
				}
			}
		}
		return null;
	}

	public static Locale getLocale(String language) {
		return Languages.getLocale(language);
	}

	public String getFieldValue(String fieldName) {
		try {
			Field field = this.getClass().getDeclaredField(fieldName);
			Object object = field.get(this);
			return (String) object;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
