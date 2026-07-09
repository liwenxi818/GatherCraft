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
- ❌ 20레벨: 뼛가루 1개로 2회 효과
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
- ❌ 20레벨: 낚시 속도 10% 증가
- ✅ 30레벨: 희귀 아이템 드롭 확률 증가 (junk 제거 누적)
- ⚠️ 30/60/90레벨: 낚시 대기시간 단축 (20%/40%/65%) — 원래 스펙(20/40/70레벨)과 레벨 다름, EntityJoinLevelEvent + 리플렉션 방식
- ❌ 40레벨: 낚시 속도 25% 증가 (30레벨에서 구현)
- ✅ 50레벨: 인챈트된 낚싯대 효과 강화 (Lure+LuckOfSea 1레벨당 10% 추가 대기시간 감소, 최대 50%)
- ✅ 60레벨: 물고기(COD) 추가 드롭 25%
- ❌ 70레벨: 낚시 속도 50% 증가 (60레벨에서 구현)
- ✅ 80레벨: 보물(BOOK) 드롭 확률 대폭 증가 (15%)
- ✅ 90레벨: 쓰레기 드롭 완전 제거
- ✅ 100레벨 각성: 5% 확률로 각성의 낚시대 획득 (LuckOfSea III + Lure III + Unbreaking III + Mending)

## 요리 (Cooking)
- ⚠️ 경험치: 화로/훈연기/용광로 제련 완료 시 `5 × 스택 수` XP — ItemSmeltedEvent 발동 여부 서버 환경 확인 필요
- ✅ 10레벨: 버프 지속시간 +10%
- ✅ 20레벨: 음식마다 고유 버프 (소고기/돼지고기/양→힘, 닭/토끼→속도, 대구/연어→수중호흡, 감자/빵→흡수, 파이/케이크→재생)
- ✅ 30레벨: 버프 강도 증가 (60레벨 이상 amplifier +1)
- ⚠️ 30/60/90레벨: 추가 포화도 (+0.4/+1.0/+2.0) — 원래 스펙(40/70레벨)과 레벨 다름
- ❌ 40레벨: 음식 포화도 20% 증가 (30레벨에서 구현)
- ⚠️ 50/80/100레벨: 추가 버프 확률 (15%/30%/50%) — 원래 스펙(2개/3개 동시 적용)과 다른 방식
- ✅ 60레벨: 버프 지속시간 +50%
- ❌ 70레벨: 음식 포화도 50% 증가 (90레벨에서 구현)
- ❌ 80레벨: 버프 3개 동시 적용 (확률 방식으로 대체)
- ❌ 90레벨: 모든 음식 버프 강도 최대
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
- **SkillPointStat 사용 금지**: 신규 기능은 레벨 기반 로직만 사용

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
- `/gathercraft test auto` — 인게임 자동 테스트 실행 (XP 공식·광물/몹 XP·NBT·PlayerClone 검증, OP 2)
- `/gathercraft quest claim <0|1|2>` — 퀘스트 보상 수령 (권한 제한 없음)
- `/gathercraft giveboard` — 퀘스트 게시판 블록 지급 (OP 2)
- `/gathercraft achievement claim <id>` — 업적 보상 수령 (권한 제한 없음)
- `/tpaccept`, `/tpdeny` — TPA 요청 수락/거절 (권한 제한 없음, 채팅 클릭 메시지의 RUN_COMMAND 대상)

# 빌드
```bash
./gradlew clean build
```
결과물: `build/libs/gathercraft-1.6.2.jar`

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

---

# ❌ 미구현 목록 (Known TODO)

## 농사
- 20레벨: 뼛가루 1개로 2회 효과 (현재: 즉시 완숙으로 대체 구현)

## 낚시
- 20/40레벨: 낚시 속도 증가 (현재: 30/60/90레벨로 구현)

## 요리
- 40/70레벨: 정확한 스펙 레벨의 포화도 증가 (현재: 30/60/90레벨로 구현)
- ItemSmeltedEvent: 서버 환경에서 발동 여부 실제 테스트 필요

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

## TPA 채팅 클릭 메시지의 한계
`TpaAskPacket` 수신 시 뜨는 클릭형 채팅 메시지(`§a[수락]`/`§c[거절]`)는 바닐라 `ClickEvent`가 `RUN_COMMAND`(`/tpaccept`, `/tpdeny`)만 지원하는 제약상 **채팅창(T/Enter로 연 상태)이 열려 있을 때만 클릭이 히트테스트된다.** 평상시 자동으로 사라지는 채팅 HUD는 클릭에 반응하지 않는 바닐라 공통 제약이다.
이를 보완하기 위해 `TpaAskPacket.handle()`은 채팅 메시지와 동시에 `TpaRequestScreen` 팝업(0.5초 딜레이, 다른 화면이 열려 있으면 재대기)도 함께 띄운다. 팝업 버튼이 `TpaResponsePacket`의 실질적인 발신 경로이며, 채팅 클릭 경로는 `/tpaccept`·`/tpdeny` 명령어를 통해 별도로 동작한다.

---

# 작업 규칙 (Claude Code 지침)
1. **개선사항 자동 구현**: 작업 완료 후 발견한 개선사항(데드코드 제거, 성능 최적화, 버그 예방 등 범위가 작은 것)은 사용자에게 다시 묻지 않고 바로 구현한다. 구현 후 완료 보고에 포함한다.
2. **대규모 업데이트 전**: 여러 파일에 걸친 신규 기능 추가 등 큰 변경은 보고서를 먼저 정리해서 사용자 승인 후 구현한다.
3. **작업 완료 후**: 이 CLAUDE.md 파일에 변경사항을 반영하여 업데이트한다.
4. **SkillPointStat 사용 금지**: 신규 기능은 반드시 레벨 기반 로직(`SkillData.getLevel()`)만 사용한다.
