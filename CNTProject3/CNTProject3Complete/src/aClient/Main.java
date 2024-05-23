/*
Name: Christopher Deluigi
Course: CNT 4714 Spring 2024
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: March 10, 2024
Class: Enterprise Computing
*/
package aClient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Main extends JFrame {

    private JComboBox<String> propertiesFilesList;
    private JComboBox<String> userPropertiesFilesList;
    private JTextField jtfUsername;
    private JPasswordField jpfPassword;
    private JComboBox<String> dataBaseURLList;

    private JTextArea jtaSqlCommand;

    private JButton jbtConnectToDB;
    private JButton jbtDisconnectFromDB;
    private JButton jbtClearSQLCommand;
    private JButton jbtExecuteSQLCommand;
    private JButton jbtClearResultWindow;

    private ResultSetTableModel tableModel = null;
    private JTable table;

    private Connection connection;
    private boolean connectedToDatabase = false;

    private JLabel jlbConnectionStatus;

    public Main() throws ClassNotFoundException, SQLException, IOException {
        GUIComponents();
        EventListeners();
        GUISetup();
    }

    private void GUIComponents() throws ClassNotFoundException, SQLException, IOException {
        String[] propertiesFiles = {"bikedb.properties", "project3.properties", "operationslog.properties"};
        propertiesFilesList = new JComboBox<>(propertiesFiles);

        String[] userPropertiesFiles = {"client1.properties", "client2.properties", "root.properties"};
        userPropertiesFilesList = new JComboBox<>(userPropertiesFiles);

        String[] dataBaseURLString = {"jdbc:mysql://localhost:3312/project3", ""};
        dataBaseURLList = new JComboBox<>(dataBaseURLString);

        jtfUsername = new JTextField(20);
        jtfUsername.setBackground(Color.WHITE); // Background color for the text field

        jpfPassword = new JPasswordField(20);
        jpfPassword.setBackground(Color.WHITE); // Background color for the password field

        jtaSqlCommand = new JTextArea(5, 75);
        jtaSqlCommand.setWrapStyleWord(true);
        jtaSqlCommand.setLineWrap(true);
        jtaSqlCommand.setBackground(Color.LIGHT_GRAY); // Background color for the text area

        jbtConnectToDB = new JButton("Connect to Database");
        jbtConnectToDB.setBackground(Color.GREEN); // Background color for the connect button

        jbtDisconnectFromDB = new JButton("Disconnect From Database");
        jbtDisconnectFromDB.setBackground(Color.RED); // Background color for the disconnect button

        jbtClearSQLCommand = new JButton("Clear SQL Command");
        jbtClearSQLCommand.setBackground(Color.ORANGE); // Background color for the clear SQL command button

        jbtExecuteSQLCommand = new JButton("Execute SQL Command");
        jbtExecuteSQLCommand.setBackground(Color.YELLOW); // Background color for the execute SQL command button

        jbtClearResultWindow = new JButton("Clear Result Window");
        jbtClearResultWindow.setBackground(Color.CYAN); // Background color for the clear result window button

        jlbConnectionStatus = new JLabel("No Connection Established");
        jlbConnectionStatus.setForeground(Color.RED); // Foreground color for the connection status label

        table = new JTable();
    }



    private void EventListeners() {
        jbtConnectToDB.addActionListener(e -> ConnectToDB());
        jbtDisconnectFromDB.addActionListener(e -> DisconnectFromDB());
        jbtClearSQLCommand.addActionListener(e -> jtaSqlCommand.setText(""));
        jbtExecuteSQLCommand.addActionListener(e -> SQLCommand());
        jbtClearResultWindow.addActionListener(e -> ResultWindows());

        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent event) {
                try {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });
    }

    private void GUISetup() {
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("DB URL Properties"), gbc);

        gbc.gridx = 1;
        topPanel.add(propertiesFilesList, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(new JLabel("User Properties"), gbc);

        gbc.gridx = 1;
        topPanel.add(userPropertiesFilesList, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(new JLabel("Username"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2; // Span 2 columns for the text field
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
        topPanel.add(jtfUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1; // Reset gridwidth
        topPanel.add(new JLabel("Password"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2; // Span 2 columns for the password field
        topPanel.add(jpfPassword, gbc);

        JPanel sqlPanel = new JPanel(new BorderLayout());
        sqlPanel.add(new JLabel("Enter An SQL Command"), BorderLayout.NORTH);
        sqlPanel.add(new JScrollPane(jtaSqlCommand), BorderLayout.CENTER);

        JPanel sqlButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sqlButtonPanel.add(jbtClearSQLCommand);
        sqlButtonPanel.add(jbtExecuteSQLCommand);
        sqlPanel.add(sqlButtonPanel, BorderLayout.SOUTH);

        JPanel connectionPanel = new JPanel(new BorderLayout());
        connectionPanel.add(jbtConnectToDB, BorderLayout.WEST);
        connectionPanel.add(jbtDisconnectFromDB, BorderLayout.EAST);
        connectionPanel.add(new JLabel("Connection Status:"), BorderLayout.CENTER);
        connectionPanel.add(jlbConnectionStatus, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        bottomPanel.add(jbtClearResultWindow, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(sqlPanel, BorderLayout.CENTER);
        mainPanel.add(connectionPanel, BorderLayout.SOUTH);

        add(mainPanel);

        setTitle("SQL Client Application  - (CD - CNT 4714 - Spring 2024 - Project 3)");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }


    private void ConnectToDB() {
        try {
          
            Properties properties = new Properties();
            String selectedPropertiesFile = String.valueOf(propertiesFilesList.getSelectedItem());
            properties.load(new FileInputStream(selectedPropertiesFile));

            Properties userProperties = new Properties();
            String selectedUserPropertiesFile = String.valueOf(userPropertiesFilesList.getSelectedItem());
            userProperties.load(new FileInputStream(selectedUserPropertiesFile));

            Class.forName(properties.getProperty("MYSQL_DB_DRIVER_CLASS"));
            String dbUrl = properties.getProperty("MYSQL_DB_URL");

            if (connectedToDatabase) {
                connection.close();
                ConnectionStatusUp("No Connection Now", Color.RED);
                connectedToDatabase = false;
                emptyTable();
            }

            String enteredUsername = jtfUsername.getText().trim();
            String enteredPassword = new String(jpfPassword.getPassword());

            String correctUsername = userProperties.getProperty("MYSQL_DB_USERNAME");
            String correctPassword = userProperties.getProperty("MYSQL_DB_PASSWORD");

            if (enteredUsername.equals(correctUsername) && enteredPassword.equals(correctPassword)) {
                connection = DriverManager.getConnection(dbUrl, userProperties.getProperty("MYSQL_DB_USERNAME"), userProperties.getProperty("MYSQL_DB_PASSWORD"));
                ConnectionStatusUp("Connected to " + dbUrl, Color.GREEN);
                connectedToDatabase = true;
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect username or password", "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            ConnectionError(e);
        }
    }

    private void DisconnectFromDB() {
        try {
            if (connectedToDatabase) {
                connection.close();
                ConnectionStatusUp("No Connection Now", Color.RED);
                connectedToDatabase = false;
                emptyTable();
            }
        } catch (SQLException e) {
            ConnectionError(e);
        }
    }

    private void SQLCommand() {
        if (connectedToDatabase && tableModel == null) {
            try {
                tableModel = new ResultSetTableModel(connection, jtaSqlCommand.getText());
                table.setModel(tableModel);
            } catch (ClassNotFoundException | SQLException e) {
                SQLError(e);
            }
        } else if (connectedToDatabase && tableModel != null) {
            String query = jtaSqlCommand.getText();
            if (query.toUpperCase().contains("select")) {
                try {
                    tableModel.setQuery(query);
                } catch (IllegalStateException | SQLException e) {
                    SQLError(e);
                }
            } else {
                try {
                    tableModel.setUpdate(query);
                    JOptionPane.showMessageDialog(this, "Update successful", "Success", JOptionPane.INFORMATION_MESSAGE);
                    emptyTable();
                } catch (IllegalStateException | SQLException e) {
                    SQLError(e);
                }
            }
        }
    }

    private void ResultWindows() {
        emptyTable();
    }

    private void emptyTable() {
        table.setModel(new DefaultTableModel());
        tableModel = null;
    }

    private void ConnectionStatusUp(String status, Color color) {
        jlbConnectionStatus.setText(status);
        jlbConnectionStatus.setForeground(color);
    }

    private void ConnectionError(Exception e) {
        ConnectionStatusUp("No Connection Now", Color.RED);
        emptyTable();
        e.printStackTrace();
    }

    private void SQLError(Exception e) {
        ConnectionStatusUp("No Connection Now", Color.RED);
        emptyTable();
        JOptionPane.showMessageDialog(null, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Main().setVisible(true);
            } catch (ClassNotFoundException | SQLException | IOException e) {
                e.printStackTrace();
            }
        });
    }
}
