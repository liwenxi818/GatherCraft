# GatherCraft Changelog

## [1.7.0] - 2026-08-10

### 버그 수정 (노션 피드백 반영)
- **[낮음] MINING_SPEED/LUMBERJACK_SPEED 표시 텍스트 혼동 수정**: `incrementText`가 "+3%"로 표시되어 실제로는 Haste 부여 방식임에도 % 증가처럼 오인되던 문제. "6회마다 성급함 +1"로 변경, `description`도 "(현재 N/6)" 형태의 사이클 진행도 표시 추가(`SkillPointScreen`이 현재 누적값 기준으로 동적 계산해 `{n}` 플레이스홀더 치환)
- **[높음] 오버밸런스 너프**: 스탯 포인트 3개(0.09)마다 Haste +1이 부여되어 포인트당 실질 채굴 속도 상승이 의도치(3%/포인트)보다 월등히 높던 문제. 임계값을 6포인트(0.18)마다 +1로 조정
- **[높음] 레벨 기반 + 스탯 기반 Haste 합산 버그 수정**: 기존에는 레벨 기반 상시 Haste와 스탯 기반 Haste가 별개로 부여되어 낮은 쪽이 높은 쪽에 덮어씌워지던 문제(예: 레벨 Haste II + 스탯 Haste III → III만 적용, V 기대). 이제 `applyMiningHaste`/`applyLumberjackHaste` 내부에서 레벨 amplifier + 스탯 amplifier를 합산한 뒤 최대 Haste V로 캡
- **[치명] 채굴/벌목 Haste 크로스 버그 수정**: MINING_SPEED 스탯으로 얻은 Haste가 벌목 중에도 그대로 적용되어(반대도 동일) LUMBERJACK_SPEED에 투자할 이유가 사라지던 문제. `PlayerTickHandler`에 `isLookingAtOre()`/`isLookingAtLog()`(5블록 레이캐스트 + `BlockTags.MINEABLE_WITH_PICKAXE`/`MINEABLE_WITH_AXE`)를 추가해, 스탯 기반 보너스는 해당 활동에 맞는 블록을 바라볼 때만 적용되도록 분리(레벨 기반 baseline은 기존과 동일하게 유지)

## [1.6.9] - 2026-08-10

### 기능 개선
- **레벨업 스탯 포인트 선택지에 설명 추가**: `SkillPointStat` enum에 `description` 필드(36개 스탯 전체 한국어 설명) 및 `getDescription()` getter 추가
  - `SkillPointScreen` 선택지 버튼을 2줄(이름/증가량) → 3줄(이름/설명/증가량) 구조로 개편, 설명 텍스트는 `PoseStack` 스케일(0.75x)로 축소 렌더링
  - 버튼 높이 `BTN_H` 38→46, 팝업 높이 `POPUP_H` 230→250으로 확대해 3줄 레이아웃 수용
  - 증가량 텍스트 색상을 초록(`§a`)에서 노랑(`§e`)으로 변경
  - 호버 툴팁(`renderStatTooltip`)에도 설명 한 줄 추가

## [1.6.8] - 2026-08-09

### 버그 수정
- **연쇄 벌목(100레벨 각성) 재진입 가드 추가**: `LumberjackHandler.triggerChainFelling()`에 `MiningHandler`의 `IS_AREA_MINING`과 동일한 `ThreadLocal<Boolean>` 재진입 방지 패턴(`IS_CHAIN_FELLING`)을 적용 — `world.destroyBlock()` 호출로 `BlockEvent.BreakEvent`가 재발생하더라도 `onBlockBreak()`가 즉시 반환되어 로그 1개당 XP/퀘스트/업적/드롭이 중복 지급될 가능성을 원천 차단 (v1.6.7에서 "미해결 확인 사항"으로 남았던 항목의 후속 조치)

