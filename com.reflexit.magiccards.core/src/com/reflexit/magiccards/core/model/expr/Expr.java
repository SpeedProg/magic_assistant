package com.reflexit.magiccards.core.model.expr;


public class Expr {
	public static Expr TRUE = new Expr() {
		@Override
		public boolean evaluate(Object o) {
			return true;
		}

		@Override
		public Object getFieldValue(Object o) {
			return Boolean.TRUE;
		}

		@Override
		public String toString() {
			return "true";
		}

		@Override
		public Expr and(Expr b) {
			return b;
		};

		@Override
		public Expr or(Expr b) {
			return this;
		};
	};
	public static Expr FALSE = new Expr() {
		@Override
		public boolean evaluate(Object o) {
			return false;
		}

		@Override
		public Object getFieldValue(Object o) {
			return Boolean.FALSE;
		}

		@Override
		public String toString() {
			return "false";
		}

		@Override
		public Expr and(Expr b) {
			return this;
		};

		@Override
		public Expr or(Expr b) {
			return b;
		};
	};
	public static Expr EMPTY = new Expr() {
		@Override
		public boolean evaluate(Object o) {
			return true;
		}

		@Override
		public Object getFieldValue(Object o) {
			return Boolean.TRUE;
		}

		@Override
		public String toString() {
			return "true";
		}

		@Override
		public Expr and(Expr b) {
			return b;
		};

		@Override
		public Expr or(Expr b) {
			return b;
		};
	};
	boolean translated = false;

	public boolean evaluate(Object o) {
		return false;
	}

	public Object getFieldValue(Object o) {
		return null;
	}

	public Expr and(Expr b) {
		if (b == Expr.EMPTY)
			return this;
		else
			return new BinaryExpr(this, Operation.AND, b);
	}

	public Expr or(Expr b) {
		if (b == Expr.EMPTY)
			return this;
		else
			return new BinaryExpr(this, Operation.OR, b);
	}

	public Expr not(Expr b) {
		if (b == Expr.EMPTY)
			return Expr.EMPTY;
		return new NotExpr(b);
	}

	public Expr translate() {
		if (translated)
			return this;
		this.translated = true;
		return this;
	}

	public static Expr valueOf(boolean b) {
		if (b)
			return TRUE;
		return FALSE;
	}
}