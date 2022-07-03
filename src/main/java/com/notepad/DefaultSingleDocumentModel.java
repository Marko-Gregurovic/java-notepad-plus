package com.notepad;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class DefaultSingleDocumentModel implements SingleDocumentModel{

	private JTextArea textArea;
	
	private boolean modified;
	
	private Path path;
	
	List<SingleDocumentListener> listeners;
	
	public DefaultSingleDocumentModel(Path path, String content) {
		textArea = new JTextArea();
		textArea.setText(content);
		modified = false;
		this.path = path;
		listeners = new ArrayList<>();
		
		//add listener to JTextArea so when i changes modified state changes
		Document documentModel = textArea.getDocument();
		
		documentModel.addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				DefaultSingleDocumentModel.this.setModified(true);
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				DefaultSingleDocumentModel.this.setModified(true);
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				// DO NOTHING
			}
		});
	}
	
	@Override
	public JTextArea getTextComponent() {
		return textArea;
	}

	@Override
	public Path getFilePath() {
		return path;
	}

	@Override
	public void setFilePath(Path path) {		
		if(this.path == null || !this.path.equals(path)) {
			this.path = path;
			notifyAllListenersOfFilePathUpdate();
		}		
	}

	@Override
	public boolean isModified() {
		return modified;
	}

	@Override
	public void setModified(boolean modified) {
		if(this.modified != modified) {
			this.modified = modified;
			notifyAllListenersOfModifiedStatusUpdate();
		}
	}

	@Override
	public void addSingleDocumentListener(SingleDocumentListener l) {
		listeners.add(l);
	}

	@Override
	public void removeSingleDocumentListener(SingleDocumentListener l) {
		listeners.remove(l);
	}
	
	private void notifyAllListenersOfFilePathUpdate() {
		for(SingleDocumentListener listener : listeners)
			listener.documentFilePathUpdated(this);
	}
	
	private void notifyAllListenersOfModifiedStatusUpdate() {
		for(SingleDocumentListener listener : listeners)
			listener.documentModifyStatusUpdated(this);;
	}

}






