### 테스트
- `/gathercraft test auto`에 코드 레벨 검증 10개 항목 추가
  - 연쇄 벌목 재진입 가드 존재 여부(리플렉션, `IS_CHAIN_FELLING` 필드 조회)
  - `AntiExploitManager` 클래스 존재 여부 + `BlockEvent.EntityPlaceEvent` 설치 감지 리스너 존재 여부(리플렉션)
  - 광석/원목 채굴 시 `AntiExploitManager.shouldGiveXP()` 호출 여부(소스 파일 텍스트 검사)
  - 5개 스킬(Mining/Farming/Fishing/Smithing/Enchanting)의 `SkillPointStat` 연동(`getStatValue()` 호출) 여부(소스 파일 텍스트 검사)
  - 소스 파일 검사 기반 항목은 개발 환경(`gradlew runServer`/`runClient`)에서만 동작하며, 배포된 서버(mods 폴더)에서는 스킵되고 pass/fail 카운트에 포함되지 않음
- 테스트 결과 요약 출력을 박스 형태로 개편 + 실패 항목을 마지막에 재출력

## [1.6.7] - 2026-08-08

### 버그 수정
- **섬손 재설치 XP 파밍 익스플로잇 수정**: 섬손으로 캔 광석/원목을 다시 설치한 뒤 재채굴하면 XP/퀘스트 진행도/업적/추가 드롭이 무한히 중복 지급되던 문제
  - `AntiExploitManager` 신설 — 플레이어가 설치한 블록 위치를 `Set<Long>`(LinkedHashSet, 최대 10만 개, 초과 시 최오래된 항목 자동 제거)으로 추적, 서버 런타임 동안만 유지(재시작 시 초기화 — 허용 범위)
  - `MiningHandler`에 `BlockEvent.EntityPlaceEvent` 리스너 신설: 광석/원목 설치 위치를 각각 기록
  - `MiningHandler`/`LumberjackHandler`의 `onBlockBreak()`에 `shouldGiveXP()` 체크 추가: 설치된 블록을 재채굴하면 XP를 포함한 모든 보상(퀘스트/업적/추가 드롭/파티클)을 스킵
  - 100레벨 각성 연쇄 채굴(`triggerAreaMining`)/연쇄 벌목(`triggerChainFelling`)도 동일 로직으로 방어 — 연쇄 파괴 대상에 설치된 블록이 있으면 건너뜀

## [1.6.6] - 2026-08-08

### 버그 수정
- **SkillPointStat 미적용 13개 스탯 전부 활성화**: `MINING_SPEED`, `MINING_XP_BONUS`, `LUMBERJACK_DURABILITY`, `LUMBERJACK_SPEED`, `FARMING_BONEMEAL`, `FARMING_GROWTH`, `FISHING_SPEED`, `COOKING_SATURATION`, `COOKING_EXTRA_BUFF`, `SMITHING_DURABILITY`, `ENCHANTING_COST_REDUCE`, `ENCHANTING_EXTRA`, `ENCHANTING_CURSE_IMMUNE` — NBT에는 저장되지만 실제 게임 로직에서 조회되지 않던 버그. 기존 레벨 기반 로직은 그대로 두고 `SkillData.getStatValue()` 값을 확률/배율/amplifier에 덧셈으로 반영하는 방식으로 구현 (포인트 미투자 시 기존 동작과 완전 동일, 하위 호환 보장)
  - `MiningHandler`/`PlayerTickHandler`/`LumberjackHandler`/`FarmingHandler`/`FishingHandler`/`CookingHandler`/`SmithingHandler`/`EnchantingHandler` 8개 파일 수정
  - `SkillPointStat.java`의 `†`(미구현) 표시 전부 제거

## [1.6.5] - 2026-07-30

