package com.reflexit.magiccards.core;

import java.io.IOException;

@SuppressWarnings("serial")
public class OfflineException extends IOException {
	public OfflineException() {
		super("Cannot connect to internet due to 'Work Offline' policy set by user");
	}
}
