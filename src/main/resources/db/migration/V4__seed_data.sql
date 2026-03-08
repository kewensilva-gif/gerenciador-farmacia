-- =============================================================
-- V4__seed_data.sql — Dados iniciais para testes e desenvolvimento
-- Senhas pré-computadas com BCryptPasswordEncoder (cost=10)
-- admin       → admin123
-- joao.silva  → employee123
-- maria.souza → employee123
-- demais      → customer123
-- =============================================================

-- ---------------------------------------------------------------
-- USUÁRIOS
-- Senhas são hashes BCrypt pré-computados — não exige pgcrypto.
-- A CTE garante que qualquer falha na busca da role surfaceie
-- como erro imediatamente, em vez de inserir com role_id nulo.
-- ---------------------------------------------------------------
WITH r AS (
    SELECT name, uuid AS role_uuid FROM "role"
)
INSERT INTO "user" (uuid, username, email, password, enabled, role_id)
VALUES
    ('a1000000-0000-0000-0000-000000000001',
     'admin', 'admin@farmacia.com',
     '$2a$10$EqykLrcGoI1J7mLXz3MZH.h9AOjtd4zdABH9ybf3/vY9rwM7ZK5vO',
     true,
     (SELECT role_uuid FROM r WHERE name = 'ADMIN')),

    ('a1000000-0000-0000-0000-000000000002',
     'joao.silva', 'joao.silva@farmacia.com',
     '$2a$10$x0Ykus8qjcfwlgCXOpZlKOuMMKYTjxeGCa7hkodnJbmnAyvaYhglO',
     true,
     (SELECT role_uuid FROM r WHERE name = 'EMPLOYEE')),

    ('a1000000-0000-0000-0000-000000000003',
     'maria.souza', 'maria.souza@farmacia.com',
     '$2a$10$x0Ykus8qjcfwlgCXOpZlKOuMMKYTjxeGCa7hkodnJbmnAyvaYhglO',
     true,
     (SELECT role_uuid FROM r WHERE name = 'EMPLOYEE')),

    ('a1000000-0000-0000-0000-000000000004',
     'carlos.pereira', 'carlos.pereira@email.com',
     '$2a$10$y7AUPo35ISUi8I3aKi0veOZR9/vZ.IoNOLMTXgiu3W6XqMhc0qsZa',
     true,
     (SELECT role_uuid FROM r WHERE name = 'CUSTOMER')),

    ('a1000000-0000-0000-0000-000000000005',
     'ana.lima', 'ana.lima@email.com',
     '$2a$10$y7AUPo35ISUi8I3aKi0veOZR9/vZ.IoNOLMTXgiu3W6XqMhc0qsZa',
     true,
     (SELECT role_uuid FROM r WHERE name = 'CUSTOMER')),

    ('a1000000-0000-0000-0000-000000000006',
     'pedro.costa', 'pedro.costa@email.com',
     '$2a$10$y7AUPo35ISUi8I3aKi0veOZR9/vZ.IoNOLMTXgiu3W6XqMhc0qsZa',
     true,
     (SELECT role_uuid FROM r WHERE name = 'CUSTOMER')),

    ('a1000000-0000-0000-0000-000000000007',
     'lucia.fernandes', 'lucia.fernandes@email.com',
     '$2a$10$y7AUPo35ISUi8I3aKi0veOZR9/vZ.IoNOLMTXgiu3W6XqMhc0qsZa',
     false,
     (SELECT role_uuid FROM r WHERE name = 'CUSTOMER'))
ON CONFLICT (username) DO NOTHING;


-- ---------------------------------------------------------------
-- PESSOAS
-- ---------------------------------------------------------------
INSERT INTO person (first_name, last_name, cpf, user_uuid)
VALUES
    ('Administrador', 'Sistema',     '00000000000', 'a1000000-0000-0000-0000-000000000001'),
    ('João',          'Silva',       '11122233344', 'a1000000-0000-0000-0000-000000000002'),
    ('Maria',         'Souza',       '22233344455', 'a1000000-0000-0000-0000-000000000003'),
    ('Carlos',        'Pereira',     '33344455566', 'a1000000-0000-0000-0000-000000000004'),
    ('Ana',           'Lima',        '44455566677', 'a1000000-0000-0000-0000-000000000005'),
    ('Pedro',         'Costa',       '55566677788', 'a1000000-0000-0000-0000-000000000006'),
    ('Lúcia',         'Fernandes',   '66677788899', 'a1000000-0000-0000-0000-000000000007')
