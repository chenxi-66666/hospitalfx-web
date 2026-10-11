-- Repeatable demo seed data for local development.
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE tb_ai_inference_record;
TRUNCATE TABLE tb_medication_image_record;
TRUNCATE TABLE tb_knowledge_document;
TRUNCATE TABLE tb_medication_knowledge;
TRUNCATE TABLE tb_medication_checkin;
TRUNCATE TABLE tb_medication_plan;
TRUNCATE TABLE tb_medication_conflict;
TRUNCATE TABLE tb_medication_inventory;
TRUNCATE TABLE tb_consult_message;
TRUNCATE TABLE tb_registinfo;
TRUNCATE TABLE tb_patient_profile;
TRUNCATE TABLE tb_user;
TRUNCATE TABLE tb_doctor;

-- 用户数据
INSERT INTO tb_user (id, username, login_code, password, real_name, role_code, enabled, phone_number, doctor_id) VALUES
(1, 'admin', 'A001', '123456', '系统管理员', 'ADMIN', 1, NULL, NULL),
(4, 'pharmacy_zhou', 'Y001', '123456', '周建华', 'PHARMACIST', 1, NULL, NULL),
(5, 'pharmacy_sun', 'Y002', '123456', '孙宝国', 'PHARMACIST', 1, NULL, NULL),
(6, 'doctor_zhang', 'K001', '123456', '张三', 'DOCTOR', 1, NULL, 'K001'),
(7, 'doctor_li', 'K002', '123456', '李四', 'DOCTOR', 1, NULL, 'K002'),
(8, 'doctor_wang', 'K003', '123456', '王五', 'DOCTOR', 1, NULL, 'K003'),
(9, 'doctor_zhao', 'K004', '123456', '赵六', 'DOCTOR', 1, NULL, 'K004'),
(10, 'doctor_liu', 'K005', '123456', '刘七', 'DOCTOR', 1, NULL, 'K005'),
(11, 'doctor_chen', 'K006', '123456', '陈八', 'DOCTOR', 1, NULL, 'K006'),
(12, 'patient_liu', NULL, '123456', '刘小明', 'PATIENT', 1, '13800000012', NULL),
(13, 'patient_wang', NULL, '123456', '王小芳', 'PATIENT', 1, '13800000013', NULL),
(14, 'patient_zhao', NULL, '123456', '赵小强', 'PATIENT', 1, '13800000014', NULL),
(15, 'patient_sun', NULL, '123456', '孙大伟', 'PATIENT', 1, '13800000015', NULL),
(16, 'patient_chen', NULL, '123456', '陈小丽', 'PATIENT', 1, '13800000016', NULL),
(17, 'patient_zhou', NULL, '123456', '周子琪', 'PATIENT', 1, '13800000017', NULL);

-- 医生数据
INSERT INTO tb_doctor (id, realname, password, dept_name, regist_level, registfee) VALUES
('K001', '张三', '123456', '内科', '普通号', 15.00),
('K002', '李四', '123456', '外科', '专家号', 35.00),
('K003', '王五', '123456', '儿科', '普通号', 20.00),
('K004', '赵六', '123456', '男科', '专家号', 40.00),
('K005', '刘七', '123456', '皮肤科', '普通号', 18.00),
('K006', '陈八', '123456', '耳鼻喉科', '专家号', 30.00);

-- 患者档案
INSERT INTO tb_patient_profile (user_id, patient_name, gender, card_number, birthdate, age, home_address, phone_number) VALUES
(12, '刘小明', '男', '110101199001010011', '1990-01-01', 36, '北京市朝阳区西二环', '13800000012'),
(13, '王小芳', '女', '110101199202023322', '1992-02-02', 34, '北京市海淀区清华园', '13800000013'),
(14, '赵小强', '男', '110101198803038899', '1988-03-03', 38, '北京市丰台区科技园区', '13800000014'),
(15, '孙大伟', '男', '110101199511115678', '1995-11-11', 30, '北京市通州区环球影城', '13800000015'),
(16, '陈小丽', '女', '110101199408087654', '1994-08-08', 31, '北京市东城区天安门', '13800000016'),
(17, '周子琪', '男', '110101199709096543', '1997-09-09', 28, '北京市石景山区苹果园', '13800000017');

