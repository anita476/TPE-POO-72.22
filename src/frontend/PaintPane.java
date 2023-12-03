package frontend;

import backend.CanvasState;
import backend.FigureSelection;
import backend.model.Figure;
import backend.model.Point;
import backend.model.Rectangle;
import frontend.figureButtons.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class PaintPane extends BorderPane {

	// BackEnd
	CanvasState canvasState;

	// Canvas y relacionados
	Canvas canvas = new Canvas(800, 600);
	GraphicsContext gc = canvas.getGraphicsContext2D();

	// Constantes de colores
	Color DEFAULT_LINE_COLOR = Color.BLACK;
	Color DEFAULT_FILL_COLOR = Color.YELLOW;
	Color DEFAULT_SELECTED_LINE_COLOR = Color.RED;

	// Botones Barra Izquierda
	final ToggleButton selectionButton = new ToggleButton("Seleccionar");
	final ToggleButton rectangleButton = new ToggleButton("Rectángulo");
	final ToggleButton circleButton = new ToggleButton("Círculo");
	final ToggleButton squareButton = new ToggleButton("Cuadrado");
	final ToggleButton ellipseButton = new ToggleButton("Elipse");
	final ToggleButton deleteButton = new ToggleButton("Borrar");
	final ToggleGroup tools = new ToggleGroup();

	// Selector de color de relleno
	ColorPicker fillColorPicker = new ColorPicker(DEFAULT_FILL_COLOR);

	// Dibujar una figura
	Point startPoint;

	// Seleccionar una figura
	Figure selectedFigure;

	// StatusBar
	StatusPane statusPane;

	// EffectsBar
	EffectsPane effectsPane = new EffectsPane();

	// Set para figuras seleccionadas
	FigureSelection figureSelection = new FigureSelection();

	public PaintPane(CanvasState canvasState, StatusPane statusPane) {
		this.canvasState = canvasState;
		this.statusPane = statusPane;

		ToggleButton[] toolsArr = {selectionButton, rectangleButton, circleButton, squareButton, ellipseButton, deleteButton};

		for (ToggleButton tool : toolsArr) {
			tool.setMinWidth(90);
			tool.setToggleGroup(tools);
			tool.setCursor(Cursor.HAND);
		}

		VBox buttonsBox = new VBox(10);
		buttonsBox.getChildren().addAll(toolsArr);
		buttonsBox.getChildren().add(fillColorPicker);
		buttonsBox.setPadding(new Insets(5));
		buttonsBox.setStyle("-fx-background-color: #999");
		buttonsBox.setPrefWidth(100);
		gc.setLineWidth(1);

		rectangleButton.setUserData(new RectangleButton(this, canvasState));
		squareButton.setUserData(new SquareButton(this,canvasState));
		circleButton.setUserData(new CircleButton(this, canvasState));
		ellipseButton.setUserData(new EllipseButton(this, canvasState));

		canvas.setOnMousePressed(this::onMousePressed);
		canvas.setOnMouseReleased(this::onMouseRelease);
		canvas.setOnMouseMoved(this::onMouseMoved);
		canvas.setOnMouseDragged(this::onMouseDragged);
		canvas.setOnMouseClicked(this::getOnMouseClicked);

		deleteButton.setOnAction(event -> {
			if (selectedFigure != null) {
				canvasState.remove(selectedFigure);
				selectedFigure = null;
				redrawCanvas();
			}
		});

		setTop(effectsPane);
		setLeft(buttonsBox);
		setRight(canvas);
	}

	private void onMousePressed(MouseEvent mouseEvent) {
		startPoint = new Point(mouseEvent.getX(), mouseEvent.getY());
	}

	private void onMouseRelease(MouseEvent mouseEvent) {
		if(startPoint == null) return;

		Point endPoint = new Point(mouseEvent.getX(), mouseEvent.getY());
		if(endPoint.getX() < startPoint.getX() || endPoint.getY() < startPoint.getY()) return;

		Toggle selectedButton = tools.getSelectedToggle();
		if(selectedButton == null) return;

		if(selectedButton == selectionButton) {
			figureSelection.clear();
			if(startPoint.distanceTo(endPoint) > 1) {
				Rectangle container = Rectangle.createFrom(startPoint, endPoint);
				canvasState.figuresContainedIn(container, figureSelection);
				System.out.println(figureSelection.size());
				if(figureSelection.isEmpty()) statusPane.updateStatus("Ninguna figura encontrada");
				if(figureSelection.size() == 1) statusPane.updateStatus("Se seleccionó: %s".formatted(selectedFigure));
				statusPane.updateStatus("Se seleccionaron %d figuras".formatted(figureSelection.size()));
			}
			else {
				Figure topFigure = canvasState.getTopFigureAt(endPoint);
				if (topFigure != null) {
					figureSelection.add(topFigure);
					statusPane.updateStatus(String.format("Se seleccionó %s", topFigure));
				}
			}
		}

		if(selectedButton != selectionButton && selectedButton != deleteButton) {
			((FigureButton) selectedButton.getUserData()).createAndAddFigure(startPoint, endPoint);
		}

        startPoint = null;
		redrawCanvas();
	}

	private void onMouseMoved(MouseEvent mouseEvent) {
		if(figureSelection.isEmpty()) {
			Point eventPoint = new Point(mouseEvent.getX(), mouseEvent.getY());
			Figure topFigure = canvasState.getTopFigureAt(eventPoint);
			statusPane.updateStatus(topFigure == null ? eventPoint.toString() : topFigure.toString());
		}
	}

	private void onMouseDragged(MouseEvent mouseEvent) {
		if(!figureSelection.isEmpty()) {
			Point eventPoint = new Point(mouseEvent.getX(), mouseEvent.getY());
			double diffX = (eventPoint.getX() - startPoint.getX());
			double diffY = (eventPoint.getY() - startPoint.getY());
			for (Figure figure : figureSelection) {
				figure.move(diffX, diffY);
			}
			startPoint.move(diffX, diffY);
			redrawCanvas();
		}
	}

	private void getOnMouseClicked(MouseEvent mouseEvent) {
		if(selectionButton.isSelected()) {
			Point eventPoint = new Point(mouseEvent.getX(), mouseEvent.getY());
			boolean found = false;
			for (Figure figure : canvasState) {
				if(figure.figureBelongs(eventPoint)) {
					found = true;
					selectedFigure = figure;
				}
			}
			if (found) {
				effectsPane.shadeBox.setSelected(selectedFigure.hasShadow());
				effectsPane.gradientBox.setSelected(selectedFigure.hasGradient());
				effectsPane.bevelBox.setSelected(selectedFigure.hasBevel());
			} else {
				selectedFigure = null;
			}
			redrawCanvas();
		}
	}

	void redrawCanvas() {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		for(Figure figure : canvasState) {
			gc.setStroke(figureSelection.contains(figure) ? DEFAULT_SELECTED_LINE_COLOR : DEFAULT_LINE_COLOR);
			if(figure.hasShadow()){
				figure.addShadow(gc);
			}
			if(figure.hasBevel()){
				figure.addBevel(gc);
			}
			figure.draw(gc);
		}
	}

	public StatusPane getStatusPane(){
		return this.statusPane;
	}

	public Color getColorFromPicker(){
		return fillColorPicker.getValue();
	}

	private class EffectsPane extends HBox {
		final Label label = new Label("Efectos:\t");
		final CheckBox shadeBox = new CheckBox("Sombra");
		final CheckBox gradientBox = new CheckBox("Gradiente");
		final CheckBox bevelBox = new CheckBox("Biselado");
		final CheckBox[] effectsArr = {shadeBox, gradientBox, bevelBox};

		public EffectsPane() {
			for (CheckBox effect : effectsArr) {
				effect.setMinWidth(90);
				effect.setCursor(Cursor.HAND);
			}
			setAlignment(Pos.CENTER);
			getChildren().add(label);
			getChildren().addAll(effectsArr);
			setPadding(new Insets(5));
			setStyle("-fx-background-color: #999");
			setPrefWidth(100);

			shadeBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				public void changed(ObservableValue<? extends Boolean> ov,
									Boolean old_val, Boolean new_val) {
					if(selectionButton.isSelected() && selectedFigure!=null){
						if(new_val){
							selectedFigure.modifyShadow(true);
						}
						if(!new_val){
							selectedFigure.modifyShadow(false);
						}
					}
					redrawCanvas();
				}
			});

			bevelBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
					if(selectionButton.isSelected() && selectedFigure!=null){
						if(t1){
							selectedFigure.modifyBevel(true);
						}
						if(!t1){
							selectedFigure.modifyBevel(false);
						}
					}
					redrawCanvas();
				}
			});

			gradientBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
					if(selectionButton.isSelected() && selectedFigure!=null){
						if(t1){
							selectedFigure.modifyGradient(true);
						}
						if(!t1){
							selectedFigure.modifyGradient(false);
						}
					}
					redrawCanvas();
				}
			});

		}

	}

}
