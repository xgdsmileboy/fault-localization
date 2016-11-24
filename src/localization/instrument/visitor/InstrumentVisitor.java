package localization.instrument.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import localization.common.util.Configure;
import localization.common.util.Debugger;

public class InstrumentVisitor extends TraversalVisitor {

	private String KEY_METHOD = " @METHOD ";
	private String KEY_TYPE = " @TYPE ";
	private String SEGMENT_SEPARATOR = " #";
	private String DOT_SEPARATOR = ".";

	private String clazzName = "";
	private String clazzFileName = "";
	private int insertCount = 0;
	private CompilationUnit cu;

	@Override
	public boolean visit(CompilationUnit node) {
		cu = node;
		clazzName = node.getPackage().getName().getFullyQualifiedName();
		for (Object object : node.types()) {
			if (object instanceof TypeDeclaration) {
				TypeDeclaration type = (TypeDeclaration) object;
				if (Modifier.isPublic(type.getModifiers())) {
					clazzName += DOT_SEPARATOR + type.getName().getFullyQualifiedName();
					clazzFileName = clazzName;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {

		if (!Modifier.isPublic(node.getModifiers())) {
			clazzName = clazzFileName + "$" + node.getName().getFullyQualifiedName();
		}
		return true;

	}

	@Override
	public boolean visit(MethodDeclaration node) {
		String location = KEY_TYPE + clazzName + SEGMENT_SEPARATOR + KEY_METHOD
				+ node.getName().getFullyQualifiedName();
		String message = Configure.INSTRUMENT_FLAG + location;
		Block methodBody = node.getBody();

		if (methodBody == null) {
			return true;
		}

		List<Statement> blockStatement = new ArrayList<>();

		for (Object object : methodBody.statements()) {
			if (object instanceof Statement) {
				Statement statement = (Statement) object;
				blockStatement.addAll(process(statement, message));
			} else {
				if (Debugger.debugOn) {
					Debugger.debug(
							"@Instruction$InstrumentVisitor #visit UNKNOWN statement type : " + object.toString());
				}
			}
		}
		methodBody.statements().clear();
		for (Statement statement : blockStatement) {
			methodBody.statements().add(ASTNode.copySubtree(methodBody.getAST(), statement));
		}

		return true;
	}

	private List<Statement> process(Statement statement, String message) {

		List<Statement> result = new ArrayList<>();

		if (statement instanceof IfStatement) {
			IfStatement ifStatement = (IfStatement) statement;

			Statement thenBody = ifStatement.getThenStatement();
			if (thenBody != null) {
				int lineNumber = cu.getLineNumber(ifStatement.getExpression().getStartPosition());
				Statement insert = genASTNode(message, lineNumber);
				Block thenBlock = null;
				if (thenBody instanceof Block) {
					thenBlock = (Block) thenBody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					thenBlock = ast.newBlock();
					thenBlock.statements().add(ASTNode.copySubtree(thenBlock.getAST(), thenBody));
				}

				Block newThenBlock = processBlock(thenBlock, insert, message);
				ifStatement.setThenStatement((Statement) ASTNode.copySubtree(ifStatement.getAST(), newThenBlock));
			}

			Statement elseBody = ifStatement.getElseStatement();
			if (elseBody != null) {
				Block elseBlock = null;
				if (elseBody instanceof Block) {
					elseBlock = (Block) elseBody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					elseBlock = ast.newBlock();
					elseBlock.statements().add(ASTNode.copySubtree(elseBlock.getAST(), elseBody));
				}
				Block newElseBlock = processBlock(elseBlock, null, message);
				ifStatement.setElseStatement((Statement) ASTNode.copySubtree(ifStatement.getAST(), newElseBlock));
			}
			result.add(ifStatement);
		} else if (statement instanceof WhileStatement) {

			WhileStatement whileStatement = (WhileStatement) statement;
			Statement whilebody = whileStatement.getBody();
			if (whilebody != null) {
				Block whileBlock = null;
				if (whilebody instanceof Block) {
					whileBlock = (Block) whilebody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					whileBlock = ast.newBlock();
					whileBlock.statements().add(ASTNode.copySubtree(whileBlock.getAST(), whilebody));
				}

				int lineNumber = cu.getLineNumber(whileStatement.getExpression().getStartPosition());
				Statement insert = genASTNode(message, lineNumber);
				Block newWhileBlock = processBlock(whileBlock, insert, message);
				whileStatement.setBody((Statement) ASTNode.copySubtree(whileStatement.getAST(), newWhileBlock));
			}

			result.add(whileStatement);
		} else if (statement instanceof ForStatement) {

			ForStatement forStatement = (ForStatement) statement;
			Statement forBody = forStatement.getBody();
			if (forBody != null) {
				Block forBlock = null;
				if (forBody instanceof Block) {
					forBlock = (Block) forBody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					forBlock = ast.newBlock();
					forBlock.statements().add(ASTNode.copySubtree(forBlock.getAST(), forBody));
				}

				int position = forStatement.getStartPosition();
				if (forStatement.getExpression() != null) {
					position = forStatement.getExpression().getStartPosition();
				} else if (forStatement.initializers() != null && forStatement.initializers().size() > 0) {
					position = ((ASTNode) forStatement.initializers().get(0)).getStartPosition();
				} else if (forStatement.updaters() != null && forStatement.updaters().size() > 0) {
					position = ((ASTNode) forStatement.updaters().get(0)).getStartPosition();
				}
				int lineNumber = cu.getLineNumber(position);
				Statement insert = genASTNode(message, lineNumber);
				Block newForBlock = processBlock(forBlock, insert, message);
				forStatement.setBody((Statement) ASTNode.copySubtree(forStatement.getAST(), newForBlock));
			}

			result.add(forStatement);
		} else if (statement instanceof DoStatement) {

			DoStatement doStatement = (DoStatement) statement;
			Statement doBody = doStatement.getBody();
			if (doBody != null) {
				Block doBlock = null;
				if (doBody instanceof Block) {
					doBlock = (Block) doBody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					doBlock = ast.newBlock();
					doBlock.statements().add(ASTNode.copySubtree(doBlock.getAST(), doBody));
				}

				Block newDoBlock = processBlock(doBlock, null, message);
				ASTNode lastStatement = (ASTNode) newDoBlock.statements().get(newDoBlock.statements().size() - 1);
				if (!(lastStatement instanceof BreakStatement || lastStatement instanceof ReturnStatement)) {
					int lineNumber = cu.getLineNumber(doStatement.getExpression().getStartPosition());
					Statement insert = genASTNode(message, lineNumber);
					newDoBlock.statements().add(ASTNode.copySubtree(newDoBlock.getAST(), insert));
				}

				doStatement.setBody((Statement) ASTNode.copySubtree(doStatement.getAST(), newDoBlock));
			}

			result.add(doStatement);
		} else if (statement instanceof Block) {
			Block block = (Block) statement;
			Block newBlock = processBlock(block, null, message);
			result.add(newBlock);
		} else if (statement instanceof EnhancedForStatement) {

			EnhancedForStatement enhancedForStatement = (EnhancedForStatement) statement;
			Statement enhancedBody = enhancedForStatement.getBody();
			if (enhancedBody != null) {
				Block enhancedBlock = null;
				if (enhancedBody instanceof Block) {
					enhancedBlock = (Block) enhancedBody;
				} else {
					AST ast = AST.newAST(AST.JLS8);
					enhancedBlock = ast.newBlock();
					enhancedBlock.statements().add(ASTNode.copySubtree(enhancedBlock.getAST(), enhancedBody));
				}

				int lineNumber = cu.getLineNumber(enhancedForStatement.getExpression().getStartPosition());
				Statement insert = genASTNode(message, lineNumber);
				Block newEnhancedBlock = processBlock(enhancedBlock, insert, message);
				enhancedForStatement
						.setBody((Statement) ASTNode.copySubtree(enhancedForStatement.getAST(), newEnhancedBlock));
			}

			result.add(enhancedForStatement);
		} else if (statement instanceof SwitchStatement) {

			SwitchStatement switchStatement = (SwitchStatement) statement;
			List<ASTNode> statements = new ArrayList<>();
			AST ast = AST.newAST(AST.JLS8);
			for (Object object : switchStatement.statements()) {
				ASTNode astNode = (ASTNode) object;
				statements.add(ASTNode.copySubtree(ast, astNode));
			}

			switchStatement.statements().clear();

			for (ASTNode astNode : statements) {
				if (astNode instanceof Statement) {
					Statement s = (Statement) astNode;
					for (Statement statement2 : process(s, message)) {
						switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), statement2));
					}
				} else {
					switchStatement.statements().add(ASTNode.copySubtree(switchStatement.getAST(), astNode));
				}
			}

			int lineNumber = cu.getLineNumber(switchStatement.getExpression().getStartPosition());
			Statement insert = genASTNode(message, lineNumber);

			result.add(insert);
			result.add(switchStatement);
		} else if (statement instanceof TryStatement) {

			TryStatement tryStatement = (TryStatement) statement;

			Block tryBlock = tryStatement.getBody();
			if (tryBlock != null) {
				Block newTryBlock = processBlock(tryBlock, null, message);
				tryStatement.setBody((Block) ASTNode.copySubtree(tryStatement.getAST(), newTryBlock));
			}

			List catchList = tryStatement.catchClauses();
			for (Object object : catchList) {
				if (object instanceof CatchClause) {
					CatchClause catchClause = (CatchClause) object;
					Block catchBlock = catchClause.getBody();
					Block newCatchBlock = processBlock(catchBlock, null, message);
					catchClause.setBody((Block) ASTNode.copySubtree(catchClause.getAST(), newCatchBlock));
				}
			}

			Block finallyBlock = tryStatement.getFinally();
			if (finallyBlock != null) {
				Block newFinallyBlock = processBlock(finallyBlock, null, message);
				tryStatement.setFinally((Block) ASTNode.copySubtree(tryStatement.getAST(), newFinallyBlock));
			}

			result.add(tryStatement);
		} else {
			int lineNumber = cu.getLineNumber(statement.getStartPosition());
			Statement copy = (Statement) ASTNode.copySubtree(AST.newAST(AST.JLS8), statement);
			Statement insert = genASTNode(message, lineNumber);

			if (statement instanceof ConstructorInvocation) {
				result.add(copy);
				result.add(insert);
			} else if (statement instanceof ContinueStatement || statement instanceof BreakStatement
					|| statement instanceof ReturnStatement || statement instanceof ThrowStatement
					|| statement instanceof AssertStatement || statement instanceof ExpressionStatement
					|| statement instanceof ConstructorInvocation
					|| statement instanceof VariableDeclarationStatement) {
				result.add(insert);
				result.add(copy);

			} else if (statement instanceof LabeledStatement) {
				result.add(copy);
			} else if (statement instanceof SynchronizedStatement) {
				result.add(copy);
			} else {
				result.add(copy);
			}
		}

		return result;
	}

	private Block processBlock(Block block, Statement insert, String message) {
		Block newBlock = AST.newAST(AST.JLS8).newBlock();
		if (block == null) {
			return newBlock;
		}
		if (insert != null) {
			newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), insert));
		}
		for (Object object : block.statements()) {
			if (object instanceof Statement) {
				Statement statement = (Statement) object;
				List<Statement> newStatements = process(statement, message);
				for (Statement newStatement : newStatements) {
					newBlock.statements().add(ASTNode.copySubtree(newBlock.getAST(), newStatement));
				}
			} else {
				if (Debugger.debugOn) {
					Debugger.debug("@Instrument$InstrumentVisitor #process UNKNOWN astnode : " + object.toString());
				}
			}
		}
		return newBlock;
	}

	private Statement genASTNode(String locMessage, int line) {

		AST ast = AST.newAST(AST.JLS8);
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setExpression(ast.newName("System.out"));
		methodInvocation.setName(ast.newSimpleName("println"));
		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue(locMessage + " <" + line + ">");
		methodInvocation.arguments().add(stringLiteral);
		ExpressionStatement expressionStatement = ast.newExpressionStatement(methodInvocation);
		// insertCount ++;
		return expressionStatement;
	}

}
