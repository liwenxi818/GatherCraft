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
│   └── GatherCraftCommand.java   # /gathercraft test ...
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
│       └── DamageTextPacket.java         # 부유 데미지 텍스트 전송 (S2C, ID 6)
├── client/
│   ├── ClientSetup.java          # 오버레이/키바인딩 등록 (modBus, 클라이언트 전용)
│   ├── gui/
│   │   ├── SkillBookScreen.java  # 스킬 책 GUI (3x3 그리드, 티어색상, XP바, 툴팁)
│   │   └── SkillPointScreen.java # 레벨업 스탯 선택 팝업 (3개 버튼, 0.5초 딜레이, 누적 툴팁)
│   ├── keybinding/
│   │   ├── KeyBindings.java      # R키 대시 KeyMapping 정의
│   │   └── ClientKeyHandler.java # forgeBus ClientTickEvent → 패킷 전송, 바닐라 XP바 숨김
│   └── overlay/
│       ├── DamageFlashOverlay.java  # 화면 가장자리 빨간 플래시 렌더링
│       ├── SkillBarOverlay.java     # 원형 쿨타임 바 + SkillSlotEntry
│       ├── SkillXpBarOverlay.java   # 화면 상단 스킬 XP 바 (색상/레벨업/티어업 애니메이션)
│       └── FloatingCombatText.java  # 부유 전투 텍스트 (데미지/HP, RenderLevelStageEvent)
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
        ├── PlayerTickHandler.java   # 채광/벌목 Haste, 각성 아이템 DIG_SPEED, 방어 속성, 대시 잔상, 로그인/로그아웃/리스폰/Clone 처리
        └── SkillBookHandler.java    # 핫바 8번 슬롯 스킬 책 상시 유지
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

# 명령어
- `/skill` — 전체 스킬 현황
- `/skill <name>` — 특정 스킬 상세 (한국어/영어명 모두 가능)
- `/gathercraft test <skill> <level>` — 특정 스킬 레벨 설정 (OP 2)
- `/gathercraft test all <level>` — 전체 스킬 레벨 설정 (OP 2)
- `/gathercraft test reset` — 전체 스킬 초기화 (OP 2)
- `/gathercraft test auto` — 인게임 자동 테스트 실행 (XP 공식·광물/몹 XP·NBT·PlayerClone 검증, OP 2)

# 빌드
```bash
./gradlew clean build
```
결과물: `build/libs/gathercraft-1.3.0.jar`

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
| v1.3.0 | IsDashing 고착 버그 수정, 채광 각성 연쇄 이벤트 방지(ThreadLocal), 즉사 데미지 텍스트 수정, 대시 키 레이블 동적화, NBT 틱 읽기 최적화(dashingPlayers Set) |

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

# 작업 규칙 (Claude Code 지침)
1. **개선사항 자동 구현**: 작업 완료 후 발견한 개선사항(데드코드 제거, 성능 최적화, 버그 예방 등 범위가 작은 것)은 사용자에게 다시 묻지 않고 바로 구현한다. 구현 후 완료 보고에 포함한다.
2. **대규모 업데이트 전**: 여러 파일에 걸친 신규 기능 추가 등 큰 변경은 보고서를 먼저 정리해서 사용자 승인 후 구현한다.
3. **작업 완료 후**: 이 CLAUDE.md 파일에 변경사항을 반영하여 업데이트한다.
4. **SkillPointStat 사용 금지**: 신규 기능은 반드시 레벨 기반 로직(`SkillData.getLevel()`)만 사용한다.
