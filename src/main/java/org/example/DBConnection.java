package org.example;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static volatile Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/persons";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static Connection getConnection() {
        if (connection == null) {
            synchronized (DBConnection.class) {
                if (connection == null) {
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        connection = DriverManager.getConnection(URL, USER, PASSWORD);
                        System.out.println("Database connected successfully");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return connection;
    }
    public static void closeConnection() {
        if (connection != null) {
            synchronized (DBConnection.class) {
                try {
                    connection.close();
                    System.out.println("Connection closed successfully");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                connection = null;
            }
        }
    }

    public static void createTable() {
        getConnection();
        String query = "CREATE TABLE IF NOT EXISTS person " + "(id INT PRIMARY KEY AUTO_INCREMENT, first_name VARCHAR(30), last_name VARCHAR(30), age INT)";
        try (Statement st = connection.createStatement()) {
            st.execute(query);
            System.out.println("Table created successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertPerson(Person person) {
        getConnection();
        String query = "INSERT INTO person(first_name, last_name, age) VALUES (?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, person.getFirstName());
            st.setString(2, person.getLastName());
            st.setInt(3, person.getAge());

            System.out.println("Rows affected: " + st.executeUpdate());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Person> getAllPerson() {
        getConnection();
        List<Person> ans = new ArrayList<>();

        String query = "SELECT * FROM person";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                ans.add(new Person(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age")
                ));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return ans;
    }

    public static Person getPersonById(int id) {
        getConnection();
        String query = "SELECT * FROM person WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                return new Person(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deletePersonById(int id) {
        getConnection();
        String query = "DELETE FROM person WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setInt(1, id);

            int rows = st.executeUpdate();
            if (rows > 0) {
                System.out.println("Person deleted successfully");
            } else {
                System.out.println("No person found with this ID");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static class Person {
        private String firstName;
        private String lastName;
        private int age;

        public Person(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public int getAge() {
            return age;
        }
    }
}