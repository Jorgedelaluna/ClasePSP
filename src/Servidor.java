import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// Clase Servidor
public class Servidor {
    public static void main(String[] args) {
        Libreria libreria = new Libreria();

        // Inicio del servidor
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Servidor Iniciado en el Puerto 8080...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente Conectado.");
                new ClientHandler(clientSocket, libreria).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el Servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

// Clase Libro
class Libro {
    private String isbn;
    private String titulo;
    private String autor;
    private double precio;

    // Constructor de Libro
    public Libro(String isbn, String titulo, String autor, double precio) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.precio = precio;
    }

    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public double getPrecio() { return precio; }

    @Override
    public String toString() {
        return "ISBN: " + isbn + ", Titulo: " + titulo + ", Autor: " + autor + ", Precio: " + precio + "€";
    }
}
// Clase Librería
class Libreria {
    private final List<Libro> libros = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Libreria() {
        // Inicializar con 5 libros predefinidos
        libros.add(new Libro("1111", "Juego de Tronos", "George R.R.Martin", 14.20));
        libros.add(new Libro("2222", "Danza de dragones", "George R.R.Martin", 14.20));
        libros.add(new Libro("3333", "La Espada del Destino", "Andrzej Sapkowski", 16.99));
        libros.add(new Libro("4444", "El Codigo Da Vinci", "Dan Brown", 17.00));
        libros.add(new Libro("5555", "Trilogía El Señor de Los Anillos", "J.R.R.Tolkien", 19.99));
    }

    public synchronized Libro findByIsbn(String isbn) {
        return libros.stream().filter(libro -> libro.getIsbn().equals(isbn)).findFirst().orElse(null);
    }

    public synchronized Libro findByTitle(String titulo) {
        return libros.stream().filter(libro -> libro.getTitulo().toLowerCase().contains(titulo.toLowerCase())).findFirst().orElse(null);
    }

    public synchronized List<Libro> findByAuthor(String autor) {
        return libros.stream().filter(libro -> libro.getAutor().toLowerCase().contains(autor.toLowerCase())).toList();
    }

    public void addLibro(Libro libro) {
        lock.lock(); // Garantiza que solo un hilo pueda añadir un libro a la vez
        try {
            libros.add(libro);
        } finally {
            lock.unlock(); // Libera el bloqueo después de agregar
        }
    }
}

class ClientHandler extends Thread {
    private final Socket clientSocket;
    private final Libreria libreria;

    public ClientHandler(Socket socket, Libreria libreria) {
        this.clientSocket = socket;
        this.libreria = libreria;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            String clientRequest;
            while ((clientRequest = (String) in.readObject()) != null) {
                String[] parts = clientRequest.split(";", 2);
                String action = parts[0];

                switch (action) {
                    case "query_by_isbn" -> {
                        Libro libro = libreria.findByIsbn(parts[1]);
                        out.writeObject(libro != null ? libro.toString() : "No se encontró el libro con ese ISBN.");
                    }
                    case "query_by_title" -> {
                        Libro libro = libreria.findByTitle(parts[1]);
                        out.writeObject(libro != null ? libro.toString() : "No se encontró el libro con ese Título.");
                    }
                    case "query_by_author" -> {
                        List<Libro> libros = libreria.findByAuthor(parts[1]);
                        out.writeObject(libros.isEmpty() ? "No se encontraron libros de ese Autor." : libros.toString());
                    }
                    case "add_book" -> {
                        String[] bookData = parts[1].split(";");
                        Libro libro = new Libro(bookData[0], bookData[1], bookData[2], Double.parseDouble(bookData[3]));
                        libreria.addLibro(libro);
                        out.writeObject("Libro Añadido Exitosamente.");
                    }
                    default -> out.writeObject("Comando no Reconocido.");
                }
                out.flush();
            }
        } catch (EOFException e) {
            System.out.println("El cliente se Desconectó.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en el Cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error al Cerrar el Socket del Cliente: " + e.getMessage());
            }
        }
    }
}