-- 教育费用主表
CREATE TABLE IF NOT EXISTS bus_education (
    id BIGINT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    semester_date DATE NOT NULL,
    tuition DECIMAL(10,2) DEFAULT 0,
    meal_fee DECIMAL(10,2) DEFAULT 0,
    accommodation_fee DECIMAL(10,2) DEFAULT 0,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT 0,
    update_by BIGINT DEFAULT 0,
    del_flag INTEGER DEFAULT 0
);

COMMENT ON TABLE bus_education IS '教育费用表';
COMMENT ON COLUMN bus_education.id IS '主键ID';
COMMENT ON COLUMN bus_education.member_id IS '家庭成员ID';
COMMENT ON COLUMN bus_education.semester_date IS '学期日期';
COMMENT ON COLUMN bus_education.tuition IS '学费';
COMMENT ON COLUMN bus_education.meal_fee IS '伙食费';
COMMENT ON COLUMN bus_education.accommodation_fee IS '住宿费';
COMMENT ON COLUMN bus_education.remark IS '备注';

-- 教育费用明细表
CREATE TABLE IF NOT EXISTS bus_education_item (
    id BIGINT PRIMARY KEY,
    education_id BIGINT NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    cost_date DATE,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    remark VARCHAR(255)
);

ALTER TABLE bus_education_item ADD COLUMN IF NOT EXISTS cost_date DATE;

COMMENT ON TABLE bus_education_item IS '教育费用明细表';
COMMENT ON COLUMN bus_education_item.id IS '主键ID';
COMMENT ON COLUMN bus_education_item.education_id IS '教育费用ID';
COMMENT ON COLUMN bus_education_item.cost_date IS '费用日期';
COMMENT ON COLUMN bus_education_item.item_type IS '费用类型（字典编码 education_item_type）';
COMMENT ON COLUMN bus_education_item.amount IS '金额';
COMMENT ON COLUMN bus_education_item.remark IS '备注';

CREATE INDEX IF NOT EXISTS idx_bus_education_member ON bus_education(member_id);
CREATE INDEX IF NOT EXISTS idx_bus_education_date ON bus_education(semester_date);
CREATE INDEX IF NOT EXISTS idx_bus_education_item_edu ON bus_education_item(education_id);
