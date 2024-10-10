# 넥서스 프록시 복사용 유틸

## 1. 개요

Proxy 로 연결된 Nexus 서버에서 파일을 요청하여 원본 Nexus 의 파일을 강제로 캐시하도록 하는 util

예 : Nexus_1 의 proxy 저장소인 Nexus_2 에 Nexus_1의 파일들을 강제 요청하여 Nexus_2 에 캐시 저장하도록 만든다.

```mermaid
stateDiagram-V2
direction TB
Nexus_2 --> Nexus_1:proxy 복제
note left of Nexus_1 8081번 포트로 실행 end note
note left of Nexus_2 8082번 포트로 실행 end note
Nexus_Proxy_Attach --> Nexus_2:Nexus_1 의 파일들을 요청
Nexus_Proxy_Attach --> Nexus_1:파일목록 요청
```

## 2. 준비사항

두대의 Nexus를 준비한다. (원본 Nexus : `192.168.0.2:8081`, 사본 Nexus : `192.168.0.2:8082`)

원본 Nexus 의 `Repository` 에 패키지들을 업로드 또는 캐시해서 준비해두고 (예 : `test-maven-repo`)

사본 Nexus 에 새로운 Proxy-Repository 를 만든다 (예: `test-maven-proxy`)

## 3. 실행

```bash
java -jar nexus-proxy-attach {원본 Nexus주소} {원본 NexusRepository명} {사본 Nexus주소} {사본 NexusRepository명}

## 예제
java -jar nexus-proxy-attach http://192.168.0.2:8081 test-maven-repo http://192.168.0.2:8082 test2-maven-proxy
```

## 4. 결과

원본 Nexus 저장소(`http://192.168.0.2:8081/repository/test-maven-repo/`) 의 내용이 사본 Nexus 저장소(`http://192.168.0.2:8082/repository/test-maven-proxy/`) 에 캐시되어 저장된다


## 5. 기타

빌드방법 : `gradlew build`