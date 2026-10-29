-- Cria validações automáticas e histórico de situações.
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
