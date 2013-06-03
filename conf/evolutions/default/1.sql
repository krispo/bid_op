# --- First database schema

# --- !Ups


CREATE TABLE "ActualBidHistory" (
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    bid double precision NOT NULL,
    bannerphrase_id bigint NOT NULL
);

CREATE TABLE "Banner" (
    geo character varying(128) NOT NULL,
    id bigint NOT NULL,
    network_banner_id character varying(128) NOT NULL
);

CREATE TABLE "BannerPhrase" (
    banner_id bigint NOT NULL,
    id bigint NOT NULL,
    phrase_id bigint NOT NULL,
    campaign_id bigint NOT NULL
);

CREATE TABLE "BannerPhrasePerformance" (
    cost_search double precision NOT NULL,
    visits integer NOT NULL,
    impress_context integer NOT NULL,
    impress_search integer NOT NULL,
    clicks_context integer NOT NULL,
    clicks_search integer NOT NULL,
    denial double precision NOT NULL,
    cost_context double precision NOT NULL,
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    periodtype_id bigint NOT NULL,
    bannerphrase_id bigint NOT NULL
);

CREATE TABLE "BannerPhrasePerformanceMetrika" (
    goal_reaches integer NOT NULL,
    visits integer NOT NULL,
    id bigint NOT NULL,
    visits_all integer NOT NULL,
    date timestamp without time zone NOT NULL,
    periodtype_id bigint NOT NULL,
    goal_id bigint NOT NULL,
    bannerphrase_id bigint NOT NULL,
    counter_id bigint NOT NULL
);

CREATE TABLE "BudgetHistory" (
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    budget double precision NOT NULL,
    campaign_id bigint NOT NULL
);

CREATE TABLE "Campaign" (
    _login character varying(128) NOT NULL,
    _token character varying(128) NOT NULL,
    network_id bigint NOT NULL,
    user_id bigint NOT NULL,
    network_campaign_id character varying(128) NOT NULL,
    id bigint NOT NULL,
    strategy character varying(128) NOT NULL,
    "_clientLogin" character varying(128) NOT NULL,
    start timestamp without time zone NOT NULL
);

CREATE TABLE "CampaignPerformance" (
    cost_search double precision NOT NULL,
    visits integer NOT NULL,
    impress_context integer NOT NULL,
    impress_search integer NOT NULL,
    clicks_context integer NOT NULL,
    clicks_search integer NOT NULL,
    denial double precision NOT NULL,
    cost_context double precision NOT NULL,
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    periodtype_id bigint NOT NULL,
    campaign_id bigint NOT NULL
);

CREATE TABLE "CampaignPerformanceMetrika" (
    goal_reaches integer NOT NULL,
    visits integer NOT NULL,
    id bigint NOT NULL,
    visits_all integer NOT NULL,
    date timestamp without time zone NOT NULL,
    periodtype_id bigint NOT NULL,
    goal_id bigint NOT NULL,
    campaign_id bigint NOT NULL,
    counter_id bigint NOT NULL
);

CREATE TABLE "CheckTime" (
    "dateDate" timestamp without time zone NOT NULL,
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    elem double precision NOT NULL
);

CREATE TABLE "Curve" (
    a double precision NOT NULL,
    id bigint NOT NULL,
    b double precision NOT NULL,
    date timestamp without time zone NOT NULL,
    c double precision NOT NULL,
    "optimalPermutation_id" bigint,
    campaign_id bigint NOT NULL,
    d double precision NOT NULL
);

CREATE TABLE "EndDateHistory" (
    "endDate" timestamp without time zone NOT NULL,
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    campaign_id bigint NOT NULL
);

CREATE TABLE "NetAdvisedBidHistory" (
    e double precision NOT NULL,
    f double precision NOT NULL,
    a double precision NOT NULL,
    id bigint NOT NULL,
    b double precision NOT NULL,
    date timestamp without time zone NOT NULL,
    c double precision NOT NULL,
    bannerphrase_id bigint NOT NULL,
    d double precision NOT NULL
);

