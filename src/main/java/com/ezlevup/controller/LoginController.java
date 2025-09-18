package com.ezlevup.controller;

import com.ezlevup.HelloWorldApp;
import com.ezlevup.util.Settings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label testAccountLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("오류", "이메일과 비밀번호를 모두 입력해주세요.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("오류", "올바른 이메일 형식이 아닙니다.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("로그인 중...");

        Task<Void> loginTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    authenticateWithAPI(email, password);
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showAlert("오류", "로그인 중 오류가 발생했습니다: " + e.getMessage());
                        loginButton.setDisable(false);
                        loginButton.setText("로그인");
                    });
                }
                return null;
            }
        };

        new Thread(loginTask).start();
    }

    private void authenticateWithAPI(String email, String password) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        String requestBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Settings.getLoginApiUrl()))
                .header("Content-Type", "application/json")
                .header("accept", "*/*")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        final String userEmail = email;

        Platform.runLater(() -> {
            try {
                if (response.statusCode() == 200) {
                    JsonNode responseData = mapper.readTree(response.body());
                    JsonNode data = responseData.get("data");

                    if (data != null) {
                        String accessToken = data.get("accessToken").asText();
                        String refreshToken = data.get("refreshToken").asText();

                        System.out.println("Access Token: " + accessToken);
                        System.out.println("Refresh Token: " + refreshToken);

                        HelloWorldApp app = HelloWorldApp.getInstance();
                        if (app != null) {
                            app.showMainScreen(userEmail, accessToken, refreshToken);
                        } else {
                            showAlert("성공", "로그인이 완료되었습니다!\nAccess Token이 발급되었습니다.");
                        }
                    } else {
                        showAlert("오류", "응답 데이터 형식이 올바르지 않습니다.");
                    }
                } else {
                    showAlert("오류", "로그인 실패: " + response.body());
                }
            } catch (Exception e) {
                showAlert("오류", "응답 처리 중 오류가 발생했습니다: " + e.getMessage());
            } finally {
                loginButton.setDisable(false);
                loginButton.setText("로그인");
            }
        });
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        emailField.clear();
        passwordField.clear();
    }

    @FXML
    private void handleTestAccountClick(MouseEvent event) {
        emailField.setText("lee2@naver.com");
        passwordField.setText("test1234");
    }
}