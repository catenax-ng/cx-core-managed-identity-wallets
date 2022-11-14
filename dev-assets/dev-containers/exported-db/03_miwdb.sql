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

DROP DATABASE postgres;
--
-- Name: postgres; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';


ALTER DATABASE postgres OWNER TO postgres;

\connect postgres

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
-- Name: DATABASE postgres; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON DATABASE postgres IS 'default administrative connection database';


--
-- Name: public; Type: SCHEMA; Schema: -; Owner: postgres
--

-- *not* creating schema, since initdb creates it


ALTER SCHEMA public OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: connections; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.connections (
    id integer NOT NULL,
    connection_id character varying(4096) NOT NULL,
    their_did character varying(4096) NOT NULL,
    my_did character varying(4096) NOT NULL,
    state character varying(4096) NOT NULL
);


ALTER TABLE public.connections OWNER TO postgres;

--
-- Name: connections_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.connections_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.connections_id_seq OWNER TO postgres;

--
-- Name: connections_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.connections_id_seq OWNED BY public.connections.id;


--
-- Name: entry_counter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.entry_counter (
    list_name character varying(255) NOT NULL,
    last_idx bigint NOT NULL
);


ALTER TABLE public.entry_counter OWNER TO postgres;

--
-- Name: list_entry; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.list_entry (
    list_name character varying(255) NOT NULL,
    index bigint NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    revoked boolean DEFAULT false NOT NULL,
    revoked_at timestamp with time zone,
    processed_to_list boolean DEFAULT false NOT NULL
);


ALTER TABLE public.list_entry OWNER TO postgres;

--
-- Name: lists; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.lists (
    list_name character varying(255) NOT NULL,
    profile_name character varying(255),
    encoded_list character varying(1048576),
    list_credential character varying(1048576),
    last_update timestamp with time zone DEFAULT '1970-01-01 00:00:00+00'::timestamp with time zone NOT NULL
);


ALTER TABLE public.lists OWNER TO postgres;

--
-- Name: scheduled_tasks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.scheduled_tasks (
    task_name text NOT NULL,
    task_instance text NOT NULL,
    task_data bytea,
    execution_time timestamp without time zone NOT NULL,
    picked boolean NOT NULL,
    picked_by text,
    last_success timestamp without time zone,
    last_failure timestamp without time zone,
    consecutive_failures integer,
    last_heartbeat timestamp without time zone,
    version bigint NOT NULL
);


ALTER TABLE public.scheduled_tasks OWNER TO postgres;

--
-- Name: verifiable_credentials; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.verifiable_credentials (
    id integer NOT NULL,
    content text NOT NULL,
    credential_id character varying(4096),
    issuer_did character varying(4096) NOT NULL,
    holder_did character varying(4096) NOT NULL,
    type character varying(4096) NOT NULL,
    wallet_id integer NOT NULL
);


ALTER TABLE public.verifiable_credentials OWNER TO postgres;

--
-- Name: verifiable_credentials_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.verifiable_credentials_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.verifiable_credentials_id_seq OWNER TO postgres;

--
-- Name: verifiable_credentials_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.verifiable_credentials_id_seq OWNED BY public.verifiable_credentials.id;


--
-- Name: wallets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.wallets (
    id integer NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    name character varying(127) NOT NULL,
    bpn character varying(36) NOT NULL,
    did character varying(4096) NOT NULL,
    wallet_id character varying(4096),
    wallet_key character varying(4096),
    wallet_token character varying(4096),
    revocation_list_name character varying(4096),
    pending_membership_issuance boolean DEFAULT false NOT NULL
);


ALTER TABLE public.wallets OWNER TO postgres;

--
-- Name: wallets_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.wallets_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.wallets_id_seq OWNER TO postgres;

--
-- Name: wallets_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.wallets_id_seq OWNED BY public.wallets.id;


--
-- Name: webhooks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.webhooks (
    id integer NOT NULL,
    thread_id character varying(4096) NOT NULL,
    webhook_url character varying(4096) NOT NULL,
    state character varying(4096) NOT NULL
);


