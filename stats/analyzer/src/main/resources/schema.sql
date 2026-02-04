create table if not exists event_similarity
(
    id         bigint generated always as identity primary key,
    event_a    bigint           not null,
    event_b    bigint           not null,
    score      double precision not null,
    updated_at timestamptz      not null,
    unique (event_a, event_b)
);

create table if not exists user_event_interaction
(
    id       bigint generated always as identity primary key,
    user_id  bigint           not null,
    event_id bigint           not null,
    weight   double precision not null,
    last_ts  timestamptz      not null,
    unique (user_id, event_id)
);

create index if not exists idx_user_event_interaction_user_ts
    on user_event_interaction (user_id, last_ts desc);

create index if not exists idx_user_event_interaction_event
    on user_event_interaction (event_id);
