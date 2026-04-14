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
- 스킬 데이터는 플레이어 NBT에 저장
- /skill 명령어로 현재 스킬 레벨 확인 가능

---

# 🌿 생활 스킬

## 채광 (Mining)
- 경험치: 광석 채굴 시 적립
- 10레벨: 추가 드롭 확률 5%
- 20레벨: Haste I 상시 적용
- 30레벨: 추가 드롭 확률 15%
- 40레벨: Haste II 상시 적용
- 50레벨: 희귀 광석(다이아, 에메랄드) 추가 드롭 확률 추가
- 60레벨: 추가 드롭 확률 30%
- 70레벨: 광석 채굴 시 경험치 오브 추가 드롭
- 80레벨: Haste III 상시 적용
- 90레벨: 추가 드롭 확률 50%
- 100레벨 각성: 채굴 시 15% 확률로 주변 3x3 범위 광석 동시 채굴

## 벌목 (Lumberjack)
- 경험치: 나무 원목 채굴 시 적립
- 10레벨: 원목 추가 드롭 5%
- 20레벨: 도끼 내구도 소모 20% 감소 ⚠️ 미구현
- 30레벨: 나뭇잎에서 사과/묘목 드롭 확률 증가 ⚠️ 미구현
- 40레벨: 원목 추가 드롭 20%
- 50레벨: 나무 채굴 시 자동으로 묘목 심기 (8종 지원)
- 60레벨: 도끼 내구도 소모 50% 감소 ⚠️ 미구현
- 70레벨: 원목 추가 드롭 35%
- 80레벨: 나뭇잎 채굴 속도 대폭 증가 ⚠️ 미구현
- 90레벨: 원목 추가 드롭 50%
- 100레벨 각성: 나무 1개 채굴 시 연결된 나무 전체 동시 채굴 (20% 확률, 최대 64블록)

## 농사 (Farming)
- 경험치: 완전히 자란 작물 수확 시 적립
- 10레벨: 작물 추가 드롭 5%
- 20레벨: 뼛가루 1개로 2회 효과 ⚠️ 미구현
- 30레벨: 작물 추가 드롭 15%
- 40레벨: 씨앗 자동 재식
- 50레벨: 희귀 작물 드롭 확률 ⚠️ 미구현
- 60레벨: 작물 추가 드롭 30%
- 70레벨: 뼛가루 1개로 3회 효과 ⚠️ 미구현
- 80레벨: 작물 성장 속도 증가 ⚠️ 미구현
- 90레벨: 작물 추가 드롭 50%
- 100레벨 각성: 수확 시 25% 확률로 주변 5x5 범위 작물 동시 수확

## 낚시 (Fishing)
- 경험치: 낚시로 아이템 획득 시 적립
- 10레벨: 쓰레기 드롭 확률 감소 (15%)
- 20레벨: 낚시 속도 10% 증가 ⚠️ 미구현
- 30레벨: 희귀 아이템 드롭 확률 증가 (junk 제거 30%)
- 40레벨: 낚시 속도 25% 증가 ⚠️ 미구현
- 50레벨: 인챈트된 낚싯대 효과 강화 ⚠️ 미구현
- 60레벨: 물고기(COD) 추가 드롭 25%
- 70레벨: 낚시 속도 50% 증가 ⚠️ 미구현
- 80레벨: 보물(BOOK) 드롭 확률 대폭 증가 (15%)
- 90레벨: 쓰레기 드롭 완전 제거
- 100레벨 각성: 5% 확률로 각성의 낚시대 획득 (LuckOfSea III + Lure III + Unbreaking III + Mending)

## 요리 (Cooking)
- 경험치: 화로/훈연기/용광로 제련 완료 시 적립 ⚠️ ItemSmeltedEvent 발동 여부 미확인
- 10레벨: 버프 지속시간 +10%
- 20레벨: 음식마다 고유 버프 (소고기→힘, 닭→속도, 생선→수중호흡, 감자/빵→흡수, 파이→재생)
- 30레벨: 버프 강도 증가 (amplifier +1, 60레벨 이상)
- 40레벨: 음식 포화도 20% 증가 ⚠️ 미구현
- 50레벨: 버프 2개 동시 적용 가능 ⚠️ 미구현
- 60레벨: 버프 지속시간 +50%
- 70레벨: 음식 포화도 50% 증가 ⚠️ 미구현
- 80레벨: 버프 3개 동시 적용 가능 ⚠️ 미구현
- 90레벨: 모든 음식 버프 강도 최대 ⚠️ 미구현
- 100레벨 각성: 음식 섭취 시 체력 +4 즉시 회복 + 모든 디버프 제거

