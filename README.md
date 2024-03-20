Hello dear readers!
To generate the database that aligns with this program, use the followiong SQL code, enjoy!:

create table notes
(
    content longtext null,
    date    datetime null,
    user_id int      null,
    id      int auto_increment
        primary key
);

create table users
(
    id       int auto_increment
        primary key,
    username varchar(255) not null,
    password varchar(255) not null,
    token    varchar(255) null
);

create table follows
(
    id          int auto_increment
        primary key,
    follower_id int not null,
    followed_id int not null,
    constraint follower_id
        unique (follower_id, followed_id),
    constraint fk_followee
        foreign key (followed_id) references users (id),
    constraint fk_follower
        foreign key (follower_id) references users (id)
);