### 버그 수정
- **낚시 속도 증가 버그 수정**: v1.6.3에서 잘못된 필드(`timeUntilHooked`)를 잘못된 시점(`EntityJoinLevelEvent`, 캐스팅 직후)에 건드려 항상 최솟값(20틱)으로 고정되던 문제. 실제 대기시간 필드는 `timeUntilLured`이며 낚시찌가 물에 착수한 뒤 첫 틱에야 랜덤 배정되므로, `TickEvent.PlayerTickEvent` + `player.fishing`으로 매 틱 감시하다가 값이 "증가"하는 순간(=새 대기 사이클이 막 시작된 순간)을 감지해 1회만 단축 적용하는 방식으로 재작성 (`FishingHandler`)
  - 실기 테스트에서 20/100레벨 체감 차이가 없던 증상이 이 버그로 확인됨

### 신규 기능
- **칭호 채팅 표시**: 착용 중인 칭호를 채팅 메시지 앞에도 표시 (`TitleChatHandler`, `ServerChatEvent` 취소 후 서명되지 않은 시스템 메시지로 재브로드캐스트)
  - 필드에서 다른 플레이어 이름표 위에 칭호를 표시하는 기능은 v1.5.1에 이미 구현되어 있었음(`TitleNameTagRenderer`)

## [1.6.4] - 2026-07-30

### 신규 기능 / 스펙 정합화
- **요리 다중 버프 시스템 재설계**: 50/80/100레벨의 확률 기반 "추가 버프"(15%/30%/50%)를 걷어내고, 원래 스펙대로 50레벨 2개·80레벨 3개 버프 동시 적용으로 재구현
  - `CookingHandler`에 음식 카테고리별 우선순위 버프 배열(`MEAT_BUFFS`/`POULTRY_BUFFS`/`FISH_BUFFS`/`STAPLE_BUFFS`/`DESSERT_BUFFS`) 신설, `buffCount(level)`(20→1개/50→2개/80→3개)만큼 순서대로 적용
  - **90레벨: 모든 음식 버프 강도 최대** 신규 구현 — `buffAmplifier()`가 90레벨 이상에서 amplifier 2(표시 "III") 고정
  - 100레벨 각성(체력 +4 회복 + 디버프 제거)과 역할이 겹치던 확률 시스템의 100레벨 티어는 삭제
- 문서화된 모든 스킬 TODO 항목 구현 완료 (CLAUDE.md `❌ 미구현 목록` 비움)

## [1.6.3] - 2026-07-30

### 버그 수정 / 스펙 정합화
- **농사 20레벨**: "뼛가루 1개로 2회 효과"를 정식 구현 (기존에는 40/70/90레벨 즉시 완숙으로만 대체되어 있었음)
  - `FarmingHandler.onBonemeal()`이 20~39레벨 구간에서 `CropBlock.performBonemeal()`을 동일 뼛가루 소비로 2회 연속 호출하도록 분기 추가, 40레벨 이상은 기존 확률 기반 즉시 완숙 로직 유지
- **낚시 속도 증가 레벨 수정**: 30/60/90레벨(20%/40%/65%) → 스펙대로 20/40/70레벨(10%/25%/50%)로 정정
  - `FishingHandler`의 `timeUntilHooked` 리플렉션 필드 조회를 이벤트마다 반복하던 것을 정적 캐시 + `reduceHookTime()` 헬퍼로 리팩토링 (기존 두 곳에 중복돼 있던 try/catch 블록 통합)
- **요리 포화도 증가 레벨/방식 수정**: 30/60/90레벨 고정값(+0.4/+1.0/+2.0) → 스펙대로 40/70레벨, `FoodProperties` 기반 실제 포화도의 20%/50% 증가로 재구현
- **ItemSmeltedEvent 서버 발동 검증**: Forge 소스(`FurnaceResultSlot.java.patch`) 확인 결과 컨테이너 슬롯 로직은 전적으로 서버 사이드에서 처리되므로 데디케이티드 서버에서도 정상 발동함을 코드 레벨로 확인. 단, 호퍼 자동 추출 시에는 발동하지 않는 한계를 문서화

