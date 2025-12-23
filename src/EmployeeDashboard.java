import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class EmployeeDashboard extends JPanel {
    private int employeeId;
    private String employeeName;
    private DoughnutChart doughnutChart;
    private JLabel welcomeLabel;
    private Timer animationTimer;
    private float animationProgress = 0f;

    public EmployeeDashboard(int employeeId, String employeeName) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        
        // Set up uncaught exception handler
        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            System.err.println("Uncaught exception in EmployeeDashboard: " + throwable.getMessage());
            SwingUtilities.invokeLater(() -> {
                showError("An error occurred: " + throwable.getMessage());
            });
        });
        
        initUI();
        loadDashboardStats();
        startAnimation();
        asyncLogActivity("Dashboard Access", "Accessed employee dashboard");
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(34, 40, 49));
        
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(45, 52, 64));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = createHeaderPanel();
        contentPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(45, 52, 64));
        
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(new Color(45, 52, 64));
        welcomePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        welcomeLabel = new JLabel("Welcome, " + employeeName + "! 👋");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(new Color(200, 200, 200));
        
        JButton refreshBtn = new JButton("🔄 Refresh Dashboard");
        styleButton(refreshBtn, new Color(70, 130, 180), new Color(60, 120, 170));
        refreshBtn.addActionListener(e -> refreshDashboard());
        
        welcomePanel.add(welcomeLabel, BorderLayout.WEST);
        welcomePanel.add(refreshBtn, BorderLayout.EAST);
        
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(new Color(57, 62, 70));
        chartPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(70, 70, 70), 2),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        doughnutChart = new DoughnutChart();
        chartPanel.add(doughnutChart, BorderLayout.CENTER);
        
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
        
        // Company name on left side
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setBackground(new Color(45, 52, 64));
        
        JLabel companyLabel = new JLabel("🏦 MS CODEFORGE");
        companyLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        companyLabel.setForeground(new Color(0, 173, 181));
        logoPanel.add(companyLabel);
        
        // Title in center
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(45, 52, 64));
        JLabel titleLabel = new JLabel("EMPLOYEE DASHBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 173, 181));
        titlePanel.add(titleLabel);
        
        // User info on right
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setBackground(new Color(45, 52, 64));
        
        JLabel userIcon = new JLabel("👤");
        userIcon.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        JLabel roleLabel = new JLabel("ID: " + employeeId + " | Employee");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(Color.WHITE);
        
        userPanel.add(userIcon);
        userPanel.add(roleLabel);
        
        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(userPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(45, 52, 64));
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBorder(new EmptyBorder(20, 15, 20, 15));
        
        // Add sidebar title (clean text version)
        addSidebarTitle(sidebar);
        addMenuItems(sidebar);
        
        // Add vertical glue to push logout button to bottom
        sidebar.add(Box.createVerticalGlue());
        
        addLogoutButton(sidebar);
        
        return sidebar;
    }

    private void addSidebarTitle(JPanel sidebar) {
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setBackground(new Color(45, 52, 64));
        titlePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.setBorder(new EmptyBorder(0, 0, 30, 0));
        
        // Company name
        JLabel companyLabel = new JLabel("🏦 MS CODEFORGE");
        companyLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        companyLabel.setForeground(new Color(0, 173, 181));
        companyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titlePanel.add(companyLabel);
        
        // Dashboard title
        JLabel dashboardLabel = new JLabel("Employee Portal");
        dashboardLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dashboardLabel.setForeground(new Color(150, 150, 150));
        dashboardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dashboardLabel.setBorder(new EmptyBorder(5, 0, 15, 0));
        titlePanel.add(dashboardLabel);
        
        sidebar.add(titlePanel);
    }

    private void addMenuItems(JPanel sidebar) {
        String[] menuItems = {"👥 Clients", "💰 Loans", "💳 Payments", "🔒 Change Password"};
        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebar.add(menuButton);
            sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        }
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
        JButton button = new JButton("🚪 LOGOUT");
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
                    ScreenManager.getInstance().showScreen(new ClientsScreen(employeeId, "employee"));
                    break;
                case "Loans":
                    ScreenManager.getInstance().showScreen(new LoansScreen(employeeId, "employee"));
                    break;
                case "Payments":
                    ScreenManager.getInstance().showScreen(new PaymentsScreen(employeeId, "employee"));
                    break;
                case "Change Password":
                    ScreenManager.getInstance().showScreen(new ChangePasswordScreen(employeeId, "employee", employeeName));
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
                    System.err.println("Error loading dashboard stats: " + ex.getMessage());
                    showDatabaseError("Error loading dashboard statistics: " + ex.getMessage());
                }
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
                if (btn.getText().contains("Refresh Dashboard")) {
                    return btn;
                }
            }
        }
        return null;
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

    private void showDatabaseError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE));
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }
}