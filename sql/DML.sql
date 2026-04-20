-- DML.sql
-- Sample inserts/updates/deletes for AIS Cafeteria POS schema

PRAGMA foreign_keys = ON;

-- ===== INSERT =====
INSERT INTO staff(staff_id, full_name, role) VALUES
('S001', 'Alex Admin', 'Manager'),
('S002', 'Casey Cashier', 'Staff');

INSERT INTO menu_item(menu_item_id, name, description, price, category, emoji, is_available) VALUES
(1, 'Butter Chicken Rice', 'Creamy butter chicken with rice', 12.00, 'Main', '🍛', 1),
(2, 'Chicken Biryani', 'Spiced rice with chicken', 13.50, 'Main', '🍗', 1),
(3, 'French Fries', 'Crispy fries with seasoning', 4.50, 'Sides', '🍟', 1),
(4, 'Cappuccino', 'Freshly brewed coffee', 4.00, 'Drinks', '☕', 1),
(5, 'Chocolate Muffin', 'Soft muffin with chocolate chips', 3.50, 'Dessert', '🧁', 1);

-- Create an order (Card => Completed by default in your app logic)
INSERT INTO "order"(order_id, order_date, total, payment_method, status, note, staff_id)
VALUES ('ORD-2026-0001', '2026-04-20T10:30:00', 0.00, 'Card', 'Completed', 'No onions, please', 'S002');

-- Add order items (store unit_price snapshot)
INSERT INTO order_item(order_id, menu_item_id, quantity, unit_price, line_total) VALUES
('ORD-2026-0001', 1, 1, 12.00, 12.00),
('ORD-2026-0001', 3, 2,  4.50,  9.00),
('ORD-2026-0001', 4, 1,  4.00,  4.00);

-- Update order total based on order items
UPDATE "order"
SET total = (
    SELECT COALESCE(SUM(line_total), 0.00)
    FROM order_item
    WHERE order_id = "order".order_id
)
WHERE order_id = 'ORD-2026-0001';

-- ===== UPDATE =====
-- Example: update menu price + updated_at
UPDATE menu_item
SET price = 12.50,
    updated_at = datetime('now')
WHERE menu_item_id = 1;

-- Example: mark item unavailable
UPDATE menu_item
SET is_available = 0,
    updated_at = datetime('now')
WHERE menu_item_id = 5;

-- Example: change order status (e.g., cash order later completed)
UPDATE "order"
SET status = 'Completed'
WHERE order_id = 'ORD-2026-0001';

-- ===== DELETE =====
-- Delete an order (cascades to order_item)
DELETE FROM "order"
WHERE order_id = 'ORD-2026-0001';

-- Delete a staff member (staff_id on orders becomes NULL)
DELETE FROM staff
WHERE staff_id = 'S002';

