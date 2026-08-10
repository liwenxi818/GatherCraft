# 프로젝트 기본 정보
- Minecraft Forge 1.20.1
- Java 17, Gradle
- 모드 이름: 모여라 마크의 숲
- 영문명: GatherCraft
- 모드 ID: gathercraft
- 패키지: com.gathercraft.gathercraft

# 목표
- 바닐라 기반 9가지 스킬 레벨 RPG 모드
- 커스텀 몬스터/아이템 없음, 성능 최적화 우선

# 스킬 시스템 공통
- 레벨 범위: 1~100
- 10레벨마다 티어 업 + 특별 보너스 해금
- 티어: 입문→견습→숙련→전문→장인→명인→전설→불멸→신화→각성
- 스킬 데이터는 플레이어 NBT에 저장 (`getPersistentData()` → `"GatherCraft"` compound)
- /skill 명령어로 현재 스킬 레벨 확인 가능

---

# 구현 범례
- ✅ 구현 완료
- ❌ 미구현
- ⚠️ 원래 스펙과 다른 방식으로 구현됨

---

# 🌿 생활 스킬

## 채광 (Mining)
- ✅ 경험치: 광석 희귀도별 차등 XP — 네더라이트 100 / 다이아·에메랄드 60 / 금·청금석 30 / 레드스톤·철 20 / 기타 10
- ✅ 10레벨: 추가 드롭 확률 5%
- ✅ 20레벨: Haste I 상시 적용
- ✅ 30레벨: 추가 드롭 확률 15%
- ✅ 40레벨: Haste II 상시 적용
- ✅ 50레벨: 희귀 광석(다이아, 에메랄드) 추가 드롭 20%
- ✅ 60레벨: 추가 드롭 확률 30%
- ✅ 70레벨: 광석 채굴 시 경험치 오브 추가 드롭
- ✅ 80레벨: Haste III 상시 적용
- ✅ 90레벨: 추가 드롭 확률 50%
- ✅ 100레벨 각성: 채굴 시 15% 확률로 주변 3x3 범위 광석 동시 채굴

## 벌목 (Lumberjack)
- ✅ 경험치: 나무 원목 채굴 시 8XP
- ✅ 10레벨: 원목 추가 드롭 5%
- ✅ 20레벨: 도끼 내구도 소모 20% 감소 (TickEvent 기반 복원 방식)
- ✅ 30레벨: 나뭇잎에서 사과/묘목 드롭 확률 5% 증가
- ✅ 40레벨: 원목 추가 드롭 20%
- ✅ 50레벨: 나무 채굴 시 자동으로 묘목 심기 (8종 지원)
- ✅ 60레벨: 도끼 내구도 소모 50% 감소
- ✅ 70레벨: 원목 추가 드롭 35% / 나뭇잎 드롭 확률 10% 증가
- ✅ 80레벨: 나뭇잎 채굴 속도 대폭 증가 (도끼 지참 시 Haste III)
- ✅ 90레벨: 원목 추가 드롭 50%
- ✅ 100레벨 각성: 나무 1개 채굴 시 연결된 나무 전체 동시 채굴 (20% 확률, 최대 64블록)

## 농사 (Farming)
- ✅ 경험치: 완전히 자란 작물 수확 시 6XP
- ✅ 10레벨: 작물 추가 드롭 5%
- ✅ 20레벨: 뼛가루 1개로 2회 효과 (`CropBlock.performBonemeal()`를 동일 뼛가루 소비로 2회 연속 호출)
- ✅ 30레벨: 작물 추가 드롭 15%
- ✅ 40레벨: 씨앗 자동 재식
- ⚠️ 40/70/90레벨: 뼛가루 사용 시 즉시 완숙 (25%/50%/80%) — 원래 스펙(2회/3회 효과)과 다른 방식
- ✅ 50레벨: 희귀 작물 드롭 확률 (3%/8%/15%, SWEET_BERRIES·GLOW_BERRIES·NETHER_WART·CHORUS_FRUIT 중 랜덤)
- ⚠️ 50/80레벨: 자연 성장 시 추가 성장 트리거 (20%/40%) — CropGrowEvent.Pre 구현
- ✅ 60레벨: 작물 추가 드롭 30%
- ✅ 90레벨: 작물 추가 드롭 50%
- ✅ 100레벨 각성: 수확 시 25% 확률로 주변 5x5 범위 작물 동시 수확

## 낚시 (Fishing)
- ✅ 경험치: 낚시로 아이템 획득 시 15XP
- ✅ 10레벨: 쓰레기 드롭 확률 감소 (15%)
- ✅ 20레벨: 낚시 속도 10% 증가 (낚시찌 `timeUntilLured` 리플렉션 단축, TickEvent 기반 감지)
- ✅ 30레벨: 희귀 아이템 드롭 확률 증가 (junk 제거 누적)
- ✅ 40레벨: 낚시 속도 25% 증가
- ✅ 50레벨: 인챈트된 낚싯대 효과 강화 (Lure+LuckOfSea 1레벨당 10% 추가 대기시간 감소, 최대 50%)
- ✅ 60레벨: 물고기(COD) 추가 드롭 25%
- ✅ 70레벨: 낚시 속도 50% 증가
- ✅ 80레벨: 보물(BOOK) 드롭 확률 대폭 증가 (15%)
- ✅ 90레벨: 쓰레기 드롭 완전 제거
- ✅ 100레벨 각성: 5% 확률로 각성의 낚시대 획득 (LuckOfSea III + Lure III + Unbreaking III + Mending)

## 요리 (Cooking)
- ✅ 경험치: 화로/훈연기/용광로 제련 완료 시 `5 × 스택 수` XP — `FurnaceResultSlot`이 `ForgeEventFactory.firePlayerSmeltedEvent()`를 직접 호출하는 서버 사이드 컨테이너 로직이라 데디케이티드 서버에서도 정상 발동 확인(포지 소스 코드 검증). 단, 호퍼로 자동 추출 시에는 `Slot.onTake` 경로를 타지 않아 발동하지 않음(플레이어가 GUI에서 직접 꺼낼 때만 적립)
- ✅ 10레벨: 버프 지속시간 +10%
- ✅ 20레벨: 음식 카테고리별 고유 버프 1개 적용 (`buffsFor()` 우선순위 배열의 1번째 — 고기→힘, 가금류→속도, 생선→수중호흡, 감자/빵→흡수, 파이/케이크→재생)
- ✅ 30레벨: 버프 강도 증가 (60레벨 이상 amplifier +1, 90레벨부터 amplifier 최대 2로 대체)
- ✅ 40레벨: 음식 포화도 20% 증가 (`FoodProperties` 기반 실제 포화도의 20% 추가 지급)
- ✅ 50레벨: 음식 버프 2개 동시 적용
- ✅ 60레벨: 버프 지속시간 +50%
- ✅ 70레벨: 음식 포화도 50% 증가
- ✅ 80레벨: 음식 버프 3개 동시 적용
- ✅ 90레벨: 모든 음식 버프 강도 최대 (amplifier 2 = III로 고정)
- ✅ 100레벨 각성: 음식 섭취 시 체력 +4 즉시 회복 + 모든 디버프 제거

---

# ⚔️ 전투 스킬

## 사냥 (Hunting)
- ✅ 경험치: 몹 등급별 차등 XP — 보스(Wither/드래곤) 500 / 엘리트(엘더가디언/워든) 200 / 중간보스(블레이즈/가스트/엔더맨/셜커) 60 / 일반몹 20 / 기타 10
- ✅ 10레벨: 공격력 +5%
- ✅ 20레벨: 크리티컬 확률 +5% (낙하 크리티컬 없을 때만 발동)
- ⚠️ 20레벨: 처치 시 10% 확률 체력 +1 회복 — 원래 스펙(50레벨)보다 이른 레벨에 추가
- ✅ 30레벨: 공격력 +15%
- ✅ 40레벨: 낙하 크리티컬 데미지 +20%
- ⚠️ 40/70레벨: 희귀 드롭 시스템 (5%/15%) — 원래 스펙(70레벨 10%/뼈)을 등급별 아이템 시스템으로 교체
  - 보스 → NETHER_STAR (확정) / 워든 → ECHO_SHARD / 블레이즈 → BLAZE_ROD×2 / 엔더맨 → ENDER_PEARL×2 / 일반 → NAME_TAG·SADDLE·EMERALD 랜덤
