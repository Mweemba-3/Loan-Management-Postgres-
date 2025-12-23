import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class LoansScreen extends JPanel {
    private int employeeId;
    private String userRole;
    private JTable loansTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterComboBox;
    private JTextField searchField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public LoansScreen(int employeeId, String userRole) {
        this.employeeId = employeeId;
        this.userRole = userRole;
        initUI();
        loadLoansData("All Loans");
        logActivity("Loans Access", "Accessed loans management");
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Search and Filter Panel
        JPanel searchFilterPanel = createSearchFilterPanel();
        add(searchFilterPanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Buttons Panel
        JPanel buttonsPanel = createButtonsPanel();
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("LOANS MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton emiCalculatorBtn = new JButton("🧮 EMI Calculator");
        styleButton(emiCalculatorBtn, new Color(151, 117, 250), new Color(130, 100, 230));
        emiCalculatorBtn.addActionListener(e -> showEMICalculator());
        headerPanel.add(emiCalculatorBtn, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createSearchFilterPanel() {
        JPanel searchFilterPanel = new JPanel(new BorderLayout(10, 0));
        searchFilterPanel.setBackground(new Color(245, 245, 245));
        searchFilterPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(245, 245, 245));
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search by client name, loan number...");
        
        JButton searchBtn = new JButton("🔍 Search");
        styleButton(searchBtn, new Color(0, 173, 181), new Color(0, 150, 160));
        searchBtn.addActionListener(e -> searchLoans());
        
        JButton backBtn = new JButton("⬅️ Back to Dashboard");
        styleButton(backBtn, new Color(108, 117, 125), new Color(90, 100, 110));
        backBtn.addActionListener(e -> navigateToDashboard());
        
        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(new Color(245, 245, 245));
        
        String[] filters = {"All Loans", "Pending", "Approved", "Active", "Rejected", "Closed", "Due Loans", "Overdue"};
        filterComboBox = new JComboBox<>(filters);
        filterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterComboBox.setBackground(Color.WHITE);
        filterComboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        filterComboBox.addActionListener(e -> filterLoans());
        
        filterPanel.add(new JLabel("Filter by Status:"));
        filterPanel.add(filterComboBox);
        
        // Add both panels to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245));
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);
        
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(backBtn);
        
        searchFilterPanel.add(topPanel, BorderLayout.CENTER);
        return searchFilterPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        tablePanel.setBackground(new Color(245, 245, 245));
        
        String[] columns = {"Loan ID", "Loan Number", "Client Name", "Amount", "Status", "Application Date", "Due Date", "Issued By"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 3) return Double.class;
                return String.class;
            }
        };
        
        loansTable = new JTable(tableModel);
        styleLoansTable(loansTable);
        
        JScrollPane scrollPane = new JScrollPane(loansTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private void styleLoansTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        
        // Custom header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
    
        // Set column widths and center all columns
        TableColumnModel columnModel = table.getColumnModel();
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        columnModel.getColumn(0).setPreferredWidth(60);
        columnModel.getColumn(0).setCellRenderer(centerRenderer);
        
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(1).setCellRenderer(centerRenderer);
        
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(2).setCellRenderer(centerRenderer);
        
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Double) {
                    setText(String.format("ZMW %,.2f", (Double) value));
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        });
        
        columnModel.getColumn(4).setPreferredWidth(80);
        columnModel.getColumn(4).setCellRenderer(centerRenderer);
        
        columnModel.getColumn(5).setPreferredWidth(100);
        columnModel.getColumn(5).setCellRenderer(centerRenderer);
        
        columnModel.getColumn(6).setPreferredWidth(100);
        columnModel.getColumn(6).setCellRenderer(centerRenderer);
        
        columnModel.getColumn(7).setPreferredWidth(120);
        columnModel.getColumn(7).setCellRenderer(centerRenderer);
        
        // Add row selection listener
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // Custom renderer for status column to color code statuses
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    String status = value.toString();
                    switch (status) {
                        case "Pending":
                            c.setBackground(new Color(255, 243, 205));
                            c.setForeground(new Color(133, 100, 4));
                            break;
                        case "Approved":
                            c.setBackground(new Color(212, 237, 218));
                            c.setForeground(new Color(21, 87, 36));
                            break;
                        case "Active":
                            c.setBackground(new Color(209, 231, 255));
                            c.setForeground(new Color(12, 67, 125));
                            break;
                        case "Rejected":
                            c.setBackground(new Color(248, 215, 218));
                            c.setForeground(new Color(114, 28, 36));
                            break;
                        case "Closed":
                            c.setBackground(new Color(226, 227, 229));
                            c.setForeground(new Color(73, 80, 87));
                            break;
                        case "Overdue":
                            c.setBackground(new Color(255, 200, 200));
                            c.setForeground(new Color(200, 0, 0));
                            break;
                        default:
                            c.setBackground(Color.WHITE);
                            c.setForeground(Color.BLACK);
                    }
                }
                
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                }
                
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                return c;
            }
        });
    }
    
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonsPanel.setBackground(new Color(245, 245, 245));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton applyLoanBtn = new JButton("📝 Apply Loan");
        styleButton(applyLoanBtn, new Color(97, 218, 121), new Color(80, 200, 100));
        applyLoanBtn.addActionListener(e -> applyLoan());
        
        JButton viewBtn = new JButton("👁️ View Details");
        styleButton(viewBtn, new Color(0, 173, 181), new Color(0, 150, 160));
        viewBtn.addActionListener(e -> viewLoan());
        
        JButton refreshBtn = new JButton("🔄 Refresh");
        styleButton(refreshBtn, new Color(108, 117, 125), new Color(90, 100, 110));
        refreshBtn.addActionListener(e -> filterLoans());
        
        buttonsPanel.add(applyLoanBtn);
        buttonsPanel.add(viewBtn);
        buttonsPanel.add(refreshBtn);
        
        if ("admin".equals(userRole)) {
            JButton createProductBtn = new JButton("📋 Create Product");
            styleButton(createProductBtn, new Color(255, 159, 67), new Color(240, 140, 50));
            createProductBtn.addActionListener(e -> createLoanProduct());
            
            JButton approveBtn = new JButton("✅ Approve");
            styleButton(approveBtn, new Color(97, 218, 121), new Color(80, 200, 100));
            approveBtn.addActionListener(e -> approveLoan());
            
            JButton rejectBtn = new JButton("❌ Reject");
            styleButton(rejectBtn, new Color(255, 107, 107), new Color(230, 80, 80));
            rejectBtn.addActionListener(e -> rejectLoan());
            
            JButton deleteBtn = new JButton("🗑️ Delete");
            styleButton(deleteBtn, new Color(255, 77, 77), new Color(220, 60, 60));
            deleteBtn.addActionListener(e -> deleteLoan());
            
            buttonsPanel.add(createProductBtn);
            buttonsPanel.add(approveBtn);
            buttonsPanel.add(rejectBtn);
            buttonsPanel.add(deleteBtn);
        } else {
            // Employee can only delete pending loans
            JButton deleteBtn = new JButton("🗑️ Delete");
            styleButton(deleteBtn, new Color(255, 77, 77), new Color(220, 60, 60));
            deleteBtn.addActionListener(e -> deleteLoan());
            buttonsPanel.add(deleteBtn);
        }
        
        return buttonsPanel;
    }
    
    private void updateButtonStates() {
        int selectedRow = loansTable.getSelectedRow();
        boolean rowSelected = selectedRow != -1;
        
        if (rowSelected) {
            String status = (String) tableModel.getValueAt(selectedRow, 4);
            
            Component[] components = ((JPanel)getComponent(3)).getComponents();
            for (Component comp : components) {
                if (comp instanceof JButton) {
                    JButton button = (JButton) comp;
                    String text = button.getText();
                    
                    if (text.contains("Approve") || text.contains("Reject")) {
                        button.setEnabled("Pending".equals(status));
                    } else if (text.contains("Delete")) {
                        if ("admin".equals(userRole)) {
                            button.setEnabled(true);
                        } else {
                            button.setEnabled("Pending".equals(status));
                        }
                    } else if (text.contains("View Details")) {
                        button.setEnabled(true);
                    }
                }
            }
        }
    }
    
    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
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
    
    private void loadLoansData(String filter) {
        tableModel.setRowCount(0);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    if (conn == null || conn.isClosed()) {
                        System.err.println("Database connection is closed");
                        return null;
                    }
                    
                    String sql = buildLoanQuery(filter);
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    
                    ResultSet rs = stmt.executeQuery();
                    
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                            rs.getInt("loan_id"),
                            rs.getString("loan_number"),
                            rs.getString("client_name"),
                            rs.getDouble("amount"),
                            rs.getString("status"),
                            formatDate(rs.getDate("application_date")),
                            formatDate(rs.getDate("due_date")),
                            rs.getString("issued_by")
                        });
                    }
                } catch (SQLException ex) {
                    System.err.println("Error loading loans: " + ex.getMessage());
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private String buildLoanQuery(String filter) {
        String baseQuery = "SELECT l.loan_id, l.loan_number, " +
                         "CONCAT(c.first_name, ' ', c.last_name) as client_name, " +
                         "l.amount, l.status, l.application_date, l.due_date, " +
                         "e.name as issued_by " +
                         "FROM loans l " +
                         "JOIN clients c ON l.client_id = c.client_id " +
                         "JOIN employees e ON l.created_by = e.employee_id " +
                         "WHERE 1=1 ";
        
        switch (filter) {
            case "Pending":
                baseQuery += "AND l.status = 'Pending' ";
                break;
            case "Approved":
                baseQuery += "AND l.status = 'Approved' ";
                break;
            case "Active":
                baseQuery += "AND l.status = 'Active' ";
                break;
            case "Rejected":
                baseQuery += "AND l.status = 'Rejected' ";
                break;
            case "Closed":
                baseQuery += "AND l.status = 'Closed' ";
                break;
            case "Due Loans":
                baseQuery += "AND l.due_date <= CURRENT_DATE AND l.status IN ('Active', 'Approved') ";
                break;
            case "Overdue":
                baseQuery += "AND l.due_date < CURRENT_DATE AND l.status IN ('Active', 'Approved') ";
                break;
        }
        
        baseQuery += "ORDER BY l.application_date DESC";
        return baseQuery;
    }
    
    private String formatDate(java.sql.Date date) {
        if (date == null) return "N/A";
        return dateFormat.format(date);
    }
    
    private void searchLoans() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            filterLoans();
            return;
        }
        
        tableModel.setRowCount(0);
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                showError("Database connection error");
                return;
            }
            
            String sql = "SELECT l.loan_id, l.loan_number, " +
                        "CONCAT(c.first_name, ' ', c.last_name) as client_name, " +
                        "l.amount, l.status, l.application_date, l.due_date, " +
                        "e.name as issued_by " +
                        "FROM loans l " +
                        "JOIN clients c ON l.client_id = c.client_id " +
                        "JOIN employees e ON l.created_by = e.employee_id " +
                        "WHERE (c.first_name ILIKE ? OR c.last_name ILIKE ? OR " +
                        "l.loan_number ILIKE ?) " +
                        "ORDER BY l.application_date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(1, likeTerm);
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("loan_id"),
                    rs.getString("loan_number"),
                    rs.getString("client_name"),
                    rs.getDouble("amount"),
                    rs.getString("status"),
                    formatDate(rs.getDate("application_date")),
                    formatDate(rs.getDate("due_date")),
                    rs.getString("issued_by")
                });
            }
            
            logActivity("Loan Search", "Searched for: " + searchTerm);
        } catch (SQLException ex) {
            showError("Error searching loans: " + ex.getMessage());
        }
    }
    
    private void filterLoans() {
        String filter = (String) filterComboBox.getSelectedItem();
        loadLoansData(filter);
    }
    
    private void applyLoan() {
        ScreenManager.getInstance().showScreen(new ApplyLoanScreen(employeeId, userRole));
    }
    
    private void createLoanProduct() {
        ScreenManager.getInstance().showScreen(new CreateLoanProductScreen(employeeId, userRole));
    }
    
    private void showEMICalculator() {
        new EMICalculatorDialog((Frame) SwingUtilities.getWindowAncestor(this)).setVisible(true);
    }
    
    private void viewLoan() {
        int selectedRow = loansTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a loan to view");
            return;
        }
        
        int modelRow = loansTable.convertRowIndexToModel(selectedRow);
        int loanId = (Integer) tableModel.getValueAt(modelRow, 0);
        showLoanDetails(loanId);
    }
    
    private void showLoanDetails(int loanId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                showError("Database connection error");
                return;
            }
            
            String sql = "SELECT l.*, CONCAT(c.first_name, ' ', c.last_name) as client_name, " +
                        "c.phone_number, c.id_number, e.name as employee_name, " +
                        "e2.name as approved_by_name " +
                        "FROM loans l " +
                        "JOIN clients c ON l.client_id = c.client_id " +
                        "JOIN employees e ON l.created_by = e.employee_id " +
                        "LEFT JOIN employees e2 ON l.approved_by = e2.employee_id " +
                        "WHERE l.loan_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, loanId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("LOAN DETAILS\n");
                details.append("============\n\n");
                details.append(String.format("Loan Number:    %s%n", rs.getString("loan_number")));
                details.append(String.format("Client:         %s%n", rs.getString("client_name")));
                details.append(String.format("ID Number:      %s%n", rs.getString("id_number")));
                details.append(String.format("Phone:          %s%n", rs.getString("phone_number")));
                details.append(String.format("Amount:         ZMW %,.2f%n", rs.getDouble("amount")));
                details.append(String.format("Interest Rate:  %.2f%%%n", rs.getDouble("interest_rate")));
                details.append(String.format("Term:           %d %s%n", rs.getInt("loan_term"), rs.getString("installment_type")));
                details.append(String.format("Status:         %s%n", rs.getString("status")));
                details.append(String.format("Created By:     %s%n", rs.getString("employee_name")));
                details.append(String.format("Application:    %s%n", formatDate(rs.getDate("application_date"))));
                
                // Show approved by and date if approved
                if ("Approved".equals(rs.getString("status")) || "Rejected".equals(rs.getString("status"))) {
                    details.append(String.format("Approved By:    %s%n", rs.getString("approved_by_name") != null ? rs.getString("approved_by_name") : "N/A"));
                    details.append(String.format("Approved Date:  %s%n", formatDate(rs.getDate("approved_date"))));
                }
                
                details.append(String.format("Due Date:       %s%n", formatDate(rs.getDate("due_date"))));
                
                // Show rejection reason if rejected
                if ("Rejected".equals(rs.getString("status")) && rs.getString("rejection_reason") != null) {
                    details.append(String.format("%n❌ REJECTION REASON:%n%s%n", rs.getString("rejection_reason")));
                }
                
                if (rs.getString("collateral_details") != null && !rs.getString("collateral_details").trim().isEmpty()) {
                    details.append(String.format("%n📋 COLLATERAL DETAILS:%n%s%n", rs.getString("collateral_details")));
                }
                
                if (rs.getString("guarantors_details") != null && !rs.getString("guarantors_details").trim().isEmpty()) {
                    details.append(String.format("%n👥 GUARANTORS:%n%s%n", rs.getString("guarantors_details")));
                }
                
                // Add loan calculation details
                details.append(String.format("%n💰 LOAN CALCULATION:%n"));
                details.append(String.format("Total Amount:    ZMW %,.2f%n", rs.getDouble("total_amount")));
                details.append(String.format("Installment:     ZMW %,.2f%n", rs.getDouble("installment_amount")));
                //details.append(String.format("Outstanding:     ZMW %,.2f%n", rs.getDouble("outstanding_balance")));
                
                JTextArea textArea = new JTextArea(details.toString(), 25, 60);
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                textArea.setCaretPosition(0); // Scroll to top
                
                JScrollPane scrollPane = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(this, scrollPane, "Loan Details - " + rs.getString("loan_number"), 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Error loading loan details: " + ex.getMessage());
        }
    }
    
    private void approveLoan() {
        int selectedRow = loansTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a loan to approve");
            return;
        }
        
        int modelRow = loansTable.convertRowIndexToModel(selectedRow);
        int loanId = (Integer) tableModel.getValueAt(modelRow, 0);
        String loanNumber = (String) tableModel.getValueAt(modelRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to approve loan: " + loanNumber + "?",
            "Confirm Approval", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                if (conn == null || conn.isClosed()) {
                    showError("Database connection error");
                    return;
                }
                
                String sql = "UPDATE loans SET status = 'Approved', approved_by = ?, approved_date = CURRENT_TIMESTAMP " +
                            "WHERE loan_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, employeeId);
                stmt.setInt(2, loanId);
                stmt.executeUpdate();
                
                logActivity("Loan Approved", "Approved loan: " + loanNumber);
                filterLoans();
                JOptionPane.showMessageDialog(this, "Loan approved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException ex) {
                showError("Error approving loan: " + ex.getMessage());
            }
        }
    }
    
    private void rejectLoan() {
        int selectedRow = loansTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a loan to reject");
            return;
        }
        
        int modelRow = loansTable.convertRowIndexToModel(selectedRow);
        int loanId = (Integer) tableModel.getValueAt(modelRow, 0);
        String loanNumber = (String) tableModel.getValueAt(modelRow, 1);
        
        // Create rejection reason dialog
        JTextArea reasonArea = new JTextArea(4, 40);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JScrollPane scrollPane = new JScrollPane(reasonArea);
        scrollPane.setPreferredSize(new Dimension(400, 120));
        
        Object[] message = {
            "❌ REJECT LOAN APPLICATION\n\n" +
            "Loan Number: " + loanNumber + "\n\n" +
            "Please provide detailed reason for rejection:",
            scrollPane
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, 
            "Reject Loan - " + loanNumber, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String rejectionReason = reasonArea.getText().trim();
            if (rejectionReason.isEmpty()) {
                showError("Please provide a rejection reason");
                return;
            }
            
            if (rejectionReason.length() < 10) {
                showError("Please provide a more detailed rejection reason (minimum 10 characters)");
                return;
            }
            
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                if (conn == null || conn.isClosed()) {
                    showError("Database connection error");
                    return;
                }
                
                String sql = "UPDATE loans SET status = 'Rejected', approved_by = ?, approved_date = CURRENT_TIMESTAMP, rejection_reason = ? WHERE loan_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, employeeId);
                stmt.setString(2, rejectionReason);
                stmt.setInt(3, loanId);
                stmt.executeUpdate();
                
                logActivity("Loan Rejected", "Rejected loan: " + loanNumber + " - Reason: " + rejectionReason);
                filterLoans();
                JOptionPane.showMessageDialog(this, 
                    "✅ Loan rejected successfully!\n\n" +
                    "Rejection reason has been recorded and will be visible to all users.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException ex) {
                showError("Error rejecting loan: " + ex.getMessage());
            }
        }
    }
    
    private void deleteLoan() {
        int selectedRow = loansTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a loan to delete");
            return;
        }
        
        int modelRow = loansTable.convertRowIndexToModel(selectedRow);
        int loanId = (Integer) tableModel.getValueAt(modelRow, 0);
        String status = (String) tableModel.getValueAt(modelRow, 4);
        String loanNumber = (String) tableModel.getValueAt(modelRow, 1);
        
        // Check permissions
        if (!"admin".equals(userRole) && !"Pending".equals(status)) {
            showError("Employees can only delete pending loans");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete loan: " + loanNumber + "?", "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                if (conn == null || conn.isClosed()) {
                    showError("Database connection error");
                    return;
                }
                
                // First delete related records to maintain referential integrity
                String[] deleteQueries = {
                    "DELETE FROM loan_payments WHERE loan_id = ?",
                    "DELETE FROM payment_receipts WHERE loan_id = ?",
                    "DELETE FROM loans WHERE loan_id = ?"
                };
                
                conn.setAutoCommit(false);
                try {
                    for (String query : deleteQueries) {
                        try (PreparedStatement stmt = conn.prepareStatement(query)) {
                            stmt.setInt(1, loanId);
                            stmt.executeUpdate();
                        }
                    }
                    conn.commit();
                    
                    logActivity("Loan Deleted", "Deleted loan: " + loanNumber);
                    filterLoans();
                    JOptionPane.showMessageDialog(this, "Loan deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                showError("Error deleting loan: " + ex.getMessage());
            }
        }
    }
    
    private void navigateToDashboard() {
        if ("admin".equals(userRole)) {
            ScreenManager.getInstance().showScreen(new AdminDashboard(employeeId, getEmployeeName(employeeId)));
        } else {
            ScreenManager.getInstance().showScreen(new EmployeeDashboard(employeeId, getEmployeeName(employeeId)));
        }
    }
    
    private String getEmployeeName(int employeeId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                return "User";
            }
            
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
    
    private void logActivity(String action, String details) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            if (conn == null || conn.isClosed()) {
                return;
            }
            
            String sql = "INSERT INTO audit_logs (employee_id, employee_name, action, details) " +
                        "SELECT ?, name, ?, ? FROM employees WHERE employee_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, employeeId);
            stmt.setString(2, action);
            stmt.setString(3, details);
            stmt.setInt(4, employeeId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("Error logging activity: " + ex.getMessage());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}