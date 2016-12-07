package localization.runtest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.launcher.JUnitMigrationDelegate;
import org.eclipse.jdt.internal.junit.launcher.TestKindRegistry;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.junit.JUnitCore;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import localization.runtest.listener.MockTestRunListener;

@SuppressWarnings("restriction")
public class ConfigAndRunTest {

	public ConfigAndRunTest() {
	}

	
	public static URLClassLoader getRuntimeClassLoader(IJavaProject javaProject)
			throws CoreException, MalformedURLException {
		ILaunchConfigurationWorkingCopy wc = createLaunchConfiguration(javaProject);
		JUnitLaunchConfigurationDelegate delegate = new JUnitLaunchConfigurationDelegate();
		String[] classpath = delegate.getClasspath(wc);
//		System.out.println(">>> Creating JUnit runtime classloader with entries:");
		URL[] classpathURLs = new URL[classpath.length];
		for (int i = 0; i < classpathURLs.length; i++) {
			System.out.println(classpath[i]);
			classpathURLs[i] = new File(classpath[i]).toURI().toURL();
		}
//		System.out.println("<<< Creating JUnit runtime classloader");
		URLClassLoader classLoader = new URLClassLoader(classpathURLs);
		return classLoader;
	}

	public static ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element) throws CoreException {
		final String testName;
		final String mainTypeQualifiedName;
		final String containerHandleId;

		switch (element.getElementType()) {
		case IJavaElement.JAVA_PROJECT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
		case IJavaElement.PACKAGE_FRAGMENT: {
			String name = element.getElementName();
			containerHandleId = element.getHandleIdentifier();
			mainTypeQualifiedName = "";
			testName = name.substring(name.lastIndexOf(IPath.SEPARATOR) + 1);
		}
			break;
		case IJavaElement.TYPE: {
			containerHandleId = "";
			mainTypeQualifiedName = ((IType) element).getFullyQualifiedName('.'); 
			testName = element.getElementName();
		}
			break;
		case IJavaElement.METHOD: {
			IMethod method = (IMethod) element;
			containerHandleId = "";
			mainTypeQualifiedName = method.getDeclaringType().getFullyQualifiedName('.');
			testName = method.getDeclaringType().getElementName() + '.' + method.getElementName();
		}
			break;
		default:
			throw new IllegalArgumentException(
					"Invalid element type to create a launch configuration: " + element.getClass().getName()); 
		}

		String testKindId = TestKindRegistry.getContainerTestKindId(element);

		ILaunchConfigurationType configType = getLaunchManager()
				.getLaunchConfigurationType(JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION);
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
				getLaunchManager().generateLaunchConfigurationName(testName));

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeQualifiedName);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, element.getJavaProject().getElementName());
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, containerHandleId);
		wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND, testKindId);
		JUnitMigrationDelegate.mapResources(wc);
		// AssertionVMArg.setArgDefault(wc);
		if (element instanceof IMethod) {
			wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME, element.getElementName()); 
		}
		return wc;
	}

	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public static TestRunSession runAsJUnit(IJavaElement element) throws CoreException, Exception, DebugException {
		ILaunchConfigurationWorkingCopy launchConf = createLaunchConfiguration(element);
		final MockTestRunListener listener = new MockTestRunListener();
		JUnitCore.addTestRunListener(listener);
		try {
			final ILaunch launch = launchConf.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor(), true);
			while (!launch.isTerminated()) {
		        Thread.sleep(500);
		    }
		} finally {
			JUnitCore.removeTestRunListener(listener);
		}
		return (TestRunSession) listener.geTestRunSessio();
	}

}
