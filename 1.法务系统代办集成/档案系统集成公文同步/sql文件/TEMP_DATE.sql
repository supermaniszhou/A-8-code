
CREATE TABLE "TEMP_DATE" (
"ID" NUMBER(18) NOT NULL ,
"STARTDATE" VARCHAR2(50 BYTE) NULL ,
"ENDDATE" VARCHAR2(50 BYTE) NULL 
);


ALTER TABLE "TEMP_DATE" ADD CHECK ("ID" IS NOT NULL);


ALTER TABLE "TEMP_DATE" ADD PRIMARY KEY ("ID");
