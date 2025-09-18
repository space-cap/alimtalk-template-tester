package com.ezlevup.controller;

import com.ezlevup.util.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class TemplateController {

    @FXML
    private TextArea requestTextArea;

    @FXML
    private Button sendButton;

    @FXML
    private VBox chatMessagesBox;

    @FXML
    private VBox previewBox;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private Label chatTitleLabel;

    @FXML
    private Label characterCountLabel;

    @FXML
    private Label previewTitleLabel;

    private VBox loadingMessageBox;
    private Timeline loadingAnimation;

    public void initialize() {
        // Set initial state for new conversation
        requestTextArea.setPromptText("Claude에게 메시지 보내기...");
        chatTitleLabel.setText("새 대화");
        previewTitleLabel.setText("템플릿 미리보기");

        // Enable resizable split pane
        mainSplitPane.setDividerPositions(0.5);

        // Auto-scroll chat to bottom - remove binding as we'll control it manually
        // chatScrollPane.vvalueProperty().bind(chatMessagesBox.heightProperty());

        // Add character count listener
        requestTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            updateCharacterCount(newValue != null ? newValue.length() : 0);
        });

        // Initialize character count
        updateCharacterCount(0);

        // Start with clean slate - no sample messages
        chatMessagesBox.getChildren().clear();
        previewBox.getChildren().clear();

        // Add welcome message
        addSystemMessage("안녕하세요! 알림톡 템플릿을 생성해드리겠습니다. 어떤 종류의 템플릿이 필요하신가요?");

        // Add initial preview content
        addInitialPreviewContent();
    }

    private void addInitialPreviewContent() {
        // Create initial preview content
        VBox initialContent = new VBox(16.0);
        initialContent.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label welcomeIcon = new Label("📱");
        welcomeIcon.setStyle("-fx-font-size: 48px;");

        Label welcomeTitle = new Label("알림톡 템플릿 미리보기");
        welcomeTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label welcomeDesc = new Label("생성된 템플릿이 여기에 표시됩니다.");
        welcomeDesc.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666;");

        initialContent.getChildren().addAll(welcomeIcon, welcomeTitle, welcomeDesc);
        previewBox.getChildren().add(initialContent);
    }

    private void updateCharacterCount(int count) {
        if (characterCountLabel != null) {
            characterCountLabel.setText(count + "/2000");
        }
    }

    @FXML
    private void handleSendMessage() {
        String requestContent = requestTextArea.getText().trim();

        if (requestContent.isEmpty()) {
            showAlert("입력 오류", "요청사항을 입력해주세요.");
            return;
        }

        // Add user message to chat
        addUserMessage(requestContent);

        // Show loading animation
        addLoadingMessage();

        sendButton.setDisable(true);
        sendButton.setText("전송중...");

        Task<Void> createTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    createTemplateWithAPI(requestContent);
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        removeLoadingMessage();
                        addSystemMessage("템플릿 생성 중 오류가 발생했습니다: " + e.getMessage());
                        sendButton.setDisable(false);
                        sendButton.setText("전송");
                    });
                }
                return null;
            }
        };

        new Thread(createTask).start();
    }

    @FXML
    private void loadTemplate1() {
        requestTextArea.setText("학원 수강 안내용 알림톡 템플릿을 만들어주세요. 학생명, 과목명, 수업시간을 포함해주세요.");
    }

    @FXML
    private void loadTemplate2() {
        requestTextArea.setText("카페에서 주문 완료 알림을 보내고 싶어요. 고객명과 주문내용, 픽업시간을 포함해주세요.");
    }

    @FXML
    private void loadTemplate3() {
        requestTextArea.setText("배송 안내 메시지를 만들어주세요. 고객명, 상품명, 배송예정일을 포함해주세요.");
    }

    @FXML
    private void loadTemplate4() {
        requestTextArea.setText("병원 예약 확인 알림을 만들어주세요. 환자명, 진료과, 예약일시를 포함해주세요.");
    }

    @FXML
    private void loadTemplate5() {
        requestTextArea.setText("이벤트 참여 안내 메시지를 만들어주세요. 이벤트명, 참여방법, 혜택을 포함해주세요.");
    }

    @FXML
    private void createNewChat() {
        // Clear chat and start new conversation
        chatMessagesBox.getChildren().clear();
        previewBox.getChildren().clear();
        requestTextArea.clear();
        chatTitleLabel.setText("새 대화");

        // Add initial system message
        addSystemMessage("안녕하세요! 새로운 알림톡 템플릿을 만들어드리겠습니다. 어떤 종류의 템플릿이 필요하신가요?");

        // Add initial preview content
        addInitialPreviewContent();
    }

    @FXML
    private void loadCurrentTemplate() {
        // This represents the current active template
        chatTitleLabel.setText("학원 수강 안내 카카오 알림톡 템플릿");
        requestTextArea.setText("현재 진행 중인 학원 수강 안내 카카오 알림톡 템플릿입니다.");
    }

    private void addUserMessage(String message) {
        VBox userMessageBox = new VBox(8.0);
        userMessageBox.setAlignment(Pos.CENTER_RIGHT);

        HBox messageHeader = new HBox(8.0);
        messageHeader.setAlignment(Pos.CENTER_RIGHT);

        Label userLabel = new Label("사용자");
        userLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        messageHeader.getChildren().add(userLabel);

        Label messageContent = new Label(message);
        messageContent.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #e3f2fd; -fx-padding: 12; -fx-background-radius: 12; -fx-max-width: 400;");
        messageContent.setWrapText(true);

        userMessageBox.getChildren().addAll(messageHeader, messageContent);
        chatMessagesBox.getChildren().add(userMessageBox);

        // Clear input
        requestTextArea.clear();

        // Scroll to bottom
        Platform.runLater(() -> {
            if (chatScrollPane != null) {
                chatScrollPane.setVvalue(1.0);
            }
        });
    }

    private void addSystemMessage(String message) {
        VBox systemMessageBox = new VBox(8.0);
        systemMessageBox.setAlignment(Pos.CENTER_LEFT);

        HBox messageHeader = new HBox(8.0);
        messageHeader.setAlignment(Pos.CENTER_LEFT);

        Label systemIcon = new Label("🤖");
        systemIcon.setStyle("-fx-font-size: 16px;");

        Label systemLabel = new Label("Claude");
        systemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        messageHeader.getChildren().addAll(systemIcon, systemLabel);

        Label messageContent = new Label(message);
        messageContent.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 12; -fx-max-width: 400;");
        messageContent.setWrapText(true);

        systemMessageBox.getChildren().addAll(messageHeader, messageContent);
        chatMessagesBox.getChildren().add(systemMessageBox);

        // Scroll to bottom
        Platform.runLater(() -> {
            if (chatScrollPane != null) {
                chatScrollPane.setVvalue(1.0);
            }
        });
    }

    private void addTemplateMessage(String templateTitle, String templateContent) {
        VBox templateMessageBox = new VBox(8.0);
        templateMessageBox.setAlignment(Pos.CENTER_LEFT);

        HBox messageHeader = new HBox(8.0);
        messageHeader.setAlignment(Pos.CENTER_LEFT);

        Label systemIcon = new Label("🤖");
        systemIcon.setStyle("-fx-font-size: 16px;");

        Label systemLabel = new Label("Claude");
        systemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        messageHeader.getChildren().addAll(systemIcon, systemLabel);

        // Template container
        VBox templateContainer = new VBox(8.0);
        templateContainer.setStyle(
                "-fx-background-color: #fff3e0; -fx-padding: 16; -fx-background-radius: 12; -fx-border-color: #ffcc80; -fx-border-width: 1; -fx-border-radius: 12; -fx-max-width: 500;");

        Label titleLabel = new Label("✅ 템플릿이 생성되었습니다!");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label templateTitleLabel = new Label("제목: " + templateTitle);
        templateTitleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label contentLabel = new Label("내용:");
        contentLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label templateContentLabel = new Label(templateContent);
        templateContentLabel.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #666666; -fx-background-color: #ffffff; -fx-padding: 8; -fx-background-radius: 6;");
        templateContentLabel.setWrapText(true);

        templateContainer.getChildren().addAll(titleLabel, templateTitleLabel, contentLabel, templateContentLabel);

        templateMessageBox.getChildren().addAll(messageHeader, templateContainer);
        chatMessagesBox.getChildren().add(templateMessageBox);

        // Scroll to bottom
        Platform.runLater(() -> {
            if (chatScrollPane != null) {
                chatScrollPane.setVvalue(1.0);
            }
        });
    }

    private void createTemplateWithAPI(String requestContent) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // HTTP/1.1 강제
                .build();
        ObjectMapper mapper = new ObjectMapper();

        // Create request body for FastAPI (curl 테스트와 동일한 구조)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("userId", 123); // curl과 동일하게 camelCase 사용
        requestBody.put("requestContent", requestContent); // curl과 동일하게 camelCase 사용
        requestBody.put("conversationContext", ""); // 누락된 필수 필드 추가

        String jsonBody = mapper.writeValueAsString(requestBody);

        // Console output for debugging
        System.out.println("Request JSON Body: " + jsonBody);
        System.out.println("JSON Body Length: " + jsonBody.length());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://3.34.43.149:8000/ai/templates"))
                .header("Accept-Encoding", "gzip, deflate")
                .header("Cache-Control", "no-cache")
                .header("Content-Type", "application/json")
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36")
                .header("accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Console output for debugging
        System.out.println("Request Headers: " + request.headers().map());
        System.out.println("Response Status Code: " + response.statusCode());
        System.out.println("Response Headers: " + response.headers().map());
        System.out.println("Response Body: " + response.body());

        Platform.runLater(() -> {
            try {
                removeLoadingMessage();
                if (response.statusCode() == 200) {
                    // Parse response and add to preview panel instead of chat
                    String responseBody = response.body();
                    processTemplateResponse(responseBody);
                    addSystemMessage("템플릿이 생성되었습니다! 오른쪽 미리보기를 확인해주세요.");
                } else {
                    addSystemMessage("템플릿 생성에 실패했습니다: " + response.body());
                }
            } catch (Exception e) {
                addSystemMessage("응답 처리 중 오류가 발생했습니다: " + e.getMessage());
            } finally {
                sendButton.setDisable(false);
                sendButton.setText("전송");
            }
        });
    }

    private void processTemplateResponse(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(responseBody, Map.class);

            String title = (String) response.get("title");
            String content = (String) response.get("content");

            // Add template to preview panel instead of chat
            addTemplateToPreview(
                    title != null ? title : "알림톡 템플릿",
                    content != null ? content : "템플릿 내용이 생성되었습니다.");

        } catch (Exception e) {
            // If JSON parsing fails, show raw response in preview
            addTemplateToPreview("생성된 템플릿", responseBody);
        }
    }

    private void addTemplateToPreview(String templateTitle, String templateContent) {
        // Clear existing preview content
        previewBox.getChildren().clear();

        // Create Claude desktop style message bubble
        VBox messageContainer = new VBox(8.0);
        messageContainer.setAlignment(Pos.TOP_LEFT); // 또는 CENTER_LEFT
        messageContainer.setStyle("-fx-padding: 16 20;");

        // Message header with Claude icon and name
        HBox headerBox = new HBox(8.0);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label claudeIcon = new Label("☁");
        claudeIcon.setStyle(
                "-fx-font-size: 14px; -fx-text-fill: #888888; -fx-background-color: #f5f5f5; -fx-background-radius: 50%; -fx-padding: 4; -fx-min-width: 24; -fx-min-height: 24; -fx-alignment: center;");

        Label claudeLabel = new Label("Claude");
        claudeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        headerBox.getChildren().addAll(claudeIcon, claudeLabel);

        // Main content bubble (matching image7.png style)
        VBox contentBubble = new VBox(12.0);
        contentBubble.setAlignment(Pos.TOP_LEFT); // 추가
        contentBubble.setStyle(
                "-fx-background-color: #ffffff; -fx-padding: 16; -fx-background-radius: 12; -fx-border-color: #e8e8e8; -fx-border-width: 1; -fx-border-radius: 12; -fx-max-width: 400;");

        // Header text
        Label headerText = new Label("주문이 완료되었습니다!");
        headerText.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        System.out.println("templateContent: " + templateContent);

        // Main template content - use TextArea for better text handling
        TextArea mainContent = new TextArea(templateContent);
        mainContent.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #333333; -fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        mainContent.setWrapText(true);
        mainContent.setEditable(false);
        mainContent.setMaxWidth(350);
        mainContent.setPrefWidth(350);
        mainContent.setPrefRowCount(20); // 충분한 행 수
        mainContent.setMaxHeight(Double.MAX_VALUE);
        mainContent.setPrefHeight(Region.USE_COMPUTED_SIZE);

        // Footer with copy icon (matching image7.png)
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        footerBox.setStyle("-fx-padding: 8 0 0 0;");

        Button copyButton = new Button("📄");
        copyButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888888; -fx-font-size: 14px; -fx-cursor: hand; -fx-border: none; -fx-padding: 4; -fx-background-radius: 4;");
        copyButton.setOnAction(e -> {
            // TODO: Implement copy functionality
            System.out.println("Template copied to clipboard");
        });

        footerBox.getChildren().add(copyButton);

        // Add elements to content bubble
        contentBubble.getChildren().addAll(headerText, mainContent, footerBox);
        // contentBubble.getChildren().addAll(mainContent);

        // Add header and bubble to message container
        messageContainer.getChildren().addAll(headerBox, contentBubble);

        // 부모 VBox의 높이 제한도 확인
        previewBox.setMaxHeight(Double.MAX_VALUE);
        // previewBox.setPrefHeight(Region.USE_COMPUTED_SIZE);

        previewBox.setAlignment(Pos.TOP_LEFT); // FXML에서 설정하거나 코드로 추가

        // Add the message to preview box
        previewBox.getChildren().add(messageContainer);

    }

    private void addLoadingMessage() {
        loadingMessageBox = new VBox(8.0);
        loadingMessageBox.setAlignment(Pos.CENTER_LEFT);

        HBox messageHeader = new HBox(8.0);
        messageHeader.setAlignment(Pos.CENTER_LEFT);

        Label systemIcon = new Label("🤖");
        systemIcon.setStyle("-fx-font-size: 16px;");

        Label systemLabel = new Label("Claude");
        systemLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        messageHeader.getChildren().addAll(systemIcon, systemLabel);

        // Loading animation container
        HBox loadingContainer = new HBox(8.0);
        loadingContainer.setAlignment(Pos.CENTER_LEFT);
        loadingContainer.setStyle(
                "-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 12; -fx-max-width: 400;");

        Label loadingIcon = new Label("⚡");
        loadingIcon.setStyle("-fx-font-size: 16px;");

        Label loadingText = new Label("템플릿을 생성하고 있습니다");
        loadingText.setStyle("-fx-font-size: 13px; -fx-text-fill: #333333;");

        loadingContainer.getChildren().addAll(loadingIcon, loadingText);

        loadingMessageBox.getChildren().addAll(messageHeader, loadingContainer);
        chatMessagesBox.getChildren().add(loadingMessageBox);

        // Create loading animation - rotating through different icons
        String[] icons = { "⚡", "💫", "✨", "🔄", "💭", "🧠" };
        loadingAnimation = new Timeline();

        for (int i = 0; i < icons.length; i++) {
            final String icon = icons[i];
            loadingAnimation.getKeyFrames().add(
                    new KeyFrame(Duration.millis((i + 1) * 300), e -> loadingIcon.setText(icon)));
        }

        loadingAnimation.setCycleCount(Timeline.INDEFINITE);
        loadingAnimation.play();

        // Scroll to bottom
        Platform.runLater(() -> {
            if (chatScrollPane != null) {
                chatScrollPane.setVvalue(1.0);
            }
        });
    }

    private void removeLoadingMessage() {
        if (loadingAnimation != null) {
            loadingAnimation.stop();
        }
        if (loadingMessageBox != null && chatMessagesBox.getChildren().contains(loadingMessageBox)) {
            chatMessagesBox.getChildren().remove(loadingMessageBox);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}