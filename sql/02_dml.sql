-- 02_dml.sql - Dados de demonstração da Assistência Técnica.
-- Contém INSERT e UPDATE dos cadastros e dos atendimentos.
-- Executar uma vez após 01_ddl.sql, em tabelas vazias.
-- Interrompa no primeiro erro; nesse caso execute ROLLBACK antes de tentar novamente.
-- Os eventos do histórico são gerados pelas triggers do DDL.
USE assistencia_tecnica_completa;
SET NAMES utf8mb4;

CALL sp_validar_carga_vazia();

START TRANSACTION;

-- Documento opcional: NULL evita usar CPF/CNPJ real ou inventar CPF válido.
INSERT INTO cliente (id_cliente, nome, documento, telefone, email, endereco) VALUES
(1, 'Carla Martins', NULL, '(11) 90000-0001', 'carla@example.com', 'Rua Exemplo, 10'),
(2, 'Bruno Almeida', NULL, '(11) 90000-0002', 'bruno@example.com', 'Rua Exemplo, 20'),
(3, 'Luiza Santos', NULL, '(11) 90000-0003', 'luiza@example.com', 'Rua Exemplo, 30'),
(4, 'Ana Costa', NULL, '(11) 90000-0004', 'ana@example.com', 'Rua Exemplo, 40'),
(5, 'Rafael Lima', NULL, '(11) 90000-0005', 'rafael@example.com', 'Rua Exemplo, 50'),
(6, 'Beatriz Souza', NULL, '(11) 90000-0006', 'beatriz@example.com', 'Rua Exemplo, 60'),
(7, 'Lucas Pereira', NULL, '(11) 90000-0007', 'lucas@example.com', 'Rua Exemplo, 70'),
(8, 'Mariana Rocha', NULL, '(11) 90000-0008', 'mariana@example.com', 'Rua Exemplo, 80'),
(9, 'Gabriel Oliveira', NULL, '(11) 90000-0009', 'gabriel@example.com', 'Rua Exemplo, 90'),
(10, 'Fernanda Alves', NULL, '(11) 90000-0010', 'fernanda@example.com', 'Rua Exemplo, 100'),
(11, 'Thiago Castro', NULL, '(11) 90000-0011', NULL, NULL),
(12, 'Camila Nunes', NULL, '(11) 90000-0012', NULL, NULL);

-- Técnico 4 inativo e sem OS, para testar filtros e LEFT JOIN.
INSERT INTO tecnico (id_tecnico, nome, telefone, email, especialidade, ativo) VALUES
(1, 'Pedro Oliveira', '(11) 90000-0101', 'pedro@example.com', 'Hardware e notebooks', 1),
(2, 'Marina Costa', '(11) 90000-0102', 'marina@example.com', 'Software e sistemas', 1),
(3, 'Diego Fernandes', '(11) 90000-0103', 'diego@example.com', 'Redes e computadores', 1),
(4, 'Juliana Ribeiro', '(11) 90000-0104', 'juliana@example.com', 'Impressoras', 0);

INSERT INTO equipamento
    (id_equipamento, id_cliente, tipo, marca, modelo, numero_serie, observacoes) VALUES
(1, 1, 'Notebook', 'Dell', 'Inspiron 15', 'DEMO-001', 'Recebido com carregador.'),
(2, 2, 'Computador', 'Lenovo', 'ThinkCentre', 'DEMO-002', NULL),
(3, 3, 'Notebook', 'Acer', 'Aspire 5', 'DEMO-003', 'Risco na tampa.'),
(4, 4, 'Notebook', 'Samsung', 'Book', 'DEMO-004', NULL),
(5, 5, 'Computador', 'Dell', 'OptiPlex', 'DEMO-005', NULL),
(6, 6, 'Notebook', 'Lenovo', 'IdeaPad 3', 'DEMO-006', NULL),
(7, 7, 'Notebook', 'Asus', 'VivoBook', 'DEMO-007', NULL),
(8, 8, 'Computador', 'HP', 'ProDesk', 'DEMO-008', NULL),
(9, 9, 'Notebook', 'HP', 'Pavilion', 'DEMO-009', NULL),
(10, 10, 'Notebook', 'Positivo', 'Motion', 'DEMO-010', NULL),
(11, 1, 'Notebook', 'Samsung', 'Expert', 'DEMO-011', NULL),
(12, 2, 'Computador', 'Acer', 'Aspire Desktop', 'DEMO-012', NULL),
(13, 3, 'Notebook', 'Dell', 'Latitude', 'DEMO-013', NULL),
(14, 4, 'Notebook', 'Lenovo', 'ThinkPad', 'DEMO-014', NULL),
(15, 5, 'Impressora', 'Epson', 'EcoTank', NULL, 'Equipamento cadastrado, sem OS.'),
(16, 6, 'Impressora', 'HP', 'LaserJet', NULL, 'Equipamento cadastrado, sem OS.');

