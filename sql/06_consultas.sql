-- Arquivo: 06_consultas.sql
-- Consultas de demonstração, incluindo o relatório principal.
-- Somente leitura; os resultados com datas fixas são reproduzíveis.
USE assistencia_tecnica_completa;
SET NAMES utf8mb4;

-- 1) Quais serviços estão disponíveis, do mais caro para o mais barato?
SELECT id_servico, nome, valor_referencia, prazo_estimado_dias
FROM servico WHERE ativo = 1
ORDER BY valor_referencia DESC, nome;

-- 2) Quais clientes possuem "Costa" no nome?
SELECT id_cliente, nome, telefone FROM cliente
WHERE nome LIKE '%Costa%' ORDER BY nome;

-- 3) Quais técnicos ativos trabalham com software ou redes?
SELECT id_tecnico, nome, especialidade FROM tecnico
WHERE ativo = 1 AND (especialidade LIKE '%Software%' OR especialidade LIKE '%Redes%')
ORDER BY nome;

-- 4) Quais equipamentos estão sem número de série informado?
SELECT e.id_equipamento, c.nome AS cliente, e.tipo, e.marca, e.modelo
FROM equipamento e JOIN cliente c ON c.id_cliente = e.id_cliente
WHERE e.numero_serie IS NULL ORDER BY e.id_equipamento;

-- 5) Quais equipamentos pertencem à cliente Carla (ID 1)?
SELECT id_equipamento, tipo, marca, modelo, numero_serie
FROM equipamento WHERE id_cliente = 1 ORDER BY id_equipamento;

-- 6) JOIN principal: quem é o cliente, qual equipamento, técnico e serviço de cada OS?
SELECT os.id_ordem_servico AS numero_os, c.nome AS cliente,
    CONCAT(e.marca, ' ', e.modelo) AS equipamento, t.nome AS tecnico,
    s.nome AS servico, os.status, os.data_abertura, os.valor_final
FROM ordem_servico os
JOIN equipamento e ON e.id_equipamento = os.id_equipamento
JOIN cliente c ON c.id_cliente = e.id_cliente
JOIN tecnico t ON t.id_tecnico = os.id_tecnico
JOIN servico s ON s.id_servico = os.id_servico
ORDER BY os.data_abertura DESC, os.id_ordem_servico DESC;

-- 7) Quantas OS cada técnico atende, incluindo o profissional sem OS?
SELECT t.id_tecnico, t.nome, t.ativo, COUNT(os.id_ordem_servico) AS quantidade_os
FROM tecnico t LEFT JOIN ordem_servico os ON os.id_tecnico = t.id_tecnico
GROUP BY t.id_tecnico, t.nome, t.ativo
ORDER BY quantidade_os DESC, t.nome;

-- 8) Quantos equipamentos cada cliente possui, incluindo quem ainda não tem aparelho?
SELECT c.id_cliente, c.nome, COUNT(e.id_equipamento) AS quantidade_equipamentos
FROM cliente c LEFT JOIN equipamento e ON e.id_cliente = c.id_cliente
GROUP BY c.id_cliente, c.nome ORDER BY quantidade_equipamentos DESC, c.nome;

-- 9) Quantas OS há em cada situação?
SELECT status, COUNT(*) AS quantidade FROM ordem_servico
GROUP BY status ORDER BY quantidade DESC, status;

-- 10) Quais OS aguardam a aprovação do orçamento pelo cliente?
SELECT numero_os, cliente, telefone_cliente, equipamento, servico, valor_orcado
FROM vw_ordens_servico WHERE status = 'AGUARDANDO_APROVACAO'
ORDER BY data_abertura, numero_os;

-- 11) Quais OS ainda em reparo estavam atrasadas em 28/10/2026?
-- Data fixa para apresentação: troque por CURRENT_DATE para consultar o dia atual.
SET @data_referencia = CAST('2026-10-28' AS DATE);
SELECT numero_os, cliente, tecnico, status, data_previsao,
    DATEDIFF(@data_referencia, data_previsao) AS dias_atraso
FROM vw_ordens_servico
WHERE status IN ('ABERTA','EM_ANALISE','AGUARDANDO_APROVACAO','EM_MANUTENCAO')
  AND data_previsao < @data_referencia
