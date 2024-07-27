package xoserver;

import DTOS.Response;
import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import static xoserver.PlayerDAO.logOutAllPlayers;
import static xoserver.PlayerHandler.onlinePlayers;

public class FXMLDocumentBase extends BorderPane {

    // Constants and fields
    private static final int SERVER_PORT = 5006;
    private static final int THREAD_POOL_SIZE = 50;
    private final List<PlayerHandler> players = new ArrayList<>();
    private ServerSocket serverSocket;
    private boolean startFlag;
    private ExecutorService executorService;
    // UI Components
    private final CategoryAxis categoryAxis;
    private final NumberAxis numberAxis;
    private final BarChart<String, Number> playersChart;
    private final Label onlinePlayersLabel;
    private final Label offlinePlayersLabel;
    private final Label inGamePlayersLabel;
    private final Label titleLabel;
    private final Label ipAddressLabel;
    private final FlowPane actionPane;
    private final Button startButton;

    public FXMLDocumentBase(Stage stage) {
        // Initialize UI elements
        titleLabel = new Label("Tic Tac Toe Server");
        ipAddressLabel = new Label("IP Address: " + getLocalIPAddress());
        actionPane = new FlowPane();
        startButton = new Button("Start");

        categoryAxis = new CategoryAxis();
        numberAxis = new NumberAxis();
        playersChart = new BarChart<>(categoryAxis, numberAxis);

        onlinePlayersLabel = new Label("Online Players: 0");
        offlinePlayersLabel = new Label("Offline Players: 0");
        inGamePlayersLabel = new Label("In-Game Players: 0");

        // Style and set properties
        styleUIComponents();
        setButtonActions(stage);

        // Layout setup
        setupLayout(stage);
    }

    private void styleUIComponents() {
        // Styles for UI components
        titleLabel.setFont(new Font("Arial Black", 18.0));
        titleLabel.setTextFill(Color.web("#333333"));
        titleLabel.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10px;");
        ipAddressLabel.setFont(new Font("Arial", 16.0));
        ipAddressLabel.setTextFill(Color.web("#666666"));

        // Button styles
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px; -fx-border-radius: 5px;");
        startButton.setOnMouseEntered(event -> startButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px; -fx-border-radius: 5px;"));
        startButton.setOnMouseExited(event -> startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 10px; -fx-border-radius: 5px;"));

        // Chart setup
        categoryAxis.setLabel("Status");
        numberAxis.setLabel("Number of Players");
        numberAxis.setTickLabelFill(Color.web("#333333"));
        categoryAxis.setTickLabelFill(Color.web("#333333"));
        playersChart.setTitle("Players in the Server");
        playersChart.setTitleSide(javafx.geometry.Side.TOP);

        // Initialize chart series
        XYChart.Series<String, Number> series1 = createSeries("Offline", 0);
        XYChart.Series<String, Number> series2 = createSeries("In-Game", 0);
        XYChart.Series<String, Number> series3 = createSeries("Online", 0);
        playersChart.getData().addAll(series1, series2, series3);
        playersChart.setBarGap(5);
        playersChart.setCategoryGap(20);
    }

    private void setButtonActions(Stage stage) {
        // Set button actions
        startButton.setOnAction(this::handleStartStopAction);
        stage.setOnCloseRequest(event -> stopServer());
    }

    private void setupLayout(Stage stage) {
        // Layout setup
        VBox topContainer = new VBox(10, titleLabel, ipAddressLabel);
        topContainer.setAlignment(javafx.geometry.Pos.CENTER);
        VBox statusContainer = new VBox(5, onlinePlayersLabel, offlinePlayersLabel, inGamePlayersLabel);
        statusContainer.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        statusContainer.setPadding(new Insets(10));
        VBox topLeftContainer = new VBox(topContainer, statusContainer);
        topLeftContainer.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        setTop(topLeftContainer);
        setCenter(playersChart);
        actionPane.getChildren().add(startButton);
        setBottom(actionPane);
        actionPane.setAlignment(javafx.geometry.Pos.CENTER);
        actionPane.setPadding(new Insets(20));
    }

    private void handleStartStopAction(ActionEvent event) {
        if (!startFlag) {
            startServer();
        } else {
            stopServer();
        }
    }

    private void startServer() {
        updateUI();

        startFlag = true;
        startButton.setText("Stop");
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        new Thread(this::runServer).start();

    }

    private void stopServer() {
        try {
            startButton.setText("Start");
            startFlag = false;
            logOutAllPlayers();
            sendJsonToAllPlayers();
            PlayerHandler.clearOnlinePlayers();
            for (PlayerHandler player : players) {
                player.stop();
            }
            for (Socket socket : onlinePlayers.values()) {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            resetGraph(); // Call updateUI directly

        } catch (IOException ex) {
            System.err.println("Error stopping server: " + ex.getMessage());
        }
    }

    private void runServer() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            while (startFlag) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New Client");
                PlayerHandler playerHandler = new PlayerHandler(clientSocket, this);
                players.add(playerHandler);
                executorService.execute(playerHandler);
                // Update UI after handling a new client
                Platform.runLater(this::updateUI);
            }
        } catch (IOException ex) {
            if (startFlag) {
                System.err.println("Server error: " + ex.getMessage());
            }
        }
    }

