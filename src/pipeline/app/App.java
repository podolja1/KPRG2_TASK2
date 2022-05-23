package pipeline.app;

import pipeline.view.Window;
import pipeline.renderer.Renderer;

/**
 * Run app
 */
public class App {
    public static void main(String[] args) {
        new Window(new Renderer());
    }
}
