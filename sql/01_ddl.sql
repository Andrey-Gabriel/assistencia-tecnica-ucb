-- 01_ddl.sql - Definição do banco de Assistência Técnica.
-- Cria tabelas, índices, views, triggers e procedures em um banco novo.
-- MySQL 8.0.16 ou superior. Execute uma vez, antes de 02_dml.sql.
-- Banco já instalado: estes arquivos não são uma migração e não devem ser reexecutados.

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

USE assistencia_tecnica_completa;
CREATE VIEW vw_ordens_servico AS
SELECT os.id_ordem_servico AS numero_os,
    c.id_cliente, c.nome AS cliente, c.telefone AS telefone_cliente,
    e.id_equipamento, CONCAT(e.tipo, ' - ', e.marca, ' ', e.modelo) AS equipamento,
    e.numero_serie, t.id_tecnico, t.nome AS tecnico,
    s.id_servico, s.nome AS servico, os.problema_relatado, os.diagnostico,
    os.solucao_aplicada, os.status, os.valor_orcado, os.valor_final,
    os.data_abertura, os.data_previsao, os.data_aprovacao,
    os.data_conclusao, os.data_entrega, os.data_cancelamento, os.motivo_cancelamento,
    os.data_status
FROM ordem_servico os
JOIN equipamento e ON e.id_equipamento = os.id_equipamento
JOIN cliente c ON c.id_cliente = e.id_cliente
JOIN tecnico t ON t.id_tecnico = os.id_tecnico
JOIN servico s ON s.id_servico = os.id_servico;

CREATE VIEW vw_os_pendentes AS
SELECT numero_os, cliente, equipamento, tecnico, status, data_abertura, data_previsao,
    CASE WHEN data_previsao IS NOT NULL AND data_previsao < CURRENT_DATE
         THEN DATEDIFF(CURRENT_DATE, data_previsao) ELSE 0 END AS dias_atraso
FROM vw_ordens_servico
WHERE status IN ('ABERTA','EM_ANALISE','AGUARDANDO_APROVACAO','EM_MANUTENCAO');

