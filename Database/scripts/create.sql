-- MySQL Workbench Forward Engineering

DROP SCHEMA IF EXISTS `intergrate-kp-2`;
-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema intergrate-kp-2
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema intergrate-kp-2
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `intergrate-kp-2` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `intergrate-kp-2`;

SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0;
SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0;
SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE =
        'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';
-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`board`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`board`
(
    `boardId`   VARCHAR(10)  NOT NULL,
    `boardName` VARCHAR(120) NOT NULL,
    `userId`    VARCHAR(36)  NULL,
    `visibility` ENUM('PRIVATE','PUBLIC') NOT NULL DEFAULT 'PRIVATE',
    PRIMARY KEY (`boardId`)
)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`limitsettings`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`limitsettings`
(
    `id`         INT         NOT NULL AUTO_INCREMENT,
    `limit`      INT         NOT NULL DEFAULT '10',
    `isEnable`   TINYINT(1)  NOT NULL DEFAULT '1',
    `ls_boardId` VARCHAR(10) NOT NULL,
    INDEX `boardId_idx` (`ls_boardId` ASC) VISIBLE,
    PRIMARY KEY (`id`),
    CONSTRAINT `ls_boardId`
        FOREIGN KEY (`ls_boardId`)
            REFERENCES `intergrate-kp-2`.`board` (`boardId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE
)
    ENGINE = InnoDB
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`status`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`status`
(
    `id`                INT          NOT NULL AUTO_INCREMENT,
    `statusName`        VARCHAR(50)  NOT NULL,
    `statusDescription` VARCHAR(200) NULL DEFAULT NULL,
    `createdOn`         DATETIME     NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedOn`         DATETIME     NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `st_boardId`        VARCHAR(10)  NOT NULL,
    PRIMARY KEY (`id` ,`statusName` , `st_boardId`),
    UNIQUE INDEX `statusName_UNIQUE` (`statusName` ASC , `st_boardId` ASC) VISIBLE,
    INDEX `boardId_idx` (`st_boardId` ASC) VISIBLE,
    CONSTRAINT `st_boardId`
        FOREIGN KEY (`st_boardId`)
            REFERENCES `intergrate-kp-2`.`board` (`boardId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE
)
    ENGINE = InnoDB
    AUTO_INCREMENT = 10
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`task`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`task`
(
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(100) NOT NULL,
    `description` VARCHAR(500) NULL     DEFAULT NULL,
    `assignees`   VARCHAR(30)  NULL     DEFAULT NULL,
    `status`      VARCHAR(50)  NOT NULL,
    `createdOn`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedOn`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `tk_boardId`  VARCHAR(10)  NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `status` (`status` ASC) VISIBLE,
    INDEX `boardId_idx` (`tk_boardId` ASC) VISIBLE,
    CONSTRAINT `task_ibfk_1`
        FOREIGN KEY (`status` , `tk_boardId`)
            REFERENCES `intergrate-kp-2`.`status` (`statusName` , `st_boardId`)
            ON DELETE RESTRICT
            ON UPDATE CASCADE,
    CONSTRAINT `tk_boardId`
        FOREIGN KEY (`tk_boardId`)
            REFERENCES `intergrate-kp-2`.`board` (`boardId`)
            ON DELETE CASCADE
            ON UPDATE CASCADE
)
    ENGINE = InnoDB
    AUTO_INCREMENT = 20
    DEFAULT CHARACTER SET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

DROP USER IF EXISTS 'dev'@'%';
CREATE USER 'dev'@'%' IDENTIFIED BY 'ip23kp2mysql';
GRANT ALL ON `intergrate-kp-2`.* TO 'dev'@'%';

SET SQL_MODE = @OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;
