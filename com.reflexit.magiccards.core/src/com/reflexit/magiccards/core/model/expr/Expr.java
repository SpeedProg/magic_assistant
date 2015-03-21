package com.reflexit.magiccards.core.model.expr;

public abstract class Expr {
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

		@Override
		public Expr not() {
			return FALSE;
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
		public Expr not() {
			return TRUE;
		};

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

		@Override
		public Expr not() {
			return this;
		};
	};

	public boolean evaluate(Object o) {
		return false;
	}

	public Object getFieldValue(Object o) {
		return null;
	}

	public Expr and(Expr b) {
		if (b == Expr.EMPTY || b == Expr.TRUE)
			return this;
		if (b == Expr.FALSE)
			return Expr.FALSE;
		return new BinaryExpr(this, Operation.AND, b);
	}

	public Expr or(Expr b) {
		if (b == Expr.EMPTY)
			return this;
		if (b == Expr.TRUE)
			return Expr.TRUE;
		if (b == Expr.FALSE)
			return this;
		return new BinaryExpr(this, Operation.OR, b);
	}

	public Expr not() {
		return new NotExpr(this);
	}

	public static Expr valueOf(boolean b) {
		if (b)
			return TRUE;
		return FALSE;
	}
}