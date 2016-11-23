package localization.common.java;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class JavaFile {

	ICompilationUnit iCompilationUnit;

	CompilationUnit compilationUnit;

	public JavaFile(ICompilationUnit iCompilationUnit) {
		this.iCompilationUnit = iCompilationUnit;
		this.compilationUnit = CompileUnit.genASTFromICU(iCompilationUnit);
	}

//	private CompilationUnit genASTFromICU(ICompilationUnit icu) {
//		ASTParser astParser = ASTParser.newParser(AST.JLS8);
//		Map<?, ?> options = JavaCore.getOptions();
//		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
//		astParser.setCompilerOptions(options);
//		astParser.setSource(icu);
//		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
//		astParser.setResolveBindings(true);
//		return (CompilationUnit) astParser.createAST(null);
//	}

	public CompilationUnit getCompilcationUnit() {
		return this.compilationUnit;
	}

	public ICompilationUnit getICompilationUnit() {
		return this.iCompilationUnit;
	}

}
