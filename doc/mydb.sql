-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Versione server:              5.6.22-log - MySQL Community Server (GPL)
-- S.O. server:                  Win64
-- HeidiSQL Versione:            9.3.0.5116
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dump della struttura del database mydb
DROP DATABASE IF EXISTS `mydb`;
CREATE DATABASE IF NOT EXISTS `mydb` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `mydb`;

-- Dump della struttura di tabella mydb.manuscript
DROP TABLE IF EXISTS `manuscript`;
CREATE TABLE IF NOT EXISTS `manuscript` (
  `idmanuscript` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(45) DEFAULT NULL,
  `author` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idmanuscript`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;

-- Dump dei dati della tabella mydb.manuscript: ~1 rows (circa)
/*!40000 ALTER TABLE `manuscript` DISABLE KEYS */;
INSERT INTO `manuscript` (`idmanuscript`, `title`, `author`) VALUES
	(5, 'Test', 'Emiliano');
/*!40000 ALTER TABLE `manuscript` ENABLE KEYS */;

-- Dump della struttura di tabella mydb.manuscript_scan
DROP TABLE IF EXISTS `manuscript_scan`;
CREATE TABLE IF NOT EXISTS `manuscript_scan` (
  `idmanuscript_scan` int(11) NOT NULL AUTO_INCREMENT,
  `scan_time` timestamp NULL DEFAULT NULL,
  `image_scan` longblob,
  `image_description` varchar(45) DEFAULT NULL,
  `pubblication` tinyint(1) DEFAULT NULL,
  `user_username` varchar(16) NOT NULL,
  `user_role_idrole` int(11) NOT NULL,
  `manuscript_idmanuscript` int(11) NOT NULL,
  PRIMARY KEY (`idmanuscript_scan`),
  KEY `fk_manuscript_scan_user1_idx` (`user_username`,`user_role_idrole`),
  KEY `fk_manuscript_scan_manuscript1_idx` (`manuscript_idmanuscript`),
  CONSTRAINT `fk_manuscript_scan_manuscript1` FOREIGN KEY (`manuscript_idmanuscript`) REFERENCES `manuscript` (`idmanuscript`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_manuscript_scan_user1` FOREIGN KEY (`user_username`, `user_role_idrole`) REFERENCES `user` (`username`, `role_idrole`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- Dump dei dati della tabella mydb.manuscript_scan: ~1 rows (circa)
/*!40000 ALTER TABLE `manuscript_scan` DISABLE KEYS */;
INSERT INTO `manuscript_scan` (`idmanuscript_scan`, `scan_time`, `image_scan`, `image_description`, `pubblication`, `user_username`, `user_role_idrole`, `manuscript_idmanuscript`) VALUES
/*!40000 ALTER TABLE `manuscript_scan` ENABLE KEYS */;

-- Dump della struttura di tabella mydb.manuscript_transcription
DROP TABLE IF EXISTS `manuscript_transcription`;
CREATE TABLE IF NOT EXISTS `manuscript_transcription` (
  `manuscript_xml_tei` longtext,
  `pubblication` tinyint(1) DEFAULT NULL,
  `user_username` varchar(16) NOT NULL,
  `user_role_idrole` int(11) NOT NULL,
  `manuscript_scan_idmanuscript_scan` int(11) NOT NULL,
  PRIMARY KEY (`user_username`,`user_role_idrole`,`manuscript_scan_idmanuscript_scan`),
  KEY `fk_manuscript_transcription_manuscript_scan1_idx` (`manuscript_scan_idmanuscript_scan`),
  CONSTRAINT `fk_manuscript_transcription_manuscript_scan1` FOREIGN KEY (`manuscript_scan_idmanuscript_scan`) REFERENCES `manuscript_scan` (`idmanuscript_scan`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_manuscript_transcription_user1` FOREIGN KEY (`user_username`, `user_role_idrole`) REFERENCES `user` (`username`, `role_idrole`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dump dei dati della tabella mydb.manuscript_transcription: ~1 rows (circa)
/*!40000 ALTER TABLE `manuscript_transcription` DISABLE KEYS */;
INSERT INTO `manuscript_transcription` (`manuscript_xml_tei`, `pubblication`, `user_username`, `user_role_idrole`, `manuscript_scan_idmanuscript_scan`) VALUES
	('<p>I am that halfgrown <em>angry</em> boy, fallen asleep<br />The tears of foolish passion yet undried<br />upon my cheeks.</p>\r\n<p>I pass through <em>the</em> travels and <span style="text-decoration: line-through;">fortunes</span> of<br />thirty<br /><br />years and become old,<br />Each in its due order comes and goes,<br />And thus a message for me comes.<br />The</p>', 1, 'admin', 1, 4);
/*!40000 ALTER TABLE `manuscript_transcription` ENABLE KEYS */;

-- Dump della struttura di tabella mydb.menu
DROP TABLE IF EXISTS `menu`;
CREATE TABLE IF NOT EXISTS `menu` (
  `menuid` int(11) NOT NULL AUTO_INCREMENT,
  `menuda` int(11) DEFAULT NULL,
  `menutext` varchar(30) DEFAULT NULL,
  `comando` varchar(45) DEFAULT NULL,
  `role_idrole` int(11) NOT NULL,
  PRIMARY KEY (`menuid`,`role_idrole`),
  KEY `fk_table1_role1_idx` (`role_idrole`),
  CONSTRAINT `fk_table1_role1` FOREIGN KEY (`role_idrole`) REFERENCES `role` (`idrole`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- Dump dei dati della tabella mydb.menu: ~9 rows (circa)
/*!40000 ALTER TABLE `menu` DISABLE KEYS */;
INSERT INTO `menu` (`menuid`, `menuda`, `menutext`, `comando`, `role_idrole`) VALUES
	(1, NULL, 'acquisizione', NULL, 1),
	(2, NULL, 'trascrizione', NULL, 1),
	(3, 1, 'scansione', 'Acquisizione_paper', 1),
	(4, 1, 'revisione', 'Revisione_Pubblicazione_paper', 1),
	(6, 2, 'editor', 'Editor_Revisione_Pubblicazione_transcription', 1),
	(7, NULL, 'opere', NULL, 1),
	(8, 7, 'visualizzazione', 'Visualizzazione_opere', 1),
	(9, NULL, 'opere', NULL, 2),
	(10, 9, 'visualizzazione', 'Visualizzazione_opere', 2);
/*!40000 ALTER TABLE `menu` ENABLE KEYS */;

-- Dump della struttura di tabella mydb.role
DROP TABLE IF EXISTS `role`;
CREATE TABLE IF NOT EXISTS `role` (
  `idrole` int(11) NOT NULL AUTO_INCREMENT,
  `role_description` varchar(45) DEFAULT NULL,
  `administrator` tinyint(1) DEFAULT NULL,
  `scan` tinyint(1) DEFAULT NULL,
  `scan_revisor` tinyint(1) DEFAULT NULL,
  `transcriber` tinyint(1) DEFAULT NULL,
  `transcriber_revisor` tinyint(1) DEFAULT NULL,
  `base_user` tinyint(1) DEFAULT NULL,
  `advanced_user` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`idrole`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- Dump dei dati della tabella mydb.role: ~2 rows (circa)
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` (`idrole`, `role_description`, `administrator`, `scan`, `scan_revisor`, `transcriber`, `transcriber_revisor`, `base_user`, `advanced_user`) VALUES
	(1, 'amministratore', 1, 0, 0, 0, 0, 0, 0),
	(2, 'base', 0, 0, 0, 0, 0, 1, 0);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

-- Dump della struttura di tabella mydb.user
DROP TABLE IF EXISTS `user`;
CREATE TABLE IF NOT EXISTS `user` (
  `username` varchar(16) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(32) NOT NULL,
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `role_idrole` int(11) NOT NULL,
  PRIMARY KEY (`username`,`role_idrole`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  KEY `fk_user_role_idx` (`role_idrole`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_idrole`) REFERENCES `role` (`idrole`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Dump dei dati della tabella mydb.user: ~2 rows (circa)
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`username`, `email`, `password`, `create_time`, `role_idrole`) VALUES
	('admin', 'emseba@gmail.com', 'pippo', '2016-09-03 00:00:00', 1),
	('base', 'base@base.it', 'base', '2016-09-09 14:34:51', 2);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;