- ✅ 50레벨: 처치 시 25% 확률 체력 +2 회복
- ✅ 60레벨: 공격력 +30%
- ✅ 80레벨: 크리티컬 확률 +20% (낙하 크리티컬 없을 때만 발동)
- ✅ 80레벨: 처치 시 40% 확률 체력 +4 회복
- ✅ 90레벨: 공격력 +50%
- ✅ 100레벨 각성: 5% 확률 즉사 공격
- **크리티컬 시스템**: 낙하 크리티컬(40레벨) OR 레벨 기반 크리티컬 — 둘 중 하나만 적용

## 방어 (Defense)
- ✅ 경험치: 몬스터에게 데미지 받을 시 2XP
- ✅ 10레벨: 데미지 감소 5%
- ✅ 20레벨: 넉백 저항 20% (속성, PlayerTickHandler)
- ✅ 30레벨: 데미지 감소 15%
- ✅ 40레벨: 체력 최대치 +4HP (속성, PlayerTickHandler)
- ✅ 50레벨: 몬스터 공격 10% 확률 무효화
- ✅ 60레벨: 데미지 감소 30%
- ✅ 70레벨: 체력 최대치 +8HP (속성, PlayerTickHandler)
- ✅ 80레벨: 독/화염 데미지 면역 (독 효과 자체 데미지만, 마녀 마법은 차단 안 함)
- ✅ 90레벨: 데미지 감소 50%
- ✅ 100레벨 각성: 치명타 데미지(6+ HP) 30% 확률 완전 무효화

---

# 🔨 제작 스킬

## 대장장이 (Smithing)
- ✅ 경험치: 철/금/다이아/네더라이트 도구 제작 시 15XP, 모루 수리 시 10XP
- ✅ 10레벨: 금속 도구 내구도 소모 10% 취소 (TickEvent 기반 복원 방식)
- ✅ 20레벨: 모루 수리 경험치 비용 20% 감소 (환급 방식)
- ✅ 30레벨: 금속 도구 내구도 소모 25% 취소
- ✅ 40레벨: 제작 시 재료 절약 확률 15% (결과물 추가 지급)
- ✅ 50레벨: 도구 제작 시 랜덤 인챈트 1개 자동 부여
- ✅ 60레벨: 금속 도구 내구도 소모 50% 취소
- ✅ 70레벨: 모루 수리 경험치 비용 50% 감소 (환급 방식)
- ✅ 80레벨: 금속 도구 내구도 소모 75% 취소
- ✅ 90레벨: 제작 시 재료 절약 확률 40%
- ✅ 100레벨 각성: 제작 도구에 §6§l[각성] 접두사 + gathercraft_awakened NBT → Haste II 지속 효과

## 마법부여 (Enchanting)
- ✅ 경험치: 인챈트 테이블에서 인챈트 수행 시 (소비 레벨 × 5 XP, 5분 쿨다운)
- ⚠️ 10/30/40/70/90레벨: XP 비용 환급 (8%/15%/20%/30%/45%) — 원래 스펙(직접 감소)과 다른 방식, 컨테이너 닫을 때 처리
- ✅ 20레벨: 인챈트 레벨 보너스 +1
- ✅ 50레벨: 인챈트 레벨 보너스 +3
- ⚠️ 50/80/100레벨: 저주 인챈트 자동 제거 (30%/60%/확정) — 원래 스펙(40레벨 면역)과 다른 레벨·방식
- ⚠️ 60/80/100레벨: 추가 인챈트 부여 확률 (10%/20%/35%) — 원래 스펙(70레벨)과 다른 레벨
- ✅ 80레벨: 인챈트 레벨 보너스 +5
- ✅ 100레벨 각성: 인챈트 시 최고 등급(레벨 30) 인챈트 보장

---

# 코딩 규칙
- DeferredRegister 패턴 사용
- 이벤트는 @SubscribeEvent 사용
- 스킬 데이터는 플레이어 NBT에 저장 (`getPersistentData()` → `"GatherCraft"` compound)
- 성능 최적화 우선 (틱마다 연산 최소화)
- 레벨업 시 채팅 + 파티클 연출
- 파티클 스폰은 `ParticleUtil` 유틸 사용 (원형/폭발/색상 헬퍼)
- 클라이언트 전용 코드는 반드시 `@OnlyIn(Dist.CLIENT)` + `DistExecutor.unsafeRunWhenOn` 분리
- S2C 패킷은 `PacketHandler.sendToPlayer()` 사용
- 난수는 `ThreadLocalRandom.current()` 사용 (`new Random()` 금지)
- 레벨+XP 동시 갱신은 반드시 `SkillData.updateSkill()` 사용 (원자성 보장)
- **SkillPointStat 사용 금지**: 신규 기능은 레벨 기반 로직만 사용 (기존 36개 스탯 중 미구현분을 마저 연결하는 버그 수정은 예외 — v1.6.6 참고)

