import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import java.io.ByteArrayInputStream;

public class ClientDetailsScreen extends JPanel {
    private ClientsScreen.Client client;
    private int currentUserId;
    private String currentUserRole;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    private SimpleDateFormat datetimeFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
    private java.awt.Image companyLogo = null;
    
    public ClientDetailsScreen(ClientsScreen.Client client, int userId, String userRole) {
        this.client = client;
        this.currentUserId = userId;
        this.currentUserRole = userRole;
        loadCompanyLogoFromDatabase(); // Load logo from database
        initUI();
        loadClientDetails();
    }
    
    private void loadCompanyLogoFromDatabase() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    if (conn == null) {
                        System.err.println("Database connection is null");
                        return null;
                    }

                    // Load report logo from database - LOAD ORIGINAL SHAPE (no circular clipping)
                    String sql = "SELECT setting_value FROM system_settings WHERE setting_key = 'LOGO_REPORT'";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setQueryTimeout(5);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                String base64Image = rs.getString("setting_value");
                                if (base64Image != null && !base64Image.trim().isEmpty()) {
                                    // Decode and load original image
                                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                                    companyLogo = javax.imageio.ImageIO.read(new ByteArrayInputStream(imageBytes));
                                    System.out.println("Loaded report logo from database with dimensions: " + 
                                        ((java.awt.image.BufferedImage)companyLogo).getWidth() + "x" + 
                                        ((java.awt.image.BufferedImage)companyLogo).getHeight());
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
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("CLIENT DETAILS");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton backButton = new JButton("← Back to Clients");
        styleButton(backButton, new Color(120, 120, 120), new Color(100, 100, 100));
        backButton.addActionListener(e -> goBack());
        headerPanel.add(backButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main Content
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("Personal Info", createPersonalInfoPanel());
        tabbedPane.addTab("Next of Kin", createNextOfKinPanel());
        tabbedPane.addTab("Bank Details", createBankDetailsPanel());
        tabbedPane.addTab("Loan History", createLoanHistoryPanel());
        tabbedPane.addTab("Payment History", createPaymentHistoryPanel());
        tabbedPane.addTab("Loan Calculations", createLoanCalculationPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createPersonalInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.*, b.branch_name FROM clients c LEFT JOIN branches b ON c.branch_id = b.branch_id WHERE c.client_id = ?")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int row = 0;
                
                // Personal Information
                addDetailRow(panel, gbc, "Client ID:", String.valueOf(client.getClientId()), row++);
                addDetailRow(panel, gbc, "Full Name:", 
                    rs.getString("title") + " " + rs.getString("first_name") + " " + 
                    (rs.getString("middle_name") != null ? rs.getString("middle_name") + " " : "") + 
                    rs.getString("last_name"), row++);
                addDetailRow(panel, gbc, "Date of Birth:", 
                    dateFormat.format(rs.getDate("date_of_birth")), row++);
                addDetailRow(panel, gbc, "Gender:", rs.getString("gender"), row++);
                addDetailRow(panel, gbc, "Marital Status:", rs.getString("marital_status"), row++);
                
                // Contact Information
                addDetailRow(panel, gbc, "Phone:", rs.getString("phone_number"), row++);
                addDetailRow(panel, gbc, "Email:", 
                    rs.getString("email") != null ? rs.getString("email") : "Not provided", row++);
                addDetailRow(panel, gbc, "Physical Address:", rs.getString("physical_address"), row++);
                addDetailRow(panel, gbc, "Province:", rs.getString("province"), row++);
                addDetailRow(panel, gbc, "Postal Address:", 
                    rs.getString("postal_address") != null ? rs.getString("postal_address") : "Not provided", row++);
                
                // Identification
                addDetailRow(panel, gbc, "ID Type:", rs.getString("id_type"), row++);
                addDetailRow(panel, gbc, "ID Number:", rs.getString("id_number"), row++);
                
                // Employment Information
                addDetailRow(panel, gbc, "Employment Status:", rs.getString("employment_status"), row++);
                addDetailRow(panel, gbc, "Employer:", 
                    rs.getString("employer_name") != null ? rs.getString("employer_name") : "Not provided", row++);
                addDetailRow(panel, gbc, "Job Title:", 
                    rs.getString("job_title") != null ? rs.getString("job_title") : "Not provided", row++);
                addDetailRow(panel, gbc, "Monthly Income:", 
                    String.format("ZMW %,.2f", rs.getDouble("monthly_income")), row++);
                
                // Branch
                addDetailRow(panel, gbc, "Branch:", 
                    rs.getString("branch_name") != null ? rs.getString("branch_name") : "Not assigned", row++);
                
                // Dates
                addDetailRow(panel, gbc, "Member Since:", 
                    dateFormat.format(rs.getTimestamp("created_at")), row++);
                addDetailRow(panel, gbc, "Last Updated:", 
                    dateFormat.format(rs.getTimestamp("updated_at")), row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading client details: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return panel;
    }
    
    private JPanel createNextOfKinPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columnNames = {"Name", "Relationship", "Phone", "ID Number", "Address"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        customizeTable(table);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM next_of_kin WHERE client_id = ?")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("relationship"),
                    rs.getString("phone"),
                    rs.getString("id_number") != null ? rs.getString("id_number") : "Not provided",
                    rs.getString("address") != null ? rs.getString("address") : "Not provided"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBankDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columnNames = {"Bank Name", "Account Number", "Account Name", "Branch", "Branch Code"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        customizeTable(table);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM bank_details WHERE client_id = ?")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("bank_name"),
                    rs.getString("account_number"),
                    rs.getString("account_name"),
                    rs.getString("branch_name") != null ? rs.getString("branch_name") : "Not provided",
                    rs.getString("branch_code") != null ? rs.getString("branch_code") : "Not provided"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLoanHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columnNames = {"Loan Number", "Principal", "Interest Rate", "Total Amount", "Status", "Application Date", "Due Date", "Outstanding Balance"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        customizeTable(table);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT l.loan_number, l.amount, l.interest_rate, l.total_amount, l.status, " +
                 "l.application_date, l.due_date, l.outstanding_balance, " +
                 "COALESCE(SUM(lp.principal_amount), 0) as total_principal_paid, " +
                 "COALESCE(SUM(lp.interest_amount), 0) as total_interest_paid " +
                 "FROM loans l " +
                 "LEFT JOIN loan_payments lp ON l.loan_id = lp.loan_id AND lp.status = 'Paid' " +
                 "WHERE l.client_id = ? " +
                 "GROUP BY l.loan_id, l.loan_number, l.amount, l.interest_rate, l.total_amount, " +
                 "l.status, l.application_date, l.due_date, l.outstanding_balance " +
                 "ORDER BY l.application_date DESC")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                double loanAmount = rs.getDouble("amount");
                double totalPrincipalPaid = rs.getDouble("total_principal_paid");
                double totalInterestPaid = rs.getDouble("total_interest_paid");
                double totalAmount = rs.getDouble("total_amount");
                
                // Calculate proper outstanding balance: remaining principal + remaining interest
                double remainingPrincipal = Math.max(0, loanAmount - totalPrincipalPaid);
                double totalInterestDue = totalAmount - loanAmount;
                double remainingInterest = Math.max(0, totalInterestDue - totalInterestPaid);
                double calculatedOutstandingBalance = remainingPrincipal + remainingInterest;
                
                model.addRow(new Object[]{
                    rs.getString("loan_number"),
                    String.format("ZMW %,.2f", loanAmount),
                    String.format("%.2f%%", rs.getDouble("interest_rate")),
                    String.format("ZMW %,.2f", totalAmount),
                    rs.getString("status"),
                    dateFormat.format(rs.getTimestamp("application_date")),
                    rs.getDate("due_date") != null ? dateFormat.format(rs.getDate("due_date")) : "N/A",
                    String.format("ZMW %,.2f", calculatedOutstandingBalance)
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // FIXED: Summary Panel with PROPER calculations
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBackground(new Color(240, 240, 240));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT " +
                 "COUNT(*) as total_loans, " +
                 "SUM(l.amount) as total_borrowed, " +
                 "SUM(l.total_amount - l.amount) as total_interest_due, " +
                 "SUM(l.amount - COALESCE(lp.total_principal_paid, 0)) as total_remaining_principal, " +
                 "SUM((l.total_amount - l.amount) - COALESCE(lp.total_interest_paid, 0)) as total_remaining_interest " +
                 "FROM loans l " +
                 "LEFT JOIN (SELECT loan_id, " +
                 "SUM(principal_amount) as total_principal_paid, " +
                 "SUM(interest_amount) as total_interest_paid " +
                 "FROM loan_payments WHERE status = 'Paid' GROUP BY loan_id) lp ON l.loan_id = lp.loan_id " +
                 "WHERE l.client_id = ? AND l.status IN ('Active', 'Approved')")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double totalRemainingPrincipal = rs.getDouble("total_remaining_principal");
                double totalRemainingInterest = rs.getDouble("total_remaining_interest");
                double totalOutstandingBalance = totalRemainingPrincipal + totalRemainingInterest;
                
                JLabel summaryLabel = new JLabel(String.format(
                    "Active Loans: %d | Total Principal: ZMW %,.2f | Total Interest Due: ZMW %,.2f | Outstanding Balance: ZMW %,.2f",
                    rs.getInt("total_loans"),
                    rs.getDouble("total_borrowed"),
                    rs.getDouble("total_interest_due"),
                    totalOutstandingBalance
                ));
                summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                summaryPanel.add(summaryLabel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createPaymentHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        String[] columnNames = {
            "Payment Date", "Loan Number", "Payment #", 
            "Amount Paid", "Principal", "Interest", "Penalty", 
            "Payment Mode", "Voucher #", "Status", "Received By"
        };
        
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        customizePaymentTable(table);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT lp.*, l.loan_number, e.name as received_by_name " +
                 "FROM loan_payments lp " +
                 "JOIN loans l ON lp.loan_id = l.loan_id " +
                 "LEFT JOIN employees e ON lp.received_by = e.employee_id " +
                 "WHERE l.client_id = ? " +
                 "ORDER BY lp.paid_date DESC, lp.scheduled_payment_date DESC")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String paymentDate = rs.getTimestamp("paid_date") != null ? 
                    datetimeFormat.format(rs.getTimestamp("paid_date")) : "Not Paid";
                
                model.addRow(new Object[]{
                    paymentDate,
                    rs.getString("loan_number"),
                    rs.getInt("payment_number"),
                    String.format("ZMW %,.2f", rs.getDouble("paid_amount")),
                    String.format("ZMW %,.2f", rs.getDouble("principal_amount")),
                    String.format("ZMW %,.2f", rs.getDouble("interest_amount")),
                    String.format("ZMW %,.2f", rs.getDouble("penalty_amount")),
                    rs.getString("payment_mode"),
                    rs.getString("voucher_number"),
                    rs.getString("status"),
                    rs.getString("received_by_name") != null ? rs.getString("received_by_name") : "N/A"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading payment history: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Payment Summary Panel
        JPanel summaryPanel = createPaymentSummaryPanel();
        panel.add(summaryPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createLoanCalculationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT l.loan_id, l.loan_number, l.amount, l.interest_rate, " +
                 "l.total_amount, l.outstanding_balance, l.status, " +
                 "COALESCE(SUM(lp.principal_amount), 0) as total_principal_paid, " +
                 "COALESCE(SUM(lp.interest_amount), 0) as total_interest_paid, " +
                 "COALESCE(SUM(lp.penalty_amount), 0) as total_penalty_paid " +
                 "FROM loans l " +
                 "LEFT JOIN loan_payments lp ON l.loan_id = lp.loan_id AND lp.status = 'Paid' " +
                 "WHERE l.client_id = ? " +
                 "GROUP BY l.loan_id, l.loan_number, l.amount, l.interest_rate, l.total_amount, " +
                 "l.outstanding_balance, l.status " +
                 "ORDER BY l.application_date DESC")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            boolean hasLoans = false;
            
            while (rs.next()) {
                hasLoans = true;
                double loanAmount = rs.getDouble("amount");
                double interestRate = rs.getDouble("interest_rate");
                double totalAmount = rs.getDouble("total_amount");
                double outstandingBalance = rs.getDouble("outstanding_balance");
                double totalPrincipalPaid = rs.getDouble("total_principal_paid");
                double totalInterestPaid = rs.getDouble("total_interest_paid");
                String status = rs.getString("status");
                
                // FIXED: Calculate interest details correctly
                double totalInterestDue = totalAmount - loanAmount;
                double remainingPrincipal = Math.max(0, loanAmount - totalPrincipalPaid);
                double remainingInterest = Math.max(0, totalInterestDue - totalInterestPaid);
                
                // CORRECT CALCULATION: Total outstanding balance = remaining principal + remaining interest
                double calculatedOutstandingBalance = remainingPrincipal + remainingInterest;
                
                JPanel loanPanel = createLoanCalculationCard(
                    rs.getString("loan_number"),
                    loanAmount,
                    interestRate,
                    totalAmount,
                    totalInterestDue,
                    totalPrincipalPaid,
                    totalInterestPaid,
                    remainingPrincipal,
                    remainingInterest,
                    calculatedOutstandingBalance,
                    status
                );
                
                contentPanel.add(loanPanel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }
            
            if (!hasLoans) {
                JLabel noLoansLabel = new JLabel("No loans found for this client");
                noLoansLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
                noLoansLabel.setForeground(Color.GRAY);
                noLoansLabel.setHorizontalAlignment(SwingConstants.CENTER);
                noLoansLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
                contentPanel.add(noLoansLabel);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading loan calculations: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLoanCalculationCard(String loanNumber, double loanAmount, double interestRate,
                                           double totalAmount, double totalInterestDue, double totalPrincipalPaid,
                                           double totalInterestPaid, double remainingPrincipal, double remainingInterest,
                                           double outstandingBalance, String status) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel loanLabel = new JLabel("Loan: " + loanNumber);
        loanLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loanLabel.setForeground(new Color(70, 130, 180));
        
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        switch (status) {
            case "Active": statusLabel.setForeground(new Color(21, 87, 36)); break;
            case "Approved": statusLabel.setForeground(new Color(12, 67, 125)); break;
            case "Pending": statusLabel.setForeground(new Color(133, 100, 4)); break;
            case "Rejected": statusLabel.setForeground(new Color(114, 28, 36)); break;
            case "Closed": statusLabel.setForeground(new Color(73, 80, 87)); break;
            default: statusLabel.setForeground(Color.BLACK);
        }
        
        headerPanel.add(loanLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Calculation details
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Add calculation rows
        addCalculationDetail(detailsPanel, "Principal Amount:", String.format("ZMW %,.2f", loanAmount));
        addCalculationDetail(detailsPanel, "Interest Rate:", String.format("%.2f%%", interestRate));
        addCalculationDetail(detailsPanel, "Total Loan Amount:", String.format("ZMW %,.2f", totalAmount));
        addCalculationDetail(detailsPanel, "Total Interest Due:", String.format("ZMW %,.2f", totalInterestDue));
        
        // Separator
        addCalculationDetail(detailsPanel, "", "");
        addCalculationDetail(detailsPanel, "--- Payments Made ---", "");
        
        addCalculationDetail(detailsPanel, "Principal Paid:", String.format("ZMW %,.2f", totalPrincipalPaid));
        addCalculationDetail(detailsPanel, "Interest Paid:", String.format("ZMW %,.2f", totalInterestPaid));
        
        // Separator
        addCalculationDetail(detailsPanel, "", "");
        addCalculationDetail(detailsPanel, "--- Remaining Balance ---", "");
        
        addCalculationDetail(detailsPanel, "Remaining Principal:", 
            String.format("ZMW %,.2f", remainingPrincipal), 
            remainingPrincipal > 0 ? new Color(220, 20, 60) : new Color(21, 87, 36));
        
        // FIXED: Show REMAINING interest correctly
        addCalculationDetail(detailsPanel, "Remaining Interest:", 
            String.format("ZMW %,.2f", remainingInterest), 
            remainingInterest > 0 ? new Color(220, 20, 60) : new Color(21, 87, 36));
        
        // CORRECT: Total outstanding balance = remaining principal + remaining interest
        addCalculationDetail(detailsPanel, "Total Outstanding Balance:", 
            String.format("ZMW %,.2f", outstandingBalance), 
            outstandingBalance > 0 ? new Color(220, 20, 60) : new Color(21, 87, 36));
        
        // Loan status message - FIXED: Only show fully paid if both principal AND interest are 0
        if (remainingPrincipal <= 0 && remainingInterest <= 0) {
            addCalculationDetail(detailsPanel, "", "");
            addCalculationDetail(detailsPanel, "✅ Loan Fully Paid", "");
        } else if (remainingPrincipal <= 0 && remainingInterest > 0) {
            addCalculationDetail(detailsPanel, "", "");
            addCalculationDetail(detailsPanel, "⚠️ Principal Paid, Interest Due:", 
                String.format("ZMW %,.2f", remainingInterest));
        } else if (remainingPrincipal > 0 && remainingInterest > 0) {
            addCalculationDetail(detailsPanel, "", "");
            addCalculationDetail(detailsPanel, "📊 Payment Required:", 
                "Principal + Interest = ZMW " + String.format("%,.2f", outstandingBalance));
        }
        
        // Progress indicators
        addCalculationDetail(detailsPanel, "", "");
        addCalculationDetail(detailsPanel, "--- Progress ---", "");
        
        double principalProgress = loanAmount > 0 ? (totalPrincipalPaid / loanAmount) * 100 : 0;
        double interestProgress = totalInterestDue > 0 ? (totalInterestPaid / totalInterestDue) * 100 : 0;
        
        addCalculationDetail(detailsPanel, "Principal Paid:", 
            String.format("%.1f%% (ZMW %,.2f / ZMW %,.2f)", 
                principalProgress, totalPrincipalPaid, loanAmount));
        
        addCalculationDetail(detailsPanel, "Interest Paid:", 
            String.format("%.1f%% (ZMW %,.2f / ZMW %,.2f)", 
                interestProgress, totalInterestPaid, totalInterestDue));
        
        card.add(detailsPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createPaymentSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(new Color(240, 240, 240));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        statsPanel.setBackground(new Color(240, 240, 240));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        // Print Button
        JButton printButton = new JButton("🖨️ Print Statement");
        styleButton(printButton, new Color(70, 130, 180), new Color(60, 120, 170));
        printButton.addActionListener(e -> printPaymentStatement());
        buttonPanel.add(printButton);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT " +
                 "COUNT(*) as total_payments, " +
                 "SUM(paid_amount) as total_paid, " +
                 "SUM(principal_amount) as total_principal, " +
                 "SUM(interest_amount) as total_interest, " +
                 "SUM(penalty_amount) as total_penalty, " +
                 "SUM(principal_amount + interest_amount + penalty_amount) as total_due " +
                 "FROM loan_payments lp " +
                 "JOIN loans l ON lp.loan_id = l.loan_id " +
                 "WHERE l.client_id = ? AND lp.status = 'Paid'")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                statsPanel.add(createSummaryCard("Total Payments", 
                    String.valueOf(rs.getInt("total_payments")), new Color(70, 130, 180)));
                statsPanel.add(createSummaryCard("Total Paid", 
                    String.format("ZMW %,.2f", rs.getDouble("total_paid")), new Color(60, 179, 113)));
                statsPanel.add(createSummaryCard("Total Principal", 
                    String.format("ZMW %,.2f", rs.getDouble("total_principal")), new Color(255, 165, 0)));
                statsPanel.add(createSummaryCard("Total Interest", 
                    String.format("ZMW %,.2f", rs.getDouble("total_interest")), new Color(220, 20, 60)));
                statsPanel.add(createSummaryCard("Total Due Paid", 
                    String.format("ZMW %,.2f", rs.getDouble("total_due")), new Color(151, 117, 250)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        summaryPanel.add(statsPanel, BorderLayout.CENTER);
        summaryPanel.add(buttonPanel, BorderLayout.EAST);
        
        return summaryPanel;
    }
    
   private void printPaymentStatement() {
    try {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Client Payment Statement - " + client.getClientId());
        
        Printable printable = new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }
                
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                
                int y = 50;
                int lineHeight = 20;
                
                // Draw logo if loaded from database successfully
                boolean logoLoaded = companyLogo != null;
                if (logoLoaded) {
                    // Draw logo with ORIGINAL SHAPE (not circular) - maintain aspect ratio
                    int maxLogoHeight = 80;
                    double aspectRatio = (double) companyLogo.getWidth(null) / companyLogo.getHeight(null);
                    int logoWidth = (int) (maxLogoHeight * aspectRatio);
                    int logoHeight = maxLogoHeight;
                    
                    // Draw logo in its original rectangular shape
                    g2d.drawImage(companyLogo, 50, y, logoWidth, logoHeight, null);
                    y += logoHeight + 10;
                }
                
                // COMPANY HEADER (position based on whether logo is loaded)
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.setColor(new Color(0, 173, 181));
                g2d.drawString("MS CODEFORGE", logoLoaded ? 150 : 50, logoLoaded ? y - 60 : y);
                
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.setColor(Color.BLACK);
                g2d.drawString("CLIENT PAYMENT STATEMENT", logoLoaded ? 150 : 50, logoLoaded ? y - 30 : y + lineHeight);
                y += lineHeight + 10;
                
                g2d.setStroke(new BasicStroke(2));
                g2d.setColor(new Color(0, 173, 181));
                g2d.drawLine(50, y, 550, y);
                y += 20;
                
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                g2d.setColor(Color.BLACK);
                g2d.drawString("Generated on: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new java.util.Date()), 50, y);
                y += lineHeight;
                
                // Client Information
                String clientInfo = getClientInfo();
                String[] clientLines = clientInfo.split("\n");
                for (String line : clientLines) {
                    g2d.drawString(line, 50, y);
                    y += lineHeight;
                }
                
                y += 10;
                
                // Payment Summary with interest breakdown
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("PAYMENT SUMMARY", 50, y);
                y += lineHeight + 5;
                
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                String paymentSummary = getPaymentSummary();
                String[] summaryLines = paymentSummary.split("\n");
                for (String line : summaryLines) {
                    g2d.drawString(line, 50, y);
                    y += lineHeight;
                }
                
                y += 10;
                
                // Loan Calculations Summary
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("LOAN CALCULATIONS SUMMARY", 50, y);
                y += lineHeight + 5;
                
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                String loanCalculations = getLoanCalculationsSummary();
                String[] calculationLines = loanCalculations.split("\n");
                for (String line : calculationLines) {
                    g2d.drawString(line, 50, y);
                    y += lineHeight;
                }
                
                y += 10;
                
                // Payment Details Header
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("PAYMENT DETAILS", 50, y);
                y += lineHeight + 5;
                
                // Payment Details Table Headers
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString("Date", 50, y);
                g2d.drawString("Loan #", 120, y);
                g2d.drawString("Amount", 200, y);
                g2d.drawString("Principal", 280, y);
                g2d.drawString("Interest", 360, y);
                g2d.drawString("Mode", 440, y);
                g2d.drawString("Status", 500, y);
                y += lineHeight;
                
                // Payment Details
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                List<String[]> paymentData = getPaymentData();
                for (String[] payment : paymentData) {
                    if (y > pageFormat.getImageableHeight() - 50) {
                        return PAGE_EXISTS;
                    }
                    
                    g2d.drawString(payment[0], 50, y); // Date
                    g2d.drawString(payment[1], 120, y); // Loan Number
                    g2d.drawString(payment[2], 200, y); // Amount
                    g2d.drawString(payment[3], 280, y); // Principal
                    g2d.drawString(payment[4], 360, y); // Interest
                    g2d.drawString(payment[5], 440, y); // Mode
                    g2d.drawString(payment[6], 500, y); // Status
                    y += lineHeight;
                }
                
                // Footer
                y = (int) pageFormat.getImageableHeight() - 30;
                g2d.setFont(new Font("Arial", Font.ITALIC, 10));
                g2d.drawString("Generated by MS CodeForge Loan Management System", 50, y);
                
                return PAGE_EXISTS;
            }
        };
        
        job.setPrintable(printable);
        
        if (job.printDialog()) {
            job.print();
            JOptionPane.showMessageDialog(this, 
                "Payment statement sent to printer successfully!", 
                "Print Success", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
    } catch (PrinterException ex) {
        JOptionPane.showMessageDialog(this, 
            "Error printing statement: " + ex.getMessage(), 
            "Print Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}
    
    private String getLoanCalculationsSummary() {
        StringBuilder summary = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT l.loan_number, l.amount, l.interest_rate, l.total_amount, " +
                 "l.outstanding_balance, " +
                 "COALESCE(SUM(lp.principal_amount), 0) as total_principal_paid, " +
                 "COALESCE(SUM(lp.interest_amount), 0) as total_interest_paid " +
                 "FROM loans l " +
                 "LEFT JOIN loan_payments lp ON l.loan_id = lp.loan_id AND lp.status = 'Paid' " +
                 "WHERE l.client_id = ? " +
                 "GROUP BY l.loan_id, l.loan_number, l.amount, l.interest_rate, l.total_amount, l.outstanding_balance")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                double loanAmount = rs.getDouble("amount");
                double totalAmount = rs.getDouble("total_amount");
                double totalPrincipalPaid = rs.getDouble("total_principal_paid");
                double totalInterestPaid = rs.getDouble("total_interest_paid");
                double totalInterestDue = totalAmount - loanAmount;
                double remainingPrincipal = Math.max(0, loanAmount - totalPrincipalPaid);
                double remainingInterest = Math.max(0, totalInterestDue - totalInterestPaid);
                double calculatedOutstandingBalance = remainingPrincipal + remainingInterest;
                
                summary.append("Loan ").append(rs.getString("loan_number")).append(": ")
                      .append("Principal: ZMW ").append(String.format("%,.2f", loanAmount))
                      .append(" | Interest Due: ZMW ").append(String.format("%,.2f", totalInterestDue))
                      .append(" | Outstanding: ZMW ").append(String.format("%,.2f", calculatedOutstandingBalance))
                      .append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary.toString();
    }
    
    private String getClientInfo() {
        StringBuilder info = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT title, first_name, last_name, id_number, phone_number " +
                 "FROM clients WHERE client_id = ?")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                info.append("Client: ").append(rs.getString("title"))
                    .append(" ").append(rs.getString("first_name"))
                    .append(" ").append(rs.getString("last_name")).append("\n");
                info.append("ID Number: ").append(rs.getString("id_number")).append("\n");
                info.append("Phone: ").append(rs.getString("phone_number")).append("\n");
                info.append("Client ID: ").append(client.getClientId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return info.toString();
    }
    
    private String getPaymentSummary() {
        StringBuilder summary = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT " +
                 "COUNT(*) as total_payments, " +
                 "SUM(paid_amount) as total_paid, " +
                 "SUM(principal_amount) as total_principal, " +
                 "SUM(interest_amount) as total_interest, " +
                 "SUM(penalty_amount) as total_penalty, " +
                 "SUM(principal_amount + interest_amount + penalty_amount) as total_due_paid " +
                 "FROM loan_payments lp " +
                 "JOIN loans l ON lp.loan_id = l.loan_id " +
                 "WHERE l.client_id = ? AND lp.status = 'Paid'")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                summary.append("Total Payments: ").append(rs.getInt("total_payments")).append("\n");
                summary.append("Total Amount Paid: ZMW ").append(String.format("%,.2f", rs.getDouble("total_paid"))).append("\n");
                summary.append("Total Principal Paid: ZMW ").append(String.format("%,.2f", rs.getDouble("total_principal"))).append("\n");
                summary.append("Total Interest Paid: ZMW ").append(String.format("%,.2f", rs.getDouble("total_interest"))).append("\n");
                summary.append("Total Penalty Paid: ZMW ").append(String.format("%,.2f", rs.getDouble("total_penalty"))).append("\n");
                summary.append("Total Due Paid: ZMW ").append(String.format("%,.2f", rs.getDouble("total_due_paid")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary.toString();
    }
    
    private List<String[]> getPaymentData() {
        List<String[]> paymentData = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT lp.paid_date, l.loan_number, lp.paid_amount, " +
                 "lp.principal_amount, lp.interest_amount, lp.payment_mode, lp.status " +
                 "FROM loan_payments lp " +
                 "JOIN loans l ON lp.loan_id = l.loan_id " +
                 "WHERE l.client_id = ? " +
                 "ORDER BY lp.paid_date DESC")) {
            
            stmt.setInt(1, client.getClientId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String date = rs.getTimestamp("paid_date") != null ? 
                    new SimpleDateFormat("dd-MMM-yy").format(rs.getTimestamp("paid_date")) : "N/A";
                String amount = String.format("%,.2f", rs.getDouble("paid_amount"));
                String principal = String.format("%,.2f", rs.getDouble("principal_amount"));
                String interest = String.format("%,.2f", rs.getDouble("interest_amount"));
                
                paymentData.add(new String[]{
                    date,
                    rs.getString("loan_number"),
                    amount,
                    principal,
                    interest,
                    rs.getString("payment_mode"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paymentData;
    }
    
    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(color);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        valueLabel.setForeground(Color.BLACK);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void customizePaymentTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // Payment Date
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Loan Number
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Payment #
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Amount Paid
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Principal
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Interest
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Penalty
        table.getColumnModel().getColumn(7).setPreferredWidth(80);  // Payment Mode
        table.getColumnModel().getColumn(8).setPreferredWidth(100); // Voucher #
        table.getColumnModel().getColumn(9).setPreferredWidth(80);  // Status
        table.getColumnModel().getColumn(10).setPreferredWidth(120); // Received By
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        
        // Center align all columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelComp.setForeground(new Color(70, 70, 70));
        panel.add(labelComp, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(valueComp, gbc);
    }
    
    private void customizeTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    
    private void addCalculationDetail(JPanel panel, String label, String value) {
        addCalculationDetail(panel, label, value, Color.BLACK);
    }
    
    private void addCalculationDetail(JPanel panel, String label, String value, Color color) {
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (label.startsWith("---") || label.contains("✅") || label.contains("⚠️") || label.contains("📊")) {
            labelLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }
        panel.add(labelLabel);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueLabel.setForeground(color);
        if (label.startsWith("---") || label.contains("✅") || label.contains("⚠️") || label.contains("📊")) {
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }
        panel.add(valueLabel);
    }
    
    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
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
    
    private void goBack() {
        ScreenManager.getInstance().showScreen(new ClientsScreen(currentUserId, currentUserRole));
    }
    
    private void loadClientDetails() {
        // Method implementation if needed
    }
}