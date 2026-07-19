-- =============================================
-- 2026-07 去掉逻辑删除，全部改为物理删除
-- 删除所有表的 del_flag 字段
-- =============================================

-- mini-bill-system
ALTER TABLE sys_user DROP COLUMN IF EXISTS del_flag;
ALTER TABLE sys_role DROP COLUMN IF EXISTS del_flag;
ALTER TABLE sys_menu DROP COLUMN IF EXISTS del_flag;
ALTER TABLE sys_dict_type DROP COLUMN IF EXISTS del_flag;
ALTER TABLE sys_dict_data DROP COLUMN IF EXISTS del_flag;

-- mini-bill-bus
ALTER TABLE bus_family DROP COLUMN IF EXISTS del_flag;
ALTER TABLE bus_address DROP COLUMN IF EXISTS del_flag;
ALTER TABLE bus_bill DROP COLUMN IF EXISTS del_flag;
ALTER TABLE bus_item DROP COLUMN IF EXISTS del_flag;
ALTER TABLE bus_item_cost DROP COLUMN IF EXISTS del_flag;
ALTER TABLE bus_maintenance DROP COLUMN IF EXISTS del_flag;
ALTER TABLE bus_education DROP COLUMN IF EXISTS del_flag;
ALTER TABLE bus_saving_item DROP COLUMN IF EXISTS del_flag;
ALTER TABLE bus_family_saving DROP COLUMN IF EXISTS del_flag;
