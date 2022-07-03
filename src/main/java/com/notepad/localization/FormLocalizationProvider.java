package com.notepad.localization;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

public class FormLocalizationProvider extends LocalizationProviderBridge{

	public FormLocalizationProvider(ILocalizationProvider parent, JFrame frame) {
		super(parent);
		
		WindowListener listener = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				disconnect();
			}
			
			@Override
			public void windowOpened(WindowEvent e) {
				connect();
			}
		};
		
		frame.addWindowListener(listener);
	}

}
