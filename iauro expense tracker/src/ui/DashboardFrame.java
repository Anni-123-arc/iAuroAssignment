package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DashboardFrame extends JFrame {
    private JTable expenseTable;
    private DefaultTableModel tableModel;
    private JTextField amountField, categoryField, descriptionField;
    private JButton addButton, editButton, deleteButton;
    private String username;

    public DashboardFrame(String username) {
        this.username = username;
        setTitle("Expense Tracker - Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Amount", "Category", "Description", "Date"}, 0);
        expenseTable = new JTable(tableModel);
        loadExpenses();  // Load expenses on startup

        // Form panel setup
        JPanel formPanel = new JPanel(new GridLayout(2, 4));
        amountField = new JTextField();
        categoryField = new JTextField();
        descriptionField = new JTextField();
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");

        // Add components to form panel
        formPanel.add(new JLabel("Amount:"));
        formPanel.add(amountField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descriptionField);
        formPanel.add(addButton);
        formPanel.add(editButton);
        formPanel.add(deleteButton);

        add(new JScrollPane(expenseTable), BorderLayout.CENTER);
        add(formPanel, BorderLayout.SOUTH);

        // Action listeners for buttons
        addButton.addActionListener(e -> addExpense());
        editButton.addActionListener(e -> editExpense());
        deleteButton.addActionListener(e -> deleteExpense());

        setVisible(true);
    }

    private void loadExpenses() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/expense_tracker", "root", "anni@9890")) {
            // SQL query to fetch expenses for the current user
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM expenses WHERE user_id = (SELECT id FROM users WHERE username=?)");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            // Populate the table with fetched data
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getDate("date")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void addExpense() {
        String amountText = amountField.getText();
        String category = categoryField.getText();
        String description = descriptionField.getText();

        // Validate inputs
        if (amountText.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount and Category cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/expense_tracker", "root", "anni@9890");
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO expenses (user_id, amount, category, description, date) VALUES ((SELECT id FROM users WHERE username=?), ?, ?, ?, CURDATE())");

            stmt.setString(1, username);
            stmt.setDouble(2, amount);
            stmt.setString(3, category);
            stmt.setString(4, description);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                JOptionPane.showMessageDialog(this, "Expense added successfully!");
                loadExpenses();  // Refresh table after adding
            }

            stmt.close();
            conn.close();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void editExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String amountText = amountField.getText();
        String category = categoryField.getText();
        String description = descriptionField.getText();

        // Validate inputs
        if (amountText.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Amount and Category cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int expenseId = (int) tableModel.getValueAt(selectedRow, 0);  // Get the selected expense ID

        try {
            double amount = Double.parseDouble(amountText);
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/expense_tracker", "root", "anni@9890");
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE expenses SET amount=?, category=?, description=? WHERE id=?");

            stmt.setDouble(1, amount);
            stmt.setString(2, category);
            stmt.setString(3, description);
            stmt.setInt(4, expenseId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Expense updated successfully!");
                loadExpenses();  // Refresh table after updating
            }

            stmt.close();
            conn.close();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount entered!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void deleteExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an expense to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int expenseId = (int) tableModel.getValueAt(selectedRow, 0);  // Get the selected expense ID

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this expense?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/expense_tracker", "root", "anni@9890");
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM expenses WHERE id=?");
            stmt.setInt(1, expenseId);

            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Expense deleted successfully!");
                loadExpenses();  // Refresh table after deletion
            }

            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

}
