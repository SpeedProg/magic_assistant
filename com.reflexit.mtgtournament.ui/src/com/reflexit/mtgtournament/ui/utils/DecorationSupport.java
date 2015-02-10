package com.reflexit.mtgtournament.ui.utils;


/**
 * Dependencies:
 * org.eclipse.jface.databinding,
 * org.eclipse.core.databinding,
 * org.eclipse.core.databinding.beans,
 * org.eclipse.core.databinding.property
 *
 * @author elaskavaia
 *
 */
public class DecorationSupport {
	//	
	//	public static interface IMsgValidator {
	//		public String validate(Object obj);
	//	}
	//
	//	public static void bindText(Text text, Object object, String field, IMsgValidator val) {
	//		UpdateValueStrategy strategy = new UpdateValueStrategy();
	//		if (val != null)
	//			strategy.setBeforeSetValidator(new IValidator() {
	//				@Override
	//				public IStatus validate(Object value) {
	//					String message = val.validate(value);
	//					if (message != null) {
	//						return ValidationStatus.error(message);
	//					}
	//					return ValidationStatus.ok();
	//				}
	//			});
	//		/* with text being the port value in your model */
	//		Binding binding = new DataBindingContext().bindValue(
	//				SWTObservables.observeText(text, SWT.Modify),
	//				PojoObservables.observeValue(object, field),
	//				strategy, null);
	//		ControlDecorationSupport.create(binding, SWT.TOP | SWT.LEFT);
	//	}
}
