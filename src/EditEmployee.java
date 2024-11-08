import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class EditEmployee extends JFrame {
    private DefaultTableModel model;
    private int selectedRow;
    private JTextField fnameField, minitField, lnameField, ssnField, bdateField, addressField, sexField, salaryField, superSsnField;
    private JComboBox<String> departmentComboBox;
    private String originalSsn;
    private Main mainFrame;

    public EditEmployee(DefaultTableModel model, int selectedRow, Main mainFrame) {
        this.model = model;
        this.selectedRow = selectedRow;
        this.mainFrame = mainFrame;

        String fname = (String) model.getValueAt(selectedRow, 1);
        String minit = (String) model.getValueAt(selectedRow, 2);
        String lname = (String) model.getValueAt(selectedRow, 3);
        originalSsn = (String) model.getValueAt(selectedRow, 4);
        Date bdate = (Date) model.getValueAt(selectedRow, 5);
        String address = (String) model.getValueAt(selectedRow, 6);
        String sex = (String) model.getValueAt(selectedRow, 7);
        double salary = Double.parseDouble(model.getValueAt(selectedRow, 8).toString());
        String superSsn = (String) model.getValueAt(selectedRow, 9);
        String department = (String) model.getValueAt(selectedRow, 10);

        // Convert java.sql.Date to String
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String bdateString = dateFormat.format(bdate);

        fnameField = new JTextField(fname);
        minitField = new JTextField(minit);
        lnameField = new JTextField(lname);
        ssnField = new JTextField(originalSsn);
        bdateField = new JTextField(bdateString);
        addressField = new JTextField(address);
        sexField = new JTextField(sex);
        salaryField = new JTextField(String.valueOf(salary));
        superSsnField = new JTextField(superSsn);
        departmentComboBox = new JComboBox<>();
        loadDepartmentNames();
        departmentComboBox.setSelectedItem(department);

        JButton saveButton = new JButton("저장");
        JButton cancelButton = new JButton("취소");

        setLayout(new GridLayout(12, 2));
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

        pack();
        setLocationRelativeTo(null);
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
            String query = "UPDATE EMPLOYEE SET Fname = ?, Minit = ?, Lname = ?, Ssn = ?, Bdate = ?, Address = ?, Sex = ?, Salary = ?, Super_ssn = ?, Dno = (SELECT Dnumber FROM DEPARTMENT WHERE Dname = ?) WHERE Ssn = ?";
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
                pstmt.setString(11, originalSsn);

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "직원 정보 업데이트에 성공했습니다.");

                // Update the table model, ignoring the checkbox column
                model.setValueAt(fnameField.getText(), selectedRow, 1);
                model.setValueAt(minitField.getText(), selectedRow, 2);
                model.setValueAt(lnameField.getText(), selectedRow, 3);
                model.setValueAt(ssnField.getText(), selectedRow, 4);
                model.setValueAt(bdateField.getText(), selectedRow, 5);
                model.setValueAt(addressField.getText(), selectedRow, 6);
                model.setValueAt(sexField.getText(), selectedRow, 7);
                model.setValueAt(salaryField.getText(), selectedRow, 8);
                model.setValueAt(superSsnField.getText(), selectedRow, 9);
                model.setValueAt(departmentComboBox.getSelectedItem(), selectedRow, 10);

                // Refresh the table in the main frame
                mainFrame.refreshTable();

                dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "직원 정보 업데이트 중 오류가 발생했습니다.\n 내용: " + e.getMessage());
        }
    }
}