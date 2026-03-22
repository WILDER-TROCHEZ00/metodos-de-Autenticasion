<?php
/**
 * Script para autenticar usuarios y generar un Bearer Token dinámico.
 */
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Content-Type: application/json");

// Conexión a la base de datos MySQL
$conn = new mysqli("localhost", "root", "", "dbpm01");

// Leer los datos JSON enviados por la App de Android
$entrada = json_decode(file_get_contents('php://input'), true);
$user = $entrada['usuario'] ?? '';
$pass = $entrada['password'] ?? '';

// 1. Validar credenciales contra la tabla de usuarios
$stmt = $conn->prepare("SELECT id FROM usuarios WHERE usuario = ? AND password = ?");
$stmt->bind_param("ss", $user, $pass);
$stmt->execute();
$res = $stmt->get_result();

if ($user_row = $res->fetch_assoc()) {
    // 2. Generar un token aleatorio único para esta sesión
    $nuevoToken = bin2hex(random_bytes(20)); 
    $userId = $user_row['id'];

    // 3. Guardar el nuevo token en la base de datos para futuras validaciones
    $updateStmt = $conn->prepare("UPDATE usuarios SET token = ? WHERE id = ?");
    $updateStmt->bind_param("si", $nuevoToken, $userId);
    $updateStmt->execute();

    // 4. Retornar éxito y el token al cliente
    echo json_encode(["status" => "success", "token" => $nuevoToken]);
} else {
    // Retornar error 401 si las credenciales fallan
    http_response_code(401);
    echo json_encode(["status" => "error", "message" => "Usuario o clave incorrectos"]);
}
$conn->close();
?>