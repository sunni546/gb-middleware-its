# gb-middleware-its

## 미들웨어

### 목적
- 엣지가 직접 VoltDB에서 엣지 정보, 신호를 조회하는 기능을 이 프로그램이 대체하여 VoltDB의 부하 경감하고자 함.
- batch insert를 적용해 DB부하 경감 및 I/O 대기시간을 줄이고자 함.

### 기존 2K 엣지 -> VoltDB 조회 부분(4K는 파악 안함)
1. VoltDB 150번(신호 DB)에서 soitdspotintsstts 테이블, SPOT_INTS_ID 기준으로 CLCT_UNIX_TM, A_RING_MVMT_NO, B_RING_MVMT_NO을 조회
   - 매초마다 조회
2. VoltDB 149번(Volt Main)에서 SPOTGCAMRINFO 테이블, ip 기준으로 SPOT_CAMR_ID 조회
3. VoltDB 149번(Volt Main)에서 SPOTGCAMRINFO 테이블, ip 기준으로 CAMR_2K_IP, EDGE_SYS_2K_IP, CAMR_4K_IP, EDGE_SYS_4K_IP 조회
4. VoltDB 149번(Volt Main)에서 SPOTGCAMRINFO 테이블, ip 기준으로 SPOT_INTS_ID,SPOT_CAMR_ID,ECU_INFO_TRSM_YN,INSTL_LOCN_NO 조회

### 신규 개발한 현상태 정보

- 엣지 <-> 서버 통신 프로토콜 : gRPC ** 현재 grpc로 통신하지만 redis, restapi로 통신하도록 바꿔도 큰 변경이 필요하지 않음
- 언어 : Java 21 (25.09 java 25가 나왔는데 배포 전이라면 버전업 추천)
- 프레임워크 : org.springframework.grpc:spring-grpc-spring-boot-starter

### VoltDB 조회 기능 구현

1. 신호 정보 조회(middleware/trafficsignal/)
   - 엣지에서 서버에 신호정보 구독을 요청하면 stream 연결 후 연결정보 저장 및 현재 신호 전송
   - 신호 테이블(soitdspotintsstts) 전체를 polling 하여 HashMap에 저장
   - 이전에 저장된 정보와 비교해 변경이 있는 교차로에 변경 정보 EventPublisher로 gRPC 계층으로 전달
   - gRPC 계층에서 신호가 변경된 교차로 중 현재 gRPC 연결이 존재하는 교차로에만 정보 전송

2. 카메라(엣지) 정보 조회(middleware/edge/selfinfo/)
   - 서버 로딩시 SOITGCAMRINFO 테이블 전체 HashMap에 저장 
   - 요청시 연결 유지하지 않고 unary로 응답

### 추후 조회 기능을 운영에 사용하게 될 시 개발 방향
1. 이 프로젝트를 그대로 활용하기에는 엣지 개발의 부담이 큼
   - 기존 신호 정보, 카메라 정보(일부)는 엣지의 C++ 코드에서 조회하였음
   - C++ 버전이 낮아 protobuf 빌드가 불가능함
   - Python 코드에서 조회 후 Redis를 통해 C++로 전달해야함

2. 현 프로젝의 gRPC 계층만 엣지 내의 Redis에 Push하는 형태로 변경 권장
   - 사내에서 개발한 모든 엣지에는 C++과 Python의 데이터 교환을 위해 Redis 사용중
   - 따라서 신호 변경이 있을때 해당 교차로의 엣지들의 Redis에 연결해 정보를 입력 후 연결 종료
   - 엣지는 별도의 요청없이 Redis의 Hash를 읽어 검지에 활용 가능

### 데이터 삽입 기능 구현(middleware/edge/generated/)

1. 엣지에서 VoltDB로 전송하던 13개의 테이블에 대한 데이터를 피나클에서 개발한 미들웨어의 protobuf 인터페이스를 그대로 구현함(기존 엣지 코드 변경 필요 없음)
2. 데이터 수신시 타입별로 대기큐에 넣고 설정값(최대 대기시간, 최대 대기열 크기)에 따라 쌓인 데이터를 한번에 비워서 DB에 삽입 쿼리 실행


