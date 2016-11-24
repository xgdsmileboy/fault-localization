package localization.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import localization.common.util.Configure;
import localization.common.util.Console;

public class RunTestHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		Console.setConsole();

		IWorkspace iWorkspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot iWorkspaceRoot = iWorkspace.getRoot();
		IProject[] iProjects = iWorkspaceRoot.getProjects();

		for (IProject project : iProjects) {
			try {
				if (project.isNatureEnabled("org.eclipse.jdt.core.javanature") && project.isOpen()) {
					System.out.println("Project : " + project.getName() + " is open!");
					IJavaProject iJavaProject = JavaCore.create(project);

					if (!iJavaProject.isOpen()) {
						iJavaProject.open(null);
					}

					IFolder iFolder = project.getFolder("src/test");
					List<IFile> fList = new ArrayList<>();
					if (iFolder.exists()) {
						traverse(fList, iFolder);
						for (IFile file : fList) {
							System.out.println(file.getName());
							ICompilationUnit iCompilationUnit = JavaCore.createCompilationUnitFrom(file);
							// for(IType type : iCompilationUnit.getTypes()){
							// IMethod[] method = type.getMethods();
							// System.out.println("IMethod : " +
							// method.toString());
							//// IMethod.
							//
							// }
						}
						System.out.println(fList.size());
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}

			// Test.runTest("org.apache.commons.lang3.AnnotationUtilsTest");
			// Test.runTest("Test");
		}

		System.out.println("Run normally");
		return null;
	}

	private void traverse(List<IFile> fList, IFolder iFolder) {
		if (iFolder == null) {
			return;
		}
		try {
			for (IResource resource : iFolder.members()) {
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					if (file.getName().endsWith(Configure.SOURCE_FILE_SUFFIX)) {
						fList.add(file);
					} else {
						// System.out.println("NOT TEST FILE :" +
						// file.getName());
					}
				} else if (resource instanceof IFolder) {
					IFolder folder = (IFolder) resource;
					traverse(fList, folder);
				} else {
					System.out.println("UNKNOWN :" + resource.getName());
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
