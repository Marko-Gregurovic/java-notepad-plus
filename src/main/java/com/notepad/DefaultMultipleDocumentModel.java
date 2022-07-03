package com.notepad;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * Contains a collection of {@link SingleDocumentModel}s and displays then.
 * Offers methods for managing this collection.
 * 
 * @author Marko-Gregurovic
 *
 */
public class DefaultMultipleDocumentModel extends JTabbedPane implements MultipleDocumentModel{
	private static final long serialVersionUID = 1L;
	
	private List<MultipleDocumentListener> listeners;
	
	private List<SingleDocumentModel> documentModels;
	
	private List<Path> openedPaths;
	
	private SingleDocumentModel currentModel;
	
	private SingleDocumentModel previousModel;
	
	private ImageIcon unsavedIcon;
	
	private ImageIcon savedIcon;
	
	private List<JScrollPane> listForRemoving;
	
	/**
	 * Creates new {@link DefaultMultipleDocumentModel}. Reads in icons. Adds listener so that current document changes.
	 * 
	 * @throws IOException
	 */
	public DefaultMultipleDocumentModel() throws IOException {
		listeners = new ArrayList<>();
		documentModels = new ArrayList<>();
		openedPaths = new ArrayList<>();
		listForRemoving = new ArrayList<>();
		
		//loading unsaved icon
		try(InputStream is = this.getClass().getResourceAsStream("icons/unsaved.png")){
			if(is == null)
				throw new IOException("Could not unsaved icon");
			
			byte[] bytes = is.readAllBytes();
			
			unsavedIcon = new ImageIcon(bytes);
		}
		
		//loading saved icon
		try(InputStream is = this.getClass().getResourceAsStream("icons/saved.png")){
			if(is == null)
				throw new IOException("Could not unsaved icon");
			
			byte[] bytes = is.readAllBytes();
			
			savedIcon = new ImageIcon(bytes);
		}
		
		//add listener to himself basically to change currentModel
		this.addChangeListener(l -> {
			if(this.getSelectedIndex() == -1) {
				currentModel = null;
			}
			else {
				if(currentModel != documentModels.get(this.getSelectedIndex())) {
					previousModel = currentModel;
					currentModel = documentModels.get(this.getSelectedIndex());
				}
				
				
			}
			this.notifyAllListenersCurrentDocumentChanged();
		});
	}
	
	@Override
	public Iterator<SingleDocumentModel> iterator() {
		return documentModels.iterator();
	}

	/**
	 * Creates new empty {@link DefaultSingleDocumentModel} and adds it to this {@link DefaultMultipleDocumentModel}.
	 * Adds {@link SingleDocumentListener} to this {@link DefaultSingleDocumentModel}.
	 */
	@Override
	public SingleDocumentModel createNewDocument() {
		DefaultSingleDocumentModel newModel = new DefaultSingleDocumentModel(null, "");
		newModel.setModified(true);
		
		documentModels.add(newModel);
		
		//add listener
		newModel.addSingleDocumentListener(documentListener);
		
		listForRemoving.add(new JScrollPane(newModel.getTextComponent()));
		
		//if i am corrent this will prompt the change listener which will set the currentModel
		this.addTab("(unnamed)", unsavedIcon, new JScrollPane(newModel.getTextComponent()), "(unnamed)");
		
		setSelectedIndex(documentModels.size() - 1);
		
		return newModel;
	}

	/**
	 * Returns current active document.
	 */
	@Override
	public SingleDocumentModel getCurrentDocument() {
		return currentModel;
	}

	/**
	 * Reads file from given path and returns {@link DefaultSingleDocumentModel} with text of file on given path. If path does not show a readable file or there was an error reading
	 * returns null.
	 * 
	 * If file is already opened just switches the current pane the pane where that document is opened
	 */
	@Override
	public SingleDocumentModel loadDocument(Path path) {
		if(openedPaths.contains(path)) {
			int index = openedPaths.indexOf(path);
			
			setSelectedIndex(index);
			return documentModels.get(index);
		}
		
		if(!Files.isReadable(path)) {
			return null;
		}
		
		String text;
		try {
			text = Files.readString(path);
		} catch(Exception ex) {
			return null;
		}
		
		SingleDocumentModel newModel = new DefaultSingleDocumentModel(path, text);
		
		documentModels.add(newModel);
		
		//add listener
		newModel.addSingleDocumentListener(documentListener);
		
		openedPaths.add(path);
		
		//currentModel = newModel;
		
		this.addTab(path.getFileName().toString(), savedIcon, new JScrollPane(newModel.getTextComponent()), path.toAbsolutePath().toString());
		
		setSelectedIndex(documentModels.size() - 1);
		
		return newModel;
	}

	/**
	 * Saves model to newPath.
	 */
	@Override
	public void saveDocument(SingleDocumentModel model, Path newPath) {
		if(newPath == null)
			newPath = model.getFilePath();
		
		
		int index = documentModels.indexOf(model);
		
		JTextArea textArea = model.getTextComponent();
		
		byte[] data = textArea.getText().getBytes(StandardCharsets.UTF_8);
		
		try {
			Files.write(newPath, data);
		} 
		catch (IOException exc) {
			//i know IllegalArgumentException is not really appropriate
			throw new IllegalArgumentException();
		}
		
		documentModels.get(index).setModified(false);
		
		//if it was successfull then set the path of document
		model.setFilePath(newPath);
		
	}

	/**
	 * Removes model from internal collection of {@link SingleDocumentModel}s and from JTabbedPane.
	 */
	@Override
	public void closeDocument(SingleDocumentModel model) {
		int index = documentModels.indexOf(model);
		
		if(index == -1)
			throw new IllegalArgumentException("Given model to close does not exist in multiple document model");
		
		documentModels.remove(index);
		this.remove(index);
	}

	@Override
	public void addMultipleDocumentListener(MultipleDocumentListener l) {
		listeners.add(l);
	}

	@Override
	public void removeMultipleDocumentListener(MultipleDocumentListener l) {
		listeners.remove(l);
	}

	@Override
	public int getNumberOfDocuments() {
		return documentModels.size();
	}

	/**
	 * Returns {@link SingleDocumentModel} stored at given index in internal collection.
	 */
	@Override
	public SingleDocumentModel getDocument(int index) {
		return documentModels.get(index);
	}

	/**
	 * Listener for when single document changes.
	 */
	private SingleDocumentListener documentListener = new SingleDocumentListener() {
		
		@Override
		public void documentModifyStatusUpdated(SingleDocumentModel model) {
			boolean status = model.isModified();
			
			//update icon
			int index = DefaultMultipleDocumentModel.this.documentModels.indexOf(model);
			
			ImageIcon icon = status ? DefaultMultipleDocumentModel.this.unsavedIcon : DefaultMultipleDocumentModel.this.savedIcon;
			
			DefaultMultipleDocumentModel.this.setIconAt(index, icon);
		}
		
		@Override
		public void documentFilePathUpdated(SingleDocumentModel model) {
			// update tooltip and title
			int index = DefaultMultipleDocumentModel.this.documentModels.indexOf(model);
			
			DefaultMultipleDocumentModel.this.setTitleAt(index, model.getFilePath().getFileName().toString());
			DefaultMultipleDocumentModel.this.setToolTipTextAt(index, model.getFilePath().toAbsolutePath().toString());
		}
	};
	
	private void notifyAllListenersCurrentDocumentChanged() {
		for(MultipleDocumentListener listener : listeners)
			listener.currentDocumentChanged(previousModel, currentModel);
	}
}
