# 이미지 폴더 구조

이 폴더는 JavaFX 애플리케이션에서 사용하는 이미지 파일들을 관리합니다.

## 폴더 구조

```
src/main/resources/images/
├── icons/          # UI 아이콘 파일들 (16x16, 32x32, 64x64 등)
├── logos/          # 애플리케이션 로고 파일들
├── backgrounds/    # 배경 이미지 파일들
└── README.md       # 이 파일
```

## 지원하는 이미지 형식

- **PNG** (권장) - 투명도 지원
- **JPG/JPEG** - 사진용
- **GIF** - 애니메이션 지원
- **BMP** - 기본 비트맵

## 사용 방법

JavaFX에서 이미지를 로드할 때:

```java
// 아이콘 로드
Image icon = new Image(getClass().getResourceAsStream("/images/icons/home.png"));

// 로고 로드
Image logo = new Image(getClass().getResourceAsStream("/images/logos/app-logo.png"));

// 배경 이미지 로드
Image background = new Image(getClass().getResourceAsStream("/images/backgrounds/main-bg.jpg"));
```

## 권장 사항

- **아이콘**: 16x16, 24x24, 32x32, 48x48, 64x64 픽셀 크기
- **로고**: 최대 512x512 픽셀
- **배경**: 애플리케이션 창 크기에 맞춰 조정 (예: 800x600, 1024x768)
- **파일명**: 소문자 + 하이픈 사용 (예: app-icon.png, login-bg.jpg)