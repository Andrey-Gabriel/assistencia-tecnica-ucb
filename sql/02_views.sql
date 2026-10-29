-- Cria as consultas reutilizáveis para as telas e relatórios.
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

