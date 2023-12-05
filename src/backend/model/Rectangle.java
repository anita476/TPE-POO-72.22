package backend.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.awt.geom.AffineTransform;

public class Rectangle extends Figure {

    private final Point topLeft, bottomRight;

    public Rectangle(Point topLeft, Point bottomRight) {
        super(new Point[]{topLeft, bottomRight});
        this.topLeft = topLeft;
        this.bottomRight = bottomRight;
    }

    public Point getTopLeft() {
        return topLeft;
    }

    public Point getBottomRight() {
        return bottomRight;
    }

    private double getWidth() {
        return bottomRight.getX() - topLeft.getX();
    }

    private double getHeight() {
        return bottomRight.getY() - topLeft.getY();
    }

    private Point getCenter() {
        return new Point(getWidth()/2, getHeight()/2);
    }

    @Override
    public String toString() {
        return String.format("Rectángulo [ %s , %s ]", topLeft, bottomRight);
    }

    @Override
    public boolean figureBelongs(Point eventPoint){
        return (eventPoint.getX() > this.getTopLeft().getX() && eventPoint.getX() < this.getBottomRight().getX() &&
                eventPoint.getY() > this.getTopLeft().getY() && eventPoint.getY() < this.getBottomRight().getY());
    }

    @Override
    public boolean isContainedIn(Rectangle rectangle) {
        return topLeft.getX() >= rectangle.topLeft.getX() &&
                topLeft.getY() >= rectangle.topLeft.getY() &&
                bottomRight.getX() <= rectangle.bottomRight.getX() &&
                bottomRight.getY() <= rectangle.bottomRight.getY();
    }

    @Override
    public void draw(GraphicsContext gc) {
        if(hasGradient()) {
            LinearGradient linearGradient = new LinearGradient(0, 0, 1, 0, true,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, getFillColor()),
                    new Stop(1, getFillColor().invert()));
            gc.setFill(linearGradient);
        }
        else {
            gc.setFill(getFillColor());
        }
        gc.fillRect(getTopLeft().getX(), getTopLeft().getY(), getWidth(), getHeight());
        gc.strokeRect(getTopLeft().getX(), getTopLeft().getY(), getWidth(), getHeight());

    }

    public static Rectangle createFrom(Point startPoint, Point endPoint){
        return new Rectangle(startPoint, endPoint);
    }

    @Override
    public void addShadow(GraphicsContext gc) {
        gc.setFill(Color.GRAY);
        gc.fillRect(this.getTopLeft().getX() + 10.0,
                this.getTopLeft().getY() + 10.0,
                Math.abs(this.getTopLeft().getX() - this.getBottomRight().getX()),
                Math.abs(this.getTopLeft().getY() - this.getBottomRight().getY()));

    }

    @Override
    public void addBevel(GraphicsContext gc) {
        double x = this.getTopLeft().getX();
        double y = this.getTopLeft().getY();
        double width = Math.abs(x - this.getBottomRight().getX());
        double height = Math.abs(y - this.getBottomRight().getY());
        gc.setLineWidth(10);
        gc.setStroke(Color.LIGHTGRAY);
        gc.strokeLine(x, y, x + width, y);
        gc.strokeLine(x, y, x, y + height);
        gc.setStroke(Color.BLACK);
        gc.strokeLine(x + width, y, x + width, y + height);
        gc.strokeLine(x, y + height, x + width, y + height);
        gc.setLineWidth(1);
    }

    @Override
    public void rotateRight() {

    }

    @Override
    public void flipHorizontally() {
        double deltaX = this.getTopLeft().getX() - this.getBottomRight().getX();
        if(!isInvertedH) {
            isInvertedH = true;
            topLeft.move(-deltaX, 0);
            bottomRight.move(-deltaX, 0);
        }
        else{
            topLeft.move(deltaX, 0);
            bottomRight.move(deltaX, 0);
            isInvertedH = false;
        }
    }

    @Override
    public void flipVertically() {
        double deltaY = this.getTopLeft().getY() - this.getBottomRight().getY();
        if(!isInvertedV) {
            isInvertedV = true;
            topLeft.move(0, -deltaY);
            bottomRight.move(0, -deltaY);
        }
        else{
            topLeft.move(0, deltaY);
            bottomRight.move(0, deltaY);
            isInvertedV = false;
        }
    }

    @Override
    public void scaleUp() {

    }

    @Override
    public void scaleDown() {

    }

}
