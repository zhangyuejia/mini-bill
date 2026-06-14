-- 维护费用表
CREATE TABLE IF NOT EXISTS bus_maintenance (
    id BIGINT PRIMARY KEY,
    address_id BIGINT NOT NULL,
    type VARCHAR(50),
    cost_date DATE NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

ALTER TABLE bus_maintenance ADD COLUMN IF NOT EXISTS type VARCHAR(50);

COMMENT ON TABLE bus_maintenance IS '维护费用表';
COMMENT ON COLUMN bus_maintenance.id IS '主键ID';
COMMENT ON COLUMN bus_maintenance.address_id IS '住址ID';
COMMENT ON COLUMN bus_maintenance.type IS '维护类型（字典编码 maintenance_type）';
COMMENT ON COLUMN bus_maintenance.cost_date IS '费用日期';
COMMENT ON COLUMN bus_maintenance.cost IS '费用金额';
COMMENT ON COLUMN bus_maintenance.remark IS '备注';

CREATE INDEX IF NOT EXISTS idx_bus_maintenance_address ON bus_maintenance(address_id);
CREATE INDEX IF NOT EXISTS idx_bus_maintenance_date ON bus_maintenance(cost_date);
