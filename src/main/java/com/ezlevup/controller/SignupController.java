package com.ezlevup.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField nicknameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button requestVerificationButton;

    @FXML
    private Button signupButton;

    @FXML
    private void handleRequestVerification() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert("오류", "이메일을 입력해주세요.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("오류", "올바른 이메일 형식이 아닙니다.");
            return;
        }

        showAlert("정보", "인증 요청이 전송되었습니다. 이메일을 확인해주세요.");
    }

    @FXML
    private void handleSignup() {
        String email = emailField.getText().trim();
        String nickname = nicknameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isEmpty() || nickname.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert("오류", "모든 필드를 입력해주세요.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("오류", "올바른 이메일 형식이 아닙니다.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("오류", "비밀번호가 일치하지 않습니다.");
            return;
        }

        if (password.length() < 6) {
            showAlert("오류", "비밀번호는 6자 이상이어야 합니다.");
            return;
        }

        showAlert("성공", "회원가입이 완료되었습니다!");
        clearFields();
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
        nicknameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }
}