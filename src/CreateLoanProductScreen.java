import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CreateLoanProductScreen extends JPanel {
    private int employeeId;
    private String userRole;
    
    private JTextField productNameField;
    private JTextField interestRateField;
    private JComboBox<String> calculationMethodComboBox;
    private JTextField loanTermField;
    private JTextField gracePeriodField;
    private JComboBox<String> installmentTypeComboBox;
    private JComboBox<String> loanFeeTypeComboBox;
    private JComboBox<String> category1ComboBox;
    private JComboBox<String> category2ComboBox;
    private JComboBox<String> refinanceComboBox;

    public CreateLoanProductScreen(int employeeId, String userRole) {
        this.employeeId = employeeId;
        this.userRole = userRole;
        initUI();
        logActivity("Loan Product", "Accessed create loan product screen");
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Form Panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonsPanel = createButtonsPanel();
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        headerPanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("CREATE LOAN PRODUCT");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton backBtn = createStyledButton("⬅️ Back to Loans", new Color(57, 62, 70));
        backBtn.addActionListener(e -> goBackToLoans());
        headerPanel.add(backBtn, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        // Product Name
        formPanel.add(new JLabel("Product Name *:"));
        productNameField = new JTextField();
        productNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(productNameField);

        // Interest Rate
        formPanel.add(new JLabel("Interest Rate (%) *:"));
        interestRateField = new JTextField();
        interestRateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(interestRateField);

        // Calculation Method
        formPanel.add(new JLabel("Calculation Method *:"));
        calculationMethodComboBox = new JComboBox<>(new String[]{"FLAT", "REDUCING"});
        calculationMethodComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(calculationMethodComboBox);

        // Installment Type
        formPanel.add(new JLabel("Installment Type *:"));
        installmentTypeComboBox = new JComboBox<>(new String[]{"Weekly", "Monthly", "Quarterly", "Annually"});
        installmentTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(installmentTypeComboBox);

        // Grace Period
        formPanel.add(new JLabel("Grace Period (Months):"));
        gracePeriodField = new JTextField("0");
        gracePeriodField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(gracePeriodField);

        // Loan Fee Type
        formPanel.add(new JLabel("Loan Fee Type *:"));
        loanFeeTypeComboBox = new JComboBox<>(new String[]{"Cash", "Mobile", "Bank"});
        loanFeeTypeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(loanFeeTypeComboBox);

        // Category 1
        formPanel.add(new JLabel("Category 1 *:"));
        category1ComboBox = new JComboBox<>(new String[]{"Personal", "Business", "Education"});
        category1ComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(category1ComboBox);

        // Category 2
        formPanel.add(new JLabel("Category 2 *:"));
        category2ComboBox = new JComboBox<>(new String[]{"Short-Term", "Long-Term", "Microloan"});
        category2ComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(category2ComboBox);

        // Refinance
        formPanel.add(new JLabel("Refinance Allowed:"));
        refinanceComboBox = new JComboBox<>(new String[]{"No", "Yes"});
        refinanceComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(refinanceComboBox);

        return formPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonsPanel.setBackground(new Color(245, 245, 245));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton clearBtn = createStyledButton("🗑️ Clear Form", new Color(255, 159, 67));
        clearBtn.addActionListener(e -> clearForm());

        JButton createBtn = createStyledButton("✅ Create Product", new Color(97, 218, 121));
        createBtn.addActionListener(e -> createLoanProduct());

        buttonsPanel.add(clearBtn);
        buttonsPanel.add(createBtn);

        return buttonsPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private void createLoanProduct() {
        if (!validateForm()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to create this loan product?", "Confirm Creation", 
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return saveLoanProduct();
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(CreateLoanProductScreen.this, 
                            "Loan product created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                    } else {
                        showError("Failed to create loan product");
                    }
                } catch (Exception ex) {
                    showError("Error creating loan product: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private boolean saveLoanProduct() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO loan_products (product_name, interest_rate, calculation_method, " +
                        "installment_type, grace_period, loan_fee_type, category1, category2, refinance, created_by) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, productNameField.getText().trim());
                stmt.setDouble(2, Double.parseDouble(interestRateField.getText()));
                stmt.setString(3, (String) calculationMethodComboBox.getSelectedItem());
                stmt.setString(4, (String) installmentTypeComboBox.getSelectedItem());
                stmt.setInt(5, Integer.parseInt(gracePeriodField.getText()));
                stmt.setString(6, (String) loanFeeTypeComboBox.getSelectedItem());
                stmt.setString(7, (String) category1ComboBox.getSelectedItem());
                stmt.setString(8, (String) category2ComboBox.getSelectedItem());
                stmt.setBoolean(9, "Yes".equals(refinanceComboBox.getSelectedItem()));
                stmt.setInt(10, employeeId);
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    logActivity("Loan Product Created", 
                        "Created loan product: " + productNameField.getText().trim());
                    return true;
                }
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("duplicate key") || ex.getMessage().contains("unique constraint")) {
                showError("A loan product with this name already exists");
            } else {
                showError("Database error: " + ex.getMessage());
            }
        }
        return false;
    }

    private boolean validateForm() {
        try {
            // Check required fields
            if (productNameField.getText().trim().isEmpty()) {
                showError("Please enter product name");
                return false;
            }
            if (interestRateField.getText().trim().isEmpty()) {
                showError("Please enter interest rate");
                return false;
            }
            if (gracePeriodField.getText().trim().isEmpty()) {
                showError("Please enter grace period");
                return false;
            }

            // Validate numeric values
            double interestRate = Double.parseDouble(interestRateField.getText());
            int gracePeriod = Integer.parseInt(gracePeriodField.getText());

            if (interestRate <= 0 || interestRate > 100) {
                showError("Interest rate must be between 0.01 and 100");
                return false;
            }
            if (gracePeriod < 0) {
                showError("Grace period cannot be negative");
                return false;
            }

            return true;

        } catch (NumberFormatException e) {
            showError("Please enter valid numbers for interest rate and grace period");
            return false;
        }
    }

    private void clearForm() {
        productNameField.setText("");
        interestRateField.setText("");
        gracePeriodField.setText("0");
        calculationMethodComboBox.setSelectedIndex(0);
        installmentTypeComboBox.setSelectedIndex(0);
        loanFeeTypeComboBox.setSelectedIndex(0);
        category1ComboBox.setSelectedIndex(0);
        category2ComboBox.setSelectedIndex(0);
        refinanceComboBox.setSelectedIndex(0);
    }

    private void goBackToLoans() {
        ScreenManager.getInstance().showScreen(new LoansScreen(employeeId, userRole));
    }

    private void logActivity(String action, String details) {
        try (Connection conn = DatabaseConnection.getConnection()) {
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