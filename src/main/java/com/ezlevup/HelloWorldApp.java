package com.ezlevup;

import com.ezlevup.controller.MainController;
import com.ezlevup.util.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HelloWorldApp extends Application {

    private static HelloWorldApp instance;
    private Stage primaryStage;
    private BorderPane rootLayout;
    private boolean isLoggedIn = false;
    private MenuItem loginItem;
    private MenuItem signupItem;
    private MenuItem logoutItem;
    private MenuItem settingsItem;
    private MenuItem mainItem;
    private String currentUserEmail;
    private String currentAccessToken;
    private String currentRefreshToken;

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("EzLevUp Application");

        initRootLayout();
        showWelcomeScreen();
    }

    public static HelloWorldApp getInstance() {
        return instance;
    }

    private void initRootLayout() {
        rootLayout = new BorderPane();

        MenuBar menuBar = createMenuBar();
        rootLayout.setTop(menuBar);

        Scene scene = new Scene(rootLayout, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // 홈 메뉴
        Menu homeMenu = new Menu("홈");
        MenuItem homeItem = new MenuItem("홈으로");
        mainItem = new MenuItem("메인");

        homeItem.setOnAction(e -> showWelcomeScreen());
        mainItem.setOnAction(e -> showMainScreenIfLoggedIn());

        homeMenu.getItems().addAll(homeItem, mainItem);

        // 사용자 메뉴
        Menu userMenu = new Menu("사용자");
        loginItem = new MenuItem("로그인");
        signupItem = new MenuItem("회원가입");
        logoutItem = new MenuItem("로그아웃");

        loginItem.setOnAction(e -> showLoginScreen());
        signupItem.setOnAction(e -> showSignupScreen());
        logoutItem.setOnAction(e -> handleMenuLogout());

        userMenu.getItems().addAll(loginItem, signupItem, new SeparatorMenuItem(), logoutItem);

        // 설정 메뉴
        Menu settingsMenu = new Menu("설정");
        settingsItem = new MenuItem("환경설정");
        settingsItem.setOnAction(e -> showSettingsScreen());
        settingsMenu.getItems().add(settingsItem);

        menuBar.getMenus().addAll(homeMenu, userMenu, settingsMenu);

        updateMenuVisibility();

        return menuBar;
    }

    public void showWelcomeScreen() {
        // Create logo placeholder (orange starburst)
        Label logoLabel = new Label("✦");
        logoLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #ff6b35; -fx-font-weight: bold;");

        // Main title
        Label titleLabel = new Label("EzLevUp for Windows");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2c2c2c; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // Subtitle
        Label subtitleLabel = new Label("EzLevUp과 대화하는 가장 빠른 방법");
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666; -fx-font-family: 'Segoe UI', Arial, sans-serif;");

        // Start button
        Button startButton = new Button("시작하기");
        startButton.setStyle(
            "-fx-background-color: #2c2c2c; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 12 32 12 32; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand; " +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );
        startButton.setOnAction(e -> {
            if (isLoggedIn()) {
                showMainScreenIfLoggedIn();
            } else {
                showLoginScreen();
            }
        });

        // Create vertical layout
        VBox contentBox = new VBox(30);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.getChildren().addAll(logoLabel, titleLabel, subtitleLabel);

        // Add some spacing before the button
        Region spacer = new Region();
        spacer.setPrefHeight(80);

        VBox mainBox = new VBox();
        mainBox.setAlignment(Pos.CENTER);
        mainBox.getChildren().addAll(contentBox, spacer, startButton);

        StackPane welcomePane = new StackPane();
        welcomePane.setStyle("-fx-background-color: #f8f8f8;");
        welcomePane.getChildren().add(mainBox);

        rootLayout.setCenter(welcomePane);
        setLoggedIn(false);
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("Could not find login.fxml");
                return;
            }
            StackPane loginScreen = loader.load();
            rootLayout.setCenter(loginScreen);
        } catch (IOException e) {
            System.err.println("Error loading login screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showSignupScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("Could not find signup.fxml");
                return;
            }
            StackPane signupScreen = loader.load();
            rootLayout.setCenter(signupScreen);
        } catch (IOException e) {
            System.err.println("Error loading signup screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void showMainScreen(String userEmail, String accessToken, String refreshToken) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("Could not find main.fxml");
                return;
            }
            StackPane mainScreen = loader.load();

            MainController mainController = loader.getController();
            if (mainController != null) {
                mainController.setUserInfo(userEmail, accessToken, refreshToken);
            }

            rootLayout.setCenter(mainScreen);

            this.currentUserEmail = userEmail;
            this.currentAccessToken = accessToken;
            this.currentRefreshToken = refreshToken;
            setLoggedIn(true);
        } catch (IOException e) {
            System.err.println("Error loading main screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateMenuVisibility() {
        if (loginItem != null && signupItem != null && logoutItem != null && mainItem != null) {
            loginItem.setVisible(!isLoggedIn);
            signupItem.setVisible(!isLoggedIn);
            logoutItem.setVisible(isLoggedIn);
            mainItem.setVisible(isLoggedIn);
        }
    }

    public void setLoggedIn(boolean loggedIn) {
        this.isLoggedIn = loggedIn;
        updateMenuVisibility();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    private void handleMenuLogout() {
        if (!isLoggedIn || currentAccessToken == null) {
            return;
        }

        Task<Void> logoutTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    logoutWithAPI();
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("오류", "로그아웃 중 오류가 발생했습니다: " + e.getMessage());
                    });
                }
                return null;
            }
        };

        new Thread(logoutTask).start();
    }

    private void logoutWithAPI() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String requestBody = String.format("{\"refreshToken\":\"%s\"}",
            currentRefreshToken != null ? currentRefreshToken : "");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Settings.getLogoutApiUrl()))
                .header("Content-Type", "application/json")
                .header("accept", "*/*")
                .header("Authorization", "Bearer " + currentAccessToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Platform.runLater(() -> {
            try {
                if (response.statusCode() == 200) {
                    showAlert("성공", "로그아웃이 완료되었습니다.");

                    currentUserEmail = null;
                    currentAccessToken = null;
                    currentRefreshToken = null;
                    showWelcomeScreen();
                } else {
                    showAlert("오류", "로그아웃 실패: " + response.body());
                }
            } catch (Exception e) {
                showAlert("오류", "응답 처리 중 오류가 발생했습니다: " + e.getMessage());
            }
        });
    }

    public void showSettingsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("Could not find settings.fxml");
                return;
            }
            StackPane settingsScreen = loader.load();
            rootLayout.setCenter(settingsScreen);
        } catch (IOException e) {
            System.err.println("Error loading settings screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showMainScreenIfLoggedIn() {
        if (isLoggedIn && currentUserEmail != null && currentAccessToken != null && currentRefreshToken != null) {
            showMainScreen(currentUserEmail, currentAccessToken, currentRefreshToken);
        } else {
            showAlert("알림", "로그인이 필요합니다.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}