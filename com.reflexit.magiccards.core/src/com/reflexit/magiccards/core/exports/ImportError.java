package com.reflexit.magiccards.core.exports;

import java.text.MessageFormat;

import com.reflexit.magiccards.core.model.abs.ICardField;

public class ImportError {
	enum Type {
		SET_NOT_FOUND("Set not found"),
		NAME_NOT_FOUND_IN_SET("Name not found in the set"),
		NAME_NOT_FOUND_IN_DB("Name not found in db"),
		NO_DB("No DB"),
		CANNOT_SET_FIELD("Cannot set field {0} to value {1}"),
		// end
		;
		String msg;

		Type(String s) {
			msg = s;
		}
	}

	public static final ImportError SET_NOT_FOUND_ERROR = new ImportError(Type.SET_NOT_FOUND);
	public static final ImportError NAME_NOT_FOUND_IN_SET_ERROR = new ImportError(Type.NAME_NOT_FOUND_IN_SET);
	public static final ImportError NAME_NOT_FOUND_IN_DB_ERROR = new ImportError(Type.NAME_NOT_FOUND_IN_DB);
	public static final ImportError NO_DB_ERROR = new ImportError(Type.NO_DB);
	private String[] arg;
	private ImportError.Type type;

	public ImportError(ImportError.Type type) {
		this.type = type;
	}

	public ImportError(ImportError.Type type, String[] arg) {
		this.type = type;
		this.arg = arg;
	}

	public ImportError.Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return MessageFormat.format(type.msg, (Object[]) arg);
	}

	public static ImportError createFieldNotSetError(ICardField f, Exception e) {
		return new ImportError(Type.CANNOT_SET_FIELD, new String[] { f.toString(), e.getMessage() });
	}
}