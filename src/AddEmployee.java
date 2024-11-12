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
        // SSN 검증
        String ssn = ssnField.getText();
        if (!ssn.matches("\\d{9}")) {
            JOptionPane.showMessageDialog(this, "SSN은 9자리 숫자여야 합니다.");
            return;
        }
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

        // 성별 검증
        String gender = sexField.getText().toUpperCase();
        if (!gender.equals("M") && !gender.equals("F")) {
            JOptionPane.showMessageDialog(this, "성별은 M 또는 F만 가능합니다.");
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

        // Salary 검증
        String salaryStr = salaryField.getText();
        try {
            Double.parseDouble(salaryStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "급여는 숫자여야 합니다.");
            return;
        }

        // 생일 형식 검증
        String bdate = bdateField.getText();
        if (!bdate.matches("(\\d{4}-\\d{2}-\\d{2}|\\d{8})")) {
            JOptionPane.showMessageDialog(this, "생일 형식은 'YYYY-MM-DD' 또는 'YYYYMMDD' 여야 합니다.");
            return;
        }

        // 모든 검증을 통과한 경우, 데이터베이스에 저장하는 로직
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            String query = "INSERT INTO EMPLOYEE (Fname, Minit, Lname, Ssn, Bdate, Address, Sex, Salary, Super_ssn, Dno, modified) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT Dnumber FROM DEPARTMENT WHERE Dname = ?), CURRENT_TIMESTAMP)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, fnameField.getText());
                pstmt.setString(2, minitField.getText());
                pstmt.setString(3, lnameField.getText());
                pstmt.setString(4, ssn);
                pstmt.setString(5, bdate);
                pstmt.setString(6, address);
                pstmt.setString(7, gender);
                pstmt.setDouble(8, Double.parseDouble(salaryField.getText()));
                pstmt.setString(9, superSsn);
                pstmt.setString(10, (String) departmentComboBox.getSelectedItem());
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "직원 추가에 성공했습니다.");
                mainFrame.refreshTable();
                dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "직원 추가 중 오류가 발생했습니다.\n 내용: " + e.getMessage());
        }
    }
}