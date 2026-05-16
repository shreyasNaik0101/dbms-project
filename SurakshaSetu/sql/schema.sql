-- ============================================================
-- SurakshaSetu — PostgreSQL Schema (Normalized 15-Table Design)
-- ============================================================

-- Drop existing tables in reverse order of dependencies
DROP TABLE IF EXISTS app_users CASCADE;
DROP TABLE IF EXISTS scheme_enrollments CASCADE;
DROP TABLE IF EXISTS benefit_schemes CASCADE;
DROP TABLE IF EXISTS loan_applications CASCADE;
DROP TABLE IF EXISTS loan_providers CASCADE;
DROP TABLE IF EXISTS monthly_earnings_summary CASCADE;
DROP TABLE IF EXISTS operational_expenses CASCADE;
DROP TABLE IF EXISTS trust_score_audit CASCADE;
DROP TABLE IF EXISTS performance_ratings CASCADE;
DROP TABLE IF EXISTS work_history CASCADE;
DROP TABLE IF EXISTS gig_platforms CASCADE;
DROP TABLE IF EXISTS kyc_documents CASCADE;
DROP TABLE IF EXISTS worker_skills CASCADE;
DROP TABLE IF EXISTS skills_master CASCADE;
DROP TABLE IF EXISTS workers CASCADE;

-- 1. workers
CREATE TABLE workers (
    worker_id SERIAL PRIMARY KEY,
    digital_work_id VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    aadhaar_hash VARCHAR(128),
    current_trust_score DECIMAL(5,2) DEFAULT 0.00,
    joining_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. skills_master
CREATE TABLE skills_master (
    skill_id SERIAL PRIMARY KEY,
    skill_name VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(50)
);

-- 3. worker_skills (M:N mapping)
CREATE TABLE worker_skills (
    worker_id INT REFERENCES workers(worker_id),
    skill_id INT REFERENCES skills_master(skill_id),
    years_experience DECIMAL(4,2),
    PRIMARY KEY (worker_id, skill_id)
);

-- 4. kyc_documents
CREATE TABLE kyc_documents (
    doc_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    document_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'Pending',
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. gig_platforms
CREATE TABLE gig_platforms (
    platform_id SERIAL PRIMARY KEY,
    platform_name VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(50),
    api_endpoint VARCHAR(255)
);

-- 6. work_history
CREATE TABLE work_history (
    work_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    platform_id INT REFERENCES gig_platforms(platform_id),
    work_date DATE NOT NULL,
    hours_logged DECIMAL(4,2),
    earnings DECIMAL(10,2),
    completion_date TIMESTAMP
);

-- 7. performance_ratings
CREATE TABLE performance_ratings (
    rating_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    platform_id INT REFERENCES gig_platforms(platform_id),
    avg_rating DECIMAL(3,2),
    total_reviews INT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. trust_score_audit
CREATE TABLE trust_score_audit (
    audit_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    old_score DECIMAL(5,2),
    new_score DECIMAL(5,2),
    reason TEXT,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. operational_expenses
CREATE TABLE operational_expenses (
    expense_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    expense_type VARCHAR(50),
    amount DECIMAL(10,2),
    expense_date DATE DEFAULT CURRENT_DATE
);

-- 10. monthly_earnings_summary
CREATE TABLE monthly_earnings_summary (
    summary_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    month_year VARCHAR(7), -- YYYY-MM
    total_gross DECIMAL(12,2),
    total_expenses DECIMAL(12,2),
    net_savings DECIMAL(12,2)
);

-- 11. loan_providers
CREATE TABLE loan_providers (
    provider_id SERIAL PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL,
    min_trust_score DECIMAL(5,2),
    interest_rate DECIMAL(4,2)
);

-- 12. loan_applications
CREATE TABLE loan_applications (
    loan_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    provider_id INT REFERENCES loan_providers(provider_id),
    loan_amount DECIMAL(12,2),
    status VARCHAR(20) DEFAULT 'Pending',
    applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. benefit_schemes
CREATE TABLE benefit_schemes (
    scheme_id SERIAL PRIMARY KEY,
    scheme_name VARCHAR(100) NOT NULL,
    coverage_amount DECIMAL(12,2),
    premium_cost DECIMAL(10,2)
);

-- 14. scheme_enrollments
CREATE TABLE scheme_enrollments (
    enrollment_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    scheme_id INT REFERENCES benefit_schemes(scheme_id),
    enrollment_date DATE DEFAULT CURRENT_DATE
);

-- 15. app_users
CREATE TABLE app_users (
    user_id SERIAL PRIMARY KEY,
    worker_id INT REFERENCES workers(worker_id),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'Worker'
);

-- Create some indexes for performance
CREATE INDEX idx_worker_id ON work_history(worker_id);
CREATE INDEX idx_trust_worker ON trust_score_audit(worker_id);
