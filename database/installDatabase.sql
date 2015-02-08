CREATE DATABASE PublicationMetaSearchEngine;
USE PublicationMetaSearchEngine;

CREATE TABLE `user` (
    `id`        INT         NOT NULL AUTO_INCREMENT,
    `login`     VARCHAR(32) NOT NULL,
    `passwd`    VARCHAR(32) NOT NULL,
    `name`      VARCHAR(32) DEFAULT NULL,
    `surname`   VARCHAR(32) DEFAULT NULL,
    `email`     VARCHAR(64) NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `login` (`login`)
);


CREATE TABLE `publication` (
    `id`                INT             NOT NULL AUTO_INCREMENT,
    `sourceDBId`        INT             NOT NULL,
    `articleId`         VARCHAR(128)    NOT NULL,
    `mainAuthorId`      INT             NOT NULL,
    `title`             VARCHAR(767)    NOT NULL,
    `summary`           VARCHAR(10240)  NOT NULL,
    `doi`               VARCHAR(128)    DEFAULT NULL,
    `journalRef`        VARCHAR(256)    DEFAULT NULL,
    `sourceTitleId`     INT             NOT NULL,
    `sourceVolume`      VARCHAR(16)     DEFAULT NULL,
    `sourceIssue`       VARCHAR(16)     DEFAULT NULL,
    `sourcePageRange`   VARCHAR(16)     DEFAULT NULL,
    `publicationDate`   DATETIME        NOT NULL,
    `pdfLink`           VARCHAR(1024)   DEFAULT NULL,
    `insertDate`        DATETIME        NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `titleInDb` (`sourceDbId`, `title`),
    UNIQUE KEY `articleInDb` (`sourceDbId`,`articleId`)
);

CREATE TABLE `author` (
    `id`        INT         NOT NULL AUTO_INCREMENT,
    `name`      VARCHAR(64) NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `authorName` (`name`)
)CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE TABLE `publicationAuthors` (
    `publicationId` INT NOT NULL,
    `authorId`      INT NOT NULL,

    UNIQUE KEY (`publicationId`,`authorId`)
);

CREATE TABLE `sourceTitle` (
    `id`        INT          NOT NULL AUTO_INCREMENT,
    `title`     VARCHAR(256) NOT NULL,

    PRIMARY KEY (`id`),
    UNIQUE KEY `sTitle` (`title`)
);

CREATE TABLE `sourceDb` (
    `id`        INT         NOT NULL AUTO_INCREMENT,
    `fullName`  VARCHAR(32) NOT NULL,
    `shortName` VARCHAR(8) NOT NULL,

    PRIMARY KEY (`id`)
);
INSERT INTO `sourceDb` (`fullName`, `shortName`) VALUES ("Arxiv", "Arxiv");                    -- ID: 1
INSERT INTO `sourceDb` (`fullName`, `shortName`) VALUES ("Web of Knowledge", "WOK");           -- ID: 2
INSERT INTO `sourceDb` (`fullName`, `shortName`) VALUES ("Biblioteka Wirtualna Nauki", "BWN"); -- ID: 3

CREATE TABLE `userPublications` (
    `IdU`           INT NOT NULL,
    `IdP`           INT NOT NULL,
    `insertDate`    DATETIME NOT NULL,

    UNIQUE KEY `userPublicatio` (`IdU`,`IdP`)
);

ALTER TABLE userPublications ADD CONSTRAINT userPublicationRelation FOREIGN KEY (IdU) REFERENCES USER(id) ON DELETE CASCADE;

CREATE TABLE `filterCriterias` (
    `IdF`           INT NOT NULL AUTO_INCREMENT,
    `IdU`           INT NOT NULL,
    `filters`       LONGTEXT NOT NULL,
    `lastSearchDate`DATETIME NOT NULL,

    PRIMARY KEY (`IdF`)
);

ALTER TABLE filterCriterias ADD CONSTRAINT userFilterCriteriasRelation FOREIGN KEY (IdU) REFERENCES USER(id) ON DELETE CASCADE;
