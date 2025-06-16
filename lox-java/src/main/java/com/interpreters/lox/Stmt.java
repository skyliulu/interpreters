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

}
