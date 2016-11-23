package localization.common.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import localization.common.java.JavaFile;
import localization.common.java.JavaProject;
import localization.common.tools.AutoCodeFormatter;
import localization.common.tools.Console;
import localization.common.util.Debugger;

public class CodeFormatHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	Console.setConsole();
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		ISelection selection = activePage.getSelection();
		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				if (Debugger.debugOn) {
					Debugger.debug("got instructuredSelection");
				}
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object element = structuredSelection.getFirstElement();
				if (element instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
					JavaProject javaProject = new JavaProject(project);
					AutoCodeFormatter.format(javaProject);
				}
			} else if (selection instanceof TextSelection) {
				TextSelection textSelection = (TextSelection) selection;
				IEditorPart iEditorPart = activePage.getActiveEditor();
				IJavaElement iJavaElement = iEditorPart.getEditorInput().getAdapter(IJavaElement.class);

				while (iJavaElement.getElementType() != IJavaElement.COMPILATION_UNIT) {
					iJavaElement = iJavaElement.getParent();
				}
				ICompilationUnit iCompilationUnit = (ICompilationUnit) iJavaElement;
				JavaFile javaFile = new JavaFile(iCompilationUnit);
				AutoCodeFormatter.format(javaFile);

			}
		}
		return null;
	}

}
