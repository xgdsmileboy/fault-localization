package localization.handler.instrument;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import localization.common.java.JavaFile;
import localization.common.java.JavaPackage;
import localization.common.java.JavaProject;
import localization.common.tools.AutoCodeFormatter;
import localization.common.tools.Console;
import localization.common.util.Configure;
import localization.common.util.Debugger;

public class DeInstrument extends AbstractHandler {

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
					removeCode(javaProject);
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

				IJavaElement ipackageElement = iCompilationUnit.getParent();
				while (ipackageElement.getElementType() != IJavaElement.PACKAGE_FRAGMENT) {
					ipackageElement = ipackageElement.getParent();
				}
				IPackageFragment iPackageFragment = (IPackageFragment) ipackageElement;

				JavaFile javaFile = new JavaFile(iCompilationUnit);
				removeCode(javaFile.getCompilcationUnit());

				try {
					iCompilationUnit = iPackageFragment.createCompilationUnit(iJavaElement.getElementName(),
							javaFile.getCompilcationUnit().toString(), true, null);
					AutoCodeFormatter.format(javaFile);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}

			}
		}
		return null;
	}

	private boolean removeCode(JavaProject javaProject) {
		for (JavaPackage javaPackage : javaProject.getJavaPackage()) {
			for (JavaFile javaFile : javaPackage.getJavaFiles()) {
				removeCode(javaFile.getCompilcationUnit());
				try {
					ICompilationUnit iCompilationUnit = javaPackage.getIPackageFragment().createCompilationUnit(
							javaFile.getICompilationUnit().getElementName(), javaFile.getCompilcationUnit().toString(),
							true, null);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	private boolean removeCode(CompilationUnit compilationUnit) {
		compilationUnit.accept(new DeInstrumentVisitor());
		return true;
	}

}

class DeInstrumentVisitor extends ASTVisitor {

	public boolean visit(Block node) {

		List<ASTNode> statements = new ArrayList<>();

		for (Object statement : node.statements()) {
			statements.add((ASTNode) ASTNode.copySubtree(AST.newAST(AST.JLS8), (ASTNode) statement));
		}

		node.statements().clear();
		for (ASTNode astNode : statements) {
			if (astNode instanceof ExpressionStatement) {
				ExpressionStatement expressionStatement = (ExpressionStatement) astNode;
				if (expressionStatement.getExpression() instanceof MethodInvocation) {
					MethodInvocation methodInvocation = (MethodInvocation) expressionStatement.getExpression();
					if (IsInstrumentation(methodInvocation)) {
						continue;
					}
				}
			} else if (astNode instanceof SwitchStatement) {
				SwitchStatement switchStatement = (SwitchStatement) astNode;
				List<ASTNode> swStatements = new ArrayList<>();
				AST ast = AST.newAST(AST.JLS8);
				for (Object object : switchStatement.statements()) {
					swStatements.add(ASTNode.copySubtree(ast, (ASTNode) object));
				}
				switchStatement.statements().clear();
				for (ASTNode swNode : swStatements) {
					if (swNode instanceof ExpressionStatement) {
						ExpressionStatement expressionStatement = (ExpressionStatement) swNode;
						if (expressionStatement.getExpression() instanceof MethodInvocation) {
							MethodInvocation methodInvocation = (MethodInvocation) expressionStatement.getExpression();
							if (IsInstrumentation(methodInvocation)) {
								continue;
							}
						}
					}
					switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), swNode));
				}

			}
			node.statements().add(ASTNode.copySubtree(node.getAST(), astNode));
		}

		return true;
	}

	private boolean IsInstrumentation(MethodInvocation node) {
		if (node.getName().getFullyQualifiedName().equals("println") && node.arguments() != null) {
			List<Object> args = node.arguments();
			if (args != null && args.get(0) instanceof StringLiteral) {
				StringLiteral stringLiteral = (StringLiteral) args.get(0);
				if (stringLiteral.getLiteralValue().startsWith(Configure.INSTRUMENT_FLAG)) {
					return true;
				}
			}
		}
		return false;
	}
}