ALTER TABLE public.webhooks OWNER TO postgres;

--
-- Name: webhooks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.webhooks_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.webhooks_id_seq OWNER TO postgres;

--
-- Name: webhooks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.webhooks_id_seq OWNED BY public.webhooks.id;


--
-- Name: connections id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.connections ALTER COLUMN id SET DEFAULT nextval('public.connections_id_seq'::regclass);


--
-- Name: verifiable_credentials id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verifiable_credentials ALTER COLUMN id SET DEFAULT nextval('public.verifiable_credentials_id_seq'::regclass);


--
-- Name: wallets id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallets ALTER COLUMN id SET DEFAULT nextval('public.wallets_id_seq'::regclass);


--
-- Name: webhooks id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.webhooks ALTER COLUMN id SET DEFAULT nextval('public.webhooks_id_seq'::regclass);


--
-- Data for Name: connections; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: entry_counter; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.entry_counter (list_name, last_idx) VALUES ('41ba891e-dbfa-4841-b14f-97d37365f789', -1);
INSERT INTO public.entry_counter (list_name, last_idx) VALUES ('81bf5e58-90ce-432a-a7ff-e5cdf1619288', -1);
INSERT INTO public.entry_counter (list_name, last_idx) VALUES ('0d58e0f0-6876-4f40-b6e9-7a8391a9f02e', 1);


--
-- Data for Name: list_entry; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.list_entry (list_name, index, created_at, revoked, revoked_at, processed_to_list) VALUES ('0d58e0f0-6876-4f40-b6e9-7a8391a9f02e', 0, '2022-11-10 11:15:13.212561+00', false, NULL, false);
INSERT INTO public.list_entry (list_name, index, created_at, revoked, revoked_at, processed_to_list) VALUES ('0d58e0f0-6876-4f40-b6e9-7a8391a9f02e', 1, '2022-11-10 11:15:31.727448+00', false, NULL, false);


