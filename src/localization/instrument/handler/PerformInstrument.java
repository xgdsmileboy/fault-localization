package localization.instrument.handler;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import localization.common.java.JavaFile;
import localization.common.util.Debugger;
import localization.instrument.visitor.TraversalVisitor;

public class PerformInstrument {

	public static void execute(ExecutionEvent event, TraversalVisitor traversalVisitor) {

		System.out.println("execute command");

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = window.getActivePage();
		ISelection selection = activePage.getSelection();
		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				Object element = structuredSelection.getFirstElement();
				if (element instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
					PerformJob performJob = new PerformJob(project, traversalVisitor);

					ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(null);
					try {
						monitorDialog.run(true, false, performJob);
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

					ICompilationUnit iCompilationUnit = (ICompilationUnit) iJavaElement;

					PerformJob performJob = new PerformJob(iCompilationUnit, traversalVisitor);
					ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(null);
					try {
						monitorDialog.run(true, false, performJob);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}

class PerformJob implements IRunnableWithProgress {
	private IProject iProject = null;
	private ICompilationUnit iCUnit = null;
	private TraversalVisitor tVisitor = null;

	public PerformJob(IProject project, TraversalVisitor visitor) {
		this.iProject = project;
		this.tVisitor = visitor;
	}

	public PerformJob(ICompilationUnit iCompilationUnit, TraversalVisitor visitor) {
		this.iCUnit = iCompilationUnit;
		this.tVisitor = visitor;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (iProject != null) {
			perform(iProject, monitor);
		} else if (iCUnit != null) {
			perform(iCUnit, monitor);
		}
	}

	private boolean perform(IProject project, IProgressMonitor monitor) {
		try {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature") && project.isOpen()) {
				IJavaProject iJavaProject = JavaCore.create(project);
				for (IPackageFragment iPackageFragment : iJavaProject.getPackageFragments()) {
					for (ICompilationUnit iCompilationUnit : iPackageFragment.getCompilationUnits()) {
						perform(iCompilationUnit, monitor);
					}
				}
			} else {
				if (Debugger.debugOn) {
					Debugger.debug("@PerformInstrument$PerformJob project: " + project.getName() + "is closed!");
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean perform(ICompilationUnit iCompilationUnit, IProgressMonitor monitor) {
		try {

			iCompilationUnit.becomeWorkingCopy(new SubProgressMonitor(monitor, 2));

			CompilationUnit compilationUnit = JavaFile.genASTFromICU(iCompilationUnit);

			tVisitor.traverse(compilationUnit);

			IBuffer buffer = iCompilationUnit.getBuffer();

			ISourceRange iSourceRange = iCompilationUnit.getSourceRange();
			String instrumentedContent = compilationUnit.toString();
			IJavaProject iJavaProject = iCompilationUnit.getJavaProject();
			String lineDelimiter = StubUtility.getLineDelimiterUsed(iJavaProject);
			String formattedContent = CodeFormatterUtil.format(CodeFormatter.K_COMPILATION_UNIT, instrumentedContent, 0,
					lineDelimiter, iJavaProject);
			formattedContent = Strings.trimLeadingTabsAndSpaces(formattedContent);
			buffer.replace(iSourceRange.getOffset(), iSourceRange.getLength(), formattedContent);

			// commit
			iCompilationUnit.commitWorkingCopy(true, monitor);
			// discard copy
			if (iCompilationUnit != null)
				iCompilationUnit.discardWorkingCopy();
			monitor.done();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return true;
	}

}
