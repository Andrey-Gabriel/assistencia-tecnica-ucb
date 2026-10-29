-- Cria o banco e as seis tabelas. Executar uma vez em banco novo.
CREATE DATABASE IF NOT EXISTS assistencia_tecnica_completa
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE assistencia_tecnica_completa;
SET NAMES utf8mb4;

-- clientes identificados por chave própria; documento opcional e único.
CREATE TABLE cliente (
    id_cliente INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    documento VARCHAR(14) NULL COMMENT 'CPF/CNPJ sem pontuação; dígitos verificadores validados no Java',
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NULL,
    endereco VARCHAR(255) NULL,
    data_cadastro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_documento UNIQUE (documento),
    CONSTRAINT ck_cliente_nome CHECK (CHAR_LENGTH(TRIM(nome)) >= 3),
    CONSTRAINT ck_cliente_telefone CHECK (CHAR_LENGTH(TRIM(telefone)) >= 8),
    CONSTRAINT ck_cliente_documento CHECK (
        documento IS NULL OR (CHAR_LENGTH(documento) IN (11, 14) AND documento NOT REGEXP '[^0-9]')
    ),
    CONSTRAINT ck_cliente_email CHECK (email IS NULL OR CHAR_LENGTH(TRIM(email)) > 0)
) ENGINE=InnoDB COMMENT='cadastro dos proprietários dos equipamentos';
CREATE INDEX idx_cliente_nome ON cliente (nome);

-- técnico inativo permanece no histórico, mas não recebe novas OS.
CREATE TABLE tecnico (
    id_tecnico INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(150) NULL,
    especialidade VARCHAR(100) NOT NULL,
    ativo TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT pk_tecnico PRIMARY KEY (id_tecnico),
    CONSTRAINT ck_tecnico_nome CHECK (CHAR_LENGTH(TRIM(nome)) >= 3),
    CONSTRAINT ck_tecnico_telefone CHECK (CHAR_LENGTH(TRIM(telefone)) >= 8),
    CONSTRAINT ck_tecnico_especialidade CHECK (CHAR_LENGTH(TRIM(especialidade)) > 0),
    CONSTRAINT ck_tecnico_ativo CHECK (ativo IN (0, 1)),
    CONSTRAINT ck_tecnico_email CHECK (email IS NULL OR CHAR_LENGTH(TRIM(email)) > 0)
) ENGINE=InnoDB COMMENT='profissionais responsáveis pelos atendimentos';
CREATE INDEX idx_tecnico_nome ON tecnico (nome);

-- equipamento pertence a um cliente; marca+série não se repetem.
CREATE TABLE equipamento (
    id_equipamento INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_cliente INT UNSIGNED NOT NULL,
    tipo VARCHAR(60) NOT NULL,
    marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    numero_serie VARCHAR(100) NULL,
    observacoes TEXT NULL,
    CONSTRAINT pk_equipamento PRIMARY KEY (id_equipamento),
    CONSTRAINT uq_equipamento_marca_serie UNIQUE (marca, numero_serie),
    CONSTRAINT fk_equipamento_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_equipamento_tipo CHECK (CHAR_LENGTH(TRIM(tipo)) > 0),
    CONSTRAINT ck_equipamento_marca CHECK (CHAR_LENGTH(TRIM(marca)) > 0),
    CONSTRAINT ck_equipamento_modelo CHECK (CHAR_LENGTH(TRIM(modelo)) > 0),
    CONSTRAINT ck_equipamento_serie CHECK (numero_serie IS NULL OR CHAR_LENGTH(TRIM(numero_serie)) > 0)
) ENGINE=InnoDB COMMENT='aparelhos e proprietário; NULL indica série desconhecida';
CREATE INDEX idx_equipamento_cliente ON equipamento (id_cliente);

-- preço de referência não substitui os valores negociados em cada OS.
CREATE TABLE servico (
    id_servico INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT NULL,
    valor_referencia DECIMAL(10,2) NOT NULL,
    prazo_estimado_dias SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    ativo TINYINT NOT NULL DEFAULT 1,
    CONSTRAINT pk_servico PRIMARY KEY (id_servico),
    CONSTRAINT uq_servico_nome UNIQUE (nome),
    CONSTRAINT ck_servico_nome CHECK (CHAR_LENGTH(TRIM(nome)) > 0),
    CONSTRAINT ck_servico_valor CHECK (valor_referencia >= 0),
    CONSTRAINT ck_servico_prazo CHECK (prazo_estimado_dias > 0),
    CONSTRAINT ck_servico_ativo CHECK (ativo IN (0, 1))
) ENGINE=InnoDB COMMENT='catálogo de serviços e preço sugerido';

