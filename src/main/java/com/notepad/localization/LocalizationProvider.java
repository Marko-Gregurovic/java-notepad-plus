package com.notepad.localization;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Singleton class for storing language assets
 * 
 * @author Marko-Gregurovic
 *
 */
public class LocalizationProvider extends AbstractLocalizationProvider{

	private static LocalizationProvider instance = new LocalizationProvider();
	
	private ResourceBundle bundle;
	
	private String language;
	
	private LocalizationProvider() {
		Locale locale = Locale.forLanguageTag("hr");
		this.setLanguage("hr");
		
		bundle = ResourceBundle.getBundle("com.notepad.localization.translations", locale);
	}
	
	@Override
	public String getString(String key) {
		return bundle.getString(key);
	}

	public void setLanguage(String language) {
		this.language = language;
		Locale locale = Locale.forLanguageTag(language);
		bundle = ResourceBundle.getBundle("com.notepad.localization.translations", locale);
		
		fire();
	}
	
	public static LocalizationProvider getInstance() {
		return instance;
	}

	@Override
	public String getCurrentLanguage() {
		return language;
	}
	
	
}
