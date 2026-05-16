-- ============================================================
-- SurakshaSetu — PostgreSQL Sample Data
-- ============================================================

-- Insert sample workers
INSERT INTO workers (digital_work_id, full_name, phone, aadhaar_hash, current_trust_score) VALUES
('W001', 'Rajesh Kumar', '9876543210', 'hash_1', 850.00),
('W002', 'Priya Sharma', '8765432109', 'hash_2', 920.00),
('W003', 'Amit Shah', '7654321098', 'hash_3', 640.00),
('W004', 'Kavya Rao', '6543210987', 'hash_4', 780.00),
('W005', 'Irfan Mohammed', '5432109876', 'hash_5', 810.00);

-- Insert gig platforms
INSERT INTO gig_platforms (platform_name, category, api_endpoint) VALUES
('Swiggy', 'Food Delivery', 'https://api.swiggy.com'),
('Zomato', 'Food Delivery', 'https://api.zomato.com'),
('Uber', 'Ride Hailing', 'https://api.uber.com'),
('Dunzo', 'Logistics', 'https://api.dunzo.com');

-- Insert app users (all passwords are 'password123' except admin)
-- Note: In a real app, these would be generated via PasswordHasher.hash()
INSERT INTO app_users (worker_id, username, password_hash, role) VALUES
(1, 'rajesh_k', '014f447aed44f349', 'Worker'),
(2, 'priya_s', '014f447aed44f349', 'Worker'),
(NULL, 'admin', '0000dc2f78d40e7d', 'Admin');

-- Insert sample work logs
INSERT INTO work_history (worker_id, platform_id, work_date, hours_logged, earnings) VALUES
(1, 1, CURRENT_DATE - INTERVAL '1 day', 8.5, 1200.00),
(1, 2, CURRENT_DATE - INTERVAL '2 days', 4.0, 600.00),
(2, 3, CURRENT_DATE - INTERVAL '1 day', 10.0, 2500.00),
(3, 4, CURRENT_DATE - INTERVAL '3 days', 6.0, 800.00);

-- Insert loan providers
INSERT INTO loan_providers (provider_name, min_trust_score, interest_rate) VALUES
('MicroTrust Finance', 600.00, 12.50),
('GigCredit Bank', 750.00, 10.00),
('Setu Prime Lending', 850.00, 8.50);

-- Insert benefit schemes
INSERT INTO benefit_schemes (scheme_name, coverage_amount, premium_cost) VALUES
('Gig Health Plus', 200000.00, 500.00),
('Accident Insurance', 500000.00, 200.00),
('Family Security Plan', 1000000.00, 1200.00);
