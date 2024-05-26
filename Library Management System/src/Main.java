import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Book {
    private String title;
    private String author;
    private boolean isAvailable;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.isAvailable = true;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    public String toString() {
        return title + " by " + author;
    }
}

class User {
    private String name;
    private int id;
    private List<Book> borrowedBooks;
    private boolean isAdmin; // New field

    // Constructor including isAdmin parameter
    public User(String name, int id, boolean isAdmin) {
        this.name = name;
        this.id = id;
        this.isAdmin = isAdmin;
        this.borrowedBooks = new ArrayList<>();
    }
    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    public void borrowBook(Book book) {
        borrowedBooks.add(book);
    }

    public void returnBook(Book book) {
        borrowedBooks.remove(book);
    }
    public boolean isAdmin() {
        return isAdmin;
    }
}

class Library {
    private List<Book> books;
    private List<User> users;

    public Library() {
        this.books = new ArrayList<>();
        this.users = new ArrayList<>();
    }

    public void addUser(User user) {
        users.add(user);
        insertUserIntoDatabase(user);
    }
    private void insertUserIntoDatabase(User user) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO userss (id, name, isAdmin) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setInt(1, user.getId());
                preparedStatement.setString(2, user.getName());
                preparedStatement.setBoolean(3, user.isAdmin());

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("User added to the database successfully!");
                } else {
                    System.out.println("Failed to add user to the database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addBook(Book book) {
        books.add(book);
        insertBookIntoDatabase(book);
    }
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/liberarymanagementsystem";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private void insertBookIntoDatabase(Book book) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "INSERT INTO AvailableBooks (title, author, isAvailable) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, book.getTitle());
                preparedStatement.setString(2, book.getAuthor());
                preparedStatement.setBoolean(3, book.isAvailable());

                int affectedRows = preparedStatement.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Book added to the database successfully!");
                } else {
                    System.out.println("Failed to add book to the database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void displayAvailableBooks() {
        System.out.println("Available Books:");

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT title, author FROM AvailableBooks WHERE isAvailable = true";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                 ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    String author = resultSet.getString("author");
                    System.out.println(title + " by " + author);
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Book findBook(String title) {
        for (Book book : books) {
            if (book.getTitle().equalsIgnoreCase(title) ) {
                return book;
            }
        }
        return null;
    }

    public User findUser(int id) {
        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
    public void displayBorrowedBooks(User user) {
        System.out.println("Borrowed Books for " + user.getName() + ":");
        List<Book> borrowedBooks = user.getBorrowedBooks();
        if (borrowedBooks.isEmpty()) {
            System.out.println("No books borrowed.");
        } else {
            for (Book book : borrowedBooks) {
                System.out.println(book.getTitle() + " by " + book.getAuthor());
            }
        }
    }
}

class LibraryManagementSystem {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Library library = new Library();

        User user1 = new User("User", 1, true);

        library.addUser(user1);

        int userId;
        String userName;
        User currentUser = null;

        do {
            // Login system
            System.out.println("Welcome to the Library Management System!");

            boolean loginSuccessful;
            do {
                System.out.print("Enter your user ID: ");
                userId = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character
                System.out.print("Enter your name: ");
                userName = scanner.nextLine();

                currentUser = library.findUser(userId);

                loginSuccessful = currentUser != null && currentUser.getName().equalsIgnoreCase(userName);

                if (!loginSuccessful) {
                    System.out.println("Login failed. User not found or incorrect credentials. Please try again.");
                }
            } while (!loginSuccessful);

            System.out.println("Login successful! Welcome, " + currentUser.getName() + "!");

            int choice;

            do {
                System.out.println("\nLibrary Management System - User: " + currentUser.getName());
                System.out.println("1. Display Available Books");
                System.out.println("2. Borrow a Book");
                System.out.println("3. Return a Book");
                System.out.println("4. Log in as Another User");
                System.out.println("5. Display Borrowed Books By User");
                System.out.println("6. Add Book");
                System.out.println("7. Add User");
                System.out.println("8. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        library.displayAvailableBooks();
                        break;
                    case 2:
                        System.out.print("Enter your user ID: ");
                        userId = scanner.nextInt();
                        currentUser = library.findUser(userId);
                        if (currentUser != null) {
                            System.out.print("Enter the title of the book to borrow: ");
                            scanner.nextLine(); // Consume the newline character
                            String borrowTitle = scanner.nextLine();
                            Book borrowBook = library.findBook(borrowTitle);
                            if (borrowBook != null && borrowBook.isAvailable()) {
                                borrowBook.setAvailable(false);
                                currentUser.borrowBook(borrowBook);
                                System.out.println(currentUser.getName() + " has borrowed: " + borrowBook);
                            } else {
                                System.out.println("Book not available or not found.");
                            }
                        } else {
                            System.out.println("User not found.");
                        }
                        break;
                    case 3:
                        System.out.print("Enter the title of the book to return: ");
                        scanner.nextLine(); // Consume the newline character
                        String returnTitle = scanner.nextLine();
                        Book returnBook = library.findBook(returnTitle);
                        if (returnBook != null) {
                            returnBook.setAvailable(true);
                            currentUser.returnBook(returnBook);
                            System.out.println("You have returned: " + returnBook.getTitle());
                        } else {
                            System.out.println("Book not found.");
                        }
                        break;
                    case 4:
                        currentUser = null; // Log out the current user
                        do {
                            // Login as another user
                            System.out.println("Log in as another user:");
                            System.out.print("Enter user ID: ");
                            userId = scanner.nextInt();
                            scanner.nextLine(); // Consume the newline character
                            System.out.print("Enter user name: ");
                            userName = scanner.nextLine();

                            currentUser = library.findUser(userId);

                            if (currentUser != null && currentUser.getName().equalsIgnoreCase(userName)) {
                                System.out.println("Login successful! Welcome, " + currentUser.getName() + "!");
                            } else {
                                System.out.println("Login failed. User not found or incorrect credentials.");
                            }
                        } while (currentUser == null);
                        break;
                    case 5:
                        library.displayBorrowedBooks(currentUser);
                        break;
                    case 6:
                        if (currentUser.isAdmin()) {
                            // Only administrators can add books
                            System.out.print("Enter new book title: ");
                            scanner.nextLine(); // Consume the newline character
                            String newTitle = scanner.nextLine();
                            System.out.print("Enter new book author: ");
                            String newAuthor = scanner.nextLine();
                            Book newBook = new Book(newTitle, newAuthor);
                            library.addBook(newBook);
                            System.out.println("Book added successfully: " + newBook);
                        } else {
                            System.out.println("Permission denied. Only administrators can add books.");
                        }
                        break;
                    case 7:
                        if (currentUser.isAdmin()) {
                            // Only administrators can add users
                            System.out.print("Enter new user name: ");
                            scanner.nextLine();
                            String newUser = scanner.nextLine();
                            System.out.println("Enter user Id : ");
                            int id = scanner.nextInt();
                            User newUserObj = new User(newUser, id, false); // New user is not admin
                            library.addUser(newUserObj);
                            System.out.println("User added successfully: " + newUserObj.getName());
                        } else {
                            System.out.println("Permission denied. Only administrators can add users.");
                        }
                        break;
                    case 8:
                        System.out.println("Exiting Library Management System. Goodbye!");
                        currentUser = null;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a valid option.");
                }
            } while (choice != 8 && currentUser != null);

        } while (currentUser != null);

        scanner.close();
    }
}