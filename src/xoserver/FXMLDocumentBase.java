package xoserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;

public class FXMLDocumentBase extends BorderPane {

    protected final Label label;
    protected final FlowPane flowPane;
    protected final Button startBtn;
    protected final CategoryAxis categoryAxis;
    protected final NumberAxis numberAxis;
    protected final BarChart<String, Number> playersChart;
    ServerSocket serverSocket;
    boolean startFlag;

    public FXMLDocumentBase() {

        label = new Label();
        flowPane = new FlowPane();
        startBtn = new Button();
        categoryAxis = new CategoryAxis();
        numberAxis = new NumberAxis();
        playersChart = new BarChart<>(categoryAxis, numberAxis);
        startFlag = false;
        categoryAxis.setLabel("Status");
        numberAxis.setLabel("No of Players");
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Offline");
        series1.getData().add(new XYChart.Data<>("online", 0));
        series1.getData().add(new XYChart.Data<>("offline", 50.0));
        series1.getData().add(new XYChart.Data<>("in a game", 0));
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("in a game");
        series2.getData().add(new XYChart.Data<>("online", 0));
        series2.getData().add(new XYChart.Data<>("offline", 50.0));
        series2.getData().add(new XYChart.Data<>("in a game", 0));
        XYChart.Series<String, Number> series3 = new XYChart.Series<>();
        series3.setName("Online");
        series3.getData().add(new XYChart.Data<>("online", 0));
        series3.getData().add(new XYChart.Data<>("offline", 50.0));
        series3.getData().add(new XYChart.Data<>("in a game", 0));

        playersChart.getData().addAll(series1, series2, series3);

        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefHeight(400.0);
        setPrefWidth(600.0);

        BorderPane.setAlignment(label, javafx.geometry.Pos.CENTER);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        label.setPrefHeight(108.0);
        label.setPrefWidth(206.0);
        label.setText("Tic Tac Toe Server");
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        label.setFont(new Font("Arial Black", 18.0));
        setTop(label);

        BorderPane.setAlignment(flowPane, javafx.geometry.Pos.CENTER);
        flowPane.setPrefHeight(108.0);
        flowPane.setPrefWidth(600.0);

        startBtn.setAlignment(javafx.geometry.Pos.CENTER);
        startBtn.setContentDisplay(javafx.scene.control.ContentDisplay.CENTER);
        startBtn.setMnemonicParsing(false);
        startBtn.setPrefHeight(66.0);
        startBtn.setPrefWidth(192.0);
        startBtn.setText("Start");
        startBtn.setTextAlignment(javafx.scene.text.TextAlignment.RIGHT);
        FlowPane.setMargin(startBtn, new Insets(20.0, 0.0, 0.0, 200.0));
        startBtn.setFont(new Font(26.0));
        startBtn.setCursor(Cursor.HAND);
        setBottom(flowPane);

        categoryAxis.setSide(javafx.geometry.Side.BOTTOM);

        numberAxis.setSide(javafx.geometry.Side.LEFT);
        BorderPane.setAlignment(playersChart, javafx.geometry.Pos.CENTER);
        playersChart.setTitle("Players in the Server");
        setCenter(playersChart);

        flowPane.getChildren().add(startBtn);
        startBtn.addEventFilter(ActionEvent.ACTION, (event) -> {
            if (!startFlag) {
                startFlag = true;
                startBtn.setText("Stop");
                new Thread(() -> {
                    try {
                        serverSocket = new ServerSocket(5005);
                        while (startFlag) {
                            Socket s = serverSocket.accept();
                            new PlayerHandler(s);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();

            } else {
                try {
                    startBtn.setText("Start");
                    startFlag = false;
                    serverSocket.close();
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentBase.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

    }
}
