<?php
// Cloudflare is actually sending real user IP but in a different header which is HTTP_CF_CONNECTING_IP
$ip = isset($_SERVER['HTTP_CF_CONNECTING_IP']) ? $_SERVER['HTTP_CF_CONNECTING_IP'] : $_SERVER['REMOTE_ADDR'];
header('Content-Type: application/json; charset=utf-8');
echo "{\"ip\":\"$ip\"}";
?>