-- uma OS contém um equipamento, um técnico e um serviço principal.
-- Não há itens de peças, vários serviços por OS ou controle de pagamento.
CREATE TABLE ordem_servico (
    id_ordem_servico INT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_equipamento INT UNSIGNED NOT NULL,
    id_tecnico INT UNSIGNED NOT NULL,
    id_servico INT UNSIGNED NOT NULL,
    problema_relatado TEXT NOT NULL,
    diagnostico TEXT NULL,
    solucao_aplicada TEXT NULL,
    valor_orcado DECIMAL(10,2) NULL,
    valor_final DECIMAL(10,2) NULL,
    status ENUM('ABERTA','EM_ANALISE','AGUARDANDO_APROVACAO','EM_MANUTENCAO',
                'CONCLUIDA','ENTREGUE','CANCELADA') NOT NULL DEFAULT 'ABERTA',
    data_abertura DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_previsao DATE NULL,
    data_aprovacao DATETIME NULL,
    data_conclusao DATETIME NULL,
    data_entrega DATETIME NULL,
    data_cancelamento DATETIME NULL,
    motivo_cancelamento VARCHAR(255) NULL,
    data_status DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao_status VARCHAR(255) NULL,
    CONSTRAINT pk_ordem_servico PRIMARY KEY (id_ordem_servico),
    CONSTRAINT fk_os_equipamento FOREIGN KEY (id_equipamento)
        REFERENCES equipamento (id_equipamento) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_os_tecnico FOREIGN KEY (id_tecnico)
        REFERENCES tecnico (id_tecnico) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_os_servico FOREIGN KEY (id_servico)
        REFERENCES servico (id_servico) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_os_problema CHECK (CHAR_LENGTH(TRIM(problema_relatado)) > 0),
    CONSTRAINT ck_os_valor_orcado CHECK (valor_orcado IS NULL OR valor_orcado >= 0),
    CONSTRAINT ck_os_valor_final CHECK (valor_final IS NULL OR valor_final >= 0),
    CONSTRAINT ck_os_status CHECK (status IN ('ABERTA','EM_ANALISE','AGUARDANDO_APROVACAO',
        'EM_MANUTENCAO','CONCLUIDA','ENTREGUE','CANCELADA')),
    CONSTRAINT ck_os_previsao CHECK (data_previsao IS NULL OR data_previsao >= DATE(data_abertura)),
    CONSTRAINT ck_os_data_status CHECK (data_status >= data_abertura),
    CONSTRAINT ck_os_orcamento CHECK (
        status NOT IN ('AGUARDANDO_APROVACAO','EM_MANUTENCAO','CONCLUIDA','ENTREGUE') OR
        (diagnostico IS NOT NULL AND CHAR_LENGTH(TRIM(diagnostico)) > 0 AND valor_orcado IS NOT NULL)
    ),
    CONSTRAINT ck_os_aprovacao CHECK (
        status NOT IN ('EM_MANUTENCAO','CONCLUIDA','ENTREGUE') OR data_aprovacao IS NOT NULL
    ),
    CONSTRAINT ck_os_data_aprovacao CHECK (data_aprovacao IS NULL OR data_aprovacao >= data_abertura),
    CONSTRAINT ck_os_conclusao CHECK (
        status NOT IN ('CONCLUIDA','ENTREGUE') OR
        (data_conclusao IS NOT NULL AND valor_final IS NOT NULL AND
         solucao_aplicada IS NOT NULL AND CHAR_LENGTH(TRIM(solucao_aplicada)) > 0)
    ),
    CONSTRAINT ck_os_data_conclusao CHECK (data_conclusao IS NULL OR
        (data_aprovacao IS NOT NULL AND data_conclusao >= data_aprovacao)),
    CONSTRAINT ck_os_status_conclusao CHECK (data_conclusao IS NULL OR status IN ('CONCLUIDA','ENTREGUE')),
    CONSTRAINT ck_os_entrega CHECK (status <> 'ENTREGUE' OR data_entrega IS NOT NULL),
    CONSTRAINT ck_os_data_entrega CHECK (data_entrega IS NULL OR
        (data_conclusao IS NOT NULL AND data_entrega >= data_conclusao)),
    CONSTRAINT ck_os_status_entrega CHECK (data_entrega IS NULL OR status = 'ENTREGUE'),
    CONSTRAINT ck_os_cancelamento CHECK (
        (status = 'CANCELADA' AND data_cancelamento IS NOT NULL AND
         motivo_cancelamento IS NOT NULL AND CHAR_LENGTH(TRIM(motivo_cancelamento)) >= 5)
        OR (status <> 'CANCELADA' AND data_cancelamento IS NULL AND motivo_cancelamento IS NULL)
    ),
    CONSTRAINT ck_os_data_cancelamento CHECK (data_cancelamento IS NULL OR
        (data_cancelamento >= data_abertura AND
         (data_aprovacao IS NULL OR data_cancelamento >= data_aprovacao)))
) ENGINE=InnoDB COMMENT='rotina principal de atendimento e conserto';
CREATE INDEX idx_os_equipamento ON ordem_servico (id_equipamento);
CREATE INDEX idx_os_tecnico_abertura ON ordem_servico (id_tecnico, data_abertura);
CREATE INDEX idx_os_servico ON ordem_servico (id_servico);
CREATE INDEX idx_os_abertura ON ordem_servico (data_abertura);
CREATE INDEX idx_os_status_previsao ON ordem_servico (status, data_previsao);

-- trilha automática das situações; não é um sexto cadastro de tela.
CREATE TABLE ordem_servico_historico (
    id_historico BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    id_ordem_servico INT UNSIGNED NOT NULL,
    status_anterior VARCHAR(30) NULL,
    status_novo VARCHAR(30) NOT NULL,
    data_evento DATETIME NOT NULL,
    observacao VARCHAR(255) NULL,
    CONSTRAINT pk_os_historico PRIMARY KEY (id_historico),
    CONSTRAINT fk_historico_os FOREIGN KEY (id_ordem_servico)
        REFERENCES ordem_servico (id_ordem_servico) ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT ck_historico_anterior CHECK (status_anterior IS NULL OR status_anterior IN
        ('ABERTA','EM_ANALISE','AGUARDANDO_APROVACAO','EM_MANUTENCAO','CONCLUIDA','ENTREGUE','CANCELADA')),
    CONSTRAINT ck_historico_novo CHECK (status_novo IN
        ('ABERTA','EM_ANALISE','AGUARDANDO_APROVACAO','EM_MANUTENCAO','CONCLUIDA','ENTREGUE','CANCELADA'))
) ENGINE=InnoDB COMMENT='eventos gerados pelas triggers da OS, sem inserção manual na aplicação';
CREATE INDEX idx_historico_os_evento ON ordem_servico_historico (id_ordem_servico, data_evento, id_historico);

