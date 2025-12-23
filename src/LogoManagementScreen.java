import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class LogoManagementScreen extends JPanel {
    private int userId;
    private String userRole;
    private JLabel primaryLogoLabel, reportLogoLabel;
    private JButton uploadPrimaryBtn, uploadReportBtn, resetPrimaryBtn, resetReportBtn, backBtn;
    
    public LogoManagementScreen(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;
        setBackground(new Color(18, 22, 27));
        initUI();
        loadCurrentLogos();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        backBtn = createStyledButton("← BACK", new Color(70, 75, 85), new Color(90, 95, 105));
        backBtn.addActionListener(e -> goBack());
        headerPanel.add(backBtn, BorderLayout.WEST);
        
        JLabel titleLabel = new JLabel("LOGO MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        mainPanel.setOpaque(false);
        
        // Primary Logo Card
        JPanel primaryCard = createLogoCard(
            "PRIMARY LOGO", 
            "Login & Dashboard",
            "Circular format, 200x200px"
        );
        
        // Report Logo Card
        JPanel reportCard = createLogoCard(
            "REPORT LOGO", 
            "Reports & Documents", 
            "Circular format, 200x200px"
        );
        
        mainPanel.add(primaryCard);
        mainPanel.add(reportCard);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Get references
        primaryLogoLabel = getLogoLabelFromCard(primaryCard);
        reportLogoLabel = getLogoLabelFromCard(reportCard);
        uploadPrimaryBtn = getUploadButtonFromCard(primaryCard);
        uploadReportBtn = getUploadButtonFromCard(reportCard);
        resetPrimaryBtn = getResetButtonFromCard(primaryCard);
        resetReportBtn = getResetButtonFromCard(reportCard);
        
        // Add action listeners
        uploadPrimaryBtn.addActionListener(e -> uploadLogo(LogoManager.LOGO_PRIMARY, primaryLogoLabel));
        uploadReportBtn.addActionListener(e -> uploadLogo(LogoManager.LOGO_REPORT, reportLogoLabel));
        resetPrimaryBtn.addActionListener(e -> resetLogo(LogoManager.LOGO_PRIMARY, primaryLogoLabel));
        resetReportBtn.addActionListener(e -> resetLogo(LogoManager.LOGO_REPORT, reportLogoLabel));
        
        // Footer
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JLabel footerLabel = new JLabel("For circular logos, upload square images (1:1 ratio) for best results");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerLabel.setForeground(new Color(150, 150, 150));
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createLogoCard(String title, String description, String specs) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(new Color(40, 45, 55));
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(60, 65, 75), 1),
            new EmptyBorder(25, 25, 25, 25)
        ));
        
        // Title
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(0, 173, 181));
        titleLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Circular logo display area
        JPanel logoContainer = new JPanel(new GridBagLayout());
        logoContainer.setOpaque(false);
        logoContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Circular panel
        JPanel circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glow effect
                g2d.setColor(new Color(0, 173, 181, 30));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(5, 5, getWidth()-10, getHeight()-10);
                
                g2d.dispose();
            }
        };
        circlePanel.setLayout(new BorderLayout());
        circlePanel.setPreferredSize(new Dimension(180, 180));
        circlePanel.setOpaque(false);
        
        // Circular logo label
        JLabel logoLabel = new JLabel() {
            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Circular clip for CLEAR display
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
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        circlePanel.add(logoLabel, BorderLayout.CENTER);
        logoContainer.add(circlePanel);
        
        card.add(logoContainer, BorderLayout.CENTER);
        
        // Description
        JLabel descLabel = new JLabel("<html><div style='text-align: center; color: #aaa'>" + description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
        
        // Specs
        JLabel specsLabel = new JLabel("<html><div style='text-align: center; color: #888; font-size: 10px'>" + specs + "</div></html>");
        specsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        specsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(descLabel, BorderLayout.NORTH);
        textPanel.add(specsLabel, BorderLayout.SOUTH);
        
        card.add(textPanel, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JButton uploadBtn = createStyledButton("UPLOAD", new Color(0, 173, 181), new Color(0, 150, 160));
        JButton resetBtn = createStyledButton("RESET", new Color(120, 120, 120), new Color(100, 100, 100));
        
        buttonPanel.add(uploadBtn);
        buttonPanel.add(resetBtn);
        
        // Store references
        card.putClientProperty("logoLabel", logoLabel);
        card.putClientProperty("uploadBtn", uploadBtn);
        card.putClientProperty("resetBtn", resetBtn);
        
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(card, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
        
        return container;
    }
    
    private JLabel getLogoLabelFromCard(JPanel card) {
        return (JLabel) ((JPanel) card.getComponent(0)).getClientProperty("logoLabel");
    }
    
    private JButton getUploadButtonFromCard(JPanel card) {
        return (JButton) ((JPanel) card.getComponent(0)).getClientProperty("uploadBtn");
    }
    
    private JButton getResetButtonFromCard(JPanel card) {
        return (JButton) ((JPanel) card.getComponent(0)).getClientProperty("resetBtn");
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(bgColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, 
                    (getWidth() - fm.stringWidth(text)) / 2,
                    (getHeight() + fm.getAscent()) / 2 - 4);
                
                g2d.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 40));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }
    
    private void loadCurrentLogos() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Load CIRCULAR logos (160x160 for clear circular display)
                ImageIcon primaryLogo = LogoManager.getLogo(LogoManager.LOGO_PRIMARY, 160, 160);
                ImageIcon reportLogo = LogoManager.getLogo(LogoManager.LOGO_REPORT, 160, 160);
                
                SwingUtilities.invokeLater(() -> {
                    primaryLogoLabel.setIcon(primaryLogo);
                    reportLogoLabel.setIcon(reportLogo);
                });
                return null;
            }
        };
        worker.execute();
    }
    
    private void uploadLogo(String logoType, JLabel logoLabel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Circular Logo (Square Image)");
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "PNG Images (Best for circular)", "png"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try {
                // Load original image
                ImageIcon originalIcon = new ImageIcon(file.getAbsolutePath());
                
                // Create HIGH QUALITY circular image
                int size = 160; // Square size for circular logo
                BufferedImage circularImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = circularImage.createGraphics();
                
                // Set HIGHEST QUALITY rendering
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Apply circular clip
                g2d.setClip(new Ellipse2D.Float(0, 0, size, size));
                
                // Draw image
                g2d.drawImage(originalIcon.getImage(), 0, 0, size, size, null);
                g2d.dispose();
                
                ImageIcon circularIcon = new ImageIcon(circularImage);
                
                // Convert to base64
                String base64Image = LogoManager.convertImageToBase64(circularIcon);
                if (base64Image != null) {
                    // Update in database
                    LogoManager.updateLogo(logoType, base64Image, userId);
                    
                    // Update display
                    logoLabel.setIcon(circularIcon);
                    
                    JOptionPane.showMessageDialog(this, 
                        "Circular logo updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(),
                    "Upload Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void resetLogo(String logoType, JLabel logoLabel) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Reset to default circular logo?",
            "Confirm Reset",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            LogoManager.updateLogo(logoType, LogoManager.DEFAULT_LOGO_BASE64, userId);
            ImageIcon defaultLogo = LogoManager.getLogo(logoType, 160, 160);
            logoLabel.setIcon(defaultLogo);
            
            JOptionPane.showMessageDialog(this, 
                "Logo reset to default",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void goBack() {
        if ("admin".equals(userRole)) {
            ScreenManager.getInstance().showScreen(new AdminDashboard(userId, getEmployeeName(userId)));
        } else {
            ScreenManager.getInstance().showScreen(new EmployeeDashboard(userId, getEmployeeName(userId)));
        }
    }
    
    private String getEmployeeName(int employeeId) {
        return "Admin";
    }
}