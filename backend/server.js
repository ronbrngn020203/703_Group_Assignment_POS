// ─────────────────────────────────────────────────────────────
//  AIS Cafeteria POS  ·  Middle-Tier REST API
//  Stack: Node.js + Express + sql.js (pure-JS SQLite)
//  Exposes: GET /api/menu, GET /api/menu/search, PUT /api/menu/:id
//
//  sql.js is used instead of better-sqlite3 because it is pure
//  JavaScript — no C++ compilation needed, works on any Node version.
// ─────────────────────────────────────────────────────────────
const express   = require('express');
const cors      = require('cors');
const initSqlJs = require('sql.js');
const path      = require('path');
const fs        = require('fs');

const app    = express();
const PORT   = 3000;
const DB_PATH = path.join(__dirname, 'cafeteria.db');

app.use(cors());
app.use(express.json());

// ── Database Setup ────────────────────────────────────────────
let db;

async function initDatabase() {
    const SQL = await initSqlJs();

    if (fs.existsSync(DB_PATH)) {
        const fileBuffer = fs.readFileSync(DB_PATH);
        db = new SQL.Database(fileBuffer);
        console.log('✅  Loaded existing cafeteria.db');
    } else {
        db = new SQL.Database();
        console.log('✅  Created new cafeteria.db');
    }

    db.run(`
        CREATE TABLE IF NOT EXISTS menu_items (
            id          INTEGER PRIMARY KEY,
            name        TEXT    NOT NULL,
            description TEXT,
            price       REAL    NOT NULL,
            category    TEXT,
            emoji       TEXT
        )
    `);

    const result = db.exec('SELECT COUNT(*) AS cnt FROM menu_items');
    const count  = result[0]?.values[0][0] ?? 0;

    if (count === 0) {
        const seedData = [
            [1,  'Butter Chicken Rice',  'Creamy butter chicken with steamed rice',          12.00, 'Mains',    '🍛'],
            [2,  'Chicken Rice Bowl',    'Grilled chicken, steamed rice, seasonal veggies',   8.50, 'Mains',    '🍱'],
            [3,  'Beef Burger',          '100% beef patty, lettuce, tomato, cheese',          9.00, 'Mains',    '🍔'],
            [4,  'Veggie Wrap',          'Fresh vegetables, hummus, whole wheat wrap',         7.50, 'Mains',    '🌯'],
            [5,  'Fish & Chips',         'Battered fish fillet, golden chips, tartar sauce', 10.00, 'Mains',    '🐟'],
            [6,  'Caesar Salad',         'Romaine, croutons, parmesan, Caesar dressing',      7.00, 'Salads',   '🥗'],
            [7,  'Garden Salad',         'Mixed greens, cherry tomatoes, cucumber',           6.00, 'Salads',   '🥬'],
            [8,  'Cheese Pizza Slice',   'Mozzarella, tomato sauce, fresh basil',             5.00, 'Snacks',   '🍕'],
            [9,  'Spring Rolls (3pc)',   'Crispy fried rolls with sweet chilli sauce',        5.50, 'Snacks',   '🥟'],
            [10, 'Latte',               'Double shot espresso with steamed milk',             4.50, 'Drinks',   '☕'],
            [11, 'Smoothie',            'Mixed berry, banana, yoghurt blend',                 5.50, 'Drinks',   '🥤'],
            [12, 'Water Bottle',        '500ml chilled mineral water',                        2.00, 'Drinks',   '💧'],
            [13, 'Chocolate Muffin',    'Rich double chocolate muffin',                       3.50, 'Desserts', '🧁'],
            [14, 'Fruit Cup',           'Seasonal fresh fruit medley',                        4.00, 'Desserts', '🍓'],
        ];
        const stmt = db.prepare(
            'INSERT INTO menu_items (id, name, description, price, category, emoji) VALUES (?,?,?,?,?,?)'
        );
        for (const row of seedData) stmt.run(row);
        stmt.free();
        saveDb();
        console.log('✅  Database seeded with 14 menu items.');
    }
}

function saveDb() {
    const data = db.export();
    fs.writeFileSync(DB_PATH, Buffer.from(data));
}

function queryAll(sql, params = []) {
    const stmt = db.prepare(sql);
    stmt.bind(params);
    const rows = [];
    while (stmt.step()) rows.push(stmt.getAsObject());
    stmt.free();
    return rows;
}

function queryOne(sql, params = []) {
    const rows = queryAll(sql, params);
    return rows.length > 0 ? rows[0] : null;
}

// ── Routes ────────────────────────────────────────────────────

app.get('/api/menu', (req, res) => {
    try {
        const items = queryAll('SELECT * FROM menu_items ORDER BY id');
        res.json({ success: true, data: items });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Failed to fetch menu items.' });
    }
});

app.get('/api/menu/search', (req, res) => {
    try {
        const q     = `%${req.query.q || ''}%`;
        const items = queryAll(
            'SELECT * FROM menu_items WHERE name LIKE ? OR description LIKE ? OR category LIKE ? ORDER BY id',
            [q, q, q]
        );
        res.json({ success: true, data: items, query: req.query.q || '' });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Search failed.' });
    }
});

app.put('/api/menu/:id', (req, res) => {
    try {
        const id       = parseInt(req.params.id, 10);
        const existing = queryOne('SELECT * FROM menu_items WHERE id = ?', [id]);
        if (!existing) {
            return res.status(404).json({ success: false, message: `Menu item #${id} not found.` });
        }
        const { name, description, price, category, emoji } = req.body;
        if (!name || !name.trim()) {
            return res.status(400).json({ success: false, message: 'Name is required.' });
        }
        if (price == null || isNaN(Number(price)) || Number(price) < 0) {
            return res.status(400).json({ success: false, message: 'Valid price is required.' });
        }
        db.run(
            'UPDATE menu_items SET name=?, description=?, price=?, category=?, emoji=? WHERE id=?',
            [name.trim(), (description||'').trim(), Number(price),
             (category||existing.category).trim(), (emoji||existing.emoji).trim(), id]
        );
        saveDb();
        const updated = queryOne('SELECT * FROM menu_items WHERE id = ?', [id]);
        res.json({ success: true, message: 'Menu item updated successfully.', data: updated });
    } catch (err) {
        res.status(500).json({ success: false, message: 'Update failed.' });
    }
});

app.get('/api/health', (req, res) => {
    res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// ── Start ─────────────────────────────────────────────────────
initDatabase().then(() => {
    app.listen(PORT, () => {
        console.log(`\n🚀  AIS Cafeteria API running on http://localhost:${PORT}`);
        console.log(`   GET  /api/menu            → Fetch all menu items`);
        console.log(`   GET  /api/menu/search?q=  → Search menu items`);
        console.log(`   PUT  /api/menu/:id        → Update a menu item\n`);
    });
}).catch(err => {
    console.error('Failed to initialise database:', err);
    process.exit(1);
});