## [1.6.2] - 2026-07-09

### 신규 기능
- **업적 보상 수령 시스템** (퀘스트와 동일한 "해금 → 수동 수령" 2단계 구조로 전환)
  - `AchievementManager.unlock()`은 이제 해금 기록 + 서버 공지 + 클릭형 채팅(`/gathercraft achievement claim <id>`)만 수행, 보상 지급은 신규 `claim()`으로 완전히 분리
  - `Achievement` record에 `condition`(조건 설명) 필드 추가, `isClaimed()`/`getCondition()`/`getGoal()`/`getCounterKey()`/`getRewardText()` 신규 API 추가
  - NBT `ach_claimed_[ID]` 신설(수령 여부), `/gathercraft test reset` 시 `ach_` 프리픽스 전체 정리(해금/수령/카운터 모두 초기화) + 클라이언트 재동기화
  - `AchievementClaimPacket`(C2S, ID 21) 신규, `AchievementSyncPacket`에 `claimed` 리스트 필드 추가
- **업적 탭 UI 전면 개선** (`SkillBookScreen`)
  - 4가지 상태(달성+수령완료 / 달성+미수령 / 미달성+진행도 / 미달성+비공개)를 2줄 레이아웃으로 표시
  - 미달성 업적에 10칸 진행도 바(`progressBar()`) 표시, 달성 시 조건/보상 설명과 함께 hover 툴팁 제공
  - 미수령 업적에 `[보상 수령]` 버튼 추가 — 클릭 시 `AchievementClaimPacket` 전송
  - 기존 스크롤/스크롤바/클리핑 인프라(v1.6.1) 그대로 재사용, 행 높이만 22px로 확장

## [1.6.1] - 2026-07-09

### 신규 기능
- **스킬 책 GUI 마우스 휠 스크롤** (`SkillBookScreen`)
  - 업적 탭(5): 카테고리 구분선 + 업적 15종을 하나의 행 목록으로 평탄화(`buildAchievementRows()`)해 한 번에 8행만 표시, 휠로 스크롤
  - 칭호 탭(2): 기존 2컬럼 레이아웃을 유지한 채(컬럼당 `totalRows = ceil(17/2)`) 8행 단위로 스크롤 지원 — 칭호가 늘어나도 스크롤 없이 잘리던 문제 방지
  - `GuiGraphics.enableScissor/disableScissor`로 두 탭 모두 콘텐츠 영역 밖 렌더링 클리핑
  - 공용 `renderScrollbar()` 헬퍼로 두 탭에 동일한 스크롤바(트랙+핸들) 렌더링
  - 탭 전환 시 `titleScrollOffset`/`achievementScrollOffset` 항상 0으로 초기화

## [1.6.0] - 2026-07-09

### 신규 기능
- **일일 퀘스트 시스템** (`quest` 패키지)
  - `QuestPool`: 쉬움/보통/어려움 각 8종, 총 24종 풀
  - `QuestManager`: 날짜(yyyyMMdd) 기반 자동 갱신, 진행도 적립(`progress()`), 클릭 가능한 완료 알림, 보상 수령(`claim()` — 스킬 XP + 경험치 병 아이템 드롭)
  - 신규 패킷 3개: `QuestSyncPacket`(S2C, ID 17), `OpenQuestBoardPacket`(S2C, ID 18), `QuestClaimPacket`(C2S, ID 20)
  - 기존 6개 스킬 핸들러(채광/사냥/농사/낚시/요리/벌목)에 진행도 적립 훅 추가
- **업적 시스템** (`achievement` 패키지, 15종)
  - `AchievementManager`: 채광/사냥/생활 각 4종 + 종합 3종, 서버 전체 공지(`broadcastSystemMessage`), 보상 지급(관련 스킬 XP 또는 전 스킬 균등 분배 + 경험치 병)
  - 신규 패킷: `AchievementSyncPacket`(S2C, ID 19)
  - `SkillManager.onLevelUp()`에 전 스킬 레벨 기반 종합 업적 체크 + 채광/사냥 100레벨 각성 업적 연동