---

# ⚔️ 전투 스킬

## 사냥 (Hunting)
- 경험치: 몬스터/슬라임 처치 시 적립 (20 XP)
- 10레벨: 공격력 +5%
- 20레벨: 크리티컬 확률 +5% (낙하 크리티컬 없을 때만 발동)
- 30레벨: 공격력 +15%
- 40레벨: 낙하 크리티컬 데미지 +20%
- 50레벨: 처치 시 20% 확률 체력 +2 회복
- 60레벨: 공격력 +30%
- 70레벨: 10% 확률 추가 뼈 드롭
- 80레벨: 크리티컬 확률 +20% (낙하 크리티컬 없을 때만 발동)
- 90레벨: 공격력 +50%
- 100레벨 각성: 5% 확률 즉사 공격
- **크리티컬 시스템**: 낙하 크리티컬(40레벨) OR 레벨 기반 크리티컬 — 둘 중 하나만 적용

## 방어 (Defense)
- 경험치: 몬스터에게 데미지 받을 시 적립 (2 XP)
- 10레벨: 데미지 감소 5%
- 20레벨: 넉백 저항 20%
- 30레벨: 데미지 감소 15%
- 40레벨: 체력 최대치 +4HP
- 50레벨: 몬스터 공격 10% 확률 무효화
- 60레벨: 데미지 감소 30%
- 70레벨: 체력 최대치 +8HP
- 80레벨: 독/화염 데미지 면역 (독 효과 자체 데미지만, 마녀 마법은 차단 안 함)
- 90레벨: 데미지 감소 50%
- 100레벨 각성: 치명타 데미지(6+ HP) 30% 확률 완전 무효화

---

# 🔨 제작 스킬

## 대장장이 (Smithing)
- 경험치: 철/금/다이아/네더라이트 도구 제작 시 15XP, 모루 수리 시 10XP
- 10레벨: 도구 내구도 +10% ⚠️ 미구현
- 20레벨: 모루 수리 경험치 비용 20% 감소 (환급 방식)
- 30레벨: 도구 내구도 +25% ⚠️ 미구현
- 40레벨: 제작 시 재료 절약 확률 15% (결과물 추가 지급)
- 50레벨: 도구 제작 시 랜덤 인챈트 1개 자동 부여
- 60레벨: 도구 내구도 +50% ⚠️ 미구현
- 70레벨: 모루 수리 경험치 비용 50% 감소 (환급 방식)
- 80레벨: 도구 내구도 +75% ⚠️ 미구현
- 90레벨: 제작 시 재료 절약 확률 40%
- 100레벨 각성: 제작한 도구에 고유 각성 인챈트 부여 ⚠️ 미구현

## 마법부여 (Enchanting)
- 경험치: 인챈트 테이블에서 인챈트 수행 시 (소비 레벨 × 5 XP, 5분 쿨다운)
- 10레벨: 인챈트 비용 경험치 10% 감소 ⚠️ 미구현
- 20레벨: 인챈트 레벨 보너스 +1
- 30레벨: 인챈트 비용 25% 감소 ⚠️ 미구현
- 40레벨: 저주 인챈트 면역 ⚠️ 미구현
- 50레벨: 인챈트 레벨 보너스 +3
- 60레벨: 인챈트 비용 50% 감소 ⚠️ 미구현
- 70레벨: 인챈트 시 추가 인챈트 부여 확률 ⚠️ 미구현
- 80레벨: 인챈트 레벨 보너스 +5
- 90레벨: 인챈트 비용 75% 감소 ⚠️ 미구현
- 100레벨 각성: 인챈트 시 최고 등급 인챈트 보장 ⚠️ 미구현

---

