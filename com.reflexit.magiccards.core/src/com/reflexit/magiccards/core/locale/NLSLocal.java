package com.reflexit.magiccards.core.locale;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import com.reflexit.magiccards.core.MagicLogger;

/**
 * Common superclass for all message bundle classes. Provides convenience methods for manipulating
 * messages.
 * <p>
 * The <code>#bind</code> methods perform string substitution and should be considered a convenience and
 * <em>not</em> a full substitute replacement for <code>MessageFormat#format</code> method calls.
 * </p>
 * <p>
 * Text appearing within curly braces in the given message, will be interpreted as a numeric index to the
 * corresponding substitution object in the given array. Calling the <code>#bind</code> methods with text that
 * does not map to an integer will result in an {@link IllegalArgumentException}.
 * </p>
 * <p>
 * Text appearing within single quotes is treated as a literal. A single quote is escaped by a preceeding
 * single quote.
 * </p>
 * <p>
 * Clients who wish to use the full substitution power of the <code>MessageFormat</code> class should call
 * that class directly and not use these <code>#bind</code> methods.
 * </p>
 * <p>
 * Clients may subclass this type.
 * </p>
 * 
 * @since 3.1
 */
public abstract class NLSLocal {
	private static final String EXTENSION = ".properties"; //$NON-NLS-1$
	private static final boolean ignoreWarnings = true;
	static final int SEVERITY_ERROR = 0x04;
	static final int SEVERITY_WARNING = 0x02;
	/*
	 * This object is assigned to the value of a field map to indicate that a translated message has
	 * already been assigned to that field.
	 */
	static final Object ASSIGNED = new Object();

	/**
	 * Creates a new NLS instance.
	 */
	protected NLSLocal() {
		super();
	}