USE assistencia_tecnica_completa;
DELIMITER $$
CREATE TRIGGER trg_equipamento_bu
BEFORE UPDATE ON equipamento FOR EACH ROW
BEGIN
    IF NEW.id_equipamento <> OLD.id_equipamento THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'identificador do equipamento nao pode ser alterado.';
    END IF;
    IF NEW.id_cliente <> OLD.id_cliente AND EXISTS (
        SELECT 1 FROM ordem_servico WHERE id_equipamento = OLD.id_equipamento
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'equipamento com OS nao pode trocar de proprietario.';
    END IF;
END$$

-- toda OS começa ABERTA, vinculada a técnico e serviço ativos.
CREATE TRIGGER trg_os_bi
BEFORE INSERT ON ordem_servico FOR EACH ROW
BEGIN
    DECLARE v_ativo TINYINT;
    DECLARE v_cliente INT UNSIGNED;
    IF NEW.status <> 'ABERTA' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'uma nova OS deve iniciar como ABERTA.';
    END IF;
    -- Leitura com bloqueio compartilha a proteção com atualizações do proprietário.
    SELECT id_cliente INTO v_cliente FROM equipamento
        WHERE id_equipamento = NEW.id_equipamento FOR SHARE;
    SET v_ativo = NULL;
    SELECT ativo INTO v_ativo FROM tecnico WHERE id_tecnico = NEW.id_tecnico FOR SHARE;
    IF v_ativo = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'tecnico inativo nao recebe nova OS.';
    END IF;
    SET v_ativo = NULL;
    SELECT ativo INTO v_ativo FROM servico WHERE id_servico = NEW.id_servico FOR SHARE;
    IF v_ativo = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'servico inativo nao pode ser escolhido.';
    END IF;
    SET NEW.data_status = NEW.data_abertura;
    IF NEW.data_aprovacao IS NOT NULL OR NEW.data_conclusao IS NOT NULL OR
       NEW.data_entrega IS NOT NULL OR NEW.data_cancelamento IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'datas de andamento sao preenchidas ao mudar a situacao.';
    END IF;
END$$

CREATE TRIGGER trg_os_bu
BEFORE UPDATE ON ordem_servico FOR EACH ROW
BEGIN
    DECLARE v_ativo TINYINT;
    IF NEW.id_ordem_servico <> OLD.id_ordem_servico OR NEW.data_abertura <> OLD.data_abertura THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'numero e data de abertura da OS sao imutaveis.';
    END IF;
    IF NEW.id_equipamento <> OLD.id_equipamento THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'equipamento da OS nao pode ser substituido.';
    END IF;
    IF OLD.status IN ('ENTREGUE','CANCELADA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'OS entregue ou cancelada nao pode ser alterada.';
    END IF;
    IF OLD.data_aprovacao IS NOT NULL AND (
        NOT (NEW.valor_orcado <=> OLD.valor_orcado) OR NEW.id_servico <> OLD.id_servico
    ) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'servico e orcamento aprovados nao podem ser alterados.';
    END IF;
    IF NEW.id_tecnico <> OLD.id_tecnico THEN
        SET v_ativo = NULL;
        SELECT ativo INTO v_ativo FROM tecnico WHERE id_tecnico = NEW.id_tecnico FOR SHARE;
        IF v_ativo = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'nao e permitido atribuir tecnico inativo.';
        END IF;
    END IF;
    IF NEW.id_servico <> OLD.id_servico THEN
        SET v_ativo = NULL;
        SELECT ativo INTO v_ativo FROM servico WHERE id_servico = NEW.id_servico FOR SHARE;
        IF v_ativo = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'nao e permitido atribuir servico inativo.';
        END IF;
    END IF;
    IF NOT (NEW.data_aprovacao <=> OLD.data_aprovacao) OR
       NOT (NEW.data_conclusao <=> OLD.data_conclusao) OR
       NOT (NEW.data_entrega <=> OLD.data_entrega) OR
       NOT (NEW.data_cancelamento <=> OLD.data_cancelamento) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'altere a situacao; as datas do fluxo sao automaticas.';
    END IF;
    IF NEW.status <> OLD.status THEN
        IF NOT (
            (OLD.status = 'ABERTA' AND NEW.status IN ('EM_ANALISE','CANCELADA')) OR
            (OLD.status = 'EM_ANALISE' AND NEW.status IN ('AGUARDANDO_APROVACAO','CANCELADA')) OR
            (OLD.status = 'AGUARDANDO_APROVACAO' AND NEW.status IN ('EM_MANUTENCAO','CANCELADA')) OR
            (OLD.status = 'EM_MANUTENCAO' AND NEW.status IN ('CONCLUIDA','CANCELADA')) OR
            (OLD.status = 'CONCLUIDA' AND NEW.status = 'ENTREGUE')
        ) THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'transicao de situacao nao permitida.';
        END IF;
        IF NEW.data_status = OLD.data_status THEN
            SET NEW.data_status = CURRENT_TIMESTAMP;
        END IF;
        IF NEW.data_status < OLD.data_status THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'evento nao pode ser anterior ao evento precedente.';
        END IF;
        IF NEW.status = 'EM_MANUTENCAO' THEN
            IF NEW.observacao_status IS NULL OR CHAR_LENGTH(TRIM(NEW.observacao_status)) < 5 THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'registre como o cliente aprovou o orcamento.';
            END IF;
            SET NEW.data_aprovacao = NEW.data_status;
        ELSEIF NEW.status = 'CONCLUIDA' THEN
            SET NEW.data_conclusao = NEW.data_status;
        ELSEIF NEW.status = 'ENTREGUE' THEN
            SET NEW.data_entrega = NEW.data_status;
        ELSEIF NEW.status = 'CANCELADA' THEN
            SET NEW.data_cancelamento = NEW.data_status;
        END IF;
    ELSEIF NEW.data_status <> OLD.data_status THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'data_status so muda quando a situacao muda.';
    END IF;
END$$

-- excluir somente erro de abertura sem diagnóstico ou orçamento.
CREATE TRIGGER trg_os_bd
BEFORE DELETE ON ordem_servico FOR EACH ROW
BEGIN
    IF OLD.status <> 'ABERTA' OR OLD.diagnostico IS NOT NULL OR OLD.valor_orcado IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'OS com andamento deve ser cancelada, nao excluida.';
    END IF;
END$$

CREATE TRIGGER trg_os_ai
AFTER INSERT ON ordem_servico FOR EACH ROW
BEGIN
    INSERT INTO ordem_servico_historico
        (id_ordem_servico, status_anterior, status_novo, data_evento, observacao)
    VALUES (NEW.id_ordem_servico, NULL, NEW.status, NEW.data_status, NEW.observacao_status);
END$$

CREATE TRIGGER trg_os_au
AFTER UPDATE ON ordem_servico FOR EACH ROW
BEGIN
    IF NEW.status <> OLD.status THEN
        INSERT INTO ordem_servico_historico
            (id_ordem_servico, status_anterior, status_novo, data_evento, observacao)
        VALUES (NEW.id_ordem_servico, OLD.status, NEW.status, NEW.data_status, NEW.observacao_status);
    END IF;
END$$


DELIMITER ;

USE assistencia_tecnica_completa;
DELIMITER $$
CREATE PROCEDURE sp_abrir_os (
    IN p_equipamento INT UNSIGNED, IN p_tecnico INT UNSIGNED, IN p_servico INT UNSIGNED,
    IN p_problema TEXT, IN p_previsao DATE, OUT p_numero_os INT UNSIGNED
)
BEGIN
    INSERT INTO ordem_servico (id_equipamento, id_tecnico, id_servico, problema_relatado, data_previsao)
    VALUES (p_equipamento, p_tecnico, p_servico, p_problema, p_previsao);
    SET p_numero_os = LAST_INSERT_ID();
END$$

CREATE PROCEDURE sp_alterar_status_os (
    IN p_numero_os INT UNSIGNED, IN p_status VARCHAR(30),
    IN p_data_evento DATETIME, IN p_observacao VARCHAR(255)
)
BEGIN
    IF p_status IS NULL OR p_status NOT IN ('EM_ANALISE','AGUARDANDO_APROVACAO',
        'EM_MANUTENCAO','CONCLUIDA','ENTREGUE','CANCELADA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Informe uma situacao de destino valida.';
    END IF;
    UPDATE ordem_servico
    SET status = p_status,
        data_status = COALESCE(p_data_evento, CURRENT_TIMESTAMP),
        observacao_status = NULLIF(TRIM(p_observacao), ''),
        motivo_cancelamento = CASE WHEN p_status = 'CANCELADA'
            THEN NULLIF(TRIM(p_observacao), '') ELSE motivo_cancelamento END
    WHERE id_ordem_servico = p_numero_os AND status <> p_status;
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'OS inexistente ou ja esta na situacao informada.';
    END IF;
END$$

-- relatório da rotina principal, por data de abertura, com dois resultados.
CREATE PROCEDURE sp_relatorio_os (
    IN p_inicio DATE, IN p_fim DATE, IN p_tecnico INT UNSIGNED, IN p_status VARCHAR(30)
)
BEGIN
    IF p_inicio IS NULL OR p_fim IS NULL OR p_inicio > p_fim OR p_fim = '9999-12-31' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Informe um periodo valido para o relatorio.';
    END IF;
    IF p_status IS NOT NULL AND p_status NOT IN ('ABERTA','EM_ANALISE','AGUARDANDO_APROVACAO',
        'EM_MANUTENCAO','CONCLUIDA','ENTREGUE','CANCELADA') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Situacao invalida no filtro do relatorio.';
    END IF;
    SELECT * FROM vw_ordens_servico
    WHERE data_abertura >= p_inicio AND data_abertura < DATE_ADD(p_fim, INTERVAL 1 DAY)
      AND (p_tecnico IS NULL OR id_tecnico = p_tecnico)
      AND (p_status IS NULL OR status = p_status)
    ORDER BY data_abertura, numero_os;

    SELECT COUNT(*) AS quantidade_os,
        COALESCE(SUM(status = 'CANCELADA'), 0) AS quantidade_canceladas,
        COALESCE(SUM(status IN ('CONCLUIDA','ENTREGUE')), 0) AS quantidade_concluidas,
        COALESCE(SUM(CASE WHEN status IN ('CONCLUIDA','ENTREGUE')
            THEN valor_final ELSE 0 END), 0) AS total_servicos_concluidos
    FROM ordem_servico
    WHERE data_abertura >= p_inicio AND data_abertura < DATE_ADD(p_fim, INTERVAL 1 DAY)
      AND (p_tecnico IS NULL OR id_tecnico = p_tecnico)
      AND (p_status IS NULL OR status = p_status);
END$$


DELIMITER ;

-- Guardar a instalação contra carga sobre cadastros já preenchidos.
DELIMITER $$
CREATE PROCEDURE sp_validar_carga_vazia()
BEGIN
    IF EXISTS (SELECT 1 FROM cliente) OR EXISTS (SELECT 1 FROM tecnico) OR
       EXISTS (SELECT 1 FROM equipamento) OR EXISTS (SELECT 1 FROM servico) OR
       EXISTS (SELECT 1 FROM ordem_servico) OR EXISTS (SELECT 1 FROM ordem_servico_historico) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Carga exige tabelas vazias; utilize o banco existente sem repetir a carga.';
    END IF;
END$$
DELIMITER ;
