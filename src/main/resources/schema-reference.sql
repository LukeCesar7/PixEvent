-- Schema de referência para PostgreSQL — equivalente às migrations do Sequelize original.
-- Você NÃO precisa rodar isso manualmente: com spring.jpa.hibernate.ddl-auto=update (padrão
-- no application.yml), o Hibernate cria/ajusta essas tabelas sozinho ao subir a aplicação.
-- Use este arquivo como referência, ou rode manualmente se preferir ddl-auto=validate em produção.

CREATE TABLE IF NOT EXISTS mesas (
    numero        VARCHAR(10) PRIMARY KEY,
    status        VARCHAR(20) NOT NULL DEFAULT 'disponivel',
    pedido_id     VARCHAR(24),
    reservado_em  TIMESTAMPTZ,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS pedidos (
    id               VARCHAR(24) PRIMARY KEY,
    cpf              VARCHAR(11) NOT NULL,
    nome             VARCHAR(255) NOT NULL,
    email            VARCHAR(255) DEFAULT '',
    telefone         VARCHAR(20) NOT NULL,
    produto          VARCHAR(20) NOT NULL,
    quantidade       INTEGER NOT NULL DEFAULT 1,
    mesa_numero      VARCHAR(64),
    valor_total      NUMERIC(10, 2) NOT NULL,
    metodo_pgto      VARCHAR(20) NOT NULL DEFAULT 'pix',
    status           VARCHAR(20) NOT NULL DEFAULT 'pendente',
    mp_payment_id    VARCHAR(255),
    qrcode_token     VARCHAR(40) UNIQUE,
    qrcode_usado     BOOLEAN NOT NULL DEFAULT false,
    qrcode_usado_em  TIMESTAMPTZ,
    itens_json       TEXT,
    pago_em          TIMESTAMPTZ,
    criado_em        TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS pedido_mesas (
    pedido_id     VARCHAR(24) NOT NULL REFERENCES pedidos(id),
    mesa_numero   VARCHAR(10) NOT NULL REFERENCES mesas(numero),
    reservado_em  TIMESTAMPTZ NOT NULL DEFAULT now(),
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (pedido_id, mesa_numero)
);

CREATE TABLE IF NOT EXISTS rifas (
    id            SERIAL PRIMARY KEY,
    pedido_id     VARCHAR(24) NOT NULL,
    numero        INTEGER NOT NULL UNIQUE,
    cpf           VARCHAR(11) NOT NULL,
    nome          VARCHAR(255) NOT NULL,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS webhooks_log (
    id            SERIAL PRIMARY KEY,
    payload       TEXT,
    criado_em     TIMESTAMPTZ NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT now()
);
