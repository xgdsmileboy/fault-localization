package localization.instrument.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchStatement;

import localization.common.util.Constant;

public class DeInstrumentVisitor extends TraversalVisitor {

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
			if (args != null && args.size() > 0 && args.get(0) instanceof StringLiteral) {
				StringLiteral stringLiteral = (StringLiteral) args.get(0);
				if (stringLiteral.getLiteralValue().startsWith(Constant.INSTRUMENT_FLAG)) {
					return true;
				}
			}
		}
		return false;
	}
}

