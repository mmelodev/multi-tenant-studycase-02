CREATE SEQUENCE IF NOT EXISTS revinfo_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE categories
(
    id          VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    created_by  VARCHAR(255) NOT NULL,
    updated_by  VARCHAR(255),
    deleted     BOOLEAN      NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE products
(
    id              VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITHOUT TIME ZONE,
    created_by      VARCHAR(255) NOT NULL,
    updated_by      VARCHAR(255),
    deleted         BOOLEAN      NOT NULL,
    name            VARCHAR(255) NOT NULL,
    reference       VARCHAR(255) NOT NULL,
    description     TEXT,
    alert_threshold INTEGER      NOT NULL,
    price           DECIMAL      NOT NULL,
    category_id     VARCHAR(255),
    CONSTRAINT pk_products PRIMARY KEY (id)
);

CREATE TABLE revchanges
(
    rev        BIGINT NOT NULL,
    entityname VARCHAR(255)
);

CREATE TABLE revinfo
(
    rev      BIGINT NOT NULL,
    revtstmp BIGINT,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE TABLE stock_mvts
(
    id         VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255),
    deleted    BOOLEAN      NOT NULL,
    type_mvt   VARCHAR(255) NOT NULL,
    quantity   INTEGER      NOT NULL,
    date_mvt   date         NOT NULL,
    comment    TEXT,
    product_id VARCHAR(255),
    CONSTRAINT pk_stock_mvts PRIMARY KEY (id)
);

ALTER TABLE products
    ADD CONSTRAINT uc_products_reference UNIQUE (reference);

ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE stock_mvts
    ADD CONSTRAINT FK_STOCK_MVTS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

ALTER TABLE revchanges
    ADD CONSTRAINT fk_revchanges_on_default_tracking_modified_entities_changelog FOREIGN KEY (rev) REFERENCES revinfo (rev);
