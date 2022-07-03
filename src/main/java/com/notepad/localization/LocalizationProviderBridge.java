package com.notepad.localization;

/**
 * Meant to act as a bridge from JavaNotepadPlus to LocalizationProvider
 * 
 * @author Marko-Gregurovic
 *
 */
public class LocalizationProviderBridge extends AbstractLocalizationProvider{

	private ILocalizationProvider parent;
	
	private ILocalizationListener listener;
	
	boolean connected;
	
	/**
	 * language used to store language that was set on disconnect
	 */
	String language;
	
	public LocalizationProviderBridge(ILocalizationProvider parent) {
		this.parent = parent;
		connected = false;
		language = parent.getCurrentLanguage();
		listener = new ILocalizationListener() {
			@Override
			public void localizationChanged() {
				fire();
			}
		};
	}
	
	public void disconnect() {
		if(connected)
			parent.removeLocalizationListener(listener);
		connected = false;
	}
	
	public void connect() {
		//i think this will notify when if language was changed while disconnected
		if(!parent.getCurrentLanguage().equals(language)) {
			this.fire();
			language = parent.getCurrentLanguage();
		}
		
		if(!connected)
			parent.addLocalizationListener(listener);
		connected = true;
	}

	@Override
	public String getString(String key) {
		return parent.getString(key);
	}

	@Override
	public String getCurrentLanguage() {
		return parent.getCurrentLanguage();
	}
	
}