# 프로젝트 구조
```
src/main/java/com/gathercraft/gathercraft/
├── GatherCraft.java              # 메인 모드 클래스, 핸들러 등록
├── command/
│   ├── SkillCommand.java         # /skill, /skill <name>
│   ├── GatherCraftCommand.java   # /gathercraft test ...
│   └── TpaCommand.java           # /tpaccept, /tpdeny (채팅 클릭 메시지의 RUN_COMMAND 대상)
├── item/
│   └── SkillBookItem.java        # 스킬 책 아이템 (isFoil=true, use()→GUI 오픈)
├── particle/
│   └── ParticleUtil.java         # 파티클 스폰 헬퍼 (원형/폭발/스킬색상)
├── network/
│   ├── PacketHandler.java        # SimpleChannel 패킷 등록 (S2C/C2S)
│   └── packet/
│       ├── ScreenFlashPacket.java        # 화면 빨간 플래시 (S2C, ID 0)
│       ├── DashRequestPacket.java        # 대시 요청 (C2S, ID 1)
│       ├── DashSyncPacket.java           # 쿨타임 동기화 (S2C, ID 2)
│       ├── SkillXpUpdatePacket.java      # XP/레벨업/티어업 동기화 (S2C, ID 3)
│       ├── SkillPointOfferPacket.java    # 레벨업 스탯 선택지 3개 전달 (S2C, ID 4)
│       ├── SkillPointChoicePacket.java   # 스탯 선택 결과 전달 (C2S, ID 5)
│       ├── DamageTextPacket.java         # 부유 데미지 텍스트 전송 (S2C, ID 6)
│       ├── WaypointSavePacket.java       # 현재 위치 웨이포인트 저장 (C2S, ID 7)
│       ├── WaypointDeletePacket.java     # 웨이포인트 삭제 (C2S, ID 8)
│       ├── WaypointTeleportPacket.java   # 웨이포인트로 텔레포트 (C2S, ID 9)
│       ├── WaypointSyncPacket.java       # 웨이포인트 목록 동기화 (S2C, ID 10)
│       ├── TitleSyncPacket.java          # 칭호 해금/착용 목록 동기화 (S2C, ID 11)
│       ├── TitleEquipPacket.java         # 칭호 착용/해제 토글 (C2S, ID 12)
│       ├── TpaRequestPacket.java         # TPA 요청 전송 (C2S, ID 13)
│       ├── TpaResponsePacket.java        # TPA 수락/거절 (C2S, ID 14)
│       ├── TpaAskPacket.java             # TPA 요청 수신 알림 (S2C, ID 15)
│       ├── TitleBroadcastPacket.java     # 착용 칭호를 주변 플레이어에게 브로드캐스트 (S2C, ID 16)
│       ├── QuestSyncPacket.java          # 오늘의 퀘스트 3개 동기화 (S2C, ID 17)
│       ├── OpenQuestBoardPacket.java     # 스킬 책 GUI를 퀘스트 탭(4)으로 오픈 (S2C, ID 18)
│       ├── AchievementSyncPacket.java    # 업적 해금 목록 + 수령 목록 + 카운터 동기화 (S2C, ID 19)
│       ├── QuestClaimPacket.java         # 퀘스트 보상 수령 요청 (C2S, ID 20)
│       └── AchievementClaimPacket.java   # 업적 보상 수령 요청 (C2S, ID 21)
├── client/
│   ├── ClientSetup.java          # 오버레이/키바인딩 등록 (modBus, 클라이언트 전용)
│   ├── gui/
│   │   ├── SkillBookScreen.java  # 스킬 책 GUI (6탭: 스킬 현황 / 웨이포인트 / 칭호 / 텔포 / 퀘스트 / 업적), `SkillBookScreen(int initialTab)` 생성자 오버로드, 칭호/업적 탭 마우스 휠 스크롤(enableScissor 클리핑 + 스크롤바)
│   │   ├── SkillPointScreen.java # 레벨업 스탯 선택 팝업 (3개 버튼, 0.5초 딜레이, 누적 툴팁)
│   │   └── TpaRequestScreen.java # TPA 요청 수신 팝업 (0.5초 딜레이, 수락/거절 버튼, TpaResponsePacket 전송)
│   ├── keybinding/
│   │   ├── KeyBindings.java      # R키 대시 KeyMapping 정의
│   │   └── ClientKeyHandler.java # forgeBus ClientTickEvent → 패킷 전송, 바닐라 XP바 숨김
│   └── overlay/
│       ├── DamageFlashOverlay.java  # 화면 가장자리 빨간 플래시 렌더링
│       ├── SkillBarOverlay.java     # 원형 쿨타임 바 + SkillSlotEntry
│       ├── SkillXpBarOverlay.java   # 화면 상단 스킬 XP 바 (색상/레벨업/티어업 애니메이션)
│       ├── FloatingCombatText.java  # 부유 전투 텍스트 (데미지/HP, RenderLevelStageEvent)
│       └── TitleNameTagRenderer.java # 착용 칭호를 이름표 위에 렌더링 (RenderNameTagEvent, forgeBus)
└── skill/
    ├── SkillType.java            # 9개 스킬 enum (color 필드, findByName())
    ├── SkillTier.java            # 10개 티어 enum (color/textColor 필드)
    ├── SkillData.java            # NBT 저장/로드 (getRoot/saveRoot/updateSkill/loadFromNBT/saveToNBT)
    ├── SkillPointStat.java       # 스킬 포인트 스탯 enum (9스킬×4옵션=36개, NBT 키 "sp_XXX")
    ├── SkillUtil.java            # 공통 유틸 (spawnExtraDrops)
    ├── AntiExploitManager.java   # 플레이어 설치 블록 위치 추적(런타임 Set<Long>, 최대 10만 개 LRU), "설치 후 재채굴" XP 파밍 방지
    ├── SkillManager.java         # addXP(), 레벨업 처리, XP패킷 전송, sendSkillPointOffer()
    ├── dash/
    │   └── DashManager.java      # 대시 서버 로직 (velocity, 무적, NBT 쿨타임)
    └── handler/
        ├── MiningHandler.java
        ├── LumberjackHandler.java
        ├── FarmingHandler.java
        ├── FishingHandler.java
        ├── CookingHandler.java
        ├── HuntingHandler.java
        ├── DefenseHandler.java
        ├── SmithingHandler.java
        ├── EnchantingHandler.java
        ├── PlayerTickHandler.java   # 채광/벌목 Haste, 각성 아이템 DIG_SPEED, 방어 속성, 대시 잔상, 로그인/로그아웃/리스폰/Clone 처리, 웨이포인트 로그인 동기화
        └── SkillBookHandler.java    # 핫바 8번 슬롯 스킬 책 상시 유지
waypoint/
├── WaypointData.java          # 웨이포인트 1개 데이터 (name/icon/dim/x/y/z/yaw), NBT 직렬화
├── WaypointManager.java       # 저장/조회/삭제/텔레포트 (SkillData.getRoot() 하위 "waypoints" ListTag, 최대 10개)
└── WaypointClientCache.java   # 클라이언트 캐시 (@OnlyIn(Dist.CLIENT), WaypointSyncPacket 수신 시 갱신)
title/
├── Title.java                 # 칭호 17종 enum (id/displayName/requiredSkill/requiredLevel), isUnlocked()/conditionText()/byId()
├── TitleManager.java          # 해금 체크(checkAndUnlock)/착용 토글(equip)/보유 판정(hasTitle)/XP배율(getXPMultiplier) (SkillData.getRoot() 하위 "unlocked_titles"/"equipped_title")
├── TitleChatHandler.java      # 착용 칭호를 채팅 메시지 앞에 표시 (ServerChatEvent 취소 후 시스템 메시지로 재브로드캐스트, forgeBus)
├── TitleClientCache.java      # 본인 칭호 클라이언트 캐시 (@OnlyIn(Dist.CLIENT), TitleSyncPacket 수신 시 갱신, 스킬 책 GUI용)
└── TitleNameTagCache.java     # 주변 플레이어 칭호 캐시 (@OnlyIn(Dist.CLIENT), Map<UUID,String>, TitleBroadcastPacket 수신 시 갱신, 이름표 렌더링용)
quest/
├── QuestData.java             # 퀘스트 1개 데이터 (id/description/skillType/actionType/targetBlock/goal/progress/completed/claimed/rewardXP/rewardExpBottles), NBT 직렬화
├── QuestPool.java             # 쉬움/보통/어려움 각 8종 풀(총 24종), `getDailyQuests(long seed)`로 시드 기반 1개씩 선택
├── QuestManager.java          # 날짜(yyyyMMdd) 기반 자동 갱신(getQuests/refreshQuests), 진행도 적립(progress, ANY/BOSS/블록·몹ID contains 매칭), 보상 수령(claim, 스킬 XP + 경험치 병 스폰) (SkillData.getRoot() 하위 "quest_date"/"quest_0~2")
└── QuestClientCache.java      # 클라이언트 캐시 (@OnlyIn(Dist.CLIENT), QuestSyncPacket 수신 시 갱신)
achievement/
├── AchievementManager.java    # 업적 15종 정의(record Achievement에 condition 필드 포함, Category enum), has()/unlock()(해금 기록+서버 공지+클릭형 채팅만, 보상 지급 없음)/claim()(보상 지급: XP+경험치 병+파티클)/isClaimed()/getCondition()/getGoal()/getCounterKey()/getRewardText()(모두 record 필드 위임)/incrementAndCheck()(카운터)/checkAllSkillLevel() (SkillData.getRoot() 하위 "ach_*"/"ach_claimed_*"/"ach_cnt_*")
└── AchievementClientCache.java # 클라이언트 캐시 (@OnlyIn(Dist.CLIENT), 해금 목록 + 수령 목록 + 카운터 맵, AchievementSyncPacket 수신 시 갱신)
block/
└── QuestBoardBlock.java       # 이 모드 최초의 커스텀 블록. 우클릭 시 QuestSyncPacket + OpenQuestBoardPacket 전송 (bookshelf 텍스처, cube_all 모델)
tpa/
└── TpaManager.java             # TPA 요청/응답, 60초 쿨다운·60초 만료, 차원 간 텔포 (WaypointManager.teleport() 패턴 재사용), NBT 저장 없음(런타임 Map)
```

# 주요 API 패턴
- XP 적립: `SkillManager.addXP(player, SkillType.XXX, amount)`
- 레벨 조회: `SkillData.getLevel(player, skill)`
- 레벨+XP 원자 갱신: `SkillData.updateSkill(player, skill, level, xp)` ← 레벨업 시 사용
- 레벨 설정(테스트용): `SkillData.setLevel(player, skill, level)` + `SkillData.setXP(player, skill, 0)`
- XP 진행도: `SkillManager.getXPProgress(player, skill)` → 0.0~1.0
- 필요 XP 공식 (3단계 티어):
  - Lv 0~19: `(level+1) × 8`
  - Lv 20~59: `(level+1) × 20`
  - Lv 60~99: `(level+1) × 50`
