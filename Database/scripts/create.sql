set character_set_client='utf8';
set character_set_connection='utf8';
set character_set_database='utf8';
set character_set_results='utf8';
set character_set_server='utf8';

DROP SCHEMA IF EXISTS `intergrate-kp-2` ;
CREATE SCHEMA IF NOT EXISTS `intergrate-kp-2` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `intergrate-kp-2` ;

-- Table `intergrate-kp-2`.`status`
DROP TABLE IF EXISTS `intergrate-kp-2`.`status` ;
CREATE TABLE `intergrate-kp-2`.`status` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `statusName` VARCHAR(50) NOT NULL,
  `statusDescription` VARCHAR(200) NULL,
  `createdOn` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id` ,`statusName`),
  UNIQUE INDEX `statusName_UNIQUE` (`statusName` ASC) VISIBLE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- Table `intergrate-kp-2`.`task`
DROP TABLE IF EXISTS `intergrate-kp-2`.`task` ;
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`task` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) NULL DEFAULT NULL,
  `assignees` VARCHAR(30) NULL DEFAULT NULL,
  `status` VARCHAR(50) NOT NULL,
  `createdOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updatedOn` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  FOREIGN KEY (`status`)
        REFERENCES `intergrate-kp-2`.`status`(`statusName`)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
        )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

-- Table `intergrate-kp-2`.`limitSettings`
DROP TABLE IF EXISTS `intergrate-kp-2`.`limitSettings`;
CREATE TABLE IF NOT EXISTS `intergrate-kp-2`.`limitSettings` (
  `limit` INT NOT NULL DEFAULT 10,
  `isEnable` BOOLEAN NOT NULL DEFAULT TRUE
)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;

insert into `intergrate-kp-2`.`status` (`statusName` , `statusDescription`) value ("No Status" , "The default status");
insert into `intergrate-kp-2`.`status` (`statusName`) value ("To Do");
insert into `intergrate-kp-2`.`status` (`statusName` , `statusDescription`) value ("Doing" , "Being worked on");
insert into `intergrate-kp-2`.`status` (`statusName` , `statusDescription`) value ("Done" , "Finished");

insert into `intergrate-kp-2`.`limitSettings` (`limit` , `isEnable`) value (10 ,TRUE);
ALTER DATABASE `intergrate-kp-2` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP USER IF EXISTS 'dev'@'%';
CREATE USER 'dev'@'%' IDENTIFIED BY 'ip23kp2mysql';
GRANT ALL ON `intergrate-kp-2`.* TO 'dev'@'%';
