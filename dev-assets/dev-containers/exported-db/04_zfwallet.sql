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

    DROP DATABASE "BPNL000000000001-2022-11-10T11:15:07Z";
    --
    -- Name: BPNL000000000001-2022-11-10T11:15:07Z; Type: DATABASE; Schema: -; Owner: postgres
    --

    CREATE DATABASE "BPNL000000000001-2022-11-10T11:15:07Z" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


    ALTER DATABASE "BPNL000000000001-2022-11-10T11:15:07Z" OWNER TO postgres;

    \connect -reuse-previous=on "dbname='BPNL000000000001-2022-11-10T11:15:07Z'"

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

    INSERT INTO public.config (name, value) VALUES ('default_profile', 'fb3750b8-c8d5-435e-935b-437cba233fb1');
    INSERT INTO public.config (name, value) VALUES ('key', 'kdf:argon2i:13:mod?salt=02b2bd05631847bb7494c0e9263c18d8');
    INSERT INTO public.config (name, value) VALUES ('version', '1');


    --
    -- Data for Name: items; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (1, 1, 2, '\xd6463a7d45aa091a0718fab0a755c011df07fba12c99029b12f95c7b409df439bfa9', '\x21207893fab3b37342fd1477954f189774f3ead4dbcfe5d9d3a7ae4624c1a2c09f0743671f4397bea3b2b8bf2ef8', '\x5cda460dfc34acc26dd157bca53cb4d563f12460d06bd2cd65d0614c2e592b0f76e0f0b6d20bff63b1', NULL);
    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (2, 1, 1, '\x207547066752e4c095ec1603b3f39838704b72726cce21a761e73e7d04466d916eff17fb74', '\x56c78973144be5c6b5e3a857e99286b76456582a958e9f0a6ddc6cb785a56c12fa79c9f5caa538119e0d3d4ff87814baf314946a4c0821d719578f3cceb9500e3a2daf2a0105698d', '\xbc7b87d1abc9aa35e607d8eb7ca0d41d2addebbaada5a656b193ce5c8a985637817c2387407d1d7477479c213081df1a689298ba4d67b6a04278f0e94f37fe75783d6221613b80c438fd3970214754cb3b9b0bb3cca333f3199369055f8886ae1714a0cfe45448b4af6e60cd922d7f6e4030abbab991ebb12d2d72a5f7204b23b71d03e325d553dbb2625f732d79690adc8a56aa243d5616dcc2c811cb3e618a4297bec30975526088b196c932', NULL);
    INSERT INTO public.items (id, profile_id, kind, category, name, value, expiry) VALUES (3, 1, 2, '\x4a8229422bb872852ade374e8633c9167549f6c6162717d2becb18a5e809a1', '\x115229b06194c0286f79fb21580b2263e2d59ec517716d8ccf9cbeddf62018861bd48432438c6502383c54849e24f36e20f7', '\xb1b3acaaa220b145f9e5d6d25d012ce190b18bf58b08d089f8939736eba764bbbba3bd3beb12aee5a0b47f7889692f2eabd2e85a7a2a8aeca1ef5b45523963f6f0ddafa0a0db30991ff8f88063bce5efc48deee2c26e30a1718393efb09ed0f70a5b1c392433c7000d43cdc65fea623cbd3cd6f11b4c54f4bc6b3eabb95aa171cd0ba48ca0035966612f156cde193f5a086cb9eb81a7b5ebdc199f1d400773c65070f5983e4790c14e7e294cd41133a05d09', NULL);


    --
    -- Data for Name: items_tags; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (1, 2, '\xd156f8c2eb75557c00e5144d6c4825583b92d70bdc6f5f252fb65eeaa98e15', '\x96efc9fb9fcf094eafc9e5eb8c050e1dbc9ac8ba20812deae3506e757bed24f5cc151e', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (2, 2, '\xa3a74e0139bd1329b01ced5c777e57f25f5ceacdbefead7e95a6e9aa69f440332f', '\x3d40ce0b8affef5707b762ff1dc809e55938bf9956b0b7e7cd23c506a11a31ef242689a17afa3ad7b2246ada45a10106aed326a84e6b9407dcce37b09fd70f9c3ef6fb4033520c', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (3, 3, '\x3f0851ac17768ebae22a093d4a5fb0b56056772b8b3b70e7fafe0edb08f9d06bb711', '\x2ea6094dc8454fcded0d7fbe0c4c9f8e0c42e9dfb1359f7001606f075b5f01', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (4, 3, '\x3e8c49a321525929e8a4435351fb5cfac23c1defe6702873762cef0ddc19c7cd2490', '\x45b255d3b8af3918b7b63f9ddf7f6456c6f0684cde9bec587adce81df319989890358559cda3ec31be680a787a742f3780e6e29fb510361d483f43aec341746fe79385188ee79138', 0);
    INSERT INTO public.items_tags (id, item_id, name, value, plaintext) VALUES (5, 3, '\xc504f1a45a5f2419e31b4ea49fae88348544b845d3a8e14d3f327ead70a2dbe18a0459ba644a75', '\x96efc9fb9fcf094eafc9e5eb8c050e1dbc9ac8ba20812deae3506e757bed24f5cc151e', 0);


    --
    -- Data for Name: profiles; Type: TABLE DATA; Schema: public; Owner: postgres
    --

    INSERT INTO public.profiles (id, name, reference, profile_key) VALUES (1, 'fb3750b8-c8d5-435e-935b-437cba233fb1', NULL, '\xa5e89b11fb8c9695db15d7d245e59c60ad368530aa75aaebe64eef5c76a605eb21b7951205c903ed0162a5e77ef58b3525fe27a898243f51b16891d1439b10fa3dc8b58176347480640cb016b737226ba3a42770ad23dbc1f77cc5763266cda2a1729f78e227fa046c2901e6f17d4102ddfbd302a6773930608381e4bb9d4659f37fcd3927b39fa9241ebb4aeed1fba4251f01072628906252dcb4994240b75e90610d0f23ffa862d7eadadb158807ab0e04ab6f7fa6a99465c10107bed8b0f0697ef8d2e755645ef5ee944f96b84f3a1fbde432a1dbb97544ca31a0356f12a7e40baa64c63f53ae60a58785dba5685e968adc9036d3bd82de52567b9e1099b84c5b01cdd354ee');


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

