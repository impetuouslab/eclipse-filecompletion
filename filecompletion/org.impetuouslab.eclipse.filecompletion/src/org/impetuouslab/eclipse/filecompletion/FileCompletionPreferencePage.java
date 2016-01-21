package org.impetuouslab.eclipse.filecompletion;

import java.io.File;
import java.util.logging.Logger;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class FileCompletionPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final Logger LOG = Logger.getLogger(FileCompletionPreferencePage.class.getName());

	static {
		LOG.info("filecompletion : static init done");
	}

	// private volatile String currentValue;

	public static volatile boolean checkDuringTyping = false;

	private static volatile boolean checkDuringTypingProposed = false;

	private Text pragrammArguments;

	private Text programmPath;

	public FileCompletionPreferencePage() {
	}

	public FileCompletionPreferencePage(String title) {
		super(title);
	}

	public FileCompletionPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	public void init(IWorkbench workbench) {
		LOG.info("filecompletion : about to init ..");
		// currentValue =
		// FileCompletionActivator.getDefault().getPreferenceStore()
		// .getString(FileCompletionActivator.openFileWithExternalProgramPerfId);

		checkDuringTyping = FileCompletionActivator.getDefault().getPreferenceStore()
				.getBoolean(FileCompletionActivator.checkDuringTypingId);

		checkDuringTypingProposed = checkDuringTyping;

		setPreferenceStore(FileCompletionActivator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(final Composite parent2) {
		final Composite parent = new Composite(parent2, SWT.NULL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		parent.setLayout(gridLayout);
		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Open file with external program");
			programmPath = new Text(parent, SWT.BORDER);
			programmPath.addVerifyListener(new VerifyListener() {

				public void verifyText(VerifyEvent e) {
					Display.getCurrent().asyncExec(new Runnable() {

						public void run() {
							// used async to get final text
							LOG.info(programmPath.getText());
							// currentValue = programmPath.getText();
							setValid(isValid());
						}
					});
				}
			});
			{
				String currentValue = FileCompletionActivator.getDefault().getPreferenceStore()
						.getString(FileCompletionActivator.openFileWithExternalProgramCmdPerfId);
				LOG.info("current external program value = " + currentValue);
				if (currentValue != null) {
					programmPath.setText(currentValue);
				}
			}
			Button button = new Button(parent, SWT.NONE);
			button.setText("Select file");
			button.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent e) {
					FileDialog fd = new FileDialog(parent.getShell(), SWT.OPEN);
					fd.setFileName(programmPath.getText());
					fd.setText("Select");
					String currentValue2 = fd.open();
					currentValue2 = currentValue2.replace('\\', '/');
					LOG.info("selected file " + currentValue2);
					programmPath.setText(currentValue2);
					setValid(isValid());

				}

				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});
		}

		{
			Label label = new Label(parent, SWT.NONE);
			label.setText("Program arguments");
			pragrammArguments = new Text(parent, SWT.BORDER);
			String currentValue = FileCompletionActivator.getDefault().getPreferenceStore()
					.getString(FileCompletionActivator.openFileWithExternalProgramArgsPerfId);
			LOG.info("current args value = " + currentValue);
			if (currentValue != null) {
				pragrammArguments.setText(currentValue);
			}
		}
		final Button doVerifictionOnline = new Button(parent, SWT.CHECK);
		doVerifictionOnline.setText("Check file name during typing");
		doVerifictionOnline.setSelection(checkDuringTyping);
		checkDuringTypingProposed = checkDuringTyping;
		doVerifictionOnline.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				checkDuringTypingProposed = doVerifictionOnline.getSelection();
			}

			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {

			}
		});

		isValid();
		return parent;
	}

	@Override
	public boolean isValid() {
		String currentValue = programmPath.getText();
		if (currentValue == null || currentValue.length() == 0) {
			setMessage("Specify file", org.eclipse.jface.dialogs.IMessageProvider.ERROR);
			return false;
		}
		boolean fileExists = new File(currentValue).isFile();
		if (fileExists) {
			setMessage(null);
		} else {
			setMessage("File not found", org.eclipse.jface.dialogs.IMessageProvider.ERROR);
		}
		return fileExists;
	}

	@Override
	public boolean performOk() {
		if (isValid()) {
			// storing preference
			FileCompletionActivator.getDefault().getPreferenceStore()
					.setValue(FileCompletionActivator.openFileWithExternalProgramCmdPerfId, programmPath.getText());

			FileCompletionActivator.getDefault().getPreferenceStore().setValue(
					FileCompletionActivator.openFileWithExternalProgramArgsPerfId, pragrammArguments.getText());

			checkDuringTyping = checkDuringTypingProposed;

			FileCompletionActivator.getDefault().getPreferenceStore()
					.setValue(FileCompletionActivator.checkDuringTypingId, checkDuringTyping);
		} else {
			return false;
		}

		return super.performOk();
	}

}
