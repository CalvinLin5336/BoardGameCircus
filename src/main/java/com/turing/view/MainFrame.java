package com.turing.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.turing.controller.GameController;
import com.turing.controller.UserController;
import com.turing.model.Code;
import com.turing.model.GameRecord;
import com.turing.model.User;
import com.turing.model.VerifierCard;
import com.turing.util.DbUtil;
import com.turing.util.PuzzleGenerator;
import com.turing.util.TuringCardRegistry;
import com.turing.service.impl.ReportServiceImpl;
/**
 * 圖靈解密主介面
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final GameController controller;

    // GUI 元件
    private JPanel contentPane;
    private JTextField txtPlayerName;
    private JComboBox<String> cmbDifficulty;
    private JComboBox<String> cmbCardCount;
    private JComboBox<String> cmbRealDifficulty;
    private JButton btnStartGame;
    
    // 狀態標籤
    private JLabel lblPuzzleId;
    private JLabel lblDifficultyVal;
    private JLabel lblRoundVal;
    private JLabel lblTestsVal;
    private JLabel lblDbStatus;

    // 🟢 密碼選擇改為全新的加減數字微調標籤 (取代舊的 JComboBox)
    private JLabel lblBlueVal;
    private JLabel lblYellowVal;
    private JLabel lblPurpleVal;
    private JPanel pnlProposer;
    // 驗證器元件
    private JPanel pnlVerifiersContainer;
    private JButton btnNextRound;
    private JButton btnGuessCode;

    // 互動筆記本網格按鈕 (3 欄 x 5 個數字)
    private final JButton[][] notepadButtons = new JButton[3][5]; 
    private final char[] colKeys = {'B', 'Y', 'P'};

    // 本場遊戲提問紀錄表格元件
    private JTable tblSessionLog;
    private DefaultTableModel sessionTableModel;

    // --- 會員專屬擴充元件 ---
    private User currentUser;
    private final UserController userController = new UserController();
    private JLabel lblUserHeader;
    private JButton btnAdminConsole;

    // --- 靜態全域快取 ---
    private static List<PuzzleGenerator.ActiveCondition> cachedConditions = new ArrayList<>();
    private static List<int[]> cachedCodes = new ArrayList<>();

    static {
        for (int b = 1; b <= 5; b++) {
            for (int y = 1; y <= 5; y++) {
                for (int p = 1; p <= 5; p++) {
                    cachedCodes.add(new int[]{b, y, p});
                }
            }
        }
        TuringCardRegistry.registerAll(cachedConditions, cachedCodes);
    }

    // --- 現代風格色彩與字型定義 ---
    private static final Color COLOR_BG = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(30, 41, 59);
    private static final Color COLOR_GREEN = new Color(46, 175, 107);
    private static final Color COLOR_ORANGE = new Color(242, 156, 17);
    private static final Color COLOR_BLUE = new Color(52, 152, 219);
    private static final Color COLOR_PURPLE = new Color(155, 89, 182);
    private static final Color COLOR_TEXT = new Color(44, 62, 80);
    private static final Font FONT_MAIN = new Font("Microsoft JhengHei", Font.PLAIN, 12);
    private static final Font FONT_BOLD = new Font("Microsoft JhengHei", Font.BOLD, 13);
    private static final Font FONT_TITLE = new Font("Microsoft JhengHei", Font.BOLD, 22);

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                LoginFrame frame = new LoginFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public MainFrame() {
        this(new User(-1, "測試開發人員", "password", "ADMIN", 100, false));
    }

    public MainFrame(User loggedInUser) {
        this.currentUser = loggedInUser;
        controller = new GameController();

        setTitle("圖靈解密");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 1. 設定還原大小與居中比例
        setBounds(100, 100, 1150, 780);
        setLocationRelativeTo(null);

        // 2. 🟢 關鍵擴充：頁面一開啟即強迫全螢幕最大化
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(248, 250, 252));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(12, 12));

        createHeaderPanel();

        JSplitPane splitPane = new JSplitPane();
        splitPane.setBackground(COLOR_BG);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(null);
     // 🟢 🔥 終極大絕招：直接抓出中間的分隔槓本體，強迫拔除它自帶的方形細線！
     // 🟢 將型態精準更正為 BasicSplitPaneDivider，紅線瞬間消失！
        if (splitPane.getUI() instanceof javax.swing.plaf.basic.BasicSplitPaneUI) {
            javax.swing.plaf.basic.BasicSplitPaneDivider divider = 
                ((javax.swing.plaf.basic.BasicSplitPaneUI) splitPane.getUI()).getDivider();
            
            if (divider != null) {
                divider.setBorder(null); // 徹底抹除中間那條垂直灰線！
            }
        }
        contentPane.add(splitPane, BorderLayout.CENTER);

        JPanel pnlLeft = new JPanel();
        pnlLeft.setBackground(COLOR_BG);
        pnlLeft.setLayout(new BorderLayout(12, 12));
        pnlLeft.setBorder(null);
        splitPane.setLeftComponent(pnlLeft);

        JPanel pnlSetup = createSetupPanel();
        pnlLeft.add(pnlSetup, BorderLayout.NORTH);

        JPanel pnlWorkspace = new JPanel();
        pnlWorkspace.setBackground(COLOR_BG);
        pnlWorkspace.setLayout(new BorderLayout(12, 12));
        pnlLeft.add(pnlWorkspace, BorderLayout.CENTER);

        pnlProposer = createProposerPanel();
        pnlWorkspace.add(pnlProposer, BorderLayout.NORTH);

        JPanel pnlVerifiers = createVerifierArea();
        pnlWorkspace.add(pnlVerifiers, BorderLayout.CENTER);

        JPanel pnlRight = new JPanel();
        pnlRight.setBackground(COLOR_BG);
        pnlRight.setLayout(new BorderLayout(12, 12));
        splitPane.setRightComponent(pnlRight);

        JPanel pnlNotepad = createNotepadPanel();
        pnlRight.add(pnlNotepad, BorderLayout.NORTH);

        JPanel pnlHistory = createSessionLogPanel();
        pnlRight.add(pnlHistory, BorderLayout.CENTER);

        updateDbStatusLabel();
        toggleGameActiveComponents(false); // 初始靜態鎖定
        
        new Thread(() -> {
            System.out.println("🚀 [系統啟動] 正在背景檢查是否有因斷電漏跑的日報表...");
            new ReportServiceImpl().checkAndCatchUpReports(); // 👈 移除了前面的 package 長字串
        }).start();
    }

    private void createHeaderPanel() {
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(new Color(30, 41, 59));
        pnlHeader.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(new Color(30, 41, 59), 1, 12),
                new EmptyBorder(10, 15, 10, 15)
        ));
        pnlHeader.setLayout(new BorderLayout(15, 0));

        // 建立左側標題與規則按鈕的組合面板
        JPanel pnlTitleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlTitleGroup.setOpaque(false);

        JLabel lblTitle = new JLabel("圖靈解密 (TURING MACHINE)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        pnlTitleGroup.add(lblTitle);

        // 🟢 完美移置：將「遊戲說明」按鈕搬移至大標題後方，採用幽靈灰透明質感
        JButton btnRules = new ModernStyleButton("遊戲說明", Color.GRAY, false);
        btnRules.setPreferredSize(new Dimension(100, 30));
        btnRules.addActionListener(e -> showHelpDialog());
        pnlTitleGroup.add(btnRules);

        pnlHeader.add(pnlTitleGroup, BorderLayout.WEST);

        // 右側會員控制項
        JPanel pnlUserControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        pnlUserControls.setOpaque(false);

        lblUserHeader = new JLabel();
        lblUserHeader.setFont(FONT_BOLD);
        lblUserHeader.setForeground(new Color(241, 245, 249));
        pnlUserControls.add(lblUserHeader);

        btnAdminConsole = new ModernStyleButton("後台管理", new Color(100, 116, 139), true);
        btnAdminConsole.addActionListener(e -> {
            AdminConsoleDialog dialog = new AdminConsoleDialog(MainFrame.this);
            dialog.setVisible(true);
        });
        pnlUserControls.add(btnAdminConsole);



        pnlHeader.add(pnlUserControls, BorderLayout.EAST);
        
        JButton btnBackToLobby = new ModernStyleButton("返回大廳", new Color(71, 85, 105), true);
        btnBackToLobby.addActionListener(e -> {
            // 1. 解構銷毀目前的遊戲主畫面
            MainFrame.this.dispose();
            
            // 2. 重新喚醒大廳，傳入最新狀態的 currentUser
            EventQueue.invokeLater(() -> {
                new LobbyFrame(currentUser).setVisible(true);
            });
        });
        pnlUserControls.add(btnBackToLobby); // 塞進 Header 右側控制列
        
        JButton btnLogout = new ModernStyleButton("登出系統", new Color(220, 38, 38), true);
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(MainFrame.this, "確定要登出目前帳號，回到登入畫面嗎？", "系統登出", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                MainFrame.this.dispose();
                EventQueue.invokeLater(() -> {
                    try {
                        LoginFrame loginFrame = new LoginFrame();
                        loginFrame.setVisible(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
        pnlUserControls.add(btnLogout);
        contentPane.add(pnlHeader, BorderLayout.NORTH);

        updateCurrentUserHeader();
    }

    public void updateCurrentUserHeader() {
        if (currentUser == null) return;
        try {
            List<User> users = userController.getAllUsers();
            for (User u : users) {
                if (u.getId() == currentUser.getId()) {
                    this.currentUser = u;
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("刷新使用者狀態失敗: " + e.getMessage());
        }

        if (lblUserHeader != null) {
            lblUserHeader.setText("玩家: " + currentUser.getUsername() + " | 代幣: " + currentUser.getTokens() + " 枚 (" + ("ADMIN".equals(currentUser.getRole()) ? "管理員" : "一般會員") + ") ");
        }
        if (btnAdminConsole != null) {
            btnAdminConsole.setVisible("ADMIN".equals(currentUser.getRole()));
        }
        if (txtPlayerName != null) {
            txtPlayerName.setText(currentUser.getUsername());
        }
    }

    private JPanel createSetupPanel() {
        JPanel pnl = new JPanel();
        styleFormPanel(pnl, " 1. 遊戲核心組態設定", COLOR_GREEN);
        pnl.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JLabel lblPlayer = new JLabel("玩家名稱:");
        lblPlayer.setFont(FONT_BOLD);
        pnl.add(lblPlayer);

        txtPlayerName = new JTextField();
        txtPlayerName.setText(currentUser != null ? currentUser.getUsername() : "測試開發人員");
        txtPlayerName.setEditable(false);
        txtPlayerName.setFont(FONT_MAIN);
        txtPlayerName.setPreferredSize(new Dimension(txtPlayerName.getPreferredSize().width, 30));
        txtPlayerName.setBorder(BorderFactory.createCompoundBorder(new RoundBorder(new Color(210, 215, 220), 1, 8), new EmptyBorder(2, 8, 2, 8)));
        pnl.add(txtPlayerName);
        txtPlayerName.setColumns(12);

        JLabel lblDifficulty = new JLabel("選擇模式:");
        lblDifficulty.setFont(FONT_BOLD);
        pnl.add(lblDifficulty);

        cmbDifficulty = new JComboBox<>();
        cmbDifficulty.setModel(new DefaultComboBoxModel<>(new String[] {
            "隨機生成", 
            "輸入題目編號"
        }));
        cmbDifficulty.setFont(FONT_MAIN);
        cmbDifficulty.setBackground(Color.WHITE);
        cmbDifficulty.setPreferredSize(new Dimension(185, 30));
        cmbDifficulty.setBorder(new RoundBorder(new Color(210, 215, 220), 1, 8));
        pnl.add(cmbDifficulty);

        JLabel lblCardCount = new JLabel("卡片張數:");
        lblCardCount.setFont(FONT_BOLD);
        pnl.add(lblCardCount);

        cmbCardCount = new JComboBox<>();
        cmbCardCount.setModel(new DefaultComboBoxModel<>(new String[] {
            "4 張卡片", 
            "5 張卡片", 
            "6 張卡片",
            "全部隨機"
        }));
        cmbCardCount.setFont(FONT_MAIN);
        cmbCardCount.setBackground(Color.WHITE);
        cmbCardCount.setPreferredSize(new Dimension(100, 30));
        cmbCardCount.setBorder(new RoundBorder(new Color(210, 215, 220), 1, 8));
        pnl.add(cmbCardCount);

        JLabel lblRealDifficulty = new JLabel("題目難度:");
        lblRealDifficulty.setFont(FONT_BOLD);
        pnl.add(lblRealDifficulty);

        cmbRealDifficulty = new JComboBox<>();
        cmbRealDifficulty.setModel(new DefaultComboBoxModel<>(new String[] {
            "A 簡單模式 (Easy)",
            "B 標準模式 (Standard)",
            "C 困難模式 (Hard)"
        }));
        cmbRealDifficulty.setSelectedIndex(1); 
        cmbRealDifficulty.setFont(FONT_MAIN);
        cmbRealDifficulty.setBackground(Color.WHITE);
        cmbRealDifficulty.setPreferredSize(new Dimension(160, 30));
        cmbRealDifficulty.setBorder(new RoundBorder(new Color(210, 215, 220), 1, 8));
        pnl.add(cmbRealDifficulty);

        // 🟢 關鍵修正點：將比對條件完美更正為 "隨機生成"
        cmbDifficulty.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isRandomMode = "隨機生成".equals(cmbDifficulty.getSelectedItem());
                cmbCardCount.setEnabled(isRandomMode);
                cmbRealDifficulty.setEnabled(isRandomMode);
            }
        });

        btnStartGame = new ModernStyleButton("啟動圖靈機", COLOR_GREEN, true);
        pnl.add(btnStartGame);

        btnStartGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleStartGame();
            }
        });

        return pnl;
    }

    /**
     * 🟢 數字增量微調輔助器 (加減 Stepper 面板設計)
     */
    private JPanel createNumberStepper(JLabel valLabel, Color themeColor) {
        JPanel pnlStepper = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pnlStepper.setOpaque(false);

        valLabel.setText("1");
        valLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valLabel.setForeground(COLOR_TEXT);
        valLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valLabel.setPreferredSize(new Dimension(35, 32));

        JButton btnMinus = new ModernStyleButton("-", themeColor, false);
        btnMinus.setPreferredSize(new Dimension(35, 32));
        btnMinus.addActionListener(e -> {
            int current = Integer.parseInt(valLabel.getText());
            if (current > 1) {
                valLabel.setText(String.valueOf(current - 1));
            }
        });

        JButton btnPlus = new ModernStyleButton("+", themeColor, false);
        btnPlus.setPreferredSize(new Dimension(35, 32));
        btnPlus.addActionListener(e -> {
            int current = Integer.parseInt(valLabel.getText());
            if (current < 5) {
                valLabel.setText(String.valueOf(current + 1));
            }
        });

        pnlStepper.add(btnMinus);
        pnlStepper.add(valLabel);
        pnlStepper.add(btnPlus);
        return pnlStepper;
    }

    private JPanel createProposerPanel() {
        JPanel pnl = new JPanel();
        styleFormPanel(pnl, " 2. 提案密碼輸入 (Proposal Code)", COLOR_BLUE);
        pnl.setLayout(new GridBagLayout());

        lblBlueVal = new JLabel();
        lblYellowVal = new JLabel();
        lblPurpleVal = new JLabel();

        GridBagConstraints gbc0 = new GridBagConstraints(); gbc0.insets = new Insets(10, 20, 10, 20); gbc0.gridy = 0; gbc0.gridx = 0;
        JPanel pnlB = new JPanel(new BorderLayout(5, 5)); pnlB.setOpaque(false);
        JLabel lblB = new JLabel("▲ 藍色 (三角形)", SwingConstants.CENTER); lblB.setFont(FONT_BOLD); lblB.setForeground(COLOR_BLUE);
        JPanel stepperB = createNumberStepper(lblBlueVal, COLOR_BLUE);
        pnlB.add(lblB, BorderLayout.NORTH); pnlB.add(stepperB, BorderLayout.CENTER);
        pnl.add(pnlB, gbc0);

        GridBagConstraints gbc1 = new GridBagConstraints(); gbc1.insets = new Insets(10, 20, 10, 20); gbc1.gridy = 0; gbc1.gridx = 1;
        JPanel pnlY = new JPanel(new BorderLayout(5, 5)); pnlY.setOpaque(false);
        JLabel lblY = new JLabel("■ 黃色 (正方形)", SwingConstants.CENTER); lblY.setFont(FONT_BOLD); lblY.setForeground(COLOR_ORANGE);
        JPanel stepperY = createNumberStepper(lblYellowVal, COLOR_ORANGE);
        pnlY.add(lblY, BorderLayout.NORTH); pnlY.add(stepperY, BorderLayout.CENTER);
        pnl.add(pnlY, gbc1);

        GridBagConstraints gbc2 = new GridBagConstraints(); gbc2.insets = new Insets(10, 20, 10, 20); gbc2.gridy = 0; gbc2.gridx = 2;
        JPanel pnlP = new JPanel(new BorderLayout(5, 5)); pnlP.setOpaque(false);
        JLabel lblP = new JLabel("● 紫色 (圓形)", SwingConstants.CENTER); lblP.setFont(FONT_BOLD); lblP.setForeground(COLOR_PURPLE);
        JPanel stepperP = createNumberStepper(lblPurpleVal, COLOR_PURPLE);
        pnlP.add(lblP, BorderLayout.NORTH); pnlP.add(stepperP, BorderLayout.CENTER);
        pnl.add(pnlP, gbc2);

        // 🟢 完美移置：將「進入下一輪」按鈕塞入第二區的最右側 (gridx = 4)，並加上適度間距與權重
        GridBagConstraints gbcGlue = new GridBagConstraints();
        gbcGlue.gridx = 3; gbcGlue.gridy = 0; gbcGlue.weightx = 1.0; // 撐開中間的彈性空白
        pnl.add(Box.createHorizontalGlue(), gbcGlue);

        GridBagConstraints gbcBtn = new GridBagConstraints();
        gbcBtn.gridx = 4; gbcBtn.gridy = 0;
        gbcBtn.insets = new Insets(15, 10, 10, 25); // 讓按鈕靠右側邊框有些許安全襯距
        gbcBtn.fill = GridBagConstraints.NONE;
        
        btnNextRound = new ModernStyleButton("進入下一輪", Color.GRAY, false);
        btnNextRound.setPreferredSize(new Dimension(120, 35)); // 稍微調整按鈕大小使其更大氣
        btnNextRound.addActionListener(e -> handleNextRound());
        pnl.add(btnNextRound, gbcBtn);

        return pnl;
    }

    private JPanel createVerifierArea() {
        JPanel pnl = new JPanel();
        styleFormPanel(pnl, " 3. 選輯篩選數學驗證器區域 (每行並排兩張卡片)", COLOR_ORANGE);
        pnl.setLayout(new BorderLayout(10, 10));

        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 6));
        pnlStatus.setBackground(new Color(248, 250, 252));
        lblPuzzleId = new JLabel("謎題編號: ---"); lblPuzzleId.setFont(FONT_BOLD);
        lblDifficultyVal = new JLabel("難度等級: ---"); lblDifficultyVal.setFont(FONT_MAIN);
        lblRoundVal = new JLabel("目前輪次: 1"); lblRoundVal.setFont(FONT_BOLD);
        lblTestsVal = new JLabel("本輪測試次數: 0 / 3"); lblTestsVal.setFont(FONT_MAIN);

        pnlStatus.add(lblPuzzleId); pnlStatus.add(lblDifficultyVal);
        pnlStatus.add(lblRoundVal); pnlStatus.add(lblTestsVal);
        pnl.add(pnlStatus, BorderLayout.NORTH);

        pnlVerifiersContainer = new JPanel();
        pnlVerifiersContainer.setBackground(COLOR_BG);
        pnlVerifiersContainer.setLayout(new GridLayout(0, 2, 12, 12));
        
        JScrollPane scrollVerifiers = new JScrollPane(pnlVerifiersContainer);
        scrollVerifiers.setBackground(COLOR_BG);
        scrollVerifiers.getViewport().setBackground(COLOR_BG);
        scrollVerifiers.setBorder(new RoundBorder(new Color(230, 235, 240), 1, 10));
        scrollVerifiers.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollVerifiers.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pnl.add(scrollVerifiers, BorderLayout.CENTER);

        // 🟢 完美調整：移除舊有的 btnNextRound，底部控制列改為純右側單顆推測按鈕
        JPanel pnlRoundActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlRoundActions.setOpaque(false);
        
        btnGuessCode = new ModernStyleButton("推測最終密碼", new Color(22, 163, 74), true);
        btnGuessCode.addActionListener(e -> handleGuessCode());

        pnlRoundActions.add(btnGuessCode);
        pnl.add(pnlRoundActions, BorderLayout.SOUTH);

        return pnl;
    }

    private JPanel createNotepadPanel() {
        JPanel pnl = new JPanel();
        styleFormPanel(pnl, " 4. 互動式邏輯演繹筆記本", COLOR_PURPLE);
        pnl.setLayout(new BorderLayout(5, 5));

        JLabel lblNotepadHeader = new JLabel("點擊數字切換狀態：正常   O 符合   X 排除", SwingConstants.CENTER);
        lblNotepadHeader.setFont(FONT_MAIN);
        lblNotepadHeader.setForeground(new Color(100, 116, 139));
        pnl.add(lblNotepadHeader, BorderLayout.NORTH);

        JPanel pnlGrid = new JPanel(new GridLayout(6, 4, 6, 6));
        pnlGrid.setBackground(COLOR_BG);

        JLabel h0 = new JLabel("數值", SwingConstants.CENTER); h0.setFont(FONT_BOLD); pnlGrid.add(h0);
        JLabel colB = new JLabel("▲ 藍色", SwingConstants.CENTER); colB.setForeground(COLOR_BLUE); colB.setFont(FONT_BOLD); pnlGrid.add(colB);
        JLabel colY = new JLabel("■ 黃色", SwingConstants.CENTER); colY.setForeground(COLOR_ORANGE); colY.setFont(FONT_BOLD); pnlGrid.add(colY);
        JLabel colP = new JLabel("● 紫色", SwingConstants.CENTER); colP.setForeground(COLOR_PURPLE); colP.setFont(FONT_BOLD); pnlGrid.add(colP);

        for (int rowVal = 1; rowVal <= 5; rowVal++) {
            JLabel lblRowVal = new JLabel(String.valueOf(rowVal), SwingConstants.CENTER);
            lblRowVal.setFont(FONT_BOLD);
            pnlGrid.add(lblRowVal);

            for (int colIndex = 0; colIndex < 3; colIndex++) {
                final int cIdx = colIndex;
                final int rVal = rowVal;
                final JButton btn = new JButton(String.valueOf(rowVal));
                btn.setFont(FONT_MAIN);
                btn.setFocusPainted(false);
                btn.setBackground(new Color(248, 250, 252));
                btn.setBorder(new RoundBorder(new Color(226, 232, 240), 1, 8));
                
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        controller.cycleNotepadValue(colKeys[cIdx], rVal);
                        updateNotepadButtonState(btn, cIdx, rVal);
                    }
                });

                notepadButtons[colIndex][rowVal - 1] = btn;
                pnlGrid.add(btn);
            }
        }

        pnl.add(pnlGrid, BorderLayout.CENTER);
        return pnl;
    }

