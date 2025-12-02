# MARU_CAP

MARU_CAP은 PG 거래 캡처와 정산 처리를 담당하는 Java 백그라운드 서비스입니다. `CaptureDaemon`이 주기적으로 신규 거래/환불을 조회해 캡처 데이터를 생성하고, 정산 정보와 리스크 데이터를 함께 적재합니다.

## 프로젝트 구성
- `src/` – `com.pgmate.cap` 네임스페이스의 주요 서비스, DAO, 훅 클래스.
- `bin/` – Ant 빌드 스크립트(`build.xml`)와 데몬 실행/정지/모니터링 스크립트.
- `conf/` – 로그백 설정(`logback.xml`)과 DB 연결 설정(`service.json`).
- `lib/` – 빌드 및 실행 시 참조하는 외부 라이브러리 JAR 파일 위치.

## 빌드 방법
프로젝트는 Ant 빌드 스크립트를 사용합니다. JDK와 Ant가 설치된 환경에서 아래 명령으로 클래스 파일을 컴파일하고 JAR을 생성할 수 있습니다.

```bash
ant -f bin/build.xml
```

빌드가 성공하면 `lib/MARU_cap.jar` 파일이 생성되며, 실행 스크립트가 이를 참조합니다.

## 실행 방법
Unix 계열 환경에서는 `bin/start.sh`로 데몬을 실행하고, `bin/stop.sh`로 중지합니다. Windows 환경에서는 `bin/start.bat`을 사용하세요.

```bash
cd bin
./start.sh    # 데몬 시작
./stop.sh     # 데몬 중지
```

데몬 프로세스는 `bin/daemon.pid`에 기록되며, `bin/monitor.sh`를 주기적으로 실행하면 프로세스가 중단된 경우 자동으로 재시작합니다.

## 환경 설정
`conf/service.json`에서 데이터베이스 연결 정보와 풀 설정을 관리합니다. 운영 환경에 맞게 접속 정보와 풀 크기를 조정한 후 데몬을 실행하세요. 로그 설정은 `conf/logback.xml`에서 수정할 수 있습니다.
