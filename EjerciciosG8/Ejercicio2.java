import java.sql.*;
import java.util.*;

public class Ejercicio2 {
    static Scanner sc = new Scanner(System.in);

    static String pedir(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection cn = DriverManager.getConnection("jdbc:sqlite:tienda.db")) {
                Gestora gestora = new Gestora();
                gestora.cargar(cn);

                System.out.println("CONSULTA DE PRODUCTOS");
                System.out.println("Campos: id, nombre, precio, stock");

                String seleccion = pedir("Campos separados por comas (* = todos): ");
                String[] campos = seleccion.equals("*")
                        ? new String[]{"id", "nombre", "precio", "stock"}
                        : seleccion.toLowerCase(Locale.ROOT).split(",", -1);

                String filtro = pedir("Campo para filtrar (Enter = ninguno): ")
                        .toLowerCase(Locale.ROOT);
                String operador = "";
                String valor = "";
                if (!filtro.isEmpty()) {
                    operador = pedir("Operador (=, !=, >, <, >=, <=): ");
                    valor = pedir("Valor: ");
                }

                String orden = pedir("Campo para ordenar (Enter = ninguno): ")
                        .toLowerCase(Locale.ROOT);
                boolean ascendente = true;
                if (!orden.isEmpty()) {
                    String sentido = pedir("Orden (asc/desc): ");
                    if (!sentido.equalsIgnoreCase("asc")
                            && !sentido.equalsIgnoreCase("desc")) {
                        throw new IllegalArgumentException("Orden incorrecto.");
                    }
                    ascendente = sentido.equalsIgnoreCase("asc");
                }

                int limite = Integer.parseInt(pedir("Limite (0 = todos): "));
                gestora.consultar(campos, filtro, operador, valor,
                        orden, ascendente, limite);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static class Producto {
        int id, stock;
        String nombre;
        double precio;

        Producto(int id, String nombre, double precio, int stock) {
            this.id = id;
            this.nombre = nombre;
            this.precio = precio;
            this.stock = stock;
        }

        Object campo(String campo) {
            switch (campo) {
                case "id": return id;
                case "nombre": return nombre;
                case "precio": return precio;
                case "stock": return stock;
                default: throw new IllegalArgumentException("Campo incorrecto.");
            }
        }
    }

    static class Gestora {
        Producto[] productos;

        void cargar(Connection cn) throws SQLException {
            ArrayList<Producto> lista = new ArrayList<>();
            try (Statement st = cn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM producto")) {
                while (rs.next()) {
                    lista.add(new Producto(rs.getInt("id"), rs.getString("nombre"),
                            rs.getDouble("precio"), rs.getInt("stock")));
                }
            }
            productos = lista.toArray(new Producto[0]);
        }

        void validar(String campo) {
            if (!Arrays.asList("id", "nombre", "precio", "stock").contains(campo))
                throw new IllegalArgumentException("Campo incorrecto: " + campo);
        }

        int comparar(Object a, Object b) {
            if (a instanceof Number)
                return Double.compare(((Number) a).doubleValue(),
                        ((Number) b).doubleValue());
            return a.toString().compareToIgnoreCase(b.toString());
        }

        boolean cumple(int comparacion, String operador) {
            switch (operador) {
                case "=": return comparacion == 0;
                case "!=": return comparacion != 0;
                case ">": return comparacion > 0;
                case "<": return comparacion < 0;
                case ">=": return comparacion >= 0;
                case "<=": return comparacion <= 0;
                default: throw new IllegalArgumentException("Operador incorrecto.");
            }
        }

        void consultar(String[] campos, String filtro, String operador,
                       String valor, String orden, boolean ascendente, int limite) {
            if (limite < 0) throw new IllegalArgumentException("Limite negativo.");
            for (int i = 0; i < campos.length; i++) {
                campos[i] = campos[i].trim();
                validar(campos[i]);
            }
            Object referencia = valor;
            if (!filtro.isEmpty()) {
                validar(filtro);
                cumple(0, operador);
                if (!filtro.equals("nombre")) {
                    double numero = Double.parseDouble(valor.replace(',', '.'));
                    if (!Double.isFinite(numero))
                        throw new IllegalArgumentException("Numero incorrecto.");
                    referencia = numero;
                }
            }
            Producto[] resultado = new Producto[productos.length];
            int cantidad = 0;
            for (Producto p : productos) {
                if (filtro.isEmpty() || cumple(comparar(p.campo(filtro), referencia), operador))
                    resultado[cantidad++] = p;
            }
            resultado = Arrays.copyOf(resultado, cantidad);
            if (!orden.isEmpty()) {
                validar(orden);
                Comparator<Producto> c = (a, b) -> comparar(a.campo(orden), b.campo(orden));
                Arrays.sort(resultado, ascendente ? c : c.reversed());
            }
            if (limite > 0) cantidad = Math.min(cantidad, limite);
            System.out.println("\nRESULTADOS");
            for (String campo : campos) System.out.printf("%-20s", campo);
            System.out.println();
            for (int i = 0; i < cantidad; i++) {
                for (String campo : campos)
                    System.out.printf("%-20s", resultado[i].campo(campo));
                System.out.println();
            }
            System.out.println("Registros mostrados: " + cantidad);
        }
    }
}