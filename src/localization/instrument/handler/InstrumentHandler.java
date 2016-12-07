package localization.instrument.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import localization.common.util.Console;
import localization.instrument.visitor.InstrumentVisitor;

public class InstrumentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Console.setConsole();
		 PerformInstrument.execute(event, new InstrumentVisitor());
		return null;
	}


	// public void test(String... args) throws ClassNotFoundException {
	// String[] classAndMethod = args[0].split("#");
	//
	// Request request = Request.method(Class.forName(classAndMethod[0]),
	// classAndMethod[1]);
	// Result result = new JUnitCore().run(request);
	// if (result.getFailureCount() > 0) {
	// System.out.println("E");
	// }
	// System.out.println("<<");
	// List<Failure> failures = result.getFailures();
	// for (Failure failure : failures) {
	// System.out.println(failure.getMessage());
	// System.out.println(failure.getDescription().toString());
	// System.out.println(failure.getTrace());
	// }
	// System.exit(0);
	// }

}
