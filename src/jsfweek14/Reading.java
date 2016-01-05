package jsfweek14;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.RunnableFuture;

/**
 * @author rvanduijnhoven
 * Memory mapped files/random access assignment (week 13).
 */
@SuppressWarnings("Duplicates")
public class Reading extends Application {

    private double zoomTranslateX = 0.0;
    private double zoomTranslateY = 0.0;
    private double zoom = 1.0;
    private Canvas kochPanel;
    private final int kpWidth = 400;
    private final int kpHeight = 400;
    private int currentLevel = 1;
    List<Edge> edges;
    KochFractal koch;
    private int level = 0;


    @Override
    public void start(Stage primaryStage) {
        //Create four buttons, for each of the read/writing ways.
        Button btnTextNoBuffer = new Button();
        btnTextNoBuffer.setText("Save and load to text file, no buffer.");
        Button btnTextWithBuffer = new Button();
        btnTextWithBuffer.setText("Save and load to text file, with buffer.");
        Button btnBinaryNoBuffer = new Button();
        btnBinaryNoBuffer.setText("Save and load to Mapped file.");
        Button btnBinaryWithBuffer = new Button();
        btnBinaryWithBuffer.setText("Save and load to binary file, with buffer.");

        //Textfield to enter the number of edges + label
        TextField nrOfEdges = new TextField();
        Label lbl = new Label();
        lbl.setText("Enter your desired level.");

        //Label to present the read/write time.
        Label speed = new Label();
        speed.setText("The write speed will be shown here.");
        //New canvas
        kochPanel = new Canvas(kpWidth,kpHeight);
        kochPanel.setTranslateX(100);


        //position the elements
        btnTextNoBuffer.setTranslateY(-80);
        btnTextWithBuffer.setTranslateY(-40);
        btnBinaryWithBuffer.setTranslateY(40);
        btnTextNoBuffer.setTranslateX(-240);
        btnTextWithBuffer.setTranslateX(-240);
        btnBinaryNoBuffer.setTranslateX(-240);
        btnBinaryWithBuffer.setTranslateX(-240);
        nrOfEdges.setTranslateY(-220);
        nrOfEdges.setTranslateX(-310);
        nrOfEdges.setMaxWidth(50);
        lbl.setTranslateX(-240);
        lbl.setTranslateY(-200);
        speed.setTranslateY(-240);


        StackPane root = new StackPane();
        //root.getChildren().add(btnTextNoBuffer);
        //root.getChildren().add(btnTextWithBuffer);
        root.getChildren().add(btnBinaryNoBuffer);
        //root.getChildren().add(btnBinaryWithBuffer);
        //root.getChildren().add(nrOfEdges);
        //root.getChildren().add(lbl);
        root.getChildren().add(kochPanel);
        root.getChildren().add(speed);

        Scene scene = new Scene(root, 700, 500);

        primaryStage.setTitle("Edges and stuff");
        primaryStage.setScene(scene);
        primaryStage.show();

        //Event handlers for buttons
//        btnTextNoBuffer.setOnMouseClicked(event -> {
//            int i = Integer.parseInt(nrOfEdges.getText());
//            currentLevel = i;
//            clearKochPanel();
//            createKochFractal(i);
//            double x = 0;
//            try {
//                x = saveTextFileNoBuffer();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            speed.setText(String.valueOf((x / 1000000)));
//            //drawAllEdges();
//        });

//        btnTextWithBuffer.setOnMouseClicked(event -> {
//            int i = Integer.parseInt(nrOfEdges.getText());
//            currentLevel = i;
//            clearKochPanel();
//            createKochFractal(i);
//            double x = 0;
//            try {
//                x = saveTextFileWithBuffer();
//            }
//            catch (Exception ex)
//            {
//                ex.printStackTrace();
//            }
//            speed.setText(String.valueOf(x / 1000000));
//            //drawAllEdges();
//        });

        btnBinaryNoBuffer.setOnMouseClicked(event -> {
            //int i = Integer.parseInt(nrOfEdges.getText());
            //currentLevel = i;
//            if (i > 10)
//            {
//                speed.setText("Too high of a level count!");
//                throw new UnsupportedOperationException();
//            }
            clearKochPanel();
            //createKochFractal(i);
            double x = 0;
            try {
                x = loadBinaryFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
            speed.setText(String.valueOf(x / 1000000));
            //drawAllEdges();
        });

        btnBinaryWithBuffer.setOnMouseClicked(event ->{
            int i = Integer.parseInt(nrOfEdges.getText());
            currentLevel = i;
            clearKochPanel();
            createKochFractal(i);
            double x = 0;
            try {
                x = saveBinaryFileWithBuffer();
            } catch (Exception e) {
                e.printStackTrace();
            }
            speed.setText(String.valueOf(x / 1000000));
        });

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private Edge edgeAfterZoomAndDrag(Edge e) {
        return new Edge(
                e.X1 * zoom + zoomTranslateX,
                e.Y1 * zoom + zoomTranslateY,
                e.X2 * zoom + zoomTranslateX,
                e.Y2 * zoom + zoomTranslateY,
                e.color);
    }

    public void clearKochPanel() {
        GraphicsContext gc = kochPanel.getGraphicsContext2D();
        gc.clearRect(0.0,0.0,kpWidth,kpHeight);
        gc.setFill(Color.BLACK);
        gc.fillRect(0.0,0.0,kpWidth,kpHeight);
    }

    public void drawEdge(Edge e) {
        // Graphics
        GraphicsContext gc = kochPanel.getGraphicsContext2D();

        // Adjust edge for zoom and drag
        Edge e1 = edgeAfterZoomAndDrag(e);

        // Set line color
        gc.setStroke(e1.color);

        // Set line width depending on level
        if (currentLevel <= 3) {
            gc.setLineWidth(1.5);
        }
        else if (currentLevel <=5 ) {
            gc.setLineWidth(1.2);
        }
        else {
            gc.setLineWidth(1.0);
        }

        // Draw line
        gc.strokeLine(e1.X1 * 400,e1.Y1 * 400,e1.X2 * 400,e1.Y2 * 400);
    }

    public double saveBinaryFileNoBuffer() throws IOException {
        double time = System.nanoTime();
        File file = new File("C:\\Users\\rvanduijnhoven\\Documents\\jsfoutput\\binFileWithoutBuffer.bin");
        FileChannel fileChannel = null;
        MappedByteBuffer map = null;
        int counter = 0;
        try {
//            fileOut = new FileOutputStream(file);
//            outPut = new DataOutputStream(fileOut);
            fileChannel = new RandomAccessFile(file, "rw").getChannel();
            map = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096 * 128 * 128);
            counter = edges.size();
            for(Edge e : edges)
            {
                map.putDouble(e.X1);
                map.putDouble(e.Y1);
                map.putDouble(e.X2);
                map.putDouble(e.Y2);
                map.putDouble(e.color.getRed());
                map.putDouble(e.color.getGreen());
                map.putDouble(e.color.getBlue());
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        edges.clear();
        map.position(0);
        //Now read every edge from the file and draw it.
        try {
//            fileIn = new FileInputStream(file);
//            inPut = new DataInputStream(fileIn);
            fileChannel = new RandomAccessFile(file, "r").getChannel();
            map = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 4096 * 128 * 128);
            for (int i = 0; i <= counter; i++)
            {
                double X1 = map.getDouble();
                double Y1 = map.getDouble();
                double X2 = map.getDouble();
                double Y2 = map.getDouble();
                double red = map.getDouble();
                double green = map.getDouble();
                double blue = map.getDouble();

                Edge e = new Edge(X1, Y1, X2, Y2, new Color(red, green, blue, 1));
                drawEdge(e);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return System.nanoTime() - time;
    }

    public double saveBinaryFileWithBuffer()
    {
        double time = System.nanoTime();
        File file = new File("C:\\Users\\rvanduijnhoven\\Documents\\jsfoutput\\binFileWithBuffer.bin");
        DataOutputStream outPut = null;
        DataInputStream inPut = null;
        FileOutputStream fileOut = null;
        FileInputStream fileIn = null;
        BufferedInputStream buffInput = null;
        BufferedOutputStream buffOut = null;
        try {
            fileOut = new FileOutputStream(file);
            buffOut = new BufferedOutputStream(fileOut);
            outPut = new DataOutputStream(buffOut);
            for(Edge e : edges)
            {
                outPut.writeDouble(e.X1);
                outPut.writeDouble(e.Y1);
                outPut.writeDouble(e.X2);
                outPut.writeDouble(e.Y2);
                outPut.writeDouble(e.color.getRed());
                outPut.writeDouble(e.color.getGreen());
                outPut.writeDouble(e.color.getBlue());
                outPut.flush();
            }
            outPut.close();
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        edges.clear();
        //Now read every edge from the file and draw it.
        try {
            fileIn = new FileInputStream(file);
            buffInput = new BufferedInputStream(fileIn);
            inPut = new DataInputStream(buffInput);

            if (inPut.available() > 0)
            {
                while (inPut.available() > 0)
                {
                    double X1 = inPut.readDouble();
                    double Y1 = inPut.readDouble();
                    double X2 = inPut.readDouble();
                    double Y2 = inPut.readDouble();
                    double red = inPut.readDouble();
                    double green = inPut.readDouble();
                    double blue = inPut.readDouble();

                    Edge e = new Edge(X1, Y1, X2, Y2, new Color(red, green, blue, 1));
                    drawEdge(e);
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return System.nanoTime() - time;
    }

    public double loadBinaryFile() throws IOException {
        double time = System.nanoTime();
        File file = new File("C:\\Users\\rvanduijnhoven\\Documents\\jsfoutput\\jsfweek14.bin");
        FileChannel fileChannel = null;
        MappedByteBuffer map = null;
        int counter = 0;
        //Now read every edge from the file and draw it.
        try {
//            fileIn = new FileInputStream(file);
//            inPut = new DataInputStream(fileIn);
            fileChannel = new RandomAccessFile(file, "r").getChannel();
            map = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 4096 * 128 * 128);
            double d = map.getDouble();
            currentLevel = (int)d;
            counter = (int) (3 * Math.pow(4, currentLevel - 1));
            for (int i = 0; i <= counter; i++)
            {
                double X1 = map.getDouble();
                double Y1 = map.getDouble();
                double X2 = map.getDouble();
                double Y2 = map.getDouble();
                double red = map.getDouble();
                double green = map.getDouble();
                double blue = map.getDouble();

                Edge e = new Edge(X1, Y1, X2, Y2, new Color(red, green, blue, 1));
                drawEdge(e);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
            fileChannel.close();
            map.clear();
        }

        return System.nanoTime() - time;
    }

    /**
     * Generates the required edges.
     * @param level The given level for which the edges will be created.
     */
    public void createKochFractal(int level)
    {
        currentLevel = level;
        //Generate the edges first.
        koch = new KochFractal();
        koch.setLevel(level);
        koch.generateBottomEdge();
        koch.generateLeftEdge();
        koch.generateRightEdge();
        edges = koch.getEdges();
    }

    public void drawAllEdges()
    {
        edges.forEach(this::drawEdge);
    }

}