- **퀘스트 게시판 블록** (`block/QuestBoardBlock.java`)
  - 이 모드 최초의 커스텀 블록 (`bookshelf` 텍스처, `cube_all` 모델)
  - 우클릭 시 오늘의 퀘스트 갱신 확인 + 스킬 책 GUI 퀘스트 탭 자동 오픈
  - `/gathercraft giveboard` 명령어(OP 2)로 지급, `/gathercraft quest claim <0|1|2>`는 권한 제한 없음
- **스킬 책 GUI 6탭 확장** (`SkillBookScreen`)
  - 기존 4탭에 [퀘스트]/[업적] 탭 추가, `SkillBookScreen(int initialTab)` 생성자 오버로드로 특정 탭 오픈 지원

## [1.5.1] - 2026-07-09

### 신규 기능
- **칭호 이름표 표시**: 착용 중인 칭호를 주변 플레이어의 화면에 이름표 위 텍스트로 렌더링 (`client/overlay/TitleNameTagRenderer.java`, `RenderNameTagEvent` 기반)
  - `title/TitleNameTagCache.java`: 클라이언트 측 UUID→칭호ID 캐시
  - 신규 패킷 `TitleBroadcastPacket`(S2C, ID 16): 칭호 착용/해제 시 주변 64블록 플레이어에게 브로드캐스트, 로그인 시 양방향 교환
- **칭호 보유 패시브 효과** (착용 여부 무관, 해금만 해도 적용):
  - `miner_3`(채광 100레벨): 채광 XP +10%
  - `hunter_3`(사냥 100레벨): 공격력 +5%
  - `all_50`(모험가): 전 스킬 XP +5%
  - `all_100`(각성왕): 전 스킬 XP +15% + 이동속도 Speed I 상시 적용
  - `TitleManager.hasTitle()`/`getXPMultiplier()` 추가, `SkillManager.addXP()`에서 전역 배율 적용

### 버그 수정
- **`/gathercraft test reset` 시 칭호 미초기화 수정**: `TitleManager.checkAndUnlock()`은 해금 목록을 추가만 하고 제거하지 않으므로, 레벨을 0으로 되돌려도 이미 해금된 칭호가 NBT에 남아있던 문제를 `resetAll()`에서 `unlocked_titles`/`equipped_title` NBT 키를 명시적으로 제거하도록 수정

## [1.5.0] - 2026-07-09

### 신규 기능
- **칭호 시스템** (`title` 패키지)
  - `Title` enum: 칭호 17종 정의 (채광/사냥/농사/낚시/요리/대장장이/마법부여 각 스킬 레벨 조건 + 전 스킬 50/100레벨 조건)
  - `TitleManager`: 자동 해금(`checkAndUnlock`, 레벨업 및 로그인 시 호출) + 착용/해제 토글(`equip`), NBT는 `SkillData.getRoot()` 하위 `"unlocked_titles"`/`"equipped_title"` 키
  - 신규 패킷 2개: `TitleSyncPacket`(S2C, ID 11), `TitleEquipPacket`(C2S, ID 12)
- **TPA 텔레포트 시스템** (`tpa` 패키지)
  - `TpaManager`: 요청/수락/거절, 60초 쿨다운·60초 요청 만료, 차원 간 텔포 지원(같은 차원/다른 차원 모두 처리), 로그아웃 시 대기 요청 정리
  - 신규 패킷 3개: `TpaRequestPacket`(C2S, ID 13), `TpaResponsePacket`(C2S, ID 14), `TpaAskPacket`(S2C, ID 15)
  - `/tpaccept`, `/tpdeny` 명령어 (`TpaCommand`) + 클릭 가능한 채팅 메시지(`ClickEvent`) + 자동 팝업 GUI(`TpaRequestScreen`) 이중 UX
