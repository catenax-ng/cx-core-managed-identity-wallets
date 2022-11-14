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

    DROP DATABASE "BPNL000000000000-2022-11-10T11:12:33Z";
    --
    -- Name: BPNL000000000000-2022-11-10T11:12:33Z; Type: DATABASE; Schema: -; Owner: postgres
    --

    CREATE DATABASE "BPNL000000000000-2022-11-10T11:12:33Z" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


    ALTER DATABASE "BPNL000000000000-2022-11-10T11:12:33Z" OWNER TO postgres;

    \connect -reuse-previous=on "dbname='BPNL000000000000-2022-11-10T11:12:33Z'"

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

    INSERT INTO public.config (name, value) VALUES ('default_profile', '68ef98e3-2d1d-417a-adf4-1b6994b0b907');
    INSERT INTO public.config (name, value) VALUES ('key', 'kdf:argon2i:13:mod?salt=1358fb38c40c74bcd78990994ea1c3d4');
    INSERT INTO public.config (name, value) VALUES ('version', '1');


    --
    -- Data for Name: items; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (2, 1, 1, '\xa680d0d8e1bc8e410604fd856bd223d5c90913022b73f362f65bc4ff03fabeb5e1f86f33aa', '\xdaf30f07ee255e24f8010dc5eb9f97bf6e6a7e90b0ec8df212bfede48dee0691b3fe7fa797486ce322a00516316e2d23a986feadc7237f61f249cabea9c3bcbed45ee2f218626099', '\x9a1cf9e826b747b48c188667d66fe73b0478d076db71edfc15ce34293c83fcaaed9f4ee7a8e9bca13c1ffb2f9bcfd2316f3106bf3311357bab13ec9f5c57e7334132852d3f0c3a9ab732a346dd6b11838896e3468346889d9ee3f2d4a0dce7ba602d4c25c5ddbd97a655e4923ed74d53989b113abd5bf763c1c41f3d60d6694d629f4b8441cf934d99640109c0f70aa26523712ea2ed56f67a82c2bb83c91f65d064b97701de38d9366a7cd628', NULL);
    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (1, 1, 2, '\x2c97b3d164f2ad980a114ff50b2007f7b7551ac7d0c00a4eb6f24f514e9d84fdf652', '\x4e524c194cc501ec30ae68a12ebd32d36211524a3482bec48343a9634c5cb3f3773bd9b7448dd4da249b1148ae78', '\x20ce6014c015b3b3d5a4840d90821ab8d95c248bd03718a2bef792f388e2f8e27fe6d06585c16077e66562fba882da96fd29371eb82a0e006222d72d9b', NULL);
    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (3, 1, 2, '\xde73a1d4b982923ca44415f62279391ddd324250a121d4432b71db3461cdc9', '\x95deb7b1ea157686c9b411e177aeaa0d9a6d65e67582237f8855b97e0459222cfaf83fffdbe6929142da28a926edf64413c4', '\x970e24616f039807d427ad0f7627669a33433798df586340fd2603a48d5a5d2d195aa6bbe8062b0e6c59d79954cea88dc5a87fba02c36fa9898c63532cb0842ff248668638bcb3620ae32a21b88d6ab809f114de6a27c4d5c44738605525347a742f69fcef7bd2c15755e11c0fc5e9f33b92a9a0058c2d3371a4214a7b349a2e99326a2fe3a50d502809b0e61f05496091dc3e3d5c8bc671988cea3040a41bee29eee4fc7ba47b966d48e38ce4fa0d15ce27d9130299fd14e53649e0cf9c83896e7de60d7a5d2d6a88d8a41161a3aeb1b368b8d8db2a28db679f4dd51b71134fa478e780be', NULL);


    --
    -- Data for Name: items_tags; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (1, 2, '\xd9bb6a1588f2eea8cf8b0821b75451e045eafc69f79d54ba54b9f115746e07', '\xe3d3ebdcb406803b776ea77d941f9a4d9bc469262ca910d1d07782dd6912b33a43710f', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (2, 2, '\x960d4889629ed0b52180a07a5d01e86434ed4d2a66e7d29361046e34a81717c975', '\x7ec9e836f826aba15bfacf6064f19f3f522f23442fda4a6e9a7c291ee724095cc636acfa19cfddf0cf02241c28da5dbd0b02a6037cb8bd0bc1377075ec04a55b5866d6d3e7347b', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (9, 3, '\x980c5f2f6145a3f642a557954b1ef2a30ea838a980d3ba78190036d264f5ecaa0949', '\x9e7d0cfd54d0b70f2efd90eeb030b84d1a9b22889cef48b0448e1908a4438f', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (10, 3, '\xa3eafe22cebc11e9a610114425cb83a8ecee58bf778cfaac76521f435fcffda9dd21', '\x9dd6856cfc6c04730f38c3ff0c4b3a3a0a4a5f530f6f655bd9bfb1f4c1b2c9abadbb2d9e7adaf9ca314d31429a63f7364e971790aad76bcc6b3e3204383d7803fc70aaf57712fb3d', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (11, 3, '\xe720c6842fd50cbc281c472d584d8a11c035afbd75a2d296dc580733491f3d3ee9f34201c2d41d', '\xe3d3ebdcb406803b776ea77d941f9a4d9bc469262ca910d1d07782dd6912b33a43710f', 0);


    --
    -- Data for Name: profiles; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.profiles (id, name, reference, profile_key) VALUES (1, '68ef98e3-2d1d-417a-adf4-1b6994b0b907', NULL, '\x64a4f416f076c1876fae5d1e646b5dc32fe54aea1ae5f9011870f047724f9b2cdc091cae57b154cfb06536f36daff4939ce1ae0514635a1d574cc5f4f5a1968d6f1dce5aa88191b6efa16b45be15cc6d43a023ae316c591198d1dcb34c215743be257e7a37790a3afddf9ba5529129752b4ddc464901098cdeb1ce073eeff56e630ac22e82ecdb9ca230cde66b780c7adf4e56c30c4a82c1593317383fec0782a24fe47ef07298ef7efa99038cfc70b4dd3437f0bf5639c623506db97cdaf6d0254e77237ffa6f36c1663aca2a66f5426daa5def43a03e15bfa5a03ac28c056d9490b080a8fc2490200ac8306d64644aa15876c6527749560c2a0e3d171a3824b79dbfd765bcbb');


    --
    -- Name: items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
    --

    SELECT pg_catalog.setval('public.items_id_seq', 3, true);


    --
    -- Name: items_tags_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
    --

    SELECT pg_catalog.setval('public.items_tags_id_seq', 11, true);


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

