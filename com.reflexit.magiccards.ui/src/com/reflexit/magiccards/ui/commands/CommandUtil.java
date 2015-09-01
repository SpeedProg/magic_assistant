package com.reflexit.magiccards.ui.commands;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;

public class CommandUtil {
	public static Object executeCommandWithParameter(String commId, String paramId, String paramValue)
			throws ExecutionException,
			NotDefinedException, NotEnabledException, NotHandledException {
		ICommandService commandService = PlatformUI.getWorkbench().getService(
				ICommandService.class);
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(
				IHandlerService.class);
		Command command = commandService.getCommand(commId);
		IParameter param = command.getParameter(paramId);
		Parameterization parm = new Parameterization(param, paramValue);
		ParameterizedCommand parmCommand = new ParameterizedCommand(command, new Parameterization[] { parm });
		return handlerService.executeCommand(parmCommand, null);
	}

	public static boolean executeCommand(String id) {
		IHandlerService handlerService = PlatformUI.getWorkbench().getService(
				IHandlerService.class);
		try {
			Object result = handlerService.executeCommand(id, null);
			if (result == Status.OK_STATUS)
				return true;
			// CommandUtil.executeCommandWithParameter("com.reflexit.magiccards.ui.commands.filterCommand",
			// "com.reflexit.magiccards.ui.viewId", getPreferencePageId());
		} catch (Exception ex) {
			throw new IllegalArgumentException();
		}
		return false;
	}
}
