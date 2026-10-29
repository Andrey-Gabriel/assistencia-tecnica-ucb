-- Cria operações de abertura, mudança de situação e relatório.
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
