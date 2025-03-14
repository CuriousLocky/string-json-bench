package asr;

public abstract class AsrString {

	public static AsrString fromLiteral(String lit) {
		if (lit.getBytes().length == 1) {
			return new AsrString1(lit.getBytes()[0]);
		}

		return new AsrStringWrapper(lit);
	}

	public abstract int length();

	public abstract boolean equals(AsrString what);

	public abstract AsrString addConcat(AsrString other);

	public abstract AsrString substring(int from, int to);

	public abstract AsrString substring1(int from, int to);

	public String toJavaString() {
		throw new UnsupportedOperationException("please dont");
	}

	private static class AsrString1 extends AsrString {
		private static final AsrString1[] CACHE = new AsrString1[256];
		static {
			for (int i = 0; i < 256; i++) {
				CACHE[i] = new AsrString1((byte) i);
			}
		}

		private final byte val;

		AsrString1(byte val) {
			this.val = val;
		}

		@Override
		public int length() {
			return 1;
		}

		@Override
		public boolean equals(AsrString what) {
			if (what instanceof AsrString1) {
				return this.val == ((AsrString1) what).val;
				// } else if (what instanceof AsrStringWrapper wrapper) {
				// if (wrapper.length() != 1) {
				// return false;
				// }
				// return this.val == wrapper.target.charAt(0);
				// } else if (what instanceof AsrStringView view) {
				// if (view.length() != 1) {
				// return false;
				// }
				// return this.val == view.target.charAt(view.from);
				// }
			} else if (what == null || what.length() != 1) {
				return false;
			} else {
				// what has length 1
				if (what instanceof AsrStringWrapper w) {
					char firstChar = w.target.charAt(0);
					return this.val == firstChar;
					// return w.target.getBytes()[0] == this.val;
				} else if (what instanceof AsrStringView w) {
					// not reached in benchmark
					return w.materialize().getBytes()[0] == this.val;
				}
			}
			throw new UnsupportedOperationException("Not equal: " + what.getClass());
			// return false;
		}

		@Override
		public AsrString addConcat(AsrString other) {
			// System.out.print("String1 add Concat");
			// not reached in benchmark
			if (other instanceof AsrStringWrapper) {
				return new AsrStringWrapper(new String(new byte[] { this.val }) + ((AsrStringWrapper) other).target);
			}
			throw new UnsupportedOperationException("???");
		}

		public AsrString substring(int from, int to) {
			if (from - to == 0 && from == 0) {
				return new AsrStringWrapper("");
			} else if (from - to == 1 && from == 0) {
				return this;
			}
			throw new StringIndexOutOfBoundsException("??");
		}

		public AsrString substring1(int from, int to) {
			if (from - to == 0 && from == 0) {
				return this;
			}
			throw new StringIndexOutOfBoundsException("??");
		}
	}

	private static class AsrStringView extends AsrString {

		private final int from;
		private final int to;
		private final String target;

		AsrStringView(String wrapped, int from, int to) {
			this.target = wrapped;
			this.from = from;
			this.to = to;
		}

		@Override
		public int length() {
			return to - from;
		}

		@Override
		public boolean equals(AsrString what) {
			if (what instanceof AsrStringView) {
				AsrStringView view = (AsrStringView) what;
				if (view.target == this.target) {
					return this.from == view.from && this.to == view.to;
				} else {
					String sub = this.target.substring(from, to);
					return sub.equals(view.target.substring(view.from, view.to));
				}
			} else if (what == null) {
				return false;
			} else {
				return what.equals(this);
			}
		}

		@Override
		public AsrString addConcat(AsrString other) {
			if (other instanceof AsrStringWrapper) {
				// System.out.print("1");
				// not reached in benchmark
				return new AsrStringWrapper(materialize() + ((AsrStringWrapper) other).target);
			}
			throw new UnsupportedOperationException("???");
		}

		public AsrString substring(int from, int to) {
			System.out.print("2");
			// not reached in benchmark
			return new AsrStringWrapper(materialize().substring(from, to));
		}

		public AsrString substring1(int from, int to) {
			// not reached in benchmark
			return AsrString1.CACHE[this.target.charAt(from + this.from)];
		}

		private String materialize() {
			return this.target.substring(this.from, this.to);
		}

	}

	private static class AsrStringWrapper extends AsrString {
		private String target;

		AsrStringWrapper(String wrapped) {
			this.target = wrapped;
		}

		@Override
		public int length() {
			return target.length();
		}

		@Override
		public boolean equals(AsrString what) {
			if (what instanceof AsrStringWrapper) {
				return this.target.equals(((AsrStringWrapper) what).target);
			} else if (what == null) {
				return false;
			} else if (what instanceof AsrStringView w) {
				if (w.length() != this.length()) {
					return false;
				} else {
					// System.out.print("3 " + what.length());
					return this.target.equals(w.materialize());
				}
			}
			throw new UnsupportedOperationException("Not equal: " + what.getClass());
		}

		@Override
		public AsrString addConcat(AsrString other) {
			if (other instanceof AsrStringWrapper) {
				return new AsrStringWrapper(this.target + ((AsrStringWrapper) other).target);
			}
			throw new UnsupportedOperationException("???");
		}

		public AsrString substring(int from, int to) {
			// boundary check
			int length = this.length();
			if (from < 0 || from > to || to > length) {
				throw new UnsupportedOperationException("out of bound");
			}
			if (to - from == 1) {
				char content = this.target.charAt(from);
				byte contentByte = (byte) content;
				if (contentByte == content) {
					return AsrString1.CACHE[contentByte];
				}
			}
			// System.out.println(this.target.substring(from, to));
			return new AsrStringView(this.target, from, to);
			// return new AsrStringWrapper(this.target.substring(from, to));
		}

		public AsrString substring1(int from, int to) {
			return AsrString1.CACHE[this.target.charAt(from)];
		}

	}
}