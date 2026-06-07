-- ========================
-- Drops
-- ========================
DROP TABLE IF EXISTS payment_transaction CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS studio CASCADE;
DROP TABLE IF EXISTS discipline CASCADE;
DROP TABLE IF EXISTS instructor CASCADE;
DROP TABLE IF EXISTS client CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS file_processing_job CASCADE;
DROP TABLE IF EXISTS excel_column_mapping CASCADE;

-- ========================
-- Client Table
-- ========================
CREATE TABLE client (
    client_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) ,
    email          VARCHAR(150) UNIQUE NOT NULL,
    phone          VARCHAR(50),
    document_id    VARCHAR(50),
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- ========================
-- Studio Table
-- ========================
CREATE TABLE studio (
    studio_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) UNIQUE NOT NULL,
    country        VARCHAR(100),
    city           VARCHAR(100),
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- ========================
-- Room Table
-- ========================
CREATE TABLE room (
    room_id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    studio_id      BIGINT NOT NULL,
    name           VARCHAR(100) UNIQUE NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_room_studio FOREIGN KEY (studio_id) REFERENCES studio(studio_id) ON DELETE CASCADE
);

-- ========================
-- Discipline Table
-- ========================
CREATE TABLE discipline (
    discipline_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(100) UNIQUE NOT NULL
);

-- ========================
-- Instructor Table
-- ========================
CREATE TABLE instructor (
    instructor_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name           VARCHAR(150) UNIQUE NOT NULL
);