--
-- Data for Name: lists; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.lists (list_name, profile_name, encoded_list, list_credential, last_update) VALUES ('0d58e0f0-6876-4f40-b6e9-7a8391a9f02e', 'TrYVeQ23QWHSGm5n3vWQva', 'H4sIAAAAAAAAAO3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA', '{"id":"http://localhost:8080/api/credentials/status/0d58e0f0-6876-4f40-b6e9-7a8391a9f02e","@context":["https://www.w3.org/2018/credentials/v1","https://w3id.org/vc/status-list/2021/v1"],"type":["VerifiableCredential","StatusList2021Credential"],"issuer":"did:sov:TrYVeQ23QWHSGm5n3vWQva","issuanceDate":"2022-11-10T11:13:37Z","credentialSubject":{"id":"http://localhost:8080/api/credentials/status/0d58e0f0-6876-4f40-b6e9-7a8391a9f02e#list","type":"StatusList2021","statusPurpose":"revocation","encodedList":"H4sIAAAAAAAAAO3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA"},"proof":{"type":"Ed25519Signature2018","created":"2022-11-10T11:13:38Z","proofPurpose":"assertionMethod","verificationMethod":"did:sov:TrYVeQ23QWHSGm5n3vWQva#key-1","jws":"eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..UBCda3h3lIIdi-Bfa1XUokGyEda0_OR8YCXLG9kTV_7PpYi3k1u1yREgMdQjWIYHsgc19MID9BWsI55-yVlBBw"}}', '2022-11-10 11:13:37.209626+00');
INSERT INTO public.lists (list_name, profile_name, encoded_list, list_credential, last_update) VALUES ('41ba891e-dbfa-4841-b14f-97d37365f789', 'LXN15K33DXGN7MDB9QvFb1', 'H4sIAAAAAAAAAO3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA', '{"id":"http://localhost:8080/api/credentials/status/41ba891e-dbfa-4841-b14f-97d37365f789","@context":["https://www.w3.org/2018/credentials/v1","https://w3id.org/vc/status-list/2021/v1"],"type":["VerifiableCredential","StatusList2021Credential"],"issuer":"did:sov:LXN15K33DXGN7MDB9QvFb1","issuanceDate":"2022-11-10T11:15:12Z","credentialSubject":{"id":"http://localhost:8080/api/credentials/status/41ba891e-dbfa-4841-b14f-97d37365f789#list","type":"StatusList2021","statusPurpose":"revocation","encodedList":"H4sIAAAAAAAAAO3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA"},"proof":{"type":"Ed25519Signature2018","created":"2022-11-10T11:15:12Z","proofPurpose":"assertionMethod","verificationMethod":"did:sov:LXN15K33DXGN7MDB9QvFb1#key-1","jws":"eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..uCQ7_2caGre-9X-DG2zT0IJdzGFX2Rky0opIUt4hTZr98QEZ_3iptxl3mjeExLjjKkbP3nwbZaBSgF9PrwD2DQ"}}', '2022-11-10 11:15:12.441121+00');
INSERT INTO public.lists (list_name, profile_name, encoded_list, list_credential, last_update) VALUES ('81bf5e58-90ce-432a-a7ff-e5cdf1619288', 'SFz2A3jZP5ciaAVdmEQJEP', 'H4sIAAAAAAAAAO3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA', '{"id":"http://localhost:8080/api/credentials/status/81bf5e58-90ce-432a-a7ff-e5cdf1619288","@context":["https://www.w3.org/2018/credentials/v1","https://w3id.org/vc/status-list/2021/v1"],"type":["VerifiableCredential","StatusList2021Credential"],"issuer":"did:sov:SFz2A3jZP5ciaAVdmEQJEP","issuanceDate":"2022-11-10T11:15:31Z","credentialSubject":{"id":"http://localhost:8080/api/credentials/status/81bf5e58-90ce-432a-a7ff-e5cdf1619288#list","type":"StatusList2021","statusPurpose":"revocation","encodedList":"H4sIAAAAAAAAAO3BMQEAAADCoPVPbQwfoAAAAAAAAAAAAAAAAAAAAIC3AYbSVKsAQAAA"},"proof":{"type":"Ed25519Signature2018","created":"2022-11-10T11:15:31Z","proofPurpose":"assertionMethod","verificationMethod":"did:sov:SFz2A3jZP5ciaAVdmEQJEP#key-1","jws":"eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..tz2_IvF7nt4VPY55Q-yJDSGKPCJijbK3Wzf45IsEzeCSTJiEHC1NSuE-sSCH0qYborQkq95hg3TCpcTe2-A7Cg"}}', '2022-11-10 11:15:31.141537+00');


--
-- Data for Name: scheduled_tasks; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.scheduled_tasks (task_name, task_instance, task_data, execution_time, picked, picked_by, last_success, last_failure, consecutive_failures, last_heartbeat, version) VALUES ('revocation-list-update', 'recurring', NULL, '2022-11-11 03:00:00', false, NULL, NULL, NULL, NULL, NULL, 1);


