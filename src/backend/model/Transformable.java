package backend.model;

public interface Transformable<F extends Movable> {

    void rotateRight();

    void flipHorizontally();

    void flipVertically();

    void scaleUp();

    void scaleDown();

}