/**
     * 5. 當前戰局推導紀錄存根 (本場提問紀錄面板)
     * 完美整合「本地歷史戰績」與「🏆 本地富豪榜」雙控制鈕
     */
    private JPanel createSessionLogPanel() {
        JPanel pnl = new JPanel();
        styleFormPanel(pnl, " 5. 當前戰局推導紀錄存根 (本場提問紀錄)", new Color(71, 85, 105));
        pnl.setLayout(new BorderLayout(5, 5));

        sessionTableModel = new DefaultTableModel(
            new Object[]{"輪次", "提案代碼 (藍-黃-紫)", "測試卡片 ID", "解密結果回報", "自訂筆記(點擊可輸入)"}, 0
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 4; 
            }
        };

        tblSessionLog = new JTable(sessionTableModel);
        styleTable(tblSessionLog);
        JScrollPane scrollPane = new JScrollPane(tblSessionLog);
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.setBorder(new RoundBorder(new Color(230, 235, 240), 1, 10));
        scrollPane.setPreferredSize(new Dimension(300, 200));
        pnl.add(scrollPane, BorderLayout.CENTER);

        JPanel pnlDbActions = new JPanel(new BorderLayout(5, 5));
        pnlDbActions.setOpaque(false);

        lblDbStatus = new JLabel("資料庫狀態: 連線檢索中...");
        lblDbStatus.setFont(FONT_BOLD);
        pnlDbActions.add(lblDbStatus, BorderLayout.WEST);

        // 🟢 將 FlowLayout 按鈕之間的水平間距加大到 10 像素，避免兩顆現代化按鈕擠在一起
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlButtons.setOpaque(false);
        
        // 藍色主題：本地歷史戰績
        JButton btnShowHistoryDialog = new ModernStyleButton("本地歷史戰績", COLOR_BLUE, false);
        btnShowHistoryDialog.addActionListener(e -> showHistoryDialog());
        pnlButtons.add(btnShowHistoryDialog);

        // 🟠 亮橘金色主題：🏆 本地富豪榜
        JButton btnShowRichList = new ModernStyleButton("本地富豪榜", COLOR_ORANGE, false);
        btnShowRichList.addActionListener(e -> showRichListDialog());
        pnlButtons.add(btnShowRichList);

        pnlDbActions.add(pnlButtons, BorderLayout.EAST);
        pnl.add(pnlDbActions, BorderLayout.SOUTH);
        
        return pnl;
    }

    private void updateNotepadButtonState(JButton btn, int colIndex, int val) {
        int state = controller.getNotepadValue(colKeys[colIndex], val);
        switch (state) {
            case 1:
                btn.setText("O " + val);
                btn.setForeground(new Color(21, 128, 61)); 
                btn.setBackground(new Color(220, 252, 231)); 
                break;
            case 2:
                btn.setText("X " + val);
                btn.setForeground(new Color(185, 28, 28)); 
                btn.setBackground(new Color(254, 226, 226)); 
                break;
            default:
                btn.setText(String.valueOf(val));
                btn.setForeground(COLOR_TEXT);
                btn.setBackground(new Color(248, 250, 252));
                break;
        }
    }

    private void resetAllNotepadButtons() {
        for (int c = 0; c < 3; c++) {
            for (int r = 1; r <= 5; r++) {
                updateNotepadButtonState(notepadButtons[c][r - 1], c, r);
            }
        }
    }

    private void toggleGameActiveComponents(boolean active) {
        btnNextRound.setEnabled(active);
        btnGuessCode.setEnabled(active);
        
        // 🟢 終極修正：直接對目標面板進行按鈕開關，再也不需要用複雜的 BorderCompound 去遞迴瞎猜！
        if (pnlProposer != null) {
            disablePanelButtons(pnlProposer, active);
        }
        
        for (JButton[] cols : notepadButtons) {
            for (JButton btn : cols) {
                btn.setEnabled(active);
            }
        }
    }

    /**
     * 🟢 專精開關：只控制第二區的加減 Stepper 按鈕，絕不波及右下角的回合按鈕
     */
    private void toggleProposerButtons(boolean active) {
        if (pnlProposer != null) {
            disablePanelButtons(pnlProposer, active);
        }
    }
    
