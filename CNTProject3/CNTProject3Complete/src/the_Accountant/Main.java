/*
Name: Christopher Deluigi
Course: CNT 4714 Spring 2024
Assignment title: Project 3 – A Two-tier Client-Server Application
Date: March 10, 2024
Class: Enterprise Computing
*/
package the_Accountant;

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

    private JLabel propertiesFileLabel;
    private JLabel userPropertiesFileLabel;
    private JLabel dataBaseURLLabel;

    private JTextField jtfUsername;
    private JPasswordField jpfPassword;

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
        propertiesFileLabel = new JLabel("operationslog.properties");
        userPropertiesFileLabel = new JLabel("the_Accountant.properties");
        dataBaseURLLabel = new JLabel("jdbc:mysql://localhost:3312/project3");

        jtfUsername = new JTextField(20); // Increased size to 20 columns
        jpfPassword = new JPasswordField(20); // Increased size to 20 columns

        jtaSqlCommand = new JTextArea(3, 75);
        jtaSqlCommand.setWrapStyleWord(true);
        jtaSqlCommand.setLineWrap(true);

        jbtConnectToDB = new JButton("Connect to Database");
        jbtDisconnectFromDB = new JButton("Disconnect From Database");
        jbtClearSQLCommand = new JButton("Clear SQL Command");
        jbtExecuteSQLCommand = new JButton("Execute SQL Command");
        jbtClearResultWindow = new JButton("Clear Result Window");
        jlbConnectionStatus = new JLabel("No Connection Established");
        jlbConnectionStatus.setForeground(Color.RED);

        table = new JTable();

        // Adjusting colors and font
        Color buttonColor = new Color(102, 204, 255); // Light blue
        Font labelFont = new Font("Arial", Font.BOLD, 14);

        jbtConnectToDB.setBackground(buttonColor);
        jbtDisconnectFromDB.setBackground(buttonColor);
        jbtClearSQLCommand.setBackground(buttonColor);
        jbtExecuteSQLCommand.setBackground(buttonColor);
        jbtClearResultWindow.setBackground(buttonColor);

        // Setting font for labels
        propertiesFileLabel.setFont(labelFont);
        userPropertiesFileLabel.setFont(labelFont);
        dataBaseURLLabel.setFont(labelFont);

        // Changing orientation and background color of buttons
        jbtConnectToDB.setBackground(Color.GREEN);
        jbtDisconnectFromDB.setBackground(Color.RED);
        jbtClearSQLCommand.setBackground(Color.ORANGE);
        jbtExecuteSQLCommand.setBackground(Color.YELLOW);
        jbtClearResultWindow.setBackground(Color.CYAN);

        // Reducing the gap between username and password fields
        JPanel userPanel = new JPanel(new GridLayout(2, 2));
        userPanel.add(new JLabel("Username"));
        userPanel.add(jtfUsername);
        userPanel.add(new JLabel("Password"));
        userPanel.add(jpfPassword);
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Creating panel for SQL Command section
        JPanel sqlCommandPanel = new JPanel(new BorderLayout());
        sqlCommandPanel.add(new JLabel("Enter An SQL Command"), BorderLayout.NORTH);
        sqlCommandPanel.add(new JScrollPane(jtaSqlCommand), BorderLayout.CENTER);
        sqlCommandPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Creating a container panel for userPanel and sqlCommandPanel
        JPanel topRightPanel = new JPanel(new BorderLayout());
        topRightPanel.add(userPanel, BorderLayout.NORTH);
        topRightPanel.add(sqlCommandPanel, BorderLayout.CENTER);


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

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("DB URL Properties"), gbc);

        gbc.gridx = 1;
        topPanel.add(propertiesFileLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(new JLabel("User Properties"), gbc);

        gbc.gridx = 1;
        topPanel.add(userPropertiesFileLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(new JLabel("Username"), gbc);

        gbc.gridx = 1;
        topPanel.add(jtfUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        topPanel.add(new JLabel("Password"), gbc);

        gbc.gridx = 1;
        topPanel.add(jpfPassword, gbc);

        JPanel sqlPanel = new JPanel(new BorderLayout());
        sqlPanel.add(new JLabel("Enter An SQL Command"), BorderLayout.NORTH);
        sqlPanel.add(new JScrollPane(jtaSqlCommand), BorderLayout.CENTER);

        JPanel sqlButtonPanel = new JPanel(new FlowLayout());
        sqlButtonPanel.add(jbtClearSQLCommand);
        sqlButtonPanel.add(jbtExecuteSQLCommand);
        sqlPanel.add(sqlButtonPanel, BorderLayout.SOUTH);
        sqlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel connectionPanel = new JPanel(new GridLayout(3, 1));
        connectionPanel.add(jbtConnectToDB);
        connectionPanel.add(jbtDisconnectFromDB);
        connectionPanel.add(jlbConnectionStatus);
        connectionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel resultButtonPanel = new JPanel(new FlowLayout());
        resultButtonPanel.add(jbtClearResultWindow);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(resultButtonPanel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        JLabel resultLabel = new JLabel("SQL Execution Result Window", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        bottomPanel.add(resultLabel, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(sqlPanel, BorderLayout.CENTER);
        mainPanel.add(connectionPanel, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        setTitle("SPECIALIZED ACCOUNTANT APPLICATION  - (CD - CNT 4714 - Spring 2024 - Project 3)");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }


    private void ConnectToDB() {
        try {
            Properties properties = new Properties();
            String selectedPropertiesFile = propertiesFileLabel.getText();
            properties.load(new FileInputStream(selectedPropertiesFile));

            Properties userProperties = new Properties();
            String selectedUserPropertiesFile = userPropertiesFileLabel.getText();
            userProperties.load(new FileInputStream(selectedUserPropertiesFile));

            Class.forName(properties.getProperty("MYSQL_DB_DRIVER_CLASS"));
            String dbUrl = properties.getProperty("MYSQL_DB_URL");

            if (connectedToDatabase) {
                connection.close();
                ConnectionStatusUp("No Connection", Color.RED);
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
                JOptionPane.showMessageDialog(this, "Incorrect username or password found", "Login Error occured", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | SQLException | ClassNotFoundException e) {
            ConnectionError(e);
        }
    }

    private void DisconnectFromDB() {
        try {
            if (connectedToDatabase) {
                connection.close();
                ConnectionStatusUp("No Connection", Color.RED);
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
            if (query.toLowerCase().contains("select")) {
                try {
                    tableModel.setQuery(query);
                } catch (IllegalStateException | SQLException e) {
                    SQLError(e);
                }
            } else {
                try {
                    tableModel.setUpdate(query);
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
        ConnectionStatusUp("No Connection", Color.RED);
        emptyTable();
        e.printStackTrace();
    }

    private void SQLError(Exception e) {
        ConnectionStatusUp("No Connection", Color.RED);
        emptyTable();
        JOptionPane.showMessageDialog(null, e.getMessage(), "Database error occured", JOptionPane.ERROR_MESSAGE);
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
