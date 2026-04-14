package com.gathercraft.gathercraft.client.gui;

import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillTier;
import com.gathercraft.gathercraft.skill.SkillType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 스킬 현황 GUI.
 * - 3x3 그리드, 스킬별 슬롯 (각 슬롯 56x72px)
 * - 슬롯 배경 티어별 색상
 * - 마우스 호버 시 다음 레벨 보너스 툴팁
 */
@OnlyIn(Dist.CLIENT)
public class SkillBookScreen extends Screen {

    private static final int SLOT_W = 56;
    private static final int SLOT_H = 72;
    private static final int SLOT_PAD = 6;
    private static final int COLS = 3;
    private static final int ROWS = 3;

    private static final SkillType[] SKILL_ORDER = {
        SkillType.MINING,    SkillType.LUMBERJACK, SkillType.FARMING,
        SkillType.FISHING,   SkillType.COOKING,    SkillType.HUNTING,
        SkillType.DEFENSE,   SkillType.SMITHING,   SkillType.ENCHANTING
    };

    // 서버에서 S2C 패킷으로 갱신되는 클라이언트 캐시
    private static final int[]   cachedLevel    = new int[SkillType.values().length];
    private static final float[] cachedProgress = new float[SkillType.values().length];

    static {
        Arrays.fill(cachedLevel, 1); // 기본값 Lv.1
    }

    /** SkillXpUpdatePacket 수신 시 호출 — GUI가 열려 있지 않아도 캐시를 갱신해둠 */
    public static void onXpUpdate(SkillType skill, int level, float progress) {
        cachedLevel[skill.ordinal()]    = level;
        cachedProgress[skill.ordinal()] = progress;
    }

    private int gridX, gridY;

    public SkillBookScreen() {
        super(Component.literal("스킬 현황"));
    }

    /** 클라이언트에서 직접 호출 */
    public static void open() {
        Minecraft.getInstance().setScreen(new SkillBookScreen());
    }

