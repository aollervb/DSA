package org.example.BTree;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;


public class BTreeVisualizer extends Application {

    private static final double H_GAP = 20;
    private static final double V_GAP = 60;
    private static final double NODE_HEIGHT = 30;
    private static final double KEY_WIDTH_BASE = 12;
    private static final double KEY_WIDTH_PADDING = 24;
    private static final double KEY_WIDTH_MIN = 48;
    private static final double CANVAS_DEFAULT_WIDTH = 800;
    private static final double CANVAS_DEFAULT_HEIGHT = 600;
    private static final double ZOOM_MIN = 0.5;
    private static final double ZOOM_MAX = 2.0;
    private static final double ZOOM_DEFAULT = 1.0;
    private static final double ZOOM_TICK = 0.5;

    private BTreeIterative tree;
    private GraphicsContext gc;
    private Canvas canvas;
    private ScrollPane scrollPane;
    private Scale scale;

    @Override
    public void start(Stage stage) {
        tree = new BTreeIterative(2);
        canvas = new Canvas(CANVAS_DEFAULT_WIDTH, CANVAS_DEFAULT_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        scale = new Scale(ZOOM_DEFAULT, ZOOM_DEFAULT);
        canvas.getTransforms().add(scale);

        scrollPane = buildScrollPane();
        HBox controls = buildControls();

        VBox root = new VBox(controls, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        stage.setTitle("B-Tree Visualizer");
        stage.setScene(new Scene(root, CANVAS_DEFAULT_WIDTH, CANVAS_DEFAULT_HEIGHT + 50));
        stage.show();

        registerListeners();
    }

    private ScrollPane buildScrollPane() {
        ScrollPane sp = new ScrollPane(new Group(canvas));
        sp.setPannable(true);
        sp.setFitToWidth(false);
        sp.setFitToHeight(false);
        return sp;
    }

    private HBox buildControls() {
        TextField input = new TextField();
        input.setPromptText("enter key");

        Button insertBtn = new Button("Insert");
        insertBtn.setOnAction(e -> handleInsert(input));

        input.setOnAction(e -> handleInsert(input));

        Slider zoomSlider = new Slider(ZOOM_MIN, ZOOM_MAX, ZOOM_DEFAULT);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setMajorTickUnit(ZOOM_TICK);
        zoomSlider.valueProperty().addListener((obs, o, n) -> handleZoom(n.doubleValue()));

        HBox controls = new HBox(8, input, insertBtn, new Label("Zoom"), zoomSlider);
        controls.setPadding(new Insets(10));
        return controls;
    }

    private void registerListeners() {
        scrollPane.viewportBoundsProperty().addListener((obs, o, n) -> {
            resizeCanvas(n.getWidth(), n.getHeight());
            drawTree();
        });
    }

    private void handleInsert(TextField input) {
        String text = input.getText().trim();
        if (text.isEmpty()) return;
        try {
            tree.insert(Integer.parseInt(text));
            input.clear();
            resizeCanvas(scrollPane.getViewportBounds().getWidth(), scrollPane.getViewportBounds().getHeight());
            drawTree();
        } catch (NumberFormatException e) {
            input.clear();
        }
    }

    private void handleZoom(double zoom) {
        scale.setX(zoom);
        scale.setY(zoom);
        resizeCanvas(scrollPane.getViewportBounds().getWidth(), scrollPane.getViewportBounds().getHeight());
        drawTree();
    }

    private void resizeCanvas(double viewportWidth, double viewportHeight) {
        double treeWidth = tree.getRoot() == null ? viewportWidth : treeWidth() + 80;
        canvas.setWidth(Math.max(viewportWidth, treeWidth));
        canvas.setHeight(Math.max(viewportHeight, CANVAS_DEFAULT_HEIGHT));
    }

    private void drawTree() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (tree.getRoot() == null) return;
        double kw = keyWidth();
        double totalWidth = subtreeWidth(tree.getRoot(), kw, H_GAP);
        double startX = Math.max(canvas.getWidth() / 2, totalWidth / 2 + 40);
        drawNode(tree.getRoot(), startX, 40, kw);
    }

    private void drawNode(Node node, double x, double y, double kw) {
        double nw = nodeWidth(node, kw);

        gc.strokeRect(x - nw / 2, y, nw, NODE_HEIGHT);
        for (int i = 0; i < node.getKeys().size(); i++) {
            if (i > 0) gc.strokeLine(x - nw/2 + i*kw, y, x - nw/2 + i*kw, y + NODE_HEIGHT);
            gc.fillText(String.valueOf(node.getKeys().get(i)), x - nw/2 + i*kw + kw/2 - 6, y + 20);
        }

        if (!node.isLeaf()) {
            double totalChildWidth = node.getChildren().stream()
                    .mapToDouble(c -> subtreeWidth(c, kw, H_GAP))
                    .sum() + H_GAP * (node.getChildren().size() - 1);

            double childX = x - totalChildWidth / 2;
            for (Node child : node.getChildren()) {
                double childW = subtreeWidth(child, kw, H_GAP);
                double cx = childX + childW / 2;
                double cy = y + NODE_HEIGHT + V_GAP;
                gc.strokeLine(x, y + NODE_HEIGHT, cx, cy);
                drawNode(child, cx, cy, kw);
                childX += childW + H_GAP;
            }
        }
    }

    private double keyWidth() {
        if (tree.getRoot() == null) return KEY_WIDTH_MIN;
        return Math.max(KEY_WIDTH_MIN,
                String.valueOf(maxKeyValue(tree.getRoot())).length() * KEY_WIDTH_BASE + KEY_WIDTH_PADDING);
    }

    private double treeWidth() {
        return subtreeWidth(tree.getRoot(), keyWidth(), H_GAP);
    }

    private double subtreeWidth(Node node, double kw, double hGap) {
        if (node.isLeaf()) return nodeWidth(node, kw);
        double total = node.getChildren().stream()
                .mapToDouble(c -> subtreeWidth(c, kw, hGap))
                .sum() + hGap * (node.getChildren().size() - 1);
        return Math.max(total, nodeWidth(node, kw));
    }

    private double nodeWidth(Node node, double kw) {
        return node.getKeys().size() * kw + 16;
    }

    private int maxKeyValue(Node node) {
        int max = node.getKeys().stream().mapToInt(Integer::intValue).max().orElse(0);
        if (!node.isLeaf()) max = Math.max(max, node.getChildren().stream()
                .mapToInt(this::maxKeyValue).max().orElse(0));
        return max;
    }

    public static void main(String[] args) { launch(args); }
}