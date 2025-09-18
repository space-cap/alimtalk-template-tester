package com.ezlevup.controller;

import com.ezlevup.HelloWorldApp;
import com.ezlevup.util.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MainController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userInfoLabel;

    @FXML
    private Button logoutButton;

    private String userEmail;
    private String accessToken;
    private String refreshToken;

    public void initialize() {
        welcomeLabel.setText("메인 페이지에 오신 것을 환영합니다!");
    }

    public void setUserInfo(String email, String accessToken, String refreshToken) {
        this.userEmail = email;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        if (userInfoLabel != null) {
            userInfoLabel.setText("로그인된 사용자: " + email);
        }
    }

    @FXML
    private void handleLogout() {
        logoutButton.setDisable(true);
        logoutButton.setText("로그아웃 중...");

        Task<Void> logoutTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    logoutWithAPI();
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("오류", "로그아웃 중 오류가 발생했습니다: " + e.getMessage());
                        logoutButton.setDisable(false);
                        logoutButton.setText("로그아웃");
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

        String requestBody = String.format("{\"refreshToken\":\"%s\"}", refreshToken != null ? refreshToken : "");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Settings.getLogoutApiUrl()))
                .header("Content-Type", "application/json")
                .header("accept", "*/*")
                .header("Authorization", "Bearer " + accessToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Platform.runLater(() -> {
            try {
                if (response.statusCode() == 200) {
                    showAlert("성공", "로그아웃이 완료되었습니다.");

                    HelloWorldApp app = HelloWorldApp.getInstance();
                    if (app != null) {
                        app.showWelcomeScreen();
                    }
                } else {
                    showAlert("오류", "로그아웃 실패: " + response.body());
                }
            } catch (Exception e) {
                showAlert("오류", "응답 처리 중 오류가 발생했습니다: " + e.getMessage());
            } finally {
                logoutButton.setDisable(false);
                logoutButton.setText("로그아웃");
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}