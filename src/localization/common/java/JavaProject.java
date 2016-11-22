package localization.common.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;

import localization.common.util.Debugger;

public class JavaProject {

	protected IJavaProject iJavaProject;
	protected ArrayList<JavaPackage> packages = new ArrayList<JavaPackage>();

	public JavaProject(IProject project) {
		try {
			if (project.isNatureEnabled("org.eclipse.jdt.core.javanature") && project.isOpen()) {
				this.iJavaProject = JavaCore.create(project);
				for (IPackageFragment iPackageFragment : iJavaProject.getPackageFragments()) {
					packages.add(new JavaPackage(iPackageFragment));
				}
			} else {
				if (Debugger.debugOn) {
					Debugger.debug("@JavaProject$JavaProject project: " + project.getName() + "is closed!");
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public IJavaProject getIJavaProject() {
		return this.iJavaProject;
	}

	public List<JavaPackage> getJavaPackage() {
		return this.packages;
	}

}
