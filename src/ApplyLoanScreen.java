import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

public class ApplyLoanScreen extends JPanel {
    private int employeeId;
    private String userRole;
    private JTextField searchField;
    private JTextField clientNameField;
    private JTextField phoneField;
    private JTextField amountField;
    private JTextField emailField;
    private JComboBox<String> productComboBox;
    private JTextField interestRateField;
    private JComboBox<String> calculationMethodComboBox;
    private JTextField loanTermField;
    private JComboBox<String> installmentTypeComboBox;
    private JComboBox<String> loanFeeComboBox;
    private JComboBox<String> category1ComboBox;
    private JComboBox<String> category2ComboBox;
    private JTextArea collateralArea;
    private JTextArea guarantorsArea;
    private JTextArea calculationDetailsArea;
    
    private Integer selectedClientId = null;
    private Map<String, Integer> clientSearchResults = new HashMap<>();
    private JDialog searchResultsDialog;
    
    private JLabel loanTermLabel;
    private String currentTermUnit = "Months";
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

    public ApplyLoanScreen(int employeeId, String userRole) {
        this.employeeId = employeeId;
        this.userRole = userRole;
        initializeLoanSequenceTable();
        initUI();
        loadLoanProducts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 245, 245));

        JPanel clientSearchPanel = createClientSearchPanel();
        mainPanel.add(clientSearchPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Loan Details", createLoanDetailsPanel());
        tabbedPane.addTab("Collateral", createCollateralPanel());
        tabbedPane.addTab("Guarantors", createGuarantorsPanel());
        tabbedPane.addTab("Calculation", createCalculationPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonsPanel = createButtonsPanel();
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        headerPanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("APPLY FOR LOAN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton backBtn = createStyledButton("Back to Loans", new Color(57, 62, 70));
        backBtn.addActionListener(e -> goBackToLoans());
        headerPanel.add(backBtn, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createClientSearchPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Client Search"));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(800, 120));

        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton searchBtn = createStyledButton("Search Client", new Color(0, 173, 181));
        searchBtn.addActionListener(e -> searchClients());

        clientNameField = new JTextField();
        clientNameField.setEditable(false);
        clientNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        phoneField = new JTextField();
        phoneField.setEditable(false);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        emailField = new JTextField();
        emailField.setEditable(false);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(new JLabel("Search Client (Name/Phone/ID):"));
        panel.add(searchField);
        panel.add(searchBtn);
        panel.add(new JLabel(""));
        panel.add(new JLabel("Client Name:"));
        panel.add(clientNameField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        return panel;
    }

    private JPanel createLoanDetailsPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        panel.add(new JLabel("Loan Product (Optional):"));
        productComboBox = new JComboBox<>();
        productComboBox.addItem("Select Product");
        productComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        productComboBox.addActionListener(e -> autoFillFromProduct());
        panel.add(productComboBox);

        panel.add(new JLabel("Interest Rate (%):"));
        interestRateField = new JTextField();
        interestRateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        interestRateField.setText("30.0");
        panel.add(interestRateField);

        panel.add(new JLabel("Calculation Method:"));
        calculationMethodComboBox = new JComboBox<>(new String[]{"FLAT", "REDUCING"});
        calculationMethodComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        calculationMethodComboBox.addActionListener(e -> calculateLoan());
        panel.add(calculationMethodComboBox);

        loanTermLabel = new JLabel("Loan Term (Months):");
        panel.add(loanTermLabel);
        loanTermField = new JTextField();
        loanTermField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loanTermField.setText("1");
        panel.add(loanTermField);

        panel.add(new JLabel("Amount (ZMW):"));
        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        amountField.setText("1000");
        amountField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { calculateLoan(); }
            public void removeUpdate(DocumentEvent e) { calculateLoan(); }
            public void insertUpdate(DocumentEvent e) { calculateLoan(); }
        });
        panel.add(amountField);

        panel.add(new JLabel("Installment Type:"));
        installmentTypeComboBox = new JComboBox<>(new String[]{"Weekly", "Monthly", "Quarterly", "Annually"});
        installmentTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        installmentTypeComboBox.addActionListener(e -> updateTermLabel());
        installmentTypeComboBox.addActionListener(e -> calculateLoan());
        panel.add(installmentTypeComboBox);

        panel.add(new JLabel("Loan Fee Type:"));
        loanFeeComboBox = new JComboBox<>(new String[]{"Cash", "Mobile", "Bank"});
        loanFeeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(loanFeeComboBox);

        panel.add(new JLabel("Category 1:"));
        category1ComboBox = new JComboBox<>(new String[]{"Personal", "Business", "Education"});
        category1ComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(category1ComboBox);

        panel.add(new JLabel("Category 2:"));
        category2ComboBox = new JComboBox<>(new String[]{"Short-Term", "Long-Term", "Microloan"});
        category2ComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(category2ComboBox);

        return panel;
    }

    private JPanel createCollateralPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Collateral Details (Optional)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(titleLabel, BorderLayout.NORTH);

        collateralArea = new JTextArea(8, 50);
        collateralArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        collateralArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        collateralArea.setText("Format: Description - Value (ZMW)\nExample:\nLaptop - 4500.00\nHouse - 150000.00\nCar - 199999.00\n......");

        JScrollPane scrollPane = new JScrollPane(collateralArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createGuarantorsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Guarantors (Optional)");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(titleLabel, BorderLayout.NORTH);

        guarantorsArea = new JTextArea(8, 50);
        guarantorsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        guarantorsArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        guarantorsArea.setText("Format: Name - Phone - Relationship - Amount Guaranteed\nExample: Mweemba Obvious - 0971234567 - Family - 6500.99");

        JScrollPane scrollPane = new JScrollPane(guarantorsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCalculationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Loan Calculation Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(titleLabel, BorderLayout.NORTH);

        calculationDetailsArea = new JTextArea(12, 50);
        calculationDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        calculationDetailsArea.setEditable(false);
        calculationDetailsArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        calculationDetailsArea.setText("Fill in loan details to see calculation results...");

        JScrollPane scrollPane = new JScrollPane(calculationDetailsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(new Color(245, 245, 245));

        JButton clearBtn = createStyledButton("Clear Form", new Color(108, 117, 125));
        clearBtn.addActionListener(e -> clearForm());

        JButton calculateBtn = createStyledButton("Calculate", new Color(40, 167, 69));
        calculateBtn.addActionListener(e -> calculateLoan());

        JButton submitBtn = createStyledButton("Submit Application", new Color(0, 123, 255));
        submitBtn.addActionListener(e -> submitApplication());

        panel.add(clearBtn);
        panel.add(calculateBtn);
        panel.add(submitBtn);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }

    private void initializeLoanSequenceTable() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSequenceSQL = "SELECT EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'loan_number_seq')";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(checkSequenceSQL)) {
                if (!rs.next() || !rs.getBoolean(1)) {
                    String createSequenceSQL = "CREATE SEQUENCE loan_number_seq START 1";
                    try (Statement createStmt = conn.createStatement()) {
                        createStmt.executeUpdate(createSequenceSQL);
                    }
                }
            }
        } catch (SQLException ex) {
            showError("Error initializing loan sequence: " + ex.getMessage());
        }
    }

    private void loadLoanProducts() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT product_name FROM loan_products WHERE is_active = true")) {
            
            productComboBox.removeAllItems();
            productComboBox.addItem("Select Product");
            
            while (rs.next()) {
                productComboBox.addItem(rs.getString("product_name"));
            }
        } catch (SQLException ex) {
            showError("Error loading loan products: " + ex.getMessage());
        }
    }

    private void searchClients() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            showError("Please enter a search term");
            return;
        }

        clientSearchResults.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT client_id, first_name, last_name, phone_number, email FROM clients " +
                 "WHERE first_name ILIKE ? OR last_name ILIKE ? OR phone_number ILIKE ? OR id_number ILIKE ?")) {
            
            String likeTerm = "%" + searchTerm + "%";
            for (int i = 1; i <= 4; i++) {
                stmt.setString(i, likeTerm);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.isBeforeFirst()) {
                showError("No clients found matching: " + searchTerm);
                return;
            }
            
            showSearchResultsDialog(rs);
            
        } catch (SQLException ex) {
            showError("Error searching clients: " + ex.getMessage());
        }
    }

    private void showSearchResultsDialog(ResultSet rs) throws SQLException {
        searchResultsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Select Client", true);
        searchResultsDialog.setLayout(new BorderLayout());
        searchResultsDialog.setSize(600, 400);
        searchResultsDialog.setLocationRelativeTo(this);

        String[] columnNames = {"ID", "First Name", "Last Name", "Phone", "Email"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable resultsTable = new JTable(model);

        while (rs.next()) {
            int clientId = rs.getInt("client_id");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String phone = rs.getString("phone_number");
            String email = rs.getString("email");
            
            model.addRow(new Object[]{clientId, firstName, lastName, phone, email});
            clientSearchResults.put(firstName + " " + lastName + " - " + phone, clientId);
        }

        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && resultsTable.getSelectedRow() != -1) {
                int selectedRow = resultsTable.getSelectedRow();
                int clientId = (Integer) resultsTable.getValueAt(selectedRow, 0);
                String firstName = (String) resultsTable.getValueAt(selectedRow, 1);
                String lastName = (String) resultsTable.getValueAt(selectedRow, 2);
                String phone = (String) resultsTable.getValueAt(selectedRow, 3);
                String email = (String) resultsTable.getValueAt(selectedRow, 4);
                
                loadClientDetails(clientId, firstName, lastName, phone, email);
                searchResultsDialog.dispose();
            }
        });

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        searchResultsDialog.add(scrollPane, BorderLayout.CENTER);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> searchResultsDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(cancelBtn);
        searchResultsDialog.add(buttonPanel, BorderLayout.SOUTH);

        searchResultsDialog.setVisible(true);
    }

    private void loadClientDetails(int clientId, String firstName, String lastName, String phone, String email) {
        selectedClientId = clientId;
        clientNameField.setText(firstName + " " + lastName);
        phoneField.setText(phone);
        emailField.setText(email != null ? email : "Not provided");
    }

    private void updateTermLabel() {
        String installmentType = (String) installmentTypeComboBox.getSelectedItem();
        switch (installmentType) {
            case "Weekly":
                loanTermLabel.setText("Loan Term (Weeks):");
                currentTermUnit = "Weeks";
                break;
            case "Monthly":
                loanTermLabel.setText("Loan Term (Months):");
                currentTermUnit = "Months";
                break;
            case "Quarterly":
                loanTermLabel.setText("Loan Term (Quarters):");
                currentTermUnit = "Quarters";
                break;
            case "Annually":
                loanTermLabel.setText("Loan Term (Years):");
                currentTermUnit = "Years";
                break;
        }
    }

    private void autoFillFromProduct() {
        String selectedProduct = (String) productComboBox.getSelectedItem();
        if (selectedProduct == null || selectedProduct.equals("Select Product")) {
            setProductFieldsEnabled(true);
            return;
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT interest_rate, calculation_method, installment_type, " +
                 "loan_fee_type, category1, category2 FROM loan_products WHERE product_name = ?")) {
            
            stmt.setString(1, selectedProduct);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                setProductFieldsEnabled(false);
                
                interestRateField.setText(String.valueOf(rs.getDouble("interest_rate")));
                calculationMethodComboBox.setSelectedItem(rs.getString("calculation_method"));
                installmentTypeComboBox.setSelectedItem(rs.getString("installment_type"));
                loanFeeComboBox.setSelectedItem(rs.getString("loan_fee_type"));
                category1ComboBox.setSelectedItem(rs.getString("category1"));
                category2ComboBox.setSelectedItem(rs.getString("category2"));
                
                calculateLoan();
            }
        } catch (SQLException ex) {
            showError("Error loading product details: " + ex.getMessage());
        }
    }

    private void setProductFieldsEnabled(boolean enabled) {
        interestRateField.setEnabled(enabled);
        calculationMethodComboBox.setEnabled(enabled);
        installmentTypeComboBox.setEnabled(enabled);
        loanFeeComboBox.setEnabled(enabled);
        category1ComboBox.setEnabled(enabled);
        category2ComboBox.setEnabled(enabled);
        
        Color bgColor = enabled ? Color.WHITE : new Color(240, 240, 240);
        interestRateField.setBackground(bgColor);
        
        if (!enabled) {
            interestRateField.setToolTipText("Field disabled - value set from selected loan product");
            calculationMethodComboBox.setToolTipText("Field disabled - value set from selected loan product");
            installmentTypeComboBox.setToolTipText("Field disabled - value set from selected loan product");
            loanFeeComboBox.setToolTipText("Field disabled - value set from selected loan product");
            category1ComboBox.setToolTipText("Field disabled - value set from selected loan product");
            category2ComboBox.setToolTipText("Field disabled - value set from selected loan product");
        } else {
            interestRateField.setToolTipText(null);
            calculationMethodComboBox.setToolTipText(null);
            installmentTypeComboBox.setToolTipText(null);
            loanFeeComboBox.setToolTipText(null);
            category1ComboBox.setToolTipText(null);
            category2ComboBox.setToolTipText(null);
        }
    }

    private void calculateLoan() {
        try {
            if (amountField.getText().trim().isEmpty() || interestRateField.getText().trim().isEmpty() || 
                loanTermField.getText().trim().isEmpty()) {
                calculationDetailsArea.setText("Please fill in amount, interest rate, and loan term to calculate.");
                return;
            }

            double principal = Double.parseDouble(amountField.getText());
            double annualInterestRate = Double.parseDouble(interestRateField.getText());
            int loanTerm = Integer.parseInt(loanTermField.getText());
            String calculationMethod = (String) calculationMethodComboBox.getSelectedItem();
            String installmentType = (String) installmentTypeComboBox.getSelectedItem();

            // Check if this should be a single payment
            boolean isSinglePayment = isSinglePaymentLoan(installmentType, loanTerm);

            double totalInterest = 0;
            double totalAmount = 0;
            double installmentAmount = 0;
            int numberOfInstallments = 1;

            if ("FLAT".equals(calculationMethod)) {
                // FLAT INTEREST: Full annual interest regardless of term
                totalInterest = principal * (annualInterestRate / 100);
                totalAmount = principal + totalInterest;
                
                if (isSinglePayment) {
                    installmentAmount = totalAmount;
                    numberOfInstallments = 1;
                } else {
                    numberOfInstallments = getNumberOfInstallments(installmentType, loanTerm);
                    installmentAmount = totalAmount / numberOfInstallments;
                }
                
            } else {
                // REDUCING BALANCE: Pro-rated interest based on actual time
                if (isSinglePayment) {
                    // SINGLE PAYMENT - simple interest for the period
                    double periodicRate = getPeriodicInterestRate(installmentType, annualInterestRate);
                    totalInterest = principal * periodicRate * loanTerm;
                    totalAmount = principal + totalInterest;
                    installmentAmount = totalAmount;
                    numberOfInstallments = 1;
                } else {
                    // MULTIPLE PAYMENTS - standard EMI formula
                    double periodicRate = getPeriodicInterestRate(installmentType, annualInterestRate);
                    numberOfInstallments = getNumberOfInstallments(installmentType, loanTerm);
                    
                    double power = Math.pow(1 + periodicRate, numberOfInstallments);
                    installmentAmount = principal * periodicRate * power / (power - 1);
                    totalAmount = installmentAmount * numberOfInstallments;
                    totalInterest = totalAmount - principal;
                }
            }

            // Round to 2 decimal places
            installmentAmount = Math.round(installmentAmount * 100.0) / 100.0;
            totalAmount = Math.round(totalAmount * 100.0) / 100.0;
            totalInterest = Math.round(totalInterest * 100.0) / 100.0;

            // Calculate due date
            String dueDate = calculateDueDate(installmentType, loanTerm);

            displayCalculationResults(principal, annualInterestRate, loanTerm, calculationMethod, 
                                    installmentType, installmentAmount, totalInterest, totalAmount, 
                                    numberOfInstallments, isSinglePayment, dueDate);
            
        } catch (NumberFormatException e) {
            calculationDetailsArea.setText("Error: Please enter valid numbers for amount, interest rate, and loan term.");
        } catch (Exception e) {
            calculationDetailsArea.setText("Error in calculation: " + e.getMessage());
        }
    }

    private boolean isSinglePaymentLoan(String installmentType, int loanTerm) {
        // Single payment for: 1,2,3 weeks OR 1 month
        return (installmentType.equals("Weekly") && loanTerm <= 3) || 
               (installmentType.equals("Monthly") && loanTerm == 1);
    }

    private int getNumberOfInstallments(String installmentType, int loanTerm) {
        if (isSinglePaymentLoan(installmentType, loanTerm)) {
            return 1;
        }
        return loanTerm;
    }

    private double getPeriodicInterestRate(String installmentType, double annualRate) {
        switch (installmentType) {
            case "Weekly": return (annualRate / 100) / 52; // Weekly rate
            case "Monthly": return (annualRate / 100) / 12; // Monthly rate
            case "Quarterly": return (annualRate / 100) / 4; // Quarterly rate
            case "Annually": return (annualRate / 100); // Annual rate
            default: return (annualRate / 100) / 12;
        }
    }

    private String calculateDueDate(String installmentType, int loanTerm) {
        try {
            java.util.Date currentDate = new java.util.Date();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(currentDate);
            
            switch (installmentType) {
                case "Weekly":
                    cal.add(java.util.Calendar.WEEK_OF_YEAR, loanTerm);
                    break;
                case "Monthly":
                    cal.add(java.util.Calendar.MONTH, loanTerm);
                    break;
                case "Quarterly":
                    cal.add(java.util.Calendar.MONTH, loanTerm * 3);
                    break;
                case "Annually":
                    cal.add(java.util.Calendar.YEAR, loanTerm);
                    break;
                default:
                    cal.add(java.util.Calendar.MONTH, loanTerm);
            }
            
            return dateFormat.format(cal.getTime());
        } catch (Exception e) {
            return "Error calculating due date";
        }
    }

    private void displayCalculationResults(double principal, double annualRate, int loanTerm, 
                                         String method, String installmentType, double installment, 
                                         double totalInterest, double totalAmount, int numInstallments, 
                                         boolean isSinglePayment, String dueDate) {
        StringBuilder details = new StringBuilder();
        details.append("LOAN CALCULATION DETAILS\n");
        details.append("=======================\n");
        details.append(String.format("Principal Amount:  ZMW %s%n", currencyFormat.format(principal)));
        details.append(String.format("Interest Rate:     %.2f%% %s%n", annualRate, method));
        details.append(String.format("Term:              %d %s%n", loanTerm, currentTermUnit.toLowerCase()));
        details.append(String.format("Installment Type:  %s%n", installmentType));
        details.append(String.format("Due Date:          %s%n", dueDate));
        details.append("-----------------------\n");
        
        if (isSinglePayment) {
            details.append("PAYMENT TYPE: SINGLE PAYMENT\n");
            details.append("-----------------------\n");
        }
        
        if ("FLAT".equals(method)) {
            details.append("INTEREST METHOD: FLAT RATE\n");
            details.append("(Full annual interest charged regardless of term)\n");
            details.append("-----------------------\n");
        } else {
            details.append("INTEREST METHOD: REDUCING BALANCE\n");
            details.append("(Interest pro-rated based on actual time period)\n");
            details.append("-----------------------\n");
        }
        
        details.append(String.format("Installment Amount: ZMW %s%n", currencyFormat.format(installment)));
        details.append(String.format("Total Interest:     ZMW %s%n", currencyFormat.format(totalInterest)));
        details.append(String.format("Total Repayment:    ZMW %s%n", currencyFormat.format(totalAmount)));
        details.append(String.format("Number of Payments: %d%n", numInstallments));
        
        if (!isSinglePayment) {
            details.append(String.format("Payment Frequency: %s%n", installmentType.toLowerCase()));
        }

        calculationDetailsArea.setText(details.toString());
    }

    private void submitApplication() {
        if (selectedClientId == null) {
            showError("Please select a client first");
            return;
        }

        if (!validateForm()) {
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                String loanNumber = generateLoanNumber(conn);
                
                double principal = Double.parseDouble(amountField.getText());
                double annualRate = Double.parseDouble(interestRateField.getText());
                int loanTerm = Integer.parseInt(loanTermField.getText());
                String method = (String) calculationMethodComboBox.getSelectedItem();
                String installmentType = (String) installmentTypeComboBox.getSelectedItem();

                // Check if single payment
                boolean isSinglePayment = isSinglePaymentLoan(installmentType, loanTerm);

                double totalInterest = 0;
                double totalAmount = 0;
                double installmentAmount = 0;
                int numberOfInstallments = 1;
if ("FLAT".equals(method)) {
                    // FLAT: Full annual interest
                    totalInterest = principal * (annualRate / 100);
                    totalAmount = principal + totalInterest;
                    
                    if (isSinglePayment) {
                        installmentAmount = totalAmount;
                        numberOfInstallments = 1;
                    } else {
                        numberOfInstallments = getNumberOfInstallments(installmentType, loanTerm);
                        installmentAmount = totalAmount / numberOfInstallments;
                    }
                    
                } else {
                    // REDUCING: Pro-rated interest
                    if (isSinglePayment) {
                        // Single payment - simple interest
                        double periodicRate = getPeriodicInterestRate(installmentType, annualRate);
                        totalInterest = principal * periodicRate * loanTerm;
                        totalAmount = principal + totalInterest;
                        installmentAmount = totalAmount;
                        numberOfInstallments = 1;
                    } else {
                        // Multiple payments - EMI formula
                        double periodicRate = getPeriodicInterestRate(installmentType, annualRate);
                        numberOfInstallments = getNumberOfInstallments(installmentType, loanTerm);
                        
                        double power = Math.pow(1 + periodicRate, numberOfInstallments);
                        installmentAmount = principal * periodicRate * power / (power - 1);
                        totalAmount = installmentAmount * numberOfInstallments;
                        totalInterest = totalAmount - principal;
                    }
                }
                
                totalAmount = Math.round(totalAmount * 100.0) / 100.0;
                installmentAmount = Math.round(installmentAmount * 100.0) / 100.0;
                totalInterest = Math.round(totalInterest * 100.0) / 100.0;

                java.util.Date currentDate = new java.util.Date();
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(currentDate);
                
                switch (installmentType) {
                    case "Weekly":
                        cal.add(java.util.Calendar.WEEK_OF_YEAR, loanTerm);
                        break;
                    case "Monthly":
                        cal.add(java.util.Calendar.MONTH, loanTerm);
                        break;
                    case "Quarterly":
                        cal.add(java.util.Calendar.MONTH, loanTerm * 3);
                        break;
                    case "Annually":
                        cal.add(java.util.Calendar.YEAR, loanTerm);
                        break;
                }
                java.sql.Date dueDate = new java.sql.Date(cal.getTimeInMillis());

                String insertLoanSQL = "INSERT INTO loans (loan_number, client_id, product_id, amount, interest_rate, " +
                    "calculation_method, loan_term, installment_type, loan_fee_type, category1, category2, " +
                    "total_amount, installment_amount, outstanding_balance, status, created_by, collateral_details, guarantors_details, due_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Pending', ?, ?, ?, ?)";
                
                try (PreparedStatement stmt = conn.prepareStatement(insertLoanSQL, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, loanNumber);
                    stmt.setInt(2, selectedClientId);
                    
                    String selectedProduct = (String) productComboBox.getSelectedItem();
                    if (selectedProduct != null && !selectedProduct.equals("Select Product")) {
                        try (PreparedStatement productStmt = conn.prepareStatement("SELECT product_id FROM loan_products WHERE product_name = ?")) {
                            productStmt.setString(1, selectedProduct);
                            ResultSet rs = productStmt.executeQuery();
                            if (rs.next()) {
                                stmt.setInt(3, rs.getInt("product_id"));
                            } else {
                                stmt.setNull(3, Types.INTEGER);
                            }
                        }
                    } else {
                        stmt.setNull(3, Types.INTEGER);
                    }
                    
                    stmt.setDouble(4, principal);
                    stmt.setDouble(5, annualRate);
                    stmt.setString(6, method);
                    stmt.setInt(7, loanTerm);
                    stmt.setString(8, installmentType);
                    stmt.setString(9, (String) loanFeeComboBox.getSelectedItem());
                    stmt.setString(10, (String) category1ComboBox.getSelectedItem());
                    stmt.setString(11, (String) category2ComboBox.getSelectedItem());
                    stmt.setDouble(12, totalAmount);
                    stmt.setDouble(13, installmentAmount);
                    stmt.setDouble(14, totalAmount);
                    stmt.setInt(15, employeeId);
                    stmt.setString(16, collateralArea.getText());
                    stmt.setString(17, guarantorsArea.getText());
                    stmt.setDate(18, dueDate);
                    
                    int affectedRows = stmt.executeUpdate();
                    
                    if (affectedRows > 0) {
                        logAudit(conn, "Applied for loan: " + loanNumber + " for client ID: " + selectedClientId);
                        conn.commit();
                        showSuccess("Loan application submitted successfully!\nLoan Number: " + loanNumber + "\nDue Date: " + dateFormat.format(dueDate));
                        clearForm();
                    }
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException ex) {
            showError("Error submitting loan application (Check your internet Connection & try again): " + ex.getMessage());
        }
    }

    private String generateLoanNumber(Connection conn) throws SQLException {
        String sql = "SELECT 'LN' || LPAD(nextval('loan_number_seq')::text, 6, '0') as loan_number";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("loan_number");
            }
        }
        
        throw new SQLException("Could not generate loan number");
    }

    private boolean validateForm() {
        if (amountField.getText().trim().isEmpty()) {
            showError("Please enter loan amount");
            return false;
        }
        if (interestRateField.getText().trim().isEmpty()) {
            showError("Please enter interest rate");
            return false;
        }
        if (loanTermField.getText().trim().isEmpty()) {
            showError("Please enter loan term");
            return false;
        }
        
        try {
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                showError("Loan amount must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid loan amount");
            return false;
        }
        
        return true;
    }

    private void clearForm() {
        searchField.setText("");
        clientNameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        amountField.setText("1000");
        interestRateField.setText("30.0");
        loanTermField.setText("1");
        productComboBox.setSelectedIndex(0);
        calculationMethodComboBox.setSelectedIndex(0);
        installmentTypeComboBox.setSelectedIndex(1);
        loanFeeComboBox.setSelectedIndex(0);
        category1ComboBox.setSelectedIndex(0);
        category2ComboBox.setSelectedIndex(0);
        collateralArea.setText("Format: Description - Value (ZMW)\nExample:\nLaptop - 4500.00\nHouse - 150000.00\nCar - 199999.00\n......");
        guarantorsArea.setText("Format: Name - Phone - Relationship - Amount Guaranteed\nExample: Mweemba Obvious - 0971234567 - Family - 6500.99");
        calculationDetailsArea.setText("Fill in loan details to see calculation results...");
        selectedClientId = null;
        setProductFieldsEnabled(true);
    }

    private void goBackToLoans() {
        ScreenManager.getInstance().showScreen(new LoansScreen(employeeId, userRole));
    }

    private void logAudit(Connection conn, String action) throws SQLException {
        String sql = "INSERT INTO audit_logs (employee_id, employee_name, action, details) " +
                    "SELECT ?, name, 'Loan Application', ? FROM employees WHERE employee_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, employeeId);
            stmt.setString(2, action);
            stmt.setInt(3, employeeId);
            stmt.executeUpdate();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}