-- ========================
-- Reservation Table
-- ========================
CREATE TABLE reservation (
    reservation_id   BIGINT PRIMARY KEY,
    class_id         BIGINT NOT NULL,
    room_id          BIGINT NOT NULL,
    discipline_id    BIGINT NOT NULL,
    instructor_id    BIGINT NOT NULL,
    client_id        BIGINT NOT NULL,
    reservation_date DATE NOT NULL,
    reservation_time TIME NOT NULL,
    order_creator    VARCHAR(150),
    payment_method   VARCHAR(100),
    status           VARCHAR(50),
    created_at       TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_reservation_room       FOREIGN KEY (room_id) REFERENCES room(room_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_discipline FOREIGN KEY (discipline_id) REFERENCES discipline(discipline_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_instructor FOREIGN KEY (instructor_id) REFERENCES instructor(instructor_id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_client     FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
);

-- ========================
-- Payment Transaction Table
-- ========================
CREATE TABLE payment_transaction (
    operation_id       BIGINT PRIMARY KEY,
    client_id          BIGINT NOT NULL,
    month              INT,
    day                INT,
    week               INT,
    purchase_date      TIMESTAMP,
    accreditation_date TIMESTAMP,
    release_date       TIMESTAMP,
    status             VARCHAR(100),
    operation_type     VARCHAR(100),
    product_value      NUMERIC(12,2),
    transaction_fee    NUMERIC(12,2),
    amount_received    NUMERIC(12,2),
    installments       INT,
    payment_method     VARCHAR(100),
    package            VARCHAR(100),
    class_count        INT,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT now(),
    CONSTRAINT fk_payment_client FOREIGN KEY (client_id) REFERENCES client(client_id) ON DELETE CASCADE
);

-- ========================
-- Process file audit Table
-- ========================

CREATE TABLE file_processing_job (
    job_id            BIGSERIAL PRIMARY KEY,
    file_name         VARCHAR(255) NOT NULL,
    file_extension    VARCHAR(20),
    file_type         VARCHAR(50) NOT NULL,
    status            VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    error_message     TEXT,
    created_at        TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at        TIMESTAMP WITH TIME ZONE,
    finished_at       TIMESTAMP WITH TIME ZONE,
    total_records     INT,
    processed_records INT,
    skipped_records   INT,
    error_records 	  INT,
    processing_result TEXT
);
CREATE INDEX idx_file_processing_status ON file_processing_job(status);

-- ========================
-- Excel Column Mapping Table
-- ========================
CREATE TABLE excel_column_mapping (
    mapping_id      BIGSERIAL PRIMARY KEY,
    file_type       VARCHAR(50) NOT NULL,
    field_name      VARCHAR(100) NOT NULL,
    excel_header    VARCHAR(255) NOT NULL,
    required        BOOLEAN DEFAULT FALSE,
    data_type       VARCHAR(50) DEFAULT 'STRING',
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(file_type, field_name)
);

-- Add indexes
CREATE INDEX idx_excel_mapping_file_type ON excel_column_mapping(file_type);

-- ========================
-- User Table
-- ========================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ========================
-- Roles Table
-- ========================

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ========================
-- Permissions Table
-- ========================

CREATE TABLE permissions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(500),
    resource VARCHAR(50),
    action VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- ========================
-- User Roles Table
-- ========================

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ========================
-- Role permissions Table
-- ========================

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);


-- ========================
-- Indexes
-- ========================
CREATE INDEX idx_room_studio             ON room(studio_id);
CREATE INDEX idx_reservation_room        ON reservation(room_id);
CREATE INDEX idx_reservation_client      ON reservation(client_id);
CREATE INDEX idx_reservation_discipline  ON reservation(discipline_id);
CREATE INDEX idx_payment_client          ON payment_transaction(client_id);
CREATE INDEX idx_payment_method          ON payment_transaction(payment_method);

-- ========================
-- Functions
-- ========================

CREATE OR REPLACE FUNCTION public.get_reservations_time_series(
    group_by text[],
    time_unit text,
    from_date date,
    to_date date
)
RETURNS TABLE(
    group_name text,
    period_start date,
    period_end date,
    total bigint
)
LANGUAGE sql
AS $function$
WITH groups AS (
    SELECT r.*, rm.*, s.*, i.*, d.*,
           string_agg(
               CASE
                   WHEN gb = 'studio'     THEN s.name
                   WHEN gb = 'instructor' THEN i.name
                   WHEN gb = 'discipline' THEN d.name
               END,
               ' / '
               ORDER BY gb
           ) AS group_name
    FROM reservation r
    LEFT JOIN room rm      ON r.room_id = rm.room_id
    LEFT JOIN studio s     ON rm.studio_id = s.studio_id
    LEFT JOIN instructor i ON r.instructor_id = i.instructor_id
    LEFT JOIN discipline d ON r.discipline_id = d.discipline_id
    CROSS JOIN unnest(group_by) AS gb
    WHERE r.reservation_date BETWEEN from_date AND to_date
    GROUP BY r.reservation_id, rm.room_id, s.studio_id, i.instructor_id, d.discipline_id
),
agg AS (
    SELECT
        group_name,
        (CASE
            WHEN time_unit = 'day'   THEN reservation_date
            WHEN time_unit = 'week'  THEN date_trunc('week', reservation_date)::date
            WHEN time_unit = 'month' THEN date_trunc('month', reservation_date)::date
        END) AS period_start,
        (CASE
            WHEN time_unit = 'day'   THEN reservation_date
            WHEN time_unit = 'week'  THEN (date_trunc('week', reservation_date) + interval '6 days')::date
            WHEN time_unit = 'month' THEN (date_trunc('month', reservation_date) + interval '1 month - 1 day')::date
        END) AS period_end,
        COUNT(*) AS total
    FROM groups
    GROUP BY group_name, period_start, period_end
)
SELECT * FROM agg
ORDER BY group_name, period_start;
$function$;


CREATE OR REPLACE FUNCTION get_clients_reservations_payments(
    start_date DATE,
    end_date DATE,
    client_filter TEXT DEFAULT NULL
)
RETURNS TABLE (
    client_id BIGINT,
    client_name VARCHAR,
    client_email VARCHAR,
    client_phone VARCHAR,
    total_reservations INT,
    total_payments INT,
    total_amount_received NUMERIC(12,2),
    last_payment_date TIMESTAMP,
    last_reservation_date DATE,
    top_discipline VARCHAR
)
LANGUAGE sql
AS $$
    WITH top_discipline_per_client AS (
        SELECT DISTINCT ON (r.client_id)
            r.client_id,
            d.name AS top_discipline,
            COUNT(r.reservation_id) AS top_discipline_count
        FROM reservation r
        JOIN discipline d ON d.discipline_id = r.discipline_id
        WHERE r.reservation_date BETWEEN start_date AND end_date
        GROUP BY r.client_id, d.name
        ORDER BY r.client_id, COUNT(r.reservation_id) DESC
    )
    SELECT
        c.client_id,
        c.name AS client_name,
        c.email AS client_email,
		c.phone AS client_phone,
        COUNT(DISTINCT r.reservation_id) AS total_reservations,
        COUNT(DISTINCT p.operation_id) AS total_payments,
        COALESCE(SUM(p.amount_received), 0) AS total_amount_received,
        MAX(p.purchase_date) AS last_payment_date,
        MAX(r.reservation_date) AS last_reservation_date,
        td.top_discipline
    FROM client c
    LEFT JOIN reservation r
        ON c.client_id = r.client_id
        AND r.reservation_date BETWEEN start_date AND end_date
    LEFT JOIN payment_transaction p
        ON c.client_id = p.client_id
        AND p.purchase_date BETWEEN start_date AND end_date
    LEFT JOIN top_discipline_per_client td
        ON c.client_id = td.client_id
    WHERE client_filter IS NULL
	OR LOWER(c.name) LIKE LOWER(CONCAT('%', client_filter, '%'))
	OR LOWER(c.email) LIKE LOWER(CONCAT('%', client_filter, '%'))
    GROUP BY c.client_id, c.name, c.email, td.top_discipline, td.top_discipline_count
    ORDER BY c.name;
$$;



-- ========================
-- Add start items
-- ========================

-- Create basic permissions
INSERT INTO permissions (name, description, resource, action) VALUES
('MANAGE_USER', 'Can create, update, delete and view users', 'user', 'manage'),
('MANAGE_ROLES', 'Can manage roles', 'role', 'manage'),
('MANAGE_PERMISSIONS', 'Can manage permissions', 'permission', 'manage'),
('FILE_UPLOAD', 'Can upload data files', 'file', 'upload'),
('REPORT_VIEW', 'Can view reports', 'report', 'view');

-- Create basic roles
INSERT INTO roles (name, description) VALUES
('ADMIN', 'Administrator with full access'),
('USER', 'Standard user with limited access');

-- Assign permissions to role: ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN';

-- Assign permissions to role: USER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'USER'
AND p.name IN ('FILE_UPLOAD', 'REPORT_VIEW');

-- Create default admin user (password: admin123)
-- Password hashed using bcrypt
INSERT INTO users (username, email, password, first_name, last_name) VALUES
('admin', 'admin@example.com', '$2b$12$Ldt7cLW42R2ngBe63ZhqJOUpULoTsd0rpl8tOhXU.0qP9x3HEfoLW', 'Admin', 'User');

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';


-- Add initial mappings for payment transactions
INSERT INTO excel_column_mapping (file_type, field_name, excel_header, required, data_type) VALUES
('PAYMENT', 'month', 'Mes', true, 'INTEGER'),
('PAYMENT', 'day', 'dia', true, 'INTEGER'),
('PAYMENT', 'week', 'semana', false, 'INTEGER'),
('PAYMENT', 'purchaseDate', 'Fecha de compra (date_created)', true, 'DATE'),
('PAYMENT', 'accreditationDate', 'Fecha de acreditación (date_approved)', true, 'DATE'),
('PAYMENT', 'releaseDate', 'Fecha de liberación del dinero (date_released)', false, 'DATE'),
('PAYMENT', 'clientEmail', 'E-mail de la contraparte (counterpart_email)', true, 'STRING'),
('PAYMENT', 'phone', 'Teléfono de la contraparte (counterpart_phone_number)', false, 'STRING'),
('PAYMENT', 'documentId', 'Documento de la contraparte (buyer_document)', false, 'STRING'),
('PAYMENT', 'operationId', 'Número de operación de Mercado Pago (operation_id)', true, 'LONG'),
('PAYMENT', 'status', 'Estado de la operación (status)', true, 'STRING'),
('PAYMENT', 'operationType', 'Tipo de operación (operation_type)', true, 'STRING'),
('PAYMENT', 'productValue', 'Valor del producto (transaction_amount)', true, 'DECIMAL'),
('PAYMENT', 'transactionFee', 'Tarifa de Mercado Pago (mercadopago_fee)', true, 'DECIMAL'),
('PAYMENT', 'amountReceived', 'Monto recibido (net_received_amount)', true, 'DECIMAL'),
('PAYMENT', 'installments', 'Cuotas (installments)', false, 'INTEGER'),
('PAYMENT', 'paymentMethod', 'Medio de pago (payment_type)', true, 'STRING'),
('PAYMENT', 'packageName', 'paquete', false, 'STRING'),
('PAYMENT', 'classCount', 'N° de clases', false, 'INTEGER');


INSERT INTO excel_column_mapping (file_type, field_name, excel_header, required, data_type) VALUES
('RESERVATION', 'reservationId', 'ID reserva', true, 'BIGINT'),
('RESERVATION', 'classId', 'ID clase', true, 'BIGINT'),
('RESERVATION', 'country', 'País', true, 'STRING'),
('RESERVATION', 'city', 'Ciudad', true, 'STRING'),
('RESERVATION', 'disciplineName', 'Disciplina', true, 'STRING'),
('RESERVATION', 'studioName', 'Estudio', true, 'STRING'),
('RESERVATION', 'roomName', 'Salon', true, 'STRING'),
('RESERVATION', 'instructorName', 'Instructor', true, 'STRING'),
('RESERVATION', 'day', 'Día', true, 'DATE'),
('RESERVATION', 'time', 'Hora', true, 'TIME'),
('RESERVATION', 'clientEmail', 'Cliente', true, 'STRING'),
('RESERVATION', 'orderCreator', 'Creador del Pedido', true, 'STRING'),
('RESERVATION', 'paymentMethod', 'Método de Pago', true, 'STRING'),
('RESERVATION', 'status', 'Estatus', true, 'STRING');