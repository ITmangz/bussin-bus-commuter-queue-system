-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 02, 2026 at 09:05 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `qpal`
--

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

CREATE TABLE `accounts` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` varchar(100) NOT NULL,
  `status` varchar(100) NOT NULL,
  `profile_image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` (`id`, `name`, `email`, `password`, `role`, `status`, `profile_image`) VALUES
(1, 'Enzo', 'enzo', 'enzo123', 'Admin', 'Active', 'C:\\Users\\Enzo\\Pictures\\Screenshots\\DOCU3.png'),
(3, 'abawan', 'arawan', 'arawan312', 'Employee', 'Active', NULL),
(4, 'JM Tenorio', 'jm', 'jm123', 'Admin', 'Inactive', NULL),
(5, 'Gab', 'gluc312', 'gluc312', 'Admin', 'Active', NULL),
(6, 'Jerome Maganda', 'jerome', 'jerome123', 'Employee', 'Active', NULL),
(8, 'kevin', 'kevin', 'kevin312', 'Admin', 'Active', 'C:\\Users\\Enzo\\Pictures\\Screenshots\\docu12.png');

-- --------------------------------------------------------

--
-- Table structure for table `activity_logs`
--

CREATE TABLE `activity_logs` (
  `log_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `email` varchar(100) NOT NULL,
  `role` enum('Admin','Employee') NOT NULL,
  `module` varchar(100) NOT NULL,
  `action` varchar(50) NOT NULL,
  `description` text NOT NULL,
  `account_id` int(11) DEFAULT NULL,
  `user_name` varchar(100) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `activity_logs`
--

