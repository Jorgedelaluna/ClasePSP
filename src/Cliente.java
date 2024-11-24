import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

// Clase Cliente
public class Cliente {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             Scanner scanner = new Scanner(System.in)) {

            // Establecer conexión al Servidor
            System.out.println("Conectado al Servidor.");

            // Menú de la aplicación
            while (true) {
                System.out.println("\n--- Menú ---");
                System.out.println("1. Consultar Libro por ISBN");
                System.out.println("2. Consultar Libro por Título");
                System.out.println("3. Consultar Libros por Autor");
                System.out.println("4. Añadir Libro");
                System.out.println("5. Salir");
                System.out.print("Elija una Opción: ");

                // Leer la opción del usuario
                int option;
                if (scanner.hasNextInt()) {
                    option = scanner.nextInt();
                    scanner.nextLine(); // Consumir el salto de línea
                } else {
                    System.out.println("Opción no válida. Inténtelo de nuevo.");
                    scanner.nextLine(); // Limpiar el búfer de entrada
                    continue; // Volver al menú sin proceder con el código siguiente
                }

                // Desarrollo de opciones de la aplicación
                switch (option) {
                    case 1 -> {
                        System.out.print("Ingrese el ISBN del Libro: ");
                        String isbn = scanner.nextLine();
                        out.writeObject("query_by_isbn;" + isbn);
                    }
                    case 2 -> {
                        System.out.print("Ingrese el Título del Libro: ");
                        String titulo = scanner.nextLine();
                        out.writeObject("query_by_title;" + titulo);
                    }
                    case 3 -> {
                        System.out.print("Ingrese el Autor: ");
                        String autor = scanner.nextLine();
                        out.writeObject("query_by_author;" + autor);
                    }
                    case 4 -> {
                        System.out.print("Ingrese el ISBN del Libro: ");
                        String isbn = scanner.nextLine();
                        System.out.print("Ingrese el Título del Libro: ");
                        String titulo = scanner.nextLine();
                        System.out.print("Ingrese el Autor del Libro: ");
                        String autor = scanner.nextLine();

                        double precio = 0.0;
                        boolean validPrecio = false;

                        // Manejo de excepciones para capturar un precio válido
                        while (!validPrecio) {
                            try {
                                System.out.print("Ingrese el Precio del Libro: ");
                                precio = scanner.nextDouble();
                                scanner.nextLine(); // Consumir el salto de línea
                                validPrecio = true; // Salir del bucle si el precio es válido
                            } catch (InputMismatchException e) {
                                System.out.println("Entrada Inválida. Por favor, Introduzca un número Válido.");
                                scanner.nextLine(); // Limpiar el búfer del Scanner
                            }
                        }

                        String bookData = isbn + ";" + titulo + ";" + autor + ";" + precio;
                        out.writeObject("add_book;" + bookData);
                    }
                    case 5 -> {
                        System.out.println("Saliendo de la Aplicación.");
                        return;
                    }
                    default -> {
                        System.out.println("Opción no válida. Inténtelo de nuevo.");
                        continue; // Vuelve al menú sin realizar ninguna acción
                    }
                }

                out.flush();
                String response = (String) in.readObject(); // Recibe la respuesta del servidor
                System.out.println("Respuesta del Servidor: " + response);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
