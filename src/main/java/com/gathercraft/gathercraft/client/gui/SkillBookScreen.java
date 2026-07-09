package com.gathercraft.gathercraft.client.gui;

import com.gathercraft.gathercraft.achievement.AchievementClientCache;
import com.gathercraft.gathercraft.achievement.AchievementManager;
import com.gathercraft.gathercraft.network.PacketHandler;
import com.gathercraft.gathercraft.network.packet.AchievementClaimPacket;
import com.gathercraft.gathercraft.network.packet.QuestClaimPacket;
import com.gathercraft.gathercraft.network.packet.TitleEquipPacket;
import com.gathercraft.gathercraft.network.packet.TpaRequestPacket;
import com.gathercraft.gathercraft.network.packet.WaypointDeletePacket;
import com.gathercraft.gathercraft.network.packet.WaypointSavePacket;
import com.gathercraft.gathercraft.network.packet.WaypointTeleportPacket;
import com.gathercraft.gathercraft.quest.QuestClientCache;
import com.gathercraft.gathercraft.quest.QuestData;
import com.gathercraft.gathercraft.skill.SkillData;
import com.gathercraft.gathercraft.skill.SkillTier;
import com.gathercraft.gathercraft.skill.SkillType;
import com.gathercraft.gathercraft.title.Title;
import com.gathercraft.gathercraft.title.TitleClientCache;
import com.gathercraft.gathercraft.waypoint.WaypointClientCache;
import com.gathercraft.gathercraft.waypoint.WaypointData;
import com.gathercraft.gathercraft.waypoint.WaypointManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
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

    // ---- 탭 UI ----
    private int activeTab = 0; // 0=스킬 현황, 1=웨이포인트, 2=칭호, 3=텔포, 4=퀘스트, 5=업적
    private int tabsY;

    private static final String[] TAB_LABELS = {"스킬 현황", "웨이포인트", "칭호", "텔포", "퀘스트", "업적"};
    private static final int TAB_W = 90;
    private static final int TAB_H = 24;

    // ---- 웨이포인트 탭 레이아웃 ----
    private static final String[] ICON_KEYS = {"home", "village", "end", "nether", "custom"};
    private static final int SWATCH_SIZE = 20;
    private static final int SWATCH_GAP = 6;

    private static final int WP_PANEL_W = 380;
    private static final int ROW_H = 22;
    private static final int ROW_GAP = 3;
    private static final int BTN_TP_W = 44;
    private static final int BTN_DEL_W = 44;
    private static final int BTN_H2 = 18;
    private static final int SAVE_BTN_W = 130;
    private static final int SAVE_BTN_H = 20;

    // ---- 칭호 탭 레이아웃 ----
    private static final int TITLE_PANEL_W = 400;
    private static final int TITLE_ROW_H = 20;
    private static final int TITLE_ROW_GAP = 2;
    private static final int TITLE_VISIBLE_ROWS = 8;
    private int titleScrollOffset = 0;

    // ---- 텔포 탭 레이아웃 ----
    private static final int MAX_VISIBLE_TPA_ROWS = 8;
    private static final int TPA_BTN_W = 90;
    private static final int TPA_BTN_H = 18;

    // ---- 퀘스트 탭 레이아웃 ----
    private static final int QUEST_PANEL_W = 420;
    private static final int QUEST_ROW_H = 40;
    private static final int QUEST_ROW_GAP = 8;
    private static final int CLAIM_BTN_W = 100;
    private static final int CLAIM_BTN_H = 18;

    // ---- 업적 탭 레이아웃 ----
    private static final int ACH_PANEL_W = 420;
    private static final int ACH_ROW_H = 22; // 2줄 + 여백
    private static final int ACH_ROW_GAP = 2;
    private static final int ACH_VISIBLE_ROWS = 8;
    private static final int ACH_CLAIM_BTN_W = 90;
    private static final int ACH_CLAIM_BTN_H = 14;
    private int achievementScrollOffset = 0;

    private EditBox nameEditBox;
    private String selectedIcon = "home";
    private String errorMessage = "";

    public SkillBookScreen() {
        this(0);
    }

    public SkillBookScreen(int initialTab) {
        super(Component.literal("스킬 현황"));
        this.activeTab = initialTab;
    }

    /** 클라이언트에서 직접 호출 */
    public static void open() {
        open(0);
    }

    /** 특정 탭을 초기 탭으로 지정해 연다 (퀘스트 게시판 우클릭 시 퀘스트 탭으로 오픈). */
    public static void open(int initialTab) {
        Minecraft.getInstance().setScreen(new SkillBookScreen(initialTab));
    }

    @Override
    protected void init() {
        int totalW = COLS * SLOT_W + (COLS - 1) * SLOT_PAD;
        int totalH = ROWS * SLOT_H + (ROWS - 1) * SLOT_PAD;
        gridX = (width - totalW) / 2;
        gridY = (height - totalH) / 2 - 10;

        tabsY = gridY - 46;

        int panelX = (width - WP_PANEL_W) / 2;
        int swatchY = tabsY + TAB_H + 14;
        int nameBoxY = swatchY + SWATCH_SIZE + 6;
        nameEditBox = new EditBox(font, panelX, nameBoxY, 220, 18, Component.literal("웨이포인트 이름"));
        nameEditBox.setMaxLength(20);
        nameEditBox.setVisible(activeTab == 1);
        addRenderableWidget(nameEditBox);
    }

    private void setActiveTab(int tab) {
        if (activeTab == tab) return;
        activeTab = tab;
        boolean wpTab = (activeTab == 1);
        nameEditBox.setVisible(wpTab);
        if (!wpTab) {
            nameEditBox.setFocused(false);
            nameEditBox.setValue("");
        }
        errorMessage = "";
        titleScrollOffset = 0;
        achievementScrollOffset = 0;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (activeTab == 5) {
            int maxScroll = Math.max(0, buildAchievementRows().size() - ACH_VISIBLE_ROWS);
            achievementScrollOffset = clampScroll(achievementScrollOffset, delta, maxScroll);
            return true;
        }
        if (activeTab == 2) {
            int totalRows = (Title.values().length + 1) / 2;
            int maxScroll = Math.max(0, totalRows - TITLE_VISIBLE_ROWS);
            titleScrollOffset = clampScroll(titleScrollOffset, delta, maxScroll);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    /** 휠 위(양수 delta) = 이전 항목으로, 휠 아래(음수 delta) = 다음 항목으로 정확히 1행씩 이동. */
    private static int clampScroll(int current, double delta, int maxScroll) {
        int step = delta > 0 ? -1 : (delta < 0 ? 1 : 0);
        return Math.max(0, Math.min(current + step, maxScroll));
    }

    /** 스크롤바 트랙 + 핸들 렌더링 (콘텐츠 영역 우측). */
    private void renderScrollbar(GuiGraphics g, int rightEdgeX, int top, int visibleHeight, int totalRows, int visibleRows, int scrollOffset) {
        if (totalRows <= visibleRows) return;
        int barX = rightEdgeX + 4;
        g.fill(barX, top, barX + 3, top + visibleHeight, 0x44FFFFFF);

        int maxScroll = totalRows - visibleRows;
        float ratio = maxScroll > 0 ? (float) scrollOffset / maxScroll : 0f;
        int handleHeight = Math.max(10, visibleHeight * visibleRows / totalRows);
        int handleY = top + (int) ((visibleHeight - handleHeight) * ratio);
        g.fill(barX, handleY, barX + 3, handleY + handleHeight, 0xCCFFFFFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 반투명 어두운 전체 배경
        graphics.fill(0, 0, width, height, 0xAA000000);

        renderTabs(graphics, mouseX, mouseY);

        switch (activeTab) {
            case 0 -> renderSkillTab(graphics, mouseX, mouseY);
            case 1 -> {
                drawTabTitle(graphics, "§6§l웨이포인트");
                renderWaypointTab(graphics, mouseX, mouseY);
            }
            case 2 -> {
                drawTabTitle(graphics, "§6§l칭호");
                renderTitleTab(graphics, mouseX, mouseY);
            }
            case 3 -> {
                drawTabTitle(graphics, "§6§l텔포");
                renderTpaTab(graphics, mouseX, mouseY);
            }
            case 4 -> {
                drawTabTitle(graphics, "§6§l퀘스트");
                renderQuestTab(graphics, mouseX, mouseY);
            }
            case 5 -> {
                drawTabTitle(graphics, "§6§l업적");
                renderAchievementTab(graphics, mouseX, mouseY);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawTabTitle(GuiGraphics g, String title) {
        int titleX = (width - font.width(title)) / 2;
        g.drawString(font, title, titleX, gridY - 20, 0xFFFFFF, true);
    }

    private void renderSkillTab(GuiGraphics graphics, int mouseX, int mouseY) {
        drawTabTitle(graphics, "§6§l스킬 현황");

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
    }

    private void renderTabs(GuiGraphics g, int mouseX, int mouseY) {
        int tabsTotalW = TAB_LABELS.length * TAB_W;
        int tabsX = (width - tabsTotalW) / 2;
        for (int i = 0; i < TAB_LABELS.length; i++) {
            int tx = tabsX + i * TAB_W;
            boolean active = (activeTab == i);
            boolean hovered = mouseX >= tx && mouseX < tx + TAB_W && mouseY >= tabsY && mouseY < tabsY + TAB_H;
            int bg = active ? 0xEE2A3A5A : (hovered ? 0xCC1A2A4A : 0x991A1A2A);
            g.fill(tx, tabsY, tx + TAB_W, tabsY + TAB_H, bg);
            int borderColor = active ? 0xFFFFD700 : 0xFF555555;
            g.fill(tx, tabsY, tx + TAB_W, tabsY + 1, borderColor);
            g.fill(tx, tabsY + TAB_H - 1, tx + TAB_W, tabsY + TAB_H, borderColor);
            String label = TAB_LABELS[i];
            int lx = tx + (TAB_W - font.width(label)) / 2;
            int ly = tabsY + (TAB_H - font.lineHeight) / 2;
            g.drawString(font, label, lx, ly, active ? 0xFFFFD700 : 0xFFAAAAAA, true);
        }
    }

    private static int iconColor(String icon) {
        return switch (icon) {
            case "home" -> 0xFFFF55;
            case "village" -> 0x55FF55;
            case "end" -> 0xAA00AA;
            case "nether" -> 0xFF5555;
            case "custom" -> 0x5555FF;
            default -> 0xFFFFFF;
        };
    }

    private static String dimensionBadgeText(String dim) {
        return switch (dim) {
            case "minecraft:overworld" -> "오버월드";
            case "minecraft:the_nether" -> "네더";
            case "minecraft:the_end" -> "엔드";
            default -> dim;
        };
    }

    private static int dimensionBadgeColor(String dim) {
        return switch (dim) {
            case "minecraft:overworld" -> 0x55FF55;
            case "minecraft:the_nether" -> 0xFF5555;
            case "minecraft:the_end" -> 0xAA00AA;
            default -> 0xAAAAAA;
        };
    }

    private void renderWaypointTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelX = (width - WP_PANEL_W) / 2;
        int swatchY = tabsY + TAB_H + 14;

        // 아이콘 선택 스와치 5개
        for (int i = 0; i < ICON_KEYS.length; i++) {
            int sx = panelX + i * (SWATCH_SIZE + SWATCH_GAP);
            boolean selected = ICON_KEYS[i].equals(selectedIcon);
            g.fill(sx, swatchY, sx + SWATCH_SIZE, swatchY + SWATCH_SIZE, 0xFF000000 | iconColor(ICON_KEYS[i]));
            int b = selected ? 0xFFFFFFFF : 0xFF000000;
            int t = selected ? 2 : 1;
            g.fill(sx, swatchY, sx + SWATCH_SIZE, swatchY + t, b);
            g.fill(sx, swatchY + SWATCH_SIZE - t, sx + SWATCH_SIZE, swatchY + SWATCH_SIZE, b);
            g.fill(sx, swatchY, sx + t, swatchY + SWATCH_SIZE, b);
            g.fill(sx + SWATCH_SIZE - t, swatchY, sx + SWATCH_SIZE, swatchY + SWATCH_SIZE, b);
        }

        // nameEditBox는 addRenderableWidget으로 등록되어 super.render()가 자동으로 그림

        int saveBtnX = panelX + WP_PANEL_W - SAVE_BTN_W;
        int saveBtnY = swatchY + SWATCH_SIZE + 26;
        boolean saveHovered = mouseX >= saveBtnX && mouseX < saveBtnX + SAVE_BTN_W
                           && mouseY >= saveBtnY && mouseY < saveBtnY + SAVE_BTN_H;
        g.fill(saveBtnX, saveBtnY, saveBtnX + SAVE_BTN_W, saveBtnY + SAVE_BTN_H, saveHovered ? 0xCC2A5A2A : 0x991A3A1A);
        g.drawString(font, "§a현재 위치 저장", saveBtnX + 6, saveBtnY + (SAVE_BTN_H - font.lineHeight) / 2, 0xFFFFFF, true);

        int msgY = saveBtnY + SAVE_BTN_H + 4;
        if (!errorMessage.isEmpty()) {
            g.drawString(font, "§c" + errorMessage, panelX, msgY, 0xFFFF5555, false);
        }

        List<WaypointData> waypoints = WaypointClientCache.get();
        String countText = "§7[" + waypoints.size() + "/" + WaypointManager.MAX_WAYPOINTS + "] 저장됨";
        g.drawString(font, countText, panelX + WP_PANEL_W - font.width(countText), msgY, 0xFFAAAAAA, false);

        int listTopY = msgY + 14;
        g.fill(panelX, listTopY, panelX + WP_PANEL_W, listTopY + 1, 0x55FFFFFF);

        int rowY = listTopY + 6;
        if (waypoints.isEmpty()) {
            g.drawString(font, "§7저장된 웨이포인트가 없습니다.", panelX, rowY, 0xFF888888, false);
        }
        for (WaypointData waypoint : waypoints) {
            renderWaypointRow(g, panelX, rowY, waypoint, mouseX, mouseY);
            rowY += ROW_H + ROW_GAP;
        }
    }

    private void renderWaypointRow(GuiGraphics g, int panelX, int rowY, WaypointData wp, int mouseX, int mouseY) {
        boolean rowHovered = mouseX >= panelX && mouseX < panelX + WP_PANEL_W && mouseY >= rowY && mouseY < rowY + ROW_H;
        g.fill(panelX, rowY, panelX + WP_PANEL_W, rowY + ROW_H, rowHovered ? 0x552A2A3A : 0x331A1A2A);

        g.fill(panelX + 2, rowY + 3, panelX + 18, rowY + 19, 0xFF000000 | iconColor(wp.icon));
        g.drawString(font, wp.name, panelX + 24, rowY + (ROW_H - font.lineHeight) / 2, 0xFFFFFFFF, false);

        String badge = "[" + dimensionBadgeText(wp.dimension) + "]";
        g.drawString(font, badge, panelX + 150, rowY + (ROW_H - font.lineHeight) / 2,
            0xFF000000 | dimensionBadgeColor(wp.dimension), false);

        int tpX = panelX + WP_PANEL_W - BTN_TP_W - BTN_DEL_W - 8;
        int tpY = rowY + (ROW_H - BTN_H2) / 2;
        boolean tpHovered = mouseX >= tpX && mouseX < tpX + BTN_TP_W && mouseY >= tpY && mouseY < tpY + BTN_H2;
        g.fill(tpX, tpY, tpX + BTN_TP_W, tpY + BTN_H2, tpHovered ? 0xCC2A4A2A : 0x992A3A2A);
        g.drawString(font, "§a이동", tpX + 6, tpY + 4, 0xFFFFFFFF, false);

        int delX = panelX + WP_PANEL_W - BTN_DEL_W;
        boolean delHovered = mouseX >= delX && mouseX < delX + BTN_DEL_W && mouseY >= tpY && mouseY < tpY + BTN_H2;
        g.fill(delX, tpY, delX + BTN_DEL_W, tpY + BTN_H2, delHovered ? 0xCC5A2A2A : 0x993A1A1A);
        g.drawString(font, "§c삭제", delX + 6, tpY + 4, 0xFFFFFFFF, false);
    }

    private void trySaveWaypoint() {
        String name = nameEditBox.getValue().trim();
        if (name.isEmpty()) {
            errorMessage = "이름을 입력하세요";
            return;
        }
        errorMessage = "";
        PacketHandler.sendToServer(new WaypointSavePacket(name, selectedIcon));
        nameEditBox.setValue("");
    }

    // ---- 칭호 탭 좌표 헬퍼 (render/click 양쪽에서 재사용) ----
    private int titlePanelX() {
        return (width - TITLE_PANEL_W) / 2;
    }

    private int titleListTopY() {
        return tabsY + TAB_H + 30;
    }

    private int titleColX(int col) {
        return titlePanelX() + col * (TITLE_PANEL_W / 2);
    }

    private int titleRowY(int row) {
        return titleListTopY() + row * (TITLE_ROW_H + TITLE_ROW_GAP);
    }

    private void renderTitleTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelX = titlePanelX();
        String equipped = TitleClientCache.getEquipped();
        String equippedText = "§7현재 칭호: §f" + (equipped.isEmpty() ? "없음" : titleDisplayName(equipped));
        g.drawString(font, equippedText, panelX, tabsY + TAB_H + 10, 0xFFAAAAAA, false);

        List<String> unlocked = TitleClientCache.getUnlocked();
        Title[] titles = Title.values();
        int totalRows = (titles.length + 1) / 2;
        int listTopY = titleListTopY();
        int visibleH = TITLE_VISIBLE_ROWS * (TITLE_ROW_H + TITLE_ROW_GAP);

        g.enableScissor(panelX, listTopY, panelX + TITLE_PANEL_W, listTopY + visibleH);
        int start = titleScrollOffset;
        int end = Math.min(start + TITLE_VISIBLE_ROWS, totalRows);
        for (int row = start; row < end; row++) {
            int visualRow = row - start;
            if (row < titles.length) {
                renderTitleRow(g, titleColX(0), titleRowY(visualRow), titles[row], unlocked, equipped, mouseX, mouseY);
            }
            int idx1 = row + totalRows;
            if (idx1 < titles.length) {
                renderTitleRow(g, titleColX(1), titleRowY(visualRow), titles[idx1], unlocked, equipped, mouseX, mouseY);
            }
        }
        g.disableScissor();

        renderScrollbar(g, panelX + TITLE_PANEL_W, listTopY, visibleH, totalRows, TITLE_VISIBLE_ROWS, titleScrollOffset);
    }

    private void renderTitleRow(GuiGraphics g, int rx, int ry, Title title, List<String> unlocked, String equipped,
                                 int mouseX, int mouseY) {
        int rowW = TITLE_PANEL_W / 2 - 10;
        boolean owned = unlocked.contains(title.id);
        boolean isEquipped = owned && title.id.equals(equipped);
        boolean hovered = owned && mouseX >= rx && mouseX < rx + rowW && mouseY >= ry && mouseY < ry + TITLE_ROW_H;

        int bg = isEquipped ? 0x4DFFD700 : (hovered ? 0x552A2A3A : 0x331A1A2A);
        g.fill(rx, ry, rx + rowW, ry + TITLE_ROW_H, bg);

        String text = owned
            ? title.displayName + (isEquipped ? " §6[착용중]" : "")
            : "§8[???] §7(" + title.conditionText() + ")";
        g.drawString(font, text, rx + 4, ry + (TITLE_ROW_H - font.lineHeight) / 2, 0xFFFFFFFF, false);
    }

    private static String titleDisplayName(String id) {
        Title title = Title.byId(id);
        return title != null ? title.displayName : "";
    }

    // ---- 텔포 탭 좌표 헬퍼 (render/click 양쪽에서 재사용) ----
    private int tpaPanelX() {
        return (width - WP_PANEL_W) / 2;
    }

    private int tpaListTopY() {
        return tabsY + TAB_H + 30;
    }

    private int tpaRowY(int row) {
        return tpaListTopY() + row * (ROW_H + ROW_GAP);
    }

    private static List<PlayerInfo> onlinePlayersExcludingSelf() {
        List<PlayerInfo> result = new ArrayList<>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return result;
        for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
            if (mc.player != null && info.getProfile().getId().equals(mc.player.getUUID())) continue;
            result.add(info);
        }
        return result;
    }

    private void renderTpaTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelX = tpaPanelX();
        List<PlayerInfo> players = onlinePlayersExcludingSelf();

        String headerText = "§7온라인 플레이어 목록 (" + players.size() + "명)";
        g.drawString(font, headerText, panelX, tabsY + TAB_H + 10, 0xFFAAAAAA, false);

        if (players.isEmpty()) {
            g.drawString(font, "§7현재 온라인 플레이어가 없습니다.", panelX, tpaListTopY(), 0xFF888888, false);
            return;
        }

        int shown = Math.min(players.size(), MAX_VISIBLE_TPA_ROWS);
        for (int i = 0; i < shown; i++) {
            renderTpaRow(g, panelX, tpaRowY(i), players.get(i), mouseX, mouseY);
        }
        if (players.size() > MAX_VISIBLE_TPA_ROWS) {
            String more = "§7...외 " + (players.size() - MAX_VISIBLE_TPA_ROWS) + "명 더 접속 중";
            g.drawString(font, more, panelX, tpaRowY(shown), 0xFF888888, false);
        }
    }

    private void renderTpaRow(GuiGraphics g, int panelX, int rowY, PlayerInfo info, int mouseX, int mouseY) {
        boolean rowHovered = mouseX >= panelX && mouseX < panelX + WP_PANEL_W && mouseY >= rowY && mouseY < rowY + ROW_H;
        g.fill(panelX, rowY, panelX + WP_PANEL_W, rowY + ROW_H, rowHovered ? 0x552A2A3A : 0x331A1A2A);

        String name = info.getProfile().getName();
        g.drawString(font, name, panelX + 6, rowY + (ROW_H - font.lineHeight) / 2, 0xFFFFFFFF, false);

        int btnX = panelX + WP_PANEL_W - TPA_BTN_W;
        int btnY = rowY + (ROW_H - TPA_BTN_H) / 2;
        boolean btnHovered = mouseX >= btnX && mouseX < btnX + TPA_BTN_W && mouseY >= btnY && mouseY < btnY + TPA_BTN_H;
        g.fill(btnX, btnY, btnX + TPA_BTN_W, btnY + TPA_BTN_H, btnHovered ? 0xCC2A4A2A : 0x992A3A2A);
        g.drawString(font, "§a텔포 요청", btnX + 6, btnY + 4, 0xFFFFFFFF, false);
    }

    // ---- 퀘스트 탭 좌표 헬퍼 (render/click 양쪽에서 재사용) ----
    private int questPanelX() {
        return (width - QUEST_PANEL_W) / 2;
    }

    private int questListTopY() {
        return tabsY + TAB_H + 30;
    }

    private int questRowY(int row) {
        return questListTopY() + row * (QUEST_ROW_H + QUEST_ROW_GAP);
    }

    private static String questDifficultyBadge(long rewardXP) {
        if (rewardXP >= 2000) return "§c[어려움]";
        if (rewardXP >= 500) return "§e[보통]";
        return "§a[쉬움]";
    }

    private void renderQuestTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelX = questPanelX();
        g.drawString(font, "§e오늘의 퀘스트 §7(게시판 우클릭으로 갱신)", panelX, tabsY + TAB_H + 10, 0xFFAAAAAA, false);

        List<QuestData> quests = QuestClientCache.get();
        if (quests.isEmpty()) {
            g.drawString(font, "§7퀘스트 게시판을 우클릭해 오늘의 퀘스트를 받아보세요.",
                panelX, questListTopY(), 0xFF888888, false);
        }
        for (int i = 0; i < quests.size(); i++) {
            renderQuestRow(g, panelX, questRowY(i), quests.get(i), mouseX, mouseY);
        }

        g.drawString(font, "§7다음 갱신: 게시판 우클릭 시 갱신됩니다",
            panelX, questRowY(quests.size()) + 6, 0xFF888888, false);
    }

    private void renderQuestRow(GuiGraphics g, int panelX, int rowY, QuestData quest, int mouseX, int mouseY) {
        boolean rowHovered = mouseX >= panelX && mouseX < panelX + QUEST_PANEL_W
            && mouseY >= rowY && mouseY < rowY + QUEST_ROW_H;
        g.fill(panelX, rowY, panelX + QUEST_PANEL_W, rowY + QUEST_ROW_H, rowHovered ? 0x552A2A3A : 0x331A1A2A);

        String badge = questDifficultyBadge(quest.rewardXP);
        g.drawString(font, badge + " §f" + quest.description, panelX + 6, rowY + 4, 0xFFFFFFFF, false);

        String progressText = "§7[" + quest.progress + "/" + quest.goal + "]";
        g.drawString(font, progressText, panelX + 6, rowY + 18, 0xFFAAAAAA, false);

        if (!quest.completed) {
            g.drawString(font, "§7진행 중", panelX + QUEST_PANEL_W - 70, rowY + 18, 0xFF888888, false);
        } else if (!quest.claimed) {
            int btnX = panelX + QUEST_PANEL_W - CLAIM_BTN_W - 6;
            int btnY = rowY + (QUEST_ROW_H - CLAIM_BTN_H) / 2;
            boolean btnHovered = mouseX >= btnX && mouseX < btnX + CLAIM_BTN_W
                && mouseY >= btnY && mouseY < btnY + CLAIM_BTN_H;
            g.fill(btnX, btnY, btnX + CLAIM_BTN_W, btnY + CLAIM_BTN_H, btnHovered ? 0xCC2A5A2A : 0x991A3A1A);
            g.drawString(font, "§a보상 수령", btnX + 6, btnY + 4, 0xFFFFFFFF, false);
        } else {
            g.drawString(font, "§8✔ 완료", panelX + QUEST_PANEL_W - 70, rowY + 18, 0xFF888888, false);
        }
    }

    private boolean handleQuestClick(double mouseX, double mouseY) {
        List<QuestData> quests = QuestClientCache.get();
        int panelX = questPanelX();
        for (int i = 0; i < quests.size(); i++) {
            QuestData quest = quests.get(i);
            if (!quest.completed || quest.claimed) continue;
            int rowY = questRowY(i);
            int btnX = panelX + QUEST_PANEL_W - CLAIM_BTN_W - 6;
            int btnY = rowY + (QUEST_ROW_H - CLAIM_BTN_H) / 2;
            if (mouseX >= btnX && mouseX < btnX + CLAIM_BTN_W && mouseY >= btnY && mouseY < btnY + CLAIM_BTN_H) {
                PacketHandler.sendToServer(new QuestClaimPacket(i));
                return true;
            }
        }
        return false;
    }

    // ---- 업적 탭 좌표 헬퍼 ----
    private int achPanelX() {
        return (width - ACH_PANEL_W) / 2;
    }

    private int achListTopY() {
        return tabsY + TAB_H + 30;
    }

    private static String achCategoryLabel(AchievementManager.Category category) {
        return switch (category) {
            case MINING -> "§6== 채광 ==";
            case HUNTING -> "§6== 사냥 ==";
            case LIVING -> "§6== 생활 ==";
            case ALL -> "§6== 종합 ==";
        };
    }

    /** 카테고리 구분선 + 업적을 하나의 스크롤 가능한 행 목록으로 평탄화한다. */
    private List<Object> buildAchievementRows() {
        List<Object> rows = new ArrayList<>();
        AchievementManager.Category lastCategory = null;
        for (AchievementManager.Achievement ach : AchievementManager.ALL) {
            if (ach.category() != lastCategory) {
                rows.add(ach.category());
                lastCategory = ach.category();
            }
            rows.add(ach);
        }
        return rows;
    }

    private void renderAchievementTab(GuiGraphics g, int mouseX, int mouseY) {
        int panelX = achPanelX();
        List<String> unlocked = AchievementClientCache.getUnlocked();
        List<String> claimed = AchievementClientCache.getClaimed();
        String header = "§6업적 [" + unlocked.size() + "/" + AchievementManager.ALL.size() + "]";
        g.drawString(font, header, panelX, tabsY + TAB_H + 10, 0xFFFFFFFF, true);

        List<Object> rows = buildAchievementRows();
        int listTopY = achListTopY();
        int rowStride = ACH_ROW_H + ACH_ROW_GAP;
        int visibleH = ACH_VISIBLE_ROWS * rowStride;

        g.enableScissor(panelX, listTopY, panelX + ACH_PANEL_W, listTopY + visibleH);
        int start = achievementScrollOffset;
        int end = Math.min(start + ACH_VISIBLE_ROWS, rows.size());
        AchievementManager.Achievement hoveredAch = null;
        for (int i = start; i < end; i++) {
            int rowY = listTopY + (i - start) * rowStride;
            Object row = rows.get(i);
            if (row instanceof AchievementManager.Category category) {
                g.drawString(font, achCategoryLabel(category), panelX, rowY + 4, 0xFFAAAAAA, false);
            } else if (row instanceof AchievementManager.Achievement ach) {
                renderAchievementRow(g, panelX, rowY, ach, unlocked, claimed, mouseX, mouseY);
                boolean rowHovered = mouseX >= panelX && mouseX < panelX + ACH_PANEL_W
                    && mouseY >= rowY && mouseY < rowY + ACH_ROW_H;
                if (rowHovered) hoveredAch = ach;
            }
        }
        g.disableScissor();

        renderScrollbar(g, panelX + ACH_PANEL_W, listTopY, visibleH, rows.size(), ACH_VISIBLE_ROWS, achievementScrollOffset);

        if (hoveredAch != null) {
            renderAchievementTooltip(g, hoveredAch, unlocked, claimed, mouseX, mouseY);
        }
    }

    private void renderAchievementRow(GuiGraphics g, int panelX, int rowY, AchievementManager.Achievement ach,
                                       List<String> unlocked, List<String> claimed, int mouseX, int mouseY) {
        boolean owned = unlocked.contains(ach.id());
        boolean isClaimed = claimed.contains(ach.id());
        int line1Y = rowY + 2;
        int line2Y = rowY + 12;

        if (owned && isClaimed) {
            g.drawString(font, "§f" + ach.displayName(), panelX + 4, line1Y, 0xFFFFFFFF, false);
            String status = "§8✔ 수령 완료";
            g.drawString(font, status, panelX + ACH_PANEL_W - font.width(status) - 4, line1Y, 0xFFFFFFFF, false);
            g.drawString(font, "§8" + ach.condition(), panelX + 4, line2Y, 0xFFFFFFFF, false);
            String reward = "§8보상: " + AchievementManager.getRewardText(ach.id());
            g.drawString(font, reward, panelX + ACH_PANEL_W - font.width(reward) - 4, line2Y, 0xFFFFFFFF, false);
        } else if (owned) {
            g.drawString(font, "§e" + ach.displayName(), panelX + 4, line1Y, 0xFFFFFFFF, false);
            int btnX = panelX + ACH_PANEL_W - ACH_CLAIM_BTN_W - 4;
            int btnY = line1Y - 1;
            boolean btnHovered = mouseX >= btnX && mouseX < btnX + ACH_CLAIM_BTN_W
                && mouseY >= btnY && mouseY < btnY + ACH_CLAIM_BTN_H;
            g.fill(btnX, btnY, btnX + ACH_CLAIM_BTN_W, btnY + ACH_CLAIM_BTN_H, btnHovered ? 0xCC2A5A2A : 0x991A3A1A);
            g.drawString(font, "§a[보상 수령]", btnX + 4, btnY + 3, 0xFFFFFFFF, false);
            g.drawString(font, "§7" + ach.condition(), panelX + 4, line2Y, 0xFFFFFFFF, false);
            String reward = "§6보상: " + AchievementManager.getRewardText(ach.id());
            g.drawString(font, reward, panelX + ACH_PANEL_W - font.width(reward) - 4, line2Y, 0xFFFFFFFF, false);
        } else if (ach.counterKey() != null) {
            g.drawString(font, "§7" + ach.displayName(), panelX + 4, line1Y, 0xFFFFFFFF, false);
            g.drawString(font, "§8" + ach.condition(), panelX + 4, line2Y, 0xFFFFFFFF, false);
            int cur = Math.min(AchievementClientCache.getCounter(ach.counterKey()), ach.counterGoal());
            String bar = progressBar(cur, ach.counterGoal());
            g.drawString(font, bar, panelX + ACH_PANEL_W - font.width(bar) - 4, line2Y, 0xFFFFFFFF, false);
        } else {
            g.drawString(font, "§8[???]", panelX + 4, line1Y, 0xFFFFFFFF, false);
            g.drawString(font, "§8조건을 달성하면 해금됩니다", panelX + 4, line2Y, 0xFFFFFFFF, false);
        }
    }

    /** "█████████░ n/goal" 형태의 10칸 진행도 바 문자열을 만든다. */
    private static String progressBar(int progress, int goal) {
        int filled = goal > 0 ? (int) (10.0 * Math.min(progress, goal) / goal) : 0;
        filled = Math.max(0, Math.min(10, filled));
        return "§a" + "█".repeat(filled) + "§8" + "█".repeat(10 - filled) + " §7" + progress + "/" + goal;
    }

    private void renderAchievementTooltip(GuiGraphics g, AchievementManager.Achievement ach,
                                           List<String> unlocked, List<String> claimed, int mouseX, int mouseY) {
        boolean owned = unlocked.contains(ach.id());
        boolean isClaimed = claimed.contains(ach.id());
        List<FormattedCharSequence> lines = new ArrayList<>();

        if (owned && isClaimed) {
            lines.add(Component.literal("§b" + ach.displayName()).getVisualOrderText());
            lines.add(Component.literal("§7" + ach.condition()).getVisualOrderText());
            lines.add(Component.literal("§6보상: " + AchievementManager.getRewardText(ach.id())).getVisualOrderText());
            lines.add(Component.literal("§a✔ 달성 및 수령 완료").getVisualOrderText());
        } else if (owned) {
            lines.add(Component.literal("§e" + ach.displayName()).getVisualOrderText());
            lines.add(Component.literal("§7" + ach.condition()).getVisualOrderText());
            lines.add(Component.literal("§6보상: " + AchievementManager.getRewardText(ach.id())).getVisualOrderText());
            lines.add(Component.literal("§a클릭하여 보상 수령").getVisualOrderText());
        } else if (ach.counterKey() != null) {
            lines.add(Component.literal("§7" + ach.displayName()).getVisualOrderText());
            lines.add(Component.literal("§8" + ach.condition()).getVisualOrderText());
            int cur = Math.min(AchievementClientCache.getCounter(ach.counterKey()), ach.counterGoal());
            lines.add(Component.literal("진행: §f" + cur + " §8/ §f" + ach.counterGoal()).getVisualOrderText());
            lines.add(Component.literal("§6보상: " + AchievementManager.getRewardText(ach.id())).getVisualOrderText());
        } else {
            lines.add(Component.literal("§8[???]").getVisualOrderText());
            lines.add(Component.literal("§8아직 해금되지 않은 업적입니다").getVisualOrderText());
        }

        g.renderTooltip(font, lines, mouseX, mouseY);
    }

    private boolean handleAchievementClick(double mouseX, double mouseY) {
        List<Object> rows = buildAchievementRows();
        List<String> unlocked = AchievementClientCache.getUnlocked();
        List<String> claimed = AchievementClientCache.getClaimed();
        int panelX = achPanelX();
        int listTopY = achListTopY();
        int rowStride = ACH_ROW_H + ACH_ROW_GAP;
        int start = achievementScrollOffset;
        int end = Math.min(start + ACH_VISIBLE_ROWS, rows.size());

        for (int i = start; i < end; i++) {
            if (!(rows.get(i) instanceof AchievementManager.Achievement ach)) continue;
            if (!unlocked.contains(ach.id()) || claimed.contains(ach.id())) continue;
            int rowY = listTopY + (i - start) * rowStride;
            int btnX = panelX + ACH_PANEL_W - ACH_CLAIM_BTN_W - 4;
            int btnY = rowY + 2 - 1;
            if (mouseX >= btnX && mouseX < btnX + ACH_CLAIM_BTN_W && mouseY >= btnY && mouseY < btnY + ACH_CLAIM_BTN_H) {
                PacketHandler.sendToServer(new AchievementClaimPacket(ach.id()));
                return true;
            }
        }
        return false;
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int tabsTotalW = TAB_LABELS.length * TAB_W;
            int tabsX = (width - tabsTotalW) / 2;
            for (int i = 0; i < TAB_LABELS.length; i++) {
                int tx = tabsX + i * TAB_W;
                if (mouseX >= tx && mouseX < tx + TAB_W && mouseY >= tabsY && mouseY < tabsY + TAB_H) {
                    setActiveTab(i);
                    return true;
                }
            }

            boolean consumed = switch (activeTab) {
                case 1 -> handleWaypointClick(mouseX, mouseY);
                case 2 -> handleTitleClick(mouseX, mouseY);
                case 3 -> handleTpaClick(mouseX, mouseY);
                case 4 -> handleQuestClick(mouseX, mouseY);
                case 5 -> handleAchievementClick(mouseX, mouseY);
                default -> false;
            };
            if (consumed) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean handleWaypointClick(double mouseX, double mouseY) {
        int panelX = (width - WP_PANEL_W) / 2;
        int swatchY = tabsY + TAB_H + 14;

        for (int i = 0; i < ICON_KEYS.length; i++) {
            int sx = panelX + i * (SWATCH_SIZE + SWATCH_GAP);
            if (mouseX >= sx && mouseX < sx + SWATCH_SIZE && mouseY >= swatchY && mouseY < swatchY + SWATCH_SIZE) {
                selectedIcon = ICON_KEYS[i];
                return true;
            }
        }

        int saveBtnX = panelX + WP_PANEL_W - SAVE_BTN_W;
        int saveBtnY = swatchY + SWATCH_SIZE + 26;
        if (mouseX >= saveBtnX && mouseX < saveBtnX + SAVE_BTN_W
         && mouseY >= saveBtnY && mouseY < saveBtnY + SAVE_BTN_H) {
            trySaveWaypoint();
            return true;
        }

        int msgY = saveBtnY + SAVE_BTN_H + 4;
        int listTopY = msgY + 14;
        List<WaypointData> waypoints = WaypointClientCache.get();
        int rowY = listTopY + 6;
        for (int i = 0; i < waypoints.size(); i++) {
            int tpX = panelX + WP_PANEL_W - BTN_TP_W - BTN_DEL_W - 8;
            int tpY = rowY + (ROW_H - BTN_H2) / 2;
            if (mouseX >= tpX && mouseX < tpX + BTN_TP_W && mouseY >= tpY && mouseY < tpY + BTN_H2) {
                PacketHandler.sendToServer(new WaypointTeleportPacket(i));
                Minecraft.getInstance().setScreen(null);
                return true;
            }
            int delX = panelX + WP_PANEL_W - BTN_DEL_W;
            if (mouseX >= delX && mouseX < delX + BTN_DEL_W && mouseY >= tpY && mouseY < tpY + BTN_H2) {
                PacketHandler.sendToServer(new WaypointDeletePacket(i));
                return true;
            }
            rowY += ROW_H + ROW_GAP;
        }
        return false;
    }

    private boolean handleTitleClick(double mouseX, double mouseY) {
        List<String> unlocked = TitleClientCache.getUnlocked();
        Title[] titles = Title.values();
        int totalRows = (titles.length + 1) / 2;
        int rowW = TITLE_PANEL_W / 2 - 10;

        int start = titleScrollOffset;
        int end = Math.min(start + TITLE_VISIBLE_ROWS, totalRows);
        for (int row = start; row < end; row++) {
            int ry = titleRowY(row - start);
            for (int col = 0; col < 2; col++) {
                int idx = row + col * totalRows;
                if (idx >= titles.length) continue;
                int rx = titleColX(col);
                if (mouseX >= rx && mouseX < rx + rowW && mouseY >= ry && mouseY < ry + TITLE_ROW_H) {
                    if (unlocked.contains(titles[idx].id)) {
                        PacketHandler.sendToServer(new TitleEquipPacket(titles[idx].id));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleTpaClick(double mouseX, double mouseY) {
        List<PlayerInfo> players = onlinePlayersExcludingSelf();
        int panelX = tpaPanelX();
        int shown = Math.min(players.size(), MAX_VISIBLE_TPA_ROWS);
        for (int i = 0; i < shown; i++) {
            int rowY = tpaRowY(i);
            int btnX = panelX + WP_PANEL_W - TPA_BTN_W;
            int btnY = rowY + (ROW_H - TPA_BTN_H) / 2;
            if (mouseX >= btnX && mouseX < btnX + TPA_BTN_W && mouseY >= btnY && mouseY < btnY + TPA_BTN_H) {
                PacketHandler.sendToServer(new TpaRequestPacket(players.get(i).getProfile().getName()));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (activeTab == 1 && nameEditBox.isFocused() && nameEditBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (activeTab == 1 && nameEditBox.isFocused() && nameEditBox.charTyped(codePoint, modifiers)) {
            errorMessage = "";
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
