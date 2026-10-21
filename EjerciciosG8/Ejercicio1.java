import java.sql.*;
import java.util.Scanner;

public class Ejercicio1 {

    static Scanner sc = new Scanner(System.in);
    static final String CLAVE = "1234";

    public static void main(String[] args) {

        try {
            Class.forName("org.sqlite.JDBC");

            try (Connection conexion =
                    DriverManager.getConnection("jdbc:sqlite:tienda.db")) {

                String tabla = "CREATE TABLE IF NOT EXISTS producto ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + "nombre TEXT NOT NULL, "
                        + "precio REAL NOT NULL CHECK(precio >= 0), "
                        + "stock INTEGER NOT NULL CHECK(stock >= 0))";

                try (Statement sentencia = conexion.createStatement()) {
                    sentencia.executeUpdate(tabla);
                }

                int opcion;

                do {
                    System.out.println("\nMENU DE PRODUCTOS");
                    System.out.println("1. Ingresar producto");
                    System.out.println("2. Borrar producto");
                    System.out.println("3. Actualizar producto");
                    System.out.println("4. Mostrar productos");
                    System.out.println("0. Salir");

                    opcion = leerEntero("Seleccione una opcion: ");

                    try {
                        switch (opcion) {
                            case 1:
                                ingresar(conexion);
                                break;

                            case 2:
                                borrar(conexion);
                                break;

                            case 3:
                                actualizar(conexion);
                                break;

                            case 4:
                                mostrar(conexion);
                                break;

                            case 0:
                                System.out.println("Programa finalizado.");
                                break;

                            default:
                                System.out.println("Opcion incorrecta.");
                        }

                    } catch (SQLException e) {
                        System.out.println("Error: " + e.getMessage());
                    }

                } while (opcion != 0);
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Error: " + e.getMessage());

        } catch (SQLException e) {
            System.out.println("Error de conexion: " + e.getMessage());
        }
    }

    public static void ingresar(Connection conexion) throws SQLException {

        String nombre = leerNombre();
        double precio = leerPrecio();
        int stock = leerStock();

        String sql = "INSERT INTO producto(nombre, precio, stock) "
                + "VALUES (?, ?, ?)";

        ejecutarCambio(conexion, sql, nombre, precio, stock);
    }

    public static void borrar(Connection conexion) throws SQLException {

        int id = leerEntero("ID del producto que desea borrar: ");

        String sql = "DELETE FROM producto WHERE id = ?";

        ejecutarCambio(conexion, sql, id);
    }

    public static void actualizar(Connection conexion) throws SQLException {

        int id = leerEntero("ID del producto que desea actualizar: ");

        System.out.println("Ingrese los nuevos datos:");

        String nombre = leerNombre();
        double precio = leerPrecio();
        int stock = leerStock();

        String sql = "UPDATE producto "
                + "SET nombre = ?, precio = ?, stock = ? WHERE id = ?";

        ejecutarCambio(conexion, sql, nombre, precio, stock, id);
    }

    public static void ejecutarCambio(
            Connection conexion,
            String sql,
            Object... datos) throws SQLException {

        // Desactiva el guardado automatico.
        conexion.setAutoCommit(false);

        try {
            int filas;

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sql)) {

                for (int i = 0; i < datos.length; i++) {
                    sentencia.setObject(i + 1, datos[i]);
                }

                filas = sentencia.executeUpdate();
            }

            if (filas == 0) {
                conexion.rollback();
                System.out.println("No se encontro el producto.");
                return;
            }

            System.out.print("Ingrese la clave para confirmar: ");
            String claveIngresada = sc.nextLine();

            if (CLAVE.equals(claveIngresada)) {

                conexion.commit();
                System.out.println("Clave correcta. Cambios guardados.");

            } else {

                conexion.rollback();
                System.out.println("Clave incorrecta. Cambios revertidos.");
            }

        } catch (SQLException e) {

            conexion.rollback();
            throw e;

        } finally {

            conexion.setAutoCommit(true);
        }
    }

    public static void mostrar(Connection conexion) throws SQLException {

        String sql = "SELECT id, nombre, precio, stock "
                + "FROM producto ORDER BY id";

        try (Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery(sql)) {

            System.out.println("\nMENU DE PRODUCTOS");

            System.out.printf(
                    "%-6s %-25s %-12s %-8s%n",
                    "ID", "NOMBRE", "PRECIO", "STOCK"
            );

            boolean hayRegistros = false;

            while (resultado.next()) {

                hayRegistros = true;

                System.out.printf(
                        java.util.Locale.US,
                        "%-6d %-25s %-12.2f %-8d%n",
                        resultado.getInt("id"),
                        resultado.getString("nombre"),
                        resultado.getDouble("precio"),
                        resultado.getInt("stock")
                );
            }

            if (!hayRegistros) {
                System.out.println("No hay productos registrados.");
            }
        }
    }

    public static int leerEntero(String mensaje) {

        while (true) {
            System.out.print(mensaje);

            try {
                return Integer.parseInt(sc.nextLine().trim());

            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero entero valido.");
            }
        }
    }

    public static String leerNombre() {

        while (true) {
            System.out.print("Nombre: ");
            String nombre = sc.nextLine().trim();

            if (!nombre.isEmpty()) {
                return nombre;
            }

            System.out.println("El nombre no puede estar vacio.");
        }
    }

    public static double leerPrecio() {

        while (true) {
            System.out.print("Precio: ");

            try {
                double precio = Double.parseDouble(
                        sc.nextLine().trim().replace(',', '.')
                );

                if (Double.isFinite(precio) && precio >= 0) {
                    return precio;
                }

            } catch (NumberFormatException e) {
            }

            System.out.println("Ingrese un precio valido y no negativo.");
        }
    }

    public static int leerStock() {

        while (true) {
            int stock = leerEntero("Stock: ");

            if (stock >= 0) {
                return stock;
            }

            System.out.println("El stock no puede ser negativo.");
        }
    }
}