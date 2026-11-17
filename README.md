# Mobile-agy

Antigravity 전용 Android 클라이언트입니다. Mobile Codex의 Android 앱과 앱 내 Linux·CLI 연결 코드를 기반으로 합니다.

- 앱 ID `dev.mobileagy.app`, 초기 버전 `0.1.0`
- Google 계정으로 Antigravity CLI에 로그인
- 계정에서 조회한 모델 선택, 응답 스트리밍, 중지, 저장된 대화 재개
- 프로젝트, 파일 탐색, 터미널과 휴대폰 도구를 기존 Android 기반에서 재사용
- 프로젝트 작업은 Antigravity 참가자 설정의 권한과 앱의 승인 확인을 적용

## 시작하기

ARM64 Android 10 이상에서 APK를 설치합니다. 계정 설정에서 앱 내 Linux 환경을 설치·활성화한 뒤 Antigravity CLI 설치와 로그인을 진행합니다. 로그인 완료 후 준비 상태를 새로고침하면 모델을 선택할 수 있습니다.

## 검증 범위

초기 버전은 로컬 APK 빌드, Antigravity 단독 전송·스트리밍·중지·재개 회귀 테스트, 공급자 런타임 테스트를 기준으로 검증합니다. 실기기 Google 로그인과 실제 모델 호출은 별도 확인이 필요합니다. 채팅 첨부는 아직 Antigravity에 연결하지 않았으며 프로젝트 파일 경로를 통해 파일을 다룹니다.

기존 Codex 전용 기능과 테스트 일부가 소스에 남아 있습니다. `AntigravityEngineTest`가 이 앱의 기본 전송 경로를 검증합니다. 이전 프로젝트의 자동 배포 작업은 수동 실행으로 전환했습니다.

## 빌드

`tools/prepare_runtime.py`, `tools/prepare_devtools.py`, `tools/build_native.py`로 Android 런타임을 준비하고 Android SDK와 JDK 17을 설정합니다.

```sh
./gradlew assembleDebug testDebugUnitTest --tests dev.mobilecodex.app.AntigravityEngineTest
python -m unittest discover -s tests -p test_agent_provider_runtime.py
```

GPL-3.0 라이선스를 따릅니다. 원본 Mobile Codex의 저작권 및 제3자 고지는 LICENSE와 THIRD_PARTY_NOTICES.md에 보존합니다.