- NBT 루트 키: `SkillData.ROOT_KEY` = `"GatherCraft"`
- 파티클 원형: `ParticleUtil.spawnCircle(level, cx, cy, cz, particle, radius, count, height)`
- 파티클 폭발: `ParticleUtil.spawnBurst(level, x, y, z, particle, count, spread)`
- 스킬 색상 파티클: `ParticleUtil.getSkillColor(SkillType.XXX)` → DustParticleOptions
- 스킬 색상 조회: `skill.color` (SkillType enum 필드, 0xRRGGBB)
- 티어 색상 조회: `tier.color` / `tier.textColor` (SkillTier enum 필드)
- 스킬명으로 조회: `SkillType.findByName(name)` (한국어/영어/Enum명 모두 가능)
- 추가 드롭 스폰: `SkillUtil.spawnExtraDrops(state, world, pos, player)`
- 화면 플래시: `PacketHandler.sendToPlayer(player, new ScreenFlashPacket(0.85f))`
- XP 바 업데이트: `PacketHandler.sendToPlayer(player, new SkillXpUpdatePacket(skill, level, progress, leveledUp, tierUp))`
- 스킬 책 GUI 오픈: `Minecraft.getInstance().setScreen(new SkillBookScreen(player))` (클라이언트 전용)
- 스탯 포인트 누적값 조회: `SkillData.getStatValue(player, SkillPointStat.XXX)` → float
- 스탯 포인트 대기 수: `SkillData.getPendingCount(player, skill)` / `setPendingCount()`
- 다음 스탯 offer 전송: `SkillManager.sendSkillPointOffer(sp, skill)` (로그인 핸들러에서도 사용)
- 방어 속성 즉시 갱신: `PlayerTickHandler.applyDefenseAttributesNow(sp)` (레벨업 시 자동 호출됨)
- 데이터 로드/저장: `SkillData.loadFromNBT(player)` / `SkillData.saveToNBT(player)`
- 웨이포인트 목록 조회: `WaypointManager.getAll(player)` → `List<WaypointData>`
- 웨이포인트 저장/삭제/텔포: `WaypointManager.add(player, data)`(최대 10개, 초과 시 false) / `WaypointManager.delete(player, index)` / `WaypointManager.teleport(serverPlayer, index)`
- 웨이포인트 목록 동기화: `PacketHandler.sendToPlayer(player, new WaypointSyncPacket(WaypointManager.getAll(player)))`
- 칭호 목록/착용 조회: `TitleManager.getUnlocked(player)` → `List<String>` / `TitleManager.getEquipped(player)` → `String`
- 칭호 해금 체크(멱등): `TitleManager.checkAndUnlock(serverPlayer)` (레벨업 시 `SkillManager.onLevelUp()`, 로그인 시 `PlayerTickHandler.onPlayerLogin()`에서 호출)
- 칭호 착용/해제: `TitleManager.equip(serverPlayer, titleId)` (이미 착용 중이면 해제하는 토글)
- 칭호 표시명/조건 텍스트: `TitleManager.getDisplayName(id)` / `TitleManager.getConditionText(id)`
- 칭호 보유 판정(착용 여부 무관): `TitleManager.hasTitle(player, id)`
- 칭호 보유 기반 전 스킬 XP 배율: `TitleManager.getXPMultiplier(player)` → all_100 1.15 / all_50 1.05 / 없으면 1.0 (`SkillManager.addXP()`에서 자동 적용, 스킬별 보너스는 각 핸들러에서 `hasTitle()`로 개별 처리)
- 칭호 브로드캐스트(이름표용): `PacketHandler.sendToPlayer(nearby, new TitleBroadcastPacket(uuid, equippedId))` (빈 문자열이면 클라이언트에서 캐시 제거)
- TPA 요청/응답: `TpaManager.request(serverPlayer, targetName)` / `TpaManager.respond(serverPlayer, accept)`
- TPA 위생 정리: `TpaManager.clearPlayer(uuid)` (로그아웃 시 호출)
- 오늘의 퀘스트 조회(날짜 갱신 포함): `QuestManager.getQuests(player)` → `List<QuestData>` (3개)
- 퀘스트 진행도 적립: `QuestManager.progress(serverPlayer, actionType, target, amount)` (`target.equals("ANY")` 이거나 `contains()` 매칭 시 적립, 완료 시 클릭형 채팅 자동 발송)
- 퀘스트 보상 수령: `QuestManager.claim(serverPlayer, index)` (0~2)
- 업적 보유 판정: `AchievementManager.has(player, id)`
- 업적 해금(멱등): `AchievementManager.unlock(serverPlayer, id)` (서버 전체 공지 + 클릭형 채팅만, 보상은 지급하지 않음)
- 업적 보상 수령: `AchievementManager.claim(serverPlayer, id)` (해금 후에만 가능, 1회 한정 — XP + 경험치 병 + 파티클 지급)
- 업적 수령 여부 판정: `AchievementManager.isClaimed(player, id)`
- 업적 조건/목표/카운터키/보상텍스트 조회: `AchievementManager.getCondition/getGoal/getCounterKey/getRewardText(id)` (모두 `Achievement` record 필드 위임)
- 업적 카운터 적립+체크: `AchievementManager.incrementAndCheck(serverPlayer, counter, amount, checkIds...)`
- 전 스킬 레벨 기반 종합 업적 체크: `AchievementManager.checkAllSkillLevel(serverPlayer)` (`SkillManager.onLevelUp()`에서 매 레벨업마다 호출)
- 업적 동기화 패킷 생성: `AchievementManager.buildSyncPacket(player)` → `AchievementSyncPacket` (unlocked/claimed/counters 3필드)

# 명령어
- `/skill` — 전체 스킬 현황
- `/skill <name>` — 특정 스킬 상세 (한국어/영어명 모두 가능)
- `/gathercraft test <skill> <level>` — 특정 스킬 레벨 설정 (OP 2)
- `/gathercraft test all <level>` — 전체 스킬 레벨 설정 (OP 2)
- `/gathercraft test reset` — 전체 스킬 초기화 (OP 2)
- `/gathercraft test auto` — 인게임 자동 테스트 실행 (XP 공식·광물/몹 XP·NBT·PlayerClone·연쇄 벌목 재진입 가드·AntiExploitManager 등록·스킬포인트 스탯 적용 검증, OP 2)
- `/gathercraft quest claim <0|1|2>` — 퀘스트 보상 수령 (권한 제한 없음)
- `/gathercraft giveboard` — 퀘스트 게시판 블록 지급 (OP 2)
- `/gathercraft achievement claim <id>` — 업적 보상 수령 (권한 제한 없음)
- `/tpaccept`, `/tpdeny` — TPA 요청 수락/거절 (권한 제한 없음, 채팅 클릭 메시지의 RUN_COMMAND 대상)

# 빌드
```bash
./gradlew clean build
```
결과물: `build/libs/gathercraft-1.7.0.jar`

# 릴리스 아카이브
`releases/` 폴더에 버전별 jar를 누적 보관한다. **버전을 올리고 빌드에 성공할 때마다** 새 jar를 이 폴더에 복사한다 (기존 파일은 덮어쓰지 않고 계속 쌓아 과거 버전도 남겨둠).
```bash
cp build/libs/gathercraft-<버전>.jar releases/
```

---

# 버전 히스토리 (요약)

