package com.reflexit.magiccards.ui.views;

public enum Presentation {
	TABLE {
		@Override
		public String getLabel() {
			return "Table";
		}
	},
	TREE {
		@Override
		public String getLabel() {
			return "Tree";
		}
	},
	SPLITTREE {
		@Override
		public String getLabel() {
			return "Split Table";
		}
	},
	GALLERY {
		@Override
		public String getLabel() {
			return "Gallery";
		}
	};
	public String getLabel() {
		return name();
	}

	public String key() {
		return name();
	}
}