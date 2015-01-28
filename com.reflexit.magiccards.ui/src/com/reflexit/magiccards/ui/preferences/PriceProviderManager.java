package com.reflexit.magiccards.ui.preferences;

import java.util.Collection;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

import com.reflexit.magiccards.core.DataManager;
import com.reflexit.magiccards.core.seller.IPriceProvider;
import com.reflexit.magiccards.core.seller.IPriceProviderStore;
import com.reflexit.magiccards.ui.MagicUIActivator;

public class PriceProviderManager implements IPropertyChangeListener {
	static private final PriceProviderManager instance = new PriceProviderManager();

	public static final PriceProviderManager getInstance() {
		return instance;
	}

	public String getProviderName() {
		String name = MagicUIActivator.getDefault().getPreferenceStore()
				.getString(PreferenceConstants.PRICE_PROVIDER);
		return name;
	}

	public void setProviderName(String name) {
		MagicUIActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.PRICE_PROVIDER, name);
	}

	@Override
	public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
		String property = event.getProperty();
		Object newValue = event.getNewValue();
		if (property.equals(PreferenceConstants.PRICE_PROVIDER)) {
			if (newValue != null && !newValue.equals(event.getOldValue())) {
				DataManager.getDBPriceStore().setProviderByName((String) newValue);
			}
		}
	}

	public void sync(IPreferenceStore preferenceStore) {
		String providerName = getProviderName();
		if (providerName != null)
			DataManager.getDBPriceStore().setProviderByName(providerName);
		preferenceStore.addPropertyChangeListener(this);
	}

	public Collection<IPriceProvider> getProviders() {
		return DataManager.getDBPriceStore().getProviders();
	}

	public IPriceProviderStore getDefaultProvider() {
		return getProviders().iterator().next();
	}
}
