package localization.runtest.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.junit.model.TestElement;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import localization.common.util.Console;
import localization.common.util.Debugger;
import localization.runtest.ConfigAndRunTest;

public class RunTestHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Console.setConsole();
		executeTest(event);

		return null;
	}

	private void executeTest(ExecutionEvent event) {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		ISelection selection = activePage.getSelection();
		if (selection != null) {

			if (selection instanceof IStructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object element = structuredSelection.getFirstElement();
				if (element instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
					try {
						if (project.isNatureEnabled("org.eclipse.jdt.core.javanature") && project.isOpen()) {
							IJavaProject iJavaProject = JavaCore.create(project);
							ConfigAndRunTest runTest = new ConfigAndRunTest();
							TestRunSession session = runTest.runAsJUnit(iJavaProject);
							if (session == null) {
								if (Debugger.debugOn) {
									Debugger.debug("@RunTestHandler #execute run test for project "
											+ iJavaProject.getElementName() + ", return TestRunSession null");
								}
							} else {
								System.out.println(session.getTotalCount() + " " + session.getErrorCount() + " "
										+ session.getFailureCount());
							}

						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else if (selection instanceof TextSelection) {
				TextSelection textSelection = (TextSelection) selection;
				IEditorPart iEditorPart = activePage.getActiveEditor();
				IJavaElement iJavaElement = (IJavaElement) iEditorPart.getEditorInput().getAdapter(IJavaElement.class);
				while (iJavaElement.getElementType() > IJavaElement.COMPILATION_UNIT) {
					iJavaElement = iJavaElement.getParent();
				}
				if (iJavaElement instanceof ICompilationUnit) {
					ICompilationUnit unit = (ICompilationUnit) iJavaElement;
					try {
						for (IType type : unit.getAllTypes()) {
							// for (IMethod method : type.getMethods()) {
							ConfigAndRunTest runTest = new ConfigAndRunTest();
							TestRunSession session = runTest.runAsJUnit(type);
							if (session == null) {
								if (Debugger.debugOn) {
									Debugger.debug("@RunTestHandler #execute run test for type " + type.getElementName()
											+ ", return TestRunSession null");
								}
							} else {
								TestElement[] failedElement = session.getAllFailedTestElements();
								for (TestElement element : failedElement) {
									if (element.getFailureTrace() != null) {
										System.out.println(element.getFailureTrace().getTrace());
									}
								}
								System.out.println(session.getTotalCount() + " " + session.getErrorCount() + " "
										+ session.getFailureCount());
							}
							// }
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					if (Debugger.debugOn) {
						Debugger.debug("@finish run test");
					}

				}
			}
		}
	}

}
