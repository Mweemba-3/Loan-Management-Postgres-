import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class ClientsScreen extends JPanel {
    private JTable clientsTable;
    private ClientTableModel tableModel;
    private JTextField searchField;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    private int currentUserId;
    private String currentUserRole;
    
    public ClientsScreen(int userId, String userRole) {
        this.currentUserId = userId;
        this.currentUserRole = userRole;
        initUI();
        loadClientsData();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("CLIENT MANAGEMENT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Search Panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBackground(new Color(245, 245, 245));
        
        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchField.putClientProperty("JTextField.placeholderText", "Search clients...");
        searchField.addActionListener(e -> searchClients());
        
        JButton searchButton = new JButton("Search");
        styleButton(searchButton, new Color(0, 120, 215), new Color(0, 100, 190));
        searchButton.setPreferredSize(new Dimension(100, 40));
        searchButton.addActionListener(e -> searchClients());
        
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        
        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
        String[] buttonIcons = {"➕", "✏️", "🗑️", "👁️", "🔄", "🏠"};
        String[] buttonTooltips = {
            "Add New Client", "Edit Selected Client", 
            "Delete Client", "View Client Details", "Refresh Data", "Return to Dashboard"
        };
        Color[] buttonColors = {
            new Color(46, 125, 50), // Green
            new Color(251, 140, 0),  // Orange
            new Color(198, 40, 40),  // Red
            new Color(21, 101, 192), // Blue
            new Color(101, 31, 255), // Purple
            new Color(120, 120, 120) // Gray
        };
        
        for (int i = 0; i < buttonIcons.length; i++) {
            JButton button = new JButton(buttonIcons[i]);
            button.setToolTipText(buttonTooltips[i]);
            styleButton(button, buttonColors[i], buttonColors[i].darker());
            button.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            button.setPreferredSize(new Dimension(45, 40));
            buttonPanel.add(button);
            
            switch (i) {
                case 0: button.addActionListener(e -> showAddClientScreen()); break;
                case 1: button.addActionListener(e -> showEditClientScreen()); break;
                case 2: button.addActionListener(e -> deleteSelectedClient()); break;
                case 3: button.addActionListener(e -> showClientDetails()); break;
                case 4: button.addActionListener(e -> loadClientsData()); break;
                case 5: button.addActionListener(e -> goBackHome()); break;
            }
        }
        
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
        
        // Table Panel
        tableModel = new ClientTableModel();
        clientsTable = new JTable(tableModel);
        customizeTable(clientsTable);
        
        // Add double-click listener for quick details
        clientsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showClientDetails();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(clientsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
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
    
    private void customizeTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // Custom header renderer
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Custom cell renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
                
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if (isSelected) {
                    setBackground(new Color(220, 235, 247));
                    setForeground(Color.BLACK);
                } else {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                    setForeground(Color.BLACK);
                }
                
                return this;
            }
        });
    }
    
    private void loadClientsData() {
        SwingWorker<List<Client>, Void> worker = new SwingWorker<List<Client>, Void>() {
            @Override
            protected List<Client> doInBackground() throws Exception {
                List<Client> clients = new ArrayList<>();
                
                String sql = "SELECT client_id, first_name, last_name, phone_number, id_number, date_of_birth, created_at FROM clients ORDER BY created_at DESC";
                
                try (Connection conn = DatabaseConnection.getConnection();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        Client client = new Client(
                            rs.getInt("client_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("id_number"),
                            rs.getString("phone_number"),
                            rs.getDate("date_of_birth"),
                            rs.getTimestamp("created_at")
                        );
                        clients.add(client);
                    }
                }
                return clients;
            }
            
            @Override
            protected void done() {
                try {
                    List<Client> clients = get();
                    tableModel.setClients(clients);
                } catch (Exception e) {
                    showError("Error loading clients: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void searchClients() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadClientsData();
            return;
        }
        
        SwingWorker<List<Client>, Void> worker = new SwingWorker<List<Client>, Void>() {
            @Override
            protected List<Client> doInBackground() throws Exception {
                List<Client> clients = new ArrayList<>();
                
                String sql = "SELECT client_id, first_name, last_name, phone_number, id_number, date_of_birth, created_at FROM clients WHERE first_name ILIKE ? OR last_name ILIKE ? OR phone_number ILIKE ? OR id_number ILIKE ? ORDER BY created_at DESC";
                
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    String likeTerm = "%" + searchTerm + "%";
                    stmt.setString(1, likeTerm);
                    stmt.setString(2, likeTerm);
                    stmt.setString(3, likeTerm);
                    stmt.setString(4, likeTerm);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Client client = new Client(
                                rs.getInt("client_id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("id_number"),
                                rs.getString("phone_number"),
                                rs.getDate("date_of_birth"),
                                rs.getTimestamp("created_at")
                            );
                            clients.add(client);
                        }
                    }
                }
                return clients;
            }
            
            @Override
            protected void done() {
                try {
                    List<Client> clients = get();
                    tableModel.setClients(clients);
                    showSuccess("Found " + clients.size() + " clients");
                } catch (Exception e) {
                    showError("Error searching clients: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private void deleteSelectedClient() {
        int selectedRow = clientsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a client to delete");
            return;
        }
        
        int modelRow = clientsTable.convertRowIndexToModel(selectedRow);
        Client client = tableModel.getClientAt(modelRow);
        String clientName = client.getFirstName() + " " + client.getLastName();
        
        // Check if client has loans
        boolean hasLoans = clientHasLoans(client.getClientId());
        
        // ROLE-BASED ACCESS CONTROL
        if (!currentUserRole.equalsIgnoreCase("admin") && hasLoans) {
            showError("Access Denied: Only administrators can delete clients with active loans.\n\n" +
                     "Client '" + clientName + "' has active loan records.\n" +
                     "Please contact your administrator for client deletion.");
            return;
        }
        
        // Different confirmation messages based on role and loan status
        if (currentUserRole.equalsIgnoreCase("admin")) {
            // Admin confirmation - can delete any client
            if (hasLoans) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "ADMIN DELETE - CLIENT WITH LOANS\n\n" +
                    "Client: " + clientName + "\n" +
                    "Status: Has active loans\n\n" +
                    "This will permanently delete:\n" +
                    "• All client loans and payment records\n" +
                    "• Next of kin information\n" +
                    "• Bank details\n" +
                    "• All related data\n\n" +
                    "This action cannot be undone!",
                    "Admin - Confirm Delete Client with Loans",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteClientWithAllRelatedData(client.getClientId(), clientName);
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Delete client: " + clientName + "?",
                    "Admin - Confirm Delete", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
                    
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteClientWithAllRelatedData(client.getClientId(), clientName);
                }
            }
        } else {
            // Employee confirmation - can only delete clients without loans
            int confirm = JOptionPane.showConfirmDialog(this,
                "EMPLOYEE DELETE CONFIRMATION\n\n" +
                "You are about to delete client: " + clientName + "\n\n" +
                "Note: Employees can only delete clients without active loans.\n" +
                "This action will remove all client data permanently.\n\n" +
                "Are you sure you want to proceed?",
                "Employee - Confirm Delete", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                deleteClientWithAllRelatedData(client.getClientId(), clientName);
            }
        }
    }
    
    private boolean clientHasLoans(int clientId) {
        String sql = "SELECT COUNT(*) FROM loans WHERE client_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            showError("Error checking client loans: " + e.getMessage());
            return false;
        }
    }
    
    private void deleteClientWithAllRelatedData(int clientId, String clientName) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);
                    
                    // Get all loan IDs for this client
                    List<Integer> loanIds = getClientLoanIds(conn, clientId);
                    
                    // Delete in correct order to respect foreign key constraints
                    if (!loanIds.isEmpty()) {
                        deleteLoanPayments(conn, loanIds);
                        deletePaymentReceipts(conn, loanIds);
                        deleteLoans(conn, clientId);
                    }
                    
                    deleteNextOfKin(conn, clientId);
                    deleteBankDetails(conn, clientId);
                    
                    // Finally delete the client
                    boolean clientDeleted = deleteClient(conn, clientId);
                    
                    if (clientDeleted) {
                        conn.commit();
                        return true;
                    } else {
                        conn.rollback();
                        return false;
                    }
                    
                } catch (SQLException e) {
                    if (conn != null) {
                        conn.rollback();
                    }
                    throw e;
                } finally {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                        conn.close();
                    }
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showSuccess("Client '" + clientName + "' and all related data deleted successfully");
                        loadClientsData();
                    } else {
                        showError("Failed to delete client");
                    }
                } catch (Exception e) {
                    showError("Error deleting client: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    private List<Integer> getClientLoanIds(Connection conn, int clientId) throws SQLException {
        List<Integer> loanIds = new ArrayList<>();
        String sql = "SELECT loan_id FROM loans WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                loanIds.add(rs.getInt("loan_id"));
            }
        }
        return loanIds;
    }
    
    private void deleteLoanPayments(Connection conn, List<Integer> loanIds) throws SQLException {
        if (loanIds.isEmpty()) return;
        
        String placeholders = String.join(",", Collections.nCopies(loanIds.size(), "?"));
        String sql = "DELETE FROM loan_payments WHERE loan_id IN (" + placeholders + ")";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < loanIds.size(); i++) {
                stmt.setInt(i + 1, loanIds.get(i));
            }
            stmt.executeUpdate();
        }
    }
    
    private void deletePaymentReceipts(Connection conn, List<Integer> loanIds) throws SQLException {
        if (loanIds.isEmpty()) return;
        
        String placeholders = String.join(",", Collections.nCopies(loanIds.size(), "?"));
        String sql = "DELETE FROM payment_receipts WHERE loan_id IN (" + placeholders + ")";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < loanIds.size(); i++) {
                stmt.setInt(i + 1, loanIds.get(i));
            }
            stmt.executeUpdate();
        }
    }
    
    private void deleteLoans(Connection conn, int clientId) throws SQLException {
        String sql = "DELETE FROM loans WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.executeUpdate();
        }
    }
    
    private void deleteNextOfKin(Connection conn, int clientId) throws SQLException {
        String sql = "DELETE FROM next_of_kin WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.executeUpdate();
        }
    }
    
    private void deleteBankDetails(Connection conn, int clientId) throws SQLException {
        String sql = "DELETE FROM bank_details WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.executeUpdate();
        }
    }
    
    private boolean deleteClient(Connection conn, int clientId) throws SQLException {
        String sql = "DELETE FROM clients WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    private void showAddClientScreen() {
        ScreenManager.getInstance().showScreen(new AddClientScreen(currentUserId, currentUserRole));
    }
    
    private void showEditClientScreen() {
        int selectedRow = clientsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a client to edit");
            return;
        }
        int modelRow = clientsTable.convertRowIndexToModel(selectedRow);
        Client client = tableModel.getClientAt(modelRow);

        ScreenManager.getInstance().showScreen(new EditClientScreen(client.getClientId(), currentUserId, currentUserRole));
    }
    
    private void showClientDetails() {
        int selectedRow = clientsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select a client to view");
            return;
        }
        
        int modelRow = clientsTable.convertRowIndexToModel(selectedRow);
        Client client = tableModel.getClientAt(modelRow);
        
        ScreenManager.getInstance().showScreen(new ClientDetailsScreen(client, currentUserId, currentUserRole));
    }
    
    private void goBackHome() {
        if (currentUserRole.equalsIgnoreCase("admin")) {
            ScreenManager.getInstance().showScreen(new AdminDashboard(currentUserId, getEmployeeName(currentUserId)));
        } else {
            ScreenManager.getInstance().showScreen(new EmployeeDashboard(currentUserId, getEmployeeName(currentUserId)));
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
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Client Table Model
    private class ClientTableModel extends AbstractTableModel {
        private List<Client> clients = new ArrayList<>();
        private String[] columnNames = {"ID", "First Name", "Last Name", "ID Number", "Phone", "Date of Birth", "Member Since"};
        
        public void setClients(List<Client> clients) {
            this.clients = clients;
            fireTableDataChanged();
        }
        
        public Client getClientAt(int row) {
            return clients.get(row);
        }
        
        @Override public int getRowCount() { return clients.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int col) { return columnNames[col]; }
        
        @Override
        public Object getValueAt(int row, int col) {
            Client client = clients.get(row);
            switch (col) {
                case 0: return client.getClientId();
                case 1: return client.getFirstName();
                case 2: return client.getLastName();
                case 3: return client.getIdNumber();
                case 4: return client.getPhoneNumber();
                case 5: return client.getDateOfBirth() != null ? dateFormat.format(client.getDateOfBirth()) : "N/A";
                case 6: return client.getCreatedAt() != null ? dateFormat.format(client.getCreatedAt()) : "N/A";
                default: return null;
            }
        }
        
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
    }
    
    // Client class
    public static class Client {
        private int clientId;
        private String firstName;
        private String lastName;
        private String idNumber;
        private String phoneNumber;
        private Date dateOfBirth;
        private Timestamp createdAt;
        
        public Client(int clientId, String firstName, String lastName, String idNumber, 
                     String phoneNumber, Date dateOfBirth, Timestamp createdAt) {
            this.clientId = clientId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.idNumber = idNumber;
            this.phoneNumber = phoneNumber;
            this.dateOfBirth = dateOfBirth;
            this.createdAt = createdAt;
        }
        
        // Getters
        public int getClientId() { return clientId; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getIdNumber() { return idNumber; }
        public String getPhoneNumber() { return phoneNumber; }
        public Date getDateOfBirth() { return dateOfBirth; }
        public Timestamp getCreatedAt() { return createdAt; }
    }
}