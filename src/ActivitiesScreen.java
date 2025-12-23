import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ActivitiesScreen extends JPanel {
    private int userId;
    private String userRole;
    private JTable activitiesTable;
    private DefaultTableModel tableModel;
    private JButton clearButton, refreshButton, backButton;
    private JComboBox<String> filterCombo;
    
    public ActivitiesScreen(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;
        initUI();
        loadActivitiesData();
        logActivity("Activities Access", "Accessed activity logs");
    }
    
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(240, 242, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(240, 242, 245));
        
        JLabel titleLabel = new JLabel("ACTIVITY LOGS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBackground(new Color(240, 242, 245));
        
        JLabel filterLabel = new JLabel("Filter by:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterPanel.add(filterLabel);
        
        filterCombo = new JComboBox<>(new String[]{"All Activities", "Login", "Logout", "Client Management", 
                                                  "Loan Management", "Payment Processing", "User Management", 
                                                  "System Events", "Dashboard Access", "Navigation"});
        filterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterCombo.addActionListener(e -> filterActivities());
        filterPanel.add(filterCombo);
        
        refreshButton = new JButton("🔄 Refresh");
        styleButton(refreshButton, new Color(70, 130, 180), new Color(60, 120, 170));
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.addActionListener(e -> loadActivitiesData());
        filterPanel.add(refreshButton);

        backButton = new JButton("← Back to Dashboard");
        styleButton(backButton, new Color(100, 100, 100), new Color(80, 80, 80));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.addActionListener(e -> goBackToDashboard());
        filterPanel.add(backButton);
        
        if ("admin".equals(userRole)) {
            clearButton = new JButton("🗑️ Clear Logs");
            styleButton(clearButton, new Color(220, 53, 69), new Color(200, 35, 51));
            clearButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            clearButton.addActionListener(e -> clearActivityLogs());
            filterPanel.add(clearButton);
        }
        
        add(filterPanel, BorderLayout.NORTH);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        String[] columns = {"Log ID", "Employee", "Action", "Date", "Details"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        activitiesTable = new JTable(tableModel);
        styleTable(activitiesTable);
        
        JScrollPane scrollPane = new JScrollPane(activitiesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(800, 400));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(240, 240, 240));
        
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(300);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < 4; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
                
                JTextArea textArea = new JTextArea(value != null ? value.toString() : "");
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(true);
                
                if (isSelected) {
                    textArea.setBackground(table.getSelectionBackground());
                    textArea.setForeground(table.getSelectionForeground());
                } else {
                    textArea.setBackground(table.getBackground());
                    textArea.setForeground(table.getForeground());
                }
                
                textArea.setBorder(new EmptyBorder(5, 5, 5, 5));
                return textArea;
            }
        });
    }
    
    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 24, 12, 24));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }
    
    private void goBackToDashboard() {
        if ("admin".equals(userRole)) {
            ScreenManager.getInstance().showScreen(new AdminDashboard(userId, getEmployeeName(userId)));
        } else {
            ScreenManager.getInstance().showScreen(new EmployeeDashboard(userId, getEmployeeName(userId)));
        }
    }
    
    private String getEmployeeName(int employeeId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name FROM employees WHERE employee_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException ex) {
            System.err.println("Error getting employee name: " + ex.getMessage());
        }
        return "User";
    }
    
    private void loadActivitiesData() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT log_id, employee_name, action, action_date, details " +
                        "FROM audit_logs ORDER BY action_date DESC LIMIT 1000";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("log_id"),
                    rs.getString("employee_name"),
                    rs.getString("action"),
                    dateFormat.format(rs.getTimestamp("action_date")),
                    rs.getString("details")
                });
            }
        } catch (SQLException ex) {
            showError("Error loading activities: " + ex.getMessage());
        }
    }
    
    private void filterActivities() {
        String filter = (String) filterCombo.getSelectedItem();
        if ("All Activities".equals(filter)) {
            loadActivitiesData();
            return;
        }
        
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT log_id, employee_name, action, action_date, details " +
                        "FROM audit_logs WHERE action = ? ORDER BY action_date DESC LIMIT 1000";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, filter);
            
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("log_id"),
                    rs.getString("employee_name"),
                    rs.getString("action"),
                    dateFormat.format(rs.getTimestamp("action_date")),
                    rs.getString("details")
                });
            }
        } catch (SQLException ex) {
            showError("Error filtering activities: " + ex.getMessage());
        }
    }
    
    private void clearActivityLogs() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to clear all activity logs? This action cannot be undone.", 
            "Confirm Clear", JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM audit_logs";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            int rowsDeleted = stmt.executeUpdate();
            
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, 
                    "Successfully cleared " + rowsDeleted + " activity logs", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadActivitiesData();
                
                logActivity("Clear Logs", "Cleared all activity logs");
            }
        } catch (SQLException ex) {
            showError("Error clearing activity logs: " + ex.getMessage());
        }
    }
    
    private void logActivity(String action, String details) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO audit_logs (employee_id, employee_name, action, details) " +
                        "SELECT ?, name, ?, ? FROM employees WHERE employee_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error logging activity: " + ex.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}