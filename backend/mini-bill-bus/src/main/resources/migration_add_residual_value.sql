-- 迁移：为 bus_item 表增加残值字段
-- 执行前请确认数据库连接信息

ALTER TABLE bus_item ADD COLUMN IF NOT EXISTS residual_value DECIMAL(10,2);

COMMENT ON COLUMN bus_item.residual_value IS '残值（报废/二手回收金额）';
