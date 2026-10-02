-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Sep 26, 2026 at 05:08 PM
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
(27, 'BK-dfd34b3d0fed44eb8f1e9c3eb2', 14, 300.00, 'Pending', '2026-09-24 17:08:08'),
(28, 'BK-ca7aa211954f43e491594f1c31', 14, 150.00, 'Pending', '2026-09-24 17:08:54');

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
(119, 27, 14, 'Passenger 1', 'Student', 30.00),
(120, 27, 14, 'Passenger 2', 'Student', 30.00),
(121, 27, 14, 'Passenger 3', 'Student', 30.00),
(122, 27, 14, 'Passenger 4', 'Student', 30.00),
(123, 27, 14, 'Passenger 5', 'Student', 30.00),
(124, 27, 14, 'Passenger 6', 'Student', 30.00),
(125, 27, 14, 'Passenger 7', 'Student', 30.00),
(126, 27, 14, 'Passenger 8', 'Student', 30.00),
(127, 27, 14, 'Passenger 9', 'Student', 30.00),
(128, 27, 14, 'Passenger 10', 'Student', 30.00),
(129, 28, 14, 'Passenger 1', 'Student', 30.00),
(130, 28, 14, 'Passenger 2', 'Student', 30.00),
(131, 28, 14, 'Passenger 3', 'Student', 30.00),
(132, 28, 14, 'Passenger 4', 'Student', 30.00),
(133, 28, 14, 'Passenger 5', 'Student', 30.00);

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
(7, 'Bus03', 20, 20, 'Available'),
(8, 'Bus04', 30, 30, 'Available'),
(9, 'Bus05', 40, 40, 'Available'),
(10, 'Bus06', 20, 20, 'Available'),
(11, 'Bus07', 40, 40, 'Available'),
(12, 'Bus10', 45, 45, 'Available');

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
(27, 14, 'Passenger 1', 300.00, 'Cash', 'Pending', '2026-09-24 17:08:08', NULL, 27),
(28, 14, 'Passenger 1', 150.00, 'Cash', 'Pending', '2026-09-24 17:08:54', NULL, 28);

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
('2026-09-24', 23);

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
(27, 27, '2026-09-24', 22, 'Waiting', '2026-09-24 17:08:08', NULL, NULL),
(28, 28, '2026-09-24', 23, 'Waiting', '2026-09-24 17:08:54', NULL, NULL);

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
(5, 'PITX', 'Amadeo', 30.00, 'Active'),
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
(16, 'PITX', 'Tanza', 0.00, 'Active'),
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
(119, 14, 119, 10, 'Active', '2026-09-24 17:08:08'),
(120, 14, 120, 2, 'Active', '2026-09-24 17:08:08'),
(121, 14, 121, 1, 'Active', '2026-09-24 17:08:08'),
(122, 14, 122, 9, 'Active', '2026-09-24 17:08:08'),
(123, 14, 123, 7, 'Active', '2026-09-24 17:08:08'),
(124, 14, 124, 4, 'Active', '2026-09-24 17:08:08'),
(125, 14, 125, 8, 'Active', '2026-09-24 17:08:08'),
(126, 14, 126, 3, 'Active', '2026-09-24 17:08:08'),
(127, 14, 127, 11, 'Active', '2026-09-24 17:08:08'),
(128, 14, 128, 12, 'Active', '2026-09-24 17:08:08'),
(129, 14, 129, 6, 'Active', '2026-09-24 17:08:54'),
(130, 14, 130, 5, 'Active', '2026-09-24 17:08:54'),
(131, 14, 131, 13, 'Active', '2026-09-24 17:08:54'),
(132, 14, 132, 14, 'Active', '2026-09-24 17:08:54'),
(133, 14, 133, 15, 'Active', '2026-09-24 17:08:54');

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
(14, 12, 5, '2026-09-25', '22:30:00', 30, 'Departed');

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
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `booking_passengers`
--
ALTER TABLE `booking_passengers`
  MODIFY `booking_passenger_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=134;

--
-- AUTO_INCREMENT for table `buses`
--
ALTER TABLE `buses`
  MODIFY `bus_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `payment_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `queue_entries`
--
ALTER TABLE `queue_entries`
  MODIFY `queue_entry_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `routes`
--
ALTER TABLE `routes`
  MODIFY `route_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT for table `seat_reservations`
--
ALTER TABLE `seat_reservations`
  MODIFY `seat_reservation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=134;

--
-- AUTO_INCREMENT for table `trips`
--
ALTER TABLE `trips`
  MODIFY `trip_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

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
-- Import into qpal for a new installation. Existing records are preserved.
CREATE TABLE IF NOT EXISTS activity_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_id INT NULL,
    user_name VARCHAR(100) NOT NULL DEFAULT '',
    email VARCHAR(100) NOT NULL,
    role VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    INDEX activity_account (account_id, log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
