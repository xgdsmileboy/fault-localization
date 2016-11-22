package localization.common.tools;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.text.edits.TextEdit;

import localization.common.java.JavaFile;
import localization.common.java.JavaPackage;
import localization.common.java.JavaProject;

public class AutoCodeFormatter {
	
	private ICompilationUnit iCU;
	
	public static void format(JavaProject javaProject){
		for(JavaPackage javaPackage : javaProject.getJavaPackage()){
			for(JavaFile javaFile : javaPackage.getJavaFiles()){
				new AutoCodeFormatter().format(javaFile.getICompilationUnit());
			}
		}
	}
	
	public static void format(JavaFile javaFile){
		new AutoCodeFormatter().format(javaFile.getICompilationUnit());
	}
	
	private void format(ICompilationUnit icu){
		iCU = icu;
		Job job = new Job("code format") {
			@Override
			protected IStatus run(IProgressMonitor arg0) {
				try {
					formatUnitSourceCode(new SubProgressMonitor(arg0, 1));
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule(0);
	}
	
	private void formatUnitSourceCode(IProgressMonitor monitor) throws JavaModelException {
	    CodeFormatter formatter = ToolFactory.createCodeFormatter(null);
	    ISourceRange range = iCU.getSourceRange();
	    TextEdit formatEdit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, iCU.getSource(), range.getOffset(), range.getLength(), 0, null);
	    if (formatEdit != null && formatEdit.hasChildren()) {
	        iCU.applyTextEdit(formatEdit, monitor);
	    } else {
	        monitor.done();
	    }
	}
}
