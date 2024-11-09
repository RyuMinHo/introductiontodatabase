import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AddEmployee extends JFrame {
    private JTextField fnameField, minitField, lnameField, ssnField, bdateField, addressField, sexField, salaryField, superSsnField;
    private JComboBox<String> departmentComboBox;
    private JButton saveButton, cancelButton;
    private Main mainFrame;

    public AddEmployee(Main mainFrame) {
        this.mainFrame = mainFrame;
        setTitle("직원 추가");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(12, 2));

        fnameField = new JTextField();
        minitField = new JTextField();
        lnameField = new JTextField();
        ssnField = new JTextField();
        bdateField = new JTextField();
        addressField = new JTextField();
        sexField = new JTextField();
        salaryField = new JTextField();
        superSsnField = new JTextField();
        departmentComboBox = new JComboBox<>();
        loadDepartmentNames();

        saveButton = new JButton("저장");
        cancelButton = new JButton("취소");

        add(new JLabel("First Name:"));
        add(fnameField);
        add(new JLabel("Middle Initial:"));
        add(minitField);
        add(new JLabel("Last Name:"));
        add(lnameField);
        add(new JLabel("SSN:"));
        add(ssnField);
        add(new JLabel("Birth Date:"));
        add(bdateField);
        add(new JLabel("Address:"));
        add(addressField);
        add(new JLabel("Sex:"));
        add(sexField);
        add(new JLabel("Salary:"));
        add(salaryField);
        add(new JLabel("Supervisor SSN:"));
        add(superSsnField);
        add(new JLabel("Department:"));
        add(departmentComboBox);
        add(saveButton);
        add(cancelButton);

        saveButton.addActionListener(e -> saveEmployeeData());
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadDepartmentNames() {
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Dname FROM DEPARTMENT")) {
            while (rs.next()) {
                departmentComboBox.addItem(rs.getString("Dname"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서 이름을 가져오는 중 오류가 발생했습니다.\n 내용: " + e.getMessage());
        }
    }

    private void saveEmployeeData() {
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            String query = "INSERT INTO EMPLOYEE (Fname, Minit, Lname, Ssn, Bdate, Address, Sex, Salary, Super_ssn, Dno, modified) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT Dnumber FROM DEPARTMENT WHERE Dname = ?), CURRENT_TIMESTAMP)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, fnameField.getText());
                pstmt.setString(2, minitField.getText());
                pstmt.setString(3, lnameField.getText());
                pstmt.setString(4, ssnField.getText());
                pstmt.setString(5, bdateField.getText());
                pstmt.setString(6, addressField.getText());
                pstmt.setString(7, sexField.getText());
                pstmt.setDouble(8, Double.parseDouble(salaryField.getText()));
                pstmt.setString(9, superSsnField.getText());
                pstmt.setString(10, (String) departmentComboBox.getSelectedItem());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "직원 추가에 성공했습니다.");
                mainFrame.refreshTable(); // Refresh the table after adding a new employee
                dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "직원 추가 중 오류가 발생했습니다.\n 내용: " + e.getMessage());
        }
    }
}