#### application-strategy.yml(middleware/edge/generated/config/DataSaveConfig.java)

1. 대기 큐의 최대 대기 시간, 최대 배치 크기, DB별 쿼리, 테이블명 저장
2. DB별 쿼리 유의사항
    * 기존 C# 미들웨어는 모두 upsert 쿼리 였으나 DB 부하 경감을 위해 pk 충돌 안날때만 insert로 변경함
    * tibero는 where not exist, mariadb는 insert ignore로 구현함
    * voltdb는 그런 기능을 지원하지 않아 그대로 upsert문으로 처리함
    * soitgunacevet 테이블의 경우 C# 버전에서는 시작, 진행, 종료 쿼리를 분리 했으나, 관리용이성을 위해 하나의 쿼리로 null이 아닌 것만 update 하도록 함
    * voltdb는 그런 기능을 지원하지 않아 마지막 값만 남게 되어있는 오류가 있음(voltdb 아무도 안보니까 은폐하거나 중간에 레이어 하나 추가해서 코드에서 처리하거나, 기존처럼 3개로 나누는 방법이 있음.)
    * 모든 개별쿼리는 테스트 해보지 않았음. soitgunacevet, 함체상태, 카메라 상태 테이블 외에는 모두 1회 실행시 삽입, 재실행시 Affected Rows 0 warn 로그가 찍혀야함.
    * 각 테이블별 최대 대기 시간, 최대 배치 크기는 모니터링하면서 최적화가 필요함

#### 메트릭(edge/generated/metric/aop/EdgeDataSaveMetricAspect.java)

1. 큐 드레인시 갯수, db별 쿼리 실행시 실행시간과 쿼리 갯수를 AOP를 이용해 메트릭을 전송하도록 함
2. 큐 드레인 갯수를 전송하면 전송 시간정보는 알 수 있기에 별도로 전송 안함
3. 큐를 드레인한 정보랑 db별 쿼리 실행 시간과 쿼리 갯수를 활용하면 엣지 데이터 수신 ~ db 저장까지의 지연시간을 grafana에 시각화 할 수 있음

#### envoy를 이용한 로드밸런싱 구현시 유의사항(미구현)

1. 무중단이 데이터 수신이 목적이므로 데이터 유실이 없는 설계가 필요함(예시)
   * kill <pid> 종료 신호 감지 -> 헬스체크 NOT_SERVING 변경 -> 연결중인 stream에서 이미 받은 요청까지만 처리 -> 연결중인 stream 끊기 -> 실행중인 쿼리 종료 대기 -> 완전 종료 
2. 1번이 충족된다면 하나의 인스턴스에 모든 요청이 몰린 후 다른 인스턴스가 실행되었을때 다시 로드밸런싱이 필요함
   * 연결 최대 시간 지정
   
---

## 로드밸런싱 : Envoy v1.29+
HTTP/2 기반 gRPC 요청을 받아서 이중화 된 미들웨어 서버로 로드밸런싱

### 아키텍처
                        ┌────────────────────┐
                        │     gRPC Client    │
                        └─────────┬──────────┘
                                  │
                          gRPC Request (HTTP/2)
                                  │
                            Port: 50050
                         ┌────────▼────────┐
                         │   Envoy Proxy   │  ◀── docker-compose 로 실행
                         └────────┬────────┘
                                  │
                   ┌──────────────┴──────────────┐
                   │                             │
           ┌───────▼───────┐             ┌───────▼───────┐
           │ gRPC Server A │             │ gRPC Server B │
           │ 192.168.6.151 │             │ 192.168.6.153 │
           └───────────────┘             └───────────────┘
              Port: 50051                   Port: 50051

### 운영 특성
- 무중단 배포: 서버 교체 중에도 요청은 살아있는 인스턴스로 분산
- 헬스 체크 연동: 문제가 있는 인스턴스는 자동으로 제외됨
- Stateless 설계: 세션 공유 불필요 (필요시 Redis 등 별도 구성)
