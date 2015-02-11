package com.reflexit.magiccards.ui.commands;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.reflexit.magicassistant.p2.Activator;
import com.reflexit.magiccards.core.FileUtils;
import com.reflexit.magiccards.ui.widgets.Toast;

public class BackupHandler extends AbstractHandler {
	@Override
	public Object execute(final ExecutionEvent aevent) {
		final File ws = FileUtils.getWorkspace();
		SimpleDateFormat format = new SimpleDateFormat("YYYY_MMdd_HHmmss");
		File backupDir = FileUtils.getBackupDir();
		final File backup = new File(backupDir, format.format(new Date()));
		Job job = new Job("Backing up...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				File[] listFiles = ws.listFiles();
				for (int i = 0; i < listFiles.length; i++) {
					File file = listFiles[i];
					if (file.getName().startsWith(".")
							|| file.getName().equals(backup.getParentFile().getName()))
						continue;
					try {
						FileUtils.copyTree(file, new File(backup, file.getName()));
					} catch (Throwable e) {
						Activator
								.getDefault()
								.getLog()
								.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1,
										"Failed to save backup " + backup, e));
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							final IWorkbenchWindow window = aevent != null ? HandlerUtil
									.getActiveWorkbenchWindowChecked(aevent)
									: PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							Shell shell = window.getShell();
							if (event.getResult() == Status.OK_STATUS) {
								MessageDialog.openInformation(shell, "Info", "Backup saved in " + backup);
							} else {
								new Toast(shell, "Backup failed:  " + event.getResult()).open();
							}
						} catch (ExecutionException e) {
						}
					}
				});
			}
		});
		job.setUser(true);
		job.schedule();
		return null;
	}

	public static File getWorkspaceFile() {
		String str = System.getProperty("osgi.instance.area");
		if (str != null) {
			return new File(str.replaceFirst("^file:", ""));
		} else {
			return new File(System.getProperty("user.home"), "MagicAssistant");
		}
	}
}