| 버전 | 주요 내용 |
|------|-----------|
| v0.1.0 | 9개 스킬 핸들러, NBT 저장, 레벨업 파티클, /skill 명령어 |
| v0.2.0 | ParticleUtil, S2C 패킷, 화면 플래시, 전투/채광/농사/낚시/요리 파티클 |
| v0.3.0 | 사망 리스폰 데이터 보존, 마법부여 레벨 보너스, 벌목 묘목 심기, 모루 XP 환급, 낚시 각성 아이템 |
| v0.4.0 | 대시 스킬 (R키, DashManager, 쿨타임 UI, SkillBarOverlay) |
| v0.5.0 | 스킬 책 아이템/GUI, SkillXpBarOverlay, SkillXpUpdatePacket |
| v0.6.0 | 바닐라 XP 바 자동 숨김, SkillHUD 제거, 스킬 책 텍스처 수정 |
| v0.7.0 | SkillUtil 추출, SkillType/SkillTier enum 강화, 중복 코드 제거, PacketHandler 제네릭화 |
| v0.8.0 | 스킬 책 실시간 XP 반영, 파티클 경량화, XP 바 위치 상단으로 변경 |
| v0.9.0 | 크리티컬 중복 버그 수정, 독 면역 조건 수정, TOTEM 파티클 중복 수정, 속성 재적용 최적화, ThreadLocalRandom 전환, updateSkill() 원자성 추가 |
| v1.0.0 | 스킬 포인트 시스템: 레벨업 시 스탯 선택 팝업, SkillPointStat enum 36개, S2C/C2S 패킷 2개, SkillPointScreen GUI, 9개 핸들러 스탯 반영, 로그인 시 대기 offer 재전송 |
| v1.1.0 | 부유 전투 텍스트: 몬스터 공격 시 머리 위 데미지 표시, 크리티컬 강조, 2.5초 페이드 아웃 |
| v1.2.0 | XP 3단계 티어 공식, 광석/몹 희귀도별 XP, 사냥/요리/농사/낚시/마법부여 신규 기능, 재접속 데이터 초기화 버그 수정 |
| v1.3.0 | IsDashing 고착 버그 수정, 채광 각성 연쇄 이벤트 방지(ThreadLocal), 즉사 데미지 텍스트 수정, 대시 키 레이블 동적화, NBT 틱 읽기 최적화(dashingPlayers Set), `/gathercraft test auto` 인게임 자동 테스트 커맨드 추가 |
| v1.4.0 | 웨이포인트 시스템: 스킬 책 탭 UI, 좌표 저장(최대 10개), 차원 간 텔포, S2C 동기화 |
| v1.5.0 | 스킬 책 4탭 UI 전면 개편: 웨이포인트(저장/차원 간 텔포), 칭호 시스템(17개), TPA(플레이어 간 텔포 요청/수락/거절) |
| v1.5.1 | 칭호 이름표 표시, 칭호 보유 효과, 리셋 시 칭호 초기화, TitleBroadcastPacket |
| v1.6.0 | 일일 퀘스트(24종 풀, 쉬움/보통/어려움), 업적 시스템(15개, 서버 전체 공지), 퀘스트 게시판 블록(bookshelf 텍스처, 우클릭으로 퀘스트 탭 오픈), 스킬 책 6탭 UI |
| v1.6.1 | 스킬 책 업적/칭호 탭 마우스 휠 스크롤 + 스크롤바 + 클리핑 |
| v1.6.2 | 업적 탭 전면 개선: 조건 설명/진행도바/툴팁/카테고리 구분선, 보상 수령 시스템(달성/미수령/완료 상태 분리), AchievementClaimPacket |
| v1.6.3 | 농사 20레벨 뼛가루 2회 효과 정식 구현, 낚시 속도 증가 레벨을 스펙대로 20/40/70(10%/25%/50%)로 수정, 요리 포화도 증가를 40/70레벨 %기반(FoodProperties)으로 재구현, FishingHandler 리플렉션 필드 캐싱 리팩토링, ItemSmeltedEvent 서버 발동 여부 포지 소스 검증 완료 |
| v1.6.4 | 요리 다중 버프 시스템 재설계: 50/80레벨 확률 기반 추가 버프 → 결정적 2개/3개 동시 적용으로 교체, 90레벨 모든 버프 강도 최대(amplifier III) 구현, 문서화된 TODO 전 항목 구현 완료 |
| v1.6.5 | 낚시 속도 버그 수정(잘못된 필드/시점 → `timeUntilLured` + TickEvent 감지 방식), 칭호 채팅 표시 기능 추가(`TitleChatHandler`) |
| v1.6.6 | SkillPointStat 13개 미적용 스탯 전부 활성화: 레벨 기반 로직에 스탯 보너스 덧셈 방식으로 반영, 하위 호환(포인트 미투자 시 기존 동작 동일) |
| v1.6.7 | 섬손 재설치 XP 파밍 익스플로잇 수정 (AntiExploitManager: 플레이어 설치 블록 위치 추적, 광석/나무 양쪽 방어, 연쇄 채굴도 적용) |
| v1.6.8 | 연쇄 벌목 재진입 가드 추가(가드 없던 경우), /gathercraft test auto 확장: 연쇄 벌목 가드·익스플로잇 방어·스탯 적용 코드 레벨 검증 추가 |
| v1.6.9 | 레벨업 선택지 설명 추가: SkillPointStat에 description 필드, SkillPointScreen 버튼 3줄 구조(이름/설명/증가량) |
| v1.7.0 | 노션 피드백 반영: MINING/LUMBERJACK_SPEED 텍스트 수정, 오버밸런스 너프(6포인트→Haste +1), 레벨+스탯 Haste 합산 버그 수정, 채굴/벌목 Haste 크로스 버그 수정(바라보는 블록 타입 분리) |

---

# ❌ 미구현 목록 (Known TODO)

(현재 없음 — 모든 스킬의 문서화된 항목 구현 완료)

---

# ⚠️ 설계 노트

## all_100 이동속도 보너스 수치 불일치
`PlayerTickHandler`에서 `all_100` 보유 시 30틱마다 `MobEffectInstance(MOVEMENT_SPEED, 40, 0, ...)`(Speed I, 앰플리파이어 0)을 부여한다. 기획 설명은 "+5% 이동속도"이지만 vanilla Speed I의 실제 효과는 약 +20%로 정확히 일치하지 않는다 — 레벨 기반 커스텀 AttributeModifier 대신 바닐라 포션 효과로 간단히 구현한 결과.

## TitleNameTagRenderer 등록 위치
`RenderNameTagEvent`는 forge 이벤트버스 클라이언트 전용 이벤트라 `ClientSetup`(modBus 전용, `RegisterGuiOverlaysEvent`/`RegisterKeyMappingsEvent`만 처리)이 아니라, 기존 `FloatingCombatText`와 동일하게 `GatherCraft.java`의 `DistExecutor.unsafeRunWhenOn(Dist.CLIENT, ...)` 블록에서 `MinecraftForge.EVENT_BUS.register(new TitleNameTagRenderer())`로 직접 등록한다.

## RenderNameTagEvent의 "추가 줄" 제약
`RenderNameTagEvent`는 단일 `Component`만 교체 가능하고 별도의 "이름표 위에 추가 줄"을 그리는 훅이 없다. 따라서 vanilla 이름표는 그대로 두고(`Result` 미변경), 같은 이벤트의 `PoseStack`/`MultiBufferSource`/`packedLight`를 이용해 vanilla 이름표보다 위(`bbHeight+0.5+0.3`)에 칭호 텍스트를 별도로 `pushPose/popPose`하여 덧그리는 방식으로 구현했다 (`client/overlay/TitleNameTagRenderer.java`).

## GatherCraftCommand 권한 구조 변경 (v1.6.0)
기존에는 루트 `literal("gathercraft")`에 `.requires(src -> src.hasPermission(2))`가 걸려 있어 모든 하위 명령(`test` 포함)이 OP 2를 요구했다. `/gathercraft quest claim`은 일반 플레이어도 사용해야 하므로, `.requires()`를 루트에서 제거하고 `test` 서브트리와 `giveboard`에 개별로 옮겼다. Brigadier는 상위 노드의 `requires()`를 만족해야 하위 노드 파싱이 진행되므로, 루트에 권한을 걸면 하위 전체가 상속받아 차단된다는 점에 유의.

## 블록 등록 인프라 신설 (v1.6.0)
v1.5.1까지 이 모드는 블록이 하나도 없는 순수 아이템/스킬 모드였다 (`DeferredRegister<Block>` 부재, `lang/` 리소스 없음). `QuestBoardBlock` 추가를 계기로 `GatherCraft.java`에 `DeferredRegister<Block> BLOCKS`를 신설하고, 기존 아이템(`skill_book`)의 싱글턴+람다 등록 방식과 달리 `RegistryObject`를 필드로 저장하는 표준 패턴을 사용했다 (`giveboard` 명령어 등에서 아이템 참조가 필요하기 때문). `lang/ko_kr.json`·`en_us.json`도 이때 처음 생성되었으며, 기존 하드코딩 아이템명(`skill_book`)은 마이그레이션하지 않았다.

