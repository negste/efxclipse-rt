/*******************************************************************************
 * Copyright (c) 2016 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Tom Schindl<tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.fx.ui.controls.styledtext.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.fx.ui.controls.Util;
import org.eclipse.fx.ui.controls.styledtext.StyledTextArea;
import org.eclipse.fx.ui.controls.styledtext.StyledTextContent;
import org.eclipse.fx.ui.controls.styledtext.StyledTextContent.TextChangeListener;
import org.eclipse.fx.ui.controls.styledtext.events.HoverTarget;
import org.eclipse.fx.ui.controls.styledtext.TextChangedEvent;
import org.eclipse.fx.ui.controls.styledtext.TextChangingEvent;
import org.eclipse.fx.ui.controls.styledtext.TextSelection;
import org.eclipse.fx.ui.controls.styledtext.model.TextAnnotationPresenter;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import javafx.beans.Observable;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

@SuppressWarnings("javadoc")
public class ContentView  extends Pane {

	private SetProperty<TextAnnotationPresenter> textAnnotationPresenter = new SimpleSetProperty<>(FXCollections.observableSet());
	public SetProperty<TextAnnotationPresenter> textAnnotationPresenterProperty() {
		return this.textAnnotationPresenter;
	}

	private class LineLayer extends VFlow<LineNode> {

		public LineLayer(Supplier<LineNode> nodeFactory, BiConsumer<LineNode, Integer> nodePopulator) {
			super(nodeFactory, nodePopulator);

			setOnRelease(n->n.release());
			setOnActivate((idx, n)->n.setIndex(idx.intValue()));
		}



//		protected void releaseNode(int lineIndex) {
//
//			get(model.get(lineIndex)).ifPresent(n->{
//				n.setVisible(false);
//				n.release();
//			});
//		}

//		protected void releaseNode(StyledTextLine line) {
//			if (debugOut) System.err.println("RELEASE " + line);
//			release(line);
////			get(line).ifPresent(n->{
////				n.setVisible(false);
////				n.release();
////			});
//		}

//		private void updateNode(StyledTextLine line) {
//			if (debugOut) System.err.println("UPDATE " + line);
//			LineNode node = get(line);
//			node.update(line, textAnnotationPresenter);
////			LineNode node = getCreate(m);
////			node.setVisible(true);
//////			node.setModel(m);
////			node.update(m, textAnnotationPresenter);
//		}

//		private void updateNode(int lineIndex, StyledTextLine m) {
//			LineNode node = getCreate(m);
//			node.setVisible(true);
////			node.setModel(m);
//			node.update(m, textAnnotationPresenter);
//		}

//		@Override
//		public void requestLayout() {
//			super.requestLayout();
//		}

//		@Override
//		protected void layoutChildren() {
//			if (debugOut) System.err.println("layout LineLayer");
//			ContiguousSet.create(visibleLines.get(), DiscreteDomain.integers()).forEach(e -> {
//				if (!yOffsetData.containsKey(e)) {
//					if (debugOut) System.err.println("NO yOffset FOR " + e);
//					return;
//				}
//				double x = 0;
//				double y = yOffsetData.get(e);
//				double width = getWidth();
//				double height = getLineHeight();
//
//				if (model.size() > e) {
//					StyledTextLine m = model.get(e);
//					LineNode lineNode = get(m);
//					lineNode.resizeRelocate(x, y, width, height);
//
//					if (debugOut) System.err.println("layout " + e + ": y=" + y + " line= " + lineNode);
//					if (debugOut) System.err.println("      visible = " + lineNode.isVisible());
//					if (debugOut) System.err.println("      bounds = " + lineNode.getBoundsInParent());
////					get(m).ifPresent(n->n.resizeRelocate(x, y, width, height));
//
//					lineNode.layout();
//				}
//			});
//		}

		@Override
		protected void releaseNode(int lineIndex) {
			super.releaseNode(lineIndex);

		}

		private Stream<LineNode> createVisibleLineNodesStream() {
			ContiguousSet<Integer> visibleIndexes = ContiguousSet.create(ContentView.this.visibleLines.get(), DiscreteDomain.integers());
//			return visibleIndexes.stream().filter(i->i<model.size()).map(idx->get(model.get(idx))).filter(n->n.isPresent()).map(n->n.get()).filter(n->n.isVisible());
			return visibleIndexes.stream()
					.filter(i -> i.intValue() < getNumberOfLines())
					.map(idx -> getVisibleNode(idx.intValue()))
					.filter(n -> n.isPresent()).map(n->n.get());
		}

		Optional<Integer> getLineIndex(javafx.geometry.Point2D point) {
			final Optional<LineNode> hitLine = createVisibleLineNodesStream().filter(n->n.getBoundsInParent().contains(point)).findFirst();
			final Optional<Integer> index = hitLine.map(n->{
				int i = n.getCaretIndexAtPoint(new javafx.geometry.Point2D(point.getX(), n.getHeight()/2));
				if (i >= 0 ) {
					return Integer.valueOf(n.getStartOffset() + i);
				}
				else {
					return Integer.valueOf(n.getEndOffset());
				}
			});
			return index;
		}



		public List<HoverTarget> findHoverTargets(Point2D localLocation) {
			ContiguousSet<Integer> visibleIndexes = ContiguousSet.create(ContentView.this.visibleLines.get(), DiscreteDomain.integers());
			return visibleIndexes.stream()
				.map(lineIndex->getVisibleNode(lineIndex))
				.filter(x->x.isPresent())
				.filter(x->x.get().getBoundsInParent().contains(localLocation))
				.flatMap(x->x.get().findHoverTargets(x.get().parentToLocal(localLocation)).stream())
				.collect(Collectors.toList());
		}


//		protected void permutate(int a, int b) {
//			LineNode nodeA = existingNodes.get(a);
//			LineNode nodeB = existingNodes.get(b);
//
//			existingNodes.put(a, nodeB);
//			existingNodes.put(b, nodeA);
//
//			System.err.println("  . permutate " + a + " -> " + b);
//		}
	}

	public List<HoverTarget> findHoverTargets(Point2D localLocation) {
		return this.lineLayer.findHoverTargets(localLocation);
	}

	private StackPane contentBody = new StackPane();

	private LineLayer lineLayer = new LineLayer(()->new LineNode(), (n, m)->{
		n.setLineHelper(getLineHelper());
		n.update(this.textAnnotationPresenter.get());
	});

//	private Predicate<Set<LineNode>> needsPresentation;

	private Map<Integer, Double> yOffsetData = new HashMap<>();

//	private boolean forceLayout = true;

	private DoubleProperty lineHeigth = new SimpleDoubleProperty(this, "lineHeight", 16.0); //$NON-NLS-1$
	public DoubleProperty lineHeightProperty() {
		return this.lineHeigth;
	}
	public double getLineHeight() {
		return this.lineHeigth.get();
	}
	public void setLineHeight(double lineHeight) {
		this.lineHeigth.set(lineHeight);
	}

	IntegerProperty numberOfLines = new SimpleIntegerProperty(this, "numberOfLines", 0); //$NON-NLS-1$
	public ReadOnlyIntegerProperty numberOfLinesProperty() {
		return this.numberOfLines;
	}
	public int getNumberOfLines() {
		return this.numberOfLines.get();
	}

	private IntegerProperty caretOffset = new SimpleIntegerProperty(this, "caretOffset", 0); //$NON-NLS-1$
	public IntegerProperty caretOffsetProperty() {
		return this.caretOffset;
	}

	private ObjectProperty<TextSelection> textSelection = new SimpleObjectProperty<>(this, "textSelection", new TextSelection(0, 0)); //$NON-NLS-1$
	public ObjectProperty<TextSelection> textSelectionProperty() {
		return this.textSelection;
	}

	private ObjectProperty<StyledTextContent> content = new SimpleObjectProperty<>(this, "content"); //$NON-NLS-1$
	public ObjectProperty<StyledTextContent> contentProperty() {
		return this.content;
	}
	public StyledTextContent getContent() {
		return this.content.get();
	}

	private LineHelper lineHelper;

	protected LineLayer getLineLayer() {
		return this.lineLayer;
	}

	protected LineHelper getLineHelper() {
		return this.lineHelper;
	}

//	private ObservableList<StyledTextLine> model;
//	private IntegerBinding modelSize;

//	public void setModel(ObservableList<StyledTextLine> model) {
//		this.model = model;
//
//
////		model.addListener((InvalidationListener)(x)->prepareNodes(getVisibleLines()));
//
////		model.addListener((ListChangeListener<StyledTextLine>)(c)->{
////			RangeSet<Integer> updateNodes = TreeRangeSet.create();
////
////			while (c.next()) {
////				if (debugOut) System.err.println("CHANGE [");
////				if (c.wasPermutated()) {
////					if (debugOut) System.err.println("-> permutation " + c);
//////					for (int i = c.getFrom(); i < c.getTo(); i++) {
//////						lineLayer.permutate(i, c.getPermutation(i));
//////					}
//////					lineLayer.requestLayout();
////				}
////				if (c.wasUpdated() || c.wasReplaced()) {
////					if (debugOut) System.err.println("-> updated or replaced: " + c.getFrom() + " - " + c.getTo());
////					updateNodes.add(Range.closedOpen(c.getFrom(), c.getTo()));
////
////
////				}
////				if (c.wasAdded()) {
////					if (debugOut) System.err.println("-> added: " + c.getFrom() + " - " + c.getTo());
////					updateNodes.add(Range.closedOpen(c.getFrom(), model.size()));
////				}
////				if (c.wasRemoved()) {
////					if (debugOut) System.err.println("-> removed: " + c.getFrom() + " - " + c.getTo());
////
////					c.getRemoved().forEach(line->lineLayer.releaseNode(line));
////
////					updateNodes.add(Range.closedOpen(c.getFrom(), model.size()));
////				}
////				if (debugOut) System.err.println("]");
////			}
////
////			updateNodesNow(updateNodes);
////		});
////
////		modelSize = Bindings.size(model);
////
////		modelSize.addListener((x, o, n)-> {
////			int newSize = n.intValue();
////			int oldSize = o.intValue();
////			if (newSize < oldSize) {
//////				for (int lineIdx = newSize; lineIdx < oldSize; lineIdx++) {
//////					lineLayer.releaseNode(lineIdx);
//////				}
////			}
////		});
//	}

//	private ListProperty<StyledTextLine> model = new SimpleListProperty<>(this, "model", FXCollections.observableArrayList());
//	public ListProperty<StyledTextLine> getModel() {
//		return this.model;
//	}

	ObjectProperty<Range<Integer>> visibleLines = new SimpleObjectProperty<>(this, "visibleLines", Range.closed(Integer.valueOf(0), Integer.valueOf(0))); //$NON-NLS-1$
	public ObjectProperty<Range<Integer>> visibleLinesProperty() {
		return this.visibleLines;
	}
	public com.google.common.collect.Range<Integer> getVisibleLines() {
		return this.visibleLines.get();
	}
	public void setVisibleLines(Range<Integer> visibleLines) {
		this.visibleLines.set(visibleLines);
	}

	private Range<Integer> curVisibleLines;
	private StyledTextArea area;

	public ContentView(LineHelper lineHelper, StyledTextArea area) {
		this.area = area;
		this.lineHelper = lineHelper;
//		setStyle("-fx-border-color: green; -fx-border-width:2px; -fx-border-style: dashed;");
		this.contentBody.setPadding(new Insets(2));
		this.contentBody.getChildren().setAll(this.lineLayer);

//		this.lineLayer.setStyle("-fx-border-color: orange; -fx-border-width:2px; -fx-border-style: solid;");


//		this.contentBody.setStyle("-fx-border-color: blue; -fx-border-width:2px; -fx-border-style: dotted;");


		this.getChildren().setAll(this.contentBody);



//		AnimationTimer t = new AnimationTimer() {
//			@Override
//			public void handle(long now) {
//				updatePulse(now);
//			}
//		};
//
//		visibleProperty().addListener((x, o, n)->{
//			if (n) {
//				t.start();
//			}
//			else {
//				t.stop();
//			}
//		});
//
//		t.start();
//		sceneProperty().addListener((x, o, n) -> {
//			if (n == null) {
//				t.stop();
//			}
//			else {
//				t.start();
//			}
//		});


		setMinWidth(200);
		setMinHeight(200);

		this.visibleLines.addListener(this::onLineChange);
		this.offsetX.addListener(this::onLineChange);



		this.lineLayer.lineHeightProperty().bind(this.lineHeigth);
		this.lineLayer.yOffsetProperty().bind(this.offsetY);
		this.lineLayer.visibleLinesProperty().bind(this.visibleLines);
		this.lineLayer.numberOfLinesProperty().bind(this.numberOfLines);

		bindContentListener();
		bindCaretListener();
		bindSelectionListener();
	}

	private void bindCaretListener() {
		this.caretOffset.addListener((x, o, n)-> {
			int oldCaretLine = getContent().getLineAtOffset(o.intValue());
			int newCaretLine = getContent().getLineAtOffset(n.intValue());

			RangeSet<Integer> toUpdate = TreeRangeSet.create();
			toUpdate.add(Range.closed(Integer.valueOf(oldCaretLine), Integer.valueOf(oldCaretLine)));
			toUpdate.add(Range.closed(Integer.valueOf(newCaretLine), Integer.valueOf(newCaretLine)));

			updateNodesNow(toUpdate);
		});
	}

	private Range<Integer> getAffectedLines(TextSelection selection) {
		int firstLine = getContent().getLineAtOffset(selection.offset);
		int lastLine = getContent().getLineAtOffset(selection.offset + selection.length);
		if (lastLine == -1) {
			lastLine = this.numberOfLines.get();
		}
		return Range.closed(Integer.valueOf(firstLine), Integer.valueOf(Math.min(lastLine, this.numberOfLines.get())));
	}

//	private static Range<Integer> toRange(TextSelection s) {
//		return Range.closedOpen(Integer.valueOf(s.offset), Integer.valueOf(s.offset + s.length));
//	}
//
//	private Range<Integer> toLineRange(Range<Integer> globalOffsetRange) {
//		int lower = getContent().getLineAtOffset(globalOffsetRange.lowerEndpoint().intValue());
//		int upper = getContent().getLineAtOffset(globalOffsetRange.upperEndpoint().intValue());
//		return Range.closed(Integer.valueOf(lower), Integer.valueOf(upper));
//	}

	private void bindSelectionListener() {
		this.textSelection.addListener((x, o, n) -> {
			RangeSet<Integer> toUpdate = TreeRangeSet.create();
			if (o != null) toUpdate.add(getAffectedLines(o));
			if (n != null) toUpdate.add(getAffectedLines(n));
			updateNodesNow(toUpdate);
		});
	}

	private void bindContentListener() {
		this.content.addListener((x, o, n)->{
			if (o != null) {
				o.removeTextChangeListener(this.textChangeListener);
			}
			if (n != null) {
				n.addTextChangeListener(this.textChangeListener);
				this.numberOfLines.set(n.getLineCount());
			}
		});
		StyledTextContent current = this.content.get();
		if (current != null) {
			current.addTextChangeListener(this.textChangeListener);

			// set inital values
			this.numberOfLines.set(current.getLineCount());

		}
	}

	private TextChangeListener textChangeListener = new TextChangeListener() {

		private Function<Integer, Integer> mapping;
		private RangeSet<Integer> toUpdate = TreeRangeSet.create();
		private RangeSet<Integer> toRelease = TreeRangeSet.create();

		@Override
		public void textSet(TextChangedEvent event) {
			// update number of lines
			ContentView.this.numberOfLines.set(getContent().getLineCount());

			getLineLayer().requestLayout();
		}

		private int computeFirstUnchangedLine(TextChangingEvent event) {

			int endOffset = event.offset + event.replaceCharCount;
			int endLineIndex = getContent().getLineAtOffset(endOffset);
			int endLineBegin = getContent().getOffsetAtLine(endLineIndex);
//			int endLineLength = ContentView.this.lineHelper.getLength(endLineIndex);

			int firstSafeLine;

			if (endLineBegin == event.offset) {
				// offset at beginning of line
				firstSafeLine = endLineIndex;
			}
			else {
				// offset in middle or at end of line
				firstSafeLine = endLineIndex + 1;
			}

			return firstSafeLine;
		}

		@Override
		public void textChanging(TextChangingEvent event) {
			final int changeBeginLine = getContent().getLineAtOffset(event.offset);

			// determine first unchanged line
			int firstUnchangedLine = computeFirstUnchangedLine(event);

			int deltaLines = event.newLineCount - event.replaceLineCount;

			if (deltaLines < 0) {
				this.toRelease.add(Range.closedOpen(Integer.valueOf(firstUnchangedLine + deltaLines), Integer.valueOf(firstUnchangedLine)));
			}

			// prepare permutation
			this.mapping = (idx) -> {
				if (idx.intValue() >= firstUnchangedLine) {
					return Integer.valueOf(idx.intValue() + deltaLines);
				}
				return idx;
			};

			this.toUpdate.add(Range.closedOpen(Integer.valueOf(changeBeginLine), Integer.valueOf(firstUnchangedLine + deltaLines)));


//
//			// simple insert
//			if (event.replaceCharCount == 0) {
//				System.err.println("# Simple Insert");
//				if (event.newLineCount > 0) {
//					System.err.println("# We have new lines");
//
//					int lineIndex = getContent().getLineAtOffset(event.offset);
//					int lineBegin = getContent().getOffsetAtLine(lineIndex);
//					int lineLength = lineHelper.getLength(lineIndex);
//
//					int firstSafeLine;
//					Range<Integer> updateRange;
//
//					if (lineBegin == event.offset) {
//						// at beginning of line
//						firstSafeLine = lineIndex;
//						updateRange = Range.closedOpen(lineIndex, lineIndex + event.newLineCount);
//					}
//					else if (lineBegin == event.offset + lineLength) {
//						// insert was at end of line
//						firstSafeLine= lineIndex + 1;
//						updateRange = Range.closedOpen(lineIndex, lineIndex + event.newLineCount);
//					}
//					else {
//						// insert was in middle of line
//						firstSafeLine = lineIndex + 2;
//						updateRange = Range.closedOpen(lineIndex, lineIndex + 1 + event.newLineCount);
//					}
//					System.err.println("# firstSafeLine = " + firstSafeLine + " / updateRange " + updateRange);
//
//					// prepare update
//					toUpdate.add(updateRange);
//
//					// prepare permutation
//					this.mapping = (idx) -> {
//						if (idx >= firstSafeLine) {
//							return idx + event.newLineCount;
//						}
//						return idx;
//					};
//
//				}
//			}



//			int firstUnchangedLine = changeBeginLine + replaceLines;
//
//			int newFirstUnchnagedLine = changeBeginLine + newLines;
//
//			System.err.println(" changeBeginLine = " + changeBeginLine);
//			System.err.println(" firstUnchangedLine = " + firstUnchangedLine);
//			System.err.println(" newFirstUnchangedLine = " + newFirstUnchnagedLine);
//
//			// prepare updates
//			toUpdate.add(Range.closedOpen(changeBeginLine, changeBeginLine + newLines));
//			// prepare permutation
//			this.mapping = (idx) -> {
//				if (idx >= firstUnchangedLine) {
//					return idx + firstUnchangedLine - newFirstUnchnagedLine;
//				}
//				return idx;
//			};

		}

		@Override
		public void textChanged(TextChangedEvent event) {
			if (!this.toRelease.isEmpty()) {
				releaseNodesNow(this.toRelease);
				this.toRelease.clear();
			}

			// execute permutation
			if (this.mapping != null) {
				getLineLayer().permutateNodes(this.mapping);
				this.mapping = null;
			}


			// execute updates
			if (!this.toUpdate.isEmpty()) {

				updateNodesNow(this.toUpdate);
				this.toUpdate.clear();
			}

			// update number of lines
			ContentView.this.numberOfLines.set(getContent().getLineCount());


			getLineLayer().requestLayout();
		}
	};

//	Timer t = new Timer();
//	volatile boolean scheduled = false;
//	private void scheduleUpdate() {
//		if (true) return;
//
//		try {
//			if (!scheduled) {
//				scheduled = true;
//				t.schedule(new TimerTask() {
//					@Override
//					public void run() {
//						Platform.runLater(()->doUpdate());
//						scheduled = false;
//					}
//				}, 16);
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

	private void onLineChange(Observable o) {
		RangeSet<Integer> toUpdate = TreeRangeSet.create();
		RangeSet<Integer> toRelease = TreeRangeSet.create();

		double offsetY = offsetYProperty().get();
		com.google.common.collect.Range<Integer> visibleLines = visibleLinesProperty().get();
		ContiguousSet<Integer> set = ContiguousSet.create(visibleLines, DiscreteDomain.integers());
		double lineHeight = lineHeightProperty().get();

//		System.err.println("onLineChange " + offsetY + " " + visibleLines);


		// schedule visible line updates
		if (this.curVisibleLines == null) {
			toUpdate.add(visibleLines);
		}
		else {
			RangeSet<Integer> hiddenLines = TreeRangeSet.create();
			hiddenLines.add(this.curVisibleLines);
			hiddenLines.remove(visibleLines);

			RangeSet<Integer> shownLines = TreeRangeSet.create();
			shownLines.add(visibleLines);
			shownLines.remove(this.curVisibleLines);

			toUpdate.addAll(shownLines);
			toRelease.addAll(hiddenLines);
		}
		this.curVisibleLines = visibleLines;

		// store precomputed y data
		for (int index : set) {
			double y = index * lineHeight - offsetY;
			this.yOffsetData.put(Integer.valueOf(index), Double.valueOf(y));
//			this.forceLayout = true;
		}

		releaseNodesNow(toRelease);
		updateNodesNow(toUpdate);

		this.lineLayer.requestLayout();
//		scheduleUpdate();
	}

	private double computeLongestLine() {
		OptionalInt longestLine = IntStream.range(0, getNumberOfLines())
			.map( index -> this.lineHelper.getLengthCountTabsAsChars(index))
			.max();
		if( longestLine.isPresent() ) {
			int lineLength = longestLine.getAsInt() + 2;
			return Math.max(getWidth(), lineLength * getCharWidth());
		}

		return getWidth();
	}

//	private double computeLongestLine_old() {
//		Optional<Integer> longestLine = IntStream.range(0, getNumberOfLines())
//				.mapToObj(index->Integer.valueOf(this.lineHelper.getLengthCountTabsAsChars(index)))
//				.collect(Collectors.maxBy(Integer::compare));
////		Optional<Integer> longestLine = model.stream().map(m->m.getLineLength() + countTabs(m.getText()) * 3).collect(Collectors.maxBy(Integer::compare));
//		longestLine = longestLine.map(s-> Integer.valueOf(s.intValue()+2)); // extra space
//		Optional<Double> longestLineWidth = longestLine.map(i->i*getCharWidth());
//		longestLineWidth = longestLineWidth.map(l->Math.max(l, getWidth()));
//		return longestLineWidth.orElse(getWidth());
//	}

	private Font lastFont;
	private double lastWidth = 8d;

	private double getCharWidth() {
		if( this.lastFont != this.area.getFont() ) {
			this.lastFont = this.area.getFont();
			this.lastWidth = Math.ceil(Util.getTextWidth("M", this.lastFont)); //$NON-NLS-1$
		}
		return this.lastWidth;
	}

	@Override
	protected void layoutChildren() {
		double scrollX = -this.offsetX.get();
		this.contentBody.resizeRelocate(scrollX, 0, computeLongestLine(), getHeight());
	}


	private DoubleProperty offsetY = new SimpleDoubleProperty();
	private DoubleProperty offsetX = new SimpleDoubleProperty();

	public void bindHorizontalScrollbar(ScrollBar bar) {
		bar.setMin(0);
		DoubleBinding max = this.contentBody.widthProperty().subtract(widthProperty());
		DoubleBinding factor = this.contentBody.widthProperty().divide(max);
		bar.maxProperty().bind(this.contentBody.widthProperty().divide(factor));
		bar.visibleAmountProperty().bind(widthProperty().divide(factor));
		this.offsetX.bind(bar.valueProperty());
	}

	public DoubleProperty offsetYProperty() {
		return this.offsetY;
	}


//	private RangeSet<Integer> toRelease = TreeRangeSet.create();
//	private RangeSet<Integer> toUpdate = TreeRangeSet.create();


	void updateNodesNow(com.google.common.collect.RangeSet<Integer> rs) {
		RangeSet<Integer> subRangeSet = rs.subRangeSet(getVisibleLines()).subRangeSet(Range.closedOpen(Integer.valueOf(0), Integer.valueOf(getNumberOfLines())));
		subRangeSet.asRanges().forEach(r-> {
			ContiguousSet.create(r, DiscreteDomain.integers()).forEach(index-> {
				getLineLayer().updateNode(index.intValue());
//				StyledTextLine m = this.model.get(index);
//				lineLayer.updateNode(m);
			});
		});
	}

	void releaseNodesNow(com.google.common.collect.RangeSet<Integer> rs) {
		RangeSet<Integer> subRangeSet = rs.subRangeSet(Range.closedOpen(Integer.valueOf(0), Integer.valueOf(getNumberOfLines())));
//		System.err.println("releaseNodesNow " + subRangeSet);
		subRangeSet.asRanges().forEach(r-> {
			ContiguousSet.create(r, DiscreteDomain.integers()).forEach(index-> {
				getLineLayer().releaseNode(index.intValue());
//				StyledTextLine m = this.model.get(index);
//				System.err.println("RELEASE " + m);
//				lineLayer.releaseNode(m);
			});
		});
	}

//	private void updateNodes(com.google.common.collect.Range<Integer> range) {
//		if (debugOut) System.err.println("updateNodes(" + range + ")");
//		toUpdate.add(range);
//		scheduleUpdate();
////
////		if (range.intersects(getVisibleLines())) {
////			Range intersection = range.intersect(getVisibleLines());
////			for (int index = intersection.getOffset(); index <intersection.getEndOffset(); index++) {
////				toUpdate.add(index);
//////				StyledTextLine m = this.model.get(index);
//////				prepareNode(index, m);
////			}
////		}
//	}

//	private boolean doUpdate() {
//		try {
//			long now = -System.nanoTime();
//			if (!toRelease.isEmpty()) {
//				toRelease.asRanges().forEach(r-> {
//					ContiguousSet.create(r, DiscreteDomain.integers()).forEach(this.lineLayer::releaseNode);
//				});
//				toRelease.clear();
//			}
//
//			if (!toUpdate.isEmpty()) {
//				toUpdate.subRangeSet(getVisibleLines()).subRangeSet(Range.closedOpen(0, this.model.size())).asRanges().forEach(r-> {
//					ContiguousSet.create(r, DiscreteDomain.integers()).forEach(index-> {
//						StyledTextLine m = this.model.get(index);
//						lineLayer.updateNode(index, m);
//					});
//				});
//				toUpdate.clear();
//			}
//
//
//			now += System.nanoTime();
//
//			if (now > 1000_000 * 5) {
//				System.err.println("update needed " + (now/1000000) + "ms");
//			}
//
//			if (!toRelease.isEmpty() || !toUpdate.isEmpty() || forceLayout) {
////				System.err.println("releasing " + toRelease + " and updating " + toUpdate + " lines");
//
//			    lineLayer.requestLayout();
//
//				forceLayout = false;
//
//				return true;
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//		return false;
//
//	}
//
//	private void updatePulse(long now) {
//		doUpdate();
//	}


	public Optional<Point2D> getLocationInScene(int globalOffset) {
		int lineIndex = getContent().getLineAtOffset(globalOffset);
		Optional<LineNode> node = this.lineLayer.getVisibleNode(lineIndex);

		return node.map(n->{
			double x = n.getCharLocation(globalOffset - n.getStartOffset());
			Point2D p = new Point2D(x, 0);
			return n.localToScene(p);
		});
	}

	public Optional<Integer> getLineIndex(Point2D point) {
		// transform point to respect horizontal scrolling
		Point2D p = this.lineLayer.sceneToLocal(this.localToScene(point));
		Optional<Integer> result =  this.lineLayer.getLineIndex(p);
		return result;

	}
	public void updateAnnotations(RangeSet<Integer> r) {
		updateNodesNow(r);
	}

}