    @Override
    protected void init() {
        int totalW = COLS * SLOT_W + (COLS - 1) * SLOT_PAD;
        int totalH = ROWS * SLOT_H + (ROWS - 1) * SLOT_PAD;
        gridX = (width - totalW) / 2;
        gridY = (height - totalH) / 2 - 10;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 반투명 어두운 전체 배경
        graphics.fill(0, 0, width, height, 0xAA000000);

        // 제목
        String title = "§6§l스킬 현황";
        int titleX = (width - font.width(title)) / 2;
        graphics.drawString(font, title, titleX, gridY - 20, 0xFFFFFF, true);

        int hoveredSlot = -1;

        // 슬롯 렌더링
        for (int i = 0; i < SKILL_ORDER.length; i++) {
            int col = i % COLS;
            int row = i / COLS;
            int sx = gridX + col * (SLOT_W + SLOT_PAD);
            int sy = gridY + row * (SLOT_H + SLOT_PAD);

            SkillType skill = SKILL_ORDER[i];
            int level = cachedLevel[skill.ordinal()];
            SkillTier tier = SkillTier.fromLevel(level);
            float progress = cachedProgress[skill.ordinal()];

            boolean hovered = mouseX >= sx && mouseX < sx + SLOT_W
                            && mouseY >= sy && mouseY < sy + SLOT_H;
            if (hovered) hoveredSlot = i;

            renderSlot(graphics, sx, sy, skill, level, tier, progress, hovered);
        }

        // 툴팁 표시
        if (hoveredSlot >= 0) {
            SkillType skill = SKILL_ORDER[hoveredSlot];
            int level = cachedLevel[skill.ordinal()];
            renderTooltip(graphics, skill, level, mouseX, mouseY);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderSlot(GuiGraphics graphics, int sx, int sy,
                             SkillType skill, int level, SkillTier tier,
                             float progress, boolean hovered) {
        // 슬롯 배경 (티어 색상, 반투명)
        int tierColor = tier.color;
        int bgAlpha = hovered ? 0xCC : 0x88;
        int bgColor = (bgAlpha << 24) | (tierColor & 0xFFFFFF);
        graphics.fill(sx, sy, sx + SLOT_W, sy + SLOT_H, bgColor);

        // 테두리
        int borderColor = hovered ? (0xFF000000 | tierColor) : (0xAA000000 | tierColor);
        graphics.fill(sx, sy, sx + SLOT_W, sy + 1, borderColor);
        graphics.fill(sx, sy + SLOT_H - 1, sx + SLOT_W, sy + SLOT_H, borderColor);
        graphics.fill(sx, sy + 1, sx + 1, sy + SLOT_H - 1, borderColor);
        graphics.fill(sx + SLOT_W - 1, sy + 1, sx + SLOT_W, sy + SLOT_H - 1, borderColor);

        // 스킬 이름 (중앙 정렬)
        String nameText = skill.getKoreanName();
        int nameX = sx + (SLOT_W - font.width(nameText)) / 2;
        graphics.drawString(font, nameText, nameX, sy + 5, 0xFFFFFF, true);

        // 레벨 텍스트
        String levelText = "Lv." + level;
        int levelX = sx + (SLOT_W - font.width(levelText)) / 2;
        graphics.drawString(font, levelText, levelX, sy + 17, 0xFFFF55, true);

        // 티어명
        String tierText = tier.getDisplayName();
        int tierTextColor = 0xFF000000 | tier.textColor;
        int tierX = sx + (SLOT_W - font.width(tierText)) / 2;
        graphics.drawString(font, tierText, tierX, sy + 29, tierTextColor, false);

        // XP 진행 바 (슬롯 하단)
        int barX = sx + 3;
        int barY = sy + SLOT_H - 14;
        int barW = SLOT_W - 6;
        int barH = 4;

        // 바 배경
        graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF222222);

        // 바 채우기
        int fillW = (int)(barW * progress);
        if (fillW > 0) {
            int skillColor = skill.color;
            graphics.fill(barX, barY, barX + fillW, barY + barH, 0xFF000000 | skillColor);
            // 하이라이트
            graphics.fill(barX, barY, barX + fillW, barY + 1,
                0xFF000000 | blendColor(skillColor, 0xFFFFFF, 0.4f));
        }

        // XP 텍스트 (진행률 %)
        String xpText;
        if (level >= SkillData.MAX_LEVEL) {
            xpText = "MAX";
        } else {
            xpText = (int)(progress * 100) + "%";
        }
        int xpX = sx + (SLOT_W - font.width(xpText)) / 2;
        graphics.drawString(font, xpText, xpX, sy + SLOT_H - 24, 0xAAAAAA, false);
    }

    private void renderTooltip(GuiGraphics graphics, SkillType skill, int level, int mouseX, int mouseY) {
        List<FormattedCharSequence> lines = new ArrayList<>();
        lines.add(Component.literal("§6" + skill.getKoreanName() + " §f(Lv." + level + ")").getVisualOrderText());

        // 현재 레벨 보너스
        String currentBonus = getCurrentBonus(skill, level);
        if (!currentBonus.isEmpty()) {
            lines.add(Component.literal("§a현재: §f" + currentBonus).getVisualOrderText());
        }

        // 다음 보너스 레벨
        int nextBonusLevel = getNextBonusLevel(level);
        if (nextBonusLevel <= SkillData.MAX_LEVEL) {
            String nextBonus = getBonusAtLevel(skill, nextBonusLevel);
            lines.add(Component.literal("§e다음 보너스 (Lv." + nextBonusLevel + "): §f" + nextBonus).getVisualOrderText());
        } else {
            lines.add(Component.literal("§6§l최고 레벨 달성!").getVisualOrderText());
        }

        graphics.renderTooltip(font, lines, mouseX, mouseY);
    }

    private static int blendColor(int base, int overlay, float amount) {
        int rA = (base >> 16) & 0xFF;
        int gA = (base >> 8) & 0xFF;
        int bA = base & 0xFF;
        int rB = (overlay >> 16) & 0xFF;
        int gB = (overlay >> 8) & 0xFF;
        int bB = overlay & 0xFF;
        return (((int)(rA + (rB - rA) * amount)) << 16)
             | (((int)(gA + (gB - gA) * amount)) << 8)
             | ((int)(bA + (bB - bA) * amount));
    }

    // ---- 다음 보너스 레벨 계산 ----
    private static int getNextBonusLevel(int currentLevel) {
        int next = ((currentLevel / 10) + 1) * 10;
        return Math.min(next, SkillData.MAX_LEVEL);
    }

    // ---- 현재 레벨 보너스 문자열 ----
    private static String getCurrentBonus(SkillType skill, int level) {
        int bonusLevel = (level / 10) * 10;
        if (bonusLevel == 0) return "";
        return getBonusAtLevel(skill, bonusLevel);
    }

    // ---- 레벨별 보너스 설명 ----
    private static String getBonusAtLevel(SkillType skill, int level) {
        return switch (skill) {
            case MINING -> switch (level) {
                case 10  -> "추가 드롭 확률 5%";
                case 20  -> "Haste I 상시 적용";
                case 30  -> "추가 드롭 확률 15%";
                case 40  -> "Haste II 상시 적용";
                case 50  -> "희귀 광석 추가 드롭";
                case 60  -> "추가 드롭 확률 30%";
                case 70  -> "채굴 시 경험치 오브 드롭";
                case 80  -> "Haste III 상시 적용";
                case 90  -> "추가 드롭 확률 50%";
                case 100 -> "각성: 3x3 범위 동시 채굴";
                default  -> "";
            };
            case LUMBERJACK -> switch (level) {
                case 10  -> "원목 추가 드롭 5%";
                case 20  -> "도끼 내구도 소모 20% 감소";
                case 30  -> "나뭇잎 드롭 확률 증가";
                case 40  -> "원목 추가 드롭 20%";
                case 50  -> "자동 묘목 심기";
                case 60  -> "도끼 내구도 소모 50% 감소";
                case 70  -> "원목 추가 드롭 35%";
                case 80  -> "잎 채굴 속도 대폭 증가";
                case 90  -> "원목 추가 드롭 50%";
                case 100 -> "각성: 연결된 나무 연쇄 벌목";
                default  -> "";
            };
            case FARMING -> switch (level) {
                case 10  -> "작물 추가 드롭 5%";
                case 20  -> "뼛가루 1개로 2회 효과";
                case 30  -> "작물 추가 드롭 15%";
                case 40  -> "씨앗 자동 재식";
                case 50  -> "희귀 작물 드롭 확률";
                case 60  -> "작물 추가 드롭 30%";
                case 70  -> "뼛가루 1개로 3회 효과";
                case 80  -> "작물 성장 속도 증가";
                case 90  -> "작물 추가 드롭 50%";
                case 100 -> "각성: 5x5 범위 동시 수확";
                default  -> "";
            };
            case FISHING -> switch (level) {
                case 10  -> "쓰레기 드롭 확률 감소";
                case 20  -> "낚시 속도 10% 증가";
                case 30  -> "희귀 아이템 드롭 확률 증가";
                case 40  -> "낚시 속도 25% 증가";
                case 50  -> "인챈트된 낚싯대 효과 강화";
                case 60  -> "물고기 추가 드롭";
                case 70  -> "낚시 속도 50% 증가";
                case 80  -> "보물 드롭 확률 대폭 증가";
                case 90  -> "쓰레기 드롭 완전 제거";
                case 100 -> "각성: 희귀 커스텀 아이템 획득";
                default  -> "";
            };
            case COOKING -> switch (level) {
                case 10  -> "버프 지속시간 +10%";
                case 20  -> "음식 고유 버프 1개 추가";
                case 30  -> "버프 강도 증가";
                case 40  -> "음식 포화도 20% 증가";
                case 50  -> "버프 2개 동시 적용";
                case 60  -> "버프 지속시간 +50%";
                case 70  -> "음식 포화도 50% 증가";
                case 80  -> "버프 3개 동시 적용";
                case 90  -> "모든 음식 버프 강도 최대";
                case 100 -> "각성: 즉시 체력 회복 + 디버프 제거";
                default  -> "";
            };
            case HUNTING -> switch (level) {
                case 10  -> "공격력 +5%";
                case 20  -> "크리티컬 확률 +5%";
                case 30  -> "공격력 +15%, 추가 드롭 증가";
                case 40  -> "크리티컬 데미지 +20%";
                case 50  -> "처치 시 체력 회복";
                case 60  -> "공격력 +30%";
                case 70  -> "몬스터 희귀 드롭 확률 증가";
                case 80  -> "크리티컬 확률 +20%";
                case 90  -> "공격력 +50%";
                case 100 -> "각성: 일정 확률 즉사 공격";
                default  -> "";
            };
            case DEFENSE -> switch (level) {
                case 10  -> "데미지 감소 5%";
                case 20  -> "넉백 저항 증가";
                case 30  -> "데미지 감소 15%";
                case 40  -> "체력 최대치 +2";
                case 50  -> "데미지 받을 시 무효화 확률";
                case 60  -> "데미지 감소 30%";
                case 70  -> "체력 최대치 +4";
                case 80  -> "독/화염 데미지 면역";
                case 90  -> "데미지 감소 50%";
                case 100 -> "각성: 치명타 데미지 무효화 확률";
                default  -> "";
            };
            case SMITHING -> switch (level) {
                case 10  -> "도구 내구도 +10%";
                case 20  -> "모루 수리 경험치 비용 20% 감소";
                case 30  -> "도구 내구도 +25%";
                case 40  -> "제작 시 재료 절약 확률";
                case 50  -> "도구 제작 시 랜덤 인챈트";
                case 60  -> "도구 내구도 +50%";
                case 70  -> "모루 수리 경험치 비용 50% 감소";
                case 80  -> "도구 내구도 +75%";
                case 90  -> "재료 절약 확률 대폭 증가";
                case 100 -> "각성: 고유 각성 인챈트 부여";
                default  -> "";
            };
            case ENCHANTING -> switch (level) {
                case 10  -> "인챈트 비용 10% 감소";
                case 20  -> "인챈트 레벨 보너스 +1";
                case 30  -> "인챈트 비용 25% 감소";
                case 40  -> "저주 인챈트 면역";
                case 50  -> "인챈트 레벨 보너스 +3";
                case 60  -> "인챈트 비용 50% 감소";
                case 70  -> "추가 인챈트 부여 확률";
                case 80  -> "인챈트 레벨 보너스 +5";
                case 90  -> "인챈트 비용 75% 감소";
                case 100 -> "각성: 최고 등급 인챈트 보장";
                default  -> "";
            };
        };
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
