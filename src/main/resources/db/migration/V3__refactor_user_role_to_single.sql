-- Adiciona coluna role_id à tabela user
ALTER TABLE "user" ADD COLUMN role_id uuid;

-- Adiciona constraint de FK
ALTER TABLE "user" 
ADD CONSTRAINT FK_user_role 
FOREIGN KEY (role_id) REFERENCES role(uuid);

-- Remove a tabela user_role (ManyToMany antigo)
DROP TABLE IF EXISTS user_role;

-- Define a role padrão como CUSTOMER para usuários existentes
UPDATE "user" u 
SET role_id = (SELECT uuid FROM role WHERE name = 'CUSTOMER' LIMIT 1)
WHERE role_id IS NULL;

-- Marca a coluna como NOT NULL
ALTER TABLE "user" ALTER COLUMN role_id SET NOT NULL;