CREATE TABLE "Network" (
    name character varying(128) NOT NULL,
    id bigint NOT NULL
);

CREATE TABLE "PeriodType" (
    description character varying(128) NOT NULL,
    factor double precision NOT NULL,
    id bigint NOT NULL
);

CREATE TABLE "Permutation" (
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    campaign_id bigint NOT NULL
);

CREATE TABLE "Phrase" (
    network_phrase_id character varying(128) NOT NULL,
    metrika_phrase_id character varying(128) NOT NULL,
    phrase character varying(1024) NOT NULL,
    id bigint NOT NULL
);

CREATE TABLE "Position" (
    permutation_id bigint NOT NULL,
    id bigint NOT NULL,
    "position" integer NOT NULL,
    bannerphrase_id bigint NOT NULL
);

CREATE TABLE "RecommendationChangeDate" (
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    campaign_id bigint NOT NULL
);

CREATE TABLE "RecommendationHistory" (
    id bigint NOT NULL,
    date timestamp without time zone NOT NULL,
    bid double precision NOT NULL,
    bannerphrase_id bigint NOT NULL
);

CREATE TABLE ad_user (
    name character varying(128) NOT NULL,
    id bigint NOT NULL,
    password character varying(128) NOT NULL
);

CREATE SEQUENCE "s_ActualBidHistory_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_BannerPhrasePerformanceMetrika_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_BannerPhrasePerformance_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_BannerPhrase_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_Banner_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_BudgetHistory_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_CampaignPerformanceMetrika_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_CampaignPerformance_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_Campaign_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_CheckTime_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_Curve_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_EndDateHistory_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_NetAdvisedBidHistory_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_Network_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_PeriodType_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_Permutation_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_Phrase_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_Position_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_RecommendationChangeDate_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE "s_RecommendationHistory_id"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE SEQUENCE s_ad_user_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER TABLE ONLY "ActualBidHistory"
    ADD CONSTRAINT "ActualBidHistory_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "BannerPhrasePerformanceMetrika"
    ADD CONSTRAINT "BannerPhrasePerformanceMetrika_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "BannerPhrasePerformance"
    ADD CONSTRAINT "BannerPhrasePerformance_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "BannerPhrase"
    ADD CONSTRAINT "BannerPhrase_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "Banner"
    ADD CONSTRAINT "Banner_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "BudgetHistory"
    ADD CONSTRAINT "BudgetHistory_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "CampaignPerformanceMetrika"
    ADD CONSTRAINT "CampaignPerformanceMetrika_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "CampaignPerformance"
    ADD CONSTRAINT "CampaignPerformance_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "Campaign"
    ADD CONSTRAINT "Campaign_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "CheckTime"
    ADD CONSTRAINT "CheckTime_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "Curve"
    ADD CONSTRAINT "Curve_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "EndDateHistory"
    ADD CONSTRAINT "EndDateHistory_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "NetAdvisedBidHistory"
    ADD CONSTRAINT "NetAdvisedBidHistory_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "Network"
    ADD CONSTRAINT "Network_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "PeriodType"
    ADD CONSTRAINT "PeriodType_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "Permutation"
    ADD CONSTRAINT "Permutation_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "Phrase"
    ADD CONSTRAINT "Phrase_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "Position"
    ADD CONSTRAINT "Position_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "RecommendationChangeDate"
    ADD CONSTRAINT "RecommendationChangeDate_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY "RecommendationHistory"
    ADD CONSTRAINT "RecommendationHistory_pkey" PRIMARY KEY (id);

ALTER TABLE ONLY ad_user
    ADD CONSTRAINT ad_user_pkey PRIMARY KEY (id);

CREATE UNIQUE INDEX idx1eb904b2 ON ad_user USING btree (name);
CREATE UNIQUE INDEX idx1ed504b9 ON "Network" USING btree (name);
CREATE UNIQUE INDEX idx7557098f ON "Banner" USING btree (network_banner_id);
CREATE UNIQUE INDEX idx774609a9 ON "Phrase" USING btree (network_phrase_id);
CREATE UNIQUE INDEX idx9f2b0b23 ON "Campaign" USING btree (network_campaign_id);

