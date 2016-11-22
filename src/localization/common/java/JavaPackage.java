package localization.common.java;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

public class JavaPackage {

	IPackageFragment packageFrag;
	ArrayList<JavaFile> files = new ArrayList<JavaFile>();

	public JavaPackage(IPackageFragment iPackageFragment) {
		packageFrag = iPackageFragment;
		try {
			for (ICompilationUnit compilationUnit : iPackageFragment.getCompilationUnits()) {
				files.add(new JavaFile(compilationUnit));
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	public IPackageFragment getIPackageFragment() {
		return this.packageFrag;
	}

	public List<JavaFile> getJavaFiles() {
		return this.files;
	}
}