- **스킬 책 GUI 4탭 확장** (`SkillBookScreen`)
  - 기존 2탭([스킬 현황]/[웨이포인트])에 [칭호]/[텔포] 탭 추가
  - 칭호 탭: 2컬럼×9행 레이아웃으로 17종 전부 스크롤 없이 표시, 미해금 항목은 조건 텍스트와 함께 회색 처리
  - 텔포 탭: 온라인 플레이어 목록(자기 자신 제외, 최대 8명 표시) + 텔포 요청 버튼
  - 탭 렌더링/클릭 처리를 `if/else` 이진 분기에서 `switch` 문으로 전환하여 탭 확장에 대응

## [1.4.0] - 2026-07-08

### 신규 기능
- **웨이포인트 시스템** (`waypoint` 패키지)
  - `WaypointData`/`WaypointManager`: 플레이어당 최대 10개, NBT ListTag로 저장 (`SkillData.getRoot()` 하위 `"waypoints"` 키)
  - 스킬 책 GUI에 [스킬 현황]/[웨이포인트] 2탭 구조 추가 (`SkillBookScreen`)
  - 웨이포인트 탭: 이름 입력(EditBox), 아이콘 5종(home/village/end/nether/custom) 선택, 현재 위치 저장, 목록에서 이동/삭제
  - 차원 간 텔레포트 지원 (오버월드/네더/엔드), 도착 시 PORTAL 파티클 + 채팅 안내
  - 신규 패킷 4개: `WaypointSavePacket`(C2S, ID 7), `WaypointDeletePacket`(C2S, ID 8), `WaypointTeleportPacket`(C2S, ID 9), `WaypointSyncPacket`(S2C, ID 10)
  - 로그인 시 `PlayerTickHandler.onPlayerLogin`에서 웨이포인트 목록 자동 동기화

## [1.3.0] - 2026-06-04

### 버그 수정
- **IsDashing 상태 고착 버그 수정** (`PlayerTickHandler`)
  - 서버 재시작 후 로그인 시 `IsDashing=false`, `DashTicksLeft=0` 강제 초기화
  - 로그아웃 시 `dashingPlayers` Set 클린업 추가
- **사냥 100레벨 즉사 공격 데미지 텍스트 미표시 수정** (`HuntingHandler`)
  - `kill()` 전에 현재 HP를 데미지값으로 `DamageTextPacket` 전송
- **채광 100레벨 각성 연쇄 이벤트 버그 수정** (`MiningHandler`)
  - `triggerAreaMining()` 내 `destroyBlock()`이 `BlockEvent.BreakEvent`를 재발화해 각성이 연쇄 트리거되던 문제 수정
  - `ThreadLocal<Boolean> IS_AREA_MINING` 플래그로 재진입 차단

### 성능 최적화
- **PlayerTickHandler NBT 읽기 최적화**
  - 대시 중이 아닌 틱에서 `SkillData.getRoot()` 호출 제거
  - `DashManager.dashingPlayers` (`Set<UUID>`, ConcurrentHashMap 기반)로 대시 상태 메모리 캐싱

### UI 개선
- **SkillBarOverlay 대시 키 레이블 동적화**
  - 기존 하드코딩 `"R"` → `KeyMapping.getKey().getDisplayName()` 실시간 반영
  - Controls 메뉴에서 키 재설정 시 UI 레이블 즉시 업데이트

### 코드 품질
- `PlayerTickHandler.applyDefenseAttributes` → `private static` 변환 (불필요한 인스턴스 생성 제거)
- `SkillXpBarOverlay.blendColor` 중복 메서드 제거 (`lerpColor` 직접 호출로 통합)
- `SkillManager.onLevelUp` `instanceof ServerLevel` 불필요한 null 체크 제거 (직접 캐스팅)

---

## [1.2.0] - 2026-06-04