## 칭호 탭 2컬럼 + 스크롤 조합 (v1.6.1)
칭호 탭은 `col = i/rows, row = i%rows` (컬럼 우선 채움) 방식으로 17종을 2컬럼에 배치한다. 여기에 스크롤을 추가하면서 `rows`(컬럼당 전체 행 수 = `ceil(17/2)`)는 스크롤과 무관하게 고정하고, 화면에는 `titleScrollOffset` 기준 윈도우(`row`~`row+TITLE_VISIBLE_ROWS`)만 슬라이싱해서 그린다. 컬럼1의 인덱스는 항상 `row + totalRows`로 계산되므로(스크롤 여부와 무관하게 원래의 컬럼 우선 인덱싱 규칙을 그대로 보존), 스크롤해도 각 항목이 원래 있어야 할 컬럼에서 어긋나지 않는다. `renderTitleTab`과 `handleTitleClick` 양쪽이 동일한 윈도우 계산을 공유해야 클릭 좌표가 어긋나지 않는다.

## 업적 보상 수령 시스템 설계 (v1.6.2)
`getCondition()`/`getGoal()`/`getCounterKey()`는 별도 switch문 대신 `Achievement` record의 `condition`/`counterGoal`/`counterKey` 필드에 위임하는 방식으로 구현했다. `counterGoal`/`counterKey`는 이미 `ALL` 리스트 생성 시점에 정의되어 있으므로, 별도 switch를 추가하면 두 데이터 소스가 어긋날 위험이 생기기 때문이다(`condition`은 이번에 record에 신규 필드로 추가). 같은 이유로 GUI(`SkillBookScreen`)에서는 `AchievementManager.getGoal()` 등을 거치지 않고 이미 들고 있는 `Achievement` 객체의 필드를 직접 사용한다.

카테고리 구분선과 업적 행은 스펙상 서로 다른 높이(14px/22px)를 제안했지만, v1.6.1에서 만든 스크롤 인프라(`buildAchievementRows()` → 균일한 `rowStride`로 슬라이싱)가 "모든 행이 같은 높이"를 전제로 하므로 구분선도 업적 행과 동일한 22px 슬롯에 맞춰 그린다(가변 높이 도입 시 스크롤/클릭 좌표 계산이 크게 복잡해짐).

## 농사 20레벨 뼛가루 2회 효과 구현 방식 (v1.6.3)
`BonemealEvent`의 `Event.Result.ALLOW`를 설정하면 이벤트 리스너 종료 후 바닐라 `BoneMealItem`이 `performBonemeal()`을 한 번 더 강제 호출한다(40레벨 즉시 완숙 구현에서 이미 검증된 패턴). 20레벨 로직은 이 특성을 활용해 리스너 안에서 `cropBlock.performBonemeal()`을 직접 2회 호출한 뒤 `Result.ALLOW`를 설정한다 — 바닐라가 추가로 한 번 더 호출하더라도 이미 `maxAge`에 도달했으면 `CropBlock.performBonemeal()` 내부에서 나이가 `maxAge`로 캡핑되므로 안전하다(초과 성장 없음). 20~39레벨 구간에서만 적용되며, 40레벨 이상은 기존 확률 기반 즉시 완숙 로직으로 완전히 분기된다.

## ItemSmeltedEvent 서버 발동 검증 (v1.6.3)
`forge-1.20.1-47.4.10-sources.jar`의 패치 파일(`FurnaceResultSlot.java.patch`)을 직접 확인한 결과, `net.minecraftforge.event.ForgeEventFactory.firePlayerSmeltedEvent(player, stack)`가 `FurnaceResultSlot`의 `onTake()`(아이템을 결과 슬롯에서 꺼낼 때 바닐라가 호출하는 훅) 안에서 무조건 호출된다. 컨테이너 슬롯 클릭 처리는 전적으로 서버 사이드 로직(`ServerboundContainerClickPacket` 처리)이므로 싱글플레이/데디케이티드 서버 구분 없이 동일하게 발동한다 — 별도 조건부 로직이 없어 실제 서버 구동 테스트 없이도 코드 레벨에서 발동이 보장된다. 단, 호퍼가 화로 결과 슬롯에서 아이템을 자동 추출하는 경우는 `Container.removeItem()` 경로를 사용해 `Slot.onTake()`를 거치지 않으므로 이 이벤트가 발동하지 않는다(플레이어가 GUI를 직접 열어 꺼낼 때만 XP 적립).

## 요리 다중 버프 시스템 재설계 (v1.6.4)
기존 50/80/100레벨 "추가 버프 확률(15%/30%/50%)"은 원래 스펙(50레벨 2개 동시 적용, 80레벨 3개 동시 적용)을 확률로 근사한 대체 구현이었다. 이를 걷어내고 음식 카테고리(고기/가금류/생선/주식/디저트)별로 최대 3개까지의 버프를 우선순위 배열(`MEAT_BUFFS` 등)로 정의한 뒤, `buffCount(level)`(20→1개, 50→2개, 80→3개)만큼 배열 앞에서부터 순서대로 적용하는 결정적 방식으로 교체했다. 100레벨의 기존 "체력 +4 회복 + 디버프 제거" 각성 효과와 역할이 겹치지 않도록 확률 시스템의 100레벨 티어(50% 확률 추가 버프)는 삭제했다.
카테고리별 2·3번째 버프(예: 고기 → 공격력/이동속도/체력증가, 생선 → 수중호흡/돌고래의 우아함/전도체의 힘)는 기존 스펙 문서에 세부 항목이 없어 테마에 맞게 새로 설계한 것이며, 필요 시 `CookingHandler`의 `*_BUFFS` 배열만 수정하면 조정 가능하다.
재생(REGENERATION) 효과는 다른 버프보다 체감 강도가 커서 지속시간을 `buffDuration()`의 1/4로 제한하는 기존 로직을 그대로 유지했다(어느 우선순위 자리에 오든 동일 적용).

## 요리 90레벨 "모든 버프 강도 최대" 구현 (v1.6.4)
`buffAmplifier(level)`이 90레벨 이상에서 무조건 `amplifier=2`(게임 내 표시 "III")를 반환하도록 해, 기존 60레벨 이상 `amplifier=1` 로직을 상위 티어로 대체했다. 이 값은 워터브레싱처럼 수치상 체감 차이가 없는 효과에도 동일하게 적용되지만(표시만 "III"로 바뀔 뿐 실질적 부작용 없음), 재생처럼 강도가 즉각 체감되는 효과에는 상당히 강력해지므로 위 지속시간 제한과 함께 균형을 맞췄다.

## 낚시 속도 버그 원인과 수정 (v1.6.5)
v1.6.3에서 구현한 낚시 속도 증가가 실기 테스트에서 "레벨과 무관하게 항상 착수 즉시 입질이 온다"는 버그로 발견됐다. 디컴파일한 바닐라 `FishingHook.java`(`net.minecraftforge:forgeflower`로 `forge-1.20.1-47.4.10_mapped_official_1.20.1.jar`에서 직접 디컴파일해 확인)를 보면, 실제 "입질까지의 대기시간"은 `timeUntilHooked`가 아니라 `timeUntilLured`이며, `timeUntilLured`는 낚시찌가 물에 착수한 뒤 `catchingFish()`가 처음 호출될 때(모든 카운터가 0인 최초 상태)에야 `Mth.nextInt(100,600) - lureSpeed*100`으로 배정된다. 반면 기존 코드는 `EntityJoinLevelEvent`(캐스팅 직후, 착수 전)에 `timeUntilHooked`를 읽어 `Math.max(20, 0 * (1-감소율))`을 계산했는데, 이 시점엔 두 필드 모두 초기값 0이라 결과는 레벨과 무관하게 항상 20(1초)으로 고정되고, 이 값이 `timeUntilHooked`에 들어가면서 첫 틱부터 "입질 직전 흔들림" 상태로 즉시 진입해버렸다(원래대로라면 100~600틱의 `timeUntilLured` 대기를 먼저 거쳐야 함).
수정은 `LumberjackHandler`의 도끼 내구도 복원과 동일한 "TickEvent로 값 변화를 감시하다 증가하는 순간을 감지" 패턴을 사용한다: `Player`의 public 필드 `fishing`(현재 낚시찌 엔티티)을 `TickEvent.PlayerTickEvent`에서 매 틱 확인하고, `timeUntilLured`가 직전 틱보다 커졌다면(=vanilla가 방금 새 대기 사이클을 배정한 순간) 그 값에 감소율을 곱해 1회만 낮춘다. 이후 매 틱 자연 감소하는 동안은 조건이 성립하지 않아 재적용되지 않고, 사이클이 끝나고 새로 배정될 때마다 다시 감지해 적용된다.

