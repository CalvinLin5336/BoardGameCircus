package com.turing.view;

import com.turing.controller.UserController;
import com.turing.model.User;
import com.turing.dao.impl.GameConfigDaoImpl; 
import com.turing.dao.impl.ReportDaoImpl; 
import com.turing.util.DbUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.List;

/**
 * Dialog window for System Administrator console.
 * 🟢 營運終極完美大螢幕高清版：升級 1280x720 寬螢幕黃金比例，完美支援從 MainFrame 或 LobbyFrame 雙向開啟。
 */
public class AdminConsoleDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private final UserController userController;
    private final MainFrame parentFrame;

    private JTable tblUsers;
    private DefaultTableModel userTableModel;

    // CRUD Fields
    private JTextField txtId;
    private JTextField txtUsername;
    private JTextField txtPassword;
    private JComboBox<String> cmbRole;
    private JSpinner spinTokens;
    private JCheckBox chkBlocked;

    // Leaderboard Fields
    private JTable tblLeaderboard;
    private DefaultTableModel leaderboardTableModel;

    // 營運報表欄位組件
    private JTable tblReports;
    private DefaultTableModel reportTableModel;
    private JRadioButton rdoDaily;
    private JRadioButton rdoMonthly;

    private final GameConfigDaoImpl configDao = new GameConfigDaoImpl();

    // --- 現代風格色彩與字型定義 ---
    private static final Color COLOR_BG = new Color(248, 250, 252);
    private static final Color COLOR_CARD = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(30, 41, 59);
    private static final Color COLOR_GREEN = new Color(46, 175, 107);
    private static final Color COLOR_ORANGE = new Color(242, 156, 17);
    private static final Color COLOR_RED = new Color(220, 38, 38);
    private static final Color COLOR_TEXT = new Color(44, 62, 80);
    private static final Font FONT_MAIN = new Font("Microsoft JhengHei", Font.PLAIN, 12);
    private static final Font FONT_BOLD = new Font("Microsoft JhengHei", Font.BOLD, 13);
    private static final Font FONT_TITLE = new Font("Microsoft JhengHei", Font.BOLD, 20); 

    /**
     * 🟢 多載建構子 1：從遊戲主畫面 (MainFrame) 開啟的版本
     */
    public AdminConsoleDialog(MainFrame parent) {
        super(parent, "後台管理", true);
        this.parentFrame = parent;
        this.userController = new UserController();
        initUI();
    }

    /**
     * 🟢 🔥 多載建構子 2：專為從遊戲大廳 (LobbyFrame) 開啟打造的新版本！
     * 徹底消滅 "The constructor AdminConsoleDialog(LobbyFrame) is undefined" 編譯紅線！
     */
    public AdminConsoleDialog(LobbyFrame parentLobby) {
        super(parentLobby, "後台管理", true);
        this.parentFrame = null; // 從大廳開啟時，沒有 MainFrame 實體
        this.userController = new UserController();
        initUI();
    }

    /**
     * 🧠 核心排版提取：將原本構造函數內的 UI 渲染電路統一集中管理，免去代碼重複複製
     */
    private void initUI() {
        setSize(1280, 720); 
        setLocationRelativeTo(getParent()); // 自動對齊至父視窗中央
        setResizable(false);

        JPanel contentPane = new JPanel(new BorderLayout(12, 12));
        contentPane.setBackground(COLOR_BG);
        contentPane.setBorder(new EmptyBorder(18, 18, 18, 18));
        setContentPane(contentPane);

        // Header Panel
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_PRIMARY);
        pnlHeader.setBorder(new EmptyBorder(15, 22, 15, 20));
        
        JLabel lblTitle = new JLabel("後台管理");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);
        
        JLabel lblDesc = new JLabel("會員帳戶限制設定、規則獎勵閾值控制、不重複活躍用戶(DAU)與各難度解密通關拿幣率雙重統計分析");
        lblDesc.setFont(FONT_MAIN);
        lblDesc.setForeground(new Color(203, 213, 225));
        pnlHeader.add(lblDesc, BorderLayout.SOUTH);

        contentPane.add(pnlHeader, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(FONT_BOLD);
        tabbedPane.setBackground(COLOR_BG);
        
        tabbedPane.addTab(" 會員帳號管理與權限限制", createMemberManagementPanel());
        tabbedPane.addTab(" 代幣獎勵與限制條件設定", createConfigSettingsPanel());
        tabbedPane.addTab(" 全球富豪排行榜 (Top 10)", createLeaderboardPanel());
        tabbedPane.addTab(" 平台數據營運戰情報表", createReportPanel()); 

        contentPane.add(tabbedPane, BorderLayout.CENTER);

        refreshUserTable();
        refreshLeaderboard();
    }

    private JPanel createMemberManagementPanel() {
        JPanel pnl = new JPanel(new BorderLayout(12, 12));
        pnl.setBackground(COLOR_CARD);
        pnl.setBorder(new EmptyBorder(12, 12, 12, 12));

        userTableModel = new DefaultTableModel(
                new Object[]{"ID", "帳號", "密碼", "角色類型", "擁有代幣", "限制登入狀態"}, 0
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblUsers = new JTable(userTableModel);
        styleTable(tblUsers);
        JScrollPane scrollPane = new JScrollPane(tblUsers);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        pnl.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlEditor = new JPanel(new GridBagLayout());
        pnlEditor.setBackground(COLOR_BG);
        pnlEditor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), " 會員編輯視窗", TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, COLOR_PRIMARY
                )
        ));
        pnlEditor.setPreferredSize(new Dimension(360, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12); 
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        pnlEditor.add(new JLabel("ID (唯讀):"), gbc);
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setFont(FONT_MAIN);
        txtId.setPreferredSize(new Dimension(0, 28));
        gbc.gridx = 1;
        pnlEditor.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        pnlEditor.add(new JLabel("會員帳號:"), gbc);
        txtUsername = new JTextField();
        txtUsername.setFont(FONT_MAIN);
        txtUsername.setPreferredSize(new Dimension(0, 28));
        gbc.gridx = 1;
        pnlEditor.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        pnlEditor.add(new JLabel("密碼明文:"), gbc);
        txtPassword = new JTextField();
        txtPassword.setFont(FONT_MAIN);
        txtPassword.setPreferredSize(new Dimension(0, 28));
        gbc.gridx = 1;
        pnlEditor.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        pnlEditor.add(new JLabel("角色權限:"), gbc);
        cmbRole = new JComboBox<>(new String[]{"USER", "ADMIN"});
        cmbRole.setFont(FONT_MAIN);
        cmbRole.setBackground(Color.WHITE);
        cmbRole.setPreferredSize(new Dimension(0, 28));
        gbc.gridx = 1;
        pnlEditor.add(cmbRole, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        pnlEditor.add(new JLabel("代幣數量:"), gbc);
        spinTokens = new JSpinner(new SpinnerNumberModel(0, 0, 99999, 5));
        spinTokens.setFont(FONT_MAIN);
        spinTokens.setPreferredSize(new Dimension(0, 28));
        gbc.gridx = 1;
        pnlEditor.add(spinTokens, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        pnlEditor.add(new JLabel("限制登入:"), gbc);
        chkBlocked = new JCheckBox("封鎖該帳戶");
        chkBlocked.setFont(FONT_BOLD);
        chkBlocked.setOpaque(false);
        chkBlocked.setForeground(COLOR_RED);
        gbc.gridx = 1;
        pnlEditor.add(chkBlocked, gbc);

        JPanel pnlFormActions = new JPanel(new GridLayout(3, 2, 8, 8));
        pnlFormActions.setOpaque(false);

        JButton btnAdd = new ModernStyleButton("新增會員", COLOR_GREEN, true);
        JButton btnUpdate = new ModernStyleButton("編輯更新", COLOR_PRIMARY, true);
        JButton btnDelete = new ModernStyleButton("刪除用戶", COLOR_RED, true);
        JButton btnToggleBlock = new ModernStyleButton("快速封鎖", COLOR_ORANGE, true);
        JButton btnAddTokens = new ModernStyleButton("+50 代幣", COLOR_PRIMARY, false);
        JButton btnRefresh = new ModernStyleButton("重新整理", Color.GRAY, false);

        Dimension btnSize = new Dimension(0, 35);
        btnAdd.setPreferredSize(btnSize); btnUpdate.setPreferredSize(btnSize);
        btnDelete.setPreferredSize(btnSize); btnToggleBlock.setPreferredSize(btnSize);

        btnAdd.addActionListener(e -> handleAddUser());
        btnUpdate.addActionListener(e -> handleUpdateUser());
        btnDelete.addActionListener(e -> handleDeleteUser());
        btnToggleBlock.addActionListener(e -> handleToggleBlock());
        btnAddTokens.addActionListener(e -> handleQuickAddTokens());
        btnRefresh.addActionListener(e -> refreshUserTable());

        pnlFormActions.add(btnAdd);
        pnlFormActions.add(btnUpdate);
        pnlFormActions.add(btnToggleBlock);
        pnlFormActions.add(btnAddTokens);
        pnlFormActions.add(btnDelete);
        pnlFormActions.add(btnRefresh);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 5, 10);
        pnlEditor.add(pnlFormActions, gbc);

        pnl.add(pnlEditor, BorderLayout.EAST);

        tblUsers.getSelectionModel().addListSelectionListener(e -> {
            int row = tblUsers.getSelectedRow();
            if (row >= 0) {
                txtId.setText(userTableModel.getValueAt(row, 0).toString());
                txtUsername.setText(userTableModel.getValueAt(row, 1).toString());
                txtPassword.setText(userTableModel.getValueAt(row, 2).toString());
                cmbRole.setSelectedItem(userTableModel.getValueAt(row, 3).toString());
                spinTokens.setValue(Integer.parseInt(userTableModel.getValueAt(row, 4).toString()));
                chkBlocked.setSelected(userTableModel.getValueAt(row, 5).toString().contains("BLOCKED"));
            }
        });

        return pnl;
    }

    private JPanel createConfigSettingsPanel() {
        JPanel pnlMain = new JPanel(new BorderLayout(15, 15));
        pnlMain.setBackground(COLOR_CARD);
        pnlMain.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTabbedPane subTabbedPane = new JTabbedPane();
        subTabbedPane.setFont(FONT_BOLD);

        subTabbedPane.addTab("簡單模式 (Easy)", createDifficultySubPanel('A'));
        subTabbedPane.addTab("標準模式 (Standard)", createDifficultySubPanel('B'));
        subTabbedPane.addTab("困難模式 (Hard)", createDifficultySubPanel('C'));

        pnlMain.add(subTabbedPane, BorderLayout.CENTER);
        return pnlMain;
    }

    private JPanel createDifficultySubPanel(char diffChar) {
        JPanel pnlWrapper = new JPanel(new GridBagLayout());
        pnlWrapper.setBackground(COLOR_CARD);

        JPanel pnlCard = new JPanel(new GridLayout(3, 2, 20, 25)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pnlCard.setOpaque(false);
        pnlCard.setBorder(new EmptyBorder(30, 40, 30, 40));
        pnlCard.setPreferredSize(new Dimension(860, 320)); 

        JLabel lblLimit = new JLabel("規定解鎖回合數限制 (Limit Rounds):");
        lblLimit.setFont(FONT_BOLD);
        pnlCard.add(lblLimit);

        JTextField txtLimit = new JTextField();
        txtLimit.setFont(FONT_BOLD);
        txtLimit.setHorizontalAlignment(SwingConstants.CENTER);
        pnlCard.add(txtLimit);

        JLabel lblReward = new JLabel("獎勵發放代幣數量 (N 枚代幣):");
        lblReward.setFont(FONT_BOLD);
        pnlCard.add(lblReward);

        JTextField txtTokens = new JTextField();
        txtTokens.setFont(FONT_BOLD);
        txtTokens.setHorizontalAlignment(SwingConstants.CENTER);
        pnlCard.add(txtTokens);

        int currentLimit = configDao.getConfigValue(diffChar, "reward_rounds_limit");
        int currentTokens = configDao.getConfigValue(diffChar, "reward_tokens");
        txtLimit.setText(String.valueOf(currentLimit));
        txtTokens.setText(String.valueOf(currentTokens));

        JButton btnSaveConfig = new ModernStyleButton("儲存代幣規則變更", COLOR_PRIMARY, true);
        btnSaveConfig.addActionListener(e -> {
            String limitStr = txtLimit.getText().trim();
            String tokensStr = txtTokens.getText().trim();

            if (limitStr.isEmpty() || tokensStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "欄位值不得為空！", "規則設定", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int limitInt = Integer.parseInt(limitStr);
                int tokensInt = Integer.parseInt(tokensStr);

                if (limitInt <= 0 || tokensInt < 0) {
                    throw new NumberFormatException();
                }

                configDao.updateConfig(diffChar, limitInt, tokensInt);
                JOptionPane.showMessageDialog(this, "難度 [" + diffChar + "] 獎勵規則更新成功！\n" +
                        "變更後：凡是在「" + limitInt + "」回合內成功找出密碼者，可獲得「" + tokensInt + "」枚代幣獎勵。");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "請輸入大於 0 的正整數限制！", "輸入不合法", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "儲存失敗: " + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        });
        pnlCard.add(btnSaveConfig);

        JButton btnResetConfig = new ModernStyleButton("回復預設值", Color.GRAY, false);
        btnResetConfig.addActionListener(e -> {
            if (diffChar == 'A') { txtLimit.setText("3"); txtTokens.setText("5"); }
            else if (diffChar == 'B') { txtLimit.setText("5"); txtTokens.setText("10"); }
            else if (diffChar == 'C') { txtLimit.setText("7"); txtTokens.setText("20"); }
        });
        pnlCard.add(btnResetConfig);

        GridBagConstraints wrapperGbc = new GridBagConstraints();
        wrapperGbc.gridx = 0; wrapperGbc.gridy = 0;
        wrapperGbc.weightx = 1.0; wrapperGbc.weighty = 1.0;
        pnlWrapper.add(pnlCard, wrapperGbc);

        return pnlWrapper;
    }

    private JPanel createLeaderboardPanel() {
        JPanel pnl = new JPanel(new BorderLayout(12, 12));
        pnl.setBackground(COLOR_CARD);
        pnl.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblInfo = new JLabel("全球富豪排行榜：目前富豪一般會員代幣排名統計 (前 10 名)", SwingConstants.CENTER);
        lblInfo.setFont(FONT_BOLD);
        lblInfo.setForeground(COLOR_PRIMARY);
        pnl.add(lblInfo, BorderLayout.NORTH);

        leaderboardTableModel = new DefaultTableModel(
                new Object[]{"排名 Rank", "玩家帳號", "身分角色", "現有代幣 (Coins)"}, 0
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblLeaderboard = new JTable(leaderboardTableModel);
        styleTable(tblLeaderboard);
        JScrollPane scrollPane = new JScrollPane(tblLeaderboard);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        pnl.add(scrollPane, BorderLayout.CENTER);

        JButton btnRefreshRank = new ModernStyleButton("重新整理排行榜", COLOR_GREEN, true);
        btnRefreshRank.setPreferredSize(new Dimension(0, 36));
        btnRefreshRank.addActionListener(e -> refreshLeaderboard());
        pnl.add(btnRefreshRank, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createReportPanel() {
        JPanel pnl = new JPanel(new BorderLayout(12, 12));
        pnl.setBackground(COLOR_CARD);
        pnl.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlControl.setOpaque(false);
        pnlControl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel lblFilter = new JLabel("報表週期切換:");
        lblFilter.setFont(FONT_BOLD);
        pnlControl.add(lblFilter);

        rdoDaily = new JRadioButton("日報表 (Daily Log)", true);
        rdoMonthly = new JRadioButton("月報表 (Monthly Summary)");
        rdoDaily.setFont(FONT_MAIN);
        rdoMonthly.setFont(FONT_MAIN);
        rdoDaily.setOpaque(false);
        rdoMonthly.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(rdoDaily);
        group.add(rdoMonthly);
        pnlControl.add(rdoDaily);
        pnlControl.add(rdoMonthly);

        JButton btnRefreshReport = new ModernStyleButton("整理並刷新數據", COLOR_PRIMARY, true);
        pnlControl.add(btnRefreshReport);
        pnl.add(pnlControl, BorderLayout.NORTH);

        reportTableModel = new DefaultTableModel(
                new Object[]{"統計週期", "平台總場次", "產出代幣總額", "A簡單通關率與代幣效率", "B標準通關率與代幣效率", "C困難通關率與代幣效率", "新註冊用戶", "不重複活躍玩家"}, 0
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblReports = new JTable(reportTableModel);
        styleTable(tblReports);
        
        tblReports.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        int[] columnWidths = {110, 90, 100, 260, 260, 260, 90, 110};

        for (int i = 0; i < columnWidths.length; i++) {
            if (i < tblReports.getColumnModel().getColumnCount()) {
                tblReports.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
            }
        }

        JScrollPane scrollPane = new JScrollPane(tblReports);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        pnl.add(scrollPane, BorderLayout.CENTER);

        ActionListener reportSwitchListener = e -> refreshReportTable();
        rdoDaily.addActionListener(reportSwitchListener);
        rdoMonthly.addActionListener(reportSwitchListener);
        btnRefreshReport.addActionListener(reportSwitchListener);

        SwingUtilities.invokeLater(this::refreshReportTable);

        return pnl;
    }

    private void refreshReportTable() {
        reportTableModel.setRowCount(0);
        if (DbUtil.isOfflineMode()) return;

        try {
            ReportDaoImpl reportDao = new ReportDaoImpl();
            List<Object[]> data = rdoDaily.isSelected() ? reportDao.getDailyReports() : reportDao.getMonthlyReports();

            for (Object[] row : data) {
                int aGames = (int) row[5]; int aWins = (int) row[6];
                int bGames = (int) row[7]; int bWins = (int) row[8];
                int cGames = (int) row[9]; int cWins = (int) row[10];

                int aRewards = (int) row[11];
                int bRewards = (int) row[12];
                int cRewards = (int) row[13];

                // 🧠 A 簡單：通關率與拿幣率換算
                String aRateStr = "";
                if (aGames > 0) {
                    double winRate = (aWins * 100.0 / aGames);
                    String coinRate = aWins > 0 ? String.format("%.1f%% (%d/%d)", (aRewards * 100.0 / aWins), aRewards, aWins) : "0.0%";
                    aRateStr = String.format("%.1f%% (%d/%d)  拿幣: %s", winRate, aWins, aGames, coinRate);
                }

                // 🧠 B 標準：通關率與拿幣率換算
                String bRateStr = "";
                if (bGames > 0) {
                    double winRate = (bWins * 100.0 / bGames);
                    String coinRate = bWins > 0 ? String.format("%.1f%% (%d/%d)", (bRewards * 100.0 / bWins), bRewards, bWins) : "0.0%";
                    bRateStr = String.format("%.1f%% (%d/%d)  拿幣: %s", winRate, bWins, bGames, coinRate);
                }

                // 🧠 C 困難：通關率與拿幣率換算
                String cRateStr = "";
                if (cGames > 0) {
                    double winRate = (cWins * 100.0 / cGames);
                    String coinRate = cWins > 0 ? String.format("%.1f%% (%d/%d)", (cRewards * 100.0 / cWins), cRewards, cWins) : "0.0%";
                    cRateStr = String.format("%.1f%% (%d/%d)  拿幣: %s", winRate, cWins, cGames, coinRate);
                }

                reportTableModel.addRow(new Object[]{
                    rdoDaily.isSelected() ? "" + row[0] : "" + row[0] + " 月份",
                    row[3] + " 場",
                    "" + row[4],
                    aRateStr, 
                    bRateStr, 
                    cRateStr, 
                    row[1] + " 人",
                    row[2] + " 人"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "讀取營運報表與拿幣率失敗: " + e.getMessage(), "營運錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshUserTable() {
        userTableModel.setRowCount(0);
        try {
            List<User> users = userController.getAllUsers();
            for (User u : users) {
                userTableModel.addRow(new Object[]{
                        u.getId(), u.getUsername(), u.getPassword(), u.getRole(), u.getTokens(),
                        u.isBlocked() ? "BLOCKED (限制登入)" : "ACTIVE (允許登入)"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "讀取會員列表失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshLeaderboard() {
        leaderboardTableModel.setRowCount(0);
        try {
            List<User> list = userController.getLeaderboard();
            int rank = 1;
            for (User u : list) {
                String medal = "";
                if (rank == 1) medal = "";
                else if (rank == 2) medal = "";
                else if (rank == 3) medal = "";
                
                leaderboardTableModel.addRow(new Object[]{
                        medal + "第 " + rank + " 名", u.getUsername(), u.getRole(), u.getTokens() + " 枚"
                });
                rank++;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "讀取排行榜失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddUser() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        int tokens = (int) spinTokens.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請完整填寫會員帳號與密碼欄位！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            userController.createUser(username, password, role, tokens);
            JOptionPane.showMessageDialog(this, "會員帳號「" + username + "」新增成功！");
            refreshUserTable(); refreshLeaderboard();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "新增失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateUser() {
        String idStr = txtId.getText();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先在表格中選擇要編輯的會員！", "編輯更新", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String role = (String) cmbRole.getSelectedItem();
        int tokens = (int) spinTokens.getValue();
        boolean blocked = chkBlocked.isSelected();

        try {
            userController.updateUser(id, username, password, role, tokens, blocked);
            JOptionPane.showMessageDialog(this, "會員「" + username + "」編輯更新成功！");
            refreshUserTable(); refreshLeaderboard();
            
            // 🟢 🔥 核心防護修正：當從大廳（LobbyFrame）打開此面板時，parentFrame 會是 null，需加上防空判斷避免 NullPointerException
            if (parentFrame != null) {
                parentFrame.updateCurrentUserHeader(); 
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "編輯更新失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteUser() {
        String idStr = txtId.getText();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先在表格中選擇要刪除的會員！", "刪除會員", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String username = txtUsername.getText();

        if ("admin".equals(username)) {
            JOptionPane.showMessageDialog(this, "系統管理員「admin」為最高權限根帳號，禁止刪除！", "權限不允許", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, 
            "⚠️ 確定要永久刪除會員帳號「" + username + "」嗎？此動作將無法復原！", 
            "刪除確認", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                userController.deleteUser(id);
                JOptionPane.showMessageDialog(this, "會員「" + username + "」已成功刪除！");
                txtId.setText(""); txtUsername.setText(""); txtPassword.setText("");
                refreshUserTable(); refreshLeaderboard();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "刪除失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleToggleBlock() {
        String idStr = txtId.getText();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先在表格中選擇要切換封鎖狀態的會員！", "限制登入", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String username = txtUsername.getText();
        boolean currentBlocked = chkBlocked.isSelected();

        if ("admin".equals(username)) {
            JOptionPane.showMessageDialog(this, "最高權限管理員「admin」禁止封鎖！", "權限不允許", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            userController.toggleUserBlockStatus(id, !currentBlocked);
            JOptionPane.showMessageDialog(this, "會員「" + username + "」限制登入狀態切換成功！");
            refreshUserTable();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "狀態切換失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleQuickAddTokens() {
        String idStr = txtId.getText();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請先在表格中選擇要發放代幣的會員！", "調整代幣", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = Integer.parseInt(idStr);
        String username = txtUsername.getText();
        int currentTokens = (int) spinTokens.getValue();

        try {
            userController.adjustUserTokens(id, currentTokens + 50);
            JOptionPane.showMessageDialog(this, "已成功為「" + username + "」發放 +50 枚額外代幣獎勵！");
            refreshUserTable(); refreshLeaderboard();
            
            // 🟢 🔥 核心防護修正：防空判斷，避免從大廳開啟時調用 parentFrame 崩潰
            if (parentFrame != null) {
                parentFrame.updateCurrentUserHeader();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "代幣發放失敗: " + e.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTable(JTable table) {
        table.setFont(FONT_MAIN);
        table.setRowHeight(28); 
        table.setGridColor(new Color(240, 242, 245));
        table.setSelectionBackground(new Color(230, 245, 238));
        table.setSelectionForeground(COLOR_TEXT);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(245, 247, 250));
        header.setForeground(COLOR_TEXT);
        header.setPreferredSize(new Dimension(header.getWidth(), 35)); 
        ((DefaultTableCellRenderer)header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
    }

    private static class ModernStyleButton extends JButton {
        private final Color baseColor;
        private final boolean isFilled;
        private final Color fColor;

        public ModernStyleButton(String text, Color baseColor, boolean isFilled) {
            super(text);
            this.baseColor = baseColor;
            this.isFilled = isFilled;
            this.fColor = isFilled ? Color.WHITE : baseColor;
            
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(FONT_BOLD);
            setForeground(fColor);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(getPreferredSize().width + 12, 34)); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isFilled) {
                g2.setColor(baseColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            } else {
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(baseColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 12, 12));
            }
            
            g2.setFont(FONT_BOLD);
            g2.setColor(fColor);
            FontMetrics fm = g2.getFontMetrics();
            int stringWidth = fm.stringWidth(getText());
            int x = (getWidth() - stringWidth) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }
}