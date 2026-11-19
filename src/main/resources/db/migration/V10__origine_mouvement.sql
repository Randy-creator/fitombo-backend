ALTER TABLE mouvement ADD COLUMN exploitation_origine_id UUID REFERENCES exploitation (id);
