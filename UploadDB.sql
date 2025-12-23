-- ============================================
-- MS CODEFORGE LOAN MANAGEMENT SYSTEM
-- Complete Database Schema for PostgreSQL/Supabase
-- ============================================

-- Enable necessary extensions
-- Note: Supabase has these enabled by default

-- ============================================
-- 1. CORE TABLES
-- ============================================

-- Employees table
CREATE TABLE employees (
    employee_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('admin', 'employee')) NOT NULL DEFAULT 'employee',
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMPTZ,
    is_active BOOLEAN DEFAULT TRUE
);

-- Branches table
CREATE TABLE branches (
    branch_id SERIAL PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    email VARCHAR(100),
    manager_id INTEGER REFERENCES employees(employee_id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- Clients table
CREATE TABLE clients (
    client_id SERIAL PRIMARY KEY,
    branch_id INTEGER REFERENCES branches(branch_id),
    title VARCHAR(20) CHECK (title IN ('Mr', 'Ms', 'Mrs', 'Dr', 'Prof', 'Other')) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')) NOT NULL,
    marital_status VARCHAR(20) CHECK (marital_status IN ('Single', 'Married', 'Divorced', 'Widowed')) NOT NULL,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(100),
    physical_address TEXT NOT NULL,
    province VARCHAR(50) NOT NULL,
    postal_address TEXT,
    id_type VARCHAR(50) NOT NULL,
    id_number VARCHAR(50) NOT NULL UNIQUE,
    id_place VARCHAR(50) DEFAULT 'GRZ',
    employment_status VARCHAR(20) CHECK (employment_status IN ('Employed', 'Self-Employed', 'Unemployed')) NOT NULL,
    employer_name VARCHAR(100),
    employee_number VARCHAR(50),
    job_title VARCHAR(100),
    monthly_income DECIMAL(15,2) DEFAULT 0.00,
    credit_score INTEGER DEFAULT 0,
    risk_level VARCHAR(20) CHECK (risk_level IN ('Low', 'Medium', 'High')) DEFAULT 'Medium',
    created_by INTEGER REFERENCES employees(employee_id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- ============================================
-- 2. CLIENT RELATED TABLES
-- ============================================

-- Next of Kin table
CREATE TABLE next_of_kin (
    kin_id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    relationship VARCHAR(20) CHECK (relationship IN ('Spouse', 'Parent', 'Sibling', 'Child', 'Other')) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    address TEXT,
    id_number VARCHAR(50),
    email VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Bank Details table
CREATE TABLE bank_details (
    bank_id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(client_id) ON DELETE CASCADE,
    bank_name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    account_name VARCHAR(100) NOT NULL,
    branch_code VARCHAR(20),
    branch_name VARCHAR(100),
    account_type VARCHAR(20) CHECK (account_type IN ('Savings', 'Current', 'Mobile Money', 'Other')),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 3. LOAN PRODUCTS & LOANS
-- ============================================

-- Loan Products table
CREATE TABLE loan_products (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL UNIQUE,
    product_code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    min_amount DECIMAL(15,2) DEFAULT 0.00,
    max_amount DECIMAL(15,2) DEFAULT 1000000.00,
    interest_rate DECIMAL(5,2) NOT NULL,
    calculation_method VARCHAR(20) CHECK (calculation_method IN ('FLAT', 'REDUCING')) NOT NULL DEFAULT 'FLAT',
    installment_type VARCHAR(20) CHECK (installment_type IN ('Weekly', 'Monthly', 'Quarterly', 'Annually')) NOT NULL,
    min_term INTEGER DEFAULT 1,
    max_term INTEGER DEFAULT 60,
    grace_period INTEGER DEFAULT 0,
    loan_fee_type VARCHAR(20) CHECK (loan_fee_type IN ('Cash', 'Mobile', 'Bank')) NOT NULL DEFAULT 'Cash',
    category1 VARCHAR(20) CHECK (category1 IN ('Personal', 'Business', 'Education', 'Agricultural')) NOT NULL,
    category2 VARCHAR(20) CHECK (category2 IN ('Short-Term', 'Long-Term', 'Microloan')) NOT NULL,
    refinance BOOLEAN DEFAULT FALSE,
    requirements TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_by INTEGER REFERENCES employees(employee_id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Loan Sequence table
CREATE SEQUENCE loan_number_seq START 1000;

-- Loans table
CREATE TABLE loans (
    loan_id SERIAL PRIMARY KEY,
    loan_number VARCHAR(20) UNIQUE NOT NULL DEFAULT 'LN' || LPAD(nextval('loan_number_seq')::text, 6, '0'),
    client_id INTEGER NOT NULL REFERENCES clients(client_id),
    product_id INTEGER REFERENCES loan_products(product_id),
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    interest_rate DECIMAL(5,2) NOT NULL,
    calculation_method VARCHAR(20) CHECK (calculation_method IN ('FLAT', 'REDUCING')) NOT NULL,
    loan_term INTEGER NOT NULL CHECK (loan_term > 0),
    installment_type VARCHAR(20) CHECK (installment_type IN ('Weekly', 'Monthly', 'Quarterly', 'Annually')) NOT NULL,
    loan_fee_type VARCHAR(20) CHECK (loan_fee_type IN ('Cash', 'Mobile', 'Bank')) NOT NULL,
    category1 VARCHAR(20) CHECK (category1 IN ('Personal', 'Business', 'Education', 'Agricultural')) NOT NULL,
    category2 VARCHAR(20) CHECK (category2 IN ('Short-Term', 'Long-Term', 'Microloan')) NOT NULL,
    total_amount DECIMAL(15,2) NOT NULL,
    interest_amount DECIMAL(15,2) NOT NULL,
    installment_amount DECIMAL(15,2) NOT NULL,
    outstanding_balance DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) CHECK (status IN ('Pending', 'Under Review', 'Approved', 'Active', 'Rejected', 'Closed', 'Defaulted', 'Written Off')) DEFAULT 'Pending',
    application_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    reviewed_date TIMESTAMPTZ,
    approved_date TIMESTAMPTZ,
    disbursement_date TIMESTAMPTZ,
    due_date DATE,
    closed_date TIMESTAMPTZ,
    reviewed_by INTEGER REFERENCES employees(employee_id),
    approved_by INTEGER REFERENCES employees(employee_id),
    disbursed_by INTEGER REFERENCES employees(employee_id),
    created_by INTEGER NOT NULL REFERENCES employees(employee_id),
    collateral_details TEXT,
    guarantors_details TEXT,
    rejection_reason TEXT,
    closing_reason TEXT,
    remarks TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 4. PAYMENT TABLES
-- ============================================

-- Loan Payments table
CREATE TABLE loan_payments (
    payment_id SERIAL PRIMARY KEY,
    loan_id INTEGER NOT NULL REFERENCES loans(loan_id) ON DELETE CASCADE,
    payment_number INTEGER NOT NULL,
    scheduled_payment_date DATE NOT NULL,
    payment_amount DECIMAL(15,2) NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL,
    interest_amount DECIMAL(15,2) NOT NULL,
    penalty_amount DECIMAL(15,2) DEFAULT 0.00,
    paid_amount DECIMAL(15,2) DEFAULT 0.00,
    balance_before DECIMAL(15,2),
    balance_after DECIMAL(15,2),
    paid_date TIMESTAMPTZ,
    status VARCHAR(20) CHECK (status IN ('Pending', 'Paid', 'Overdue', 'Partial', 'Missed')) DEFAULT 'Pending',
    payment_mode VARCHAR(20) CHECK (payment_mode IN ('Cash', 'Mobile', 'Bank', 'Other')) NOT NULL DEFAULT 'Cash',
    voucher_number VARCHAR(50) NOT NULL,
    reference_number VARCHAR(50),
    received_by INTEGER REFERENCES employees(employee_id),
    approved_by INTEGER REFERENCES employees(employee_id),
    approved_date TIMESTAMPTZ,
    rejection_reason TEXT,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Payment Receipts table
CREATE TABLE payment_receipts (
    receipt_id SERIAL PRIMARY KEY,
    loan_id INTEGER NOT NULL REFERENCES loans(loan_id),
    receipt_number VARCHAR(50) UNIQUE NOT NULL DEFAULT 'RCPT' || LPAD(nextval('loan_number_seq')::text, 6, '0'),
    amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_mode VARCHAR(20) CHECK (payment_mode IN ('Cash', 'Mobile', 'Bank', 'Other')) NOT NULL,
    voucher_number VARCHAR(50) NOT NULL,
    reference_number VARCHAR(50),
    status VARCHAR(20) CHECK (status IN ('Pending', 'Approved', 'Rejected')) DEFAULT 'Pending',
    rejection_reason TEXT,
    received_by INTEGER NOT NULL REFERENCES employees(employee_id),
    approved_by INTEGER REFERENCES employees(employee_id),
    approved_date TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 5. SYSTEM TABLES
-- ============================================

-- System Settings table for logos and configurations
CREATE TABLE system_settings (
    setting_id SERIAL PRIMARY KEY,
    setting_key VARCHAR(50) UNIQUE NOT NULL,
    setting_name VARCHAR(100) NOT NULL,
    setting_value TEXT,
    setting_type VARCHAR(20) CHECK (setting_type IN ('TEXT', 'NUMBER', 'BOOLEAN', 'IMAGE', 'JSON', 'COLOR')) NOT NULL DEFAULT 'TEXT',
    category VARCHAR(50) DEFAULT 'General',
    is_public BOOLEAN DEFAULT TRUE,
    editable BOOLEAN DEFAULT TRUE,
    description TEXT,
    last_updated TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_by INTEGER REFERENCES employees(employee_id),
    CONSTRAINT valid_number CHECK (
        setting_type != 'NUMBER' OR setting_value ~ '^[0-9]+(\.[0-9]+)?$'
    ),
    CONSTRAINT valid_boolean CHECK (
        setting_type != 'BOOLEAN' OR setting_value IN ('true', 'false', '1', '0')
    )
);

-- Audit Logs table
CREATE TABLE audit_logs (
    log_id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employees(employee_id),
    employee_name VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    action_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    table_name VARCHAR(50),
    record_id INTEGER,
    old_values JSONB,
    new_values JSONB,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    severity VARCHAR(20) CHECK (severity IN ('INFO', 'WARNING', 'ERROR', 'CRITICAL')) DEFAULT 'INFO'
);

-- System Backup table
CREATE TABLE system_backups (
    backup_id SERIAL PRIMARY KEY,
    backup_name VARCHAR(255) NOT NULL,
    backup_date TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    backup_type VARCHAR(20) CHECK (backup_type IN ('FULL', 'INCREMENTAL', 'MANUAL')) DEFAULT 'MANUAL',
    created_by INTEGER NOT NULL REFERENCES employees(employee_id),
    records_count INTEGER DEFAULT 0,
    status VARCHAR(20) CHECK (status IN ('SUCCESS', 'FAILED', 'IN_PROGRESS')) DEFAULT 'SUCCESS',
    error_message TEXT,
    completed_at TIMESTAMPTZ
);

-- ============================================
-- 6. ADDITIONAL TABLES FOR ENHANCED FUNCTIONALITY
-- ============================================

-- Notifications table
CREATE TABLE notifications (
    notification_id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(employee_id),
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(20) CHECK (type IN ('INFO', 'WARNING', 'SUCCESS', 'ERROR', 'REMINDER')),
    is_read BOOLEAN DEFAULT FALSE,
    related_table VARCHAR(50),
    related_id INTEGER,
    action_url VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ
);

-- Loan Schedules table (pre-calculated schedules)
CREATE TABLE loan_schedules (
    schedule_id SERIAL PRIMARY KEY,
    loan_id INTEGER NOT NULL REFERENCES loans(loan_id) ON DELETE CASCADE,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    principal_due DECIMAL(15,2) NOT NULL,
    interest_due DECIMAL(15,2) NOT NULL,
    total_due DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0.00,
    paid_date TIMESTAMPTZ,
    status VARCHAR(20) CHECK (status IN ('Pending', 'Paid', 'Overdue', 'Partial')) DEFAULT 'Pending',
    penalty_applied DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 7. INSERT DEFAULT DATA
-- ============================================

-- Insert default branches
INSERT INTO branches (branch_name, location) VALUES 
('Lusaka', 'Lusaka Province'),
('Kitwe', 'Copperbelt Province'),
('Ndola', 'Copperbelt Province'),
('Livingstone', 'Southern Province'),
('Chipata', 'Eastern Province');

-- Insert default admin user with hashed password (change in production!)
-- Note: Use proper password hashing in your application
INSERT INTO employees (name, role, password, email) VALUES 
('Admin User', 'admin', '$2a$10$YourHashedPasswordHere', 'admin@mscodeforge.com'),
('Loan Officer', 'employee', '$2a$10$YourHashedPasswordHere', 'officer@mscodeforge.com');

-- Insert default loan products
INSERT INTO loan_products (product_name, product_code, description, min_amount, max_amount, interest_rate, calculation_method, installment_type, min_term, max_term, category1, category2) VALUES 
('Personal Microloan', 'PM-001', 'Short-term personal loans for urgent needs', 1000.00, 50000.00, 15.00, 'FLAT', 'Monthly', 1, 12, 'Personal', 'Microloan'),
('Business Expansion', 'BE-001', 'Medium-term loans for business growth', 50000.00, 500000.00, 12.50, 'REDUCING', 'Monthly', 6, 36, 'Business', 'Long-Term'),
('Education Loan', 'EL-001', 'Student and education financing', 10000.00, 200000.00, 10.00, 'REDUCING', 'Quarterly', 12, 60, 'Education', 'Long-Term'),
('Agricultural Inputs', 'AI-001', 'Seasonal loans for farming inputs', 5000.00, 100000.00, 14.00, 'FLAT', 'Annually', 1, 2, 'Agricultural', 'Short-Term');

-- Insert default system settings
INSERT INTO system_settings (setting_key, setting_name, setting_value, setting_type, category, description) 
VALUES 
('LOGO_PRIMARY', 'Primary Logo', 'iVBORw0KGgoAAAANSUhEUgAAAMgAAABkCAYAAADDhn8LAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAgSURBVHgB7cEBDQAAAMKg909tDjegAAAAAACeI3AABnGwOjQAAAAASUVORK5CYII=', 'IMAGE', 'Branding', 'Primary company logo for dashboards and reports'),
('LOGO_REPORT', 'Report Logo', 'iVBORw0KGgoAAAANSUhEUgAAAMgAAABkCAYAAADDhn8LAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAgSURBVHgB7cEBDQAAAMKg909tDjegAAAAAACeI3AABnGwOjQAAAAASUVORK5CYII=', 'IMAGE', 'Branding', 'Logo for printed reports and documents'),
('COMPANY_NAME', 'Company Name', 'MS CODEFORGE', 'TEXT', 'Branding', 'Official company name'),
('COMPANY_SLOGAN', 'Company Slogan', 'Banking Management System', 'TEXT', 'Branding', 'Company tagline'),
('REPORT_FOOTER', 'Report Footer', 'Generated by MS CodeForge - Loan Management System', 'TEXT', 'Reports', 'Default footer for all reports'),
('CURRENCY', 'Currency', 'ZMW', 'TEXT', 'General', 'Default currency'),
('CURRENCY_SYMBOL', 'Currency Symbol', 'K', 'TEXT', 'General', 'Currency symbol'),
('PENALTY_RATE', 'Late Payment Penalty', '5', 'NUMBER', 'Loans', 'Late payment penalty rate (%)'),
('GRACE_PERIOD', 'Grace Period', '7', 'NUMBER', 'Loans', 'Grace period for late payments (days)'),
('MAX_LOAN_AMOUNT', 'Maximum Loan Amount', '1000000', 'NUMBER', 'Loans', 'Maximum loan amount allowed'),
('MIN_LOAN_AMOUNT', 'Minimum Loan Amount', '1000', 'NUMBER', 'Loans', 'Minimum loan amount allowed'),
('DEFAULT_INTEREST', 'Default Interest Rate', '15', 'NUMBER', 'Loans', 'Default interest rate (%)'),
('SYSTEM_EMAIL', 'System Email', 'noreply@mscodeforge.com', 'TEXT', 'Email', 'System email address'),
('SMTP_SERVER', 'SMTP Server', 'smtp.gmail.com', 'TEXT', 'Email', 'SMTP server address'),
('THEME_PRIMARY', 'Primary Color', '#2c3e50', 'COLOR', 'UI', 'Primary theme color'),
('THEME_SECONDARY', 'Secondary Color', '#3498db', 'COLOR', 'UI', 'Secondary theme color'),
('DATE_FORMAT', 'Date Format', 'DD/MM/YYYY', 'TEXT', 'General', 'Default date format'),
('TIMEZONE', 'Timezone', 'Africa/Lusaka', 'TEXT', 'General', 'System timezone');

-- ============================================
-- 8. CREATE TRIGGERS AND FUNCTIONS
-- ============================================

-- Function to update timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at on all relevant tables
CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON employees FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_clients_updated_at BEFORE UPDATE ON clients FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_branches_updated_at BEFORE UPDATE ON branches FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_loans_updated_at BEFORE UPDATE ON loans FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_loan_payments_updated_at BEFORE UPDATE ON loan_payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payment_receipts_updated_at BEFORE UPDATE ON payment_receipts FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to handle loan approval
CREATE OR REPLACE FUNCTION handle_loan_approval()
RETURNS TRIGGER AS $$
DECLARE
    v_product loan_products%ROWTYPE;
    v_term INTEGER;
BEGIN
    IF NEW.status = 'Approved' AND OLD.status != 'Approved' THEN
        NEW.approved_date := CURRENT_TIMESTAMP;
        
        -- Get product details
        SELECT * INTO v_product FROM loan_products WHERE product_id = NEW.product_id;
        
        -- Calculate due date based on installment type and loan term
        v_term := NEW.loan_term;
        CASE NEW.installment_type 
            WHEN 'Weekly' THEN 
                NEW.due_date := CURRENT_DATE + (v_term * 7);
            WHEN 'Monthly' THEN 
                NEW.due_date := CURRENT_DATE + (v_term * 30);
            WHEN 'Quarterly' THEN 
                NEW.due_date := CURRENT_DATE + (v_term * 90);
            WHEN 'Annually' THEN 
                NEW.due_date := CURRENT_DATE + (v_term * 365);
        END CASE;
        
        -- Log the approval
        INSERT INTO audit_logs (employee_id, employee_name, action, table_name, record_id, details)
        SELECT NEW.approved_by, e.name, 'LOAN_APPROVED', 'loans', NEW.loan_id,
               CONCAT('Loan ', NEW.loan_number, ' approved for ZMW ', NEW.amount)
        FROM employees e WHERE e.employee_id = NEW.approved_by;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for loan approval
CREATE TRIGGER after_loan_approval
    BEFORE UPDATE ON loans
    FOR EACH ROW
    EXECUTE FUNCTION handle_loan_approval();

-- Function to handle payment status updates
CREATE OR REPLACE FUNCTION handle_payment_update()
RETURNS TRIGGER AS $$
BEGIN
    -- Update loan outstanding balance when payment is approved
    IF NEW.status = 'Paid' AND OLD.status != 'Paid' THEN
        UPDATE loans 
        SET outstanding_balance = outstanding_balance - NEW.paid_amount
        WHERE loan_id = NEW.loan_id;
        
        -- Update balance before/after
        NEW.balance_after := COALESCE(NEW.balance_before, 0) - NEW.paid_amount;
    END IF;
    
    -- Mark as overdue if payment date passed
    IF NEW.status = 'Pending' AND NEW.scheduled_payment_date < CURRENT_DATE THEN
        NEW.status := 'Overdue';
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for payment updates
CREATE TRIGGER after_payment_update
    BEFORE INSERT OR UPDATE ON loan_payments
    FOR EACH ROW
    EXECUTE FUNCTION handle_payment_update();

-- ============================================
-- 9. CREATE INDEXES FOR PERFORMANCE
-- ============================================

-- Clients indexes
CREATE INDEX idx_clients_branch_id ON clients(branch_id);
CREATE INDEX idx_clients_phone ON clients(phone_number);
CREATE INDEX idx_clients_id_number ON clients(id_number);
CREATE INDEX idx_clients_created_by ON clients(created_by);
CREATE INDEX idx_clients_created_at ON clients(created_at);
CREATE INDEX idx_clients_status ON clients(is_active);

-- Loans indexes
CREATE INDEX idx_loans_client_id ON loans(client_id);
CREATE INDEX idx_loans_product_id ON loans(product_id);
CREATE INDEX idx_loans_status ON loans(status);
CREATE INDEX idx_loans_due_date ON loans(due_date);
CREATE INDEX idx_loans_created_by ON loans(created_by);
CREATE INDEX idx_loans_approved_by ON loans(approved_by);
CREATE INDEX idx_loans_created_at ON loans(created_at);

-- Payment indexes
CREATE INDEX idx_loan_payments_loan_id ON loan_payments(loan_id);
CREATE INDEX idx_loan_payments_status ON loan_payments(status);
CREATE INDEX idx_loan_payments_date ON loan_payments(scheduled_payment_date);
CREATE INDEX idx_loan_payments_paid_date ON loan_payments(paid_date);
CREATE INDEX idx_payment_receipts_loan_id ON payment_receipts(loan_id);
CREATE INDEX idx_payment_receipts_status ON payment_receipts(status);
CREATE INDEX idx_payment_receipts_date ON payment_receipts(payment_date);

-- Related tables indexes
CREATE INDEX idx_next_of_kin_client_id ON next_of_kin(client_id);
CREATE INDEX idx_bank_details_client_id ON bank_details(client_id);
CREATE INDEX idx_audit_logs_employee_id ON audit_logs(employee_id);
CREATE INDEX idx_audit_logs_action_date ON audit_logs(action_date);
CREATE INDEX idx_audit_logs_severity ON audit_logs(severity);
CREATE INDEX idx_notifications_employee_id ON notifications(employee_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_loan_schedules_loan_id ON loan_schedules(loan_id);
CREATE INDEX idx_loan_schedules_status ON loan_schedules(status);

-- System settings index
CREATE INDEX idx_system_settings_key ON system_settings(setting_key);
CREATE INDEX idx_system_settings_category ON system_settings(category);

-- ============================================
-- 10. CREATE VIEWS FOR REPORTING
-- ============================================

-- Client Summary View
CREATE OR REPLACE VIEW client_summary AS
SELECT 
    c.client_id,
    c.branch_id,
    b.branch_name,
    CONCAT(c.title, ' ', c.first_name, ' ', COALESCE(c.middle_name || ' ', ''), c.last_name) as full_name,
    c.phone_number,
    c.email,
    c.id_number,
    c.monthly_income,
    c.credit_score,
    c.risk_level,
    COUNT(l.loan_id) as total_loans,
    SUM(CASE WHEN l.status IN ('Active', 'Approved') THEN 1 ELSE 0 END) as active_loans,
    SUM(CASE WHEN l.status = 'Defaulted' THEN 1 ELSE 0 END) as defaulted_loans,
    MAX(l.due_date) as latest_due_date,
    SUM(CASE WHEN l.status = 'Active' THEN l.outstanding_balance ELSE 0 END) as total_balance,
    c.created_at,
    c.is_active
FROM clients c
LEFT JOIN branches b ON c.branch_id = b.branch_id
LEFT JOIN loans l ON c.client_id = l.client_id
GROUP BY c.client_id, b.branch_id, b.branch_name;

-- Loan Portfolio View
CREATE OR REPLACE VIEW loan_portfolio AS
SELECT 
    l.loan_id,
    l.loan_number,
    c.client_id,
    CONCAT(c.first_name, ' ', c.last_name) as client_name,
    b.branch_name,
    lp.product_name,
    l.amount,
    l.outstanding_balance,
    l.total_amount,
    l.interest_amount,
    l.status,
    l.application_date,
    l.approved_date,
    l.due_date,
    l.installment_amount,
    COUNT(p.payment_id) as total_payments,
    SUM(CASE WHEN p.status = 'Paid' THEN 1 ELSE 0 END) as payments_made,
    SUM(CASE WHEN p.status = 'Paid' THEN p.paid_amount ELSE 0 END) as total_paid,
    SUM(CASE WHEN p.status = 'Overdue' THEN 1 ELSE 0 END) as overdue_payments,
    (l.outstanding_balance / l.amount * 100) as repayment_percentage,
    CASE 
        WHEN l.due_date < CURRENT_DATE AND l.status = 'Active' THEN 'Overdue'
        WHEN l.status = 'Active' THEN 'Current'
        ELSE l.status
    END as loan_health
FROM loans l
JOIN clients c ON l.client_id = c.client_id
LEFT JOIN branches b ON c.branch_id = b.branch_id
LEFT JOIN loan_products lp ON l.product_id = lp.product_id
LEFT JOIN loan_payments p ON l.loan_id = p.loan_id
GROUP BY l.loan_id, c.client_id, b.branch_id, lp.product_id;

-- Payment Performance View
CREATE OR REPLACE VIEW payment_performance AS
SELECT 
    DATE_TRUNC('month', p.paid_date) as payment_month,
    b.branch_id,
    b.branch_name,
    COUNT(DISTINCT p.loan_id) as loans_paid,
    COUNT(p.payment_id) as total_payments,
    SUM(p.paid_amount) as total_amount,
    AVG(p.paid_amount) as average_payment,
    SUM(CASE WHEN p.status = 'Overdue' THEN 1 ELSE 0 END) as overdue_count,
    SUM(CASE WHEN p.status = 'Overdue' THEN p.penalty_amount ELSE 0 END) as total_penalties
FROM loan_payments p
JOIN loans l ON p.loan_id = l.loan_id
JOIN clients c ON l.client_id = c.client_id
JOIN branches b ON c.branch_id = b.branch_id
WHERE p.status = 'Paid'
GROUP BY DATE_TRUNC('month', p.paid_date), b.branch_id, b.branch_name;

-- Dashboard Summary View
CREATE OR REPLACE VIEW dashboard_summary AS
SELECT 
    (SELECT COUNT(*) FROM clients WHERE is_active = true) as total_clients,
    (SELECT COUNT(*) FROM loans WHERE status IN ('Active', 'Approved')) as active_loans,
    (SELECT COUNT(*) FROM loans WHERE status = 'Pending') as pending_loans,
    (SELECT COUNT(*) FROM loans WHERE status = 'Defaulted') as defaulted_loans,
    (SELECT COALESCE(SUM(outstanding_balance), 0) FROM loans WHERE status IN ('Active', 'Approved')) as total_portfolio,
    (SELECT COALESCE(SUM(paid_amount), 0) FROM loan_payments WHERE status = 'Paid' AND paid_date >= CURRENT_DATE - INTERVAL '30 days') as monthly_collections,
    (SELECT COUNT(*) FROM loan_payments WHERE status = 'Overdue') as overdue_payments,
    (SELECT COALESCE(SUM(penalty_amount), 0) FROM loan_payments WHERE status = 'Overdue') as total_penalties,
    (SELECT COUNT(*) FROM employees WHERE is_active = true) as active_staff;

-- ============================================
-- 11. UTILITY FUNCTIONS
-- ============================================

-- Function to generate next loan number
CREATE OR REPLACE FUNCTION generate_loan_number()
RETURNS VARCHAR(20) AS $$
DECLARE
    next_num INTEGER;
    loan_num VARCHAR(20);
BEGIN
    SELECT nextval('loan_number_seq') INTO next_num;
    loan_num := 'LN' || LPAD(next_num::text, 6, '0');
    RETURN loan_num;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate loan schedule
CREATE OR REPLACE FUNCTION generate_loan_schedule(
    p_loan_id INTEGER
)
RETURNS VOID AS $$
DECLARE
    loan_rec loans%ROWTYPE;
    installment_date DATE;
    i INTEGER;
    principal_installment DECIMAL(15,2);
    interest_installment DECIMAL(15,2);
BEGIN
    -- Get loan details
    SELECT * INTO loan_rec FROM loans WHERE loan_id = p_loan_id;
    
    -- Clear existing schedule
    DELETE FROM loan_schedules WHERE loan_id = p_loan_id;
    
    -- Calculate installment dates and amounts
    FOR i IN 1..loan_rec.loan_term LOOP
        -- Calculate due date
        CASE loan_rec.installment_type 
            WHEN 'Weekly' THEN 
                installment_date := loan_rec.disbursement_date + (i * 7);
            WHEN 'Monthly' THEN 
                installment_date := loan_rec.disbursement_date + (i * INTERVAL '1 month');
            WHEN 'Quarterly' THEN 
                installment_date := loan_rec.disbursement_date + (i * INTERVAL '3 months');
            WHEN 'Annually' THEN 
                installment_date := loan_rec.disbursement_date + (i * INTERVAL '1 year');
        END CASE;
        
        -- Calculate installment amounts (simplified)
        IF loan_rec.calculation_method = 'FLAT' THEN
            principal_installment := loan_rec.amount / loan_rec.loan_term;
            interest_installment := (loan_rec.amount * loan_rec.interest_rate / 100) / loan_rec.loan_term;
        ELSE
            -- Reducing balance calculation
            -- Simplified for example
            principal_installment := loan_rec.amount / loan_rec.loan_term;
            interest_installment := (loan_rec.outstanding_balance * loan_rec.interest_rate / 100) / 12;
        END IF;
        
        -- Insert schedule record
        INSERT INTO loan_schedules (loan_id, installment_number, due_date, principal_due, interest_due, total_due)
        VALUES (p_loan_id, i, installment_date, principal_installment, interest_installment, 
                principal_installment + interest_installment);
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- 12. SECURITY & ROW LEVEL SECURITY (RLS)
-- ============================================

-- Enable Row Level Security on all tables
ALTER TABLE employees ENABLE ROW LEVEL SECURITY;
ALTER TABLE clients ENABLE ROW LEVEL SECURITY;
ALTER TABLE branches ENABLE ROW LEVEL SECURITY;
ALTER TABLE loans ENABLE ROW LEVEL SECURITY;
ALTER TABLE loan_payments ENABLE ROW LEVEL SECURITY;
ALTER TABLE payment_receipts ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE system_settings ENABLE ROW LEVEL SECURITY;

-- Basic RLS policies (customize based on your needs)
CREATE POLICY "Employees can view own data" ON employees FOR SELECT USING (employee_id = current_user_id());
CREATE POLICY "Admins can view all clients" ON clients FOR SELECT USING (is_admin());
CREATE POLICY "Branch staff can view branch clients" ON clients FOR SELECT USING (branch_id = current_branch_id());

-- ============================================
-- DATABASE CREATION COMPLETE
-- ============================================

-- Display completion message
DO $$
BEGIN
    RAISE NOTICE 'MS CODEFORGE Loan Management System database created successfully!';
    RAISE NOTICE 'Tables created: %', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public');
    RAISE NOTICE 'Default data inserted successfully';
END $$;