--
-- Data for Name: verifiable_credentials; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.verifiable_credentials (id, content, credential_id, issuer_did, holder_did, type, wallet_id) VALUES (1, '{"id":"ae5ebf51-a11f-4486-a410-c61094c8a01f","@context":["https://www.w3.org/2018/credentials/v1","https://raw.githubusercontent.com/catenax-ng/product-core-schemas/main/businessPartnerData"],"type":["BpnCredential","VerifiableCredential"],"issuer":"did:sov:TrYVeQ23QWHSGm5n3vWQva","issuanceDate":"2022-11-10T11:15:13Z","credentialSubject":{"type":["BpnCredential"],"bpn":"BPNL000000000001","id":"did:sov:LXN15K33DXGN7MDB9QvFb1"},"credentialStatus":null,"proof":{"type":"Ed25519Signature2018","created":"2022-11-10T11:15:14Z","proofPurpose":"assertionMethod","verificationMethod":"did:sov:TrYVeQ23QWHSGm5n3vWQva#key-1","jws":"eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..CgGjx73hrVWo4YgEiaarr4_kgNr8LvXka_U1krNC-WSVVQGX0o2aPmM018BJsoESsr_Tdzx-ZFsS7vynjorGCg"}}', 'ae5ebf51-a11f-4486-a410-c61094c8a01f', 'did:sov:TrYVeQ23QWHSGm5n3vWQva', 'did:sov:LXN15K33DXGN7MDB9QvFb1', 'BpnCredential', 2);
INSERT INTO public.verifiable_credentials (id, content, credential_id, issuer_did, holder_did, type, wallet_id) VALUES (2, '{"id":"1f0db9f2-1d8f-4105-af61-7200f6a768e0","@context":["https://www.w3.org/2018/credentials/v1","https://raw.githubusercontent.com/catenax-ng/product-core-schemas/main/businessPartnerData","https://w3id.org/vc/status-list/2021/v1"],"type":["MembershipCredential","VerifiableCredential"],"issuer":"did:sov:TrYVeQ23QWHSGm5n3vWQva","issuanceDate":"2022-11-10T11:15:13Z","credentialSubject":{"type":["MembershipCredential"],"memberOf":"Catena-X","status":"Active","startTime":"2022-11-10T11:15:13Z","id":"did:sov:LXN15K33DXGN7MDB9QvFb1"},"credentialStatus":{"id":"http://localhost:8080/api/credentials/status/0d58e0f0-6876-4f40-b6e9-7a8391a9f02e#0","statusListIndex":"0","statusListCredential":"http://localhost:8080/api/credentials/status/0d58e0f0-6876-4f40-b6e9-7a8391a9f02e"},"proof":{"type":"Ed25519Signature2018","created":"2022-11-10T11:15:14Z","proofPurpose":"assertionMethod","verificationMethod":"did:sov:TrYVeQ23QWHSGm5n3vWQva#key-1","jws":"eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..hv32u393jWf-nfEQ9c1Lr-mL1P6FfyFvIM92vzUcAab2dr_FSGTa0g6aaQzzJlKTKtWyngYOJOUoC1bpt2IUDQ"}}', '1f0db9f2-1d8f-4105-af61-7200f6a768e0', 'did:sov:TrYVeQ23QWHSGm5n3vWQva', 'did:sov:LXN15K33DXGN7MDB9QvFb1', 'MembershipCredential', 2);
INSERT INTO public.verifiable_credentials (id, content, credential_id, issuer_did, holder_did, type, wallet_id) VALUES (3, '{"id":"a8c262f9-0aa9-4b56-84d5-875c0b4b0dd0","@context":["https://www.w3.org/2018/credentials/v1","https://raw.githubusercontent.com/catenax-ng/product-core-schemas/main/businessPartnerData"],"type":["BpnCredential","VerifiableCredential"],"issuer":"did:sov:TrYVeQ23QWHSGm5n3vWQva","issuanceDate":"2022-11-10T11:15:31Z","credentialSubject":{"type":["BpnCredential"],"bpn":"BPNL000000000002","id":"did:sov:SFz2A3jZP5ciaAVdmEQJEP"},"credentialStatus":null,"proof":{"type":"Ed25519Signature2018","created":"2022-11-10T11:15:32Z","proofPurpose":"assertionMethod","verificationMethod":"did:sov:TrYVeQ23QWHSGm5n3vWQva#key-1","jws":"eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..lIzzzB_XsMFyRxP-ofw4EbM2Q7kvKvqa-J8qG8t7byexDHykHydsraU2JhlhKjYbL6E6Dyh2mlxG-AfnfWMXDQ"}}', 'a8c262f9-0aa9-4b56-84d5-875c0b4b0dd0', 'did:sov:TrYVeQ23QWHSGm5n3vWQva', 'did:sov:SFz2A3jZP5ciaAVdmEQJEP', 'BpnCredential', 3);
INSERT INTO public.verifiable_credentials (id, content, credential_id, issuer_did, holder_did, type, wallet_id) VALUES (4, '{"id":"9b29121c-dc4b-43d4-aeaa-32cbf87efb45","@context":["https://www.w3.org/2018/credentials/v1","https://raw.githubusercontent.com/catenax-ng/product-core-schemas/main/businessPartnerData","https://w3id.org/vc/status-list/2021/v1"],"type":["MembershipCredential","VerifiableCredential"],"issuer":"did:sov:TrYVeQ23QWHSGm5n3vWQva","issuanceDate":"2022-11-10T11:15:31Z","credentialSubject":{"type":["MembershipCredential"],"memberOf":"Catena-X","status":"Active","startTime":"2022-11-10T11:15:31Z","id":"did:sov:SFz2A3jZP5ciaAVdmEQJEP"},"credentialStatus":{"id":"http://localhost:8080/api/credentials/status/0d58e0f0-6876-4f40-b6e9-7a8391a9f02e#1","statusListIndex":"1","statusListCredential":"http://localhost:8080/api/credentials/status/0d58e0f0-6876-4f40-b6e9-7a8391a9f02e"},"proof":{"type":"Ed25519Signature2018","created":"2022-11-10T11:15:32Z","proofPurpose":"assertionMethod","verificationMethod":"did:sov:TrYVeQ23QWHSGm5n3vWQva#key-1","jws":"eyJhbGciOiAiRWREU0EiLCAiYjY0IjogZmFsc2UsICJjcml0IjogWyJiNjQiXX0..itUFgoYRmPCFvLg07z-zzi0gL0UrIPZ8owkW7DDP0FsjeJZHs1UcVAVZPmB141jLKSaUYhtxYlk9EnRieMLLDw"}}', '9b29121c-dc4b-43d4-aeaa-32cbf87efb45', 'did:sov:TrYVeQ23QWHSGm5n3vWQva', 'did:sov:SFz2A3jZP5ciaAVdmEQJEP', 'MembershipCredential', 3);


