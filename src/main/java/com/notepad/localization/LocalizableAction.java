package com.notepad.localization;

import javax.swing.AbstractAction;
import javax.swing.Action;

public abstract class LocalizableAction extends AbstractAction{
	private static final long serialVersionUID = 1L;
	
	String key;
	
	public LocalizableAction(String key, ILocalizationProvider lp) {	
		this.key = key;
		
		putValue(Action.NAME, lp.getString(key));
		
		lp.addLocalizationListener(() -> {
			putValue(Action.NAME, lp.getString(key));
		});
	}
}