ORDER BY data_previsao, numero_os;

-- 12) Quais equipamentos foram consertados, mas ainda aguardam retirada?
SELECT numero_os, cliente, telefone_cliente, equipamento, data_conclusao, valor_final
FROM vw_ordens_servico WHERE status = 'CONCLUIDA'
ORDER BY data_conclusao, numero_os;

-- 13) Qual o valor dos serviços concluídos/entregues por técnico?
-- Não significa dinheiro recebido: não existe tabela de pagamentos nesta versão.
SELECT t.id_tecnico, t.nome,
    COUNT(os.id_ordem_servico) AS quantidade_concluidas,
    COALESCE(SUM(os.valor_final), 0) AS valor_servicos
FROM tecnico t
LEFT JOIN ordem_servico os ON os.id_tecnico = t.id_tecnico AND os.status IN ('CONCLUIDA','ENTREGUE')
GROUP BY t.id_tecnico, t.nome ORDER BY valor_servicos DESC, t.nome;

-- 14) Quais clientes têm mais de R$ 300,00 em serviços concluídos/entregues?
SELECT c.id_cliente, c.nome, SUM(os.valor_final) AS valor_servicos
FROM cliente c
JOIN equipamento e ON e.id_cliente = c.id_cliente
JOIN ordem_servico os ON os.id_equipamento = e.id_equipamento
WHERE os.status IN ('CONCLUIDA','ENTREGUE')
GROUP BY c.id_cliente, c.nome HAVING SUM(os.valor_final) > 300
ORDER BY valor_servicos DESC;

-- 15) Quais clientes nunca abriram uma OS? (subconsulta com NOT EXISTS)
SELECT c.id_cliente, c.nome FROM cliente c
WHERE NOT EXISTS (
    SELECT 1 FROM equipamento e JOIN ordem_servico os ON os.id_equipamento = e.id_equipamento
    WHERE e.id_cliente = c.id_cliente
) ORDER BY c.nome;

-- 16) Quais serviços custam acima da média do catálogo ativo?
SELECT id_servico, nome, valor_referencia FROM servico
WHERE ativo = 1 AND valor_referencia > (SELECT AVG(valor_referencia) FROM servico WHERE ativo = 1)
ORDER BY valor_referencia DESC;

-- 17) Qual o histórico completo da OS 16, desde a abertura até a entrega?
SELECT id_historico, status_anterior, status_novo, data_evento, observacao
FROM ordem_servico_historico WHERE id_ordem_servico = 16
ORDER BY data_evento, id_historico;

-- 18) Quais serviços foram solicitados, incluindo os sem nenhuma OS?
SELECT s.id_servico, s.nome, s.ativo, COUNT(os.id_ordem_servico) AS quantidade_os
FROM servico s LEFT JOIN ordem_servico os ON os.id_servico = s.id_servico
GROUP BY s.id_servico, s.nome, s.ativo ORDER BY quantidade_os DESC, s.nome;

-- 19) Qual o prazo médio de conserto por técnico, em horas?
SELECT t.id_tecnico, t.nome,
    COUNT(os.id_ordem_servico) AS quantidade_concluidas,
    ROUND(AVG(TIMESTAMPDIFF(MINUTE, os.data_abertura, os.data_conclusao)) / 60, 2) AS media_horas
FROM tecnico t JOIN ordem_servico os ON os.id_tecnico = t.id_tecnico
WHERE os.status IN ('CONCLUIDA','ENTREGUE')
GROUP BY t.id_tecnico, t.nome ORDER BY media_horas, t.nome;

-- 20) Qual é o relatório de OS abertas em outubro/2026, sem filtrar técnico ou situação?
-- 1º resultado: 23 OS. 2º: 23 OS, 2 canceladas, 9 concluídas/entregues e R$ 2.040,00.
-- O dia final é inclusivo, inclusive OS abertas às 23:59:59.
CALL sp_relatorio_os('2026-10-01', '2026-10-31', NULL, NULL);

-- Outros filtros para estudar (executar separadamente, se desejar):
-- CALL sp_relatorio_os('2026-10-01', '2026-10-31', 1, NULL);
-- CALL sp_relatorio_os('2026-10-01', '2026-10-31', NULL, 'ENTREGUE');