ALTER TABLE ONLY "ActualBidHistory"
    ADD CONSTRAINT "ActualBidHistoryFK15" FOREIGN KEY (bannerphrase_id) REFERENCES "BannerPhrase"(id);

ALTER TABLE ONLY "BannerPhrase"
    ADD CONSTRAINT "BannerPhraseFK10" FOREIGN KEY (banner_id) REFERENCES "Banner"(id);

ALTER TABLE ONLY "BannerPhrase"
    ADD CONSTRAINT "BannerPhraseFK11" FOREIGN KEY (phrase_id) REFERENCES "Phrase"(id);

ALTER TABLE ONLY "BannerPhrase"
    ADD CONSTRAINT "BannerPhraseFK8" FOREIGN KEY (campaign_id) REFERENCES "Campaign"(id);

ALTER TABLE ONLY "BannerPhrasePerformance"
    ADD CONSTRAINT "BannerPhrasePerformanceFK13" FOREIGN KEY (bannerphrase_id) REFERENCES "BannerPhrase"(id);

ALTER TABLE ONLY "BannerPhrasePerformance"
    ADD CONSTRAINT "BannerPhrasePerformanceFK19" FOREIGN KEY (periodtype_id) REFERENCES "PeriodType"(id);

ALTER TABLE ONLY "BannerPhrasePerformanceMetrika"
    ADD CONSTRAINT "BannerPhrasePerformanceMetrikaFK14" FOREIGN KEY (bannerphrase_id) REFERENCES "BannerPhrase"(id);

ALTER TABLE ONLY "BannerPhrasePerformanceMetrika"
    ADD CONSTRAINT "BannerPhrasePerformanceMetrikaFK20" FOREIGN KEY (periodtype_id) REFERENCES "PeriodType"(id);

ALTER TABLE ONLY "BudgetHistory"
    ADD CONSTRAINT "BudgetHistoryFK6" FOREIGN KEY (campaign_id) REFERENCES "Campaign"(id);

ALTER TABLE ONLY "Campaign"
    ADD CONSTRAINT "CampaignFK1" FOREIGN KEY (user_id) REFERENCES ad_user(id);

ALTER TABLE ONLY "Campaign"
    ADD CONSTRAINT "CampaignFK2" FOREIGN KEY (network_id) REFERENCES "Network"(id);

ALTER TABLE ONLY "CampaignPerformance"
    ADD CONSTRAINT "CampaignPerformanceFK21" FOREIGN KEY (periodtype_id) REFERENCES "PeriodType"(id);

ALTER TABLE ONLY "CampaignPerformance"
    ADD CONSTRAINT "CampaignPerformanceFK4" FOREIGN KEY (campaign_id) REFERENCES "Campaign"(id);

ALTER TABLE ONLY "CampaignPerformanceMetrika"
    ADD CONSTRAINT "CampaignPerformanceMetrikaFK22" FOREIGN KEY (periodtype_id) REFERENCES "PeriodType"(id);

ALTER TABLE ONLY "CampaignPerformanceMetrika"
    ADD CONSTRAINT "CampaignPerformanceMetrikaFK5" FOREIGN KEY (campaign_id) REFERENCES "Campaign"(id);

ALTER TABLE ONLY "Curve"
    ADD CONSTRAINT "CurveFK3" FOREIGN KEY (campaign_id) REFERENCES "Campaign"(id);

ALTER TABLE ONLY "EndDateHistory"
    ADD CONSTRAINT "EndDateHistoryFK7" FOREIGN KEY (campaign_id) REFERENCES "Campaign"(id);

ALTER TABLE ONLY "NetAdvisedBidHistory"
    ADD CONSTRAINT "NetAdvisedBidHistoryFK17" FOREIGN KEY (bannerphrase_id) REFERENCES "BannerPhrase"(id);

ALTER TABLE ONLY "Permutation"
    ADD CONSTRAINT "PermutationFK9" FOREIGN KEY (campaign_id) REFERENCES "Campaign"(id);

