package localization.runtest.listener;

import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestRunSession;

public class MockTestRunListener extends TestRunListener {

	private ITestRunSession session = null;
	
	public ITestRunSession geTestRunSessio(){
		return this.session;
	}
	//same session for test
	public void sessionFinished(ITestRunSession session) {
		this.session = session;
    }

}
