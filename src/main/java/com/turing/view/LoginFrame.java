package com.turing.view;

import com.turing.controller.UserController;
import com.turing.model.User;
import com.turing.exception.GameException;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern Morandi style Login and Registration window.
 * Serves as the starting entry point for the Turing Machine game.
 */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final UserController userController;

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegister;

    private static final Color COLOR_BG = new Color(248, 250, 252);
    private static final Color COLOR_CARD = Color.WHITE;
    private static final Color COLOR_PRIMARY = new Color(30, 41, 59); // Dark Slate Blue
    private static final Color COLOR_GREEN = new Color(46, 175, 107);
    private static final Color COLOR_TEXT = new Color(44, 62, 80);
    private static final Font FONT_MAIN = new Font("Microsoft JhengHei", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Microsoft JhengHei", Font.BOLD, 14);
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

    public LoginFrame() {
        this.userController = new UserController();

        setTitle("登入");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 420);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel();
        contentPane.setBackground(COLOR_BG);
        contentPane.setBorder(new EmptyBorder(25, 25, 25, 25));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 15));

        // Header Panel
        JPanel pnlHeader = new JPanel();
        pnlHeader.setOpaque(false);
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        
        JLabel lblTitle = new JLabel("解密系統...", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_PRIMARY);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);

        pnlHeader.add(Box.createVerticalStrut(5));

        JLabel lblSub = new JLabel("Code Decryption Engine...", SwingConstants.CENTER);
        lblSub.setFont(new Font("Consolas", Font.PLAIN, 12));
        lblSub.setForeground(new Color(100, 116, 139));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblSub);

        contentPane.add(pnlHeader, BorderLayout.NORTH);

        // Center Login Form Panel (Morandi Card Style)
        JPanel pnlForm = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        pnlForm.setOpaque(false);
        pnlForm.setBorder(new EmptyBorder(20, 25, 20, 25));
        pnlForm.setLayout(new GridLayout(4, 1, 8, 8));

        // Username section
        JPanel pnlUserLabel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlUserLabel.setOpaque(false);
        JLabel lblUser = new JLabel("帳號名稱 (Username):");
        lblUser.setFont(FONT_BOLD);
        lblUser.setForeground(COLOR_TEXT);
        pnlUserLabel.add(lblUser);
        pnlForm.add(pnlUserLabel);

        txtUsername = new JTextField();
        txtUsername.setFont(FONT_MAIN);
        txtUsername.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(new Color(203, 213, 225), 1, 8),
                new EmptyBorder(4, 10, 4, 10)
        ));
        pnlForm.add(txtUsername);

        // Password section
        JPanel pnlPassLabel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlPassLabel.setOpaque(false);
        JLabel lblPass = new JLabel("登入密碼 (Password):");
        lblPass.setFont(FONT_BOLD);
        lblPass.setForeground(COLOR_TEXT);
        pnlPassLabel.add(lblPass);
        pnlForm.add(pnlPassLabel);

        txtPassword = new JPasswordField();
        txtPassword.setFont(FONT_MAIN);
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(new Color(203, 213, 225), 1, 8),
                new EmptyBorder(4, 10, 4, 10)
        ));
        pnlForm.add(txtPassword);

        contentPane.add(pnlForm, BorderLayout.CENTER);

        // Bottom Actions Button Panel
        JPanel pnlActions = new JPanel(new GridLayout(1, 2, 12, 0));
        pnlActions.setOpaque(false);
        pnlActions.setPreferredSize(new Dimension(0, 40));

        btnLogin = new ModernButton("帳號登入 (Sign In)", COLOR_PRIMARY, true);
        btnRegister = new ModernButton("註冊玩家 (Sign Up)", COLOR_GREEN, false);

        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> handleRegister());

        pnlActions.add(btnRegister);
        pnlActions.add(btnLogin);

        contentPane.add(pnlActions, BorderLayout.SOUTH);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        try {
            User user = userController.login(username, password);
            JOptionPane.showMessageDialog(this, 
                "歡迎回來， " + user.getUsername() + "！\n您的身分角色是： " + 
                ("ADMIN".equals(user.getRole()) ? "系統管理員" : "一般會員") + 
                "\n目前身上擁有的代幣數： " + user.getTokens() + " 枚",
                "登入成功", JOptionPane.INFORMATION_MESSAGE);
            
            // Dispose the login window and open the MainFrame with User context!
            this.dispose();
            EventQueue.invokeLater(() -> {
                try {
                	LobbyFrame lobbyFrame = new LobbyFrame(user);
                    lobbyFrame.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "開啟遊戲主介面失敗: " + ex.getMessage());
                }
            });
        } catch (GameException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "登入失敗", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "系統錯誤: " + e.getMessage(), "登入失敗", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "請在上方輸入框填寫欲註冊的帳號與密碼！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this, 
            "確定要註冊新玩家帳號：「" + username + "」嗎？", 
            "註冊確認", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                User newUser = userController.register(username, password);
                JOptionPane.showMessageDialog(this, 
                    "🎉 恭喜！玩家「" + newUser.getUsername() + "」註冊成功！\n" +
                    "系統贈送您 10 枚新玩家代幣，請直接點擊「帳號登入」按鈕開始遊戲遊玩！",
                    "註冊成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (GameException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "註冊失敗", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class ModernButton extends JButton {
        private final Color baseColor;
        private final boolean isFilled;

        public ModernButton(String text, Color baseColor, boolean isFilled) {
            super(text);
            this.baseColor = baseColor;
            this.isFilled = isFilled;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(FONT_BOLD);
            setForeground(isFilled ? Color.WHITE : baseColor);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isFilled) {
                g2.setColor(baseColor);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
            } else {
                g2.setColor(COLOR_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(baseColor);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));
            }
            g2.setFont(FONT_BOLD);
            g2.setColor(isFilled ? Color.WHITE : baseColor);
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
}
