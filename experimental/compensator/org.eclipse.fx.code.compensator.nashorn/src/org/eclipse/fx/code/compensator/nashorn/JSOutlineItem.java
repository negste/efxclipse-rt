package org.eclipse.fx.code.compensator.nashorn;

import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.eclipse.fx.code.compensator.editor.Outline.OutlineItem;

public class JSOutlineItem implements OutlineItem {
		private final String name;
		private final ObservableList<OutlineItem> list = FXCollections.observableArrayList();
		private final String type;

		public JSOutlineItem(String name, String type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String getLabel() {
			return name;
		}

		@Override
		public ObservableList<OutlineItem> getChildren() {
			return list;
		}

		@Override
		public List<String> getStyle() {
			return type == null ? Collections.emptyList() : Collections.singletonList(type);
		}
	}