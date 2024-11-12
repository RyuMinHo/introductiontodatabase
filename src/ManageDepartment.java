import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ManageDepartment extends JFrame {
    private JTextField dnameField;
    private JTextField dnumberField;
    private JTextField mgrSsnField;
    private JTextField mgrStartDateField;
    private JComboBox<String> departmentComboBox;
    private JLabel dnumberLabel, mgrSsnLabel, mgrStartDateLabel;
    private Main mainFrame;
    private DepartmentInfoView departmentInfoView;
    private JPanel formPanel;
    private String currentMode;

    public ManageDepartment(Main mainFrame, DepartmentInfoView departmentInfoView) {
        this.mainFrame = mainFrame;
        this.departmentInfoView = departmentInfoView;
        setTitle("부서 관리");
        setSize(400, 300);
        setLayout(new BorderLayout());
        initComponents();
        layoutComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2));

        departmentComboBox = new JComboBox<>();
        dnameField = new JTextField(20);
        dnumberField = new JTextField();
        mgrSsnField = new JTextField();
        mgrStartDateField = new JTextField();

        dnumberLabel = new JLabel();
        mgrSsnLabel = new JLabel();
        mgrStartDateLabel = new JLabel();

        departmentComboBox.addActionListener(e -> {
            if ("edit".equals(currentMode) || "delete".equals(currentMode)) {
                if (departmentComboBox.getSelectedIndex() > 0) {
                    loadDepartmentDetails();
                } else {
                    clearFields();
                }
            }
        });
    }

    private void layoutComponents() {
        add(formPanel, BorderLayout.CENTER);
    }

    public void setupAddMode() {
        currentMode = "add";
        formPanel.removeAll();
        formPanel.setLayout(new GridLayout(6, 2));
        formPanel.add(new JLabel("부서 이름:"));
        formPanel.add(dnameField);
        formPanel.add(new JLabel("부서 번호:"));
        formPanel.add(dnumberField);
        formPanel.add(new JLabel("관리자 SSN:"));
        formPanel.add(mgrSsnField);
        formPanel.add(new JLabel("관리자 시작 날짜 (YYYY-MM-DD):"));
        formPanel.add(mgrStartDateField);
        JButton saveButton = new JButton("저장");
        JButton cancelButton = new JButton("취소");
        formPanel.add(saveButton);
        formPanel.add(cancelButton);
        saveButton.addActionListener(e -> addDepartment());
        cancelButton.addActionListener(e -> dispose());
        clearFields();
        formPanel.setVisible(true);
        formPanel.revalidate();
        formPanel.repaint();
    }

    public void setupEditMode() {
        currentMode = "edit";
        formPanel.removeAll();
        formPanel.setLayout(new GridLayout(6, 2));
        formPanel.add(new JLabel("부서 이름:"));
        formPanel.add(departmentComboBox);
        formPanel.add(new JLabel("부서 번호:"));
        formPanel.add(dnumberField);
        formPanel.add(new JLabel("관리자 SSN:"));
        formPanel.add(mgrSsnField);
        formPanel.add(new JLabel("관리자 시작 날짜 (YYYY-MM-DD):"));
        formPanel.add(mgrStartDateField);
        JButton saveButton = new JButton("저장");
        JButton cancelButton = new JButton("취소");
        formPanel.add(saveButton);
        formPanel.add(cancelButton);
        saveButton.addActionListener(e -> editDepartment());
        cancelButton.addActionListener(e -> dispose());
        loadDepartments();
        formPanel.setVisible(true);
        formPanel.revalidate();
        formPanel.repaint();
    }

    public void setupDeleteMode() {
        currentMode = "delete";
        formPanel.removeAll();
        formPanel.setLayout(new GridLayout(6, 2));
        formPanel.add(new JLabel("부서 이름:"));
        formPanel.add(departmentComboBox);
        formPanel.add(new JLabel("부서 번호:"));
        formPanel.add(dnumberLabel);
        formPanel.add(new JLabel("관리자 SSN:"));
        formPanel.add(mgrSsnLabel);
        formPanel.add(new JLabel("관리자 시작 날짜:"));
        formPanel.add(mgrStartDateLabel);
        JButton deleteButton = new JButton("삭제");
        JButton cancelButton = new JButton("취소");
        formPanel.add(deleteButton);
        formPanel.add(cancelButton);
        deleteButton.addActionListener(e -> deleteDepartment());
        cancelButton.addActionListener(e -> dispose());
        loadDepartments();
        formPanel.setVisible(true);
        formPanel.revalidate();
        formPanel.repaint();
    }

    private void loadDepartments() {
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            String query = "SELECT Dname FROM DEPARTMENT";
            try (PreparedStatement pstmt = connection.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                departmentComboBox.removeAllItems();
                departmentComboBox.addItem("부서를 선택하세요");
                while (rs.next()) {
                    departmentComboBox.addItem(rs.getString("Dname"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void loadDepartmentDetails() {
        String selectedDepartment = (String) departmentComboBox.getSelectedItem();
        if (selectedDepartment == null || selectedDepartment.equals("부서를 선택하세요")) {
            return;
        }
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            String query = "SELECT * FROM DEPARTMENT WHERE Dname = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, selectedDepartment);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        if ("edit".equals(currentMode)) {
                            dnumberField.setText(String.valueOf(rs.getInt("Dnumber")));
                            mgrSsnField.setText(rs.getString("Mgr_ssn"));
                            mgrStartDateField.setText(rs.getString("Mgr_start_date"));
                        } else if ("delete".equals(currentMode)) {
                            dnumberLabel.setText(String.valueOf(rs.getInt("Dnumber")));
                            mgrSsnLabel.setText(rs.getString("Mgr_ssn"));
                            mgrStartDateLabel.setText(rs.getString("Mgr_start_date"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void clearFields() {
        dnameField.setText("");
        dnumberField.setText("");
        mgrSsnField.setText("");
        mgrStartDateField.setText("");
    }

    private void addDepartment() {
        String dname = dnameField.getText();
        String dnumber = dnumberField.getText();
        String mgrSsn = mgrSsnField.getText();
        String mgrStartDate = mgrStartDateField.getText();

        if (dname.isEmpty() || dnumber.isEmpty() || mgrSsn.isEmpty() || mgrStartDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.");
            return;
        }

        // Mgr_ssn 검증
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM EMPLOYEE WHERE Ssn = ?")) {
            pstmt.setString(1, mgrSsn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                JOptionPane.showMessageDialog(this, "존재하지 않는 Ssn입니다.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "관리자 SSN 확인 중 오류가 발생했습니다: " + e.getMessage());
            return;
        }

        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            connection.setAutoCommit(false);

            // 부서 추가
            String query = "INSERT INTO DEPARTMENT (Dname, Dnumber, Mgr_ssn, Mgr_start_date) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, dname);
                pstmt.setInt(2, Integer.parseInt(dnumber));
                pstmt.setString(3, mgrSsn);
                pstmt.setString(4, mgrStartDate);
                pstmt.executeUpdate();
            }

            // 관리자의 부서 정보 업데이트
            String updateEmployeeQuery = "UPDATE EMPLOYEE SET Dno = ? WHERE Ssn = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateEmployeeQuery)) {
                pstmt.setInt(1, Integer.parseInt(dnumber));
                pstmt.setString(2, mgrSsn);
                pstmt.executeUpdate();
            }

            connection.commit();
            JOptionPane.showMessageDialog(this, "부서가 성공적으로 추가되었습니다.");
            mainFrame.refreshTable();
            departmentInfoView.refreshTable();
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void editDepartment() {
        String dname = (String) departmentComboBox.getSelectedItem();
        String dnumber = dnumberField.getText();
        String mgrSsn = mgrSsnField.getText();
        String mgrStartDate = mgrStartDateField.getText();

        if (dname == null || dname.equals("부서를 선택하세요") || dnumber.isEmpty() || mgrSsn.isEmpty() || mgrStartDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 필드를 입력해주세요.");
            return;
        }

        // Mgr_ssn 검증
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM EMPLOYEE WHERE Ssn = ?")) {
            pstmt.setString(1, mgrSsn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                JOptionPane.showMessageDialog(this, "존재하지 않는 Ssn입니다.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "관리자 SSN 확인 중 오류가 발생했습니다: " + e.getMessage());
            return;
        }

        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            connection.setAutoCommit(false);

            // 1. 부서 정보 업데이트
            String updateDeptQuery = "UPDATE DEPARTMENT SET Dnumber = ?, Mgr_ssn = ?, Mgr_start_date = ? WHERE Dname = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateDeptQuery)) {
                pstmt.setInt(1, Integer.parseInt(dnumber));
                pstmt.setString(2, mgrSsn);
                pstmt.setString(3, mgrStartDate);
                pstmt.setString(4, dname);
                pstmt.executeUpdate();
            }

            // 2. 새 관리자의 부서 업데이트
            String updateEmployeeQuery = "UPDATE EMPLOYEE SET Dno = ? WHERE Ssn = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateEmployeeQuery)) {
                pstmt.setInt(1, Integer.parseInt(dnumber));
                pstmt.setString(2, mgrSsn);
                pstmt.executeUpdate();
            }

            connection.commit();
            JOptionPane.showMessageDialog(this, "부서가 성공적으로 수정되었습니다.");
            mainFrame.refreshTable();
            departmentInfoView.refreshTable();
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서를 수정하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void deleteDepartment() {
        String dname = (String) departmentComboBox.getSelectedItem();
        if (dname == null || dname.equals("부서를 선택하세요")) {
            JOptionPane.showMessageDialog(this, "삭제할 부서를 선택해주세요.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "정말로 부서 '" + dname + "'를 삭제하시겠습니까?",
                "부서 삭제 확인",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // 직원 존재 여부 확인
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM EMPLOYEE WHERE Dno = (SELECT Dnumber FROM DEPARTMENT WHERE Dname = ?)")) {
            pstmt.setString(1, dname);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "부서에 직원이 있어 삭제할 수 없습니다.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return;
        }

        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            String query = "DELETE FROM DEPARTMENT WHERE Dname = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                pstmt.setString(1, dname);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "부서가 성공적으로 삭제되었습니다.");
                mainFrame.refreshTable();
                departmentInfoView.refreshTable();
                dispose();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서를 삭제하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}