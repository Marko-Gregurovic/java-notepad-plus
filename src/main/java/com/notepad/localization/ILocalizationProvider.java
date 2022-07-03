package com.notepad.localization;

/**
 * Defines behaviour of Localizations providers
 * 
 * @author Marko-Gregurovic
 *
 */
public interface ILocalizationProvider {

	void addLocalizationListener(ILocalizationListener listener);
	
	void removeLocalizationListener(ILocalizationListener listener);
	
	/**
	 * Takes key parameter and returns key bound to given key
	 * 
	 * @return returns key bound to given key
	 */
	String getString(String key);
	
	/**
	 * Returns currently set language
	 * 
	 * @return current language
	 */
	String getCurrentLanguage();
	
}