INSERT INTO servico
    (id_servico, nome, descricao, valor_referencia, prazo_estimado_dias, ativo) VALUES
(1, 'Formatação', 'Reinstalação e configuração do sistema operacional.', 150.00, 2, 1),
(2, 'Limpeza preventiva', 'Limpeza interna e troca de pasta térmica.', 180.00, 2, 1),
(3, 'Troca de tela', 'Substituição de tela conforme o modelo e orçamento.', 480.00, 5, 1),
(4, 'Troca de teclado', 'Substituição do teclado e testes.', 250.00, 3, 1),
(5, 'Recuperação de dados', 'Tentativa de recuperação e cópia de arquivos.', 300.00, 5, 1),
(6, 'Remoção de malware', 'Remoção de ameaças e configuração de proteção.', 120.00, 2, 1),
(7, 'Upgrade de memória', 'Instalação de memória conforme orçamento.', 220.00, 3, 1),
(8, 'Manutenção de impressora', 'Serviço temporariamente indisponível.', 200.00, 5, 0);

-- Todas as 24 OS começam ABERTAS, conforme .
-- IDs 1-3 abertas; 4-6 análise; 7-9 aguardando; 10-12 manutenção;
-- 13-15 concluídas; 16-21 entregues; 22-24 canceladas.
INSERT INTO ordem_servico
    (id_ordem_servico, id_equipamento, id_tecnico, id_servico, problema_relatado,
     data_abertura, data_previsao, observacao_status) VALUES
(1, 1, 1, 1, 'Sistema apresenta lentidão.', '2026-10-26 09:00:00', '2026-10-30', 'Recebimento do aparelho.'),
(2, 2, 2, 2, 'Equipamento aquece durante o uso.', '2026-10-27 09:00:00', '2026-10-30', 'Recebimento do aparelho.'),
(3, 3, 3, 3, 'Tela apresenta manchas.', '2026-10-28 09:00:00', '2026-11-04', 'Recebimento do aparelho.'),
(4, 4, 1, 4, 'Teclas não respondem.', '2026-10-24 09:00:00', '2026-10-27', 'Recebimento do aparelho.'),
(5, 5, 2, 5, 'Arquivos não são encontrados.', '2026-10-25 09:00:00', '2026-10-30', 'Recebimento do aparelho.'),
(6, 6, 3, 6, 'Navegador abre páginas sozinho.', '2026-10-26 09:00:00', NULL, 'Recebimento do aparelho.'),
(7, 7, 1, 7, 'Memória insuficiente para aplicativos.', '2026-10-21 09:00:00', '2026-10-24', 'Recebimento do aparelho.'),
(8, 8, 2, 1, 'Sistema não inicia.', '2026-10-22 09:00:00', '2026-10-25', 'Recebimento do aparelho.'),
(9, 9, 3, 2, 'Ventoinha faz muito barulho.', '2026-10-23 09:00:00', '2026-10-26', 'Recebimento do aparelho.'),
(10, 10, 1, 3, 'Tela quebrada após queda.', '2026-10-18 09:00:00', '2026-10-23', 'Recebimento do aparelho.'),
(11, 11, 2, 4, 'Teclado apresenta falhas.', '2026-10-19 09:00:00', '2026-10-23', 'Recebimento do aparelho.'),
(12, 12, 3, 5, 'Disco com arquivos inacessíveis.', '2026-10-20 09:00:00', '2026-10-27', 'Recebimento do aparelho.'),
(13, 13, 1, 6, 'Alertas constantes de ameaças.', '2026-10-15 09:00:00', '2026-10-18', 'Recebimento do aparelho.'),
(14, 14, 2, 7, 'Necessidade de expansão de memória.', '2026-10-16 09:00:00', '2026-10-20', 'Recebimento do aparelho.'),
(15, 1, 3, 1, 'Reinstalação do sistema solicitada.', '2026-10-17 09:00:00', '2026-10-20', 'Recebimento do aparelho.'),
(16, 2, 1, 2, 'Limpeza preventiva solicitada.', '2026-10-01 09:00:00', '2026-10-05', 'Recebimento do aparelho.'),
(17, 3, 2, 3, 'Tela sem imagem.', '2026-10-03 09:00:00', '2026-10-08', 'Recebimento do aparelho.'),
(18, 4, 3, 4, 'Teclado não funciona.', '2026-10-05 09:00:00', '2026-10-09', 'Recebimento do aparelho.'),
(19, 5, 1, 5, 'Solicitação de recuperação de arquivos.', '2026-10-07 09:00:00', '2026-10-12', 'Recebimento do aparelho.'),
(20, 6, 2, 6, 'Suspeita de malware.', '2026-10-09 09:00:00', '2026-10-13', 'Recebimento do aparelho.'),
(21, 7, 3, 7, 'Aplicativos exigem mais memória.', '2026-10-11 09:00:00', '2026-10-15', 'Recebimento do aparelho.'),
(22, 8, 1, 1, 'Solicitação inicial de formatação.', '2026-09-27 09:00:00', NULL, 'Recebimento do aparelho.'),
(23, 9, 2, 2, 'Solicitação inicial de limpeza.', '2026-10-12 09:00:00', NULL, 'Recebimento do aparelho.'),
(24, 10, 3, 3, 'Solicitação de substituição de tela.', '2026-10-14 09:00:00', NULL, 'Recebimento do aparelho.');

