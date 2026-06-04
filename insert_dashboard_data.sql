-- Insert Mock Data for Dashboards Page
-- Note: Replace INTO ensures we don't hit duplicate key errors if ran multiple times.

-- 1. Restaurant
REPLACE INTO restaurants (id, name, location, opening_hours, contact_info) VALUES 
('rest-mock-001', 'Bistro SuChef', '123 Culinary Ave, Paris', '08:00 - 23:00', 'contact@bistrosuchef.com');

-- 2. User
REPLACE INTO users (id, email, name, password, restaurant_id) VALUES 
('user-mock-001', 'admin@bistrosuchef.com', 'Chef Admin', '$2a$10$C.kM5o..', 'rest-mock-001');

-- 3. Menu Items
REPLACE INTO menu_items (id, name, description, price, category, restaurant_id) VALUES 
('menu-001', 'Truffle Pasta', 'Creamy truffle linguine', 24.50, 'Mains', 'rest-mock-001'),
('menu-002', 'Wagyu Steak', 'A5 Wagyu with asparagus', 85.00, 'Mains', 'rest-mock-001'),
('menu-003', 'Caesar Salad', 'Classic caesar', 12.00, 'Starters', 'rest-mock-001'),
('menu-004', 'Tiramisu', 'Italian coffee dessert', 8.50, 'Desserts', 'rest-mock-001'),
('menu-005', 'Lemonade', 'Fresh squeezed lemonade', 4.00, 'Drinks', 'rest-mock-001');

-- 4. Inventory Items (linked to User 'user-mock-001')
REPLACE INTO inventory_items (id, name, current_stock, unit, unit_price, min_threshold, category, user_id) VALUES 
('inv-001', 'Truffle Oil', 12.5, 'Liters', 45.00, 2.0, 'Pantry', 'user-mock-001'),
('inv-002', 'Wagyu Beef', 15.0, 'kg', 120.00, 5.0, 'Meat', 'user-mock-001'),
('inv-003', 'Romaine Lettuce', 30.0, 'heads', 1.50, 10.0, 'Produce', 'user-mock-001'),
('inv-004', 'Mascarpone', 8.0, 'kg', 12.00, 2.0, 'Dairy', 'user-mock-001');

-- 5. Orders (Historical data for charting)
-- Let's put some dates scattered across the last month
REPLACE INTO orders (id, order_date, total_amount, status, restaurant_id) VALUES 
('ord-001', '2026-03-01 19:30:00', 109.50, 'COMPLETED', 'rest-mock-001'),
('ord-002', '2026-03-05 20:15:00', 85.00, 'COMPLETED', 'rest-mock-001'),
('ord-003', '2026-03-10 18:45:00', 205.00, 'COMPLETED', 'rest-mock-001'),
('ord-004', '2026-03-15 19:00:00', 36.50, 'COMPLETED', 'rest-mock-001'),
('ord-005', '2026-03-20 21:00:00', 121.50, 'COMPLETED', 'rest-mock-001'),
('ord-006', '2026-03-22 13:30:00', 44.00, 'COMPLETED', 'rest-mock-001'),
('ord-007', '2026-03-25 19:45:00', 455.00, 'COMPLETED', 'rest-mock-001'); -- This will be the "Best Order"

-- 6. Order Items
REPLACE INTO order_items (id, order_id, menu_item_id, quantity, price) VALUES 
('oi-001', 'ord-001', 'menu-001', 1, 24.50),
('oi-002', 'ord-001', 'menu-002', 1, 85.00),
('oi-003', 'ord-002', 'menu-002', 1, 85.00),
('oi-004', 'ord-003', 'menu-002', 2, 170.00),
('oi-005', 'ord-003', 'menu-001', 1, 24.50),
('oi-006', 'ord-003', 'menu-004', 1, 8.50),
('oi-007', 'ord-004', 'menu-003', 2, 24.00),
('oi-008', 'ord-004', 'menu-004', 1, 8.50),
('oi-009', 'ord-004', 'menu-005', 1, 4.00),
('oi-010', 'ord-005', 'menu-002', 1, 85.00),
('oi-011', 'ord-005', 'menu-001', 1, 24.50),
('oi-012', 'ord-005', 'menu-003', 1, 12.00),
('oi-013', 'ord-006', 'menu-001', 1, 24.50),
('oi-014', 'ord-006', 'menu-003', 1, 12.00),
('oi-015', 'ord-006', 'menu-005', 2, 8.00),
('oi-016', 'ord-007', 'menu-002', 5, 425.00),
('oi-017', 'ord-007', 'menu-004', 3, 25.50),
('oi-018', 'ord-007', 'menu-005', 1, 4.00);

-- 7. Stock Transactions (For Waste Reduction, Cost Opt)
REPLACE INTO stock_transactions (id, item_id, quantity_change, type, document_id, timestamp) VALUES 
('stx-001', 'inv-001', -0.5, 'USAGE', NULL, '2026-03-01 19:00:00'),
('stx-002', 'inv-002', -1.0, 'USAGE', NULL, '2026-03-05 20:00:00'),
('stx-003', 'inv-003', -0.2, 'WASTE', NULL, '2026-03-10 22:00:00'),
('stx-004', 'inv-002', 10.0, 'PURCHASE', NULL, '2026-03-11 10:00:00'),
('stx-005', 'inv-004', -0.5, 'WASTE', NULL, '2026-03-15 23:00:00'),
('stx-006', 'inv-002', -2.0, 'USAGE', NULL, '2026-03-20 20:30:00'),
('stx-007', 'inv-002', -5.0, 'USAGE', NULL, '2026-03-25 19:30:00');

