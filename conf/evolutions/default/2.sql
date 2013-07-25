# Update Phrase
 
# --- !Ups

ALTER TABLE "Phrase" ADD stats BIGINT;
 

# --- !Downs

ALTER TABLE "Phrase" DROP stats;