-- 挂号记录
INSERT INTO tb_registinfo (
  id, patient_user_id, realname, gender, card_number, birthdate, age, home_address,
  dept_name, doctor_name, doctor_id, regist_level, is_book, registfee, regist_date,
  diagiosis, prescrption, drug_price, visit_state, purchase_type
) VALUES
(1, 12, '刘小明', '男', '110101199001010011', '1990-01-01', 36, '北京市朝阳区西二环', '内科', '张三', 'K001', '普通号', '否', 15.00, '2026-03-24', NULL, NULL, 0.00, 1, 0),
(2, 13, '王小芳', '女', '110101199202023322', '1992-02-02', 34, '北京市海淀区清华园', '外科', '李四', 'K002', '专家号', '是', 36.00, '2026-03-24', '腹部软组织挫伤', '活血化瘀治疗', 48.50, 3, 0),
(3, 14, '赵小强', '男', '110101198803038899', '1988-03-03', 38, '北京市丰台区科技园区', '儿科', '王五', 'K003', '普通号', '否', 20.00, '2026-03-24', '轻度感冒', '对症治疗', 32.00, 2, 1),
(4, 15, '孙大伟', '男', '110101199511115678', '1995-11-11', 30, '北京市通州区环球影城', '男科', '赵六', 'K004', '专家号', '否', 40.00, '2026-03-24', NULL, NULL, 0.00, 1, 0),
(5, 16, '陈小丽', '女', '110101199408087654', '1994-08-08', 31, '北京市东城区天安门', '皮肤科', '刘七', 'K005', '普通号', '否', 18.00, '2026-03-24', '过敏性皮炎', '抗过敏治疗', 26.00, 2, 0),
(6, 17, '周子琪', '男', '110101199709096543', '1997-09-09', 28, '北京市石景山区苹果园', '耳鼻喉科', '陈八', 'K006', '专家号', '是', 31.00, '2026-03-24', '慢性咽炎', '药物治疗', 35.00, 3, 0);

-- 药品库存
INSERT INTO tb_medication_inventory (
  id, medication_code, medication_name, specification, unit, unit_price, stock_quantity, safe_stock, usage_notes
) VALUES
(1, 'M001', '布洛芬缓释胶囊', '0.3g*20粒', '盒', 28.50, 42, 12, '餐后服用'),
(2, 'M002', '阿莫西林胶囊', '0.25g*24粒', '盒', 22.00, 58, 15, '青霉素过敏者禁用'),
(3, 'M003', '氯雷他定片', '10mg*12片', '盒', 19.80, 24, 10, '可能出现轻度嗜睡'),
(4, 'M004', '复方感冒灵颗粒', '10g*9袋', '盒', 17.50, 18, 8, '避免重复使用'),
(5, 'M005', '阿司匹林肠溶片', '100mg*30片', '盒', 16.20, 14, 10, '胃溃疡患者慎用'),
(6, 'M006', '奥美拉唑肠溶胶囊', '20mg*14粒', '盒', 25.60, 30, 8, '晨起空腹服用');