ALTER TABLE ONLY "Position"
    ADD CONSTRAINT "PositionFK12" FOREIGN KEY (bannerphrase_id) REFERENCES "BannerPhrase"(id);

ALTER TABLE ONLY "Position"
    ADD CONSTRAINT "PositionFK18" FOREIGN KEY (permutation_id) REFERENCES "Permutation"(id);

ALTER TABLE ONLY "RecommendationHistory"
    ADD CONSTRAINT "RecommendationHistoryFK16" FOREIGN KEY (bannerphrase_id) REFERENCES "BannerPhrase"(id);


# --- !Downs


ALTER TABLE ONLY "RecommendationHistory" DROP CONSTRAINT "RecommendationHistoryFK16";
ALTER TABLE ONLY "Position" DROP CONSTRAINT "PositionFK18";
ALTER TABLE ONLY "Position" DROP CONSTRAINT "PositionFK12";
ALTER TABLE ONLY "Permutation" DROP CONSTRAINT "PermutationFK9";
ALTER TABLE ONLY "NetAdvisedBidHistory" DROP CONSTRAINT "NetAdvisedBidHistoryFK17";
ALTER TABLE ONLY "EndDateHistory" DROP CONSTRAINT "EndDateHistoryFK7";
ALTER TABLE ONLY "Curve" DROP CONSTRAINT "CurveFK3";
ALTER TABLE ONLY "CampaignPerformanceMetrika" DROP CONSTRAINT "CampaignPerformanceMetrikaFK5";
ALTER TABLE ONLY "CampaignPerformanceMetrika" DROP CONSTRAINT "CampaignPerformanceMetrikaFK22";
ALTER TABLE ONLY "CampaignPerformance" DROP CONSTRAINT "CampaignPerformanceFK4";
ALTER TABLE ONLY "CampaignPerformance" DROP CONSTRAINT "CampaignPerformanceFK21";
ALTER TABLE ONLY "Campaign" DROP CONSTRAINT "CampaignFK2";
ALTER TABLE ONLY "Campaign" DROP CONSTRAINT "CampaignFK1";
ALTER TABLE ONLY "BudgetHistory" DROP CONSTRAINT "BudgetHistoryFK6";
ALTER TABLE ONLY "BannerPhrasePerformanceMetrika" DROP CONSTRAINT "BannerPhrasePerformanceMetrikaFK20";
ALTER TABLE ONLY "BannerPhrasePerformanceMetrika" DROP CONSTRAINT "BannerPhrasePerformanceMetrikaFK14";
ALTER TABLE ONLY "BannerPhrasePerformance" DROP CONSTRAINT "BannerPhrasePerformanceFK19";
ALTER TABLE ONLY "BannerPhrasePerformance" DROP CONSTRAINT "BannerPhrasePerformanceFK13";
ALTER TABLE ONLY "BannerPhrase" DROP CONSTRAINT "BannerPhraseFK8";
ALTER TABLE ONLY "BannerPhrase" DROP CONSTRAINT "BannerPhraseFK11";
ALTER TABLE ONLY "BannerPhrase" DROP CONSTRAINT "BannerPhraseFK10";
ALTER TABLE ONLY "ActualBidHistory" DROP CONSTRAINT "ActualBidHistoryFK15";

DROP INDEX idx9f2b0b23;
DROP INDEX idx774609a9;
DROP INDEX idx7557098f;
DROP INDEX idx1ed504b9;
DROP INDEX idx1eb904b2;

