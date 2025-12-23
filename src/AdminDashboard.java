import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.io.ByteArrayInputStream;

public class AdminDashboard extends JPanel {
    private int employeeId;
    private String employeeName;
    private DoughnutChart doughnutChart;
    private JLabel welcomeLabel;
    private Timer animationTimer;
    private float animationProgress = 0f;
    private BufferedImage companyLogo = null;
    
    // Loading dialog for async operations
    private JDialog loadingDialog;

    public AdminDashboard(int employeeId, String employeeName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        
        // Set up uncaught exception handler for this panel
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception in AdminDashboard: " + throwable.getMessage());
            SwingUtilities.invokeLater(() -> {
                showError("An error occurred: " + throwable.getMessage());
            });
        });
        
        initUI();
        loadDashboardStats();
        loadCompanyLogoInOriginalShape();
        startAnimation();
        asyncLogActivity("Dashboard Access", "Accessed admin dashboard");
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(34, 40, 49));
        
        // Create sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // Main content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(45, 52, 64));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Center Panel with Doughnut Chart
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(45, 52, 64));
        
        // Welcome and refresh panel
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(45, 52, 64));
        welcomePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        welcomeLabel = new JLabel("Welcome back, " + employeeName + "!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(200, 200, 200));
        
        JButton refreshBtn = new JButton("Refresh Dashboard");
        styleButton(refreshBtn, new Color(70, 130, 180), new Color(60, 120, 170));
        refreshBtn.addActionListener(e -> refreshDashboard());
        
        welcomePanel.add(welcomeLabel, BorderLayout.WEST);
        welcomePanel.add(refreshBtn, BorderLayout.EAST);
        
        // Doughnut Chart Panel
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(new Color(57, 62, 70));
        chartPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 70, 70), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        doughnutChart = new DoughnutChart();
        chartPanel.add(doughnutChart, BorderLayout.CENTER);
        
        // Chart legend
        JPanel legendPanel = createLegendPanel();
        chartPanel.add(legendPanel, BorderLayout.SOUTH);
        
        centerPanel.add(welcomePanel, BorderLayout.NORTH);
        centerPanel.add(chartPanel, BorderLayout.CENTER);
        
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(45, 52, 64));
        headerPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(70, 70, 70)));
        
        JLabel titleLabel = new JLabel("ADMIN DASHBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 173, 181));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel roleLabel = new JLabel("Role: Administrator | ID: " + employeeId);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(Color.WHITE);
        headerPanel.add(roleLabel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 64));
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));
        
        addLogo(sidebar);
        addMenuItems(sidebar);
        addBackupButton(sidebar);
        
        // Add vertical glue to push footer to bottom
        sidebar.add(Box.createVerticalGlue());
        
        addLogoutButton(sidebar);
        
        return sidebar;
    }

    private void addLogo(JPanel sidebar) {
        JLabel logoLabel = new JLabel("MS CODEFORGE");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setForeground(new Color(0, 173, 181));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        sidebar.add(logoLabel);
    }

    private void addMenuItems(JPanel sidebar) {
        String[] menuItems = {"Clients", "Loans", "Payments", "📊 Activities","📊 Logo Management", "Employees", "🔒 Change Password"};
        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebar.add(menuButton);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }
    }

    private void addBackupButton(JPanel sidebar) {
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton backupBtn = createMenuButton("Backup Today");
        backupBtn.addActionListener(e -> generateBackup());
        sidebar.add(backupBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void addLogoutButton(JPanel sidebar) {
        JButton logoutButton = createLogoutButton();
        sidebar.add(logoutButton);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(57, 62, 70));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { 
                button.setBackground(new Color(70, 76, 85)); 
            }
            @Override public void mouseExited(MouseEvent e) { 
                button.setBackground(new Color(57, 62, 70)); 
            }
        });
        
        button.addActionListener(e -> {
            button.setEnabled(false);
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    handleMenuClickSync(text);
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get();
                        asyncLogActivity("Navigation", "Accessed " + 
                            text.substring(text.indexOf(" ") + 1).trim() + " section");
                    } catch (Exception ex) {
                        System.err.println("Menu action failed: " + ex.getMessage());
                        showError("Action failed: " + ex.getMessage());
                    } finally {
                        button.setEnabled(true);
                    }
                }
            };
            worker.execute();
        });
        
        return button;
    }

    private JButton createLogoutButton() {
        JButton button = new JButton("LOGOUT");
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 107, 107));
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { 
                button.setBackground(new Color(255, 77, 77)); 
            }
            @Override public void mouseExited(MouseEvent e) { 
                button.setBackground(new Color(255, 107, 107)); 
            }
        });
        
        button.addActionListener(e -> {
            button.setEnabled(false);
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    asyncLogActivity("Logout", "User logged out from system");
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get();
                        SwingUtilities.invokeLater(() -> 
                            ScreenManager.getInstance().showScreen(new LoginScreen()));
                    } catch (Exception ex) {
                        System.err.println("Logout failed: " + ex.getMessage());
                        button.setEnabled(true);
                    }
                }
            };
            worker.execute();
        });
        return button;
    }

    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });
    }

    private JPanel createLegendPanel() {
        JPanel legendPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        legendPanel.setBackground(new Color(57, 62, 70));
        legendPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        Color[] colors = {
            new Color(0, 173, 181),    // Total Clients - Teal
            new Color(97, 218, 121),   // Active Loans - Green
            new Color(255, 107, 107),  // Due Payments - Red
            new Color(255, 159, 67)    // Pending - Orange
        };
        
        String[] labels = {"Total Clients", "Active Loans", "Due Payments", "Pending Loans"};
        
        for (int i = 0; i < labels.length; i++) {
            JPanel legendItem = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            legendItem.setBackground(new Color(57, 62, 70));
            
            JLabel colorLabel = new JLabel("■");
            colorLabel.setForeground(colors[i]);
            colorLabel.setFont(new Font("Arial", Font.BOLD, 16));
            
            JLabel textLabel = new JLabel(labels[i]);
            textLabel.setForeground(Color.WHITE);
            textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            
            legendItem.add(colorLabel);
            legendItem.add(textLabel);
            legendPanel.add(legendItem);
        }
        
        return legendPanel;
    }

    private void handleMenuClickSync(String menuItem) {
        String menuText = menuItem.substring(menuItem.indexOf(" ") + 1).trim();
        
        SwingUtilities.invokeLater(() -> {
            switch (menuText) {
                case "Clients":
                    ScreenManager.getInstance().showScreen(new ClientsScreen(employeeId, "admin"));
                    break;
                case "Loans":
                    ScreenManager.getInstance().showScreen(new LoansScreen(employeeId, "admin"));
                    break;
                case "Payments":
                    ScreenManager.getInstance().showScreen(new PaymentsScreen(employeeId, "admin"));
                    break;
                case "Activities":
                    ScreenManager.getInstance().showScreen(new ActivitiesScreen(employeeId, "admin"));
                    break;
                case "Logo Management":
                    ScreenManager.getInstance().showScreen(new LogoManagementScreen(employeeId, "admin"));
                    break;
                case "Employees":
                    ScreenManager.getInstance().showScreen(new EmployeesScreen(employeeId, "admin"));
                    break;
                case "Change Password":
                    ScreenManager.getInstance().showScreen(new ChangePasswordScreen(employeeId, "admin", employeeName));
                    break;
            }
        });
    }

    private void loadDashboardStats() {
        SwingWorker<Map<String, Integer>, Void> worker = new SwingWorker<Map<String, Integer>, Void>() {
            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                return fetchDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Integer> stats = get();
                    updateDoughnutChart(stats);
                } catch (Exception ex) {
                    showDatabaseError("Error loading dashboard statistics");
                }
            }
        };
        worker.execute();
    }

    private void loadCompanyLogoInOriginalShape() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        System.err.println("Database connection is null");
                        return null;
                    }

                    // Load logo from database - ORIGINAL SHAPE, not circular
                    String sql = "SELECT setting_value FROM system_settings WHERE setting_key = 'LOGO_PRIMARY'";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setQueryTimeout(5);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                String base64Image = rs.getString("setting_value");
                                if (base64Image != null && !base64Image.trim().isEmpty()) {
                                    // Decode and load original image
                                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                                    companyLogo = javax.imageio.ImageIO.read(new ByteArrayInputStream(imageBytes));
                                    System.out.println("Loaded logo with original dimensions: " + 
                                        companyLogo.getWidth() + "x" + companyLogo.getHeight());
                                }
                            }
                        }
                    }
                } catch (SQLException ex) {
                    System.err.println("SQL Error loading logo: " + ex.getMessage());
                } catch (Exception ex) {
                    System.err.println("Error loading logo: " + ex.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }

    private Map<String, Integer> fetchDashboardStats() {
        Map<String, Integer> stats = new HashMap<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection is null");
                return stats;
            }

            stats.put("totalClients", getCountWithTimeout(conn, "SELECT COUNT(*) FROM clients"));
            stats.put("activeLoans", getCountWithTimeout(conn, 
                "SELECT COUNT(*) FROM loans WHERE status IN ('Active', 'Approved')"));
            stats.put("duePayments", getCountWithTimeout(conn,
                "SELECT COUNT(*) FROM loan_payments " +
                "WHERE status = 'Overdue' AND scheduled_payment_date < CURRENT_DATE"));
            stats.put("pendingLoans", getCountWithTimeout(conn, 
                "SELECT COUNT(*) FROM loans WHERE status = 'Pending'"));
                
        } catch (SQLException ex) {
            System.err.println("SQL Error in fetchDashboardStats: " + ex.getMessage());
        }
        
        return stats;
    }

    private int getCountWithTimeout(Connection conn, String sql) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5); // 5 second timeout
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            System.err.println("Error in getCount: " + e.getMessage());
            return 0;
        }
    }

    private void updateDoughnutChart(Map<String, Integer> stats) {
        int totalClients = stats.getOrDefault("totalClients", 0);
        int activeLoans = stats.getOrDefault("activeLoans", 0);
        int duePayments = stats.getOrDefault("duePayments", 0);
        int pendingLoans = stats.getOrDefault("pendingLoans", 0);
        
        SwingUtilities.invokeLater(() -> {
            doughnutChart.setData(new int[]{totalClients, activeLoans, duePayments, pendingLoans});
        });
    }

    private void startAnimation() {
        animationTimer = new Timer(16, e -> {
            animationProgress += 0.05f;
            if (animationProgress >= 1f) {
                animationProgress = 1f;
                animationTimer.stop();
            }
            doughnutChart.setAnimationProgress(animationProgress);
            doughnutChart.repaint();
        });
        animationTimer.start();
    }

    private void refreshDashboard() {
        JButton refreshBtn = findRefreshButton();
        if (refreshBtn != null) {
            refreshBtn.setEnabled(false);
        }
        
        animationProgress = 0f;
        startAnimation();
        
        SwingWorker<Map<String, Integer>, Void> worker = new SwingWorker<Map<String, Integer>, Void>() {
            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                return fetchDashboardStats();
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Integer> stats = get();
                    updateDoughnutChart(stats);
                    asyncLogActivity("Dashboard Refresh", "Refreshed dashboard statistics");
                } catch (Exception ex) {
                    showDatabaseError("Error refreshing dashboard");
                } finally {
                    if (refreshBtn != null) {
                        refreshBtn.setEnabled(true);
                    }
                }
            }
        };
        worker.execute();
    }
    
    private JButton findRefreshButton() {
        for (Component comp : getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if ("Refresh Dashboard".equals(btn.getText())) {
                    return btn;
                }
            }
        }
        return null;
    }

    private void generateBackup() {
        showLoadingDialog("Generating backup...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                printBackupDirectly();
                return null;
            }

            @Override
            protected void done() {
                hideLoadingDialog();
                try {
                    get();
                } catch (Exception ex) {
                    showError("Error printing backup: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void showLoadingDialog(String message) {
        if (loadingDialog == null) {
            loadingDialog = new JDialog((Frame) null, "Please Wait", true);
            loadingDialog.setSize(300, 100);
            loadingDialog.setLocationRelativeTo(this);
            loadingDialog.setLayout(new BorderLayout());
            
            JLabel label = new JLabel(message, SwingConstants.CENTER);
            loadingDialog.add(label, BorderLayout.CENTER);
            
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            loadingDialog.add(progressBar, BorderLayout.SOUTH);
        }
        
        loadingDialog.setVisible(true);
    }
    
    private void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.setVisible(false);
        }
    }

    private void printBackupDirectly() {
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("MS CODEFORGE - Daily Backup Report");
            
            printerJob.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) 
                        throws PrinterException {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }
                    
                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                    
                    Font titleFont = new Font("Arial", Font.BOLD, 20);
                    Font headerFont = new Font("Arial", Font.BOLD, 14);
                    Font normalFont = new Font("Arial", Font.PLAIN, 10);
                    Font smallFont = new Font("Arial", Font.PLAIN, 9);
                    
                    int y = 50;
                    int lineHeight = 15;
                    
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        if (conn == null || conn.isClosed()) {
                            g2d.drawString("Database connection failed", 50, y);
                            return PAGE_EXISTS;
                        }
                        
                        // COMPANY LOGO AND TITLE SECTION
                        int logoX = 50;
                        int logoY = 50;
                        
                        // Draw company logo in ORIGINAL SHAPE (not circular)
                        if (companyLogo != null) {
                            // Calculate dimensions while maintaining original aspect ratio
                            int maxLogoHeight = 80;
                            double aspectRatio = (double) companyLogo.getWidth() / companyLogo.getHeight();
                            int logoWidth = (int) (maxLogoHeight * aspectRatio);
                            int logoHeight = maxLogoHeight;
                            
                            // Draw logo in ORIGINAL SHAPE - no circular clipping
                            g2d.drawImage(companyLogo, logoX, logoY, logoWidth, logoHeight, null);
                            
                            // Position title to the right of logo
                            int titleX = logoX + logoWidth + 20;
                            g2d.setFont(titleFont);
                            g2d.setColor(Color.BLACK);
                            g2d.drawString("MS CODEFORGE", titleX, logoY + 30);
                            g2d.drawString("DAILY BACKUP REPORT", titleX, logoY + 55);
                            
                            y = logoY + logoHeight + 20;
                        } else {
                            // Fallback: Draw title without logo
                            g2d.setFont(titleFont);
                            g2d.setColor(Color.BLACK);
                            g2d.drawString("MS CODEFORGE - DAILY BACKUP REPORT", 50, y);
                            y += 40;
                        }
                        
                        // DATE AND USER INFO
                        g2d.setFont(normalFont);
                        g2d.drawString("Generated on: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new Date()), 50, y);
                        y += lineHeight;
                        g2d.drawString("Generated by: " + employeeName + " (ID: " + employeeId + ")", 50, y);
                        y += lineHeight;
                        
                        // SEPARATOR LINE
                        g2d.drawLine(50, y, 550, y);
                        y += 20;
                        
                        // 1. INCOME 24 HOURS
                        g2d.setFont(headerFont);
                        g2d.drawString("INCOME (24 HOURS)", 50, y);
                        y += lineHeight + 5;
                        
                        g2d.setFont(smallFont);
                        String income24hr = getIncome24Hours(conn);
                        String[] incomeLines = income24hr.split("\n");
                        for (String line : incomeLines) {
                            g2d.drawString(line, 50, y);
                            y += lineHeight;
                        }
                        y += 10;
                        
                        // 2. PENDING CLIENTS
                        g2d.setFont(headerFont);
                        g2d.drawString("PENDING CLIENTS SUMMARY", 50, y);
                        y += lineHeight + 5;
                        
                        g2d.setFont(smallFont);
                        String pendingClients = getPendingClients(conn);
                        String[] pendingLines = pendingClients.split("\n");
                        for (String line : pendingLines) {
                            g2d.drawString(line, 50, y);
                            y += lineHeight;
                        }
                        y += 10;
                        
                        // 3. PENDING DISBURSEMENTS
                        g2d.setFont(headerFont);
                        g2d.drawString("PENDING DISBURSEMENTS", 50, y);
                        y += lineHeight + 5;
                        
                        g2d.setFont(smallFont);
                        String pendingDisbursements = getPendingDisbursements(conn);
                        String[] disbursementLines = pendingDisbursements.split("\n");
                        for (String line : disbursementLines) {
                            g2d.drawString(line, 50, y);
                            y += lineHeight;
                        }
                        y += 10;
                        
                        // 4. IMPORTANT ACTIVITIES
                        g2d.setFont(headerFont);
                        g2d.drawString("IMPORTANT ACTIVITIES TODAY", 50, y);
                        y += lineHeight + 5;
                        
                        g2d.setFont(smallFont);
                        String importantActivities = getImportantActivities(conn);
                        String[] activityLines = importantActivities.split("\n");
                        for (String line : activityLines) {
                            g2d.drawString(line, 50, y);
                            y += lineHeight;
                        }
                        y += 10;
                        
                        // 5. CLIENT SUMMARY
                        g2d.setFont(headerFont);
                        g2d.drawString("CLIENT SUMMARY", 50, y);
                        y += lineHeight + 5;
                        
                        g2d.setFont(smallFont);
                        String clientSummary = getClientSummary(conn);
                        String[] clientLines = clientSummary.split("\n");
                        for (String line : clientLines) {
                            g2d.drawString(line, 50, y);
                            y += lineHeight;
                        }
                        y += 10;
                        
                        // 6. LOAN SUMMARY
                        g2d.setFont(headerFont);
                        g2d.drawString("LOAN SUMMARY", 50, y);
                        y += lineHeight + 5;
                        
                        g2d.setFont(smallFont);
                        String loanSummary = getLoanSummary(conn);
                        String[] loanLines = loanSummary.split("\n");
                        for (String line : loanLines) {
                            g2d.drawString(line, 50, y);
                            y += lineHeight;
                        }
                        y += 10;
                        
                        // 7. PAYMENT SUMMARY
                        g2d.setFont(headerFont);
                        g2d.drawString("PAYMENT SUMMARY", 50, y);
                        y += lineHeight + 5;
                        
                        g2d.setFont(smallFont);
                        String paymentSummary = getPaymentSummary(conn);
                        String[] paymentLines = paymentSummary.split("\n");
                        for (String line : paymentLines) {
                            g2d.drawString(line, 50, y);
                            y += lineHeight;
                        }
                        
                        // DYNAMIC FOOTER - ALWAYS AT BOTTOM WITH PAGE BREAK PROTECTION
                        int pageHeight = (int) pageFormat.getImageableHeight();
                        int minFooterSpace = 80; // Minimum space needed above footer
                        
                        // Calculate where footer should go
                        int footerY = pageHeight - 40; // Fixed position from bottom
                        
                        // Check if content would collide with footer
                        if (y > footerY - minFooterSpace) {
                            // If content is too close, move footer further down
                            footerY = y + 50;
                        }
                        
                        // Draw footer separator line
                        g2d.setColor(Color.BLACK);
                        g2d.drawLine(50, footerY - 15, 550, footerY - 15);
                        
                        // Draw footer text
                        g2d.setFont(new Font("Arial", Font.ITALIC, 9));
                        String footer = " ";
                        int footerWidth = g2d.getFontMetrics().stringWidth(footer);
                        int footerX = (int)(pageFormat.getImageableWidth()/2 - footerWidth/2);
                        g2d.drawString(footer, footerX, footerY);
                        
                        insertBackupRecord("PRINTED_BACKUP_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()), 
                                          "Printed directly to printer", conn);
                        
                        asyncLogActivity("Backup Printed", "Printed daily backup report directly to printer");
                        
                    } catch (SQLException ex) {
                        g2d.drawString("Database Error: " + ex.getMessage(), 50, y);
                    }
                    
                    return PAGE_EXISTS;
                }
            });
            
            if (printerJob.printDialog()) {
                printerJob.print();
                
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(AdminDashboard.this, 
                        "Backup report sent to printer successfully!", 
                        "Print Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                });
            }
            
        } catch (PrinterException ex) {
            throw new RuntimeException("Printing error: " + ex.getMessage(), ex);
        }
    }

    private String getIncome24Hours(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT " +
                    "SUM(CASE WHEN status = 'Approved' THEN amount ELSE 0 END) as total_income, " +
                    "COUNT(CASE WHEN status = 'Approved' THEN 1 END) as payment_count " +
                    "FROM payment_receipts " +
                    "WHERE created_at >= NOW() - INTERVAL '24 HOURS'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double income = rs.getDouble("total_income");
                    int count = rs.getInt("payment_count");
                    sb.append(String.format("24hr Income: ZMW %,.2f\n", income));
                    sb.append("Payments (24hr): ").append(count);
                } else {
                    sb.append("24hr Income: ZMW 0.00\n");
                    sb.append("Payments (24hr): 0");
                }
            }
        }
        return sb.toString();
    }

    private String getPendingClients(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT " +
                    "COUNT(*) as pending_clients, " +
                    "COUNT(CASE WHEN DATE(created_at) = CURRENT_DATE THEN 1 END) as today_added " +
                    "FROM clients " +
                    "WHERE created_at >= NOW() - INTERVAL '7 DAYS' " +
                    "AND client_id NOT IN (SELECT DISTINCT client_id FROM loans)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sb.append("Pending Clients: ").append(rs.getInt("pending_clients")).append("\n");
                    sb.append("Added Today: ").append(rs.getInt("today_added"));
                } else {
                    sb.append("Pending Clients: 0\n");
                    sb.append("Added Today: 0");
                }
            }
        }
        return sb.toString();
    }

    private String getPendingDisbursements(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT " +
                    "COUNT(*) as pending_count, " +
                    "SUM(amount) as total_amount " +
                    "FROM loans " +
                    "WHERE status = 'Pending' " +
                    "AND application_date >= CURRENT_DATE - INTERVAL '30 DAYS'";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sb.append("Pending Loans: ").append(rs.getInt("pending_count")).append("\n");
                    sb.append(String.format("Total Amount: ZMW %,.2f", rs.getDouble("total_amount")));
                } else {
                    sb.append("Pending Loans: 0\n");
                    sb.append("Total Amount: ZMW 0.00");
                }
            }
        }
        return sb.toString();
    }

    private String getImportantActivities(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT employee_name, action, details, action_date " +
                    "FROM audit_logs " +
                    "WHERE action IN ('Client Added', 'Payment Received', 'Client Deleted', 'Loan Applied', 'Loan Approved', 'Payment Approved') " +
                    "AND DATE(action_date) = CURRENT_DATE " +
                    "ORDER BY action_date DESC " +
                    "LIMIT 10";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    sb.append(String.format("%s: %s - %s\n", 
                        new SimpleDateFormat("HH:mm").format(rs.getTimestamp("action_date")),
                        rs.getString("employee_name"),
                        rs.getString("action")));
                }
                
                if (count == 0) {
                    sb.append("No important activities today.\n");
                }
                sb.append("Total Important: ").append(count);
            }
        }
        
        return sb.toString();
    }

    private String getTodaysActivities(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT employee_name, action, details, action_date " +
                    "FROM audit_logs " +
                    "WHERE DATE(action_date) = CURRENT_DATE " +
                    "ORDER BY action_date DESC";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    sb.append(String.format("%s - %s: %s\n", 
                        new SimpleDateFormat("HH:mm").format(rs.getTimestamp("action_date")),
                        rs.getString("employee_name"),
                        rs.getString("action")));
                }
                
                if (count == 0) {
                    sb.append("No activities recorded today.\n");
                }
                sb.append("Total activities: ").append(count);
            }
        }
        
        return sb.toString();
    }

    private String getClientSummary(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT COUNT(*) as total_clients, " +
                    "COUNT(DISTINCT created_by) as created_by_employees, " +
                    "MAX(created_at) as latest_client " +
                    "FROM clients";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sb.append("Total Clients: ").append(rs.getInt("total_clients")).append("\n");
                    sb.append("Registered by: ").append(rs.getInt("created_by_employees")).append(" employees\n");
                    
                    Timestamp latest = rs.getTimestamp("latest_client");
                    if (latest != null) {
                        sb.append("Latest Client: ").append(new SimpleDateFormat("dd-MMM-yyyy").format(latest));
                    } else {
                        sb.append("Latest Client: N/A");
                    }
                }
            }
        }
        
        return sb.toString();
    }

    private String getLoanSummary(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT " +
                    "COUNT(*) as total_loans, " +
                    "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) as pending_loans, " +
                    "SUM(CASE WHEN status IN ('Approved', 'Active') THEN 1 ELSE 0 END) as active_loans, " +
                    "SUM(CASE WHEN status = 'Rejected' THEN 1 ELSE 0 END) as rejected_loans, " +
                    "SUM(CASE WHEN status = 'Closed' THEN 1 ELSE 0 END) as closed_loans, " +
                    "SUM(amount) as total_amount, " +
                    "SUM(outstanding_balance) as total_outstanding " +
                    "FROM loans";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sb.append("Total Loans: ").append(rs.getInt("total_loans")).append("\n");
                    sb.append("Pending: ").append(rs.getInt("pending_loans")).append("\n");
                    sb.append("Active: ").append(rs.getInt("active_loans")).append("\n");
                    sb.append("Rejected: ").append(rs.getInt("rejected_loans")).append("\n");
                    sb.append("Closed: ").append(rs.getInt("closed_loans")).append("\n");
                    sb.append(String.format("Total Amount: ZMW %,.2f\n", rs.getDouble("total_amount")));
                    sb.append(String.format("Outstanding: ZMW %,.2f", rs.getDouble("total_outstanding")));
                }
            }
        }
        
        return sb.toString();
    }
    
    private String getPaymentSummary(Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT " +
                    "COUNT(*) as total_payments, " +
                    "SUM(amount) as total_amount, " +
                    "SUM(CASE WHEN status = 'Pending' THEN 1 ELSE 0 END) as pending_payments, " +
                    "SUM(CASE WHEN status = 'Approved' THEN 1 ELSE 0 END) as approved_payments, " +
                    "SUM(CASE WHEN status = 'Rejected' THEN 1 ELSE 0 END) as rejected_payments " +
                    "FROM payment_receipts " +
                    "WHERE DATE(created_at) = CURRENT_DATE";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sb.append("Today's Payments: ").append(rs.getInt("total_payments")).append("\n");
                    sb.append(String.format("Today's Amount: ZMW %,.2f\n", rs.getDouble("total_amount")));
                    sb.append("Pending: ").append(rs.getInt("pending_payments")).append("\n");
                    sb.append("Approved: ").append(rs.getInt("approved_payments")).append("\n");
                    sb.append("Rejected: ").append(rs.getInt("rejected_payments"));
                } else {
                    sb.append("Today's Payments: 0\n");
                    sb.append("Today's Amount: ZMW 0.00\n");
                    sb.append("Pending: 0\n");
                    sb.append("Approved: 0\n");
                    sb.append("Rejected: 0");
                }
            }
        }
        
        return sb.toString();
    }

    private void insertBackupRecord(String backupName, String filePath, Connection conn) throws SQLException {
        String sql = "INSERT INTO system_backups (backup_name, file_path, created_by, backup_date) " +
                    "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setQueryTimeout(5);
            stmt.setString(1, backupName);
            stmt.setString(2, filePath);
            stmt.setInt(3, employeeId);
            stmt.executeUpdate();
            System.out.println("Backup record inserted: " + backupName);
        }
    }
    
    // ASYNC LOG ACTIVITY - PREVENTS FREEZING
    private void asyncLogActivity(String action, String details) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null || conn.isClosed()) {
                        return null;
                    }
                    
                    String sql = "INSERT INTO audit_logs (employee_id, employee_name, action, details) " +
                                "SELECT ?, name, ?, ? FROM employees WHERE employee_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setQueryTimeout(3);
                        stmt.setInt(1, employeeId);
                        stmt.setString(2, action);
                        stmt.setString(3, details);
                        stmt.setInt(4, employeeId);
                        stmt.executeUpdate();
                    }
                } catch (SQLException ex) {
                    System.err.println("Async Audit Log Error: " + ex.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }

    private class DoughnutChart extends JPanel {
        private int[] data = new int[4];
        private float animationProgress = 0f;
        private final Color[] colors = {
            new Color(0, 173, 181),
            new Color(97, 218, 121),
            new Color(255, 107, 107),
            new Color(255, 159, 67)
        };

        public DoughnutChart() {
            setPreferredSize(new Dimension(400, 300));
            setBackground(new Color(57, 62, 70));
        }

        public void setData(int[] newData) {
            this.data = newData;
            repaint();
        }

        public void setAnimationProgress(float progress) {
            this.animationProgress = progress;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int diameter = Math.min(width, height) - 40;
            int x = (width - diameter) / 2;
            int y = (height - diameter) / 2;

            int total = 0;
            for (int value : data) {
                total += value;
            }

            if (total == 0) {
                g2d.setColor(new Color(100, 100, 100));
                g2d.fillOval(x, y, diameter, diameter);
                
                g2d.setColor(new Color(57, 62, 70));
                g2d.fillOval(x + 20, y + 20, diameter - 40, diameter - 40);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 16));
                String noData = "No Data Available";
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(noData);
                g2d.drawString(noData, (width - textWidth) / 2, height / 2);
                return;
            }

            float startAngle = 90;
            for (int i = 0; i < data.length; i++) {
                float extent = (360 * data[i] / total) * animationProgress;
                
                g2d.setColor(colors[i]);
                g2d.fill(new Arc2D.Float(x, y, diameter, diameter, startAngle, extent, Arc2D.PIE));
                
                startAngle += extent;
            }

            g2d.setColor(new Color(57, 62, 70));
            int innerDiameter = diameter / 2;
            int innerX = (width - innerDiameter) / 2;
            int innerY = (height - innerDiameter) / 2;
            g2d.fillOval(innerX, innerY, innerDiameter, innerDiameter);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            String totalText = String.format("%,d", total);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(totalText);
            g2d.drawString(totalText, (width - textWidth) / 2, height / 2 - 10);
            
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            String label = "Total Records";
            textWidth = fm.stringWidth(label);
            g2d.drawString(label, (width - textWidth) / 2, height / 2 + 15);
        }
    }

    private void showDatabaseError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE));
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }
}