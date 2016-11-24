package localization.common.util;

import java.io.PrintStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console {

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager iConsoleManager = plugin.getConsoleManager();
		IConsole[] existing = iConsoleManager.getConsoles();
		for (int i = 0; i < existing.length; i++) {
			if (name.equals(existing[i].getName())) {
				return (MessageConsole) existing[i];
			}
		}
		MessageConsole mMessageConsole = new MessageConsole(name, null);
		iConsoleManager.addConsoles(new IConsole[] { mMessageConsole });
		return mMessageConsole;
	}
	
	public static void setConsole(){
		MessageConsole console = Console.findConsole("Localization:console");
		MessageConsoleStream stream = console.newMessageStream();
//		System.setErr(new PrintStream(stream));
//		System.setErr(null);
		System.setOut(new PrintStream(stream));
		console.clearConsole();
	}
}
