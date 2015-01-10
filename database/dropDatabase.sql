ALTER TABLE userPublications DROP FOREIGN KEY userPublicationRelation;
ALTER TABLE filterCriterias DROP FOREIGN KEY userFilterCriteriasRelation;


DROP TABLE `filterCriterias`;
DROP TABLE `userPublications`;
DROP TABLE `sourceDb`;
DROP TABLE `sourceTitle`;
DROP TABLE `publicationAuthors`;
DROP TABLE `author`;
DROP TABLE `publication`;
DROP TABLE `user`;
DROP DATABASE `PublicationMetaSearchEngine`;