import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PaymentsScreen extends JPanel {
    private int userId;
    private String userRole;
    private JTable paymentsTable;
    private DefaultTableModel tableModel;
    private JButton approveButton, rejectButton, initiateButton, refreshButton, backButton;
    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public PaymentsScreen(int userId, String userRole) {
        this.userId = userId;
        this.userRole = userRole;
        initUI();
        loadPaymentsData();
        logActivity("Payments Access", "Accessed payments management");
    }
    
    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(240, 242, 245));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(240, 242, 245));
        
        // Back button
        backButton = new JButton("← Back to Dashboard");
        styleButton(backButton, new Color(100, 100, 100), new Color(80, 80, 80));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backButton.addActionListener(e -> goBackToDashboard());
        headerPanel.add(backButton, BorderLayout.WEST);
       
        JLabel titleLabel = new JLabel("PAYMENT MANAGEMENT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(240, 242, 245));
        
        // Search and Action Panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(new Color(240, 242, 245));
        
        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(new Color(240, 242, 245));
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchPanel.add(searchLabel);
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.setToolTipText("Search by client name, ID number, phone, or loan number");
        searchField.addActionListener(e -> filterPayments());
        searchPanel.add(searchField);
        
        // Status filter
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchPanel.add(statusLabel);
        
        statusFilterCombo = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilterCombo.addActionListener(e -> filterPayments());
        searchPanel.add(statusFilterCombo);
        
        JButton searchButton = new JButton("🔍 Search");
        styleButton(searchButton, new Color(0, 173, 181), new Color(0, 150, 160));
        searchButton.addActionListener(e -> filterPayments());
        searchPanel.add(searchButton);
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        
        // Action Buttons
        JPanel actionTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionTopPanel.setBackground(new Color(240, 242, 245));
        
        refreshButton = new JButton("🔄 Refresh");
        styleButton(refreshButton, new Color(120, 120, 120), new Color(100, 100, 100));
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshButton.addActionListener(e -> loadPaymentsData());
        actionTopPanel.add(refreshButton);
        
        initiateButton = new JButton("💳 New Payment");
        styleButton(initiateButton, new Color(46, 125, 50), new Color(39, 105, 42));
        initiateButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        initiateButton.addActionListener(e -> showNewPaymentDialog());
        actionTopPanel.add(initiateButton);
        
        topPanel.add(actionTopPanel, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Table Panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        String[] columns = {"Receipt ID", "Loan Number", "Client Name", "Amount", "Payment Date", 
                          "Payment Mode", "Status", "Received By", "Approved By", "Rejection Reason"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        paymentsTable = new JTable(tableModel);
        styleTable(paymentsTable);
        
        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(800, 400));
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Admin Action Panel
        if ("admin".equals(userRole)) {
            JPanel adminPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
            adminPanel.setBackground(new Color(240, 242, 245));
            adminPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
            
            approveButton = new JButton("✅ Approve Payment");
            styleButton(approveButton, new Color(46, 125, 50), new Color(39, 105, 42));
            approveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            approveButton.addActionListener(e -> approvePayment());
            approveButton.setEnabled(false);
            adminPanel.add(approveButton);
            
            rejectButton = new JButton("❌ Reject Payment");
            styleButton(rejectButton, new Color(220, 53, 69), new Color(200, 35, 51));
            rejectButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            rejectButton.addActionListener(e -> rejectPayment());
            rejectButton.setEnabled(false);
            adminPanel.add(rejectButton);
            
            mainPanel.add(adminPanel, BorderLayout.SOUTH);
        }
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Add selection listener
        paymentsTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());
    }
    
    private void goBackToDashboard() {
        if ("admin".equals(userRole)) {
            ScreenManager.getInstance().showScreen(new AdminDashboard(userId, getEmployeeName(userId)));
        } else {
            ScreenManager.getInstance().showScreen(new EmployeeDashboard(userId, getEmployeeName(userId)));
        }
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(70, 130, 180));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // Receipt ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Loan Number
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Client Name
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Amount
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Payment Date
        table.getColumnModel().getColumn(5).setPreferredWidth(80);  // Payment Mode
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status
        table.getColumnModel().getColumn(7).setPreferredWidth(120); // Received By
        table.getColumnModel().getColumn(8).setPreferredWidth(120); // Approved By
        table.getColumnModel().getColumn(9).setPreferredWidth(200); // Rejection Reason
        
        // Center align all columns except rejection reason
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Left align rejection reason
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.getColumnModel().getColumn(9).setCellRenderer(leftRenderer);
        
        // Custom renderer for amount column
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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
        
        // Custom renderer for status column
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
                
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                String status = value.toString();
                switch (status) {
                    case "Approved":
                        setForeground(new Color(40, 167, 69));
                        setBackground(new Color(240, 255, 240));
                        break;
                    case "Rejected":
                        setForeground(new Color(220, 53, 69));
                        setBackground(new Color(255, 240, 240));
                        break;
                    case "Pending":
                        setForeground(new Color(255, 193, 7));
                        setBackground(new Color(255, 250, 240));
                        break;
                    default:
                        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
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
    
    private void loadPaymentsData() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT pr.receipt_id, l.loan_number, " +
                        "CONCAT(c.first_name, ' ', c.last_name) as client_name, " +
                        "pr.amount, pr.payment_date, pr.payment_mode, pr.status, " +
                        "e1.name as received_by, COALESCE(e2.name, 'N/A') as approved_by, " +
                        "COALESCE(pr.rejection_reason, '') as rejection_reason " +
                        "FROM payment_receipts pr " +
                        "JOIN loans l ON pr.loan_id = l.loan_id " +
                        "JOIN clients c ON l.client_id = c.client_id " +
                        "JOIN employees e1 ON pr.received_by = e1.employee_id " +
                        "LEFT JOIN employees e2 ON pr.approved_by = e2.employee_id " +
                        "ORDER BY pr.created_at DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("receipt_id"),
                    rs.getString("loan_number"),
                    rs.getString("client_name"),
                    rs.getDouble("amount"),
                    formatDate(rs.getDate("payment_date")),
                    rs.getString("payment_mode"),
                    rs.getString("status"),
                    rs.getString("received_by"),
                    rs.getString("approved_by"),
                    rs.getString("rejection_reason")
                });
            }
        } catch (SQLException ex) {
            showError("Error loading payments: " + ex.getMessage());
        }
    }
    
    private void filterPayments() {
        String searchTerm = searchField.getText().trim();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT pr.receipt_id, l.loan_number, " +
                        "CONCAT(c.first_name, ' ', c.last_name) as client_name, " +
                        "pr.amount, pr.payment_date, pr.payment_mode, pr.status, " +
                        "e1.name as received_by, COALESCE(e2.name, 'N/A') as approved_by, " +
                        "COALESCE(pr.rejection_reason, '') as rejection_reason " +
                        "FROM payment_receipts pr " +
                        "JOIN loans l ON pr.loan_id = l.loan_id " +
                        "JOIN clients c ON l.client_id = c.client_id " +
                        "JOIN employees e1 ON pr.received_by = e1.employee_id " +
                        "LEFT JOIN employees e2 ON pr.approved_by = e2.employee_id " +
                        "WHERE 1=1 ";
            
            if (!searchTerm.isEmpty()) {
                sql += "AND (c.first_name ILIKE ? OR c.last_name ILIKE ? OR " +
                      "c.id_number ILIKE ? OR c.phone_number ILIKE ? OR " +
                      "l.loan_number ILIKE ?) ";
            }
            
            if (!"All".equals(statusFilter)) {
                sql += "AND pr.status = ? ";
            }
            
            sql += "ORDER BY pr.created_at DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            
            if (!searchTerm.isEmpty()) {
                String likeTerm = "%" + searchTerm + "%";
                for (int i = 0; i < 5; i++) {
                    stmt.setString(paramIndex++, likeTerm);
                }
            }
            
            if (!"All".equals(statusFilter)) {
                stmt.setString(paramIndex++, statusFilter);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("receipt_id"),
                    rs.getString("loan_number"),
                    rs.getString("client_name"),
                    rs.getDouble("amount"),
                    formatDate(rs.getDate("payment_date")),
                    rs.getString("payment_mode"),
                    rs.getString("status"),
                    rs.getString("received_by"),
                    rs.getString("approved_by"),
                    rs.getString("rejection_reason")
                });
            }
            
            logActivity("Payment Search", "Searched payments: " + searchTerm + " | Status: " + statusFilter);
        } catch (SQLException ex) {
            showError("Error filtering payments: " + ex.getMessage());
        }
    }
    
    private String formatDate(java.sql.Date date) {
        if (date == null) return "N/A";
        return dateFormat.format(date);
    }
    
    private void updateButtonStates() {
        int selectedRow = paymentsTable.getSelectedRow();
        boolean hasSelection = selectedRow != -1;
        
        if ("admin".equals(userRole) && hasSelection) {
            String status = tableModel.getValueAt(selectedRow, 6).toString();
            boolean isPending = "Pending".equals(status);
            approveButton.setEnabled(isPending);
            rejectButton.setEnabled(isPending);
        }
    }
    
    private void showNewPaymentDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Initialize New Payment", true);
        dialog.setSize(700, 800);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("New Payment - Search Client", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Main content with search and results
        JPanel mainContent = new JPanel(new BorderLayout(15, 15));
        mainContent.setBackground(Color.WHITE);
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
           "Client Search"
        ));
        
        JLabel searchLabel = new JLabel("Search Client:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchPanel.add(searchLabel);
        
        JTextField clientSearchField = new JTextField(25);
        clientSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clientSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        clientSearchField.setToolTipText("Search by client name, ID number, or phone number");
        searchPanel.add(clientSearchField);
        
        JButton searchButton = new JButton("🔍 Search");
        styleSmallButton(searchButton);
        searchPanel.add(searchButton);
        
        mainContent.add(searchPanel, BorderLayout.NORTH);
        
        // Results panel
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Client Search Results"
        ));
        
        String[] resultColumns = {"Client ID", "Name", "ID Number", "Phone", "Active Loans"};
        DefaultTableModel resultsModel = new DefaultTableModel(resultColumns, 0);
        JTable resultsTable = new JTable(resultsModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(30);
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsPanel.add(resultsScroll, BorderLayout.CENTER);
        
        mainContent.add(resultsPanel, BorderLayout.CENTER);
        
        // Payment form panel (initially hidden)
        JPanel paymentFormPanel = new JPanel(new GridBagLayout());
        paymentFormPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Payment Details"
        ));
        paymentFormPanel.setVisible(false);
        paymentFormPanel.setBackground(Color.WHITE);
        
        mainContent.add(paymentFormPanel, BorderLayout.SOUTH);
        
        contentPanel.add(mainContent, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JButton submitButton = new JButton("💳 Submit Payment");
        styleButton(submitButton, new Color(46, 125, 50), new Color(39, 105, 42));
        submitButton.setEnabled(false);
        buttonPanel.add(submitButton);
        
        JButton cancelButton = new JButton("❌ Cancel");
        styleButton(cancelButton, new Color(120, 120, 120), new Color(100, 100, 100));
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);
        
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel, BorderLayout.CENTER);
        
        // Store references to form components
        final JTextField[] amountField = new JTextField[1];
        final JTextField[] dateField = new JTextField[1];
        final JComboBox<String>[] methodCombo = new JComboBox[1];
        final JTextField[] voucherField = new JTextField[1];
        final String[] loanNumber = new String[1];
        final int[] clientId = new int[1];
        final int[] loanId = new int[1];
        
        // Search button action - FIXED: Show message when no results found
        searchButton.addActionListener(e -> {
            String searchTerm = clientSearchField.getText().trim();
            if (searchTerm.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter search term", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            searchClients(searchTerm, resultsModel, dialog);
        });
        
        // Results table selection listener
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && resultsTable.getSelectedRow() != -1) {
                int selectedRow = resultsTable.getSelectedRow();
                String idNumber = (String) resultsModel.getValueAt(selectedRow, 2);
                loadClientLoanDetails(idNumber, paymentFormPanel, submitButton,
                                    amountField, dateField, methodCombo, voucherField,
                                    loanNumber, clientId, loanId, dialog);
            }
        });
        
        // Submit button action
        submitButton.addActionListener(e -> {
            if (validatePayment(amountField[0].getText(), voucherField[0].getText())) {
                submitPayment(loanNumber[0], amountField[0].getText(), 
                            (String) methodCombo[0].getSelectedItem(),
                            dateField[0].getText(), voucherField[0].getText(),
                            clientId[0], loanId[0], dialog);
            }
        });
        
        dialog.setVisible(true);
    }
    
    private void styleSmallButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(0, 173, 181));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(5, 10, 5, 10));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    // FIXED: Show message when no clients found or no active loans
    private void searchClients(String searchTerm, DefaultTableModel resultsModel, JDialog dialog) {
        resultsModel.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT c.client_id, CONCAT(c.first_name, ' ', c.last_name) as client_name, " +
                        "c.id_number, c.phone_number, " +
                        "COUNT(CASE WHEN l.status IN ('Active', 'Approved') THEN 1 END) as active_loans " +
                        "FROM clients c " +
                        "LEFT JOIN loans l ON c.client_id = l.client_id " +
                        "WHERE c.first_name ILIKE ? OR c.last_name ILIKE ? OR " +
                        "c.id_number ILIKE ? OR c.phone_number ILIKE ? " +
                        "GROUP BY c.client_id " +
                        "ORDER BY c.first_name, c.last_name"; // REMOVED HAVING clause to show all clients
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            String likeTerm = "%" + searchTerm + "%";
            for (int i = 1; i <= 4; i++) {
                stmt.setString(i, likeTerm);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                int activeLoans = rs.getInt("active_loans");
                String activeLoansText = activeLoans > 0 ? String.valueOf(activeLoans) : "No active loans";
                
                resultsModel.addRow(new Object[]{
                    rs.getInt("client_id"),
                    rs.getString("client_name"),
                    rs.getString("id_number"),
                    rs.getString("phone_number"),
                    activeLoansText
                });
            }
            
            if (!hasResults) {
                JOptionPane.showMessageDialog(dialog, 
                    "No clients found matching: " + searchTerm, 
                    "No Results", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Error searching clients: " + ex.getMessage());
        }
    }
    
    private void loadClientLoanDetails(String idNumber, JPanel paymentFormPanel, JButton submitButton,
                                     JTextField[] amountField, JTextField[] dateField, 
                                     JComboBox<String>[] methodCombo, JTextField[] voucherField,
                                     String[] loanNumber, int[] clientId, int[] loanId, JDialog dialog) {
        paymentFormPanel.removeAll();
        paymentFormPanel.setVisible(true);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get client details
            String clientSql = "SELECT client_id, first_name, last_name FROM clients WHERE id_number = ?";
            PreparedStatement clientStmt = conn.prepareStatement(clientSql);
            clientStmt.setString(1, idNumber);
            ResultSet clientRs = clientStmt.executeQuery();
            
            if (clientRs.next()) {
                clientId[0] = clientRs.getInt("client_id");
                
                int row = 0;
                
                // Display client information
                gbc.gridx = 0; gbc.gridy = row;
                JLabel nameLabel = new JLabel("Client Name:");
                nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                paymentFormPanel.add(nameLabel, gbc);
                
                gbc.gridx = 1;
                JTextField nameField = new JTextField(clientRs.getString("first_name") + " " + clientRs.getString("last_name"));
                nameField.setEditable(false);
                nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                nameField.setBackground(new Color(245, 245, 245));
                paymentFormPanel.add(nameField, gbc);
                
                gbc.gridx = 0; gbc.gridy = ++row;
                JLabel idLabel = new JLabel("ID Number:");
                idLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                paymentFormPanel.add(idLabel, gbc);
                
                gbc.gridx = 1;
                JTextField idField = new JTextField(idNumber);
                idField.setEditable(false);
                idField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                idField.setBackground(new Color(245, 245, 245));
                paymentFormPanel.add(idField, gbc);
                
                // Get active loans with CORRECT outstanding balance calculation
                String loanSql = "SELECT l.loan_id, l.loan_number, l.amount, l.total_amount, " +
                               "COALESCE(SUM(lp.principal_amount), 0) as total_principal_paid, " +
                               "COALESCE(SUM(lp.interest_amount), 0) as total_interest_paid " +
                               "FROM loans l " +
                               "LEFT JOIN loan_payments lp ON l.loan_id = lp.loan_id AND lp.status = 'Paid' " +
                               "WHERE l.client_id = ? AND l.status IN ('Active', 'Approved') " +
                               "GROUP BY l.loan_id, l.loan_number, l.amount, l.total_amount " +
                               "ORDER BY l.loan_id DESC LIMIT 1";
                PreparedStatement loanStmt = conn.prepareStatement(loanSql);
                loanStmt.setInt(1, clientId[0]);
                ResultSet loanRs = loanStmt.executeQuery();
                
                if (loanRs.next()) {
                    loanId[0] = loanRs.getInt("loan_id");
                    loanNumber[0] = loanRs.getString("loan_number");
                    
                    // FIXED: Calculate outstanding balance correctly (like in ClientDetailScreen)
                    double loanAmount = loanRs.getDouble("amount");
                    double totalAmount = loanRs.getDouble("total_amount");
                    double totalPrincipalPaid = loanRs.getDouble("total_principal_paid");
                    double totalInterestPaid = loanRs.getDouble("total_interest_paid");
                    
                    double totalInterestDue = totalAmount - loanAmount;
                    double remainingPrincipal = Math.max(0, loanAmount - totalPrincipalPaid);
                    double remainingInterest = Math.max(0, totalInterestDue - totalInterestPaid);
                    double calculatedOutstandingBalance = remainingPrincipal + remainingInterest;
                    
                    gbc.gridx = 0; gbc.gridy = ++row;
                    JLabel loanLabel = new JLabel("Loan Number:");
                    loanLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    paymentFormPanel.add(loanLabel, gbc);
                    
                    gbc.gridx = 1;
                    JTextField loanNumberField = new JTextField(loanNumber[0]);
                    loanNumberField.setEditable(false);
                    loanNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    loanNumberField.setBackground(new Color(245, 245, 245));
                    paymentFormPanel.add(loanNumberField, gbc);
                    
                    gbc.gridx = 0; gbc.gridy = ++row;
                    JLabel balanceLabel = new JLabel("Outstanding Balance:");
                    balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    paymentFormPanel.add(balanceLabel, gbc);
                    
                    gbc.gridx = 1;
                    JTextField balanceField = new JTextField(String.format("ZMW %,.2f", calculatedOutstandingBalance));
                    balanceField.setEditable(false);
                    balanceField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    balanceField.setBackground(new Color(245, 245, 245));
                    paymentFormPanel.add(balanceField, gbc);
                    
                    // Show breakdown of remaining balance
                    gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
                    JLabel breakdownLabel = new JLabel(
                        String.format("Breakdown: Principal: ZMW %,.2f | Interest: ZMW %,.2f", 
                            remainingPrincipal, remainingInterest));
                    breakdownLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    breakdownLabel.setForeground(new Color(100, 100, 100));
                    paymentFormPanel.add(breakdownLabel, gbc);
                    gbc.gridwidth = 1;
                    
                    // Payment amount
                    gbc.gridx = 0; gbc.gridy = ++row;
                    JLabel amountLabel = new JLabel("Payment Amount *:");
                    amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    paymentFormPanel.add(amountLabel, gbc);
                    
                    gbc.gridx = 1;
                    amountField[0] = new JTextField();
                    amountField[0].setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    amountField[0].setToolTipText("Enter payment amount. Interest will be paid first, then principal.");
                    paymentFormPanel.add(amountField[0], gbc);
                    
                    // Payment date
                    gbc.gridx = 0; gbc.gridy = ++row;
                    JLabel dateLabel = new JLabel("Payment Date *:");
                    dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    paymentFormPanel.add(dateLabel, gbc);
                    
                    gbc.gridx = 1;
                    dateField[0] = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    dateField[0].setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    paymentFormPanel.add(dateField[0], gbc);
                    
                    // Payment method
                    gbc.gridx = 0; gbc.gridy = ++row;
                    JLabel methodLabel = new JLabel("Payment Method *:");
                    methodLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    paymentFormPanel.add(methodLabel, gbc);
                    
                    gbc.gridx = 1;
                    methodCombo[0] = new JComboBox<>(new String[]{"Cash", "Mobile", "Bank", "Other"});
                    methodCombo[0].setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    paymentFormPanel.add(methodCombo[0], gbc);
                    
                    // Voucher number
                    gbc.gridx = 0; gbc.gridy = ++row;
                    JLabel voucherLabel = new JLabel("Voucher Number *:");
                    voucherLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    paymentFormPanel.add(voucherLabel, gbc);
                    
                    gbc.gridx = 1;
                    voucherField[0] = new JTextField("VOU" + (System.currentTimeMillis() % 100000));
                    voucherField[0].setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    paymentFormPanel.add(voucherField[0], gbc);
                    
                    submitButton.setEnabled(true);
                } else {
                    // FIXED: Show clear message when no active loans
                    gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
                    JLabel noLoanLabel = new JLabel("❌ No active loans found for this client");
                    noLoanLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    noLoanLabel.setForeground(Color.RED);
                    noLoanLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    paymentFormPanel.add(noLoanLabel, gbc);
                    
                    gbc.gridy = ++row;
                    JLabel suggestionLabel = new JLabel("Client must have an Active or Approved loan to make payments");
                    suggestionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    suggestionLabel.setForeground(new Color(150, 150, 150));
                    suggestionLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    paymentFormPanel.add(suggestionLabel, gbc);
                    
                    gbc.gridwidth = 1;
                    submitButton.setEnabled(false);
                }
                dialog.pack();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Client not found with ID: " + idNumber, 
                    "Client Not Found", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            showError("Error loading client details: " + ex.getMessage());
        }
    }
    
    private boolean validatePayment(String amount, String voucher) {
        try {
            double paymentAmount = Double.parseDouble(amount);
            if (paymentAmount <= 0) {
                showError("Payment amount must be greater than zero");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid payment amount");
            return false;
        }
        if (voucher.isEmpty()) {
            showError("Please enter a voucher number");
            return false;
        }
        
        return true;
    }
    
    private void submitPayment(String loanNumber, String amount, String mode, String date, String voucher, 
                             int clientId, int loanId, JDialog dialog) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            double paymentAmount = Double.parseDouble(amount);
            
            // Generate receipt number
            String receiptNumber = "RCP" + System.currentTimeMillis();
            
            // Insert payment receipt
            String sql = "INSERT INTO payment_receipts (loan_id, receipt_number, amount, payment_date, " +
                        "payment_mode, voucher_number, received_by, status) " +
                        "VALUES (?, ?, ?, ?::date, ?, ?, ?, 'Pending')";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, loanId);
            stmt.setString(2, receiptNumber);
            stmt.setDouble(3, paymentAmount);
            stmt.setString(4, date);
            stmt.setString(5, mode);
            stmt.setString(6, voucher);
            stmt.setInt(7, userId);
            
            stmt.executeUpdate();
            
            logActivity("Payment Initiated", 
                "Initiated payment of ZMW " + paymentAmount + " for loan " + loanNumber);
            
            JOptionPane.showMessageDialog(dialog, 
                "✅ Payment submitted successfully!\n\n" +
                "Receipt Number: " + receiptNumber + "\n" +
                "Amount: ZMW " + String.format("%,.2f", paymentAmount) + "\n" +
                "Status: Pending Approval\n\n" +
                "Waiting for admin approval.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            dialog.dispose();
            loadPaymentsData();
            
        } catch (SQLException ex) {
            showError("Error submitting payment: " + ex.getMessage());
        }
    }
    
    private void approvePayment() {
        int selectedRow = paymentsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a payment to approve");
            return;
        }
        
        int modelRow = paymentsTable.convertRowIndexToModel(selectedRow);
        int receiptId = (Integer) tableModel.getValueAt(modelRow, 0);
        String loanNumber = (String) tableModel.getValueAt(modelRow, 1);
        double amount = (Double) tableModel.getValueAt(modelRow, 3);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "💰 APPROVE PAYMENT\n\n" +
            "Loan: " + loanNumber + "\n" +
            "Amount: ZMW " + String.format("%,.2f", amount) + "\n\n" +
            "Are you sure you want to approve this payment?",
            "Confirm Approval", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    // Update payment receipt status
                    String updateReceipt = "UPDATE payment_receipts SET status = 'Approved', " +
                                          "approved_by = ?, approved_date = CURRENT_TIMESTAMP " +
                                          "WHERE receipt_id = ?";
                    PreparedStatement stmt1 = conn.prepareStatement(updateReceipt);
                    stmt1.setInt(1, userId);
                    stmt1.setInt(2, receiptId);
                    stmt1.executeUpdate();
                    
                     // Get loan details from receipt
                    String getLoanDetails = "SELECT pr.loan_id, pr.amount " +
                                          "FROM payment_receipts pr " +
                                          "WHERE pr.receipt_id = ?";
                    PreparedStatement stmt2 = conn.prepareStatement(getLoanDetails);
                    stmt2.setInt(1, receiptId);
                    ResultSet rs = stmt2.executeQuery();
                    
                    if (rs.next()) {
                        int loanId = rs.getInt("loan_id");
                        double paymentAmount = rs.getDouble("amount");
                        
                        // FIXED: Calculate principal and interest breakdown CORRECTLY (same as ClientDetailScreen)
                        // Get loan details with payment history
                        String getLoanWithPayments = "SELECT " +
                            "l.loan_id, l.loan_number, l.amount, l.total_amount, " +
                            "COALESCE(SUM(lp.principal_amount), 0) as total_principal_paid, " +
                            "COALESCE(SUM(lp.interest_amount), 0) as total_interest_paid " +
                            "FROM loans l " +
                            "LEFT JOIN loan_payments lp ON l.loan_id = lp.loan_id AND lp.status = 'Paid' " +
                            "WHERE l.loan_id = ? " +
                            "GROUP BY l.loan_id, l.loan_number, l.amount, l.total_amount";
                        
                        PreparedStatement loanStmt = conn.prepareStatement(getLoanWithPayments);
                        loanStmt.setInt(1, loanId);
                        ResultSet loanRs = loanStmt.executeQuery();
                        
                        if (loanRs.next()) {
                            double loanAmount = loanRs.getDouble("amount");
                            double totalAmount = loanRs.getDouble("total_amount");
                            double totalPrincipalPaid = loanRs.getDouble("total_principal_paid");
                            double totalInterestPaid = loanRs.getDouble("total_interest_paid");
                            
                            double totalInterestDue = totalAmount - loanAmount;
                            double remainingPrincipal = Math.max(0, loanAmount - totalPrincipalPaid);
                            double remainingInterest = Math.max(0, totalInterestDue - totalInterestPaid);
                            
                            // FIXED: Pay interest first, then principal
                            double interestAmount = 0.0;
                            double principalAmount = paymentAmount;
                            
                            if (remainingInterest > 0) {
                                // Pay interest first
                                interestAmount = Math.min(paymentAmount, remainingInterest);
                                principalAmount = paymentAmount - interestAmount;
                            } else {
                                // All interest paid, apply full amount to principal
                                interestAmount = 0;
                                principalAmount = paymentAmount;
                            }
                            
                            // Get next payment number
                            String getNextPaymentNumber = "SELECT COALESCE(MAX(payment_number), 0) + 1 as next_payment " +
                                                        "FROM loan_payments WHERE loan_id = ?";
                            PreparedStatement stmt3 = conn.prepareStatement(getNextPaymentNumber);
                            stmt3.setInt(1, loanId);
                            ResultSet rs2 = stmt3.executeQuery();
                            int nextPaymentNumber = 1;
                            if (rs2.next()) {
                                nextPaymentNumber = rs2.getInt("next_payment");
                            }
                            
                            // Create loan_payments record
                            String insertLoanPayment = "INSERT INTO loan_payments (loan_id, payment_number, " +
                                                    "scheduled_payment_date, payment_amount, principal_amount, " +
                                                    "interest_amount, paid_amount, paid_date, status, payment_mode, " +
                                                    "voucher_number, received_by, approved_by, approved_date) " +
                                                    "VALUES (?, ?, CURRENT_DATE, ?, ?, ?, ?, CURRENT_TIMESTAMP, " +
                                                    "'Paid', ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                            PreparedStatement stmt4 = conn.prepareStatement(insertLoanPayment);
                            stmt4.setInt(1, loanId);
                            stmt4.setInt(2, nextPaymentNumber);
                            stmt4.setDouble(3, paymentAmount);
                            stmt4.setDouble(4, principalAmount);
                            stmt4.setDouble(5, interestAmount);
                            stmt4.setDouble(6, paymentAmount);
                            stmt4.setString(7, "Cash"); // Default mode for approved payments
                            stmt4.setString(8, "APPROVED_" + receiptId);
                            stmt4.setInt(9, userId); // received_by
                            stmt4.setInt(10, userId); // approved_by
                            stmt4.executeUpdate();
                            
                            // Update loan outstanding balance - CORRECT calculation
                            double newRemainingPrincipal = Math.max(0, remainingPrincipal - principalAmount);
                            double newRemainingInterest = Math.max(0, remainingInterest - interestAmount);
                            double newTotalOutstanding = newRemainingPrincipal + newRemainingInterest;
                            
                            String updateLoanBalance = "UPDATE loans SET outstanding_balance = ? WHERE loan_id = ?";
                            PreparedStatement stmt5 = conn.prepareStatement(updateLoanBalance);
                            stmt5.setDouble(1, newTotalOutstanding);
                            stmt5.setInt(2, loanId);
                            stmt5.executeUpdate();
                            
                            // Check if loan is fully paid
                            if (newTotalOutstanding <= 0.01) { // Small tolerance for floating point
                                String closeLoan = "UPDATE loans SET status = 'Closed' WHERE loan_id = ?";
                                PreparedStatement stmt6 = conn.prepareStatement(closeLoan);
                                stmt6.setInt(1, loanId);
                                stmt6.executeUpdate();
                                
                                System.out.println("✅ Loan " + loanNumber + " fully paid and closed. " +
                                                 "Remaining Principal: " + newRemainingPrincipal + 
                                                 ", Remaining Interest: " + newRemainingInterest);
                            } else {
                                System.out.println("⚠️ Loan " + loanNumber + " not fully paid. " +
                                                 "Remaining: Principal=" + newRemainingPrincipal + 
                                                 ", Interest=" + newRemainingInterest);
                            }
                        }
                    }
                    
                    conn.commit();
                    
                    logActivity("Payment Approved", 
                        "Approved payment of ZMW " + amount + " for loan " + loanNumber);
                    
                    loadPaymentsData();
                    JOptionPane.showMessageDialog(this, 
                        "✅ Payment approved successfully!\n\n" +
                        "• Payment recorded in loan payments\n" +
                        "• Loan balance updated\n" +
                        "• Receipt status changed to Approved", 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                showError("Error approving payment: " + ex.getMessage());
            }
        }
    }
    
    private void rejectPayment() {
        int selectedRow = paymentsTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a payment to reject");
            return;
        }
        
        int modelRow = paymentsTable.convertRowIndexToModel(selectedRow);
        int receiptId = (Integer) tableModel.getValueAt(modelRow, 0);
        String loanNumber = (String) tableModel.getValueAt(modelRow, 1);
        double amount = (Double) tableModel.getValueAt(modelRow, 3);
        
        // Create rejection reason dialog
        JTextArea reasonArea = new JTextArea(4, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JScrollPane scrollPane = new JScrollPane(reasonArea);
        
        Object[] message = {
            "❌ REJECT PAYMENT\n\n" +
            "Loan: " + loanNumber + "\n" +
            "Amount: ZMW " + String.format("%,.2f", amount) + "\n\n" +
            "Please provide reason for rejection:",
            scrollPane
        };
        
        int option = JOptionPane.showConfirmDialog(this, message, 
            "Reject Payment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.OK_OPTION) {
            String rejectionReason = reasonArea.getText().trim();
            if (rejectionReason.isEmpty()) {
                showError("Please provide a rejection reason");
                return;
            }
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE payment_receipts SET status = 'Rejected', " +
                            "approved_by = ?, approved_date = CURRENT_TIMESTAMP, " +
                            "rejection_reason = ? " +
                            "WHERE receipt_id = ?";
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);
                stmt.setString(2, rejectionReason);
                stmt.setInt(3, receiptId);
                stmt.executeUpdate();
                
                logActivity("Payment Rejected", 
                    "Rejected payment of ZMW " + amount + " for loan " + loanNumber + 
                    ". Reason: " + rejectionReason);
                
                loadPaymentsData();
                JOptionPane.showMessageDialog(this, 
                    "❌ Payment rejected successfully!\n\n" +
                    "Reason recorded for employee reference.", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException ex) {
                showError("Error rejecting payment: " + ex.getMessage());
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
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}