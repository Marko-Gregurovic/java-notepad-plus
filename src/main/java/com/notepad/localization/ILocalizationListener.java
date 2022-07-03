package com.notepad.localization;

/**
 * 
 * @author Marko-Gregurovic
 *
 */
public interface ILocalizationListener {
	
	/**
	 * Called by the subject when the selected language changed
	 */
	void localizationChanged();
}
