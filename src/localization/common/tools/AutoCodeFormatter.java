package localization.common.tools;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import localization.common.java.JavaFile;
import localization.common.java.JavaProject;
import localization.common.util.Debugger;

public class AutoCodeFormatter {

	public static void format(IProject project){
		try {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature") && project.isOpen()) {
				IJavaProject iJavaProject = JavaCore.create(project);
				for (IPackageFragment iPackageFragment : iJavaProject.getPackageFragments()) {
					for (ICompilationUnit iCompilationUnit : iPackageFragment.getCompilationUnits()) {
						AutoCodeFormatter.format(iCompilationUnit);
					}
				}
			} else {
				if (Debugger.debugOn) {
					Debugger.debug("@JavaProject$JavaProject project: " + project.getName() + "is closed!");
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static void format(JavaProject javaProject) {

		CodeFormatJob job = new CodeFormatJob(javaProject);

		try {
			ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(null);
			monitorDialog.run(true, false, job);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void format(JavaFile javaFile) {
		
		CodeFormatJob job = new CodeFormatJob(javaFile);

		try {
			ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(null);
			monitorDialog.run(true, false, job);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void format(ICompilationUnit iCompilationUnit) {
		
		CodeFormatJob job = new CodeFormatJob(iCompilationUnit);

		try {
			ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(null);
			monitorDialog.run(true, false, job);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class CodeFormatJob implements IRunnableWithProgress {

	private JavaProject jProject = null;
	private JavaFile jFile = null;
	private ICompilationUnit iCUnit = null;

	public CodeFormatJob(JavaProject javaProject) {
		this.jProject = javaProject;
	}

	public CodeFormatJob(JavaFile javaFile) {
		jFile = javaFile;
	}
	
	public CodeFormatJob(ICompilationUnit iCompilationUnit){
		iCUnit = iCompilationUnit;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		if (jProject != null) {
			IJavaProject javaProject = jProject.getIJavaProject();
			if (javaProject.exists() && javaProject != null) {
				try {
					IPackageFragmentRoot root = javaProject.getPackageFragmentRoots()[0];

					for (IJavaElement pack : root.getChildren()) {

						if (pack instanceof IPackageFragment) {

							for (ICompilationUnit cu : ((IPackageFragment) pack).getCompilationUnits()) {

								String lineDelimiter = StubUtility.getLineDelimiterUsed(javaProject);

								format(monitor, cu, lineDelimiter);
							}
						}
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		} else if (jFile != null) {
			ICompilationUnit iCUnit = jFile.getICompilationUnit();
			IJavaProject iJavaProject = iCUnit.getJavaProject();
			String lineDelimiter = StubUtility.getLineDelimiterUsed(iJavaProject);
			try {
				format(monitor, iCUnit, lineDelimiter);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else if(iCUnit != null){
			IJavaProject iJavaProject = iCUnit.getJavaProject();
			String lineDelimiter = StubUtility.getLineDelimiterUsed(iJavaProject);
			try {
				format(monitor, iCUnit, lineDelimiter);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private void format(IProgressMonitor monitor, ICompilationUnit parentCU, String lineDelimiter)
			throws CoreException {

		// working copy
		parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1));

		IBuffer buffer = parentCU.getBuffer();
		IType type = parentCU.getTypes()[0];

		// format
		ISourceRange sourceRange = type.getSourceRange();
		String originalContent = buffer.getText(sourceRange.getOffset(), sourceRange.getLength());
		String formattedContent = CodeFormatterUtil.format(CodeFormatter.K_CLASS_BODY_DECLARATIONS, originalContent, 0,
				lineDelimiter, parentCU.getJavaProject());
		formattedContent = Strings.trimLeadingTabsAndSpaces(formattedContent);
		buffer.replace(sourceRange.getOffset(), sourceRange.getLength(), formattedContent);

		// commit
		parentCU.commitWorkingCopy(true, monitor);
		// discard copy
		if (parentCU != null)
			parentCU.discardWorkingCopy();
		monitor.done();
	}

}