## 칭호 채팅 표시와 서명된 채팅의 트레이드오프 (v1.6.5)
1.20.1의 보안 채팅(secure chat) 시스템은 발신자 표시를 클라이언트가 `ChatType` 바인딩으로 직접 그리므로, `ServerChatEvent#setMessage()`로 Component를 바꿔도 화면에 표시되는 "<발신자>" 부분은 바뀌지 않는다(서명된 프로필 이름과 분리해서 표시 이름만 바꿀 훅이 없음). 따라서 `TitleChatHandler`는 칭호를 착용한 플레이어의 메시지만 `ServerChatEvent`를 취소하고 `PlayerList.broadcastSystemMessage()`로 서명되지 않은 시스템 메시지를 직접 재전송하는 방식을 쓴다 — 이 메시지들은 채팅 신고(chat reporting)/서명 검증 배지가 빠진다는 트레이드오프가 있지만, 이 프로젝트의 다른 기능들(TPA, 퀘스트/업적 알림 등)도 이미 전부 같은 방식(`Component.literal` + 시스템 메시지)을 쓰고 있어 일관성이 있다. 칭호 미착용 플레이어의 메시지는 이벤트를 건드리지 않고 그대로 통과시켜 보안 채팅을 유지한다.

## 섬손 재설치 XP 파밍 익스플로잇 수정 (v1.6.7)
섬손(Silk Touch)으로 캔 광석/원목을 다시 설치한 뒤 재채굴하면 XP·퀘스트 진행도·업적 카운터·추가 드롭이 무한 반복 지급되는 익스플로잇이 있었다. `AntiExploitManager`가 `BlockEvent.EntityPlaceEvent`로 플레이어가 놓은 광석/원목 위치를 `Set<Long>`(블록 좌표를 `BlockPos.asLong()`으로 인코딩)에 기록해두고, `MiningHandler`/`LumberjackHandler`의 `onBlockBreak()` 맨 앞에서 `shouldGiveXP()`로 조회한다 — 추적된 위치면 보상 전체(XP뿐 아니라 퀘스트/업적/드롭/파티클까지 전부)를 스킵하고 조회와 동시에 Set에서 제거한다(같은 위치에 다시 자연 생성되거나 재설치될 수 있으므로 1회성 마킹). 서버 재시작 시 Set이 초기화되는 것은 의도적으로 허용한 범위다(재시작 직후에만 악용 가능, 실질적 억제 효과는 유지).

100레벨 각성 연쇄 채굴(`MiningHandler.triggerAreaMining`)·연쇄 벌목(`LumberjackHandler.triggerChainFelling`) 내부의 `world.destroyBlock()` 호출 지점에도 동일한 체크를 추가했다. 다만 `triggerAreaMining`은 자체 `IS_AREA_MINING` ThreadLocal 가드로 인해 연쇄 파괴된 블록에는 애초에 XP/보상이 지급되지 않으므로(가드가 `onBlockBreak()` 전체를 조기 반환시킴), 이번 추가는 향후 가드 로직이 바뀌거나 드롭만 파밍하는 변형 악용을 막기 위한 선제적 방어에 가깝다.

**미해결 확인 사항(v1.6.8에서 해소)**: `triggerChainFelling`에는 원래 `triggerAreaMining`의 `IS_AREA_MINING` 같은 재진입 방지 가드가 없었다. `world.destroyBlock()`이 Forge에서 `BlockEvent.BreakEvent`를 재발생시키는지(재발생시킨다면 자연 상태의 나무를 100레벨에서 연쇄 벌목할 때마다 로그 1개당 XP가 이미 중복 지급되고 있었다는 뜻) Forge 소스(`Level.java.patch`)에서 명확히 확인하지 못한 상태였다. v1.6.8에서 `IS_CHAIN_FELLING` ThreadLocal 가드를 선제적으로 추가해, `destroyBlock()`이 실제로 이벤트를 재발생시키는지와 무관하게 중복 지급 가능성 자체를 차단했다(근본 원인은 여전히 Forge 소스 레벨로 확정하지 못했지만, 가드로 인해 더 이상 실무적으로 문제되지 않음).

## TPA 채팅 클릭 메시지의 한계
`TpaAskPacket` 수신 시 뜨는 클릭형 채팅 메시지(`§a[수락]`/`§c[거절]`)는 바닐라 `ClickEvent`가 `RUN_COMMAND`(`/tpaccept`, `/tpdeny`)만 지원하는 제약상 **채팅창(T/Enter로 연 상태)이 열려 있을 때만 클릭이 히트테스트된다.** 평상시 자동으로 사라지는 채팅 HUD는 클릭에 반응하지 않는 바닐라 공통 제약이다.
이를 보완하기 위해 `TpaAskPacket.handle()`은 채팅 메시지와 동시에 `TpaRequestScreen` 팝업(0.5초 딜레이, 다른 화면이 열려 있으면 재대기)도 함께 띄운다. 팝업 버튼이 `TpaResponsePacket`의 실질적인 발신 경로이며, 채팅 클릭 경로는 `/tpaccept`·`/tpdeny` 명령어를 통해 별도로 동작한다.

## 연쇄 벌목 재진입 가드 & `/gathercraft test auto`의 리플렉션/소스읽기 혼합 검증 (v1.6.8)
`LumberjackHandler`에 `MiningHandler.IS_AREA_MINING`과 동일한 구조의 `IS_CHAIN_FELLING` ThreadLocal 가드를 추가했다. `onBlockBreak()` 맨 위에서 플래그를 체크해 즉시 반환하고, `triggerChainFelling()`을 호출하는 지점(메서드 내부가 아니라 **호출부**)을 `try/finally`로 감싸 플래그를 set/reset한다 — `MiningHandler`가 `triggerAreaMining()`을 감싸는 위치와 완전히 동일하게 맞춘 것으로, 이후 두 핸들러의 각성 연쇄 로직을 비교할 때 구조가 어긋나지 않는다.

`/gathercraft test auto`에 추가한 10개 코드 레벨 검증은 두 가지 방식을 혼합한다:
- **존재 여부**(ThreadLocal 필드, `AntiExploitManager` 클래스, `@SubscribeEvent`+`EntityPlaceEvent` 리스너)는 **리플렉션**(`Class.forName`/`getDeclaredField`/`getDeclaredMethods`)으로 검증한다. 클래스 바이트코드만 있으면 되므로 배포된 jar(mods 폴더)에서도 정상 동작한다.
- **호출 여부**(`onBlockBreak()` 내부에서 `AntiExploitManager.shouldGiveXP()`를 부르는지, 각 핸들러가 `SkillData.getStatValue()`로 특정 `SkillPointStat`을 조회하는지)는 메서드 본문 내부 로직이라 리플렉션으로 볼 수 없다. `GatherCraftCommand.readSourceFile()`이 `.java` 소스 파일을 텍스트로 읽어 문자열 포함 여부로 대체 검증한다.
  - 이 방식은 소스 트리가 실행 위치 기준 상대 경로(`src/main/java/...` 또는 `../src/main/java/...` 등)에 존재해야 하므로 `gradlew runServer`/`runClient` 같은 개발 환경에서만 동작한다. 실제 배포 서버(mods 폴더에 jar만 있는 환경)에서는 소스 파일을 찾지 못해 `null`을 반환하고, 해당 항목은 `§7⚠ 소스 파일 없음`으로 표시되며 통과/실패 어느 쪽으로도 집계되지 않는다(오탐 방지).

