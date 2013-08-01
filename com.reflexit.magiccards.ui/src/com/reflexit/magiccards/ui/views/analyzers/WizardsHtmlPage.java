package com.reflexit.magiccards.ui.views.analyzers;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.MagicLogger;
import com.reflexit.magiccards.core.exports.WizardsHtmlExportDelegate;
import com.reflexit.magiccards.core.model.IMagicCard;
import com.reflexit.magiccards.core.model.events.EventManager;
import com.reflexit.magiccards.core.model.storage.IDbCardStore;
import com.reflexit.magiccards.core.model.storage.IFilteredCardStore;

public class WizardsHtmlPage extends AbstractDeckPage {
	private Browser textBrowser;
	private IStructuredSelection selection;
	private ISelectionProvider selProvider = new MySelectionProvider();
	private IFilteredCardStore fstore;

	class MySelectionProvider extends EventManager implements ISelectionProvider {
		@Override
		public void setSelection(ISelection sel) {
			selection = (IStructuredSelection) sel;
			selectionChanged(new SelectionChangedEvent(this, selection));
		}

		private void selectionChanged(final SelectionChangedEvent event) {
			Object[] listeners = getListeners();
			for (Object listener : listeners) {
				ISelectionChangedListener lis = (ISelectionChangedListener) listener;
				try {
					lis.selectionChanged(event);
				} catch (Throwable t) {
					MagicLogger.log(t);
				}
			}
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			removeListenerObject(listener);
		}

		@Override
		public ISelection getSelection() {
			if (selection == null)
				return new StructuredSelection();
			return selection;
		}

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			addListenerObject(listener);
		}
	}

	@Override
	public Composite createContents(Composite parent) {
		super.createContents(parent);
		Composite area = getArea();
		this.textBrowser = new Browser(area, SWT.WRAP | SWT.INHERIT_DEFAULT);
		this.textBrowser.setFont(area.getFont());
		textBrowser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				String location = event.location;
				if (location.startsWith(WizardsHtmlExportDelegate.CARD_URI)) {
					location = location.substring(WizardsHtmlExportDelegate.CARD_URI.length());
					if (location.endsWith("/")) {
						location = location.substring(0, location.length() - 1);
					}
					String params[] = location.split("&");
					for (int i = 0; i < params.length; i++) {
						String string = params[i];
						if (string.startsWith(WizardsHtmlExportDelegate.CARDID)) {
							event.doit = false;
							String value = string.substring(WizardsHtmlExportDelegate.CARDID.length());
							int cardId = Integer.valueOf(value).intValue();
							IDbCardStore magicDBStore = DataManager.getCardHandler().getMagicDBStore();
							IMagicCard card = (IMagicCard) magicDBStore.getCard(cardId);
							if (card != null)
								selProvider.setSelection(new StructuredSelection(card));
						}
					}
					event.doit = false;
				}
			}
		});
		textBrowser.setLayoutData(new GridData(GridData.FILL_BOTH));
		return getArea();
	}

	@Override
	protected ISelectionProvider getSelectionProvider() {
		return selProvider;
	}

	@Override
	public void activate() {
		super.activate();
		this.textBrowser.setText("Loading page...");
		try {
			String text = getHtml();
			// System.err.println(text);
			this.textBrowser.setText(text);
		} catch (InvocationTargetException e) {
			this.textBrowser.setText("Error: " + e.getCause());
		} catch (InterruptedException e) {
			this.textBrowser.setText("Cancelled");
		}
	}

	@Override
	public void setFilteredStore(IFilteredCardStore store) {
		this.fstore = store;
		super.setFilteredStore(store);
	}

	private String getHtml() throws InvocationTargetException, InterruptedException {
		ByteArrayOutputStream byteSt = new ByteArrayOutputStream();
		WizardsHtmlExportDelegate<IMagicCard> ex = new WizardsHtmlExportDelegate<IMagicCard>();
		ex.init(byteSt, false, fstore);
		// ex.setReportType(ReportType.createReportType("html"));
		ex.run(null);
		return byteSt.toString();
	}
}
