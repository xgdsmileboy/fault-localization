package localization.instrument.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import localization.common.util.Console;
import localization.instrument.visitor.DeInstrumentVisitor;

public class DeInstrumentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Console.setConsole();
		PerformInstrument.execute(event, new DeInstrumentVisitor());
		return null;
	}

}