	/**
	 * Initialize the given class with the values from the specified message bundle.
	 * 
	 * @param bundleName
	 *            fully qualified path of the class name
	 * @param clazz
	 *            the class where the constants will exist
	 */
	public static void initializeMessages(final String bundleName, final Object obj, final Locale locale) {
		if (System.getSecurityManager() == null) {
			load(bundleName, obj, locale);
			return;
		}
		AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {
				load(bundleName, obj, locale);
				return null;
			}
		});
	}

	/*
	 * Build an array of property files to search. The returned array contains the property fields
	 * in order from most specific to most generic. So, in the FR_fr locale, it will return
	 * file_fr_FR.properties, then file_fr.properties, and finally file.properties.
	 */
	private static String[] buildVariants(String root, Locale locale) {
		String[] nlSuffixes = buildNlSuffixes(locale);
		root = root.replace('.', '/');
		String[] variants = new String[nlSuffixes.length];
		for (int i = 0; i < variants.length; i++)
			variants[i] = root + nlSuffixes[i];
		return variants;
	}

	static String[] buildNlSuffixes(Locale locale) {
		String[] nlSuffixes = null;
		// build list of suffixes for loading resource bundles
		String nl = locale.toString();
		ArrayList result = new ArrayList(4);
		int lastSeparator;
		while (true) {
			result.add('_' + nl + EXTENSION);
			lastSeparator = nl.lastIndexOf('_');
			if (lastSeparator == -1)
				break;
			nl = nl.substring(0, lastSeparator);
		}
		// add the empty suffix last (most general)
		result.add(EXTENSION);
		nlSuffixes = (String[]) result.toArray(new String[result.size()]);
		return nlSuffixes;
	}

	/*
	 * Load the given resource bundle using the specified class loader.
	 */
	static void load(final String bundleName, Object obj, Locale locale) {
		final Class clazz = (obj instanceof Class) ? ((Class) obj) : obj.getClass();
		long start = System.currentTimeMillis();
		final Field[] fieldArray = clazz.getDeclaredFields();
		ClassLoader loader = clazz.getClassLoader();
		boolean isAccessible = (clazz.getModifiers() & Modifier.PUBLIC) != 0;
		// build a map of field names to CardFieldExpr objects
		final int len = fieldArray.length;
		Map fields = new HashMap(len * 2);
		for (int i = 0; i < len; i++)
			fields.put(fieldArray[i].getName(), fieldArray[i]);
		// search the variants from most specific to most general, since
		// the MessagesProperties.put method will mark assigned fields
		// to prevent them from being assigned twice
		final String[] variants = buildVariants(bundleName, locale);
		for (int i = 0; i < variants.length; i++) {
			// loader==null if we're launched off the Java boot classpath
			final InputStream input = loader == null ? ClassLoader.getSystemResourceAsStream(variants[i])
					: loader
							.getResourceAsStream(variants[i]);
			if (input == null)
				continue;
			try {
				final MessagesProperties properties = new MessagesProperties(fields, bundleName,
						isAccessible, obj);
				properties.load(input);
			} catch (IOException e) {
				log(SEVERITY_ERROR, "Error loading " + variants[i], e); //$NON-NLS-1$
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		// if (Debug.DEBUG_MESSAGE_BUNDLES)
		//System.out.println("Time to load message bundle: " + bundleName + " was " + (System.currentTimeMillis() - start) + "ms."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/*
	 * The method adds a log entry based on the error message and exception. The output is written
	 * to the System.err.
	 * 
	 * This method is only expected to be called if there is a problem in the NLS mechanism. As a
	 * result, translation facility is not available here and messages coming out of this log are
	 * generally not translated.
	 * 
	 * @param severity - severity of the message (SEVERITY_ERROR or SEVERITY_WARNING)
	 * 
	 * @param message - message to log
	 * 
	 * @param e - exception to log
	 */
	private static void log(int severity, String message, Exception e) {
		if (severity == SEVERITY_WARNING && ignoreWarnings)
			return; // ignoring warnings; bug 292980
		String statusMsg;
		switch (severity) {
			case SEVERITY_ERROR:
				statusMsg = "Error: "; //$NON-NLS-1$
				break;
			case SEVERITY_WARNING:
				// intentionally fall through:
			default:
				statusMsg = "Warning: "; //$NON-NLS-1$
		}
		if (message != null)
			statusMsg += message;
		if (e != null)
			statusMsg += ": " + e.getMessage(); //$NON-NLS-1$
		System.err.println(statusMsg);
		if (e != null)
			e.printStackTrace();
	}

	/*
	 * Class which sub-classes java.util.Properties and uses the #put method to set field values
	 * rather than storing the values in the table.
	 */
	private static class MessagesProperties extends Properties {
		private static final int MOD_EXPECTED = Modifier.PUBLIC;
		private static final int MOD_MASK = MOD_EXPECTED | Modifier.FINAL;
		private static final long serialVersionUID = 1L;
		private final String bundleName;
		private final Map fields;
		private final boolean isAccessible;
		private Object obj;

		public MessagesProperties(Map fieldMap, String bundleName, boolean isAccessible, Object obj) {
			super();
			this.fields = fieldMap;
			this.bundleName = bundleName;
			this.isAccessible = isAccessible;
			this.obj = obj;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
		 */
		@Override
		public synchronized Object put(Object key, Object value) {
			Object fieldObject = fields.put(key, ASSIGNED);
			// if already assigned, there is nothing to do
			if (fieldObject == ASSIGNED)
				return null;
			if (fieldObject == null) {
				final String msg = "NLS unused message: " + key + " in: " + bundleName;//$NON-NLS-1$ //$NON-NLS-2$
				MagicLogger.log(msg);
				log(SEVERITY_WARNING, msg, null);
				return null;
			}
			final Field field = (Field) fieldObject;
			setField(value, field);
			return null;
		}

		void setField(Object value, final Field field) {
			// can only set value of public static non-final fields
			if ((field.getModifiers() & MOD_MASK) != MOD_EXPECTED)
				return;
			try {
				// Check to see if we are allowed to modify the field. If we
				// aren't (for instance
				// if the class is not public) then change the accessible
				// attribute of the field
				// before trying to set the value.
				if (!isAccessible)
					field.setAccessible(true);
				// Set the value into the field. We should never get an
				// exception here because
				// we know we have a public static non-final field. If we do get
				// an exception, silently
				// log it and continue. This means that the field will (most
				// likely) be un-initialized and
				// will fail later in the code and if so then we will see both
				// the NPE and this error.
				// Extra care is taken to be sure we create a String with its
				// own backing char[] (bug 287183)
				// This is to ensure we do not keep the key chars in memory.
				field.set(obj, new String(((String) value).toCharArray()));
			} catch (Exception e) {
				log(SEVERITY_ERROR, "Exception setting field value.", e); //$NON-NLS-1$
			}
		}
	}
}