ON CONFLICT (cpf) DO NOTHING;


-- ---------------------------------------------------------------
-- FUNCIONÁRIOS
-- ---------------------------------------------------------------
INSERT INTO employee (hiring_date, termination_date, salary, person_id)
VALUES
    -- Admin (também é funcionário para poder registrar vendas)
    ('2022-01-10', NULL,         5500.00, (SELECT id FROM person WHERE cpf = '00000000000')),
    -- Funcionários ativos
    ('2023-03-15', NULL,         3200.00, (SELECT id FROM person WHERE cpf = '11122233344')),
    ('2024-06-01', NULL,         2900.00, (SELECT id FROM person WHERE cpf = '22233344455'))
ON CONFLICT (person_id) DO NOTHING;


-- ---------------------------------------------------------------
-- CLIENTES
-- ---------------------------------------------------------------
INSERT INTO customer (registration_date, person_id)
VALUES
    ('2024-02-20', (SELECT id FROM person WHERE cpf = '33344455566')),
    ('2024-05-11', (SELECT id FROM person WHERE cpf = '44455566677')),
    ('2025-01-08', (SELECT id FROM person WHERE cpf = '55566677788')),
    ('2025-07-30', (SELECT id FROM person WHERE cpf = '66677788899'))
ON CONFLICT (person_id) DO NOTHING;


-- ---------------------------------------------------------------
-- CATEGORIAS
-- ---------------------------------------------------------------
INSERT INTO category (name)
VALUES
    ('Analgésicos'),
    ('Anti-inflamatórios'),
    ('Antibióticos'),
    ('Vitaminas e Suplementos'),
    ('Dermocosméticos'),
    ('Higiene Pessoal'),
    ('Antialérgicos'),
    ('Antifúngicos')
ON CONFLICT (name) DO NOTHING;


-- ---------------------------------------------------------------
-- PRODUTOS
-- ---------------------------------------------------------------
INSERT INTO product (name, barcode, expiration_date, stock_quantity, unit_price, path_image, category_id)
VALUES
    ('Dipirona 500mg 20 cpr',        '7891058012345', '2027-06-30',  150,  8.90,  NULL, (SELECT id FROM category WHERE name = 'Analgésicos')),
    ('Paracetamol 750mg 20 cpr',      '7891058012346', '2027-08-31',  200,  7.50,  NULL, (SELECT id FROM category WHERE name = 'Analgésicos')),
    ('Ibuprofeno 600mg 20 cpr',       '7891058012347', '2026-12-31',  120, 18.90,  NULL, (SELECT id FROM category WHERE name = 'Anti-inflamatórios')),
    ('Nimesulida 100mg 30 cpr',       '7891058012348', '2026-10-15',   80, 22.50,  NULL, (SELECT id FROM category WHERE name = 'Anti-inflamatórios')),
    ('Amoxicilina 500mg 21 cpr',      '7891058012349', '2026-09-30',   60, 35.00,  NULL, (SELECT id FROM category WHERE name = 'Antibióticos')),
    ('Azitromicina 500mg 3 cpr',      '7891058012350', '2027-03-31',   40, 28.90,  NULL, (SELECT id FROM category WHERE name = 'Antibióticos')),
    ('Vitamina C 1g Efervescente 10', '7891058012351', '2028-01-31',  300, 12.50,  NULL, (SELECT id FROM category WHERE name = 'Vitaminas e Suplementos')),
    ('Complexo B 60 cpr',             '7891058012352', '2028-06-30',  250, 19.90,  NULL, (SELECT id FROM category WHERE name = 'Vitaminas e Suplementos')),
    ('Vitamina D3 2000UI 60 cpr',     '7891058012353', '2028-04-30',  180, 34.90,  NULL, (SELECT id FROM category WHERE name = 'Vitaminas e Suplementos')),
    ('Protetor Solar FPS50 50g',      '7891058012354', '2027-11-30',  100, 49.90,  NULL, (SELECT id FROM category WHERE name = 'Dermocosméticos')),
    ('Hidratante Corporal 200ml',     '7891058012355', '2028-02-28',   90, 27.90,  NULL, (SELECT id FROM category WHERE name = 'Dermocosméticos')),
    ('Shampoo Anticaspa 200ml',       '7891058012356', '2027-09-30',  110, 22.00,  NULL, (SELECT id FROM category WHERE name = 'Higiene Pessoal')),
    ('Escova Dental Macia',           '7891058012357', '2029-01-01',  200,  6.90,  NULL, (SELECT id FROM category WHERE name = 'Higiene Pessoal')),
    ('Loratadina 10mg 30 cpr',        '7891058012358', '2027-05-31',   95, 16.80,  NULL, (SELECT id FROM category WHERE name = 'Antialérgicos')),
    ('Cetirizina 10mg 20 cpr',        '7891058012359', '2027-07-31',   70, 14.90,  NULL, (SELECT id FROM category WHERE name = 'Antialérgicos')),
    ('Fluconazol 150mg 1 cpr',        '7891058012360', '2026-11-30',   55, 18.50,  NULL, (SELECT id FROM category WHERE name = 'Antifúngicos'))