### XP 밸런스
- `xpToNextLevel` 3단계 티어 공식으로 변경 (기존: `(level+1) × 100`)
  - Lv 0~19: `(level+1) × 20`
  - Lv 20~59: `(level+1) × 50`
  - Lv 60~99: `(level+1) × 120`
- 요리 XP: 제련 결과물 스택 수 반영 (`5 × max(1, count)`)

### 버그 수정
- **재접속 시 스킬 데이터 초기화 버그 수정**
  - `PlayerEvent.Clone` 핸들러가 사망 리스폰만 처리하고 엔더 드래곤 클리어(`isWasDeath=false`) 케이스를 스킵해 데이터가 소실되던 문제 수정
  - `onPlayerClone`에서 `isWasDeath()` 조건 제거 → 두 케이스 모두 GatherCraft 데이터 복사
- 로그인 시 클라이언트 XP 바 동기화 누락 수정 (`SkillXpUpdatePacket` 전송 추가)
- `onPlayerLogout` / `onPlayerRespawn` 이벤트 핸들러 신규 추가

### 광물/몹 희귀도 XP
- 채광: 광석 희귀도별 XP 차등 지급 (`getOreXP`)
  - 네더라이트 스크랩 100 / 다이아·에메랄드 60 / 금·청금석 30 / 레드스톤·철 20 / 기타 10
- 사냥: 몹 등급별 XP 차등 지급 (`getMobXP`)
  - Wither·엔더드래곤 500 / 엘더가디언·워든 200 / 블레이즈·가스트·엔더맨·셜커 60 / 일반몹 20 / 기타 10

### 신규 기능 구현
- **사냥 (Hunting)**
  - 3단계 체력 회복: 20레벨 10%/1HP, 50레벨 25%/2HP, 80레벨 40%/4HP + HEART 파티클
  - 희귀 드롭 시스템: 보스→NETHER_STAR(확정), 워든→ECHO_SHARD, 블레이즈→BLAZE_ROD×2, 엔더맨→ENDER_PEARL×2, 일반→NAME_TAG·SADDLE·EMERALD 랜덤 (40레벨 5%, 70레벨 15%)
- **요리 (Cooking)**
  - 포화도 증가: 30레벨 +10%, 60레벨 +25%, 90레벨 +50%
  - 추가 버프 확률: 50레벨 15%, 80레벨 30%, 100레벨 50% (고기→DAMAGE_BOOST, 생선→WATER_BREATHING, 기타→REGENERATION)
- **농사 (Farming)**
  - 뼛가루 사용 시 즉시 완숙: 40레벨 25%, 70레벨 50%, 90레벨 80%
  - 자연 성장 시 추가 성장 트리거: 50레벨 20%, 80레벨 40%
- **낚시 (Fishing)**
  - 낚시 속도 단축: 30레벨 20%, 60레벨 40%, 90레벨 65% (리플렉션으로 `timeUntilHooked` 감소)
- **마법부여 (Enchanting)**
  - XP 비용 환급: 40레벨 10%, 70레벨 25%, 90레벨 40%
  - 저주 인챈트 자동 제거: 50레벨 30%, 80레벨 60%, 100레벨 확정
  - 추가 인챈트 부여: 60레벨 10%, 80레벨 20%, 100레벨 35%
- **데이터 저장 (`SkillData`)**
  - `loadFromNBT(Player)` / `saveToNBT(Player)` 메서드 추가

---

## [1.1.0]
- 부유 전투 텍스트 (DamageTextPacket, FloatingCombatText)
- 크리티컬 강조, 2.5초 페이드 아웃

## [1.0.0]
- 9개 스킬 핸들러, NBT 저장, 레벨업 파티클, /skill 명령어
- 스킬 포인트 시스템 (레벨업 시 스탯 선택 팝업)
- 대시 스킬 (R키, DashManager, 쿨타임 UI)
- 스킬 책 아이템/GUI, XP 바 오버레이
