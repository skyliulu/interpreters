package com.interpreters.lox;

import java.util.List;

public abstract class Stmt {
	public interface Visitor<R> {
		R visitExpressionStmt(Expression stmt);

		R visitPrintStmt(Print stmt);

		R visitVarStmt(Var stmt);

		R visitBlockStmt(Block stmt);

		R visitIfStmt(If stmt);

		R visitWhileStmt(While stmt);

		R visitBreakStmt(Break stmt);

		R visitFunctionStmt(Function stmt);

		R visitReturnStmt(Return stmt);

		R visitClassStmt(Class stmt);

	}


	abstract <R> R accept(Visitor<R> visitor);

	public static class Expression extends Stmt {
		private final Expr expression;

		public Expression(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitExpressionStmt(this);
		}

		public Expr getExpression() {
			return expression;
		}

	}

	public static class Print extends Stmt {
		private final Expr expression;

		public Print(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitPrintStmt(this);
		}

		public Expr getExpression() {
			return expression;
		}

	}

	public static class Var extends Stmt {
		private final Token name;
		private final Expr initializer;

		public Var(Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVarStmt(this);
		}

		public Token getName() {
			return name;
		}

		public Expr getInitializer() {
			return initializer;
		}

	}

	public static class Block extends Stmt {
		private final List<Stmt> statements;

		public Block(List<Stmt> statements) {
			this.statements = statements;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBlockStmt(this);
		}

		public List<Stmt> getStatements() {
			return statements;
		}

	}

	public static class If extends Stmt {
		private final Expr condition;
		private final Stmt thenStatement;
		private final Stmt elseStatement;

		public If(Expr condition, Stmt thenStatement, Stmt elseStatement) {
			this.condition = condition;
			this.thenStatement = thenStatement;
			this.elseStatement = elseStatement;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitIfStmt(this);
		}

		public Expr getCondition() {
			return condition;
		}

		public Stmt getThenStatement() {
			return thenStatement;
		}

		public Stmt getElseStatement() {
			return elseStatement;
		}

	}

	public static class While extends Stmt {
		private final Expr condition;
		private final Stmt body;

		public While(Expr condition, Stmt body) {
			this.condition = condition;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitWhileStmt(this);
		}

		public Expr getCondition() {
			return condition;
		}

		public Stmt getBody() {
			return body;
		}

	}

	public static class Break extends Stmt {
		private final Token keyword;

		public Break(Token keyword) {
			this.keyword = keyword;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBreakStmt(this);
		}

		public Token getKeyword() {
			return keyword;
		}

	}

	public static class Function extends Stmt {
		private final Token name;
		private final Expr.Function function;

		public Function(Token name, Expr.Function function) {
			this.name = name;
			this.function = function;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionStmt(this);
		}

		public Token getName() {
			return name;
		}

		public Expr.Function getFunction() {
			return function;
		}

	}

	public static class Return extends Stmt {
		private final Token keyword;
		private final Expr value;

		public Return(Token keyword, Expr value) {
			this.keyword = keyword;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitReturnStmt(this);
		}

		public Token getKeyword() {
			return keyword;
		}

		public Expr getValue() {
			return value;
		}

	}

	public static class Class extends Stmt {
		private final Token name;
		private final Expr.Variable superclass;
		private final List<Stmt.Function> methods;
		private final List<Stmt.Function> classMethods;

		public Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt.Function> classMethods) {
			this.name = name;
			this.superclass = superclass;
			this.methods = methods;
			this.classMethods = classMethods;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitClassStmt(this);
		}

		public Token getName() {
			return name;
		}

		public Expr.Variable getSuperclass() {
			return superclass;
		}

		public List<Stmt.Function> getMethods() {
			return methods;
		}

		public List<Stmt.Function> getClassMethods() {
			return classMethods;
		}

	}

}
