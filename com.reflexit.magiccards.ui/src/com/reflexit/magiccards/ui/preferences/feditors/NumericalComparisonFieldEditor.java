package com.reflexit.magiccards.ui.preferences.feditors;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NumericalComparisonFieldEditor extends FieldEditor {
	/**
	 * Validation strategy constant (value <code>0</code>) indicating that the editor should perform
	 * validation after every key stroke.
	 * 
	 * @see #setValidateStrategy
	 */
	public static final int VALIDATE_ON_KEY_STROKE = 0;
	/**
	 * Validation strategy constant (value <code>1</code>) indicating that the editor should perform
	 * validation only when the text widget loses focus.
	 * 
	 * @see #setValidateStrategy
	 */
	public static final int VALIDATE_ON_FOCUS_LOST = 1;
	/**
	 * Text limit constant (value <code>-1</code>) indicating unlimited text limit and width.
	 */
	public static int UNLIMITED = -1;
	/**
	 * Cached valid state.
	 */
	private boolean isValid;
	/**
	 * Old text value.
	 * 
	 * @since 3.4 this field is protected.
	 */
	protected String oldValue;
	/**
	 * The text field, or <code>null</code> if none.
	 */
	protected Text textField;
	/**
	 * Width of text field in characters; initially unlimited.
	 */
	private int widthInChars = UNLIMITED;
	/**
	 * Text limit of text field in characters; initially unlimited.
	 */
	private int textLimit = UNLIMITED;
	/**
	 * The error message, or <code>null</code> if none.
	 */
	private String errorMessage;
	/**
	 * Indicates whether the empty string is legal; <code>true</code> by default.
	 */
	private boolean emptyStringAllowed = true;
	/**
	 * The validation strategy; <code>VALIDATE_ON_KEY_STROKE</code> by default.
	 */
	private int validateStrategy = VALIDATE_ON_KEY_STROKE;
	private Combo operationControl;

	/**
	 * Creates a new string field editor
	 */
	protected NumericalComparisonFieldEditor() {
	}

	/**
	 * Creates a string field editor. Use the method <code>setTextLimit</code> to limit the text.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param width
	 *            the width of the text input field in characters, or <code>UNLIMITED</code> for no
	 *            limit
	 * @param strategy
	 *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on the fly checking (the
	 *            default), or <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation only after
	 *            the text has been typed in
	 * @param parent
	 *            the parent of the field editor's control
	 * @since 2.0
	 */
	public NumericalComparisonFieldEditor(String name, String labelText, int width, int strategy, Composite parent) {
		init(name, labelText);
		this.widthInChars = width;
		setValidateStrategy(strategy);
		this.isValid = false;
		this.errorMessage = JFaceResources.getString("StringFieldEditor.errorMessage");//$NON-NLS-1$
		createControl(parent);
	}

	public NumericalComparisonFieldEditor(String name, String labelText, Composite parent) {
		this(name, labelText, UNLIMITED, VALIDATE_ON_KEY_STROKE, parent);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = (GridData) this.textField.getLayoutData();
		gd.horizontalSpan = numColumns > 2 ? numColumns - 2 : 1;
		// We only grab excess space if we have to
		// If another field editor has more columns then
		// we assume it is setting the width.
		gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	/**
	 * Checks whether the text input field contains a valid value or not.
	 * 
	 * @return <code>true</code> if the field value is valid, and <code>false</code> if invalid
	 */
	protected boolean checkState() {
		Text text = getTextControl();
		if (text == null) {
			return false;
		}
		String txt = text.getText().trim();
		boolean result;
		if (txt.length() == 0) {
			result = this.emptyStringAllowed;
		} else {
			if (txt.equals("*")) {
				result = true;
			} else {
				try {
					Integer.parseInt(txt);
					result = true;
				} catch (NumberFormatException e1) {
					result = false;
				}
			}
		}
		// call hook for subclasses
		result = result && doCheckState();
		if (result) {
			clearErrorMessage();
		} else {
			showErrorMessage(this.errorMessage);
		}
		return result;
	}

	/**
	 * Hook for subclasses to do specific state checks.
	 * <p>
	 * The default implementation of this framework method does nothing and returns
	 * <code>true</code>. Subclasses should override this method to specific state checks.
	 * </p>
	 * 
	 * @return <code>true</code> if the field value is valid, and <code>false</code> if invalid
	 */
	protected boolean doCheckState() {
		return true;
	}

	/**
	 * Fills this field editor's basic controls into the given parent.
	 * <p>
	 * The string field implementation of this <code>FieldEditor</code> framework method contributes
	 * the text field. Subclasses may override but must call <code>super.doFillIntoGrid</code>.
	 * </p>
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getLabelControl(parent);
		this.operationControl = getOperationControl(parent);
		this.operationControl.setLayoutData(new GridData());
		this.textField = getTextControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns - 1;
		if (this.widthInChars != UNLIMITED) {
			GC gc = new GC(this.textField);
			try {
				Point extent = gc.textExtent("X");//$NON-NLS-1$
				gd.widthHint = this.widthInChars * extent.x;
			} finally {
				gc.dispose();
			}
		} else {
			gd.horizontalAlignment = GridData.FILL;
			gd.grabExcessHorizontalSpace = true;
		}
		this.textField.setLayoutData(gd);
	}

	protected Combo getOperationControl(Composite parent) {
		if (this.operationControl == null) {
			this.operationControl = new Combo(parent, SWT.READ_ONLY);
			this.operationControl.setFont(parent.getFont());
			this.operationControl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					NumericalComparisonFieldEditor.this.operationControl = null;
				}
			});
			this.operationControl.setItems(new String[] { "=", "<=", ">=" });
			this.operationControl.select(2);
		} else {
			checkParent(this.operationControl, parent);
		}
		return this.operationControl;
	}

	public String getOperation() {
		return this.operationControl.getText();
	}

	protected Object[] prefValues(String value) {
		String number;
		int op = 0;
		if (value.startsWith("= ")) {
			number = value.substring(2);
		} else if (value.startsWith("<= ")) {
			number = value.substring(3);
			op = 1;
		} else if (value.startsWith(">= ")) {
			number = value.substring(3);
			op = 2;
		} else if (value.length() == 0) {
			number = "";
		} else {
			op = 2;
			number = "0";
		}
		return new Object[] { number, op };
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doLoad() {
		if (this.textField != null) {
			String value = getPreferenceStore().getString(getPreferenceName());
			Object res[] = prefValues(value);
			String number = (String) res[0];
			int op = (Integer) res[1];
			getTextControl().setText(number);
			this.operationControl.select(op);
			this.oldValue = number;
		}
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doLoadDefault() {
		if (this.textField != null) {
			String value = getPreferenceStore().getDefaultString(getPreferenceName());
			Object res[] = prefValues(value);
			String number = (String) res[0];
			int op = (Integer) res[1];
			getTextControl().setText(number);
			this.operationControl.select(op);
		}
		valueChanged();
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void doStore() {
		String number = getStringValue().trim();
		String op = getOperation();
		String res = op + " " + number;
		getPreferenceStore().setValue(getPreferenceName(), res);
	}

	/**
	 * Returns the error message that will be displayed when and if an error occurs.
	 * 
	 * @return the error message, or <code>null</code> if none
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public int getNumberOfControls() {
		return 3;
	}

	/**
	 * Returns the field editor's value.
	 * 
	 * @return the current value
	 */
	public String getStringValue() {
		if (this.textField != null) {
			return this.textField.getText();
		}
		return getPreferenceStore().getString(getPreferenceName());
	}

	/**
	 * Returns this field editor's text control.
	 * 
	 * @return the text control, or <code>null</code> if no text field is created yet
	 */
	protected Text getTextControl() {
		return this.textField;
	}

	/**
	 * Returns this field editor's text control.
	 * <p>
	 * The control is created if it does not yet exist
	 * </p>
	 * 
	 * @param parent
	 *            the parent
	 * @return the text control
	 */
	public Text getTextControl(Composite parent) {
		if (this.textField == null) {
			this.textField = new Text(parent, SWT.SINGLE | SWT.BORDER);
			this.textField.setFont(parent.getFont());
			switch (this.validateStrategy) {
				case VALIDATE_ON_KEY_STROKE:
					this.textField.addKeyListener(new KeyAdapter() {
						/*
						 * (non-Javadoc)
						 * 
						 * @see
						 * org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.
						 * KeyEvent)
						 */
						@Override
						public void keyReleased(KeyEvent e) {
							valueChanged();
						}
					});
					break;
				case VALIDATE_ON_FOCUS_LOST:
					this.textField.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(KeyEvent e) {
							clearErrorMessage();
						}
					});
					this.textField.addFocusListener(new FocusAdapter() {
						@Override
						public void focusGained(FocusEvent e) {
							refreshValidState();
						}

						@Override
						public void focusLost(FocusEvent e) {
							valueChanged();
							clearErrorMessage();
						}
					});
					break;
				default:
					assert (false);
			}
			this.textField.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					NumericalComparisonFieldEditor.this.textField = null;
				}
			});
			if (this.textLimit > 0) {// Only set limits above 0 - see SWT spec
				this.textField.setTextLimit(this.textLimit);
			}
		} else {
			checkParent(this.textField, parent);
		}
		return this.textField;
	}

	/**
	 * Returns whether an empty string is a valid value.
	 * 
	 * @return <code>true</code> if an empty string is a valid value, and <code>false</code> if an
	 *         empty string is invalid
	 * @see #setEmptyStringAllowed
	 */
	public boolean isEmptyStringAllowed() {
		return this.emptyStringAllowed;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public boolean isValid() {
		return this.isValid;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	protected void refreshValidState() {
		this.isValid = checkState();
	}

	/**
	 * Sets whether the empty string is a valid value or not.
	 * 
	 * @param b
	 *            <code>true</code> if the empty string is allowed, and <code>false</code> if it is
	 *            considered invalid
	 */
	public void setEmptyStringAllowed(boolean b) {
		this.emptyStringAllowed = b;
	}

	/**
	 * Sets the error message that will be displayed when and if an error occurs.
	 * 
	 * @param message
	 *            the error message
	 */
	public void setErrorMessage(String message) {
		this.errorMessage = message;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	@Override
	public void setFocus() {
		if (this.textField != null) {
			this.textField.setFocus();
		}
	}

	/**
	 * Sets this field editor's value.
	 * 
	 * @param value
	 *            the new value, or <code>null</code> meaning the empty string
	 */
	public void setStringValue(String value) {
		if (this.textField != null) {
			if (value == null) {
				value = "";//$NON-NLS-1$
			}
			this.oldValue = this.textField.getText();
			if (!this.oldValue.equals(value)) {
				this.textField.setText(value);
				valueChanged();
			}
		}
	}

	/**
	 * Sets this text field's text limit.
	 * 
	 * @param limit
	 *            the limit on the number of character in the text input field, or
	 *            <code>UNLIMITED</code> for no limit
	 */
	public void setTextLimit(int limit) {
		this.textLimit = limit;
		if (this.textField != null) {
			this.textField.setTextLimit(limit);
		}
	}

	/**
	 * Sets the strategy for validating the text.
	 * <p>
	 * Calling this method has no effect after <code>createPartControl</code> is called. Thus this
	 * method is really only useful for subclasses to call in their constructor. However, it has
	 * public visibility for backward compatibility.
	 * </p>
	 * 
	 * @param value
	 *            either <code>VALIDATE_ON_KEY_STROKE</code> to perform on the fly checking (the
	 *            default), or <code>VALIDATE_ON_FOCUS_LOST</code> to perform validation only after
	 *            the text has been typed in
	 */
	public void setValidateStrategy(int value) {
		assert (value == VALIDATE_ON_FOCUS_LOST || value == VALIDATE_ON_KEY_STROKE);
		this.validateStrategy = value;
	}

	/**
	 * Shows the error message set via <code>setErrorMessage</code>.
	 */
	public void showErrorMessage() {
		showErrorMessage(this.errorMessage);
	}

	/**
	 * Informs this field editor's listener, if it has one, about a change to the value (
	 * <code>VALUE</code> property) provided that the old and new values are different.
	 * <p>
	 * This hook is <em>not</em> called when the text is initialized (or reset to the default value)
	 * from the preference store.
	 * </p>
	 */
	protected void valueChanged() {
		setPresentsDefaultValue(false);
		boolean oldState = this.isValid;
		refreshValidState();
		if (this.isValid != oldState) {
			fireStateChanged(IS_VALID, oldState, this.isValid);
		}
		String newValue = this.textField.getText();
		if (!newValue.equals(this.oldValue)) {
			fireValueChanged(VALUE, this.oldValue, newValue);
			this.oldValue = newValue;
		}
	}

	/*
	 * @see FieldEditor.setEnabled(boolean,Composite).
	 */
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getTextControl(parent).setEnabled(enabled);
		getOperationControl(parent).setEnabled(enabled);
	}
}
