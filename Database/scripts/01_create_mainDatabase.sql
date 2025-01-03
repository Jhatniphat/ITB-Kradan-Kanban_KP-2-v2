-- MySQL Workbench Forward Engineering
DROP SCHEMA IF EXISTS `intergrate-kp-2`;
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema intergrate-kp-2
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema intergrate-kp-2
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `intergrate-kp-2` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE `intergrate-kp-2` ;

-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`board`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`board` (
  `boardId` VARCHAR(10) NOT NULL,
  `boardName` VARCHAR(120) NOT NULL,
  `userId` VARCHAR(36) NULL DEFAULT NULL,
  `visibility` ENUM('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PRIVATE',
  `createdOn` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`boardId`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`status`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`status` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `statusName` VARCHAR(50) NOT NULL,
  `statusDescription` VARCHAR(200) NULL DEFAULT NULL,
  `createdOn` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `color` VARCHAR(6) NULL,
  `st_boardId` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`id`, `statusName`, `st_boardId`),
  UNIQUE INDEX `statusName_UNIQUE` (`statusName` ASC, `st_boardId` ASC) VISIBLE,
  INDEX `boardId_idx` (`st_boardId` ASC) VISIBLE,
  CONSTRAINT `st_boardId`
    FOREIGN KEY (`st_boardId`)
    REFERENCES `intergrate-kp-2`.`board` (`boardId`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 326
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`task`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`task` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) NULL DEFAULT NULL,
  `assignees` VARCHAR(30) NULL DEFAULT NULL,
  `status` VARCHAR(50) NOT NULL,
  `createdOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `tk_boardId` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `status` (`status` ASC) VISIBLE,
  INDEX `boardId_idx` (`tk_boardId` ASC) VISIBLE,
  INDEX `task_ibfk_1` (`status` ASC, `tk_boardId` ASC) VISIBLE,
  CONSTRAINT `task_ibfk_1`
    FOREIGN KEY (`status` , `tk_boardId`)
    REFERENCES `intergrate-kp-2`.`status` (`statusName` , `st_boardId`)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT `tk_boardId`
    FOREIGN KEY (`tk_boardId`)
    REFERENCES `intergrate-kp-2`.`board` (`boardId`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 21
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`attachment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`attachment` (
  `attachment_id` INT NOT NULL AUTO_INCREMENT,
  `fileName` VARCHAR(255) NULL DEFAULT NULL,
  `fileType` VARCHAR(255) NULL DEFAULT NULL,
  `fileData` LONGBLOB NULL DEFAULT NULL,
  `uploadedOn` DATETIME NULL DEFAULT NULL,
  `task_id` INT NOT NULL,
  PRIMARY KEY (`attachment_id`),
  INDEX `fk_attachment_task1_idx` (`task_id` ASC) VISIBLE,
  CONSTRAINT `fk_attachment_task1`
    FOREIGN KEY (`task_id`)
    REFERENCES `intergrate-kp-2`.`task` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci;


-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`collab`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`collab` (
  `board_boardId` VARCHAR(10) NOT NULL,
  `userId` VARCHAR(36) NOT NULL,
  `added_On` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `accessRight` ENUM('READ', 'WRITE') NULL DEFAULT 'READ',
  `status` ENUM('PENDING', 'ACCEPTED') NULL DEFAULT 'PENDING',
  PRIMARY KEY (`board_boardId`, `userId`),
  INDEX `fk_collab_board1_idx` (`board_boardId` ASC) VISIBLE,
  CONSTRAINT `fk_collab_board1`
    FOREIGN KEY (`board_boardId`)
    REFERENCES `intergrate-kp-2`.`board` (`boardId`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `intergrate-kp-2`.`limitsettings`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`limitsettings` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `limit` INT NOT NULL DEFAULT '10',
  `isEnable` TINYINT(1) NOT NULL DEFAULT '1',
  `ls_boardId` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `boardId_idx` (`ls_boardId` ASC) VISIBLE,
  CONSTRAINT `ls_boardId`
    FOREIGN KEY (`ls_boardId`)
    REFERENCES `intergrate-kp-2`.`board` (`boardId`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 80
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
