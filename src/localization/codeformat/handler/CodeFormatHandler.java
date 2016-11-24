package localization.codeformat.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import localization.codeformat.AutoCodeFormatter;
import localization.common.util.Console;

public class CodeFormatHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Console.setConsole();
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		ISelection selection = activePage.getSelection();
		List<String> files = new ArrayList<>();
		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object element = structuredSelection.getFirstElement();
				if (element instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
					AutoCodeFormatter.format(project);
				}
			} else if (selection instanceof TextSelection) {
				TextSelection textSelection = (TextSelection) selection;
				IEditorPart iEditorPart = activePage.getActiveEditor();
				IJavaElement iJavaElement = (IJavaElement) iEditorPart.getEditorInput().getAdapter(IJavaElement.class);
				while (iJavaElement.getElementType() > IJavaElement.COMPILATION_UNIT) {
					iJavaElement = iJavaElement.getParent();
				}
				if(iJavaElement instanceof ICompilationUnit){
					ICompilationUnit iCompilationUnit = (ICompilationUnit) iJavaElement;
					AutoCodeFormatter.format(iCompilationUnit);
				}
			}
		}
		
		return null;
	}
}