---

# 🧪 다음 세션 확인 필요

## v1.7.0 실기 테스트 미완료 (2026-08-10 기준)
- **Haste 합산/크로스 버그 수정 실기 확인 필요**: `PlayerTickHandler.applyMiningHaste()`/`applyLumberjackHaste()`를 레벨+스탯 amplifier 합산 방식으로 재작성하고, `isLookingAtOre()`/`isLookingAtLog()`(5블록 레이캐스트 기반)로 스탯 보너스를 활동별로 분리했다. 컴파일만 확인했고 `gradlew runClient`로 아직 검증하지 않음. 다음 세션에서 `/gathercraft test mining 80` + MINING_SPEED에 포인트 다수 투자 후: (1) 곡괭이로 광석을 바라볼 때 레벨+스탯 합산 Haste가 붙는지, (2) 나무를 바라볼 때는 스탯 보너스 없이 레벨 기반 Haste만 남는지, (3) 허공을 볼 때도 레벨 기반 Haste는 유지되는지 실기로 확인 필요. LUMBERJACK_SPEED(80레벨+도끼 소지 조건)도 동일하게 교차 검증 필요.
- **SkillPointScreen "(현재 N/6)" 동적 표시 시각 확인 필요**: `{n}` 플레이스홀더를 `resolveDescription()`에서 실제 누적값 기준으로 치환하도록 구현했으나, 축소 렌더링(0.75x) 폭 안에서 텍스트가 잘리지 않는지, 6번째 선택 직후 카운터가 0/6으로 정상 롤오버되는지 시각 확인 필요.

## v1.6.9 실기 테스트 미완료 (2026-08-10 기준)
- **레벨업 팝업 3줄 버튼 레이아웃 시각 확인 필요**: `SkillPointScreen.renderButton()`에 설명 텍스트(2번째 줄)를 `PoseStack.scale(0.75f)`로 축소 렌더링하는 로직을 추가했다. 컴파일은 성공했지만, 36개 스탯 설명 텍스트가 `BTN_W=260` 폭 안에서 잘리거나 겹치지 않는지, `BTN_H=46`/`POPUP_H=250`으로 확대한 팝업이 시각적으로 자연스러운지는 아직 `gradlew runClient`로 확인하지 않았다. 다음 세션에서 `/gathercraft test <skill> <level>`로 레벨업을 유도해 팝업을 직접 띄워 확인 필요(특히 가장 긴 설명 문자열인 `MINING_SPEED`/`LUMBERJACK_SPEED`의 "채굴/벌목 속도 증가 (3포인트마다 Haste +1)").

## v1.6.8 실기 테스트 현황 (2026-08-09 기준)
- ✅ **`/gathercraft test auto` 신규 10개 항목 실기 확인 완료**: `gradlew runClient`로 실제 접속해 실행한 결과 기존 5개 + 신규 10개 총 15개 항목 전부 `§a✔` 통과 확인(요약 박스·`readSourceFile()`의 `../src/main/java/...` 경로 포함 정상 동작).
  - (참고: 동일 코드/설정으로 `gradlew runServer` 데디케이티드 서버 부팅 자체는 정상이었으나, 이 모드는 `PacketHandler`가 `NetworkRegistry` 채널을 엄격한 버전 매칭으로 등록해 모드 미설치 클라이언트(예: 자동화용 vanilla 프로토콜 봇)의 접속을 원천 차단한다 — 의도된 정상 동작이며, 그래서 실기 검증은 항상 모드가 설치된 실제 클라이언트(`runClient`)로 사람이 직접 확인해야 한다.)
- ⏳ **연쇄 벌목 재진입 가드의 "중복 지급 없음" 실동작 확인 미완료**: `/gathercraft test auto`는 `IS_CHAIN_FELLING` 필드 존재 여부만 리플렉션으로 확인하며, 100레벨에서 실제 연쇄 벌목(20% 확률)을 유도해 로그 개수만큼만(중복 없이) XP가 지급되는지는 아직 실기로 확인하지 않았다. 다음 세션에서 `/gathercraft test lumberjack 100`으로 레벨 세팅 후 큰 나무 군락을 벌목해 XP 적립량을 눈으로 검증 필요.

## v1.6.7 실기 테스트 미완료 (2026-08-08 기준)
- **섬손 재설치 후 재채굴 시 XP 미지급 확인**: 광석/원목을 캐서 다시 설치한 뒤 재채굴했을 때 XP/퀘스트/업적/드롭이 실제로 지급되지 않는지 확인. `/gathercraft test mining <레벨>` 등으로 레벨 세팅 후 검증.

## v1.6.6 실기 테스트 미완료 (2026-08-08 기준)
SkillPointStat 13개 † 스탯 활성화는 코드 레벨 검증 + 빌드 성공까지만 이번 세션에서 진행했고, 실기 테스트는 하지 못했다. 다음 세션에서 `/gathercraft test <skill> <level>`로 레벨을 세팅하고 스킬 포인트를 해당 스탯에 투자한 뒤 체감 확인이 필요하다.
- `MINING_SPEED`/`LUMBERJACK_SPEED`: Haste amplifier가 실제로 +1 올라가는지(3포인트=0.09 누적 시)
- `MINING_XP_BONUS`/`COOKING_SATURATION`: XP/포화도 수치가 정확히 곱·덧셈되는지
- `FARMING_GROWTH`: 주변 플레이어 중 최고 레벨(`bestPlayer`) 판정이 의도대로 동작하는지(여러 플레이어가 같이 있을 때)
- `COOKING_EXTRA_BUFF`: 80레벨 미만에서만 체감 효과가 있어야 함(80레벨 이상은 배열 길이 3 제한으로 무효과가 정상)

## v1.6.5 실기 테스트 현황 (2026-07-30 기준)

`gradlew runClient` 로컬 개발 클라이언트로 진행. 크래시/예외 없음(넷티 리플렉션 경고는 무해, 항상 발생).

- ✅ **칭호 채팅 표시**: 로그에서 `[각성 사냥꾼] <Dev> 메시지` 형태로 정상 출력 확인됨 (콘솔 로그가 CP949로 인코딩돼 `iconv`/PowerShell `GetEncoding(949)`로 디코딩해서 확인).
- ⏳ **낚시 속도 버그 수정 재검증 미완료**: `timeUntilLured` 기반으로 재작성 후 컴파일·크래시 없음만 확인, 20/40/70레벨 대기시간 단축 체감 비교는 세션 종료로 못함. v1.6.3 당시 70레벨(50%)은 체감됨, 20/40레벨(10%/25%)은 체감이 약했던 전례가 있어 다음 세션에서 `/gathercraft test reset` 후 `/gathercraft test fishing <20|40|70>`으로 재확인 필요.
- ⏳ **칭호 이름표(필드) 표시 미검증**: `TitleNameTagRenderer`는 v1.5.1부터 있던 기존 기능이라 코드는 존재하지만, 이번 세션엔 멀티플레이(다른 계정)로 접속해 실제로 확인하지 못함. LAN 공개(`로컬 게임을 [2222]번 포트에서 호스트합니다` 로그)까지는 시도했으나 두 번째 클라이언트 접속 전에 세션 종료됨.

---

# 작업 규칙 (Claude Code 지침)
1. **개선사항 자동 구현**: 작업 완료 후 발견한 개선사항(데드코드 제거, 성능 최적화, 버그 예방 등 범위가 작은 것)은 사용자에게 다시 묻지 않고 바로 구현한다. 구현 후 완료 보고에 포함한다.
2. **대규모 업데이트 전**: 여러 파일에 걸친 신규 기능 추가 등 큰 변경은 보고서를 먼저 정리해서 사용자 승인 후 구현한다.
3. **작업 완료 후**: 이 CLAUDE.md 파일에 변경사항을 반영하여 업데이트한다.
4. **SkillPointStat 사용 금지**: 신규 기능은 반드시 레벨 기반 로직(`SkillData.getLevel()`)만 사용한다.
