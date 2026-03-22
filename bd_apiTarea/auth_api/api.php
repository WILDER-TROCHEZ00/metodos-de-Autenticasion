<?php
/**
 * API REST para el CRUD de productos con protección Bearer Token.
 */
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Authorization, Content-Type");
header("Content-Type: application/json");

// Manejo de peticiones de comprobación (CORS)
if ($_SERVER['REQUEST_METHOD'] == 'OPTIONS') exit;

$conn = new mysqli("localhost", "root", "", "dbpm01");

// --- SEGURIDAD: Validación del Bearer Token ---
$headers = apache_request_headers();
$auth = $headers['Authorization'] ?? '';
$token = str_replace('Bearer ', '', $auth); // Extraer el token puro

// Verificar si el token existe en la base de datos
$stmtT = $conn->prepare("SELECT id FROM usuarios WHERE token = ?");
$stmtT->bind_param("s", $token);
$stmtT->execute();
if ($stmtT->get_result()->num_rows === 0) {
    http_response_code(401);
    echo json_encode(["error" => "No autorizado: Token inválido"]);
    exit;
}

// --- CRUD: Operaciones según el método HTTP ---
$metodo = $_SERVER['REQUEST_METHOD'];
$entrada = json_decode(file_get_contents('php://input'), true);

switch($metodo) {
    case 'GET': // LEER: Retorna todos los productos
        $res = $conn->query("SELECT * FROM productos");
        echo json_encode($res->fetch_all(MYSQLI_ASSOC));
        break;

    case 'POST': // CREAR: Inserta un nuevo producto
        $stmt = $conn->prepare("INSERT INTO productos (nombre, descripcion, precio) VALUES (?, ?, ?)");
        $stmt->bind_param("ssd", $entrada['nombre'], $entrada['descripcion'], $entrada['precio']);
        $stmt->execute();
        echo json_encode(["mensaje" => "Producto creado"]);
        break;

    case 'PUT': // ACTUALIZAR: Modifica un producto por ID
        $stmt = $conn->prepare("UPDATE productos SET nombre=?, descripcion=?, precio=? WHERE id=?");
        $stmt->bind_param("ssdi", $entrada['nombre'], $entrada['descripcion'], $entrada['precio'], $entrada['id']);
        $stmt->execute();
        echo json_encode(["mensaje" => "Producto actualizado"]);
        break;

    case 'DELETE': // ELIMINAR: Borra un producto por ID enviado por URL
        $id = $_GET['id'];
        $conn->query("DELETE FROM productos WHERE id=$id");
        echo json_encode(["mensaje" => "Producto eliminado"]);
        break;
}
$conn->close();
?>