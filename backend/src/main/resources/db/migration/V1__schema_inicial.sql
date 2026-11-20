create table tipo_caminhao (
    id   bigint not null auto_increment,
    nome varchar(50) not null,
    primary key (id),
    constraint uk_tipo_caminhao_nome unique (nome)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

create table tipo_paletizacao (
    id            bigint not null auto_increment,
    nome          varchar(50) not null,
    fator_pallets int not null,
    primary key (id),
    constraint uk_tipo_paletizacao_nome unique (nome),
    constraint ck_fator_pallets check (fator_pallets > 0)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

create table faixa_horaria (
    id          bigint not null auto_increment,
    descricao   varchar(50) not null,
    hora_inicio time not null,
    hora_fim    time not null,
    primary key (id),
    constraint ck_faixa_horas check (hora_inicio < hora_fim)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

create table configuracao_capacidade (
    id                bigint not null,
    pallets_por_dia   int not null,
    pallets_por_faixa int not null,
    primary key (id),
    constraint ck_cap_dia check (pallets_por_dia > 0),
    constraint ck_cap_faixa check (pallets_por_faixa > 0)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

create table usuario (
    id     bigint not null auto_increment,
    nome   varchar(80) not null,
    email  varchar(80) not null,
    senha  varchar(100) not null,
    perfil varchar(20) not null,
    primary key (id),
    constraint uk_usuario_email unique (email)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

create table agendamento (
    id                  bigint not null auto_increment,
    data                date not null,
    faixa_horaria_id    bigint not null,
    id_pedido           bigint not null,
    fornecedor          varchar(80) not null,
    email_fornecedor    varchar(80) not null,
    tipo_caminhao_id    bigint not null,
    tipo_paletizacao_id bigint not null,
    quantidade_pallets  int not null,
    pallets_consumidos  int not null,
    observacao          varchar(500),
    status              varchar(20) not null,
    criado_por          varchar(80) not null,
    criado_em           datetime(6) not null,
    primary key (id),
    constraint fk_agendamento_faixa foreign key (faixa_horaria_id) references faixa_horaria (id),
    constraint fk_agendamento_caminhao foreign key (tipo_caminhao_id) references tipo_caminhao (id),
    constraint fk_agendamento_paletizacao foreign key (tipo_paletizacao_id) references tipo_paletizacao (id),
    constraint ck_agendamento_qtd check (quantidade_pallets > 0),
    constraint ck_agendamento_consumo check (pallets_consumidos > 0)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

create index idx_agendamento_data_status on agendamento (data, status);

create table log_auditoria (
    id        bigint not null auto_increment,
    data_hora datetime(6) not null,
    usuario   varchar(80) not null,
    acao      varchar(40) not null,
    recurso   varchar(200) not null,
    detalhe   varchar(500),
    ip        varchar(60),
    status    int,
    primary key (id)
) engine = InnoDB default charset = utf8mb4 collate = utf8mb4_unicode_ci;

create index idx_log_auditoria_data on log_auditoria (data_hora);
