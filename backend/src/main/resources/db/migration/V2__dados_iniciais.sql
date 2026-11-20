insert into tipo_caminhao (nome) values ('Truck'), ('Carreta'), ('Van');

insert into tipo_paletizacao (nome, fator_pallets) values ('Paletizado', 1), ('Não Paletizado', 2);

insert into faixa_horaria (descricao, hora_inicio, hora_fim) values
    ('08:00 - 10:00', '08:00:00', '10:00:00'),
    ('10:00 - 12:00', '10:00:00', '12:00:00'),
    ('13:00 - 15:00', '13:00:00', '15:00:00'),
    ('15:00 - 17:00', '15:00:00', '17:00:00');

insert into configuracao_capacidade (id, pallets_por_dia, pallets_por_faixa) values (1, 480, 120);