-- Eventos em ordem cronológica: as triggers geram o histórico e as datas do fluxo.
UPDATE ordem_servico SET status = 'EM_ANALISE',
    data_status = DATE_ADD(data_abertura, INTERVAL 1 HOUR),
    observacao_status = 'Técnico iniciou os testes.'
WHERE id_ordem_servico IN (4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,23,24);

UPDATE ordem_servico os JOIN servico s ON s.id_servico = os.id_servico
SET os.diagnostico = CONCAT('Avaliação técnica indica: ', s.nome, '.'),
    os.valor_orcado = s.valor_referencia,
    os.status = 'AGUARDANDO_APROVACAO',
    os.data_status = DATE_ADD(os.data_abertura, INTERVAL 2 HOUR),
    os.observacao_status = 'Orçamento enviado ao cliente.'
WHERE os.id_ordem_servico IN (7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,24);

UPDATE ordem_servico SET status = 'EM_MANUTENCAO',
    data_status = DATE_ADD(data_abertura, INTERVAL 1 DAY),
    observacao_status = 'Cliente aprovou o orçamento por telefone.'
WHERE id_ordem_servico IN (10,11,12,13,14,15,16,17,18,19,20,21);

UPDATE ordem_servico SET solucao_aplicada = 'Serviço executado e equipamento testado.',
    valor_final = valor_orcado, status = 'CONCLUIDA',
    data_status = DATE_ADD(data_abertura, INTERVAL 2 DAY),
    observacao_status = 'Conserto concluído; cliente avisado.'
WHERE id_ordem_servico IN (13,14,15,16,17,18,19,20,21);

UPDATE ordem_servico SET status = 'ENTREGUE',
    data_status = DATE_ADD(data_abertura, INTERVAL 3 DAY),
    observacao_status = 'Equipamento retirado pelo cliente.'
WHERE id_ordem_servico IN (16,17,18,19,20,21);

UPDATE ordem_servico SET status = 'CANCELADA',
    motivo_cancelamento = 'Cliente desistiu do atendimento.',
    data_status = DATE_ADD(data_abertura, INTERVAL 1 DAY),
    observacao_status = 'Cancelamento solicitado pelo cliente.'
WHERE id_ordem_servico IN (22,23,24);

COMMIT;

-- Esperado: 12 clientes, 4 técnicos, 16 equipamentos, 8 serviços, 24 OS, 90 eventos.
SELECT 'cliente' AS tabela, COUNT(*) AS quantidade FROM cliente
UNION ALL SELECT 'tecnico', COUNT(*) FROM tecnico
UNION ALL SELECT 'equipamento', COUNT(*) FROM equipamento
UNION ALL SELECT 'servico', COUNT(*) FROM servico
UNION ALL SELECT 'ordem_servico', COUNT(*) FROM ordem_servico
UNION ALL SELECT 'ordem_servico_historico', COUNT(*) FROM ordem_servico_historico;
