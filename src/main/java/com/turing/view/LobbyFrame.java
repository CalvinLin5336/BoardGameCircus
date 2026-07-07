package com.turing.view;

import com.turing.model.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

import java.awt.AlphaComposite;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * 🏆 遊戲遊玩大廳 - 營運權限分流版
 * 🟢 完美增補：只有 ADMIN 帳號才看得到的「後台管理」控制按鈕，一般會員完全隱藏。
 */
public class LobbyFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final User currentUser;
    private JLabel lblWelcome;
    private JButton btnAdminConsole; // 👈 宣告後台按鈕變數

    public LobbyFrame(User user) {
        this.currentUser = user;
        setTitle("遊戲大廳");
        setSize(1280, 720); // 標準 HD 黃金比例
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout(15, 15));
        contentPane.setBackground(new Color(248, 250, 252)); // 灰底襯托
        contentPane.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(contentPane);

        // ===================================================================
        // 1. 頂部狀態戰情列 (Header)
        // ===================================================================
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(30, 41, 59)); // 經典深藍底
        pnlHeader.setBorder(new EmptyBorder(15, 25, 15, 25));
        
        lblWelcome = new JLabel("歡迎回來，" + currentUser.getUsername() + " | 現有代幣: " + currentUser.getTokens() + " 枚 (" + ("ADMIN".equals(currentUser.getRole()) ? "管理員" : "一般會員") + ")");
        lblWelcome.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        lblWelcome.setForeground(Color.WHITE);
        pnlHeader.add(lblWelcome, BorderLayout.WEST);

        // 右側按鈕控制列
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlControls.setOpaque(false);

        // 🟢 核心功能：建立管理後台按鈕 (灰藍色主題)
        btnAdminConsole = new JButton("進入後台管理") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(100, 116, 139)); // 灰藍色
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnAdminConsole.setPreferredSize(new Dimension(120, 34));
        btnAdminConsole.setContentAreaFilled(false);
        btnAdminConsole.setBorderPainted(false);
        btnAdminConsole.setFocusPainted(false);
        btnAdminConsole.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnAdminConsole.addActionListener(e -> {
            // 點擊開啟後台對話框
            AdminConsoleDialog dialog = new AdminConsoleDialog(this); // 傳入大廳作為 parent
            dialog.setVisible(true);
        });
        pnlControls.add(btnAdminConsole);

        // 登出系統按鈕
        JButton btnLogout = new JButton("登出系統") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 38, 38)); // 鮮紅色
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnLogout.setPreferredSize(new Dimension(100, 34));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnLogout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "確定要登出並返回登入畫面嗎？", "登出系統", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                this.dispose();
                EventQueue.invokeLater(() -> {
                    new LoginFrame().setVisible(true);
                });
            }
        });
        pnlControls.add(btnLogout);

        pnlHeader.add(pnlControls, BorderLayout.EAST);
        contentPane.add(pnlHeader, BorderLayout.NORTH);

        // 🟢 核心驗證防線：依據當前登入使用者的 Role，動態開關後台按鈕的可見度
        if (btnAdminConsole != null) {
            btnAdminConsole.setVisible("ADMIN".equals(currentUser.getRole()));
        }

        // ===================================================================
        // 2. 中央遊戲卡片矩陣區 (Center)
        // ===================================================================
        JPanel pnlGrid = new JPanel(new GridLayout(0, 3, 25, 25)); // 橫向並排三張大卡片
        pnlGrid.setOpaque(false);

        // 卡片實例：建立你的圖靈解密卡片 (傳入圖片名稱)
        pnlGrid.add(createGameCard("圖靈解密 (Turing Machine)", "演繹邏輯謎題，挑戰神祕的三位數密碼。", "turing_cover.png", e -> {
            this.dispose(); // 關閉大廳
            new MainFrame(currentUser).setVisible(true); // 奔向遊戲主介面
        }));

        // 未來擴充卡片：灰化鎖定狀態
        pnlGrid.add(createLockedGameCard("血色鐘樓 (Blood on the Clocktower)", "經典語音陣營推理桌遊 (即將推出)", "blood_cover.png"));
        pnlGrid.add(createLockedGameCard("克隆邏輯 (Kronologic)", "引人入勝的時空線索演繹難題 (即將推出)", "kronologic_cover.png"));

        contentPane.add(pnlGrid, BorderLayout.CENTER);
    }

    /**
     * 高質感圓角遊戲卡片產生器 (內含智慧圖片載入與等比例裁切)
     */
    private JPanel createGameCard(String title, String desc, String imgFileName, ActionListener onStart) {
        JPanel card = new JPanel(new BorderLayout(12, 12)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16)); // 畫精美底色圓角
                g2.dispose();
            }
        };
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel pnlBody = new JPanel();
        pnlBody.setOpaque(false);
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));

        // 🖼️ 1. 智慧圓角圖片加載區
        JLabel lblImage = new JLabel();
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        ImageIcon originalIcon = null;
        java.net.URL imgUrl = LobbyFrame.class.getResource("/cards/" + imgFileName);
        if (imgUrl != null) {
            originalIcon = new ImageIcon(imgUrl);
        } else {
            File file = new File("cards/" + imgFileName);
            if (file.exists()) originalIcon = new ImageIcon(file.getAbsolutePath());
        }

        if (originalIcon != null) {
            Image origImg = originalIcon.getImage();
            lblImage.setIcon(new ImageIcon(getRoundedAndScaledImage(origImg, 340, 180, 12))); 
        } else {
            lblImage.setText("<html><center>📷 暫無遊戲封面圖<br><small>" + imgFileName + "</small></center></html>");
            lblImage.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 12));
            lblImage.setForeground(Color.LIGHT_GRAY);
            lblImage.setOpaque(true);
            lblImage.setBackground(new Color(241, 245, 249));
            lblImage.setPreferredSize(new Dimension(340, 180));
            lblImage.setMaximumSize(new Dimension(340, 180));
            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
        }
        pnlBody.add(lblImage);
        pnlBody.add(Box.createVerticalStrut(15)); // 間距

        // 📝 2. 標題與說明文字
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Microsoft JhengHei", Font.BOLD, 16));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlBody.add(lblTitle);
        pnlBody.add(Box.createVerticalStrut(8));

        JLabel lblDesc = new JLabel("<html><center>" + desc + "</center></html>");
        lblDesc.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        lblDesc.setForeground(Color.GRAY);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlBody.add(lblDesc);

        card.add(pnlBody, BorderLayout.CENTER);

        // 🎯 3. 下方啟動按鈕
        JButton btnStart = new JButton("啟動遊戲");
        btnStart.setFont(new Font("Microsoft JhengHei", Font.BOLD, 13));
        btnStart.setBackground(new Color(30, 41, 59));
        btnStart.setForeground(Color.BLACK);
        btnStart.setFocusPainted(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.setPreferredSize(new Dimension(0, 36));
        if (onStart != null) btnStart.addActionListener(onStart);
        card.add(btnStart, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createLockedGameCard(String title, String desc, String imgFileName) {
        JPanel card = createGameCard(title, desc, imgFileName, null);
        JButton btn = (JButton) card.getComponent(1);
        btn.setEnabled(false);
        btn.setText("尚未解鎖 (Coming Soon)");
        btn.setBackground(Color.LIGHT_GRAY);
        return card;
    }

    /**
     * 🧠 智慧同步縮放與圓角重繪引擎
     */
    private BufferedImage getRoundedAndScaledImage(Image srcImage, int targetWidth, int targetHeight, int cornerRadius) {
        BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        g2.setComposite(AlphaComposite.Src);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, targetWidth, targetHeight, cornerRadius, cornerRadius));
        
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(srcImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        
        return output;
    }
}