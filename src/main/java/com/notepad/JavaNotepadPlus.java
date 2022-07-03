package com.notepad;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.CollationKey;
import java.text.Collator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

import com.notepad.localization.FormLocalizationProvider;
import com.notepad.localization.LocalizableAction;
import com.notepad.localization.LocalizationProvider;

/**
 * Java Notepad Plus main program.
 * 
 * @author Marko-Gregurovic
 *
 */
public class JavaNotepadPlus extends JFrame {
	private static final long serialVersionUID = 1L;

	private DefaultMultipleDocumentModel documentsModel;

	private FormLocalizationProvider flp = new FormLocalizationProvider(LocalizationProvider.getInstance(), this);
	
	private String savedString;
	
	private Timer timer;

	public JavaNotepadPlus() throws IOException {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("Java Notepad Plus");
		documentsModel = new DefaultMultipleDocumentModel();
		savedString = "";

		initGUI();

		WindowListener wl = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exitAction.actionPerformed(null);
			}
		};

		
		//add listener to multiple document to change title
		documentsModel.addMultipleDocumentListener(new MultipleDocumentListener() {
			
			@Override
			public void documentRemoved(SingleDocumentModel model) {
				// DO NOTHING
				
			}
			
			@Override
			public void documentAdded(SingleDocumentModel model) {
				// DO NOTHING
				
			}
			
			@Override
			public void currentDocumentChanged(SingleDocumentModel previousModel, SingleDocumentModel currentModel) {
				//unsubscribe previous model carot and subscribe new one
				if(previousModel != null) {
					previousModel.getTextComponent().getCaret().removeChangeListener(changeListener);
					previousModel.getTextComponent().getDocument().removeDocumentListener(documentListener);
				}
					
				
				if(currentModel != null) {
					saveAction.setEnabled(true);
					saveAsAction.setEnabled(true);
					pasteAction.setEnabled(true);
					closeAction.setEnabled(true);
					cutAction.setEnabled(false);
					copyAction.setEnabled(false);
					deleteAction.setEnabled(false);
					toUpperAction.setEnabled(false);
					toLowerAction.setEnabled(false);
					invertCaseAction.setEnabled(false);
					ascendingAction.setEnabled(false);
					descendingAction.setEnabled(false);
					uniqueAction.setEnabled(false);
					currentModel.getTextComponent().getCaret().addChangeListener(changeListener);
					currentModel.getTextComponent().setCaretPosition(0);
					
					//attach listener for length
					currentModel.getTextComponent().getDocument().addDocumentListener(documentListener);
					//update length
					lengthStatusAction.actionPerformed(null);
					otherStatusAction.actionPerformed(null);
				}
				else {
					//update length
					lengthStatusAction.actionPerformed(null);
					otherStatusAction.actionPerformed(null);
					
					cutAction.setEnabled(false);
					copyAction.setEnabled(false);
					deleteAction.setEnabled(false);
					pasteAction.setEnabled(false);
					saveAction.setEnabled(false);
					saveAsAction.setEnabled(false);
					closeAction.setEnabled(false);
					toUpperAction.setEnabled(false);
					toLowerAction.setEnabled(false);
					invertCaseAction.setEnabled(false);
					ascendingAction.setEnabled(false);
					descendingAction.setEnabled(false);
					uniqueAction.setEnabled(false);
				}
					
				
				
				
				updateTitle();
			}

		});
		
		
		this.addWindowListener(wl);

		setSize(800, 500);
		// pack();
		setLocationRelativeTo(null);
	}

	/**
	 * Initializes the GUI
	 */
	private void initGUI() {
		getContentPane().setLayout(new BorderLayout());

		createMenus();
		createToolbar();
		setActionProperties();
		
		cutAction.setEnabled(false);
		copyAction.setEnabled(false);
		deleteAction.setEnabled(false);
		pasteAction.setEnabled(false);
		saveAction.setEnabled(false);
		saveAsAction.setEnabled(false);
		closeAction.setEnabled(false);
		toUpperAction.setEnabled(false);
		toLowerAction.setEnabled(false);
		invertCaseAction.setEnabled(false);
		ascendingAction.setEnabled(false);
		uniqueAction.setEnabled(false);
		descendingAction.setEnabled(false);
		
		//add listener so then language changes there action properties get set again
		flp.addLocalizationListener(() -> {
			setActionProperties();
		});
		
		//listener to update length when language is changed
		lengthStatusAction.addPropertyChangeListener(e -> {
			lengthStatusAction.actionPerformed(null);
		});
		
		//add listener to update status when language is changed
		otherStatusAction.addPropertyChangeListener(e -> {
			otherStatusAction.actionPerformed(null);
		});
		
		
		JPanel innerPanel = new JPanel(new BorderLayout());
		innerPanel.add(documentsModel, BorderLayout.CENTER);

		JPanel statusPanel = new JPanel(new GridLayout(0, 3));
		innerPanel.add(statusPanel, BorderLayout.PAGE_END);
		statusPanel.setBorder(new MatteBorder(2, 0, 0, 0, Color.BLACK));
		
		// I am using a button instead of a lable because labels dont take actions
		JButton lengthLabel = new JButton(lengthStatusAction);
		lengthLabel.setBorderPainted(false);
		lengthLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lengthLabel.setFocusPainted(false);
		lengthLabel.setMargin(new Insets(0, 0, 0, 0));
		lengthLabel.setContentAreaFilled(false);
		lengthLabel.setOpaque(false);
		
		statusPanel.add(lengthLabel);
		
		
		JButton otherLabel = new JButton(otherStatusAction);
		otherLabel.setBorderPainted(false);
		otherLabel.setHorizontalAlignment(SwingConstants.LEFT);
		otherLabel.setFocusPainted(false);
		otherLabel.setMargin(new Insets(0, 0, 0, 0));
		otherLabel.setContentAreaFilled(false);
		otherLabel.setOpaque(false);
		
		statusPanel.add(otherLabel);
		
		
		//time label
		JLabel timeLabel = new JLabel();
		timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"); 
		timeLabel.setText(LocalDateTime.now().toString());
		
		statusPanel.add(timeLabel);
		
		//timer for updating time every second
		timer = new Timer(1000, e -> {
			timeLabel.setText(dtf.format(LocalDateTime.now()));
		});
		timer.setRepeats(true);
		timer.start();
		
		// adding tabbed pane
		this.add(innerPanel, BorderLayout.CENTER);
		
		//add listener to tabbedPane so that active document can be set
		

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				new JavaNotepadPlus().setVisible(true);
			} catch (IOException e) {
				
			}
		});

	}

	/**
	 * {@link LocalizableAction} for changing language to Croatian.
	 */
	LocalizableAction setCroatianAction = new LocalizableAction("croatian", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			LocalizationProvider.getInstance().setLanguage("hr");
		}
	};

	/**
	 * {@link LocalizableAction} for changing language to English.
	 */
	LocalizableAction setEnglishAction = new LocalizableAction("english", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			LocalizationProvider.getInstance().setLanguage("en");
		}
	};
	
	/**
	 * {@link LocalizableAction} for opening new file.
	 */
	LocalizableAction createAction = new LocalizableAction("new", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			documentsModel.createNewDocument();
		}
	};

	/**
	 * {@link LocalizableAction} for opening new file.
	 */
	LocalizableAction openAction = new LocalizableAction("open", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle(flp.getString("open_file"));

			if (fc.showOpenDialog(JavaNotepadPlus.this) != JFileChooser.APPROVE_OPTION) {
				return;
			}

			File fileName = fc.getSelectedFile();
			Path path = fileName.toPath();

			if (!Files.isReadable(path)) {
				JOptionPane.showMessageDialog(JavaNotepadPlus.this,
						flp.getString("file") + " " + fileName.getAbsolutePath() + " " + flp.getString("does_not_exist") + "!",
						flp.getString("error"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			documentsModel.loadDocument(path);
		}
	};
	
	/**
	 * {@link LocalizableAction} for saving file.
	 */
	private LocalizableAction saveAction = new LocalizableAction("save", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			//saving currentModel from MultipleDocumentModel
			//if path is not set get one
			SingleDocumentModel model = documentsModel.getCurrentDocument();
			
			Path path = model.getFilePath();
			if(path == null) {
				JFileChooser fc = new JFileChooser();
				fc.setDialogTitle(flp.getString("choose_where_to_save"));
				if (fc.showOpenDialog(JavaNotepadPlus.this) != JFileChooser.APPROVE_OPTION) {
					return;
				}
				File fileName = fc.getSelectedFile();
				path = fileName.toPath();
			}
			
			try{
				documentsModel.saveDocument(model, path);
			}
			catch (IllegalArgumentException exc){
				JOptionPane.showMessageDialog(
						JavaNotepadPlus.this,
						flp.getString("error_while_writing_to_path") + " " + path.toFile().getAbsolutePath(), 
						flp.getString("error"), 
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			
			updateTitle();
			JOptionPane.showMessageDialog(
					JavaNotepadPlus.this,
					flp.getString("file_was_saved"), 
					flp.getString("information"), 
					JOptionPane.INFORMATION_MESSAGE);
		}
	};
	
	
	/**
	 * {@link LocalizableAction} for saving file to new location.
	 */
 	private LocalizableAction saveAsAction = new LocalizableAction("save_as", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel model = documentsModel.getCurrentDocument();
			
			//get path
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle(flp.getString("choose_where_to_save"));
			if (fc.showOpenDialog(JavaNotepadPlus.this) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File fileName = fc.getSelectedFile();
			Path path = fileName.toPath();
			
			if(Files.isRegularFile(path)) {
				//as user if he wants to overwrite
//				String[] options = new String[] { flp.getString("yes"), flp.getString("no"), flp.getString("cancel") };
//
//				int result = JOptionPane.showOptionDialog(JavaNotepadPlus.this, flp.getString("do_you_want_to_overwrite?"), "Upozorenje",
//						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				
				int result = askIfSave();

				switch (result) {
				// yes
				case 0 -> {
					break;
				}
				// no
				case 1 -> {
					return;
				}
				// cancel
				case 2 -> {
					return;
				}
				// exit button
				case JOptionPane.CLOSED_OPTION -> {
					return;
				}
				}
			}
			
			//if path is a directore store in that directory in unspecified.txt
			if(Files.isDirectory(path))
				path = path.resolve("unspecified.txt");
			
			
			//change path of document
			model.setFilePath(path);
			
			try{
				documentsModel.saveDocument(model, path);
			}
			catch (IllegalArgumentException exc){
				JOptionPane.showMessageDialog(
						JavaNotepadPlus.this,
						flp.getString("error_while_writing_to_path") + " " + path.toFile().getAbsolutePath(), 
						flp.getString("error"), 
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			
			updateTitle();
			JOptionPane.showMessageDialog(
					JavaNotepadPlus.this,
					flp.getString("file_was_saved"), 
					flp.getString("information"), 
					JOptionPane.INFORMATION_MESSAGE);
		}
	};
	
	/**
	 * {@link LocalizableAction} for exiting program.
	 */
	private LocalizableAction exitAction = new LocalizableAction("exit", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			//check if there is unsaved data
			boolean unsavedData = false;
			
			for(SingleDocumentModel document : documentsModel)
				if(document.isModified())
					unsavedData = true;

			if (!unsavedData) {
				dispose();
				return;
			}
			
			//String[] options = new String[] { flp.getString("yes"), flp.getString("no"), flp.getString("cancel") };
			for(int i = 0; i < documentsModel.getNumberOfDocuments(); i++) {
				SingleDocumentModel document = documentsModel.getDocument(i);
				//skip the ones that dont need saving
				if(!document.isModified())
					continue;
				
				//set active document to the one that is being asked to save
				documentsModel.setSelectedIndex(i);
				
				//ask for every document
//				int result = JOptionPane.showOptionDialog(JavaNotepadPlus.this, flp.getString("should_save?"), flp.getString("warning"),
//						JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
				int result = askIfSave();

				switch (result) {
				// yes
				case 0 -> {
					//save
					Path path = document.getFilePath();
					if(path == null) {
						JFileChooser fc = new JFileChooser();
						fc.setDialogTitle(flp.getString("choose_where_to_save"));
						if (fc.showOpenDialog(JavaNotepadPlus.this) != JFileChooser.APPROVE_OPTION) {
							return;
						}
						File fileName = fc.getSelectedFile();
						path = fileName.toPath();
					}
					documentsModel.saveDocument(document, path);
					
				}
				// no
				case 1 -> {
					continue;
				}
				// cancel
				case 2 -> {
					return;
				}
				// exit button
				case JOptionPane.CLOSED_OPTION -> {
					return;
				}
				}
			}
			timer.stop();
			dispose();
		}
	}; 
	
	/**
	 * {@link LocalizableAction} for closing one tab.
	 */
	private LocalizableAction closeAction = new LocalizableAction("close_tab", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			//ask only if there is something to save
			if(documentsModel.getCurrentDocument().isModified()) {
				int result = askIfSave();
				
				if(result == 0) {
					saveAction.actionPerformed(e);
				}
				else if(result == 1) {
					//dont save
				}
				else {
					return;
				}
			}
			
			
			
			//remove currentModel
			documentsModel.closeDocument(documentsModel.getCurrentDocument());
			
			
		}
	};
	
	/**
	 * {@link LocalizableAction} for deleting selected text.
	 */
	private LocalizableAction deleteAction = new LocalizableAction("delete", flp) {
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
			Document document = currentModel.getTextComponent().getDocument();
			Caret caret = currentModel.getTextComponent().getCaret();
			
			int offset = Math.min(caret.getDot(), caret.getMark());
			int length = Math.abs(caret.getDot() - caret.getMark());
			
			try {
				document.remove(offset, length);
			} catch (BadLocationException e1) {
				throw new IllegalArgumentException("error while deleting");
			}
			
		}
	};
	
	/**
	 * {@link LocalizableAction} for cutting selected text.
	 */
	private LocalizableAction cutAction = new LocalizableAction("cut", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
			Document document = currentModel.getTextComponent().getDocument();
			Caret caret = currentModel.getTextComponent().getCaret();
			
			int offset = Math.min(caret.getDot(), caret.getMark());
			int length = Math.abs(caret.getDot() - caret.getMark());
			
			if(length <= 0)
				return;
			
			try {
				savedString = document.getText(offset, length);
				document.remove(offset, length);
			} catch (BadLocationException e1) {
				throw new IllegalArgumentException("error while deleting");
			}
			
		}
	};	
	
	/**
	 * {@link LocalizableAction} for copying selected text.
	 */
	private LocalizableAction copyAction = new LocalizableAction("copy", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
			Document document = currentModel.getTextComponent().getDocument();
			Caret caret = currentModel.getTextComponent().getCaret();
			
			int offset = Math.min(caret.getDot(), caret.getMark());
			int length = Math.abs(caret.getDot() - caret.getMark());
			
			if(length <= 0)
				return;
			
			try {
				savedString = document.getText(offset, length);
			} catch (BadLocationException e1) {
				throw new IllegalArgumentException("error copying");
			}			
		}
	};	
	
	/**
	 * {@link LocalizableAction} for pasting cached text.
	 */
	private LocalizableAction pasteAction = new LocalizableAction("paste", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
			Document document = currentModel.getTextComponent().getDocument();
			Caret caret = currentModel.getTextComponent().getCaret();
			
			int offset = Math.min(caret.getDot(), caret.getMark());
			int length = Math.abs(caret.getDot() - caret.getMark());
			
			//if some text is selected remove old
			if(length > 0) {
				try {
					document.remove(offset, length);
				} catch (BadLocationException e1) {
					throw new IllegalArgumentException("error deleting");
				}
			}
			
			try {
				document.insertString(offset, savedString, null);
			} catch (BadLocationException e1) {
				throw new IllegalArgumentException("error pasting");
			}		
		}
	};	
	
	/**
	 * {@link LocalizableAction} for showing statistics.
	 */
	private LocalizableAction statisticsAction = new LocalizableAction("statistics", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
			
			if(currentModel == null) {
				JOptionPane.showMessageDialog(JavaNotepadPlus.this, flp.getString("no_file"));
				return;
			}
				
			
			Document document = currentModel.getTextComponent().getDocument();
			
			String text = "";
			try {
				text = document.getText(0, document.getLength());
			} catch (BadLocationException e1) {
				throw new RuntimeException("Error while reading document text");
			}
			int numberOfCharacters = text.length();
			int numberofNonBlankCharacters = 0;
			int numberOfLines = 1;
			for(int i = 0; i < document.getLength(); i++) {
				if(!Character.isWhitespace(text.charAt(i)))
					numberofNonBlankCharacters++;
				if(text.charAt(i) == '\n')
					numberOfLines++;
			}
			
			JOptionPane.showMessageDialog(JavaNotepadPlus.this, String.format("%s %d %s, %d %s %s %d %s", flp.getString("your_document_has"), numberOfCharacters,
							flp.getString("characters"), numberofNonBlankCharacters, flp.getString("non_blank_characters"), 
							flp.getString("and"), numberOfLines, flp.getString("lines")));
		}
	};
	
	/**
	 * {@link LocalizableAction} for showing file length.
	 */
	private LocalizableAction lengthStatusAction = new LocalizableAction("length", flp) {
		private static final long serialVersionUID = 1L;	
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = (String)this.getValue(Action.NAME);
			
			//get first word
			String firstWord = name.substring(0, name.indexOf(' ') == -1 ? name.length() : name.indexOf(' '));
			
			SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
			
			if(currentModel != null) {
				Document document = currentModel.getTextComponent().getDocument();
				String text = "";
				try {
					text = document.getText(0, document.getLength());
				} catch (BadLocationException e1) {
					throw new RuntimeException("Error while reading document text");
				}
				int numberOfCharacters = text.length();
				
				String newLength = firstWord + " : " + numberOfCharacters;
				this.putValue(Action.NAME, newLength);
			}
			else {
				this.putValue(Action.NAME, firstWord);
			}
			
		}
	};
	
	/**
	 * {@link LocalizableAction} for showing current row, line, selection size.
	 */
	private LocalizableAction otherStatusAction = new LocalizableAction("other_status", flp) {
		private static final long serialVersionUID = 1L;	
		
		@Override
		public void actionPerformed(ActionEvent e) {			
			SingleDocumentModel currentDocument = documentsModel.getCurrentDocument();
			
			if(currentDocument == null) {
				this.putValue(Action.NAME, flp.getString("other_status"));
				return;
			}
			
			
			Caret caret = currentDocument.getTextComponent().getCaret();
			int offset = currentDocument.getTextComponent().getCaretPosition();
			
			int currentLine = 0;
			int currentCol = 0;
			int selectionLength = 0;
			
			try {
				currentLine = currentDocument.getTextComponent().getLineOfOffset(offset) + 1;
				currentCol = offset - currentDocument.getTextComponent().getLineStartOffset(currentLine-1) + 1;
				
			} catch (BadLocationException e2) {
				throw new RuntimeException("Error while reading document");
			}

			selectionLength = Math.abs(caret.getDot() - caret.getMark());
			
			
			
			String result = flp.getString("other_status");
			
			result = result.substring(0, 5) + currentLine + result.substring(5, 12) + currentCol + result.substring(12) + " " + selectionLength;
			
			this.putValue(Action.NAME, result);
		}
	};
	
	/**
	 * {@link LocalizableAction} for changing to upper case selected text.
	 */
	private LocalizableAction toUpperAction = new LocalizableAction("to_upper", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			changeCase(String::toUpperCase);	
		}
	};
	
	/**
	 * {@link LocalizableAction} for changing to lower case selected text.
	 */
	private LocalizableAction toLowerAction = new LocalizableAction("to_lower", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			changeCase(String::toLowerCase);	
		}
	};
	
	/**
	 * {@link LocalizableAction} for inverting case of selected text.
	 */
	private LocalizableAction invertCaseAction = new LocalizableAction("invert_case", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			changeCase(new Function<String, String>() {

				@Override
				public String apply(String t) {
					StringBuilder builder = new StringBuilder();
					for(int i = 0; i < t.length(); i++) {
						if(Character.isUpperCase(t.charAt(i))) {
							builder.append(Character.toLowerCase(t.charAt(i)));
						}
						else if(Character.isLowerCase(t.charAt(i))) {
							builder.append(Character.toUpperCase(t.charAt(i)));
						}
						else {
							builder.append(t.charAt(i));
						}
					}
					
					return builder.toString();
				}
				
			});	
		}
	};
	
	/**
	 * {@link LocalizableAction} for sorting selected lines in ascending order
	 */
	private LocalizableAction ascendingAction = new LocalizableAction("ascending", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Collator collator = Collator.getInstance(Locale.forLanguageTag(flp.getCurrentLanguage()));
			changeStringByLinesByComparator(collator);
			
		}
	};
	
	/**
	 * {@link LocalizableAction} for sorting selected lines in descending order
	 */
	private LocalizableAction descendingAction = new LocalizableAction("descending", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			Collator collator = Collator.getInstance(Locale.forLanguageTag(flp.getCurrentLanguage()));
			Collator reversed = new Collator() {
				
				@Override
				public int hashCode() {
					return collator.hashCode();
				}
				
				@Override
				public CollationKey getCollationKey(String source) {
					return collator.getCollationKey(source);
				}
				
				@Override
				public int compare(String source, String target) {
					return collator.compare(target, source);
				}
			};
			changeStringByLinesByComparator(reversed);
			
		}
	};
	
	/**
	 * {@link LocalizableAction} for deleting non unique lines.
	 */
	private LocalizableAction uniqueAction = new LocalizableAction("unique", flp) {
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			JTextComponent c = documentsModel.getCurrentDocument().getTextComponent();
			int startPos = Math.min(c.getCaret().getDot(), c.getCaret().getMark());
			int endPos = Math.max(c.getCaret().getDot(), c.getCaret().getMark());
			
			SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
			Document doc = currentModel.getTextComponent().getDocument();
			Element root = doc.getDefaultRootElement();
			int startingRow = root.getElementIndex(startPos);
			int endingRow = root.getElementIndex(endPos);
			
			// positions start and end counting in entire lines
			int start = root.getElement(startingRow).getStartOffset();
			int end = root.getElement(endingRow).getEndOffset();
			
			//if this was the last line i need to decrement end
			if(endingRow == root.getElementIndex(doc.getLength()))
				end--;
			
			List<String> lines = new ArrayList<>();
			for(int i = startingRow; i <= endingRow; i++) {
				String line = "";
				int startOffset = root.getElement(i).getStartOffset();
				int endOffset = Math.min(root.getElement(i).getEndOffset(), end);
				try {
					line = doc.getText(startOffset, endOffset - startOffset);
				} catch (BadLocationException exc) {
					throw new RuntimeException("Error reading");
				}
				
				if(!lines.contains(line))
					lines.add(line);
			}
			
			//remove old lines
			try {
				doc.remove(start, end - start);
			} catch (BadLocationException e1) {
				throw new RuntimeException("Error while deleting");
			}
			
			//insert new lines
			for(int i = 0; i < lines.size(); i++) {
				try {
					doc.insertString(start, lines.get(i), null);
				} catch (BadLocationException exc) {
					throw new RuntimeException("Error reading");
				}
				start += lines.get(i).length();
			}
			
		}
	};

	/**
	 * Shows prompt to user and asks him if he wants to save, doesn't want to save or want to cancel.
	 * @return
	 */
	private int askIfSave() {
		String[] options = new String[] { flp.getString("yes"), flp.getString("no"), flp.getString("cancel") };
		int result = JOptionPane.showOptionDialog(JavaNotepadPlus.this, flp.getString("should_save?"), flp.getString("warning"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		
		return result;
	}
	
	/**
	 * Updates the program title.
	 */
	private void updateTitle() {
		if(documentsModel.getNumberOfDocuments() > 0 && documentsModel.getCurrentDocument().getFilePath() == null) {
			setTitle("(unnamed) - Java Notepad Plus");
		}
		else if(documentsModel.getCurrentDocument() == null) {
			setTitle("Java Notepad Plus");
		}
		else {
			setTitle(documentsModel.getCurrentDocument().getFilePath().toAbsolutePath().toString() + " - Java Notepad Plus");
		}
		
	}
	
	/**
	 * Creates program menu bar
	 */
	private void createMenus() {
		JMenuBar bar = new JMenuBar();
		this.setJMenuBar(bar);

		// adding file menu
		JMenu fileMenu = new JMenu(new LocalizableAction("file", flp) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// DO NOTHING
			}
		});
		bar.add(fileMenu);

		//adding button for creating new File in File menu
		JMenuItem createItem = new JMenuItem(createAction);
		fileMenu.add(createItem);
		
		//adding button for opening file in File menu
		JMenuItem openItem = new JMenuItem(openAction);
		fileMenu.add(openItem);
		
		//adding statisticks button
		JMenuItem statisticsItem = new JMenuItem(statisticsAction);
		fileMenu.add(statisticsItem);
		
		//adding button for saving file in File menu
		JMenuItem saveItem = new JMenuItem(saveAction);
		fileMenu.add(saveItem);
		
		//adding save as button
		JMenuItem saveAsItem = new JMenuItem(saveAsAction);
		fileMenu.add(saveAsItem);
		
		//adding close as button
		JMenuItem closeItem = new JMenuItem(closeAction);
		fileMenu.add(closeItem);
		
		//adding exit button
		JMenuItem exitItem = new JMenuItem(exitAction);
		fileMenu.add(exitItem);

		//adding edit menu
		JMenu editMenu = new JMenu(new LocalizableAction("edit", flp) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// DO NOTHING
			}
		});
		bar.add(editMenu);
		
		//adding delete button
		JMenuItem deleteItem = new JMenuItem(deleteAction);
		editMenu.add(deleteItem);
		
		//adding cut button
		JMenuItem cutItem = new JMenuItem(cutAction);
		editMenu.add(cutItem);
		
		//adding copy button
		JMenuItem copyItem = new JMenuItem(copyAction);
		editMenu.add(copyItem);
		
		//adding paste button
		JMenuItem pasteItem = new JMenuItem(pasteAction);
		editMenu.add(pasteItem);
		
		// adding menu for languages
		JMenu languages = new JMenu(new LocalizableAction("languages", flp) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// DO NOTHING
			}
		});
		bar.add(languages);

		JMenuItem itemHr = new JMenuItem(setCroatianAction);
		JMenuItem itemEn = new JMenuItem(setEnglishAction);
		languages.add(itemEn);
		languages.add(itemHr);
		
		
		//adding tools menu
		JMenu toolsMenu = new JMenu(new LocalizableAction("tools", flp) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// DO NOTHING
			}
		});
		bar.add(toolsMenu);
		
		//adding change case submenu
		JMenu changeCaseMenu = new JMenu(new LocalizableAction("change_case", flp) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// DO NOTHING
			}
		});
		toolsMenu.add(changeCaseMenu);
		
		//adding to upper case item to change case submenu
		JMenuItem uppercaseItem = new JMenuItem(toUpperAction);
		changeCaseMenu.add(uppercaseItem);
		
		//adding to lower case item to change case submenu
		JMenuItem lowercaseItem = new JMenuItem(toLowerAction);
		changeCaseMenu.add(lowercaseItem);
				
		//adding invert case item to change case submenu
		JMenuItem invertCaseItem = new JMenuItem(invertCaseAction);
		changeCaseMenu.add(invertCaseItem);
		
		//adding sort submenu
		JMenu sortMenu = new JMenu(new LocalizableAction("sort", flp) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// DO NOTHING
			}
		});
		toolsMenu.add(sortMenu);
		
		//adding ascending item
		JMenuItem ascendingItem = new JMenuItem(ascendingAction);
		sortMenu.add(ascendingItem);
		
		//adding descending item
		JMenuItem descendingItem = new JMenuItem(descendingAction);
		sortMenu.add(descendingItem);
		
		//add unique item
		JMenuItem uniqueItem = new JMenuItem(uniqueAction);
		toolsMenu.add(uniqueItem);
		
	}
	
	/**
	 * Creates dockable toolbar
	 */
	private void createToolbar() {
		JToolBar toolBar = new JToolBar(flp.getString("tools"));
		toolBar.setFloatable(true);
		
		toolBar.add(new JButton(createAction));
		toolBar.add(new JButton(openAction));
		toolBar.addSeparator();
		toolBar.add(new JButton(saveAction));
		toolBar.add(new JButton(saveAsAction));
		toolBar.addSeparator();
		toolBar.add(new JButton(copyAction));
		toolBar.add(new JButton(cutAction));
		toolBar.add(new JButton(pasteAction));
		toolBar.add(new JButton(deleteAction));
		toolBar.addSeparator();
		toolBar.add(new JButton(statisticsAction));
		toolBar.addSeparator();
		toolBar.add(new JButton(closeAction));
		toolBar.add(new JButton(exitAction));

		
		this.getContentPane().add(toolBar, BorderLayout.PAGE_START);
	}

	/**
	 * Sets action properties according to current language.
	 */
	private void setActionProperties(){
		//create
		createAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
		createAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		createAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("create_description")); 
		
		//open
		openAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
		openAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		openAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("open_description")); 
		
		//save
		saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
		saveAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		saveAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("save_description"));
		
		//save as
		saveAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control alt S"));
		saveAsAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		saveAsAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("save_as_description"));
		
		//copy
		copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control alt C"));
		copyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		copyAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("copy_description"));
		
		//cut
		cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control alt X"));
		cutAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		cutAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("cut_description"));
		
		//paste
		pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control alt V"));
		pasteAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
		pasteAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("paste_description"));
		
		//delete
		deleteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
		deleteAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
		deleteAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("delete_description"));
		
		//statistics
		statisticsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
		statisticsAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
		statisticsAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("statistics_description"));
		
		//close
		closeAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control W"));
		closeAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);
		closeAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("close_description"));
		
		//exit
		exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
		exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		exitAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("exit_description"));
		
		//to upper case
		toUpperAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
		toUpperAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
		toUpperAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("upper_case_description"));
		
		//to lower case
		toLowerAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control L"));
		toLowerAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		toLowerAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("lower_case_description"));
		
		//invert case
		invertCaseAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control I"));
		invertCaseAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		invertCaseAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("invert_case_description"));
		
		//ascending
		ascendingAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("ascending_description"));
		
		//descending
		descendingAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("descending_description"));
		
		//unique
		uniqueAction.putValue(Action.SHORT_DESCRIPTION, flp.getString("unique_description"));
		
	}
	
	/**
	 * Change listener for carot to disable/enable buttons
	 */
	private ChangeListener changeListener = new ChangeListener() {
		
		@Override
		public void stateChanged(ChangeEvent e) {
			Caret c = (Caret) e.getSource();
			int length = Math.abs(c.getDot() - c.getMark());
			
			cutAction.setEnabled(length > 0);
			deleteAction.setEnabled(length > 0);
			copyAction.setEnabled(length > 0);
			toUpperAction.setEnabled(length > 0);
			toLowerAction.setEnabled(length > 0);
			invertCaseAction.setEnabled(length > 0);
			ascendingAction.setEnabled(length > 0);
			descendingAction.setEnabled(length > 0);
			uniqueAction.setEnabled(length > 0);
			
			
			//caret changed
			otherStatusAction.actionPerformed(null);
		}
	};
	
	/**
	 * Listen to document and update length every time something is inserted or removed.
	 */
	private DocumentListener documentListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
			lengthStatusAction.actionPerformed(null);
			
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
			lengthStatusAction.actionPerformed(null);
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			// TODO Auto-generated method stub
			
		}
	};
	
	/**
	 * Changes case according to given function
	 */
	private void changeCase(Function<String, String> func) {
		SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
		Document document = currentModel.getTextComponent().getDocument();
		Caret caret = currentModel.getTextComponent().getCaret();
		
		int offset = Math.min(caret.getDot(), caret.getMark());
		int length = Math.abs(caret.getDot() - caret.getMark());
		
		String oldString = "";
		
		//if some text is selected remove old
		if(length > 0) {
			try {
				oldString = document.getText(offset, length);
				document.remove(offset, length);
				document.insertString(offset, func.apply(oldString), null);
			} catch (BadLocationException e1) {
				throw new RuntimeException("error deleting");
			}
		}	
		
	}
	
	/**
	 * Oders selected lines by given collator.
	 * 
	 * @param collator by which to do order
	 */
	private void changeStringByLinesByComparator(Collator collator) {
		JTextComponent c = documentsModel.getCurrentDocument().getTextComponent();
		int startPos = Math.min(c.getCaret().getDot(), c.getCaret().getMark());
		int endPos = Math.max(c.getCaret().getDot(), c.getCaret().getMark());
		
		SingleDocumentModel currentModel = documentsModel.getCurrentDocument();
		Document doc = currentModel.getTextComponent().getDocument();
		Element root = doc.getDefaultRootElement();
		int startingRow = root.getElementIndex(startPos);
		int endingRow = root.getElementIndex(endPos);
		
		// positions start and end counting in entire lines
		int start = root.getElement(startingRow).getStartOffset();
		int end = root.getElement(endingRow).getEndOffset();
		
		//if this was the last line i need to decrement end
		if(endingRow == root.getElementIndex(doc.getLength()))
			end--;
		
		List<String> lines = new ArrayList<>();
		for(int i = startingRow; i <= endingRow; i++) {
			String line = "";
			int startOffset = root.getElement(i).getStartOffset();
			int endOffset = Math.min(root.getElement(i).getEndOffset(), end);
			try {
				line = doc.getText(startOffset, endOffset - startOffset);
			} catch (BadLocationException e) {
				throw new RuntimeException("Error reading");
			}
			//if it was the last line add \n
			if(line.length() > 0 && line.charAt(line.length()-1) != '\n')
				line += "\n";
			
			lines.add(line);
		}
		Collections.sort(lines, collator);

		//remove new line on last line 
//		String lastLine = lines.get(lines.size()-1);
//		if(lastLine.charAt(lastLine.length()-1) == '\n')
//			lines.set(lines.size() - 1, lastLine.substring(0, lastLine.length() - 1));
		
		//remove old lines
		try {
			doc.remove(start, end - start);
		} catch (BadLocationException e1) {
			throw new RuntimeException("Error while deleting");
		}
		
		//replace all selected lines
		for(int i = 0; i < lines.size(); i++) {
			try {
				doc.insertString(start, lines.get(i), null);
			} catch (BadLocationException e) {
				throw new RuntimeException("Error reading");
			}
			start += lines.get(i).length();
		}

	}

    
}
