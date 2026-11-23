INSERT INTO tipo_caminhao (nome) VALUES 
    ('Truck'), 
    ('Carreta'), 
    ('Van');

INSERT INTO tipo_paletizacao (nome, fator_pallets) VALUES 
    ('Paletizado', 1), 
    ('Não Paletizado', 2);

INSERT INTO faixa_horaria (descricao, hora_inicio, hora_fim)
SELECT 
    TO_CHAR(inicio, 'HH24:MI') || ' - ' || TO_CHAR(fim, 'HH24:MI'),
    inicio,
    fim
FROM (VALUES 
    ('08:00:00'::time, '10:00:00'::time),
    ('10:00:00'::time, '12:00:00'::time),
    ('13:00:00'::time, '15:00:00'::time),
    ('15:00:00'::time, '17:00:00'::time)
) AS v(inicio, fim);

INSERT INTO configuracao_capacidade (id, pallets_por_dia, pallets_por_faixa) VALUES 
    (1, 480, 120);