INSERT INTO `activity_logs` (`log_id`, `created_at`, `email`, `role`, `module`, `action`, `description`, `account_id`, `user_name`) VALUES
(1, '2026-10-02 16:02:41', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(2, '2026-10-02 17:58:05', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(3, '2026-10-02 18:43:56', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(4, '2026-10-02 18:45:12', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(5, '2026-10-02 18:45:26', 'enzo', 'Admin', 'Authentication', 'Logout', 'User logged out of the system.', 1, 'Enzo'),
(6, '2026-10-02 18:45:30', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(7, '2026-10-02 18:46:14', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(8, '2026-10-02 18:48:22', 'enzo', 'Admin', 'Authentication', 'Logout', 'User logged out of the system.', 1, 'Enzo'),
(9, '2026-10-02 18:53:09', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(10, '2026-10-02 18:57:39', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(11, '2026-10-02 19:02:37', 'enzo', 'Admin', 'Authentication', 'Login', 'User logged in to the system.', 1, 'Enzo'),
(12, '2026-10-02 19:05:10', 'enzo', 'Admin', 'Authentication', 'Logout', 'User logged out of the system.', 1, 'Enzo');

-- --------------------------------------------------------

--
-- Table structure for table `boarding_gates`
--

CREATE TABLE `boarding_gates` (
  `gate` int(11) NOT NULL,
  `trip_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `boarding_gates`
--

INSERT INTO `boarding_gates` (`gate`, `trip_id`) VALUES
(2, NULL),
(1, 20);

-- --------------------------------------------------------

--
-- Table structure for table `boarding_skips`
--

CREATE TABLE `boarding_skips` (
  `queue_entry_id` int(11) NOT NULL,
  `skipped_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

CREATE TABLE `bookings` (
  `booking_id` int(11) NOT NULL,
  `booking_reference` varchar(30) NOT NULL,
  `trip_id` int(11) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL DEFAULT 0.00,
  `status` varchar(20) NOT NULL DEFAULT 'Pending',
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`booking_id`, `booking_reference`, `trip_id`, `total_amount`, `status`, `created_at`) VALUES
(6, 'BK-c4be51acdfba41719ba4b5051d', 6, 30.00, 'Completed', '2026-09-24 00:01:03'),
(7, 'BK-56036a43ba46470c828cdaa7ef', 6, 30.00, 'Completed', '2026-09-24 00:06:38'),
(8, 'BK-00a98f63349e448389adbaf3cf', 7, 150.00, 'Completed', '2026-09-24 00:37:35'),
(9, 'BK-81c17f544eb34c9fbb62f903c9', 7, 30.00, 'Completed', '2026-09-24 00:38:30'),
(10, 'BK-07fd876bc3e44da18b1b7c034f', 7, 30.00, 'Completed', '2026-09-24 00:42:26'),
(11, 'BK-6e9bff120f17448fb641a49346', 7, 30.00, 'Confirmed', '2026-09-24 00:44:52'),
(12, 'BK-dee3a825031b4d7a9adc366de6', 7, 30.00, 'Pending', '2026-09-24 00:55:02'),
(13, 'BK-f74ea7a2646f4f5a9bb27c4663', 7, 90.00, 'Pending', '2026-09-24 01:25:11'),
(14, 'BK-13740262099c4b249058863887', 7, 90.00, 'Pending', '2026-09-24 01:34:16'),
(15, 'BK-5dd1579ff6124d63be5cfbcd74', 7, 30.00, 'Pending', '2026-09-24 01:43:06'),
(16, 'BK-e5c9436443a34f96b0238152c0', 7, 300.00, 'Pending', '2026-09-24 01:54:14'),
(17, 'BK-1314d98a34794901b0d62a365a', 7, 300.00, 'Pending', '2026-09-24 01:55:09'),
(18, 'BK-e8b868d1b571448bb7241b705c', 7, 120.00, 'Pending', '2026-09-24 01:55:48'),
(19, 'BK-da07f7ac90ac4e9e9ef134f613', 8, 300.00, 'Pending', '2026-09-24 02:06:55'),
(20, 'BK-42420d27e34c491a99a05574bd', 8, 270.00, 'Pending', '2026-09-24 02:07:21'),
(21, 'BK-ae5a8b850f914517af8ddb8b83', 8, 30.00, 'Pending', '2026-09-24 02:07:43'),
(22, 'BK-29b9ea41f6044101add789010c', 11, 90.00, 'Pending', '2026-09-24 02:08:18'),
(23, 'BK-417de391cafb4b679c6ab11a75', 9, 30.00, 'Pending', '2026-09-24 02:14:23'),
(24, 'BK-593ab0c51e524a88bfdf6bf88e', 13, 500.00, 'Pending', '2026-09-24 15:26:22'),
(25, 'BK-0e32fc34ac12408a90cb39fcf1', 13, 500.00, 'Pending', '2026-09-24 15:26:53'),
(26, 'BK-6ed04e0a75b942bb80b6e2ff2c', 11, 300.00, 'Pending', '2026-09-24 16:38:11'),
(29, 'BK-d198fc654655418dab5c0eede4', 15, 50.00, 'Pending', '2026-09-27 05:48:10'),
(30, 'BK-4940af1d04b045029c2d9fb78b', 16, 50.00, 'Pending', '2026-09-28 23:20:05'),
(31, 'BK-731b617ea0404c70ba3626c447', 19, 500.00, 'Completed', '2026-10-01 00:41:38'),
(32, 'BK-10326c9a2fef43b59b002fc26d', 19, 50.00, 'Completed', '2026-10-01 01:22:52'),
(33, 'BK-eb9c46a35ee14811b9341d82fa', 19, 50.00, 'Completed', '2026-10-01 01:40:53'),
(34, 'BK-5c5399dd451d43e88f8cf93de8', 19, 50.00, 'Completed', '2026-10-01 01:52:38'),
(35, 'BK-0c5389c81abb43bb8d0f1b9c0b', 20, 50.00, 'Completed', '2026-10-01 02:31:10'),
(36, 'BK-6e5a08a533a9476b9ce7e306da', 20, 100.00, 'Completed', '2026-10-01 02:31:43'),
(37, 'BK-6e126e54067e4da8aa6ad52849', 20, 50.00, 'Completed', '2026-10-01 02:32:08'),
(38, 'BK-96382466205448eda9fe6c04a1', 20, 50.00, 'Completed', '2026-10-01 03:30:58'),
(39, 'BK-712d7f51808a45498568893913', 20, 50.00, 'No-show', '2026-10-01 04:22:49'),
(40, 'BK-770ec008b2ee4731b03232ec37', 20, 50.00, 'No-show', '2026-10-01 04:23:12'),
(41, 'BK-53a65af377ce47568bbad09448', 20, 150.00, 'No-show', '2026-10-01 04:23:34'),
(42, 'BK-70766ad8e43a4c1ab80ec03cb2', 20, 50.00, 'Completed', '2026-10-01 04:23:55'),
(43, 'BK-1044decd9f82461eb3012679b2', 20, 50.00, 'Completed', '2026-10-01 04:24:13'),
(44, 'BK-50faa97b5b104a0daf18a1b093', 20, 50.00, 'Completed', '2026-10-01 04:24:34'),
(45, 'BK-3d0529a6393f4f7d9090b55e12', 20, 50.00, 'No-show', '2026-10-01 04:24:50'),
(46, 'BK-3604edf3bec14b11a47a929656', 20, 50.00, 'Completed', '2026-10-01 04:25:06'),
(47, 'BK-82fc08176d704ef89720bcdeab', 20, 50.00, 'Completed', '2026-10-01 04:25:26'),
(48, 'BK-482df2b1c6e04a0aa93245399b', 20, 50.00, 'Completed', '2026-10-01 04:25:44'),
(49, 'BK-2cf8104470c242b781d0cc945d', 20, 150.00, 'Completed', '2026-10-01 18:15:31'),
(50, 'BK-b2b5bfd88a1a45f49b9557c53c', 21, 60.00, 'Pending', '2026-10-01 18:33:16'),
(51, 'BK-c850a777692c4bcda022efa29a', 21, 180.00, 'Pending', '2026-10-01 18:34:50'),
(52, 'BK-106823b0619c46aa8abbb675b6', 21, 60.00, 'Pending', '2026-10-01 19:07:53'),
(53, 'BK-a87b7072c32349a4ab0a0ce7da', 21, 600.00, 'Pending', '2026-10-01 20:15:40'),
(54, 'BK-642ddf791dfa424ab6709138a2', 21, 600.00, 'Pending', '2026-10-01 20:16:17'),
(55, 'BK-ad15c6c2cfd34e6a8f9797aee6', 21, 600.00, 'Pending', '2026-10-01 20:16:54'),
(56, 'BK-a5300e5a1235496985c5b0e965', 21, 480.00, 'Pending', '2026-10-01 20:17:37'),
(57, 'BK-88887ca0e68d47eb93fe9ac63e', 21, 120.00, 'Pending', '2026-10-01 20:24:46'),
(58, 'BK-2d4c072fbdd448dbbe4948b2da', 24, 500.00, 'Completed', '2026-10-02 04:14:06'),
(59, 'BK-99465ed340b54430932c1d4ee7', 25, 50.00, 'Completed', '2026-10-02 17:49:24');

-- --------------------------------------------------------

--
-- Table structure for table `booking_passengers`
--

CREATE TABLE `booking_passengers` (
  `booking_passenger_id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `trip_id` int(11) NOT NULL,
  `passenger_name` varchar(150) NOT NULL,
  `passenger_type` varchar(30) NOT NULL,
  `fare` decimal(10,2) NOT NULL DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `booking_passengers`
--

INSERT INTO `booking_passengers` (`booking_passenger_id`, `booking_id`, `trip_id`, `passenger_name`, `passenger_type`, `fare`) VALUES
(23, 6, 6, 'Passenger 1', 'Student', 30.00),
(24, 7, 6, 'Passenger 1', 'Student', 30.00),
(25, 8, 7, 'Passenger 1', 'Student', 30.00),
(26, 8, 7, 'Passenger 2', 'Student', 30.00),
(27, 8, 7, 'Passenger 3', 'Student', 30.00),
(28, 8, 7, 'Passenger 4', 'Student', 30.00),
(29, 8, 7, 'Passenger 5', 'Student', 30.00),
(30, 9, 7, 'Passenger 1', 'Student', 30.00),
(31, 10, 7, 'Passenger 1', 'Student', 30.00),
(32, 11, 7, 'Passenger 1', 'Regular', 30.00),
(33, 12, 7, 'Passenger 1', 'Student', 30.00),
(34, 13, 7, 'Passenger 1', 'Student', 30.00),
(35, 13, 7, 'Passenger 2', 'Student', 30.00),
(36, 13, 7, 'Passenger 3', 'Student', 30.00),
(37, 14, 7, 'Passenger 1', 'Student', 30.00),
(38, 14, 7, 'Passenger 2', 'Student', 30.00),
(39, 14, 7, 'Passenger 3', 'Student', 30.00),
(40, 15, 7, 'Passenger 1', 'Student', 30.00),
(41, 16, 7, 'Passenger 1', 'Regular', 30.00),
(42, 16, 7, 'Passenger 2', 'Regular', 30.00),
(43, 16, 7, 'Passenger 3', 'Regular', 30.00),
(44, 16, 7, 'Passenger 4', 'Regular', 30.00),
(45, 16, 7, 'Passenger 5', 'Regular', 30.00),
(46, 16, 7, 'Passenger 6', 'Regular', 30.00),
(47, 16, 7, 'Passenger 7', 'Regular', 30.00),
(48, 16, 7, 'Passenger 8', 'Regular', 30.00),
(49, 16, 7, 'Passenger 9', 'Regular', 30.00),
(50, 16, 7, 'Passenger 10', 'Regular', 30.00),
(51, 17, 7, 'Passenger 1', 'Regular', 30.00),
(52, 17, 7, 'Passenger 2', 'Regular', 30.00),
(53, 17, 7, 'Passenger 3', 'Regular', 30.00),
(54, 17, 7, 'Passenger 4', 'Regular', 30.00),
(55, 17, 7, 'Passenger 5', 'Regular', 30.00),
(56, 17, 7, 'Passenger 6', 'Regular', 30.00),
(57, 17, 7, 'Passenger 7', 'Regular', 30.00),
(58, 17, 7, 'Passenger 8', 'Regular', 30.00),
(59, 17, 7, 'Passenger 9', 'Regular', 30.00),
(60, 17, 7, 'Passenger 10', 'Regular', 30.00),
(61, 18, 7, 'Passenger 1', 'Regular', 30.00),
(62, 18, 7, 'Passenger 2', 'Regular', 30.00),
(63, 18, 7, 'Passenger 3', 'Regular', 30.00),
(64, 18, 7, 'Passenger 4', 'Regular', 30.00),
(65, 19, 8, 'Passenger 1', 'Regular', 30.00),
(66, 19, 8, 'Passenger 2', 'Regular', 30.00),
(67, 19, 8, 'Passenger 3', 'Regular', 30.00),
(68, 19, 8, 'Passenger 4', 'Regular', 30.00),
(69, 19, 8, 'Passenger 5', 'Regular', 30.00),
(70, 19, 8, 'Passenger 6', 'Regular', 30.00),
(71, 19, 8, 'Passenger 7', 'Regular', 30.00),
(72, 19, 8, 'Passenger 8', 'Regular', 30.00),
(73, 19, 8, 'Passenger 9', 'Regular', 30.00),
(74, 19, 8, 'Passenger 10', 'Regular', 30.00),
(75, 20, 8, 'Passenger 1', 'Regular', 30.00),
(76, 20, 8, 'Passenger 2', 'Regular', 30.00),
(77, 20, 8, 'Passenger 3', 'Regular', 30.00),
(78, 20, 8, 'Passenger 4', 'Regular', 30.00),
(79, 20, 8, 'Passenger 5', 'Regular', 30.00),
(80, 20, 8, 'Passenger 6', 'Regular', 30.00),
(81, 20, 8, 'Passenger 7', 'Regular', 30.00),
(82, 20, 8, 'Passenger 8', 'Regular', 30.00),
(83, 20, 8, 'Passenger 9', 'Regular', 30.00),
(84, 21, 8, 'Passenger 1', 'Student', 30.00),
(85, 22, 11, 'Passenger 1', 'Regular', 30.00),
(86, 22, 11, 'Passenger 2', 'Regular', 30.00),
(87, 22, 11, 'Passenger 3', 'Regular', 30.00),
(88, 23, 9, 'Passenger 1', 'Regular', 30.00),
(89, 24, 13, 'Passenger 1', 'Student', 50.00),
(90, 24, 13, 'Passenger 2', 'Student', 50.00),
(91, 24, 13, 'Passenger 3', 'Student', 50.00),
(92, 24, 13, 'Passenger 4', 'Student', 50.00),
(93, 24, 13, 'Passenger 5', 'Student', 50.00),
(94, 24, 13, 'Passenger 6', 'Student', 50.00),
(95, 24, 13, 'Passenger 7', 'Student', 50.00),
(96, 24, 13, 'Passenger 8', 'Student', 50.00),
(97, 24, 13, 'Passenger 9', 'Student', 50.00),
(98, 24, 13, 'Passenger 10', 'Student', 50.00),
(99, 25, 13, 'Passenger 1', 'Student', 50.00),
(100, 25, 13, 'Passenger 2', 'Student', 50.00),
(101, 25, 13, 'Passenger 3', 'Student', 50.00),
(102, 25, 13, 'Passenger 4', 'Student', 50.00),
(103, 25, 13, 'Passenger 5', 'Student', 50.00),
(104, 25, 13, 'Passenger 6', 'Student', 50.00),
(105, 25, 13, 'Passenger 7', 'Student', 50.00),
(106, 25, 13, 'Passenger 8', 'Student', 50.00),
(107, 25, 13, 'Passenger 9', 'Student', 50.00),
(108, 25, 13, 'Passenger 10', 'Student', 50.00),
(109, 26, 11, 'Passenger 1', 'Student', 30.00),
(110, 26, 11, 'Passenger 2', 'Student', 30.00),
(111, 26, 11, 'Passenger 3', 'Student', 30.00),
(112, 26, 11, 'Passenger 4', 'Student', 30.00),
(113, 26, 11, 'Passenger 5', 'Student', 30.00),
(114, 26, 11, 'Passenger 6', 'Student', 30.00),
(115, 26, 11, 'Passenger 7', 'Student', 30.00),
(116, 26, 11, 'Passenger 8', 'Student', 30.00),
(117, 26, 11, 'Passenger 9', 'Student', 30.00),
(118, 26, 11, 'Passenger 10', 'Student', 30.00),
(134, 29, 15, 'Passenger 1', 'Student', 50.00),
(135, 30, 16, 'Passenger 1', 'Regular', 50.00),
(136, 31, 19, 'Passenger 1', 'Regular', 50.00),
(137, 31, 19, 'Passenger 2', 'Regular', 50.00),
(138, 31, 19, 'Passenger 3', 'Regular', 50.00),
(139, 31, 19, 'Passenger 4', 'Regular', 50.00),
(140, 31, 19, 'Passenger 5', 'Regular', 50.00),
(141, 31, 19, 'Passenger 6', 'Regular', 50.00),
(142, 31, 19, 'Passenger 7', 'Regular', 50.00),
(143, 31, 19, 'Passenger 8', 'Regular', 50.00),
(144, 31, 19, 'Passenger 9', 'Regular', 50.00),
(145, 31, 19, 'Passenger 10', 'Regular', 50.00),
(146, 32, 19, 'Passenger 1', 'Regular', 50.00),
(147, 33, 19, 'Passenger 1', 'Regular', 50.00),
(148, 34, 19, 'Passenger 1', 'Regular', 50.00),
(149, 35, 20, 'Passenger 1', 'Regular', 50.00),
(150, 36, 20, 'Passenger 1', 'Regular', 50.00),
(151, 36, 20, 'Passenger 2', 'Regular', 50.00),
(152, 37, 20, 'Passenger 1', 'Student', 50.00),
(153, 38, 20, 'Passenger 1', 'Regular', 50.00),
(154, 39, 20, 'Passenger 1', 'Regular', 50.00),
(155, 40, 20, 'Passenger 1', 'Regular', 50.00),
(156, 41, 20, 'Passenger 1', 'Regular', 50.00),
(157, 41, 20, 'Passenger 2', 'Regular', 50.00),
(158, 41, 20, 'Passenger 3', 'Regular', 50.00),
(159, 42, 20, 'Passenger 1', 'Regular', 50.00),
(160, 43, 20, 'Passenger 1', 'Regular', 50.00),
(161, 44, 20, 'Passenger 1', 'Regular', 50.00),
(162, 45, 20, 'Passenger 1', 'Regular', 50.00),
(163, 46, 20, 'Passenger 1', 'Regular', 50.00),
(164, 47, 20, 'Passenger 1', 'Regular', 50.00),
(165, 48, 20, 'Passenger 1', 'Senior', 50.00),
(166, 49, 20, 'Passenger 1', 'Regular', 50.00),
(167, 49, 20, 'Passenger 2', 'Regular', 50.00),
(168, 49, 20, 'Passenger 3', 'Regular', 50.00),
(169, 50, 21, 'Passenger 1', 'Regular', 60.00),
(170, 51, 21, 'Passenger 1', 'Student', 60.00),
(171, 51, 21, 'Passenger 2', 'Student', 60.00),
(172, 51, 21, 'Passenger 3', 'Student', 60.00),
(173, 52, 21, 'Passenger 1', 'Regular', 60.00),
(174, 53, 21, 'Passenger 1', 'Student', 60.00),
(175, 53, 21, 'Passenger 2', 'Student', 60.00),
(176, 53, 21, 'Passenger 3', 'Student', 60.00),
(177, 53, 21, 'Passenger 4', 'Student', 60.00),
(178, 53, 21, 'Passenger 5', 'Student', 60.00),
(179, 53, 21, 'Passenger 6', 'Student', 60.00),
(180, 53, 21, 'Passenger 7', 'Student', 60.00),
(181, 53, 21, 'Passenger 8', 'Student', 60.00),
(182, 53, 21, 'Passenger 9', 'Student', 60.00),
(183, 53, 21, 'Passenger 10', 'Student', 60.00),
(184, 54, 21, 'Passenger 1', 'Regular', 60.00),
(185, 54, 21, 'Passenger 2', 'Regular', 60.00),
(186, 54, 21, 'Passenger 3', 'Regular', 60.00),
(187, 54, 21, 'Passenger 4', 'Regular', 60.00),
(188, 54, 21, 'Passenger 5', 'Regular', 60.00),
(189, 54, 21, 'Passenger 6', 'Regular', 60.00),
(190, 54, 21, 'Passenger 7', 'Regular', 60.00),
(191, 54, 21, 'Passenger 8', 'Regular', 60.00),
(192, 54, 21, 'Passenger 9', 'Regular', 60.00),
(193, 54, 21, 'Passenger 10', 'Regular', 60.00),
(194, 55, 21, 'Passenger 1', 'Regular', 60.00),
(195, 55, 21, 'Passenger 2', 'Regular', 60.00),
(196, 55, 21, 'Passenger 3', 'Regular', 60.00),
(197, 55, 21, 'Passenger 4', 'Regular', 60.00),
(198, 55, 21, 'Passenger 5', 'Regular', 60.00),
(199, 55, 21, 'Passenger 6', 'Regular', 60.00),
(200, 55, 21, 'Passenger 7', 'Regular', 60.00),
(201, 55, 21, 'Passenger 8', 'Regular', 60.00),
(202, 55, 21, 'Passenger 9', 'Regular', 60.00),
(203, 55, 21, 'Passenger 10', 'Regular', 60.00),
(204, 56, 21, 'Passenger 1', 'Regular', 60.00),
(205, 56, 21, 'Passenger 2', 'Regular', 60.00),
(206, 56, 21, 'Passenger 3', 'Regular', 60.00),
(207, 56, 21, 'Passenger 4', 'Regular', 60.00),
(208, 56, 21, 'Passenger 5', 'Regular', 60.00),
(209, 56, 21, 'Passenger 6', 'Regular', 60.00),
(210, 56, 21, 'Passenger 7', 'Regular', 60.00),
(211, 56, 21, 'Passenger 8', 'Regular', 60.00),
(212, 57, 21, 'Passenger 1', 'Student', 60.00),
(213, 57, 21, 'Passenger 2', 'Student', 60.00),
(214, 58, 24, 'Passenger 1', 'Regular', 50.00),
(215, 58, 24, 'Passenger 2', 'Regular', 50.00),
(216, 58, 24, 'Passenger 3', 'Regular', 50.00),
(217, 58, 24, 'Passenger 4', 'Regular', 50.00),
(218, 58, 24, 'Passenger 5', 'Regular', 50.00),
(219, 58, 24, 'Passenger 6', 'Regular', 50.00),
(220, 58, 24, 'Passenger 7', 'Regular', 50.00),
(221, 58, 24, 'Passenger 8', 'Regular', 50.00),
(222, 58, 24, 'Passenger 9', 'Regular', 50.00),
(223, 58, 24, 'Passenger 10', 'Regular', 50.00),
(224, 59, 25, 'Passenger 1', 'Regular', 50.00);

-- --------------------------------------------------------

--
-- Table structure for table `buses`
--

CREATE TABLE `buses` (
  `bus_id` int(11) NOT NULL,
  `bus_number` varchar(20) NOT NULL,
  `seat_capacity` int(11) NOT NULL,
  `available_seats` int(11) NOT NULL,
  `bus_status` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `buses`
--

INSERT INTO `buses` (`bus_id`, `bus_number`, `seat_capacity`, `available_seats`, `bus_status`) VALUES
(5, 'Bus01', 40, 40, 'Available'),
(6, 'Bus02', 20, 20, 'Available'),
(7, 'Bus03', 20, 20, 'Maintenance'),
(8, 'Bus04', 30, 30, 'Available'),
(9, 'Bus05', 40, 40, 'Available'),
(10, 'Bus06', 20, 20, 'Available'),
(11, 'Bus07', 40, 40, 'Available'),
(12, 'Bus10', 45, 45, 'Available'),
(13, 'Bus012', 20, 20, 'Maintenance'),
(14, 'Bus013', 25, 25, 'Available'),
(15, 'Bus1', 20, 20, 'Available');

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

CREATE TABLE `payments` (
  `payment_id` int(11) NOT NULL,
  `trip_id` int(11) NOT NULL,
  `commuter_name` varchar(100) NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `payment_method` enum('Cash','GCash','Card') DEFAULT NULL,
  `status` enum('Pending','Paid','Cancelled') NOT NULL DEFAULT 'Pending',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `paid_at` datetime DEFAULT NULL,
  `booking_id` int(11) DEFAULT NULL
) ;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`payment_id`, `trip_id`, `commuter_name`, `amount`, `payment_method`, `status`, `created_at`, `paid_at`, `booking_id`) VALUES
(6, 6, 'Passenger 1', 30.00, 'Cash', 'Paid', '2026-09-24 00:01:03', '2026-09-24 01:44:55', 6),
(7, 6, 'Passenger 1', 30.00, 'Cash', 'Paid', '2026-09-24 00:06:39', '2026-09-24 01:45:55', 7),
(8, 7, 'Passenger 1', 150.00, 'Cash', 'Paid', '2026-09-24 00:37:35', '2026-09-24 15:27:26', 8),
(9, 7, 'Passenger 1', 30.00, 'Cash', 'Paid', '2026-09-24 00:38:30', '2026-09-24 15:28:03', 9),
(10, 7, 'Passenger 1', 30.00, 'Cash', 'Paid', '2026-09-24 00:42:26', '2026-09-24 15:28:45', 10),
(11, 7, 'Passenger 1', 30.00, 'Cash', 'Paid', '2026-09-24 00:44:52', '2026-09-24 17:10:14', 11),
(12, 7, 'Passenger 1', 30.00, 'Cash', 'Pending', '2026-09-24 00:55:02', NULL, 12),
(13, 7, 'Passenger 1', 90.00, 'Cash', 'Pending', '2026-09-24 01:25:11', NULL, 13),
(14, 7, 'Passenger 1', 90.00, 'Cash', 'Pending', '2026-09-24 01:34:16', NULL, 14),
(15, 7, 'Passenger 1', 30.00, 'Cash', 'Pending', '2026-09-24 01:43:06', NULL, 15),
(16, 7, 'Passenger 1', 300.00, 'Cash', 'Pending', '2026-09-24 01:54:14', NULL, 16),
(17, 7, 'Passenger 1', 300.00, 'Cash', 'Pending', '2026-09-24 01:55:09', NULL, 17),
(18, 7, 'Passenger 1', 120.00, 'Cash', 'Pending', '2026-09-24 01:55:48', NULL, 18),
(19, 8, 'Passenger 1', 300.00, 'Cash', 'Pending', '2026-09-24 02:06:55', NULL, 19),
(20, 8, 'Passenger 1', 270.00, 'Cash', 'Pending', '2026-09-24 02:07:21', NULL, 20),
(21, 8, 'Passenger 1', 30.00, 'GCash', 'Pending', '2026-09-24 02:07:43', NULL, 21),
(22, 11, 'Passenger 1', 90.00, 'Card', 'Pending', '2026-09-24 02:08:18', NULL, 22),
(23, 9, 'Passenger 1', 30.00, 'Cash', 'Pending', '2026-09-24 02:14:23', NULL, 23),
(24, 13, 'Passenger 1', 500.00, 'Cash', 'Pending', '2026-09-24 15:26:22', NULL, 24),
(25, 13, 'Passenger 1', 500.00, 'Cash', 'Pending', '2026-09-24 15:26:53', NULL, 25),
(26, 11, 'Passenger 1', 300.00, 'Cash', 'Pending', '2026-09-24 16:38:11', NULL, 26),
(29, 15, 'Passenger 1', 50.00, 'Cash', 'Pending', '2026-09-27 05:48:10', NULL, 29),
(30, 16, 'Passenger 1', 50.00, 'Cash', 'Pending', '2026-09-28 23:20:05', NULL, 30),
(31, 19, 'Passenger 1', 500.00, 'Cash', 'Paid', '2026-10-01 00:41:38', '2026-10-01 00:47:58', 31),
(32, 19, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 01:22:52', '2026-10-01 01:23:41', 32),
(33, 19, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 01:40:53', '2026-10-01 01:53:36', 33),
(34, 19, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 01:52:38', '2026-10-01 02:48:57', 34),
(35, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 02:31:10', '2026-10-01 03:27:29', 35),
(36, 20, 'Passenger 1', 100.00, 'Cash', 'Paid', '2026-10-01 02:31:43', '2026-10-01 02:54:33', 36),
(37, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 02:32:08', '2026-10-01 03:10:51', 37),
(38, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 03:30:58', '2026-10-01 03:31:45', 38),
(39, 20, 'Passenger 1', 50.00, 'Cash', 'Cancelled', '2026-10-01 04:22:49', NULL, 39),
(40, 20, 'Passenger 1', 50.00, 'Cash', 'Cancelled', '2026-10-01 04:23:12', NULL, 40),
(41, 20, 'Passenger 1', 150.00, 'Cash', 'Cancelled', '2026-10-01 04:23:34', NULL, 41),
(42, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 04:23:55', '2026-10-01 17:44:36', 42),
(43, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 04:24:13', '2026-10-01 18:12:13', 43),
(44, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 04:24:34', '2026-10-01 18:37:50', 44),
(45, 20, 'Passenger 1', 50.00, 'Cash', 'Cancelled', '2026-10-01 04:24:50', NULL, 45),
(46, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 04:25:06', '2026-10-01 19:10:02', 46),
(47, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 04:25:26', '2026-10-01 20:31:22', 47),
(48, 20, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-01 04:25:44', '2026-10-01 20:32:28', 48),
(49, 20, 'Passenger 1', 150.00, 'Cash', 'Paid', '2026-10-01 18:15:31', '2026-10-01 20:33:30', 49),
(50, 21, 'Passenger 1', 60.00, 'Cash', 'Pending', '2026-10-01 18:33:16', NULL, 50),
(51, 21, 'Passenger 1', 180.00, 'Cash', 'Pending', '2026-10-01 18:34:50', NULL, 51),
(52, 21, 'Passenger 1', 60.00, 'Cash', 'Pending', '2026-10-01 19:07:53', NULL, 52),
(53, 21, 'Passenger 1', 600.00, 'Cash', 'Pending', '2026-10-01 20:15:40', NULL, 53),
(54, 21, 'Passenger 1', 600.00, 'Cash', 'Pending', '2026-10-01 20:16:17', NULL, 54),
(55, 21, 'Passenger 1', 600.00, 'Cash', 'Pending', '2026-10-01 20:16:55', NULL, 55),
(56, 21, 'Passenger 1', 480.00, 'Cash', 'Pending', '2026-10-01 20:17:37', NULL, 56),
(57, 21, 'Passenger 1', 120.00, 'GCash', 'Pending', '2026-10-01 20:24:46', NULL, 57),
(58, 24, 'Passenger 1', 500.00, 'Cash', 'Paid', '2026-10-02 04:14:06', '2026-10-02 04:15:42', 58),
(59, 25, 'Passenger 1', 50.00, 'Cash', 'Paid', '2026-10-02 17:49:24', '2026-10-02 17:50:47', 59);

-- --------------------------------------------------------

--
-- Table structure for table `queue_boarding`
--

CREATE TABLE `queue_boarding` (
  `queue_entry_id` int(11) NOT NULL,
  `boarded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `queue_daily_counters`
--

CREATE TABLE `queue_daily_counters` (
  `queue_date` date NOT NULL,
  `last_queue_number` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `queue_daily_counters`
--

INSERT INTO `queue_daily_counters` (`queue_date`, `last_queue_number`) VALUES
('2026-09-21', 1),
('2026-09-22', 4),
('2026-09-24', 23),
('2026-09-27', 1),
('2026-09-28', 1),
('2026-10-01', 27),
('2026-10-02', 2);

-- --------------------------------------------------------

--
-- Table structure for table `queue_entries`
--

CREATE TABLE `queue_entries` (
  `queue_entry_id` int(11) NOT NULL,
  `booking_id` int(11) NOT NULL,
  `queue_date` date NOT NULL,
  `queue_number` int(11) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'Waiting',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `called_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `queue_entries`
--

INSERT INTO `queue_entries` (`queue_entry_id`, `booking_id`, `queue_date`, `queue_number`, `status`, `created_at`, `called_at`, `completed_at`) VALUES
(6, 6, '2026-09-24', 1, 'Completed', '2026-09-24 00:01:03', '2026-09-24 01:43:48', '2026-09-24 01:45:17'),
(7, 7, '2026-09-24', 2, 'Completed', '2026-09-24 00:06:39', '2026-09-24 01:45:32', '2026-09-24 01:45:55'),
(8, 8, '2026-09-24', 3, 'Completed', '2026-09-24 00:37:35', '2026-09-24 01:45:56', '2026-09-24 15:27:28'),
(9, 9, '2026-09-24', 4, 'Completed', '2026-09-24 00:38:30', '2026-09-24 15:27:31', '2026-09-24 15:28:04'),
(10, 10, '2026-09-24', 5, 'Completed', '2026-09-24 00:42:26', '2026-09-24 15:28:37', '2026-09-24 17:09:32'),
(11, 11, '2026-09-24', 6, 'Serving', '2026-09-24 00:44:52', '2026-09-24 17:09:34', NULL),
(12, 12, '2026-09-24', 7, 'Waiting', '2026-09-24 00:55:02', NULL, NULL),
(13, 13, '2026-09-24', 8, 'Waiting', '2026-09-24 01:25:11', NULL, NULL),
(14, 14, '2026-09-24', 9, 'Waiting', '2026-09-24 01:34:16', NULL, NULL),
(15, 15, '2026-09-24', 10, 'Waiting', '2026-09-24 01:43:06', NULL, NULL),
(16, 16, '2026-09-24', 11, 'Waiting', '2026-09-24 01:54:14', NULL, NULL),
(17, 17, '2026-09-24', 12, 'Waiting', '2026-09-24 01:55:09', NULL, NULL),
(18, 18, '2026-09-24', 13, 'Waiting', '2026-09-24 01:55:48', NULL, NULL),
(19, 19, '2026-09-24', 14, 'Waiting', '2026-09-24 02:06:55', NULL, NULL),
(20, 20, '2026-09-24', 15, 'Waiting', '2026-09-24 02:07:21', NULL, NULL),
(21, 21, '2026-09-24', 16, 'Waiting', '2026-09-24 02:07:43', NULL, NULL),
(22, 22, '2026-09-24', 17, 'Waiting', '2026-09-24 02:08:18', NULL, NULL),
(23, 23, '2026-09-24', 18, 'Waiting', '2026-09-24 02:14:23', NULL, NULL),
(24, 24, '2026-09-24', 19, 'Waiting', '2026-09-24 15:26:22', NULL, NULL),
(25, 25, '2026-09-24', 20, 'Waiting', '2026-09-24 15:26:53', NULL, NULL),
(26, 26, '2026-09-24', 21, 'Waiting', '2026-09-24 16:38:11', NULL, NULL),
(29, 29, '2026-09-27', 1, 'Waiting', '2026-09-27 05:48:10', NULL, NULL),
(30, 30, '2026-09-28', 1, 'Waiting', '2026-09-28 23:20:05', NULL, NULL),
(31, 31, '2026-10-01', 1, 'Completed', '2026-10-01 00:41:38', '2026-10-01 00:42:01', '2026-10-01 01:25:11'),
(32, 32, '2026-10-01', 2, 'Completed', '2026-10-01 01:22:52', '2026-10-01 01:23:23', '2026-10-01 01:24:30'),
(33, 33, '2026-10-01', 3, 'Completed', '2026-10-01 01:40:53', '2026-10-01 01:53:20', '2026-10-01 01:56:30'),
(34, 34, '2026-10-01', 4, 'Completed', '2026-10-01 01:52:38', '2026-10-01 01:58:52', '2026-10-01 02:49:26'),
(35, 35, '2026-10-01', 5, 'Completed', '2026-10-01 02:31:10', '2026-10-01 02:49:28', '2026-10-01 18:09:16'),
(36, 36, '2026-10-01', 6, 'Completed', '2026-10-01 02:31:43', '2026-10-01 02:54:09', '2026-10-01 02:54:57'),
(37, 37, '2026-10-01', 7, 'Completed', '2026-10-01 02:32:08', '2026-10-01 03:11:06', '2026-10-01 03:11:25'),
(38, 38, '2026-10-01', 8, 'Completed', '2026-10-01 03:30:58', '2026-10-01 03:31:57', '2026-10-01 03:32:09'),
(39, 39, '2026-10-01', 9, 'No-show', '2026-10-01 04:22:49', '2026-10-01 04:53:44', NULL),
(40, 40, '2026-10-01', 10, 'No-show', '2026-10-01 04:23:12', '2026-10-01 14:02:21', NULL),
(41, 41, '2026-10-01', 11, 'No-show', '2026-10-01 04:23:34', '2026-10-01 14:06:50', NULL),
(42, 42, '2026-10-01', 12, 'Completed', '2026-10-01 04:23:55', '2026-10-01 17:44:01', '2026-10-01 17:44:55'),
(43, 43, '2026-10-01', 13, 'Completed', '2026-10-01 04:24:13', '2026-10-01 18:12:19', '2026-10-01 19:09:16'),
(44, 44, '2026-10-01', 14, 'Completed', '2026-10-01 04:24:34', '2026-10-01 18:36:36', '2026-10-01 18:38:42'),
(45, 45, '2026-10-01', 15, 'No-show', '2026-10-01 04:24:50', '2026-10-01 18:38:52', NULL),
(46, 46, '2026-10-01', 16, 'Completed', '2026-10-01 04:25:06', '2026-10-01 19:10:24', '2026-10-01 19:10:37'),
(47, 47, '2026-10-01', 17, 'Completed', '2026-10-01 04:25:26', '2026-10-01 20:30:13', '2026-10-01 20:32:10'),
(48, 48, '2026-10-01', 18, 'Completed', '2026-10-01 04:25:44', '2026-10-01 20:32:17', '2026-10-01 20:32:58'),
(49, 49, '2026-10-01', 19, 'Completed', '2026-10-01 18:15:31', '2026-10-01 20:33:14', '2026-10-01 20:34:26'),
(50, 50, '2026-10-01', 20, 'Waiting', '2026-10-01 18:33:16', NULL, NULL),
(51, 51, '2026-10-01', 21, 'Waiting', '2026-10-01 18:34:50', NULL, NULL),
(52, 52, '2026-10-01', 22, 'Waiting', '2026-10-01 19:07:53', NULL, NULL),
(53, 53, '2026-10-01', 23, 'Waiting', '2026-10-01 20:15:41', NULL, NULL),
(54, 54, '2026-10-01', 24, 'Waiting', '2026-10-01 20:16:17', NULL, NULL),
(55, 55, '2026-10-01', 25, 'Waiting', '2026-10-01 20:16:55', NULL, NULL),
(56, 56, '2026-10-01', 26, 'Waiting', '2026-10-01 20:17:37', NULL, NULL),
(57, 57, '2026-10-01', 27, 'Waiting', '2026-10-01 20:24:46', NULL, NULL),
(58, 58, '2026-10-02', 1, 'Completed', '2026-10-02 04:14:06', '2026-10-02 04:15:26', '2026-10-02 04:16:22'),
(59, 59, '2026-10-02', 2, 'Completed', '2026-10-02 17:49:24', '2026-10-02 17:50:27', '2026-10-02 17:50:58');

-- --------------------------------------------------------

--
-- Table structure for table `queue_payment_progress`
--

CREATE TABLE `queue_payment_progress` (
  `queue_entry_id` int(11) NOT NULL,
  `received` decimal(12,2) DEFAULT NULL,
  `receipt_printed` tinyint(1) NOT NULL DEFAULT 0,
  `tickets_printed` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `queue_payment_progress`
--

INSERT INTO `queue_payment_progress` (`queue_entry_id`, `received`, `receipt_printed`, `tickets_printed`) VALUES
(31, 800.00, 1, 1),
(32, 500.00, 1, 1),
(33, 500.00, 1, 1),
(34, 500.00, 1, 1),
(35, 400.00, 1, 1),
(36, 500.00, 1, 1),
(37, 400.00, 1, 1),
(38, 50.00, 1, 1),
(42, 500.00, 1, 1),
(43, 400.00, 1, 1),
(44, 1000.00, 1, 1),
(46, 50.00, 1, 1),
(47, 100.00, 1, 1),
(48, 100.00, 1, 1),
(49, 400.00, 1, 1),
(58, 1000.00, 1, 1),
(59, 50.00, 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `queue_stations`
--

CREATE TABLE `queue_stations` (
  `kind` varchar(16) NOT NULL,
  `station` int(11) NOT NULL,
  `queue_entry_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `queue_stations`
--

INSERT INTO `queue_stations` (`kind`, `station`, `queue_entry_id`) VALUES
('Boarding', 1, 36),
('Payment', 2, 45),
('Payment', 1, 59);

-- --------------------------------------------------------

--
-- Table structure for table `routes`
--

CREATE TABLE `routes` (
  `route_id` int(11) NOT NULL,
  `origin` varchar(100) NOT NULL,
  `destination` varchar(100) NOT NULL,
  `fare` decimal(10,2) NOT NULL,
  `status` enum('Active','Inactive') NOT NULL DEFAULT 'Active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `routes`
--

INSERT INTO `routes` (`route_id`, `origin`, `destination`, `fare`, `status`) VALUES
(1, 'PITX', 'Dasmariñas', 0.00, 'Active'),
(2, 'PITX', 'General Mariano Alvarez (GMA)', 0.00, 'Active'),
(3, 'PITX', 'Trece Martires', 0.00, 'Active'),
(4, 'PITX', 'Alfonso', 50.00, 'Active'),
(5, 'PITX', 'Amadeo', 433.00, 'Active'),
(6, 'PITX', 'Mendez', 0.00, 'Active'),
(7, 'PITX', 'Silang', 0.00, 'Active'),
(8, 'PITX', 'Tagaytay', 0.00, 'Active'),
(9, 'PITX', 'Maragondon', 0.00, 'Active'),
(10, 'PITX', 'Naic', 0.00, 'Active'),
(11, 'PITX', 'Ternate', 0.00, 'Active'),
(12, 'PITX', 'Cavite City', 50.00, 'Active'),
(13, 'PITX', 'Lancaster City', 0.00, 'Active'),
(14, 'PITX', 'Molino', 0.00, 'Active'),
(15, 'PITX', 'Paliparan', 0.00, 'Active'),
(16, 'PITX', 'Tanza', 60.00, 'Active'),
(17, 'PITX', 'Balibago', 0.00, 'Active'),
(18, 'PITX', 'Sta. Cruz', 0.00, 'Active'),
(19, 'PITX', 'Batangas City', 0.00, 'Active'),
(20, 'PITX', 'Lipa City', 0.00, 'Active'),
(21, 'PITX', 'San Juan', 0.00, 'Active'),
(22, 'PITX', 'Nasugbu via Aguinaldo Highway', 0.00, 'Active'),
(23, 'PITX', 'Nasugbu via Kaybiang', 0.00, 'Active'),
(24, 'PITX', 'Antipolo City', 0.00, 'Active'),
(25, 'PITX', 'Lucena City', 30.00, 'Active'),
(26, 'PITX', 'Calauag', 0.00, 'Active'),
(27, 'PITX', 'Guinayangan', 0.00, 'Active'),
(28, 'PITX', 'San Andres', 0.00, 'Active'),
(29, 'PITX', 'Tagkawayan', 0.00, 'Active');

-- --------------------------------------------------------

--
-- Table structure for table `seat_reservations`
--

CREATE TABLE `seat_reservations` (
  `seat_reservation_id` int(11) NOT NULL,
  `trip_id` int(11) NOT NULL,
  `booking_passenger_id` int(11) NOT NULL,
  `seat_number` int(11) NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'Active',
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `active_seat_number` int(11) GENERATED ALWAYS AS (case when `status` = 'Active' then `seat_number` else NULL end) STORED
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `seat_reservations`
--

INSERT INTO `seat_reservations` (`seat_reservation_id`, `trip_id`, `booking_passenger_id`, `seat_number`, `status`, `created_at`) VALUES
(23, 6, 23, 1, 'Active', '2026-09-24 00:01:03'),
(24, 6, 24, 2, 'Active', '2026-09-24 00:06:38'),
(25, 7, 25, 1, 'Active', '2026-09-24 00:37:35'),
(26, 7, 26, 2, 'Active', '2026-09-24 00:37:35'),
(27, 7, 27, 5, 'Active', '2026-09-24 00:37:35'),
(28, 7, 28, 6, 'Active', '2026-09-24 00:37:35'),
(29, 7, 29, 10, 'Active', '2026-09-24 00:37:35'),
(30, 7, 30, 9, 'Active', '2026-09-24 00:38:30'),
(31, 7, 31, 14, 'Active', '2026-09-24 00:42:26'),
(32, 7, 32, 13, 'Active', '2026-09-24 00:44:52'),
(33, 7, 33, 3, 'Active', '2026-09-24 00:55:02'),
(34, 7, 34, 4, 'Active', '2026-09-24 01:25:11'),
(35, 7, 35, 8, 'Active', '2026-09-24 01:25:11'),
(36, 7, 36, 7, 'Active', '2026-09-24 01:25:11'),
(37, 7, 37, 11, 'Active', '2026-09-24 01:34:16'),
(38, 7, 38, 12, 'Active', '2026-09-24 01:34:16'),
(39, 7, 39, 15, 'Active', '2026-09-24 01:34:16'),
(40, 7, 40, 16, 'Active', '2026-09-24 01:43:06'),
(41, 7, 41, 17, 'Active', '2026-09-24 01:54:14'),
(42, 7, 42, 18, 'Active', '2026-09-24 01:54:14'),
(43, 7, 43, 22, 'Active', '2026-09-24 01:54:14'),
(44, 7, 44, 21, 'Active', '2026-09-24 01:54:14'),
(45, 7, 45, 25, 'Active', '2026-09-24 01:54:14'),
(46, 7, 46, 26, 'Active', '2026-09-24 01:54:14'),
(47, 7, 47, 30, 'Active', '2026-09-24 01:54:14'),
(48, 7, 48, 29, 'Active', '2026-09-24 01:54:14'),
(49, 7, 49, 19, 'Active', '2026-09-24 01:54:14'),
(50, 7, 50, 20, 'Active', '2026-09-24 01:54:14'),
(51, 7, 51, 23, 'Active', '2026-09-24 01:55:09'),
(52, 7, 52, 27, 'Active', '2026-09-24 01:55:09'),
(53, 7, 53, 24, 'Active', '2026-09-24 01:55:09'),
(54, 7, 54, 28, 'Active', '2026-09-24 01:55:09'),
(55, 7, 55, 32, 'Active', '2026-09-24 01:55:09'),
(56, 7, 56, 31, 'Active', '2026-09-24 01:55:09'),
(57, 7, 57, 34, 'Active', '2026-09-24 01:55:09'),
(58, 7, 58, 38, 'Active', '2026-09-24 01:55:09'),
(59, 7, 59, 33, 'Active', '2026-09-24 01:55:09'),
(60, 7, 60, 37, 'Active', '2026-09-24 01:55:09'),
(61, 7, 61, 35, 'Active', '2026-09-24 01:55:48'),
(62, 7, 62, 39, 'Active', '2026-09-24 01:55:48'),
(63, 7, 63, 36, 'Active', '2026-09-24 01:55:48'),
(64, 7, 64, 40, 'Active', '2026-09-24 01:55:48'),
(65, 8, 65, 2, 'Active', '2026-09-24 02:06:55'),
(66, 8, 66, 1, 'Active', '2026-09-24 02:06:55'),
(67, 8, 67, 5, 'Active', '2026-09-24 02:06:55'),
(68, 8, 68, 6, 'Active', '2026-09-24 02:06:55'),
(69, 8, 69, 10, 'Active', '2026-09-24 02:06:55'),
(70, 8, 70, 9, 'Active', '2026-09-24 02:06:55'),
(71, 8, 71, 13, 'Active', '2026-09-24 02:06:55'),
(72, 8, 72, 14, 'Active', '2026-09-24 02:06:55'),
(73, 8, 73, 3, 'Active', '2026-09-24 02:06:55'),
(74, 8, 74, 4, 'Active', '2026-09-24 02:06:55'),
(75, 8, 75, 7, 'Active', '2026-09-24 02:07:21'),
(76, 8, 76, 8, 'Active', '2026-09-24 02:07:21'),
(77, 8, 77, 12, 'Active', '2026-09-24 02:07:21'),
(78, 8, 78, 11, 'Active', '2026-09-24 02:07:21'),
(79, 8, 79, 16, 'Active', '2026-09-24 02:07:21'),
(80, 8, 80, 15, 'Active', '2026-09-24 02:07:21'),
(81, 8, 81, 17, 'Active', '2026-09-24 02:07:21'),
(82, 8, 82, 18, 'Active', '2026-09-24 02:07:21'),
(83, 8, 83, 19, 'Active', '2026-09-24 02:07:21'),
(84, 8, 84, 20, 'Active', '2026-09-24 02:07:43'),
(85, 11, 85, 2, 'Active', '2026-09-24 02:08:18'),
(86, 11, 86, 1, 'Active', '2026-09-24 02:08:18'),
(87, 11, 87, 5, 'Active', '2026-09-24 02:08:18'),
(88, 9, 88, 1, 'Active', '2026-09-24 02:14:23'),
(89, 13, 89, 1, 'Active', '2026-09-24 15:26:22'),
(90, 13, 90, 2, 'Active', '2026-09-24 15:26:22'),
(91, 13, 91, 5, 'Active', '2026-09-24 15:26:22'),
(92, 13, 92, 6, 'Active', '2026-09-24 15:26:22'),
(93, 13, 93, 9, 'Active', '2026-09-24 15:26:22'),
(94, 13, 94, 10, 'Active', '2026-09-24 15:26:22'),
(95, 13, 95, 19, 'Active', '2026-09-24 15:26:22'),
(96, 13, 96, 20, 'Active', '2026-09-24 15:26:22'),
(97, 13, 97, 23, 'Active', '2026-09-24 15:26:22'),
(98, 13, 98, 24, 'Active', '2026-09-24 15:26:22'),
(99, 13, 99, 26, 'Active', '2026-09-24 15:26:53'),
(100, 13, 100, 25, 'Active', '2026-09-24 15:26:53'),
(101, 13, 101, 29, 'Active', '2026-09-24 15:26:53'),
(102, 13, 102, 30, 'Active', '2026-09-24 15:26:53'),
(103, 13, 103, 22, 'Active', '2026-09-24 15:26:53'),
(104, 13, 104, 21, 'Active', '2026-09-24 15:26:53'),
(105, 13, 105, 17, 'Active', '2026-09-24 15:26:53'),
(106, 13, 106, 18, 'Active', '2026-09-24 15:26:53'),
(107, 13, 107, 27, 'Active', '2026-09-24 15:26:53'),
(108, 13, 108, 28, 'Active', '2026-09-24 15:26:53'),
(109, 11, 109, 6, 'Active', '2026-09-24 16:38:11'),
(110, 11, 110, 10, 'Active', '2026-09-24 16:38:11'),
(111, 11, 111, 9, 'Active', '2026-09-24 16:38:11'),
(112, 11, 112, 13, 'Active', '2026-09-24 16:38:11'),
(113, 11, 113, 14, 'Active', '2026-09-24 16:38:11'),
(114, 11, 114, 32, 'Active', '2026-09-24 16:38:11'),
(115, 11, 115, 28, 'Active', '2026-09-24 16:38:11'),
(116, 11, 116, 24, 'Active', '2026-09-24 16:38:11'),
(117, 11, 117, 20, 'Active', '2026-09-24 16:38:11'),
(118, 11, 118, 19, 'Active', '2026-09-24 16:38:11'),
(134, 15, 134, 6, 'Active', '2026-09-27 05:48:10'),
(135, 16, 135, 6, 'Active', '2026-09-28 23:20:05'),
(136, 19, 136, 1, 'Active', '2026-10-01 00:41:38'),
(137, 19, 137, 2, 'Active', '2026-10-01 00:41:38'),
(138, 19, 138, 5, 'Active', '2026-10-01 00:41:38'),
(139, 19, 139, 6, 'Active', '2026-10-01 00:41:38'),
(140, 19, 140, 10, 'Active', '2026-10-01 00:41:38'),
(141, 19, 141, 9, 'Active', '2026-10-01 00:41:38'),
(142, 19, 142, 13, 'Active', '2026-10-01 00:41:38'),
(143, 19, 143, 14, 'Active', '2026-10-01 00:41:38'),
(144, 19, 144, 3, 'Active', '2026-10-01 00:41:38'),
(145, 19, 145, 4, 'Active', '2026-10-01 00:41:38'),
(146, 19, 146, 7, 'Active', '2026-10-01 01:22:52'),
(147, 19, 147, 11, 'Active', '2026-10-01 01:40:53'),
(148, 19, 148, 8, 'Active', '2026-10-01 01:52:38'),
(149, 20, 149, 10, 'Active', '2026-10-01 02:31:10'),
(150, 20, 150, 9, 'Active', '2026-10-01 02:31:43'),
(151, 20, 151, 6, 'Active', '2026-10-01 02:31:43'),
(152, 20, 152, 1, 'Active', '2026-10-01 02:32:08'),
(153, 20, 153, 7, 'Active', '2026-10-01 03:30:58'),
(154, 20, 154, 15, 'Expired', '2026-10-01 04:22:49'),
(155, 20, 155, 16, 'Expired', '2026-10-01 04:23:12'),
(156, 20, 156, 12, 'Expired', '2026-10-01 04:23:34'),
(157, 20, 157, 8, 'Expired', '2026-10-01 04:23:34'),
(158, 20, 158, 11, 'Expired', '2026-10-01 04:23:34'),
(159, 20, 159, 3, 'Active', '2026-10-01 04:23:55'),
(160, 20, 160, 4, 'Active', '2026-10-01 04:24:13'),
(161, 20, 161, 14, 'Active', '2026-10-01 04:24:34'),
(162, 20, 162, 13, 'Expired', '2026-10-01 04:24:50'),
(163, 20, 163, 5, 'Active', '2026-10-01 04:25:06'),
(164, 20, 164, 2, 'Active', '2026-10-01 04:25:26'),
(165, 20, 165, 19, 'Active', '2026-10-01 04:25:44'),
(166, 20, 166, 20, 'Active', '2026-10-01 18:15:31'),
(167, 20, 167, 24, 'Active', '2026-10-01 18:15:31'),
(168, 20, 168, 23, 'Active', '2026-10-01 18:15:31'),
(169, 21, 169, 45, 'Active', '2026-10-01 18:33:16'),
(170, 21, 170, 6, 'Active', '2026-10-01 18:34:50'),
(171, 21, 171, 10, 'Active', '2026-10-01 18:34:50'),
(172, 21, 172, 9, 'Active', '2026-10-01 18:34:50'),
(173, 21, 173, 14, 'Active', '2026-10-01 19:07:53'),
(174, 21, 174, 5, 'Active', '2026-10-01 20:15:40'),
(175, 21, 175, 1, 'Active', '2026-10-01 20:15:40'),
(176, 21, 176, 2, 'Active', '2026-10-01 20:15:40'),
(177, 21, 177, 3, 'Active', '2026-10-01 20:15:40'),
(178, 21, 178, 4, 'Active', '2026-10-01 20:15:40'),
(179, 21, 179, 7, 'Active', '2026-10-01 20:15:40'),
(180, 21, 180, 8, 'Active', '2026-10-01 20:15:40'),
(181, 21, 181, 11, 'Active', '2026-10-01 20:15:40'),
(182, 21, 182, 12, 'Active', '2026-10-01 20:15:40'),
(183, 21, 183, 13, 'Active', '2026-10-01 20:15:40'),
(184, 21, 184, 15, 'Active', '2026-10-01 20:16:17'),
(185, 21, 185, 16, 'Active', '2026-10-01 20:16:17'),
(186, 21, 186, 19, 'Active', '2026-10-01 20:16:17'),
(187, 21, 187, 23, 'Active', '2026-10-01 20:16:17'),
(188, 21, 188, 20, 'Active', '2026-10-01 20:16:17'),
(189, 21, 189, 24, 'Active', '2026-10-01 20:16:17'),
(190, 21, 190, 27, 'Active', '2026-10-01 20:16:17'),
(191, 21, 191, 28, 'Active', '2026-10-01 20:16:17'),
(192, 21, 192, 31, 'Active', '2026-10-01 20:16:17'),
(193, 21, 193, 32, 'Active', '2026-10-01 20:16:17'),
(194, 21, 194, 17, 'Active', '2026-10-01 20:16:54'),
(195, 21, 195, 18, 'Active', '2026-10-01 20:16:54'),
(196, 21, 196, 21, 'Active', '2026-10-01 20:16:54'),
(197, 21, 197, 26, 'Active', '2026-10-01 20:16:55'),
(198, 21, 198, 25, 'Active', '2026-10-01 20:16:55'),
(199, 21, 199, 22, 'Active', '2026-10-01 20:16:55'),
(200, 21, 200, 30, 'Active', '2026-10-01 20:16:55'),
(201, 21, 201, 29, 'Active', '2026-10-01 20:16:55'),
(202, 21, 202, 35, 'Active', '2026-10-01 20:16:55'),
(203, 21, 203, 36, 'Active', '2026-10-01 20:16:55'),
(204, 21, 204, 40, 'Active', '2026-10-01 20:17:37'),
(205, 21, 205, 44, 'Active', '2026-10-01 20:17:37'),
(206, 21, 206, 39, 'Active', '2026-10-01 20:17:37'),
(207, 21, 207, 43, 'Active', '2026-10-01 20:17:37'),
(208, 21, 208, 42, 'Active', '2026-10-01 20:17:37'),
(209, 21, 209, 38, 'Active', '2026-10-01 20:17:37'),
(210, 21, 210, 34, 'Active', '2026-10-01 20:17:37'),
(211, 21, 211, 33, 'Active', '2026-10-01 20:17:37'),
(212, 21, 212, 37, 'Active', '2026-10-01 20:24:46'),
(213, 21, 213, 41, 'Active', '2026-10-01 20:24:46'),
(214, 24, 214, 10, 'Active', '2026-10-02 04:14:06'),
(215, 24, 215, 6, 'Active', '2026-10-02 04:14:06'),
(216, 24, 216, 2, 'Active', '2026-10-02 04:14:06'),
(217, 24, 217, 1, 'Active', '2026-10-02 04:14:06'),
(218, 24, 218, 5, 'Active', '2026-10-02 04:14:06'),
(219, 24, 219, 9, 'Active', '2026-10-02 04:14:06'),
(220, 24, 220, 13, 'Active', '2026-10-02 04:14:06'),
(221, 24, 221, 14, 'Active', '2026-10-02 04:14:06'),
(222, 24, 222, 15, 'Active', '2026-10-02 04:14:06'),
(223, 24, 223, 11, 'Active', '2026-10-02 04:14:06'),
(224, 25, 224, 1, 'Active', '2026-10-02 17:49:24');

-- --------------------------------------------------------

--
-- Table structure for table `trips`
--

CREATE TABLE `trips` (
  `trip_id` int(11) NOT NULL,
  `bus_id` int(11) NOT NULL,
  `route_id` int(11) NOT NULL,
  `departure_date` date NOT NULL,
  `departure_time` time NOT NULL,
  `available_seats` int(11) NOT NULL,
  `status` enum('Scheduled','Boarding','Departed','Cancelled') NOT NULL DEFAULT 'Scheduled'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `trips`
--

INSERT INTO `trips` (`trip_id`, `bus_id`, `route_id`, `departure_date`, `departure_time`, `available_seats`, `status`) VALUES
(5, 5, 4, '2026-09-23', '00:30:00', 40, 'Departed'),
(6, 6, 5, '2026-09-24', '00:30:00', 18, 'Departed'),
(7, 5, 5, '2026-09-25', '03:30:00', 0, 'Departed'),
(8, 6, 5, '2026-09-25', '00:00:00', 0, 'Departed'),
(9, 7, 5, '2026-09-25', '00:00:00', 19, 'Departed'),
(10, 8, 5, '2026-09-12', '00:00:00', 30, 'Departed'),
(11, 9, 5, '2026-09-25', '00:00:00', 27, 'Departed'),
(12, 10, 5, '2026-09-25', '00:00:00', 20, 'Departed'),
(13, 11, 12, '2026-09-25', '00:00:00', 20, 'Departed'),
(15, 5, 4, '2026-09-29', '03:30:00', 39, 'Departed'),
(16, 6, 4, '2026-09-29', '02:30:00', 19, 'Departed'),
(18, 5, 4, '2026-08-04', '03:30:00', 40, 'Departed'),
(19, 5, 4, '2026-10-01', '02:00:00', 27, 'Departed'),
(20, 5, 4, '2026-10-03', '00:00:00', 26, 'Boarding'),
(21, 12, 16, '2026-10-01', '23:00:00', 0, 'Departed'),
(22, 14, 5, '2026-10-01', '23:00:00', 25, 'Departed'),
(23, 5, 5, '2026-11-12', '00:30:00', 40, 'Scheduled'),
(24, 6, 4, '2026-10-03', '00:00:00', 10, 'Scheduled'),
(25, 8, 4, '2026-10-03', '00:00:00', 29, 'Scheduled');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD PRIMARY KEY (`log_id`);

--
-- Indexes for table `boarding_gates`
--
ALTER TABLE `boarding_gates`
  ADD PRIMARY KEY (`gate`),
  ADD UNIQUE KEY `trip_id` (`trip_id`);

--
-- Indexes for table `boarding_skips`
--
ALTER TABLE `boarding_skips`
  ADD PRIMARY KEY (`queue_entry_id`);

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`booking_id`),
  ADD UNIQUE KEY `uq_bookings_reference` (`booking_reference`),
  ADD UNIQUE KEY `uq_bookings_id_trip` (`booking_id`,`trip_id`),
  ADD KEY `idx_bookings_trip` (`trip_id`),
  ADD KEY `idx_bookings_status` (`status`),
  ADD KEY `idx_bookings_created_at` (`created_at`);

--
-- Indexes for table `booking_passengers`
--
ALTER TABLE `booking_passengers`
  ADD PRIMARY KEY (`booking_passenger_id`),
  ADD UNIQUE KEY `uq_booking_passenger_trip` (`booking_passenger_id`,`trip_id`),
  ADD KEY `idx_booking_passengers_booking` (`booking_id`),
  ADD KEY `idx_booking_passengers_trip` (`trip_id`),
  ADD KEY `fk_booking_passengers_booking_trip` (`booking_id`,`trip_id`);

--
-- Indexes for table `buses`
--
ALTER TABLE `buses`
  ADD PRIMARY KEY (`bus_id`),
  ADD UNIQUE KEY `bus_number` (`bus_number`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`payment_id`),
  ADD KEY `idx_payment_status_date` (`status`,`paid_at`),
  ADD KEY `fk_payment_trip` (`trip_id`),
  ADD KEY `idx_payments_booking` (`booking_id`);

--
-- Indexes for table `queue_boarding`
--
ALTER TABLE `queue_boarding`
  ADD PRIMARY KEY (`queue_entry_id`);

--
-- Indexes for table `queue_daily_counters`
--
ALTER TABLE `queue_daily_counters`
  ADD PRIMARY KEY (`queue_date`);

--
-- Indexes for table `queue_entries`
--
ALTER TABLE `queue_entries`
  ADD PRIMARY KEY (`queue_entry_id`),
  ADD UNIQUE KEY `uq_queue_booking` (`booking_id`),
  ADD UNIQUE KEY `uq_queue_date_number` (`queue_date`,`queue_number`),
  ADD KEY `idx_queue_date_status` (`queue_date`,`status`),
  ADD KEY `idx_queue_status` (`status`),
  ADD KEY `idx_queue_created_at` (`created_at`);

--
-- Indexes for table `queue_payment_progress`
--
ALTER TABLE `queue_payment_progress`
  ADD PRIMARY KEY (`queue_entry_id`);

--
-- Indexes for table `queue_stations`
--
ALTER TABLE `queue_stations`
  ADD PRIMARY KEY (`kind`,`station`),
  ADD UNIQUE KEY `assigned_queue` (`kind`,`queue_entry_id`);

--
-- Indexes for table `routes`
--
ALTER TABLE `routes`
  ADD PRIMARY KEY (`route_id`);

--
-- Indexes for table `seat_reservations`
--
ALTER TABLE `seat_reservations`
  ADD PRIMARY KEY (`seat_reservation_id`),
  ADD UNIQUE KEY `uq_active_trip_seat` (`trip_id`,`active_seat_number`),
  ADD KEY `idx_seat_reservations_trip` (`trip_id`),
  ADD KEY `idx_seat_reservations_passenger` (`booking_passenger_id`),
  ADD KEY `idx_seat_reservations_status` (`status`),
  ADD KEY `fk_seat_reservations_passenger_trip` (`booking_passenger_id`,`trip_id`);

--
-- Indexes for table `trips`
--
ALTER TABLE `trips`
  ADD PRIMARY KEY (`trip_id`),
  ADD KEY `fk_trip_bus` (`bus_id`),
  ADD KEY `fk_trip_route` (`route_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `accounts`
--
ALTER TABLE `accounts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `activity_logs`
--
ALTER TABLE `activity_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `booking_passengers`
--
ALTER TABLE `booking_passengers`
  MODIFY `booking_passenger_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=225;

--
-- AUTO_INCREMENT for table `buses`
--
ALTER TABLE `buses`
  MODIFY `bus_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=16;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `payment_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `queue_entries`
--
ALTER TABLE `queue_entries`
  MODIFY `queue_entry_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=60;

--
-- AUTO_INCREMENT for table `routes`
--
ALTER TABLE `routes`
  MODIFY `route_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `seat_reservations`
--
ALTER TABLE `seat_reservations`
  MODIFY `seat_reservation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=225;

--
-- AUTO_INCREMENT for table `trips`
--
ALTER TABLE `trips`
  MODIFY `trip_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `fk_bookings_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`trip_id`) ON UPDATE CASCADE;

--
-- Constraints for table `booking_passengers`
--
ALTER TABLE `booking_passengers`
  ADD CONSTRAINT `fk_booking_passengers_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_booking_passengers_booking_trip` FOREIGN KEY (`booking_id`,`trip_id`) REFERENCES `bookings` (`booking_id`, `trip_id`) ON UPDATE CASCADE;

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `fk_payment_trip` FOREIGN KEY (`trip_id`) REFERENCES `trips` (`trip_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_payments_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `queue_boarding`
--
ALTER TABLE `queue_boarding`
  ADD CONSTRAINT `queue_boarding_ibfk_1` FOREIGN KEY (`queue_entry_id`) REFERENCES `queue_entries` (`queue_entry_id`) ON DELETE CASCADE;

--
-- Constraints for table `queue_entries`
--
ALTER TABLE `queue_entries`
  ADD CONSTRAINT `fk_queue_entries_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`) ON UPDATE CASCADE;

--
-- Constraints for table `queue_payment_progress`
--
ALTER TABLE `queue_payment_progress`
  ADD CONSTRAINT `queue_payment_progress_ibfk_1` FOREIGN KEY (`queue_entry_id`) REFERENCES `queue_entries` (`queue_entry_id`) ON DELETE CASCADE;

--
-- Constraints for table `seat_reservations`
--
ALTER TABLE `seat_reservations`
  ADD CONSTRAINT `fk_seat_reservations_passenger_trip` FOREIGN KEY (`booking_passenger_id`,`trip_id`) REFERENCES `booking_passengers` (`booking_passenger_id`, `trip_id`) ON UPDATE CASCADE;

--
-- Constraints for table `trips`
--
ALTER TABLE `trips`
  ADD CONSTRAINT `fk_trip_bus` FOREIGN KEY (`bus_id`) REFERENCES `buses` (`bus_id`),
  ADD CONSTRAINT `fk_trip_route` FOREIGN KEY (`route_id`) REFERENCES `routes` (`route_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

-- Selected drop-off; older bookings continue to use the route destination.
CREATE TABLE IF NOT EXISTS booking_dropoffs (
  booking_id INT NOT NULL PRIMARY KEY,
  drop_off VARCHAR(150) NOT NULL
);
