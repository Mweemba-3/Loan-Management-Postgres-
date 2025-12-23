import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.sql.*;

public class LoginScreen extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel logoLabel;
    private JLabel statusLabel;
    private boolean isLoggingIn = false;

    public LoginScreen() {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 22, 27));
        initUI();
    }

    private void initUI() {
        // Main container
        JPanel mainPanel = new JPanel(new BorderLayout(30, 30)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Dark gradient background
                Color color1 = new Color(18, 22, 27);
                Color color2 = new Color(28, 32, 40);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle grid pattern
                g2d.setColor(new Color(255, 255, 255, 5));
                int gridSize = 50;
                for (int x = 0; x < getWidth(); x += gridSize) {
                    g2d.drawLine(x, 0, x, getHeight());
                }
                for (int y = 0; y < getHeight(); y += gridSize) {
                    g2d.drawLine(0, y, getWidth(), y);
                }
                
                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Left Panel - Brand Section
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(25, 30, 40, 180));
        leftPanel.setOpaque(true);
        leftPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Circular logo container
        JPanel logoContainer = new JPanel(new BorderLayout());
        logoContainer.setOpaque(false);
        logoContainer.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // Circular panel for logo with glow effect
        JPanel circlePanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Circular glow effect
                g2d.setColor(new Color(0, 173, 181, 20));
                for (int i = 0; i < 3; i++) {
                    int offset = i * 5;
                    g2d.drawOval(offset, offset, getWidth()-offset*2, getHeight()-offset*2);
                }
                
                g2d.dispose();
            }
        };
        circlePanel.setOpaque(false);
        circlePanel.setPreferredSize(new Dimension(180, 180));
        
        // Circular logo label
        logoLabel = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Apply circular clip
                g2d.setClip(new Ellipse2D.Float(0, 0, getWidth(), getHeight()));
                
                ImageIcon icon = (ImageIcon) getIcon();
                if (icon != null) {
                    Image img = icon.getImage();
                    g2d.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                }
                
                g2d.dispose();
            }
        };
        logoLabel.setPreferredSize(new Dimension(160, 160));
        loadLogoAsync();
        
        circlePanel.add(logoLabel);
        logoContainer.add(circlePanel, BorderLayout.CENTER);
        
        // Company name with clear visibility
        JLabel companyLabel = new JLabel("MS CODEFORGE");
        companyLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        companyLabel.setForeground(new Color(0, 173, 181));
        companyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Tagline
        JLabel taglineLabel = new JLabel("Loan Management System");
        taglineLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        taglineLabel.setForeground(new Color(180, 180, 180));
        taglineLabel.setHorizontalAlignment(SwingConstants.CENTER);
        taglineLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Arrange left panel components
        JPanel leftContent = new JPanel();
        leftContent.setLayout(new BoxLayout(leftContent, BoxLayout.Y_AXIS));
        leftContent.setOpaque(false);
        leftContent.add(logoContainer);
        leftContent.add(companyLabel);
        leftContent.add(taglineLabel);
        
        leftPanel.add(leftContent);

        // Right Panel - Login Form
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setOpaque(false);
        rightPanel.setBorder(new EmptyBorder(0, 60, 0, 0));
        
        // Form container with clean border
        JPanel formContainer = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Clean form background
                g2d.setColor(new Color(40, 45, 55, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Border
                g2d.setColor(new Color(0, 173, 181, 80));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                
                g2d.dispose();
            }
        };
        formContainer.setOpaque(false);
        formContainer.setBorder(new EmptyBorder(40, 40, 40, 40));
        formContainer.setPreferredSize(new Dimension(400, 450));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Secure Login");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        formContainer.add(titleLabel, gbc);

        // Username field
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 8, 0);
        formContainer.add(userLabel, gbc);

        usernameField = createStyledTextField();
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        formContainer.add(usernameField, gbc);

        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setForeground(new Color(200, 200, 200));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 8, 0);
        formContainer.add(passLabel, gbc);

        passwordField = createStyledPasswordField();
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 30, 0);
        formContainer.add(passwordField, gbc);

        // Login Button - BIG AND CLEAR
        loginButton = new JButton("LOGIN");
        styleLoginButton(loginButton);
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 15, 0);
        formContainer.add(loginButton, gbc);

        // Status label
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(255, 100, 100));
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 0, 10, 0);
        formContainer.add(statusLabel, gbc);

        // Simple forgot password
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        forgotPanel.setOpaque(false);
        JLabel forgotLabel = new JLabel("Need help? Contact Admin: +260 123 456 789");
        forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        forgotLabel.setForeground(new Color(150, 150, 150));
        forgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginScreen.this,
                    "Contact System Administrator:\nPhone: +260 123 456 789\nEmail: admin@mscodeforge.com",
                    "Support", JOptionPane.INFORMATION_MESSAGE);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotLabel.setForeground(new Color(0, 173, 181));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                forgotLabel.setForeground(new Color(150, 150, 150));
            }
        });
        forgotPanel.add(forgotLabel);
        gbc.gridy = 7;
        gbc.insets = new Insets(10, 0, 0, 0);
        formContainer.add(forgotPanel, gbc);

        rightPanel.add(formContainer);
        
        // Add panels to main
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Add enter key listener
        passwordField.addActionListener(e -> performLogin());
        
        revalidate();
        repaint();
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(320, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(50, 55, 65));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 75, 85), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setCaretColor(new Color(0, 173, 181));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 173, 181), 2),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 75, 85), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        
        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(320, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBackground(new Color(50, 55, 65));
        field.setForeground(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 75, 85), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setCaretColor(new Color(0, 173, 181));
        field.setEchoChar('•');
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0, 173, 181), 2),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 75, 85), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
        });
        
        return field;
    }

    private void styleLoginButton(JButton button) {
        button.setPreferredSize(new Dimension(320, 50));
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(0, 173, 181));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(0, 190, 200));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(0, 173, 181));
            }
        });
        
        button.addActionListener(e -> performLogin());
    }

    private void loadLogoAsync() {
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    // Load CIRCULAR logo (160x160 for circular display)
                    return LogoManager.getLogo(LogoManager.LOGO_PRIMARY, 160, 160);
                } catch (Exception e) {
                    return createClearCircularLogo();
                }
            }
            
            @Override
            protected void done() {
                try {
                    ImageIcon logo = get();
                    logoLabel.setIcon(logo);
                    logoLabel.setText(""); // Clear any text
                } catch (Exception e) {
                    logoLabel.setIcon(createClearCircularLogo());
                }
            }
        };
        worker.execute();
    }

    private ImageIcon createClearCircularLogo() {
        BufferedImage img = new BufferedImage(160, 160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        // Circular background
        g2d.setColor(new Color(0, 173, 181));
        g2d.fillOval(0, 0, 160, 160);
        
        // Clear text for circular logo
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 28));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "MS";
        
        // Center text properly in circle
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int x = (160 - textWidth) / 2;
        int y = (160 - textHeight) / 2 + fm.getAscent() - 20;
        
        g2d.drawString(text, x, y);
        
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
        fm = g2d.getFontMetrics();
        text = "CF";
        textWidth = fm.stringWidth(text);
        x = (160 - textWidth) / 2;
        y = (160 - textHeight) / 2 + fm.getAscent() + 20;
        
        g2d.drawString(text, x, y);
        
        g2d.dispose();
        return new ImageIcon(img);
    }

    private void performLogin() {
        if (isLoggingIn) return;
        
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            return;
        }

        isLoggingIn = true;
        loginButton.setEnabled(false);
        loginButton.setText("AUTHENTICATING...");
        statusLabel.setText("");

        SwingWorker<LoginResult, Void> worker = new SwingWorker<LoginResult, Void>() {
            @Override
            protected LoginResult doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql = "SELECT employee_id, name, role FROM employees WHERE name = ? AND password = ? AND is_active = TRUE";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, username);
                        stmt.setString(2, password);
                        
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                int id = rs.getInt("employee_id");
                                String name = rs.getString("name");
                                String role = rs.getString("role");
                                return new LoginResult(true, null, new UserData(id, name, role));
                            } else {
                                return new LoginResult(false, "Invalid username or password", null);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    return new LoginResult(false, "Database connection error", null);
                }
            }

            @Override
            protected void done() {
                try {
                    LoginResult result = get();
                    if (result.success) {
                        statusLabel.setText("Login successful!");
                        statusLabel.setForeground(new Color(0, 200, 100));
                        
                        Timer timer = new Timer(500, e -> {
                            if ("admin".equals(result.userData.role)) {
                                ScreenManager.getInstance().showScreen(new AdminDashboard(result.userData.id, result.userData.name));
                            } else {
                                ScreenManager.getInstance().showScreen(new EmployeeDashboard(result.userData.id, result.userData.name));
                            }
                        });
                        timer.setRepeats(false);
                        timer.start();
                        
                    } else {
                        statusLabel.setText(result.message);
                        statusLabel.setForeground(new Color(255, 100, 100));
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Login failed. Try again.");
                    statusLabel.setForeground(new Color(255, 100, 100));
                } finally {
                    isLoggingIn = false;
                    loginButton.setEnabled(true);
                    loginButton.setText("LOGIN TO SYSTEM");
                }
            }
        };
        worker.execute();
    }

    private static class LoginResult {
        boolean success;
        String message;
        UserData userData;
        
        LoginResult(boolean success, String message, UserData userData) {
            this.success = success;
            this.message = message;
            this.userData = userData;
        }
    }
    
    private static class UserData {
        int id;
        String name;
        String role;
        
        UserData(int id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }
    }
}