--
-- Data for Name: wallets; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO public.wallets (id, created_at, modified_at, name, bpn, did, wallet_id, wallet_key, wallet_token, revocation_list_name, pending_membership_issuance) VALUES (1, '2022-11-10 12:12:38.187292', '2022-11-10 12:12:38.246426', 'Trusted_Provider', 'BPNL000000000000', 'did:sov:TrYVeQ23QWHSGm5n3vWQva', '38c88bd5-0129-4003-9b0d-4b15266e374f', '6eFfasjjxqwISKY1RxCFiDRVH', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ3YWxsZXRfaWQiOiIzOGM4OGJkNS0wMTI5LTQwMDMtOWIwZC00YjE1MjY2ZTM3NGYiLCJpYXQiOjE2NjgwNzg3NTd9.4JRMuPUdu5ydtTn3xEO5tNv0ouTL-nsfJUYGsDjLlv4', '0d58e0f0-6876-4f40-b6e9-7a8391a9f02e', true);
INSERT INTO public.wallets (id, created_at, modified_at, name, bpn, did, wallet_id, wallet_key, wallet_token, revocation_list_name, pending_membership_issuance) VALUES (2, '2022-11-10 12:15:12.364908', '2022-11-10 12:15:12.417415', 'ZF_Wallet', 'BPNL000000000001', 'did:sov:LXN15K33DXGN7MDB9QvFb1', 'b117fabf-7155-4817-944a-510d215dc288', '6XFspooWDqdLvP29pUGURvGaB', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ3YWxsZXRfaWQiOiJiMTE3ZmFiZi03MTU1LTQ4MTctOTQ0YS01MTBkMjE1ZGMyODgiLCJpYXQiOjE2NjgwNzg5MTF9.i2vLnTeNRA0_DTkXjgbUYadUiP87dsryABT7gYodpRM', '41ba891e-dbfa-4841-b14f-97d37365f789', true);
INSERT INTO public.wallets (id, created_at, modified_at, name, bpn, did, wallet_id, wallet_key, wallet_token, revocation_list_name, pending_membership_issuance) VALUES (3, '2022-11-10 12:15:31.093204', '2022-11-10 12:15:31.12507', 'BMW_Wallet', 'BPNL000000000002', 'did:sov:SFz2A3jZP5ciaAVdmEQJEP', '45d6959d-a4d4-41e1-85c3-1a4ea13eb088', 'f1RImkZrGbMZFad8B8FF2t2e2', 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ3YWxsZXRfaWQiOiI0NWQ2OTU5ZC1hNGQ0LTQxZTEtODVjMy0xYTRlYTEzZWIwODgiLCJpYXQiOjE2NjgwNzg5MzB9.x4gUSg3GQCjxWSqnv6AHT9epo-PVyjstntyXJ-yVybU', '81bf5e58-90ce-432a-a7ff-e5cdf1619288', true);


--
-- Data for Name: webhooks; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Name: connections_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.connections_id_seq', 1, false);


--
-- Name: verifiable_credentials_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.verifiable_credentials_id_seq', 4, true);


--
-- Name: wallets_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.wallets_id_seq', 3, true);


--
-- Name: webhooks_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.webhooks_id_seq', 1, false);


--
-- Name: wallets bpn; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT bpn UNIQUE (bpn);


--
-- Name: connections connectionid; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.connections
    ADD CONSTRAINT connectionid UNIQUE (connection_id);


--
-- Name: connections connections_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.connections
    ADD CONSTRAINT connections_pkey PRIMARY KEY (id);


--
-- Name: verifiable_credentials content; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verifiable_credentials
    ADD CONSTRAINT content UNIQUE (content);


--
-- Name: verifiable_credentials credentialid; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verifiable_credentials
    ADD CONSTRAINT credentialid UNIQUE (credential_id);


--
-- Name: wallets did; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT did UNIQUE (did);


--
-- Name: entry_counter entry_counter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entry_counter
    ADD CONSTRAINT entry_counter_pkey PRIMARY KEY (list_name);


--
-- Name: list_entry list_entry_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_entry
    ADD CONSTRAINT list_entry_pkey PRIMARY KEY (list_name, index);


--
-- Name: lists lists_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lists
    ADD CONSTRAINT lists_pkey PRIMARY KEY (list_name);


--
-- Name: lists lists_profile_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.lists
    ADD CONSTRAINT lists_profile_name_key UNIQUE (profile_name);


--
-- Name: scheduled_tasks pk_scheduler_tasks; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.scheduled_tasks
    ADD CONSTRAINT pk_scheduler_tasks PRIMARY KEY (task_name, task_instance);


--
-- Name: webhooks threadid; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.webhooks
    ADD CONSTRAINT threadid UNIQUE (thread_id);


--
-- Name: verifiable_credentials verifiable_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verifiable_credentials
    ADD CONSTRAINT verifiable_credentials_pkey PRIMARY KEY (id);


--
-- Name: wallets wallets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wallets
    ADD CONSTRAINT wallets_pkey PRIMARY KEY (id);


--
-- Name: webhooks webhooks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.webhooks
    ADD CONSTRAINT webhooks_pkey PRIMARY KEY (id);


--
-- Name: idx_list_entry_processed; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_list_entry_processed ON public.list_entry USING btree (list_name, processed_to_list, revoked) WHERE ((processed_to_list IS FALSE) AND (revoked IS TRUE));


--
-- Name: entry_counter entry_counter_list_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.entry_counter
    ADD CONSTRAINT entry_counter_list_name_fkey FOREIGN KEY (list_name) REFERENCES public.lists(list_name);


--
-- Name: verifiable_credentials fk_verifiable_credentials_wallet_id__id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.verifiable_credentials
    ADD CONSTRAINT fk_verifiable_credentials_wallet_id__id FOREIGN KEY (wallet_id) REFERENCES public.wallets(id) ON UPDATE RESTRICT ON DELETE RESTRICT;


--
-- Name: list_entry list_entry_list_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.list_entry
    ADD CONSTRAINT list_entry_list_name_fkey FOREIGN KEY (list_name) REFERENCES public.lists(list_name);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE USAGE ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

