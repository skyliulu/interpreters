package com.interpreters.lox;

import java.util.List;

public abstract class Expr {
	public interface Visitor<R> {
		R visitAssignmentExpr(Assignment expr);

		R visitTernaryExpr(Ternary expr);

		R visitBinaryExpr(Binary expr);

		R visitGroupingExpr(Grouping expr);

		R visitLiteralExpr(Literal expr);

		R visitUnaryExpr(Unary expr);

		R visitVariableExpr(Variable expr);

		R visitLogicalExpr(Logical expr);

		R visitCallExpr(Call expr);

		R visitFunctionExpr(Function expr);

		R visitGetExpr(Get expr);

		R visitSetExpr(Set expr);

	}


	abstract <R> R accept(Visitor<R> visitor);

	public static class Assignment extends Expr {
		private final Token name;
		private final Expr value;

		public Assignment(Token name, Expr value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitAssignmentExpr(this);
		}

		public Token getName() {
			return name;
		}

		public Expr getValue() {
			return value;
		}

	}

	public static class Ternary extends Expr {
		private final Expr expr;
		private final Expr thenBranch;
		private final Expr elseBranch;

		public Ternary(Expr expr, Expr thenBranch, Expr elseBranch) {
			this.expr = expr;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitTernaryExpr(this);
		}

		public Expr getExpr() {
			return expr;
		}

		public Expr getThenBranch() {
			return thenBranch;
		}

		public Expr getElseBranch() {
			return elseBranch;
		}

	}

	public static class Binary extends Expr {
		private final Expr left;
		private final Token operator;
		private final Expr right;

		public Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitBinaryExpr(this);
		}

		public Expr getLeft() {
			return left;
		}

		public Token getOperator() {
			return operator;
		}

		public Expr getRight() {
			return right;
		}

	}

	public static class Grouping extends Expr {
		private final Expr expression;

		public Grouping(Expr expression) {
			this.expression = expression;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGroupingExpr(this);
		}

		public Expr getExpression() {
			return expression;
		}

	}

	public static class Literal extends Expr {
		private final Object value;

		public Literal(Object value) {
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLiteralExpr(this);
		}

		public Object getValue() {
			return value;
		}

	}

	public static class Unary extends Expr {
		private final Token operator;
		private final Expr right;

		public Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitUnaryExpr(this);
		}

		public Token getOperator() {
			return operator;
		}

		public Expr getRight() {
			return right;
		}

	}

	public static class Variable extends Expr {
		private final Token name;

		public Variable(Token name) {
			this.name = name;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitVariableExpr(this);
		}

		public Token getName() {
			return name;
		}

	}

	public static class Logical extends Expr {
		private final Expr left;
		private final Token operator;
		private final Expr right;

		public Logical(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitLogicalExpr(this);
		}

		public Expr getLeft() {
			return left;
		}

		public Token getOperator() {
			return operator;
		}

		public Expr getRight() {
			return right;
		}

	}

	public static class Call extends Expr {
		private final Expr callee;
		private final Token paren;
		private final List<Expr> arguments;

		public Call(Expr callee, Token paren, List<Expr> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitCallExpr(this);
		}

		public Expr getCallee() {
			return callee;
		}

		public Token getParen() {
			return paren;
		}

		public List<Expr> getArguments() {
			return arguments;
		}

	}

	public static class Function extends Expr {
		private final List<Token> params;
		private final List<Stmt> body;

		public Function(List<Token> params, List<Stmt> body) {
			this.params = params;
			this.body = body;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitFunctionExpr(this);
		}

		public List<Token> getParams() {
			return params;
		}

		public List<Stmt> getBody() {
			return body;
		}

	}

	public static class Get extends Expr {
		private final Expr object;
		private final Token name;

		public Get(Expr object, Token name) {
			this.object = object;
			this.name = name;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitGetExpr(this);
		}

		public Expr getObject() {
			return object;
		}

		public Token getName() {
			return name;
		}

	}

	public static class Set extends Expr {
		private final Expr object;
		private final Token name;
		private final Expr value;

		public Set(Expr object, Token name, Expr value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}

		@Override
		public <R> R accept(Visitor<R> visitor) {
			return visitor.visitSetExpr(this);
		}

		public Expr getObject() {
			return object;
		}

		public Token getName() {
			return name;
		}

		public Expr getValue() {
			return value;
		}

	}

}
