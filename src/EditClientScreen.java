import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class EditClientScreen extends JPanel {
    private int clientId;
    private int currentUserId;
    private String currentUserRole;
    
    // Form fields
    private JTextField firstNameField, middleNameField, lastNameField, phoneField, emailField;
    private JTextField physicalAddressField, provinceField, postalAddressField, idNumberField;
    private JTextField employerNameField, employeeNumberField, jobTitleField, monthlyIncomeField;
    private JComboBox<String> titleCombo, idTypeCombo, employmentStatusCombo;
    private JComboBox<String> genderCombo, maritalStatusCombo, branchCombo;
    private JTextField dobField;
    
    // Next of Kin fields
    private JTextField kinNameField, kinPhoneField, kinAddressField, kinIdField;
    private JComboBox<String> kinRelationshipCombo;
    
    // Bank details fields
    private JTextField bankNameField, accountNumberField, accountNameField, branchCodeField, branchNameField;
    
    public EditClientScreen(int clientId, int userId, String userRole) {
        this.clientId = clientId;
        this.currentUserId = userId;
        this.currentUserRole = userRole;
        initUI();
        loadClientData();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("EDIT CLIENT - ID: " + clientId);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(new Color(245, 245, 245));
        
        JButton saveButton = new JButton("💾 Save");
        JButton cancelButton = new JButton("❌ Cancel");
        
        styleButton(saveButton, new Color(46, 125, 50), new Color(39, 105, 42));
        styleButton(cancelButton, new Color(198, 40, 40), new Color(170, 34, 34));
        
        saveButton.addActionListener(e -> saveClient());
        cancelButton.addActionListener(e -> goBack());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main form with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Personal Info", createPersonalInfoPanel());
        tabbedPane.addTab("Next of Kin", createNextOfKinPanel());
        tabbedPane.addTab("Bank Details", createBankDetailsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
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
    
    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Personal Information Section
        JPanel personalPanel = new JPanel(new GridBagLayout());
        personalPanel.setBorder(createTitledBorder("Personal Information"));
        personalPanel.setBackground(Color.WHITE);
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        personalPanel.add(new JLabel("Branch:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        branchCombo = new JComboBox<>(new String[]{"Lusaka", "Kitwe", "Ndola", "Livingstone", "Chipata"});
        personalPanel.add(branchCombo, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        personalPanel.add(new JLabel("Title:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        titleCombo = new JComboBox<>(new String[]{"Mr", "Ms", "Mrs", "Dr", "Prof", "Other"});
        personalPanel.add(titleCombo, gbc);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        personalPanel.add(new JLabel("First Name:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        firstNameField = new JTextField(20);
        personalPanel.add(firstNameField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        personalPanel.add(new JLabel("Middle Name:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        middleNameField = new JTextField(20);
        personalPanel.add(middleNameField, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        personalPanel.add(new JLabel("Last Name:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        lastNameField = new JTextField(20);
        personalPanel.add(lastNameField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        personalPanel.add(new JLabel("Date of Birth:*"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        dobField = new JTextField(20);
        personalPanel.add(dobField, gbc);
        
        // Contact Information Section
        JPanel contactPanel = new JPanel(new GridBagLayout());
        contactPanel.setBorder(createTitledBorder("Contact Information"));
        contactPanel.setBackground(Color.WHITE);
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        contactPanel.add(new JLabel("Phone:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        phoneField = new JTextField(20);
        contactPanel.add(phoneField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        contactPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        emailField = new JTextField(20);
        contactPanel.add(emailField, gbc);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        contactPanel.add(new JLabel("Physical Address:*"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.8;
        physicalAddressField = new JTextField(20);
        contactPanel.add(physicalAddressField, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.2;
        contactPanel.add(new JLabel("Province:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        provinceField = new JTextField(20);
        contactPanel.add(provinceField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        contactPanel.add(new JLabel("Postal Address:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        postalAddressField = new JTextField(20);
        contactPanel.add(postalAddressField, gbc);
        
        // ID Information Section
        JPanel idPanel = new JPanel(new GridBagLayout());
        idPanel.setBorder(createTitledBorder("Identification"));
        idPanel.setBackground(Color.WHITE);
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        idPanel.add(new JLabel("ID Type:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        idTypeCombo = new JComboBox<>(new String[]{"NRC", "Passport", "Driver's License", "Other"});
        idPanel.add(idTypeCombo, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        idPanel.add(new JLabel("ID Number:*"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        idNumberField = new JTextField(20);
        idPanel.add(idNumberField, gbc);
        
        // Employment Information Section
        JPanel employmentPanel = new JPanel(new GridBagLayout());
        employmentPanel.setBorder(createTitledBorder("Employment Information"));
        employmentPanel.setBackground(Color.WHITE);
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        employmentPanel.add(new JLabel("Employer:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        employerNameField = new JTextField(20);
        employmentPanel.add(employerNameField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        employmentPanel.add(new JLabel("Employee #:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        employeeNumberField = new JTextField(20);
        employmentPanel.add(employeeNumberField, gbc);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.2;
        employmentPanel.add(new JLabel("Job Title:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        jobTitleField = new JTextField(20);
        employmentPanel.add(jobTitleField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        employmentPanel.add(new JLabel("Monthly Income:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        monthlyIncomeField = new JTextField(20);
        employmentPanel.add(monthlyIncomeField, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        employmentPanel.add(new JLabel("Employment Status:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        employmentStatusCombo = new JComboBox<>(new String[]{"Employed", "Self-Employed", "Unemployed"});
        employmentPanel.add(employmentStatusCombo, gbc);
        
        // Personal Details Section
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(createTitledBorder("Personal Details"));
        detailsPanel.setBackground(Color.WHITE);
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        detailsPanel.add(new JLabel("Gender:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        detailsPanel.add(genderCombo, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        detailsPanel.add(new JLabel("Marital Status:*"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        maritalStatusCombo = new JComboBox<>(new String[]{"Single", "Married", "Divorced", "Widowed"});
        detailsPanel.add(maritalStatusCombo, gbc);
        
        // Add all sections to the main panel
        gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        
        panel.add(personalPanel, gbc);
        gbc.gridy++;
        panel.add(contactPanel, gbc);
        gbc.gridy++;
        panel.add(idPanel, gbc);
        gbc.gridy++;
        panel.add(employmentPanel, gbc);
        gbc.gridy++;
        panel.add(detailsPanel, gbc);
        
        // Add a filler to push everything to the top
        gbc.gridy++;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);
        
        return panel;
    }
    
    private JPanel createNextOfKinPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        JPanel kinPanel = new JPanel(new GridBagLayout());
        kinPanel.setBorder(createTitledBorder("Next of Kin Information"));
        kinPanel.setBackground(Color.WHITE);
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        kinPanel.add(new JLabel("Name:*"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.8;
        kinNameField = new JTextField(20);
        kinPanel.add(kinNameField, gbc);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.2;
        kinPanel.add(new JLabel("Relationship:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        kinRelationshipCombo = new JComboBox<>(new String[]{"Spouse", "Parent", "Sibling", "Other"});
        kinPanel.add(kinRelationshipCombo, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        kinPanel.add(new JLabel("Phone:*"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        kinPhoneField = new JTextField(20);
        kinPanel.add(kinPhoneField, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        kinPanel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        kinAddressField = new JTextField(20);
        kinPanel.add(kinAddressField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        kinPanel.add(new JLabel("ID Number:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        kinIdField = new JTextField(20);
        kinPanel.add(kinIdField, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(kinPanel, gbc);
        
        // Add a filler to push everything to the top
        gbc.gridy++;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);
        
        return panel;
    }
    
    private JPanel createBankDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        JPanel bankPanel = new JPanel(new GridBagLayout());
        bankPanel.setBorder(createTitledBorder("Bank Account Details"));
        bankPanel.setBackground(Color.WHITE);
        
        // Row 0
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.2;
        bankPanel.add(new JLabel("Bank Name:*"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.8;
        bankNameField = new JTextField(20);
        bankPanel.add(bankNameField, gbc);
        
        // Row 1
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.2;
        bankPanel.add(new JLabel("Account Number:*"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        accountNumberField = new JTextField(20);
        bankPanel.add(accountNumberField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        bankPanel.add(new JLabel("Account Name:*"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        accountNameField = new JTextField(20);
        bankPanel.add(accountNameField, gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.2;
        bankPanel.add(new JLabel("Branch Code:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.8;
        branchCodeField = new JTextField(20);
        bankPanel.add(branchCodeField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0.2;
        bankPanel.add(new JLabel("Branch Name:"), gbc);
        
        gbc.gridx = 3; gbc.weightx = 0.8;
        branchNameField = new JTextField(20);
        bankPanel.add(branchNameField, gbc);
        
        gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(bankPanel, gbc);
        
        // Add a filler to push everything to the top
        gbc.gridy++;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);
        
        return panel;
    }
    
    private TitledBorder createTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            title
        );
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        border.setTitleColor(new Color(70, 70, 70));
        return border;
    }
    
    private void loadClientData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    // Load client details
                    loadClientDetails(conn);
                    
                    // Load next of kin details
                    loadNextOfKinDetails(conn);
                    
                    // Load bank details
                    loadBankDetails(conn);
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> 
                        showError("Database Error", "Failed to load client data: " + e.getMessage())
                    );
                } finally {
                    if (conn != null) {
                        try { conn.close(); } catch (SQLException e) {}
                    }
                }
                return null;
            }
        };
        worker.execute();
    }
    
    private void loadClientDetails(Connection conn) throws SQLException {
        String sql = "SELECT c.*, b.branch_name FROM clients c LEFT JOIN branches b ON c.branch_id = b.branch_id WHERE c.client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                setComboBoxValue(branchCombo, rs.getString("branch_name"));
                setComboBoxValue(titleCombo, rs.getString("title"));
                firstNameField.setText(rs.getString("first_name"));
                middleNameField.setText(rs.getString("middle_name"));
                lastNameField.setText(rs.getString("last_name"));
                if (rs.getDate("date_of_birth") != null) {
                    dobField.setText(rs.getDate("date_of_birth").toString());
                }
                phoneField.setText(rs.getString("phone_number"));
                emailField.setText(rs.getString("email"));
                physicalAddressField.setText(rs.getString("physical_address"));
                provinceField.setText(rs.getString("province"));
                postalAddressField.setText(rs.getString("postal_address"));
                setComboBoxValue(idTypeCombo, rs.getString("id_type"));
                idNumberField.setText(rs.getString("id_number"));
                employerNameField.setText(rs.getString("employer_name"));
                employeeNumberField.setText(rs.getString("employee_number"));
                jobTitleField.setText(rs.getString("job_title"));
                if (rs.getObject("monthly_income") != null) {
                   monthlyIncomeField.setText(String.valueOf(rs.getDouble("monthly_income")));
                }
                setComboBoxValue(employmentStatusCombo, rs.getString("employment_status"));
                setComboBoxValue(genderCombo, rs.getString("gender"));
                setComboBoxValue(maritalStatusCombo, rs.getString("marital_status"));
            }
        }
    }
    
    private void loadNextOfKinDetails(Connection conn) throws SQLException {
        String sql = "SELECT * FROM next_of_kin WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                kinNameField.setText(rs.getString("name"));
                setComboBoxValue(kinRelationshipCombo, rs.getString("relationship"));
                kinPhoneField.setText(rs.getString("phone"));
                kinAddressField.setText(rs.getString("address"));
                kinIdField.setText(rs.getString("id_number"));
            }
        }
    }
    
    private void loadBankDetails(Connection conn) throws SQLException {
        String sql = "SELECT * FROM bank_details WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                bankNameField.setText(rs.getString("bank_name"));
                accountNumberField.setText(rs.getString("account_number"));
                accountNameField.setText(rs.getString("account_name"));
                branchCodeField.setText(rs.getString("branch_code"));
                branchNameField.setText(rs.getString("branch_name"));
            }
        }
    }
    
    private void setComboBoxValue(JComboBox<String> combo, String value) {
        if (value != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).equalsIgnoreCase(value)) {
                    combo.setSelectedIndex(i);
                    return;
                }
            }
        }
    }
    
    private void saveClient() {
        if (!validateForm()) return;
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                Connection conn = null;
                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);
                    
                    // Update client details
                    updateClientDetails(conn);
                    
                    // Update next of kin
                    updateNextOfKinDetails(conn);
                    
                    // Update bank details
                    updateBankDetails(conn);
                    
                    // Log the action
                    logAuditAction(conn, "Updated client ID: " + clientId);
                    
                    conn.commit();
                    return true;
                } catch (SQLException e) {
                    if (conn != null) {
                        try { conn.rollback(); } catch (SQLException ex) {}
                    }
                    SwingUtilities.invokeLater(() -> 
                        showError("Save Error", "Failed to save client data: " + e.getMessage())
                    );
                    return false;
                } finally {
                    if (conn != null) {
                        try { conn.close(); } catch (SQLException e) {}
                    }
                }
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(EditClientScreen.this,
                            "Client updated successfully!", "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        goBack();
                    }
                } catch (Exception e) {
                    showError("Error", "Failed to complete save operation");
                }
            }
        };
        worker.execute();
    }
    
    private void updateClientDetails(Connection conn) throws SQLException {
        String sql = "UPDATE clients SET branch_id=?, title=?, first_name=?, middle_name=?, " +
                   "last_name=?, date_of_birth=?, phone_number=?, email=?, physical_address=?, " +
                   "province=?, postal_address=?, id_type=?, id_number=?, " +
                   "employer_name=?, employee_number=?, job_title=?, monthly_income=?, " +
                   "employment_status=?, gender=?, marital_status=?, updated_at=CURRENT_TIMESTAMP " +
                   "WHERE client_id=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            
            // Get branch ID from branch name
            int branchId = getBranchId(conn, (String) branchCombo.getSelectedItem());
            stmt.setInt(paramIndex++, branchId);
            
            stmt.setString(paramIndex++, (String) titleCombo.getSelectedItem());
            stmt.setString(paramIndex++, firstNameField.getText());
            stmt.setString(paramIndex++, middleNameField.getText());
            stmt.setString(paramIndex++, lastNameField.getText());
            stmt.setDate(paramIndex++, java.sql.Date.valueOf(dobField.getText()));
            stmt.setString(paramIndex++, phoneField.getText());
            stmt.setString(paramIndex++, emailField.getText());
            stmt.setString(paramIndex++, physicalAddressField.getText());
            stmt.setString(paramIndex++, provinceField.getText());
            stmt.setString(paramIndex++, postalAddressField.getText());
            stmt.setString(paramIndex++, (String) idTypeCombo.getSelectedItem());
            stmt.setString(paramIndex++, idNumberField.getText());
            stmt.setString(paramIndex++, employerNameField.getText());
            stmt.setString(paramIndex++, employeeNumberField.getText());
            stmt.setString(paramIndex++, jobTitleField.getText());
            
            if (monthlyIncomeField.getText().isEmpty()) {
                stmt.setNull(paramIndex++, Types.DECIMAL);
            } else {
                stmt.setDouble(paramIndex++, Double.parseDouble(monthlyIncomeField.getText()));
            }
            
            stmt.setString(paramIndex++, (String) employmentStatusCombo.getSelectedItem());
            stmt.setString(paramIndex++, (String) genderCombo.getSelectedItem());
            stmt.setString(paramIndex++, (String) maritalStatusCombo.getSelectedItem());
            stmt.setInt(paramIndex, clientId);
            
            stmt.executeUpdate();
        }
    }
    
    private int getBranchId(Connection conn, String branchName) throws SQLException {
        String sql = "SELECT branch_id FROM branches WHERE branch_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, branchName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("branch_id");
            }
        }
        return 1; // Default to first branch
    }
    
    private void updateNextOfKinDetails(Connection conn) throws SQLException {
        // First delete existing records
        String deleteSql = "DELETE FROM next_of_kin WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, clientId);
            stmt.executeUpdate();
        }
        
        // Then insert new record if we have data
        if (!kinNameField.getText().trim().isEmpty()) {
            String insertSql = "INSERT INTO next_of_kin (client_id, name, relationship, phone, address, id_number) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, clientId);
                stmt.setString(2, kinNameField.getText());
                stmt.setString(3, (String) kinRelationshipCombo.getSelectedItem());
                stmt.setString(4, kinPhoneField.getText());
                stmt.setString(5, kinAddressField.getText());
                stmt.setString(6, kinIdField.getText());
                
                stmt.executeUpdate();
            }
        }
    }
    
    private void updateBankDetails(Connection conn) throws SQLException {
        // First delete existing records
        String deleteSql = "DELETE FROM bank_details WHERE client_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, clientId);
            stmt.executeUpdate();
        }
        
        // Then insert new record if we have data
        if (!bankNameField.getText().trim().isEmpty()) {
            String insertSql = "INSERT INTO bank_details (client_id, bank_name, account_number, account_name, branch_code, branch_name) " +
                             "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, clientId);
                stmt.setString(2, bankNameField.getText());
                stmt.setString(3, accountNumberField.getText());
                stmt.setString(4, accountNameField.getText());
                stmt.setString(5, branchCodeField.getText());
                stmt.setString(6, branchNameField.getText());
                
                stmt.executeUpdate();
            }
        }
    }
    
    private void logAuditAction(Connection conn, String action) throws SQLException {
        String sql = "INSERT INTO audit_logs (employee_id, employee_name, action, details) " +
                    "SELECT ?, name, 'Client Update', ? FROM employees WHERE employee_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            stmt.setString(2, action);
            stmt.setInt(3, currentUserId);
            stmt.executeUpdate();
        }
    }
    
    private boolean validateForm() {
        // Check required fields
        if (firstNameField.getText().trim().isEmpty()) {
            showValidationError("First Name is required");
            return false;
        }
        
        if (lastNameField.getText().trim().isEmpty()) {
            showValidationError("Last Name is required");
            return false;
        }
        
        if (phoneField.getText().trim().isEmpty()) {
            showValidationError("Phone Number is required");
            return false;
        }
        
        if (physicalAddressField.getText().trim().isEmpty()) {
            showValidationError("Physical Address is required");
            return false;
        }
        
        if (provinceField.getText().trim().isEmpty()) {
            showValidationError("Province is required");
            return false;
        }
        
        if (idNumberField.getText().trim().isEmpty()) {
            showValidationError("ID Number is required");
            return false;
        }
        
        return true;
    }
    
    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private void goBack() {
        ScreenManager.getInstance().showScreen(new ClientsScreen(currentUserId, currentUserRole));
    }
}               