private void disablePanelButtons(JPanel panel, boolean active) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) {
                // 🟢 核心修正：如果抓到的按鈕是「進入下一輪」，絕對不可以把它關掉！
                if (comp == btnNextRound) {
                    continue; 
                }
                comp.setEnabled(active);
            } else if (comp instanceof JPanel) {
                disablePanelButtons((JPanel)comp, active);
            }
        }
    }

    // 用於比對標題的輔助判斷
    private static class BorderCompound {} 

private void handleStartGame() {
        String currentPlayerName = txtPlayerName.getText(); 
        String difficultyMode = (String) cmbDifficulty.getSelectedItem(); 
        String puzzleCodeDisplay = "";

        if (cachedConditions == null || cachedConditions.isEmpty() || cachedCodes == null || cachedCodes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "核心快取資料未載入完成，請稍後再試！", "系統提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        controller.startNewGameWithLivePuzzle(null, currentPlayerName); 
        
        try (Connection conn = DbUtil.getConnection()) {
            if ("隨機生成".equals(difficultyMode)) {
            	int kCount = controller.getFinalCardCount((String) cmbCardCount.getSelectedItem());
                
                String selectedDiffStr = (String) cmbRealDifficulty.getSelectedItem();
                char diffChar = (selectedDiffStr != null) ? selectedDiffStr.charAt(0) : 'B'; 
                
                puzzleCodeDisplay = controller.generateAndSetupRandomGame(conn, cachedConditions, cachedCodes, diffChar, kCount);
                
                lblPuzzleId.setText("謎題編號: " + puzzleCodeDisplay);
                
                String diffLabelText = "A 簡單";
                if (diffChar == 'B') diffLabelText = "B 標準";
                if (diffChar == 'C') diffLabelText = "C 困難";
                lblDifficultyVal.setText("難度等級: " + diffLabelText);
                
            } else if ("輸入題目編號".equals(difficultyMode)) {
                String inputCode = JOptionPane.showInputDialog(this, 
                    "請輸入題目編號：\n(格式範例：#C6 39I6Z06A7D)", 
                    "載入題目編號", 
                    JOptionPane.QUESTION_MESSAGE);
                
                if (inputCode == null || inputCode.trim().isEmpty()) return; 
                puzzleCodeDisplay = inputCode.trim().toUpperCase();

                controller.loadAndSetupGameByCode(conn, puzzleCodeDisplay, cachedConditions);
                
                lblPuzzleId.setText("謎題編號: " + puzzleCodeDisplay);
                lblDifficultyVal.setText("難度等級: 自訂 (序號載入)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "啟動遊戲失敗，請檢查資料庫連線或序號格式！\n錯誤原因：" + ex.getMessage(), "錯誤", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. 刷新重置數值標籤與歷史戰績模型
        lblBlueVal.setText("1");
        lblYellowVal.setText("1");
        lblPurpleVal.setText("1");
        sessionTableModel.setRowCount(0);
        resetAllNotepadButtons();

        // 2. 🎬 核心解鎖：全面解鎖與喚醒所有遊戲功能元件（全面取代原本局部的 toggleProposerButtons）
        toggleGameActiveComponents(true); 

        // 3. 渲染驗證器卡片版面佈局
        refreshGameUiLayout(); 
    }

    /**
     * 🟢 專屬 UI 佈局渲染器：只管把 Controller 鎖定的卡片繪製出來
     */
    private void refreshGameUiLayout() {
        pnlVerifiersContainer.removeAll(); 

        com.turing.model.Puzzle activePuzzle = controller.getCurrentPuzzle();
        
        if (activePuzzle != null) {
        	// 🌟 改造核心：直接向 Controller 索取卡片物件清單，UI 不再瞎猜資料庫！
            List<com.turing.model.VerifierCard> liveCards = controller.getActiveVerifierCardsForLiveGame();
            
            for (com.turing.model.VerifierCard cardObj : liveCards) {
                // 直接將乾淨的卡片資料丟進元件產生器
                pnlVerifiersContainer.add(createVerifierCardComponent(cardObj));
            }
        }

        pnlVerifiersContainer.revalidate();
        pnlVerifiersContainer.repaint();
        if (pnlVerifiersContainer.getParent() != null) {
            pnlVerifiersContainer.getParent().revalidate();
        }
        
        // 同步文字狀態
        lblRoundVal.setText("目前輪次: 1");
        lblTestsVal.setText("本輪已測試: 0 / 3 (累計: 0 次)");

        JOptionPane.showMessageDialog(this, "圖靈密碼機啟動成功！新賽局已開始！", "系統提示", JOptionPane.INFORMATION_MESSAGE);
    }



    private JPanel createVerifierCardComponent(VerifierCard card) {
        JPanel pnlCard = new JPanel(new GridBagLayout());
        pnlCard.setBackground(new Color(248, 250, 252));
        pnlCard.setPreferredSize(new Dimension(0, 245)); 
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(203, 213, 225), 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(300, 195));

        JLabel lblCardImage = new JLabel();
        lblCardImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblCardImage.setBounds(0, 0, 300, 195);

        String resPath = "/cards/TM_GameCards_CNT-" + card.getCardId() + ".png";
        java.net.URL imgUrl = MainFrame.class.getResource(resPath);
        ImageIcon icon = null;

        if (imgUrl != null) {
            icon = new ImageIcon(imgUrl);
        } else {
            File rFile = new File("cards/TM_GameCards_CNT-" + card.getCardId() + ".png");
            if (rFile.exists()) icon = new ImageIcon(rFile.getAbsolutePath());
        }
        
        if (icon != null) {
            Image scaledImg = icon.getImage().getScaledInstance(300, 195, Image.SCALE_SMOOTH);
            lblCardImage.setIcon(new ImageIcon(scaledImg));
        } else {
            lblCardImage.setText("<html><center><b>卡片 #" + card.getCardId() + "</b><br><small>缺少圖檔</small></center></html>");
            lblCardImage.setFont(FONT_MAIN);
            lblCardImage.setOpaque(true);
            lblCardImage.setBackground(new Color(226, 232, 240));
        }
        layeredPane.add(lblCardImage, JLayeredPane.DEFAULT_LAYER);

        List<Rectangle> optionBounds = com.turing.util.CardAssetManager.getCardOptionBounds(card.getCardId());
        for (Rectangle rect : optionBounds) {
            JToggleButton btnCross = new JToggleButton();
            btnCross.setBounds(rect);
            btnCross.setContentAreaFilled(false);
            btnCross.setBorderPainted(false);
            btnCross.setFocusPainted(false);
            btnCross.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            btnCross.addActionListener(e -> {
                if (btnCross.isSelected()) {
                    int fontSize = rect.width > 100 ? 42 : (rect.width < 75 ? 28 : 34);
                    btnCross.setText("<html><body style='text-align:center;'><b style='font-size:" + fontSize + "px; color:rgba(220,38,38,0.85); font-family:Arial;'>X</b></body></html>");
                } else {
                    btnCross.setText("");
                }
            });
            layeredPane.add(btnCross, JLayeredPane.PALETTE_LAYER);
        }

        GridBagConstraints gbcLeft = new GridBagConstraints();
        gbcLeft.gridx = 0; gbcLeft.gridy = 0; gbcLeft.weightx = 0.75; gbcLeft.weighty = 1.0;
        gbcLeft.fill = GridBagConstraints.CENTER;
        gbcLeft.insets = new Insets(0, 2, 0, 2);
        pnlCard.add(layeredPane, gbcLeft);

        JPanel pnlActions = new JPanel();
        pnlActions.setOpaque(false);
        pnlActions.setLayout(new BoxLayout(pnlActions, BoxLayout.Y_AXIS));
        pnlActions.add(Box.createVerticalGlue());

        JButton btnTest = new ModernStyleButton("驗證", COLOR_GREEN, false);
        btnTest.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTest.setPreferredSize(new Dimension(75, 30));
        pnlActions.add(btnTest);

        pnlActions.add(Box.createVerticalStrut(12));

        JLabel lblResult = new JLabel("未測試", SwingConstants.CENTER);
        lblResult.setFont(FONT_BOLD);
        lblResult.setForeground(new Color(100, 116, 139));
        lblResult.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblResult.setPreferredSize(new Dimension(75, 24));
        pnlActions.add(lblResult);
        pnlActions.add(Box.createVerticalGlue());

        GridBagConstraints gbcRight = new GridBagConstraints();
        gbcRight.gridx = 1; gbcRight.gridy = 0;
        gbcRight.weightx = 0.25; gbcRight.weighty = 1.0;
        gbcRight.fill = GridBagConstraints.CENTER;
        gbcRight.insets = new Insets(0, 0, 0, 5);
        pnlCard.add(pnlActions, gbcRight);

        btnTest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controller.getTestsThisRound() >= 3) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "每一輪最多只能測試 3 張驗證卡片！\n請點擊「進入下一輪」來重設測試次數及密碼。",
                        "已達驗證上限", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                toggleProposerButtons(false);

                // 🟢 修正：讀取加減 Stepper 面板文字，且不再封鎖輸入選單
                int b = Integer.parseInt(lblBlueVal.getText());
                int y = Integer.parseInt(lblYellowVal.getText());
                int p = Integer.parseInt(lblPurpleVal.getText());
                Code proposal = new Code(b, y, p);

                boolean result = controller.testVerifier(proposal, card.getCardId());
                lblTestsVal.setText("本輪已測試: " + controller.getTestsThisRound() + " / 3 (累計: " + controller.getTotalTests() + " 次)");

                String logOutcome = result ? "O TRUE" : "X FALSE";
                lblResult.setText(result ? "O  TRUE" : "X  FALSE");
                lblResult.setForeground(result ? new Color(22, 163, 74) : new Color(220, 38, 38));

                sessionTableModel.addRow(new Object[]{
                    "第 " + controller.getCurrentRound() + " 輪",
                    "  [ " + b + " - " + y + " - " + p + " ]",
                    "卡片 #" + card.getCardId(),
                    logOutcome,
                    ""
                });
            }
        });

        return pnlCard;
    }

    private void handleNextRound() {
        // 1. 呼叫大腦推進回合
        controller.nextRound();
        
        // 2. 🟢 關鍵修正：立刻重新啟用所有畫面的互動元件（包括加減 Stepper 與相關按鈕）
        toggleProposerButtons(true);
        
        // 3. 刷新輪次與測試次數的文字狀態標籤
        lblRoundVal.setText("目前輪次: " + controller.getCurrentRound());
        lblTestsVal.setText("本輪已測試: 0 / 3 (累計: " + controller.getTotalTests() + " 次)");

        // 4. 清空畫面所有驗證卡片的「TRUE/FALSE」暫存狀態，全部還原變回「未測試」的綠色彩色狀態
        for (Component c : pnlVerifiersContainer.getComponents()) {
            if (c instanceof JPanel) {
                JPanel cardPnl = (JPanel) c;
                for (Component rightPnl : cardPnl.getComponents()) {
                    if (rightPnl instanceof JPanel) {
                        JPanel actPnl = (JPanel) rightPnl;
                        for (Component innerComp : actPnl.getComponents()) {
                            // 喚醒卡片內部被停用的驗證按鈕
                            if (innerComp instanceof JButton) {
                                innerComp.setEnabled(true);
                            }
                            // 重置結果標籤文字與顏色
                            if (innerComp instanceof JLabel) {
                                JLabel resultLbl = (JLabel) innerComp;
                                if (resultLbl.getText().contains("TRUE") || resultLbl.getText().contains("FALSE") || resultLbl.getText().contains("結果")) {
                                    resultLbl.setText("未測試");
                                    resultLbl.setForeground(new Color(100, 116, 139));
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. 🟢 關鍵修正：強迫全畫面容器重新計算佈局並重繪，徹底解決「看起來點不下去」的畫面假死 Bug！
        pnlVerifiersContainer.revalidate();
        pnlVerifiersContainer.repaint();
        contentPane.revalidate();
        contentPane.repaint();

        // 6. 彈出成功提示
        JOptionPane.showMessageDialog(this,
            "已成功進入第 " + controller.getCurrentRound() + " 回合。\n新回合測試次數已重製！請調整密碼進行新一輪測試！",
            "新回合開始", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleGuessCode() {
        // 1. 防呆
        com.turing.model.Puzzle activePuzzle = controller.getCurrentPuzzle();
        if (activePuzzle == null) {
            JOptionPane.showMessageDialog(this, "目前沒有正在進行中的遊戲賽局！請先點擊「啟動圖靈機」。", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. 建立宣告真相專屬的互動彈窗
        JDialog guessDialog = new JDialog(this, "提交最終推測密碼", true);
        guessDialog.setSize(650, 280); 
        guessDialog.setLocationRelativeTo(this);
        guessDialog.setResizable(false);

        JPanel pnlDialogContent = new JPanel(new BorderLayout(15, 20));
        pnlDialogContent.setBackground(COLOR_BG);
        pnlDialogContent.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel lblPrompt = new JLabel("請使用加減按鈕調整您推測的最終 3 位數神秘密碼：", SwingConstants.CENTER);
        lblPrompt.setFont(FONT_BOLD);
        lblPrompt.setForeground(COLOR_TEXT);
        pnlDialogContent.add(lblPrompt, BorderLayout.NORTH);

        JPanel pnlSteppersContainer = new JPanel(new GridBagLayout());
        pnlSteppersContainer.setOpaque(false);

        JLabel dlgBlueVal = new JLabel();
        JLabel dlgYellowVal = new JLabel();
        JLabel dlgPurpleVal = new JLabel();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.insets = new Insets(5, 25, 5, 25); 

        gbc.gridx = 0;
        JPanel pnlB = new JPanel(new BorderLayout(5, 5)); pnlB.setOpaque(false);
        JLabel lblB = new JLabel("▲ 藍色", SwingConstants.CENTER); lblB.setFont(FONT_BOLD); lblB.setForeground(COLOR_BLUE);
        pnlB.add(lblB, BorderLayout.NORTH); pnlB.add(createNumberStepper(dlgBlueVal, COLOR_BLUE), BorderLayout.CENTER);
        pnlSteppersContainer.add(pnlB, gbc);

        gbc.gridx = 1;
        JPanel pnlY = new JPanel(new BorderLayout(5, 5)); pnlY.setOpaque(false);
        JLabel lblY = new JLabel("■ 黃色", SwingConstants.CENTER); lblY.setFont(FONT_BOLD); lblY.setForeground(COLOR_ORANGE);
        pnlY.add(lblY, BorderLayout.NORTH); pnlY.add(createNumberStepper(dlgYellowVal, COLOR_ORANGE), BorderLayout.CENTER);
        pnlSteppersContainer.add(pnlY, gbc);

        gbc.gridx = 2;
        JPanel pnlP = new JPanel(new BorderLayout(5, 5)); pnlP.setOpaque(false);
        JLabel lblP = new JLabel("● 紫色", SwingConstants.CENTER); lblP.setFont(FONT_BOLD); lblP.setForeground(COLOR_PURPLE);
        pnlP.add(lblP, BorderLayout.NORTH); pnlP.add(createNumberStepper(dlgPurpleVal, COLOR_PURPLE), BorderLayout.CENTER);
        pnlSteppersContainer.add(pnlP, gbc);

        pnlDialogContent.add(pnlSteppersContainer, BorderLayout.CENTER);

        JPanel pnlActionButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        pnlActionButtons.setOpaque(false);
        JButton btnConfirm = new ModernStyleButton("確認提交真相", new Color(22, 163, 74), true);
        JButton btnCancel = new ModernStyleButton("取消", Color.GRAY, false);
        pnlActionButtons.add(btnCancel); pnlActionButtons.add(btnConfirm);
        
        JPanel pnlSouthWrapper = new JPanel(new BorderLayout());
        pnlSouthWrapper.setOpaque(false);
        pnlSouthWrapper.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        pnlSouthWrapper.add(pnlActionButtons, BorderLayout.CENTER);
        pnlDialogContent.add(pnlSouthWrapper, BorderLayout.SOUTH);

        guessDialog.setContentPane(pnlDialogContent);

        final boolean[] submitted = {false};
        btnCancel.addActionListener(ex -> guessDialog.dispose());
        btnConfirm.addActionListener(ex -> {
            submitted[0] = true;
            guessDialog.dispose();
        });

        guessDialog.setVisible(true);
        if (!submitted[0]) return;

        // 3. 🎯 讀取加減面版定格的數值
        int b = Integer.parseInt(dlgBlueVal.getText());
        int y = Integer.parseInt(dlgYellowVal.getText());
        int p = Integer.parseInt(dlgPurpleVal.getText());

        // 4. 🧠 關鍵核心改動：從下拉選單中當場撈出當前賽局的難度代號第一個字元 ('A', 'B', 'C')
        String selectedDiffStr = (String) cmbRealDifficulty.getSelectedItem();
        char diffChar = (selectedDiffStr != null) ? selectedDiffStr.charAt(0) : 'B'; 

        // 5. 🧠 將難度參數 diffChar 動態注入 Controller 結算大腦
        // 💡 提示：記得同步在你的 GameController.submitAndBuildResult 中加上 char 難度參數接收，並丟給底層 Service！
        com.turing.model.GameResultDto resultDto = controller.submitAndBuildResult(currentUser, b, y, p, diffChar);

        // 如果贏了，即時更新前台右上角的會員錢包餘額顯示
        if (resultDto.isWon() && resultDto.getRewardTokens() > 0) {
            updateCurrentUserHeader(); 
        }

        // 6. 📢 彈出最終結算成果框 (文字內容已由後端 Service 完美組裝)
        JOptionPane.showMessageDialog(this, resultDto.getMessage(), "賽局最終解密回報 (Game Over)", JOptionPane.INFORMATION_MESSAGE);

        // 7. 🛑 結束並鎖定這場遊戲
        toggleProposerButtons(false); 
        btnGuessCode.setEnabled(false);    
    }

private void showHistoryDialog() {
        JDialog dialog = new JDialog(this, "歷史紀錄日誌 (Game History Logs)", true);
        dialog.setSize(850, 450); // 稍微加寬 50px 給新代幣欄位舒展空間
        dialog.setLocationRelativeTo(this);
        
        JPanel pnlDialogContent = new JPanel(new BorderLayout(10, 10));
        pnlDialogContent.setBackground(COLOR_BG);
        pnlDialogContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // 🟢 完美優化：在表頭欄位同步追加「獲得獎勵代幣」欄位，讓歷史數據 100% 透明化！
        DefaultTableModel dialogModel = new DefaultTableModel(
            new Object[]{ "挑戰玩家", "謎題編號", "消耗輪次", "測試次數", "獲得獎勵", "勝負結局", "存檔時間" }, 0
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable tblDialogHistory = new JTable(dialogModel);
        styleTable(tblDialogHistory);
        JScrollPane scrollPane = new JScrollPane(tblDialogHistory);
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.setBorder(new RoundBorder(new Color(230, 235, 240), 1, 10));
        pnlDialogContent.add(scrollPane, BorderLayout.CENTER);
        
        // 🧠 強迫重刷：在載入前，確保 dialogModel 是完全清空乾淨的狀態
        dialogModel.setRowCount(0);
        
        try {
            // 🚀 即時連動：每一次點開按鈕，都強迫大腦重新下 SQL 向 MySQL 要最燙手的熱騰騰戰績！
            List<GameRecord> records = controller.getHistory();
            for (GameRecord record : records) {
                // 對齊 7 個標準欄位填入資料
                dialogModel.addRow(new Object[]{
                    record.getPlayerName(),
                    record.getPuzzleId(), 
                    record.getRoundsUsed() + " 輪",
                    record.getTestsUsed() + " 次",
                    record.getTokensRewarded() > 0 ? " +" + record.getTokensRewarded() : "—", // 顯示實體新代幣
                    record.isWon() ? "成功" : "失敗",
                    record.getPlayedAt()
                });
            }
        } catch (Exception e) {
            System.err.println("🚨 讀取彈窗歷史表格數據失敗: " + e.getMessage());
        }
        
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBottom.setOpaque(false);
        JButton btnClose = new ModernStyleButton("關閉日誌", Color.GRAY, true);
        btnClose.addActionListener(e -> dialog.dispose());
        pnlBottom.add(btnClose);
        pnlDialogContent.add(pnlBottom, BorderLayout.SOUTH);
        
        dialog.setContentPane(pnlDialogContent);
        dialog.setVisible(true);
    }

    private void updateDbStatusLabel() {
        if (DbUtil.isOfflineMode()) {
            lblDbStatus.setText("資料庫狀態: 離線模式 ");
            lblDbStatus.setForeground(new Color(185, 28, 28)); 
        } else {
            lblDbStatus.setText("資料庫狀態: 連線成功 ");
            lblDbStatus.setForeground(new Color(21, 128, 61)); 
        }
    }


/**
     * 🖼️ 全新升級：高清寬螢幕極大化 — 圖片說明書檢視器
     * 自動抓取螢幕解析度並放大至 90%，圖片同步高清重繪，視覺大氣不失真
     */
    private void showHelpDialog() {
        JDialog helpDialog = new JDialog(this, "圖靈解密 — 遊戲操作說明", true);
        
        // 🟢 核心動態放大機制 1：即時獲取玩家當前電腦螢幕的實體解析度寬高
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int dialogWidth = (int) (screenSize.width * 0.9);  // 💡 佔據螢幕 90% 寬度
        int dialogHeight = (int) (screenSize.height * 0.95); // 💡 佔據螢幕 95% 高度
        
        helpDialog.setSize(dialogWidth, dialogHeight); 
        helpDialog.setLocationRelativeTo(this); // 置中彈出
        helpDialog.setResizable(false);

        JPanel pnlContent = new JPanel(new BorderLayout(15, 15));
        pnlContent.setBackground(COLOR_BG);
        pnlContent.setBorder(new EmptyBorder(20, 25, 20, 25)); // 大畫面搭配寬鬆襯距
        helpDialog.setContentPane(pnlContent);

        // 頂部狀態列
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        JLabel lblTitle = new JLabel("密碼破解手冊", JLabel.LEFT);
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 18)); // 標題字體同步放大
        lblTitle.setForeground(COLOR_PRIMARY);
        
        JLabel lblPageIndicator = new JLabel("第 1 / 7 頁", JLabel.RIGHT);
        lblPageIndicator.setFont(FONT_BOLD);
        lblPageIndicator.setForeground(new Color(100, 116, 139));
        
        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(lblPageIndicator, BorderLayout.EAST);
        pnlContent.add(pnlTop, BorderLayout.NORTH);

        // 中央大圖片容器
        JLabel lblImageDisplay = new JLabel();
        lblImageDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        lblImageDisplay.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        pnlContent.add(lblImageDisplay, BorderLayout.CENTER);

        String[] helpImages = {
        	"help_0.png", "help_1.png", "help_2.png", "help_3.png", 
            "help_4.png", "help_5.png", "help_6.png"
        };

        final int[] currentPage = {0}; 

        // 🟢 核心動態放大機制 2：扣除上下控制列預留高度，精算出最完美的圖片展示黃金比例尺寸
        int targetImgWidth = dialogWidth - 60;   // 左右各扣除邊距
        int targetImgHeight = dialogHeight - 140; // 上下扣除標題列與按鈕控制列

        Runnable refreshHelpPage = () -> {
            int pageIdx = currentPage[0];
            lblPageIndicator.setText("第 " + (pageIdx + 1) + " / 7 頁");
            
            String imgName = helpImages[pageIdx];
            java.awt.image.BufferedImage rawImg = null;
            
            try {
                java.net.URL imgUrl = MainFrame.class.getResource("/help/" + imgName);
                if (imgUrl != null) {
                    rawImg = javax.imageio.ImageIO.read(imgUrl);
                } else {
                    File file = new File("help/" + imgName);
                    rawImg = javax.imageio.ImageIO.read(file);
                }
            } catch (Exception ex) {
                System.err.println("❌ [說明書讀取失敗] 錯誤原因：");
                ex.printStackTrace();
            }

            if (rawImg != null) {
                // 🟢 將精算出來的 target 寬高塞入同步雙線性引擎，強迫圖片放大成高清劇院版！
                lblImageDisplay.setIcon(new ImageIcon(getSyncScaledImage(rawImg, targetImgWidth, targetImgHeight)));
                lblImageDisplay.setText("");
            } else {
                lblImageDisplay.setIcon(null);
                lblImageDisplay.setText("<html><center><font size='5' color='#94A3B8'>📷 尚未偵測到說明圖檔</font></center></html>");
            }
        };

        // 底部控制列
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setOpaque(false);

        JPanel pnlNavButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlNavButtons.setOpaque(false);
        
        JButton btnPrev = new ModernStyleButton("< 上一頁", COLOR_PRIMARY, false);
        JButton btnNext = new ModernStyleButton("下一頁 >", COLOR_PRIMARY, false);
        // 大螢幕下讓按鈕稍微增寬，操作更舒服
        btnPrev.setPreferredSize(new Dimension(110, 36));
        btnNext.setPreferredSize(new Dimension(110, 36));
        pnlNavButtons.add(btnPrev);
        pnlNavButtons.add(btnNext);
        pnlBottom.add(pnlNavButtons, BorderLayout.WEST);

        JButton btnClose = new ModernStyleButton("離開說明手冊", Color.GRAY, true);
        btnClose.setPreferredSize(new Dimension(130, 36));
        btnClose.addActionListener(e -> helpDialog.dispose());
        pnlBottom.add(btnClose, BorderLayout.EAST);
        pnlContent.add(pnlBottom, BorderLayout.SOUTH);

        btnPrev.addActionListener(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                refreshHelpPage.run();
                btnNext.setEnabled(true);
            }
            btnPrev.setEnabled(currentPage[0] > 0);
        });

        btnNext.addActionListener(e -> {
            if (currentPage[0] < helpImages.length - 1) {
                currentPage[0]++;
                refreshHelpPage.run();
                btnPrev.setEnabled(true);
            }
            btnNext.setEnabled(currentPage[0] < helpImages.length - 1);
        });

        btnPrev.setEnabled(false);
        refreshHelpPage.run();

        helpDialog.setVisible(true);
    }

/**
     * 🧠 終極安全同步縮放引擎（強迫剝離透明通道版）
     * 解決部分 PNG 圖片因 Alpha 透明通道被 Java 誤判為全透明而導致畫面全白的 Bug
     */
    private java.awt.image.BufferedImage getSyncScaledImage(Image srcImage, int targetWidth, int targetHeight) {
        // 🟢 核心修正 1：將畫布型態改為 TYPE_INT_RGB（不帶透明通道），強迫 Java 以實體色彩渲染
        java.awt.image.BufferedImage output = new java.awt.image.BufferedImage(
            targetWidth, targetHeight, java.awt.image.BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g2 = output.createGraphics();
        
        // 開啟最高畫質雙線性內插補點與抗鋸齒
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // 🟢 核心修正 2：先在底層塗滿一層「實心的純白色」，防止圖片背景變成黑色或透明
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, targetWidth, targetHeight);
        
        // 🟢 核心修正 3：強制將原始 PNG 圖片同步覆蓋畫在白底上
        g2.drawImage(srcImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        
        return output;
    }

    private void styleFormPanel(JPanel panel, String title, Color titleColor) {
        panel.setBackground(COLOR_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(titleColor, 1, 12),
                BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder(), title, TitledBorder.LEFT, TitledBorder.TOP, FONT_BOLD, titleColor
                        )
                ));
    }

    private void styleTable(JTable table) {
        table.setFont(FONT_MAIN);
        table.setRowHeight(26);
        table.setGridColor(new Color(240, 242, 245));
        table.setSelectionBackground(new Color(230, 245, 238));
        table.setSelectionForeground(COLOR_TEXT);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(245, 247, 250));
        header.setForeground(COLOR_TEXT);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
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
            setPreferredSize(new Dimension(getPreferredSize().width + 10, 32));
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (java.beans.Beans.isDesignTime()) {
                super.paintComponent(g);
                return;
            }
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
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    private static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int thickness;
        private final int radii;

        public RoundBorder(Color color, int thickness, int radii) {
            this.color = color;
            this.thickness = thickness;
            this.radii = radii;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + (thickness/2f), y + (thickness/2f), width - thickness, height - thickness, radii, radii));
            g2.dispose();
        }
    }
    
    
    
/**
     * 👑 彈出本地富豪排行榜對話框 (Top 10 Rich List) - 精簡無權限欄位版
     * 從底層 UserDao 動態撈取代幣排行前十名的一般會員
     */
    private void showRichListDialog() {
        JDialog dialog = new JDialog(this, "本地富豪榜 — 頂尖財富排行", true);
        dialog.setSize(500, 400); // 欄位變少，寬度稍微縮減到 500 更緊湊精緻
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel pnlDialogContent = new JPanel(new BorderLayout(10, 15));
        pnlDialogContent.setBackground(COLOR_BG);
        pnlDialogContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitle = new JLabel("伺服器代幣財富名譽榜 (Top 10)", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_ORANGE);
        pnlDialogContent.add(lblTitle, BorderLayout.NORTH);
        
        // 🟢 修正 1：刪除原本的「身份權限」欄位，縮減為 3 個標準欄位
        DefaultTableModel richModel = new DefaultTableModel(
            new Object[]{ "名次", "玩家帳號", "持有的代幣總額" }, 0
        ) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable tblRich = new JTable(richModel);
        styleTable(tblRich); // 完美沿用主介面的高顏值表格樣式
        
        JScrollPane scrollPane = new JScrollPane(tblRich);
        scrollPane.setBackground(COLOR_BG);
        scrollPane.getViewport().setBackground(COLOR_BG);
        scrollPane.setBorder(new RoundBorder(new Color(230, 235, 240), 1, 10));
        pnlDialogContent.add(scrollPane, BorderLayout.CENTER);
        
        // 資料庫與 DAO 連動層
        try {
            List<User> richUsers = userController.getTopRichUsers();
            int rank = 1;
            for (User u : richUsers) {
                String rankStr;
                // 幫前三名加上榮譽勳章圖示
                if (rank == 1) rankStr = "第 1 名";
                else if (rank == 2) rankStr = "第 2 名";
                else if (rank == 3) rankStr = "第 3 名";
                else rankStr = "第 " + rank + " 名";

                // 🟢 修正 2：移除了 roleName 的比對與填入，對齊 3 個欄位填入 Object 陣列
                richModel.addRow(new Object[]{
                    rankStr,
                    u.getUsername(),
                    u.getTokens() + " 枚"
                });
                rank++;
            }
        } catch (Exception e) {
            System.err.println("無法加載富豪榜數據: " + e.getMessage());
            JOptionPane.showMessageDialog(dialog, "讀取排行失敗，請確認網路或 MySQL 資料庫伺服器連線狀態！", "錯誤", JOptionPane.ERROR_MESSAGE);
        }
        
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlBottom.setOpaque(false);
        JButton btnClose = new ModernStyleButton("關閉排行榜", Color.GRAY, true);
        btnClose.addActionListener(e -> dialog.dispose());
        pnlBottom.add(btnClose);
        pnlDialogContent.add(pnlBottom, BorderLayout.SOUTH);
        
        dialog.setContentPane(pnlDialogContent);
        dialog.setVisible(true);
        

    }
    
}