    // Method to reset the graph
    public void resetGraph() {
        Platform.runLater(() -> {
            playersChart.getData().clear();
            XYChart.Series<String, Number> series1 = createSeries("Offline", 0);
            XYChart.Series<String, Number> series2 = createSeries("In-Game", 0);
            XYChart.Series<String, Number> series3 = createSeries("Online", 0);
            playersChart.getData().addAll(series1, series2, series3);
            onlinePlayersLabel.setText("Online Players: 0");
            offlinePlayersLabel.setText("Offline Players: 0");
            inGamePlayersLabel.setText("In-Game Players: 0");
        });
    }

    public void updateUI() {
        // Ensure that the UI updates occur on the JavaFX Application Thread
        Platform.runLater(() -> {
            try {
                updateChart();
                updateLabels();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateChart() {
        System.out.println("Updating chart: Offline = " + PlayerDAO.getOfflineNumber() + ", In-Game = " + PlayerDAO.getIngameNumber() + ", Online = " + PlayerDAO.getOnlineNumber());

        Platform.runLater(() -> {
            playersChart.getData().clear();
            XYChart.Series<String, Number> offlineSeries = createSeries("Offline", PlayerDAO.getOfflineNumber());
            XYChart.Series<String, Number> inGameSeries = createSeries("In-Game", PlayerDAO.getIngameNumber());
            XYChart.Series<String, Number> onlineSeries = createSeries("Online", PlayerDAO.getOnlineNumber());
            playersChart.getData().addAll(offlineSeries, inGameSeries, onlineSeries);
        });
    }

    private XYChart.Series<String, Number> createSeries(String name, int count) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        series.getData().add(new XYChart.Data<>(name, count));
        return series;
    }

    private void updateLabels() {
        System.out.println("Updating Labels: Offline = " + PlayerDAO.getOfflineNumber() + ", In-Game = " + PlayerDAO.getIngameNumber() + ", Online = " + PlayerDAO.getOnlineNumber());

        Platform.runLater(() -> {
            onlinePlayersLabel.setText("Online Players: " + PlayerDAO.getOnlineNumber());
            offlinePlayersLabel.setText("Offline Players: " + PlayerDAO.getOfflineNumber());
            inGamePlayersLabel.setText("In-Game Players: " + PlayerDAO.getIngameNumber());
        });
    }

    private String getLocalIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            return "Unable to retrieve IP address";
        }
    }

    private static void sendJsonToAllPlayers() {
        Gson gson = new Gson();
        System.out.println("Server down");
        Map<String, Socket> snapshot = new ConcurrentHashMap<>(onlinePlayers);
        for (Map.Entry<String, Socket> entry : snapshot.entrySet()) {
            Socket socket = entry.getValue();
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                System.out.println("Sending JSON to player: " + entry.getKey());
                String jsonResponse = gson.toJson(new Response(false, "Server is Down", null));
                bw.write(jsonResponse);
                bw.newLine();
                bw.flush();
            } catch (IOException ex) {
                System.err.println("Error sending message to player " + entry.getKey() + ": " + ex.getMessage());
                onlinePlayers.remove(entry.getKey());
            }
        }
    }
}
