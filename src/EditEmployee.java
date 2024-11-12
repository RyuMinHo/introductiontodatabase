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
        // SSN 검증 (EditEmployee.java)
        String ssn = ssnField.getText();
        if (!ssn.matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "SSN은 9자리 숫자여야 합니다.");
            return;
        }
        if (!ssn.equals(originalSsn)) {
            try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD);
                 PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM EMPLOYEE WHERE Ssn = ?")) {
                pstmt.setString(1, ssn);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "이미 DB에 존재하는 SSN입니다.");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "SSN 확인 중 오류가 발생했습니다: " + e.getMessage());
                return;
            }
        }

        // 성별 검증
        String gender = sexField.getText().toUpperCase();
        if (!gender.equals("M") && !gender.equals("F")) {
            JOptionPane.showMessageDialog(this, "성별은 M 또는 F만 가능합니다.");
            return;
        }

        // Salary 검증
        String salaryStr = salaryField.getText();
        try {
            Double.parseDouble(salaryStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "급여는 숫자여야 합니다.");
            return;
        }

        // Supervisor SSN 검증
        String superSsn = superSsnField.getText();
        if (!superSsn.isEmpty()) {
            try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD);
                 PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM EMPLOYEE WHERE Ssn = ?")) {
                pstmt.setString(1, superSsn);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    JOptionPane.showMessageDialog(this, "존재하지 않는 슈퍼바이저 SSN입니다.");
                    return;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "슈퍼바이저 SSN 확인 중 오류가 발생했습니다: " + e.getMessage());
                return;
            }
        }

        // 주소 형식 검증
        String address = addressField.getText();
        if (!address.matches("^[^,]+,\\s*[A-Za-z ]+$")) {
            JOptionPane.showMessageDialog(this, "주소 형식은 'city, state' 여야 합니다.");
            return;
        }

        // 생일 형식 검증
        String bdate = bdateField.getText();
        if (!bdate.matches("(\\d{4}-\\d{2}-\\d{2}|\\d{8})")) {
            JOptionPane.showMessageDialog(this, "생일 형식은 'YYYY-MM-DD' 또는 'YYYYMMDD' 여야 합니다.");
            return;
        }

        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            String query = "UPDATE EMPLOYEE SET Fname = ?, Minit = ?, Lname = ?, Ssn = ?, Bdate = ?, Address = ?, Sex = ?, Salary = ?, Super_ssn = ?, Dno = (SELECT Dnumber FROM DEPARTMENT WHERE Dname = ?), modified = CURRENT_TIMESTAMP WHERE Ssn = ?";
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