package localization.codeformat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

import localization.common.util.Debugger;

public class AutoCodeFormatter {

	public static void format(IProject project) {
		CodeFormatJob job = new CodeFormatJob(project);
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

	private IProject iProject = null;
	private ICompilationUnit iCUnit = null;

	public CodeFormatJob(IProject project) {
		this.iProject = project;
	}

	public CodeFormatJob(ICompilationUnit iCompilationUnit) {
		iCUnit = iCompilationUnit;
	}

	@Override
	public void run(IProgressMonitor monitor) {
		if (iProject != null) {
			try {
				if (iProject.isNatureEnabled("org.eclipse.jdt.core.javanature") && iProject.isOpen()) {
					IJavaProject iJavaProject = JavaCore.create(iProject);
					for (IPackageFragment iPackageFragment : iJavaProject.getPackageFragments()) {
						for (ICompilationUnit iCompilationUnit : iPackageFragment.getCompilationUnits()) {
							format(monitor, iCompilationUnit);
						}
					}
				} else {
					if (Debugger.debugOn) {
						Debugger.debug("@JavaProject$JavaProject project: " + iProject.getName() + "is closed!");
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else if (iCUnit != null) {
			format(monitor, iCUnit);
		}
	}

	private void format(IProgressMonitor monitor, ICompilationUnit iCompilationUnit) {
		try {

			iCompilationUnit.becomeWorkingCopy(new SubProgressMonitor(monitor, 2));
			IBuffer buffer = iCompilationUnit.getBuffer();

			ISourceRange iSourceRange = iCompilationUnit.getSourceRange();
			String originalContent = iCompilationUnit.getSource();
			IJavaProject iJavaProject = iCompilationUnit.getJavaProject();
			String lineDelimiter = StubUtility.getLineDelimiterUsed(iJavaProject);
			String formattedContent = CodeFormatterUtil.format(CodeFormatter.K_COMPILATION_UNIT, originalContent, 0,
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
	}

}