-- 药品知识
INSERT INTO tb_medication_knowledge (
  id, medication_inventory_id, medication_code, medication_name, generic_name,
  indications, contraindications, adverse_reactions,
  source_name, source_url, version_label, review_status, created_at, updated_at
) VALUES
(1, 1, 'M001', '布洛芬缓释胶囊', '布洛芬', '用于缓解轻至中度疼痛，也用于普通感冒或流行性感冒引起的发热。', '对布洛芬或其他非甾体抗炎药过敏者禁用；活动性消化道溃疡患者禁用。', '可能出现恶心、胃痛、头晕或皮疹。', '国家药品监督管理局药品说明书', 'https://www.nmpa.gov.cn/', '2026-01', 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(2, 2, 'M002', '阿莫西林胶囊', '阿莫西林', '用于敏感菌所致的呼吸道、泌尿道、皮肤软组织等感染。', '对青霉素类药物过敏者禁用。', '可能出现恶心、腹泻、皮疹等。', '国家药品监督管理局药品说明书', 'https://www.nmpa.gov.cn/', '2026-01', 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(3, 3, 'M003', '氯雷他定片', '氯雷他定', '用于缓解过敏性鼻炎、荨麻疹等过敏症状。', '对本品成分过敏者禁用。', '可能出现头痛、嗜睡、口干等。', '国家药品监督管理局药品说明书', 'https://www.nmpa.gov.cn/', '2026-01', 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(4, 4, 'M004', '复方感冒灵颗粒', '复方感冒灵', '用于感冒引起的头痛、发热、鼻塞、流涕、咽痛等症状。', '严重肝肾功能不全者禁用；服药期间不得饮酒。', '可能引起困倦、口干、胃部不适。', '国家药品监督管理局药品说明书', 'https://www.nmpa.gov.cn/', '2026-01', 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(5, 5, 'M005', '阿司匹林肠溶片', '阿司匹林', '用于降低心肌梗死、脑卒中等心血管事件风险，也可用于解热镇痛。', '活动性溃疡、出血体质或对水杨酸类过敏者禁用。', '可能增加胃肠道刺激和出血风险。', '国家药品监督管理局药品说明书', 'https://www.nmpa.gov.cn/', '2026-01', 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(6, 6, 'M006', '奥美拉唑肠溶胶囊', '奥美拉唑', '用于胃溃疡、十二指肠溃疡、反流性食管炎等酸相关疾病。', '对本品成分过敏者禁用。', '可能出现头痛、腹泻、恶心等。', '国家药品监督管理局药品说明书', 'https://www.nmpa.gov.cn/', '2026-01', 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

-- 知识库文档
INSERT INTO tb_knowledge_document (
  id, title, content, source_name, source_url, version_label, review_status, created_at, updated_at
) VALUES
(1, '阿莫西林胶囊说明书', '适用于敏感菌所致的呼吸道、泌尿道等感染；对青霉素过敏者禁用，用药前需确认过敏史。', '国家药品监督管理局', 'https://www.nmpa.gov.cn/', '2026-01', 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(2, '布洛芬与阿司匹林同服的注意点', '两药同服可能增加胃肠道刺激与出血风险，建议在医生指导下调整用药方案。', '国家药品监督管理局', 'https://www.nmpa.gov.cn/', '2026-01', 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00'),
(3, '服药依从性提示', '漏服后不要自行加倍补服，应按说明书或医嘱处理，并及时咨询药师。', NULL, NULL, NULL, 'PENDING', '2026-01-01 00:00:00', '2026-01-01 00:00:00');

-- 图片识别与模型推理审计示例
INSERT INTO tb_medication_image_record (
  id, uploaded_by_user_id, image_hash, original_filename, content_type,
  medicine_name, specification, manufacturer, expiry_date, confidence, model_version,
  warnings, confirmation_status, created_at, updated_at
) VALUES
(1, 13, '7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069', 'demo-medicine.jpg', 'image/jpeg',
 NULL, NULL, NULL, NULL, 0.0000, 'mock-0.1',
 '["当前为联调占位结果，请接入真实识别模型。"]', 'PENDING', '2026-04-20 09:00:00', NULL);

INSERT INTO tb_ai_inference_record (
  id, model_type, input_summary, output_summary, model_version, confidence, duration_ms, created_at
) VALUES
(1, 'MEDICINE_IMAGE', 'imageHash=7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069',
 '{"medicineName":null,"specification":null,"manufacturer":null,"expiryDate":null,"confidence":0.0,"modelVersion":"mock-0.1"}',
 'mock-0.1', 0.0000, 15, '2026-04-20 09:00:00');

-- 药品冲突
INSERT INTO tb_medication_conflict (
  id, left_medication_name, right_medication_name, severity, guidance
) VALUES
(1, '布洛芬缓释胶囊', '阿司匹林肠溶片', '中风险', '可能增加胃肠道刺激'),
(2, '氯雷他定片', '复方感冒灵颗粒', '中风险', '存在成分叠加风险');

-- 用药计划
INSERT INTO tb_medication_plan (
  patient_user_id, registration_id, medication_inventory_id, medication_name, dosage,
  quantity, frequency_code, frequency_label, frequency_per_day, unit, instructions,
  status, next_reminder_at, last_checked_in_at
) VALUES
(13, 2, 1, '布洛芬缓释胶囊', '1 粒 / 次', 2, 'BID', '每日 2 次', 2, '盒', '饭后服用', 'ACTIVE', '2026-04-20 20:00:00', '2026-04-20 08:10:00'),
(13, 2, 5, '阿司匹林肠溶片', '1 片 / 次', 1, 'QD', '每日 1 次', 1, '盒', '早餐后服用', 'ACTIVE', '2026-04-21 08:00:00', '2026-04-20 08:10:00'),
(17, 6, 3, '氯雷他定片', '1 片 / 次', 1, 'HS', '睡前 1 次', 1, '盒', '睡前服用', 'ACTIVE', '2026-04-20 22:00:00', '2026-04-19 22:10:00');

-- 用药打卡
INSERT INTO tb_medication_checkin (
  id, plan_id, patient_user_id, medication_name, status, note, checked_in_at
) VALUES
(1, 1, 13, '布洛芬缓释胶囊', 'TAKEN', '患者已完成晨间服药', '2026-04-20 08:10:00'),
(2, 2, 13, '阿司匹林肠溶片', 'TAKEN', '患者已完成晨间服药', '2026-04-20 08:10:00'),
(3, 3, 17, '氯雷他定片', 'TAKEN', '昨晚已服用', '2026-04-19 22:10:00');

-- 咨询消息
INSERT INTO tb_consult_message (
  id, patient_user_id, patient_name, doctor_user_id, doctor_name, symptom_summary,
  patient_message, doctor_reply, status, created_at, replied_at
) VALUES
(1, 12, '刘小明', 6, '张三', '发热、低烧 2 天', '医生你好，我最近低烧并且发热，需要尽快来院检查吗？', '建议尽快到院就诊，如出现高烧、呼吸困难或持续高热请直接前往急诊。', 'REPLIED', '2026-03-24 08:30:00', '2026-03-24 09:10:00'),
(2, 13, '王小芳', 7, '李四', '右手腕疼痛 1 周', '最近拧毛巾时手腕疼痛，是否需要先拍片再来问诊？', '建议先来问诊检查，必要时再安排拍片检查，这两天内就诊最合适。', 'REPLIED', '2026-03-24 10:15:00', '2026-03-24 11:00:00');

SET FOREIGN_KEY_CHECKS = 1;
