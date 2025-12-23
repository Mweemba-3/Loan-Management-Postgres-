import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class EmployeesScreen extends JPanel {
    private int userId;
    private String userRole;
    private JTable employeesTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton, backButton;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public EmployeesScreen(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;
        initUI();
        loadEmployeesData();
        logActivity("Employees Access", "Accessed employee management");
    }
    
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(240, 242, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(240, 242, 245));
        
        JLabel titleLabel = new JLabel("EMPLOYEE MANAGEMENT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        
        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionPanel.setBackground(new Color(240, 242, 245));
        
        addButton = new JButton("➕ Add Employee");
        styleButton(addButton, new Color(46, 125, 50), new Color(39, 105, 42));
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.addActionListener(e -> showAddEditDialog(null));
        actionPanel.add(addButton);
        
        editButton = new JButton("✏️ Edit Employee");
        styleButton(editButton, new Color(70, 130, 180), new Color(60, 120, 170));
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editSelectedEmployee());
        actionPanel.add(editButton);
        
        deleteButton = new JButton("🗑️ Delete Employee");
        styleButton(deleteButton, new Color(220, 53, 69), new Color(200, 35, 51));
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteSelectedEmployee());
        actionPanel.add(deleteButton);
        
        refreshButton = new JButton("🔄 Refresh");
        styleButton(refreshButton, new Color(120, 120, 120), new Color(100, 100, 100));
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.addActionListener(e -> loadEmployeesData());
        actionPanel.add(refreshButton);

        backButton = new JButton("← Back to Dashboard");
        styleButton(backButton, new Color(100, 100, 100), new Color(80, 80, 80));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.addActionListener(e -> goBackToDashboard());
        actionPanel.add(backButton);
        
        add(actionPanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        String[] columns = {"ID", "Name", "Role", "Status", "Created Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        employeesTable = new JTable(tableModel);
        styleTable(employeesTable);
        
        // Add selection listener
        employeesTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = employeesTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection && canDeleteSelectedEmployee());
        });
        
        JScrollPane scrollPane = new JScrollPane(employeesTable);
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
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Custom renderer for role column
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
                
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                String role = value.toString();
                if ("admin".equals(role)) {
                    setForeground(new Color(220, 53, 69));
                    setBackground(new Color(255, 240, 240));
                } else {
                    setForeground(new Color(40, 167, 69));
                    setBackground(new Color(240, 255, 240));
                }
                
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                }
                
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
        
        // Custom renderer for status column
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
                
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                boolean isActive = "Active".equals(value);
                if (isActive) {
                    setForeground(new Color(40, 167, 69));
                    setBackground(new Color(240, 255, 240));
                } else {
                    setForeground(new Color(108, 117, 125));
                    setBackground(new Color(248, 249, 250));
                }
                
                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                }
                
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
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
        ScreenManager.getInstance().showScreen(new AdminDashboard(userId, getEmployeeName(userId)));
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
    
    private void loadEmployeesData() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT employee_id, name, role, is_active, created_at " +
                        "FROM employees ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("employee_id"),
                    rs.getString("name"),
                    rs.getString("role"),
                    rs.getBoolean("is_active") ? "Active" : "Inactive",
                    dateFormat.format(rs.getTimestamp("created_at"))
                });
            }
        } catch (SQLException ex) {
            showError("Error loading employees: " + ex.getMessage());
        }
    }
    
    private void showAddEditDialog(Integer employeeId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
                                   employeeId == null ? "Add Employee" : "Edit Employee", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.WHITE);
        
        JPanel contentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Name field
        contentPanel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField();
        contentPanel.add(nameField);
        
        // Role field
        contentPanel.add(new JLabel("Role:"));
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"employee", "admin"});
        contentPanel.add(roleCombo);
        
        // Status field (only for edit)
        JComboBox<String> statusCombo = null;
        if (employeeId != null) {
            contentPanel.add(new JLabel("Status:"));
            statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
            contentPanel.add(statusCombo);
        }
        
        // Password field
        contentPanel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        contentPanel.add(passwordField);
        
        // Confirm password field
        contentPanel.add(new JLabel("Confirm Password:"));
        JPasswordField confirmPasswordField = new JPasswordField();
        contentPanel.add(confirmPasswordField);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("Save");
        styleButton(saveButton, new Color(46, 125, 50), new Color(39, 105, 42));
        
        final JComboBox<String> finalStatusCombo = statusCombo;
        saveButton.addActionListener(e -> {
            if (validateEmployeeForm(nameField, passwordField, confirmPasswordField, employeeId != null)) {
                if (employeeId == null) {
                    addEmployee(nameField.getText(), (String) roleCombo.getSelectedItem(), 
                              new String(passwordField.getPassword()));
                } else {
                    boolean isActive = "Active".equals(finalStatusCombo.getSelectedItem());
                    updateEmployee(employeeId, nameField.getText(), (String) roleCombo.getSelectedItem(), 
                                 new String(passwordField.getPassword()), isActive);
                }
                dialog.dispose();
                loadEmployeesData();
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(120, 120, 120), new Color(100, 100, 100));
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // If editing, load existing data
        if (employeeId != null) {
            loadEmployeeData(employeeId, nameField, roleCombo, statusCombo);
        }
        
        dialog.setVisible(true);
    }
    
    private void loadEmployeeData(int employeeId, JTextField nameField, JComboBox<String> roleCombo, JComboBox<String> statusCombo) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name, role, is_active FROM employees WHERE employee_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                roleCombo.setSelectedItem(rs.getString("role"));
                if (statusCombo != null) {
                    statusCombo.setSelectedItem(rs.getBoolean("is_active") ? "Active" : "Inactive");
                }
            }
        } catch (SQLException ex) {
            showError("Error loading employee data: " + ex.getMessage());
        }
    }
    
    private boolean validateEmployeeForm(JTextField nameField, JPasswordField passwordField, 
                                       JPasswordField confirmPasswordField, boolean isEdit) {
        if (nameField.getText().trim().isEmpty()) {
            showError("Please enter employee name");
            return false;
        }
        
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // For new employees, password is required
        if (!isEdit && password.isEmpty()) {
            showError("Please enter a password");
            return false;
        }
        
        // For edits, password is optional
        if (isEdit && !password.isEmpty()) {
            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match");
                return false;
            }
            
            if (password.length() < 4) {
                showError("Password must be at least 4 characters long");
                return false;
            }
        }
        
        // For new employees, validate password requirements
        if (!isEdit) {
            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match");
                return false;
            }
            
            if (password.length() < 4) {
                showError("Password must be at least 4 characters long");
                return false;
            }
        }
        
        return true;
    }
    
    private void addEmployee(String name, String role, String password) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO employees (name, role, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name.trim());
            stmt.setString(2, role);
            stmt.setString(3, password);
            stmt.executeUpdate();
            
            logActivity("Employee Added", "Added new employee: " + name + " (" + role + ")");
            JOptionPane.showMessageDialog(this, "Employee added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException ex) {
            showError("Error adding employee: " + ex.getMessage());
        }
    }
    
    private void editSelectedEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = employeesTable.convertRowIndexToModel(selectedRow);
            int employeeId = (Integer) tableModel.getValueAt(modelRow, 0);
            showAddEditDialog(employeeId);
        }
    }
    
    private void updateEmployee(int employeeId, String name, String role, String password, boolean isActive) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql;
            PreparedStatement stmt;
            
            if (password.isEmpty()) {
                // Update without password
                sql = "UPDATE employees SET name = ?, role = ?, is_active = ? WHERE employee_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name.trim());
                stmt.setString(2, role);
                stmt.setBoolean(3, isActive);
                stmt.setInt(4, employeeId);
            } else {
                // Update with password
                sql = "UPDATE employees SET name = ?, role = ?, password = ?, is_active = ? WHERE employee_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name.trim());
                stmt.setString(2, role);
                stmt.setString(3, password);
                stmt.setBoolean(4, isActive);
                stmt.setInt(5, employeeId);
            }
            
            stmt.executeUpdate();
            
            logActivity("Employee Updated", "Updated employee: " + name + " (" + role + ")");
            JOptionPane.showMessageDialog(this, "Employee updated successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException ex) {
            showError("Error updating employee: " + ex.getMessage());
        }
    }
    
    private boolean canDeleteSelectedEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) return false;
        
        int modelRow = employeesTable.convertRowIndexToModel(selectedRow);
        int employeeId = (Integer) tableModel.getValueAt(modelRow, 0);
        String employeeRole = (String) tableModel.getValueAt(modelRow, 2);
        
        // Cannot delete yourself
        if (employeeId == userId) {
            return false;
        }
        
        // Cannot delete admins (only allow deleting employees)
        if ("admin".equals(employeeRole)) {
            return false;
        }
        
          // Check if employee has created any records
        try (Connection conn = DatabaseConnection.getConnection()) {
            String[] checkQueries = {
                "SELECT COUNT(*) FROM clients WHERE created_by = ?",
                "SELECT COUNT(*) FROM loans WHERE created_by = ? OR approved_by = ?",
                "SELECT COUNT(*) FROM payment_receipts WHERE received_by = ? OR approved_by = ?"
            };
            
            for (String query : checkQueries) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    if (query.contains("approved_by")) {
                        stmt.setInt(1, employeeId);
                        stmt.setInt(2, employeeId);
                    } else if (query.contains("received_by")) {
                        stmt.setInt(1, employeeId);
                        stmt.setInt(2, employeeId);
                    } else {
                        stmt.setInt(1, employeeId);
                    }
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Employee has created records
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return true;
    }
    
    private void deleteSelectedEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an employee to delete");
            return;
        }
        
        int modelRow = employeesTable.convertRowIndexToModel(selectedRow);
        int employeeId = (Integer) tableModel.getValueAt(modelRow, 0);
        String employeeName = (String) tableModel.getValueAt(modelRow, 1);
        String employeeRole = (String) tableModel.getValueAt(modelRow, 2);
        
        // Cannot delete yourself
        if (employeeId == userId) {
            showError("You cannot delete your own account");
            return;
        }
        
        // Cannot delete admins
        if ("admin".equals(employeeRole)) {
            showError("You cannot delete admin accounts");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete employee: " + employeeName + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM employees WHERE employee_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, employeeId);
                int rowsDeleted = stmt.executeUpdate();
                
                if (rowsDeleted > 0) {
                    logActivity("Employee Deleted", "Deleted employee: " + employeeName);
                    loadEmployeesData();
                    JOptionPane.showMessageDialog(this, "Employee deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException ex) {
                showError("Error deleting employee: " + ex.getMessage());
            }
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