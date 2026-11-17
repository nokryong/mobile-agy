# Mobile-agy

Antigravity 전용 Android 클라이언트입니다. Mobile Codex의 Android 앱과 앱 내 Linux·CLI 연결 코드를 기반으로 합니다.

- 앱 ID `dev.mobileagy.app`, 버전 `0.1.1`
- Google 계정으로 Antigravity CLI에 로그인
- 계정에서 조회한 모델 선택, 응답 스트리밍, 중지, 저장된 대화 재개
- 프로젝트, 파일 탐색, 터미널과 휴대폰 도구를 기존 Android 기반에서 재사용
- 프로젝트 작업은 Antigravity 참가자 설정의 권한과 앱의 승인 확인을 적용
- 파일·이미지 첨부와 파일 멘션, PDF 페이지 열람, 맞춤 지침, 완료·실패 알림
- 사용자가 제공한 파란 별 로고를 앱 아이콘과 앱 내 로고에 적용

## 시작하기

ARM64 Android 10 이상에서 APK를 설치합니다. 계정 설정에서 앱 내 Linux 환경을 설치·활성화한 뒤 Antigravity CLI 설치와 로그인을 진행합니다. 로그인 완료 후 준비 상태를 새로고침하면 모델을 선택할 수 있습니다.

## 검증 범위

로컬 APK 빌드, Android 테스트 APK 빌드, 단독 엔진·첨부 접근 권한·이미지 전달·업데이트 판별 테스트 14개, 공급자 런타임 테스트 21개(1개 건너뜀), 모바일/데스크톱 화면 검사를 통과했습니다. Windows Antigravity CLI의 실제 모델 응답과 모델 목록 조회도 확인했습니다. Android 실기기 로그인·모델 호출·PDF 렌더링은 미검증입니다.

첨부 원본은 보존하며 모델에 전달할 이미지는 최대 변 1024픽셀 이하로 샘플링합니다. PDF는 페이지별 이미지로 전달합니다. 다른 파일은 64 KiB 단위로 읽으며 텍스트 또는 base64 바이트를 제공합니다.

음성 대화·플러그인·스킬·사용자 MCP·다중 계정은 이번 앱의 범위에서 제외합니다. 음성 받아쓰기와 앱 내부 파일·휴대폰 도구는 유지합니다. 비공개 저장소의 릴리스 다운로드에는 GitHub 로그인이 필요하며 앱 내 공개 릴리스 조회에는 표시되지 않습니다.

기존 Codex 전용 기능과 테스트 일부가 소스에 남아 있습니다. `AntigravityEngineTest`가 이 앱의 기본 전송 경로를 검증합니다. 이전 프로젝트의 자동 배포 작업은 수동 실행으로 전환했습니다.

## 빌드

`tools/prepare_runtime.py`, `tools/prepare_devtools.py`, `tools/build_native.py`로 Android 런타임을 준비하고 Android SDK와 JDK 17을 설정합니다.

```sh
./gradlew assembleDebug testDebugUnitTest --tests dev.mobilecodex.app.AntigravityEngineTest
python -m unittest discover -s tests -p test_agent_provider_runtime.py
```

GPL-3.0 라이선스를 따릅니다. 원본 Mobile Codex의 저작권 및 제3자 고지는 LICENSE와 THIRD_PARTY_NOTICES.md에 보존합니다.
