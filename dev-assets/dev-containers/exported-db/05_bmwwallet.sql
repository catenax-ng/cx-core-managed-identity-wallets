    --
    -- PostgreSQL database dump
    --

    -- Dumped from database version 14.2
    -- Dumped by pg_dump version 15.0

    SET statement_timeout = 0;
    SET lock_timeout = 0;
    SET idle_in_transaction_session_timeout = 0;
    SET client_encoding = 'UTF8';
    SET standard_conforming_strings = on;
    SELECT pg_catalog.set_config('search_path', '', false);
    SET check_function_bodies = false;
    SET xmloption = content;
    SET client_min_messages = warning;
    SET row_security = off;

    DROP DATABASE "BPNL000000000002-2022-11-10T11:15:26Z";
    --
    -- Name: BPNL000000000002-2022-11-10T11:15:26Z; Type: DATABASE; Schema: -; Owner: postgres
    --

    CREATE DATABASE "BPNL000000000002-2022-11-10T11:15:26Z" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


    ALTER DATABASE "BPNL000000000002-2022-11-10T11:15:26Z" OWNER TO postgres;

    \connect -reuse-previous=on "dbname='BPNL000000000002-2022-11-10T11:15:26Z'"

    SET statement_timeout = 0;
    SET lock_timeout = 0;
    SET idle_in_transaction_session_timeout = 0;
    SET client_encoding = 'UTF8';
    SET standard_conforming_strings = on;
    SELECT pg_catalog.set_config('search_path', '', false);
    SET check_function_bodies = false;
    SET xmloption = content;
    SET client_min_messages = warning;
    SET row_security = off;

    --
    -- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
    --

    -- *not* creating schema, since initdb creates it


    ALTER SCHEMA public OWNER TO postgres;

    SET default_tablespace = '';

    SET default_table_access_method = heap;

    --
    -- Name: config; Type: TABLE; Schema: public; Owner: postgres
    --

    CREATE TABLE public.config (
        name text NOT NULL,
        value text
    );


    ALTER TABLE public.config OWNER TO postgres;

    --
    -- Name: items; Type: TABLE; Schema: public; Owner: postgres
    --

    CREATE TABLE public.items (
        id bigint NOT NULL,
        profile_id bigint NOT NULL,
        kind smallint NOT NULL,
        category bytea NOT NULL,
        name bytea NOT NULL,
        value bytea NOT NULL,
        expiry timestamp without time zone
    );


    ALTER TABLE public.items OWNER TO postgres;

    --
    -- Name: items_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
    --

    CREATE SEQUENCE public.items_id_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;


    ALTER TABLE public.items_id_seq OWNER TO postgres;

    --
    -- Name: items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
    --

    ALTER SEQUENCE public.items_id_seq OWNED BY public.items.id;


    --
    -- Name: items_tags; Type: TABLE; Schema: public; Owner: postgres
    --

    CREATE TABLE public.items_tags (
        id bigint NOT NULL,
        item_id bigint NOT NULL,
        name bytea NOT NULL,
        value bytea NOT NULL,
        plaintext smallint NOT NULL
    );


    ALTER TABLE public.items_tags OWNER TO postgres;

    --
    -- Name: items_tags_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
    --

    CREATE SEQUENCE public.items_tags_id_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;


    ALTER TABLE public.items_tags_id_seq OWNER TO postgres;

    --
    -- Name: items_tags_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
    --

    ALTER SEQUENCE public.items_tags_id_seq OWNED BY public.items_tags.id;


    --
    -- Name: profiles; Type: TABLE; Schema: public; Owner: postgres
    --

    CREATE TABLE public.profiles (
        id bigint NOT NULL,
        name text NOT NULL,
        reference text,
        profile_key bytea
    );


    ALTER TABLE public.profiles OWNER TO postgres;

    --
    -- Name: profiles_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
    --

    CREATE SEQUENCE public.profiles_id_seq
        START WITH 1
        INCREMENT BY 1
        NO MINVALUE
        NO MAXVALUE
        CACHE 1;


    ALTER TABLE public.profiles_id_seq OWNER TO postgres;

    --
    -- Name: profiles_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
    --

    ALTER SEQUENCE public.profiles_id_seq OWNED BY public.profiles.id;


    --
    -- Name: items id; Type: DEFAULT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.items ALTER COLUMN id SET DEFAULT nextval('public.items_id_seq'::regclass);


    --
    -- Name: items_tags id; Type: DEFAULT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.items_tags ALTER COLUMN id SET DEFAULT nextval('public.items_tags_id_seq'::regclass);


    --
    -- Name: profiles id; Type: DEFAULT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.profiles ALTER COLUMN id SET DEFAULT nextval('public.profiles_id_seq'::regclass);


    --
    -- Data for Name: config; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.config (name, value) VALUES ('default_profile', '8d9d67b5-5b46-475c-a2d7-a549a531b3d0');
    INSERT INTO public.config (name, value) VALUES ('key', 'kdf:argon2i:13:mod?salt=b4ab1583937f6e753c3e826165412c95');
    INSERT INTO public.config (name, value) VALUES ('version', '1');


    --
    -- Data for Name: items; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (1, 1, 2, '\x6f9cd7fbad1c66b11599ce670f4745c1e8e6b437f2faca1092e5dcb65893556972b1', '\xfb4c94180987da2d3372990754a9771b74367de897260c2042044501f99a661e88ed69a9a8cd4f478bcfb850e7dc', '\xa96a028d97f5798127e2233a2064c5acbec38238c34265774159ed23f0d504815d5a31af911d41ff73', NULL);
    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (2, 1, 1, '\x32eb360510ec9c14fe2b3f4b03c579ba42be85c0d864b2a4d3b7eed9fc908e03bdbb0c6209', '\x451f822f20c5fb1883710e8af4b8e550d566e418e8ad8293298c2e67f3d0b7dc42f1d8cf0755792c800ace88ca460c2c6c2da92f951ef26801c6a1f4e8fad28665e74355cc7b15ec', '\xf47372cb78fba4e6cfe9dcadd9141b6d837b5bb922db6f6be4c0fa501b69c9b05978434f3f9ebe6ea9612a88e9acdc1f02bf98d116ce5ebcc477f5df22a46291b880550f49fb9b7fcb6f760b9ada394be2141cb18d28fd737aa9acef49ce7c1679d83ba4ca41ef3e03a2fc93358762fe1ddddc868e15051a10239d34db3445e82c254d414019a9d8e18755d0c6c2eaaac4e6bf853970b10261318af0d3b9fa758bbf6e20fbdec38402af5a1aaf', NULL);
    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (3, 1, 2, '\x30d9a7797e9d4459fc222d90fce01b41b564dd5250e7197fc87625834b59bd', '\x95089e19695813857a5275b7d266552334a9a12f3ca74e47b828358d1c9e080893df69d73ec3940f95ce2dab86a0d820292e', '\x87a4c5bda5a4ec22762e7d294c76f9bc4265d93ad36b529f362377a593bf4f6c179b422eda5134912c1cf1cf07860aa535d48a22dafcc769318545777ad3b0f04461b505d7f2662c1ce9e2745fc808440bb845be1453704ba3f6cfb8f905c453968cce03a9d48cf1c22ba0ebb341adb2b56dea1e221add89aecdaee5a6a24aae6a774e102ad9da242356dc3ce2b94e609d22e72daa9957eccbeaea2261272d0dfde088b40e9c5658c6662d15c956544e89eb', NULL);


    --
    -- Data for Name: items_tags; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (1, 2, '\xa2f0a8441e4e1752dd298ea64e19ef5fa16ce084d88283d67b5c7c5fd92ea6', '\x40ec2a978b0648ba1a7698917a0002c27c2b76169536bc3d0ad6d2a0ac94d20f2ee1a7', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (2, 2, '\x8de00da4e8e3cc986aff1f28214cf90d7864908b1847ada99707e02acccd7130ae', '\x938d671bf5bd783c9f242b4e25f6016d2c711be62279da864d75f94338360f146120fb5d606401409bc75f7f5840a591d10ccc0792ee97e7d639b0516c4f760f7c18f8b356bf73', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (3, 3, '\xa4dce5ccc7275ff89280f1f092104690c7d43f2786aea6d7513a57daf188be9006f1', '\xbac9f2d4b16714c68e1f9c197bede3b7d0cd32487e30119db937b2364247a8', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (4, 3, '\x65e84f0284a84f12f74955da7b2d1a5176a548bcd0fdb3c1bf181536a16f81218bae', '\x75e71ab7eb88f22d7c87ba6016bc9fac7f1c78986ae06ce42a835c92e037761315fb4ac710dc6ff5d893b264880e184656e548029f8f96a5310bf9c053de7f45152133ce95480422', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (5, 3, '\xa51f4e8f7359c2efbab85bda28ef5b5820124fef141b50c0ef0812e054434cf631489e541e7784', '\x40ec2a978b0648ba1a7698917a0002c27c2b76169536bc3d0ad6d2a0ac94d20f2ee1a7', 0);


    --
    -- Data for Name: profiles; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.profiles (id, name, reference, profile_key) VALUES (1, '8d9d67b5-5b46-475c-a2d7-a549a531b3d0', NULL, '\xfd42f44b5b1358ca20f187d09dd6b71a62ff8909c65f932e070c49150579c5be7066af57c7bcf4196e9175d544bd14bd8bb0a44da2ce984febf430391436f6bdfba1d68366b4ce9324dde1fdc07f991f8296608aab1f81174727e47660e7cb686b257535d03e40c3371a57e4f5879e11d8a8caf1acdefe26fe238e59567e73d3e722af497ff0cc9ce5fced5ab1f4b56daf7c33eb81bf39bea2c53505387ce6da49915295868344d3f3038ebed7805a66a4c3ea8d948f559480619536df902be5de02ce26d32a958ddb1d88460547184df3de63cb0dd22445548bdd8a2112b68de6c7ff4df462ad94baf979f16de8b0dc219145313664197d3d4a7b29eb18d20fb3f2df57b6d635');


    --
    -- Name: items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
    --

    SELECT pg_catalog.setval('public.items_id_seq', 3, true);


    --
    -- Name: items_tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
    --

    SELECT pg_catalog.setval('public.items_tags_id_seq', 5, true);


    --
    -- Name: profiles_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
    --

    SELECT pg_catalog.setval('public.profiles_id_seq', 1, true);


    --
    -- Name: config config_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.config
        ADD CONSTRAINT config_pkey PRIMARY KEY (name);


    --
    -- Name: items items_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.items
        ADD CONSTRAINT items_pkey PRIMARY KEY (id);


    --
    -- Name: items_tags items_tags_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.items_tags
        ADD CONSTRAINT items_tags_pkey PRIMARY KEY (id);


    --
    -- Name: profiles profiles_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.profiles
        ADD CONSTRAINT profiles_pkey PRIMARY KEY (id);


    --
    -- Name: ix_items_tags_item_id; Type: INDEX; Schema: public; Owner: postgres
    --

    CREATE INDEX ix_items_tags_item_id ON public.items_tags USING btree (item_id);


    --
    -- Name: ix_items_tags_name_enc; Type: INDEX; Schema: public; Owner: postgres
    --

    CREATE INDEX ix_items_tags_name_enc ON public.items_tags USING btree (name, substr(value, 1, 12)) WHERE (plaintext = 0);


    --
    -- Name: ix_items_tags_name_plain; Type: INDEX; Schema: public; Owner: postgres
    --

    CREATE INDEX ix_items_tags_name_plain ON public.items_tags USING btree (name, value) WHERE (plaintext = 1);


    --
    -- Name: ix_items_uniq; Type: INDEX; Schema: public; Owner: postgres
    --

    CREATE UNIQUE INDEX ix_items_uniq ON public.items USING btree (profile_id, kind, category, name);


    --
    -- Name: ix_profile_name; Type: INDEX; Schema: public; Owner: postgres
    --

    CREATE UNIQUE INDEX ix_profile_name ON public.profiles USING btree (name);


    --
    -- Name: items items_profile_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.items
        ADD CONSTRAINT items_profile_id_fkey FOREIGN KEY (profile_id) REFERENCES public.profiles(id) ON UPDATE CASCADE ON DELETE CASCADE;


    --
    -- Name: items_tags items_tags_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
    --

    ALTER TABLE ONLY public.items_tags
        ADD CONSTRAINT items_tags_item_id_fkey FOREIGN KEY (item_id) REFERENCES public.items(id) ON UPDATE CASCADE ON DELETE CASCADE;


    --
    -- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
    --

    REVOKE USAGE ON SCHEMA public FROM PUBLIC;
    GRANT ALL ON SCHEMA public TO PUBLIC;


    --
    -- PostgreSQL database dump complete
    --