ON CONFLICT (barcode) DO NOTHING;


-- ---------------------------------------------------------------
-- VENDAS
-- ---------------------------------------------------------------
INSERT INTO sale (discount, total_price, payment_method, customer_id, employee_id)
VALUES
    (0.00,  32.40, 'PIX',        (SELECT id FROM customer WHERE person_id = (SELECT id FROM person WHERE cpf = '33344455566')),
                                  (SELECT id FROM employee WHERE person_id = (SELECT id FROM person WHERE cpf = '11122233344'))),

    (5.00,  51.80, 'CREDITCARD', (SELECT id FROM customer WHERE person_id = (SELECT id FROM person WHERE cpf = '44455566677')),
                                  (SELECT id FROM employee WHERE person_id = (SELECT id FROM person WHERE cpf = '11122233344'))),

    (0.00,  14.90, 'CASH',       NULL,
                                  (SELECT id FROM employee WHERE person_id = (SELECT id FROM person WHERE cpf = '22233344455'))),

    (10.00, 89.70, 'DEBITCARD',  (SELECT id FROM customer WHERE person_id = (SELECT id FROM person WHERE cpf = '55566677788')),
                                  (SELECT id FROM employee WHERE person_id = (SELECT id FROM person WHERE cpf = '00000000000'))),

    (0.00,  62.70, 'PIX',        (SELECT id FROM customer WHERE person_id = (SELECT id FROM person WHERE cpf = '33344455566')),
                                  (SELECT id FROM employee WHERE person_id = (SELECT id FROM person WHERE cpf = '22233344455')));


-- ---------------------------------------------------------------
-- ITENS DE VENDA
-- ---------------------------------------------------------------
-- Venda 1: Dipirona x2 + Vitamina C x1  → (2*8.90) + 12.50 = 30.30 → c/ desconto 0 = 30.30
INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 2, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 0),
       p.id
FROM product p WHERE p.barcode = '7891058012345';

INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 1, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 0),
       p.id
FROM product p WHERE p.barcode = '7891058012351';

-- Venda 2: Ibuprofeno x1 + Complexo B x1 + Loratadina x1
INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 1, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 1),
       p.id
FROM product p WHERE p.barcode = '7891058012347';

INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 1, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 1),
       p.id
FROM product p WHERE p.barcode = '7891058012352';

INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 1, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 1),
       p.id
FROM product p WHERE p.barcode = '7891058012358';

-- Venda 3: Cetirizina x1 (sem cliente)
INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 1, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 2),
       p.id
FROM product p WHERE p.barcode = '7891058012359';

-- Venda 4: Amoxicilina x1 + Vitamina D3 x2 + Protetor Solar x1
INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 1, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 3),
       p.id
FROM product p WHERE p.barcode = '7891058012349';

INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 2, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 3),
       p.id
FROM product p WHERE p.barcode = '7891058012353';

INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 1, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 3),
       p.id
FROM product p WHERE p.barcode = '7891058012354';

-- Venda 5: Paracetamol x2 + Hidratante x1 + Escova x3
INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 2, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 4),
       p.id
FROM product p WHERE p.barcode = '7891058012346';

INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 1, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 4),
       p.id
FROM product p WHERE p.barcode = '7891058012355';

INSERT INTO sale_product (quantity, unit_price, sale_id, product_id)
SELECT 3, p.unit_price,
       (SELECT id FROM sale ORDER BY id LIMIT 1 OFFSET 4),
       p.id
FROM product p WHERE p.barcode = '7891058012357';