ALTER TABLE ONLY ad_user DROP CONSTRAINT ad_user_pkey;
ALTER TABLE ONLY "RecommendationHistory" DROP CONSTRAINT "RecommendationHistory_pkey";
ALTER TABLE ONLY "RecommendationChangeDate" DROP CONSTRAINT "RecommendationChangeDate_pkey";
ALTER TABLE ONLY "Position" DROP CONSTRAINT "Position_pkey";
ALTER TABLE ONLY "Phrase" DROP CONSTRAINT "Phrase_pkey";
ALTER TABLE ONLY "Permutation" DROP CONSTRAINT "Permutation_pkey";
ALTER TABLE ONLY "PeriodType" DROP CONSTRAINT "PeriodType_pkey";
ALTER TABLE ONLY "Network" DROP CONSTRAINT "Network_pkey";
ALTER TABLE ONLY "NetAdvisedBidHistory" DROP CONSTRAINT "NetAdvisedBidHistory_pkey";
ALTER TABLE ONLY "EndDateHistory" DROP CONSTRAINT "EndDateHistory_pkey";
ALTER TABLE ONLY "Curve" DROP CONSTRAINT "Curve_pkey";
ALTER TABLE ONLY "CheckTime" DROP CONSTRAINT "CheckTime_pkey";
ALTER TABLE ONLY "Campaign" DROP CONSTRAINT "Campaign_pkey";
ALTER TABLE ONLY "CampaignPerformance" DROP CONSTRAINT "CampaignPerformance_pkey";
ALTER TABLE ONLY "CampaignPerformanceMetrika" DROP CONSTRAINT "CampaignPerformanceMetrika_pkey";
ALTER TABLE ONLY "BudgetHistory" DROP CONSTRAINT "BudgetHistory_pkey";
ALTER TABLE ONLY "Banner" DROP CONSTRAINT "Banner_pkey";
ALTER TABLE ONLY "BannerPhrase" DROP CONSTRAINT "BannerPhrase_pkey";
ALTER TABLE ONLY "BannerPhrasePerformance" DROP CONSTRAINT "BannerPhrasePerformance_pkey";
ALTER TABLE ONLY "BannerPhrasePerformanceMetrika" DROP CONSTRAINT "BannerPhrasePerformanceMetrika_pkey";
ALTER TABLE ONLY "ActualBidHistory" DROP CONSTRAINT "ActualBidHistory_pkey";

DROP SEQUENCE s_ad_user_id;
DROP SEQUENCE "s_RecommendationHistory_id";
DROP SEQUENCE "s_RecommendationChangeDate_id";
DROP SEQUENCE "s_Position_id";
DROP SEQUENCE "s_Phrase_id";
DROP SEQUENCE "s_Permutation_id";
DROP SEQUENCE "s_PeriodType_id";
DROP SEQUENCE "s_Network_id";
DROP SEQUENCE "s_NetAdvisedBidHistory_id";
DROP SEQUENCE "s_EndDateHistory_id";
DROP SEQUENCE "s_Curve_id";
DROP SEQUENCE "s_CheckTime_id";
DROP SEQUENCE "s_Campaign_id";
DROP SEQUENCE "s_CampaignPerformance_id";
DROP SEQUENCE "s_CampaignPerformanceMetrika_id";
DROP SEQUENCE "s_BudgetHistory_id";
DROP SEQUENCE "s_Banner_id";
DROP SEQUENCE "s_BannerPhrase_id";
DROP SEQUENCE "s_BannerPhrasePerformance_id";
DROP SEQUENCE "s_BannerPhrasePerformanceMetrika_id";
DROP SEQUENCE "s_ActualBidHistory_id";

DROP TABLE ad_user;
DROP TABLE "RecommendationHistory";
DROP TABLE "RecommendationChangeDate";
DROP TABLE "Position";
DROP TABLE "Phrase";
DROP TABLE "Permutation";
DROP TABLE "PeriodType";
DROP TABLE "Network";
DROP TABLE "NetAdvisedBidHistory";
DROP TABLE "EndDateHistory";
DROP TABLE "Curve";
DROP TABLE "CheckTime";
DROP TABLE "CampaignPerformanceMetrika";
DROP TABLE "CampaignPerformance";
DROP TABLE "Campaign";
DROP TABLE "BudgetHistory";
DROP TABLE "BannerPhrasePerformanceMetrika";
DROP TABLE "BannerPhrasePerformance";
DROP TABLE "BannerPhrase";
DROP TABLE "Banner";
DROP TABLE "ActualBidHistory";