# 코딩 규칙
- DeferredRegister 패턴 사용
- 이벤트는 @SubscribeEvent 사용
- 스킬 데이터는 플레이어 NBT에 저장
- 성능 최적화 우선 (틱마다 연산 최소화)
- 레벨업 시 채팅 + 파티클 연출
- 파티클 스폰은 `ParticleUtil` 유틸 사용 (원형/폭발/색상 헬퍼)
- 클라이언트 전용 코드는 반드시 `@OnlyIn(Dist.CLIENT)` + `DistExecutor.unsafeRunWhenOn` 분리
- S2C 패킷은 `PacketHandler.sendToPlayer()` 사용
- 난수는 `ThreadLocalRandom.current()` 사용 (`new Random()` 금지)
- 레벨+XP 동시 갱신은 반드시 `SkillData.updateSkill()` 사용 (원자성 보장)

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
    ├── SkillData.java            # 플레이어 NBT 저장/로드 (getRoot/saveRoot/updateSkill/getStatValue)
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
        ├── PlayerTickHandler.java   # 채광 Haste, 방어 속성, 대시 잔상, 사망 데이터 복사
        └── SkillBookHandler.java    # 핫바 8번 슬롯 스킬 책 상시 유지
```

# 주요 API 패턴
- XP 적립: `SkillManager.addXP(player, SkillType.XXX, amount)`
- 레벨 조회: `SkillData.getLevel(player, skill)`
- 레벨+XP 원자 갱신: `SkillData.updateSkill(player, skill, level, xp)` ← 레벨업 시 사용
- 레벨 설정(테스트용): `SkillData.setLevel(player, skill, level)` + `SkillData.setXP(player, skill, 0)`
- XP 진행도: `SkillManager.getXPProgress(player, skill)` → 0.0~1.0
- 필요 XP 공식: `(level + 1) * 100` (1→2레벨: 200 XP, 99→100레벨: 10000 XP)
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

# 명령어
- `/skill` — 전체 스킬 현황
- `/skill <name>` — 특정 스킬 상세 (한국어/영어명 모두 가능)
- `/gathercraft test <skill> <level>` — 특정 스킬 레벨 설정 (OP 2)
- `/gathercraft test all <level>` — 전체 스킬 레벨 설정 (OP 2)
- `/gathercraft test reset` — 전체 스킬 초기화 (OP 2)

# 빌드
```bash
./gradlew build
```
결과물: `build/libs/gathercraft-0.1.0.jar`

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
| v1.2.0 | 버그 수정 및 최적화: SmithingHandler/EnchantingHandler 메모리 누수 수정, 방어 속성 즉시 갱신, DamageTextPacket 미사용 필드 제거, LumberjackHandler sapling 코드 정리 |

---

# 미구현 (Known TODO)
- 요리 XP: `ItemSmeltedEvent` 실제 발동 여부 확인 필요 (이벤트는 구현됨)
- 대장장이 내구도 보너스: PlayerTickHandler에 구현 필요 (10/30/60/80레벨)
- 대장장이 모루 비용 감소 UI: `AnvilUpdateEvent`에 플레이어 없어 실시간 슬롯 표시 불가 (환급 방식으로 구현됨)
- 낚시 속도 증가: 20/40/70레벨 (Forge에서 낚시 속도 직접 제어 방법 조사 필요)
- 농사 뼛가루 2~3회 효과: 20/70레벨
- 마법부여 인챈트 비용 감소: 10/30/60/90레벨
- 마법부여 저주 인챈트 면역: 40레벨
- 마법부여 추가 인챈트 확률: 70레벨
- 마법부여 100레벨 각성: 최고 등급 인챈트 보장

## 스탯 포인트 미구현 (†표시 스탯 — NBT 저장은 되나 효과 미적용)
- MINING_SPEED, MINING_XP_BONUS
- LUMBERJACK_DURABILITY, LUMBERJACK_SPEED
- FARMING_BONEMEAL, FARMING_GROWTH
- FISHING_SPEED
- COOKING_SATURATION, COOKING_EXTRA_BUFF
- SMITHING_DURABILITY
- ENCHANTING_COST_REDUCE, ENCHANTING_EXTRA, ENCHANTING_CURSE_IMMUNE

---

# 작업 규칙 (Claude Code 지침)
1. **개선사항 자동 구현**: 작업 완료 후 발견한 개선사항(데드코드 제거, 성능 최적화, 버그 예방 등 범위가 작은 것)은 사용자에게 다시 묻지 않고 바로 구현한다. 구현 후 완료 보고에 포함한다.
2. **대규모 업데이트 전**: 여러 파일에 걸친 신규 기능 추가 등 큰 변경은 보고서를 먼저 정리해서 사용자 승인 후 구현한다.
3. **작업 완료 후**: 이 CLAUDE.md 파일에 변경사항을 반영하여 업데이트한다.
