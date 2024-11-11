import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Main extends JFrame {
    /* GUI 컴포넌트 */
    private JPanel panel1;
    private JTable EmployeeTable;
    private JCheckBox fnameCheckBox, minitCheckBox, lnameCheckBox, ssnCheckBox, bdateCheckBox, addressCheckBox,
            sexCheckBox, salaryCheckBox, supervisorCheckBox, departmentCheckBox, modifiedCheckBox;
    private JComboBox<String> SearchRangeComboBox, genderComboBox, departmentComboBox, cityComboBox, groupAvgSalaryComboBox;
    private JTextField salaryTextField, nameField, ssnField, birthdateField, superSsnField/*, modifiedDateField;*/;
    private JButton updateButton, searchButton, addEmployeeButton, editEmployeeButton, deleteEmployeeButton, calculateAvgSalaryButton, showDepartmentButton, showERDButton, downloadCSVButton;

    public static final String DB_URL = "jdbc:mysql://localhost:3306/COMPANY";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "BradleyRyu";

    private DepartmentInfoView departmentInfoView;

    public Main() {
        setTitle("105조 직원 관리 시스템");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        initComponents();
        layoutComponents();

        try {
            Connection connection = getConnection();
            if (connection != null) {
                System.out.println("DB에 성공적으로 연결했습니다!");
                displayEmployeeTable(connection);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setVisible(true);
    }

    private void initComponents() {
        panel1 = new JPanel();
        SearchRangeComboBox = new JComboBox<>(new String[]{"전체", "이름", "성별", "부서", "거주 도시", "연봉", "Ssn", "상사 Ssn", "생년월일"/*, "수정 날짜"*/});
        genderComboBox = new JComboBox<>(new String[]{"M", "F"});
        departmentComboBox = new JComboBox<>();
        cityComboBox = new JComboBox<>();
        salaryTextField = new JTextField(20);
        EmployeeTable = new JTable();
        fnameCheckBox = new JCheckBox("First Name", true);
        minitCheckBox = new JCheckBox("Middle Initial", true);
        lnameCheckBox = new JCheckBox("Last Name", true);
        ssnCheckBox = new JCheckBox("SSN", true);
        bdateCheckBox = new JCheckBox("Birth Date", true);
        addressCheckBox = new JCheckBox("Address", true);
        sexCheckBox = new JCheckBox("Sex", true);
        salaryCheckBox = new JCheckBox("Salary", true);
        supervisorCheckBox = new JCheckBox("Super_ssn", true);
        departmentCheckBox = new JCheckBox("Department", true);
        modifiedCheckBox = new JCheckBox("Modified Date", true);
        //updateButton = new JButton("새로고침");
        searchButton = new JButton("검색");
        addEmployeeButton = new JButton("직원 추가");
        editEmployeeButton = new JButton("직원 수정");
        deleteEmployeeButton = new JButton("직원 삭제");
        showERDButton = new JButton("ERD");
        groupAvgSalaryComboBox = new JComboBox<>(new String[]{"그룹 없음", "성별", "상급자", "부서"});
        calculateAvgSalaryButton = new JButton("평균 월급 계산");
        showDepartmentButton = new JButton("부서 정보 보기");
        downloadCSVButton = new JButton("CSV 파일로 다운로드");

        nameField = new JTextField(20);
        ssnField = new JTextField(20);
        birthdateField = new JTextField(20);
        superSsnField = new JTextField(20);
        //modifiedDateField = new JTextField(20);

        ItemListener itemListener = e -> {
            try {
                Connection connection = getConnection();
                if (connection != null) {
                    displayEmployeeTable(connection);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };

        fnameCheckBox.addItemListener(e -> refreshTable());
        minitCheckBox.addItemListener(e -> refreshTable());
        lnameCheckBox.addItemListener(e -> refreshTable());
        ssnCheckBox.addItemListener(e -> refreshTable());
        bdateCheckBox.addItemListener(e -> refreshTable());
        addressCheckBox.addItemListener(e -> refreshTable());
        sexCheckBox.addItemListener(e -> refreshTable());
        salaryCheckBox.addItemListener(e -> refreshTable());
        supervisorCheckBox.addItemListener(e -> refreshTable());
        departmentCheckBox.addItemListener(e -> refreshTable());
        modifiedCheckBox.addItemListener(e -> refreshTable());

        SearchRangeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) e.getItem();
                genderComboBox.setVisible(false);
                departmentComboBox.setVisible(false);
                salaryTextField.setVisible(false);
                cityComboBox.setVisible(false);
                nameField.setVisible(false);
                ssnField.setVisible(false);
                birthdateField.setVisible(false);
                superSsnField.setVisible(false);
                //modifiedDateField.setVisible(false);

                switch (selected) {
                    case "성별":
                        genderComboBox.setVisible(true);
                        break;
                    case "부서":
                        departmentComboBox.setVisible(true);
                        displayDepartmentComboBox();
                        break;
                    case "연봉":
                        salaryTextField.setVisible(true);
                        break;
                    case "거주 도시":
                        cityComboBox.setVisible(true);
                        displayCityComboBox();
                        break;
                    case "이름":
                        nameField.setVisible(true);
                        break;
                    case "Ssn":
                        ssnField.setVisible(true);
                        break;
                    case "생년월일":
                        birthdateField.setVisible(true);
                        break;
                    case "상사 Ssn":
                        superSsnField.setVisible(true);
                        break;
                    //case "수정 날짜":
                    //    modifiedDateField.setVisible(true);
                    //    break;
                }
                panel1.revalidate();
                panel1.repaint();
                refreshTable();
            }
        });


        //calculateAvgSalaryButton.addActionListener(e -> calculateAndDisplayAvgSalary());

        showERDButton.addActionListener(e -> showERD());

        calculateAvgSalaryButton.addActionListener(e ->  {
            calculateAndDisplayAvgSalary();
            refreshTable();
        });
    }



    private String buildAvgSalaryQuery(String selectedGroup) {
        String query = "SELECT ";
        switch (selectedGroup) {
            case "성별":
                query += "E.Sex, AVG(E.Salary) ";
                query += "FROM EMPLOYEE E ";
                query += "GROUP BY E.Sex";
                break;
            case "상급자":
                query += "E.Super_ssn, AVG(E.Salary) ";
                query += "FROM EMPLOYEE E ";
                query += "WHERE E.Super_ssn IS NOT NULL ";
                query += "GROUP BY E.Super_ssn";
                break;
            case "부서":
                query += "D.Dname, AVG(E.Salary) ";
                query += "FROM EMPLOYEE E JOIN DEPARTMENT D ON E.Dno = D.Dnumber ";
                query += "GROUP BY D.Dname";
                break;
            default:
                query += "'전체' AS GroupName, AVG(E.Salary) ";
                query += "FROM EMPLOYEE E";
                break;
        }
        return query;
    }

    private void calculateAndDisplayAvgSalary() {
        String selectedGroup = (String) groupAvgSalaryComboBox.getSelectedItem();
        String query = buildAvgSalaryQuery(selectedGroup);

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            StringBuilder result = new StringBuilder(selectedGroup + "에 따른 평균 월급:\n");

            if ("상급자".equals(selectedGroup)) {
                while (rs.next()) {
                    String supervisorSsn = rs.getString(1);
                    double avgSalary = rs.getDouble(2);

                    // 상급자 정보 조회
                    String supervisorQuery = "SELECT Fname, Lname, Salary FROM EMPLOYEE WHERE Ssn = ?";
                    try (PreparedStatement supervisorStmt = connection.prepareStatement(supervisorQuery)) {
                        supervisorStmt.setString(1, supervisorSsn);
                        try (ResultSet supervisorRs = supervisorStmt.executeQuery()) {
                            if (supervisorRs.next()) {
                                String supervisorName = supervisorRs.getString("Fname") + " " + supervisorRs.getString("Lname");
                                double supervisorSalary = supervisorRs.getDouble("Salary");
                                result.append("상급자: ").append(supervisorName)
                                        .append(", 월급: ").append(String.format("%.2f", supervisorSalary)).append("\n");
                            }
                        }
                    }

                    // 부하직원 정보 및 평균 월급 조회
                    String subordinatesQuery = "SELECT AVG(Salary) as AvgSalary FROM EMPLOYEE WHERE Super_ssn = ?";
                    try (PreparedStatement subStmt = connection.prepareStatement(subordinatesQuery)) {
                        subStmt.setString(1, supervisorSsn);
                        try (ResultSet subRs = subStmt.executeQuery()) {
                            if (subRs.next()) {
                                double subordinatesAvgSalary = subRs.getDouble("AvgSalary");
                                result.append("부하직원 평균 월급: ").append(String.format("%.2f", subordinatesAvgSalary)).append("\n");
                            }
                        }
                    }

                    result.append("전체 평균 월급: ").append(String.format("%.2f", avgSalary)).append("\n\n");
                }
            } else {
                // 기존 코드 유지
                while (rs.next()) {
                    if ("성별".equals(selectedGroup)) {
                        result.append("성별: ").append(rs.getString(1))
                                .append(", 평균 월급: ").append(String.format("%.2f", rs.getDouble(2))).append("\n");
                    } else if ("부서".equals(selectedGroup)) {
                        result.append("부서: ").append(rs.getString(1))
                                .append(", 평균 월급: ").append(String.format("%.2f", rs.getDouble(2))).append("\n");
                    } else {
                        result.append("전체 평균 월급: ").append(String.format("%.2f", rs.getDouble(2))).append("\n");
                    }
                }
            }

            JOptionPane.showMessageDialog(this, result.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "평균 월급 계산 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void layoutComponents() {
        panel1.setLayout(new BorderLayout());
        JPanel checkBoxPanel = new JPanel(new GridLayout(1, 10));
        JPanel searchPanel = new JPanel(new FlowLayout());

        searchPanel.add(new JLabel("검색 범위:"));
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JPanel groupByPanel = new JPanel(new FlowLayout());

        //buttonPanel.add(updateButton);
        buttonPanel.add(addEmployeeButton);
        buttonPanel.add(editEmployeeButton);
        buttonPanel.add(deleteEmployeeButton);
        buttonPanel.add(showERDButton);
        buttonPanel.add(showDepartmentButton);
        buttonPanel.add(downloadCSVButton);

        searchPanel.add(SearchRangeComboBox);
        searchPanel.add(genderComboBox);
        searchPanel.add(departmentComboBox);
        searchPanel.add(salaryTextField);
        searchPanel.add(cityComboBox);
        searchPanel.add(nameField);
        searchPanel.add(ssnField);
        searchPanel.add(birthdateField);
        searchPanel.add(superSsnField);
        //searchPanel.add(modifiedDateField);
        searchPanel.add(searchButton);

        groupByPanel.add(new JLabel("그룹별 평균 월급: "));
        groupByPanel.add(groupAvgSalaryComboBox);
        groupByPanel.add(calculateAvgSalaryButton);

        genderComboBox.setVisible(false);
        departmentComboBox.setVisible(false);
        salaryTextField.setVisible(false);
        cityComboBox.setVisible(false);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(searchPanel, BorderLayout.NORTH);
        northPanel.add(checkBoxPanel, BorderLayout.SOUTH);
        northPanel.add(groupByPanel, BorderLayout.CENTER);

        panel1.add(northPanel, BorderLayout.NORTH);
        panel1.add(new JScrollPane(EmployeeTable), BorderLayout.CENTER);
        panel1.add(buttonPanel, BorderLayout.SOUTH);
        //panel1.add(avgSalaryPanel, BorderLayout.EAST);

        checkBoxPanel.add(fnameCheckBox);
        checkBoxPanel.add(minitCheckBox);
        checkBoxPanel.add(lnameCheckBox);
        checkBoxPanel.add(ssnCheckBox);
        checkBoxPanel.add(bdateCheckBox);
        checkBoxPanel.add(addressCheckBox);
        checkBoxPanel.add(sexCheckBox);
        checkBoxPanel.add(salaryCheckBox);
        checkBoxPanel.add(supervisorCheckBox);
        checkBoxPanel.add(departmentCheckBox);
        checkBoxPanel.add(modifiedCheckBox);

        nameField.setVisible(false);
        ssnField.setVisible(false);
        birthdateField.setVisible(false);
        superSsnField.setVisible(false);
        //modifiedDateField.setVisible(false);

        add(panel1);
    /*
        updateButton.addActionListener(e -> {
            try {
                Connection connection = getConnection();
                if (connection != null) {
                    displayEmployeeTable(connection);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
    */
        searchButton.addActionListener(e -> {
            try (Connection connection = getConnection()) {
                String selectedRange = (String) SearchRangeComboBox.getSelectedItem();
                String query = buildQuery(selectedRange);
                displayEmployeeTableWithQuery(connection, query);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(Main.this, "검색 중 오류가 발생했습니다.\n 내용: " + ex.getMessage());
            }
        });

        downloadCSVButton.addActionListener(e -> exportToCSVFromTable(EmployeeTable));

        addEmployeeButton.addActionListener(e -> {
            AddEmployee addEmployeeFrame = new AddEmployee(Main.this);
            addEmployeeFrame.setVisible(true);
            refreshTable();
        });

        showDepartmentButton.addActionListener(e -> {
            if (departmentInfoView == null || !departmentInfoView.isVisible()) {
                departmentInfoView = new DepartmentInfoView(this);
            } else {
                departmentInfoView.toFront();
            }
        });

        editEmployeeButton.addActionListener(e -> {
            editEmployee();
            refreshTable();
        });

        deleteEmployeeButton.addActionListener(e ->  {
            deleteEmployee();
            refreshTable();
        });
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void refreshTable() {
        try (Connection connection = getConnection()) {
            String selectedRange = (String) SearchRangeComboBox.getSelectedItem();
            String query = buildQuery(selectedRange);
            displayEmployeeTableWithQuery(connection, query);
            displayDepartmentComboBox();
            if (departmentInfoView != null && departmentInfoView.isVisible()) {
                departmentInfoView.refreshTable();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "테이블을 새로고침하는 동안 오류가 발생했습니다.\n 내용: " + ex.getMessage());
        }
    }

    private void displayEmployeeTable(Connection connection) {
        String query = buildQuery("전체");
        displayEmployeeTableWithQuery(connection, query);
    }

    private void displayEmployeeTableWithQuery(Connection connection, String query) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Create column names array including the checkbox column
            String[] columnNames = new String[columnCount + 1];
            columnNames[0] = "선택"; // First column name
            for (int i = 1; i <= columnCount; i++) {
                columnNames[i] = metaData.getColumnName(i);
            }

            // Create DefaultTableModel
            DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return columnIndex == 0 ? Boolean.class : super.getColumnClass(columnIndex);
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0; // Make only the checkbox column editable
                }
            };

            // Add rows from ResultSet to the model
            while (rs.next()) {
                Object[] row = new Object[columnCount + 1];
                row[0] = Boolean.FALSE; // Default value for the checkbox
                for (int i = 1; i <= columnCount; i++) {
                    row[i] = rs.getObject(i);
                }
                model.addRow(row);
            }

            EmployeeTable.setModel(model);

            // Adjust column visibility based on checkbox states
            TableColumnModel columnModel = EmployeeTable.getColumnModel();
            columnModel.getColumn(1).setMinWidth(fnameCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(1).setMaxWidth(fnameCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(2).setMinWidth(minitCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(2).setMaxWidth(minitCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(3).setMinWidth(lnameCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(3).setMaxWidth(lnameCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(4).setMinWidth(ssnCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(4).setMaxWidth(ssnCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(5).setMinWidth(bdateCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(5).setMaxWidth(bdateCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(6).setMinWidth(addressCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(6).setMaxWidth(addressCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(7).setMinWidth(sexCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(7).setMaxWidth(sexCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(8).setMinWidth(salaryCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(8).setMaxWidth(salaryCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(9).setMinWidth(supervisorCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(9).setMaxWidth(supervisorCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(10).setMinWidth(departmentCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(10).setMaxWidth(departmentCheckBox.isSelected() ? Integer.MAX_VALUE : 0);
            columnModel.getColumn(11).setMinWidth(modifiedCheckBox.isSelected() ? 15 : 0);
            columnModel.getColumn(11).setMaxWidth(modifiedCheckBox.isSelected() ? Integer.MAX_VALUE : 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showERD() {
        ERDViewer erdViewer = new ERDViewer();
        erdViewer.setVisible(true);
    }

    private void displayDepartmentComboBox() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT Dname FROM DEPARTMENT")) {
            departmentComboBox.removeAllItems();
            while (rs.next()) {
                departmentComboBox.addItem(rs.getString("Dname").trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayCityComboBox() {
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT SUBSTRING_INDEX(Address, ',', -2) AS CityState FROM EMPLOYEE")) {
            cityComboBox.removeAllItems();
            while (rs.next()) {
                cityComboBox.addItem(rs.getString("CityState").trim());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayDepartmentTable() {
        String query = "SELECT * FROM DEPARTMENT";

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            DefaultTableModel model = new DefaultTableModel();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Add column names to the model
            for (int i = 1; i <= columnCount; i++) {
                model.addColumn(metaData.getColumnName(i));
            }

            // Add rows to the model
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                model.addRow(row);
            }

            // Set the model to the table
            EmployeeTable.setModel(model);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서 테이블을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String buildQuery(String selectedRange) {
        List<String> selectedColumns = new ArrayList<>();
        selectedColumns.add("E.Fname"); // Always include Fname column
        selectedColumns.add("E.Minit"); // Always include Minit column
        selectedColumns.add("E.Lname"); // Always include Lname column
        selectedColumns.add("E.Ssn"); // Always include Ssn column
        selectedColumns.add("E.Bdate"); // Always include Bdate column
        selectedColumns.add("E.Address"); // Always include Address column
        selectedColumns.add("E.Sex"); // Always include Sex column
        selectedColumns.add("E.Salary"); // Always include Salary column
        selectedColumns.add("E.Super_ssn"); // Always include Super_ssn column
        selectedColumns.add("D.Dname"); // Always include Dname column
        selectedColumns.add("E.modified");

        String columns = String.join(", ", selectedColumns);
        String query = "SELECT " + columns + " FROM EMPLOYEE E JOIN DEPARTMENT D ON E.Dno = D.Dnumber";

        switch (selectedRange) {
            case "성별":
                query += " WHERE E.Sex = '" + genderComboBox.getSelectedItem() + "'";
                break;
            case "부서":
                query += " WHERE D.Dname = '" + departmentComboBox.getSelectedItem() + "'";
                break;
            case "연봉":
                query += " WHERE E.Salary > " + salaryTextField.getText();
                break;
            case "거주 도시":
                String selectedCity = (String) cityComboBox.getSelectedItem();
                if (selectedCity != null) {
                    String[] parts = selectedCity.split(", ");
                    query += " WHERE E.Address LIKE '%" + parts[0] + "%' AND E.Address LIKE '%" + parts[1] + "%'";
                }
                break;
            case "이름":
                query += " WHERE CONCAT(E.Fname, ' ', E.Minit, ' ', E.Lname) LIKE '%" + nameField.getText() + "%'";
                break;
            case "Ssn":
                query += " WHERE E.Ssn = '" + ssnField.getText() + "'";
                break;
            case "생년월일":
                query += " WHERE E.Bdate = '" + birthdateField.getText() + "'";
                break;
            case "상사 Ssn":
                query += " WHERE E.Super_ssn = '" + superSsnField.getText() + "'";
                break;
            /* case "수정 날짜":
                query += " WHERE E.modified >= " + modifiedDateField.getText();
                break;*/
        }

        return query;
    }

    private void editEmployee() {
        DefaultTableModel model = (DefaultTableModel) EmployeeTable.getModel();
        int selectedRow = EmployeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수정할 직원을 선택해주세요.");
            return;
        }

        new EditEmployee(model, selectedRow, this).setVisible(true);
    }

    private void deleteEmployee() {
        DefaultTableModel model = (DefaultTableModel) EmployeeTable.getModel();
        List<String> ssnsToDelete = new ArrayList<>();

        // Collect SSNs of selected employees
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isChecked = (Boolean) model.getValueAt(i, 0);
            if (isChecked) {
                String ssn = (String) model.getValueAt(i, 4); // Assuming SSN is in the 5th column (index 4)
                ssnsToDelete.add(ssn);
            }
        }

        if (ssnsToDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this, "삭제할 직원을 선택해주세요.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "선택한 " + ssnsToDelete.size() + "명의 직원을 삭제하시겠습니까?",
                "직원 삭제 확인",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection connection = getConnection()) {
            String query = "DELETE FROM EMPLOYEE WHERE Ssn = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                connection.setAutoCommit(false);
                for (String ssn : ssnsToDelete) {
                    pstmt.setString(1, ssn);
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                connection.commit();

                int deletedCount = 0;
                for (int result : results) {
                    if (result > 0) deletedCount++;
                }

                JOptionPane.showMessageDialog(this, deletedCount + "명의 직원이 성공적으로 삭제되었습니다.");
                refreshTable();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "직원 삭제 중 오류가 발생했습니다.\n내용: " + ex.getMessage());
        }
    }

    private void exportToCSVFromTable(JTable table) {
        String filePath = System.getProperty("user.home") + "/검색결과.csv";
        try (FileWriter csvWriter = new FileWriter(filePath)) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            int columnCount = model.getColumnCount();

            for (int i = 0; i < columnCount; i++) {
                csvWriter.append(model.getColumnName(i));
                if (i < columnCount - 1) {
                    csvWriter.append(",");
                }
            }
            csvWriter.append("\n");

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < columnCount; j++) {
                    csvWriter.append(String.valueOf(model.getValueAt(i, j)));
                    if (j < columnCount - 1) {
                        csvWriter.append(",");
                    }
                }
                csvWriter.append("\n");
            }

            System.out.println("검색 결과가 파일로 저장되었습니다: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "CSV 파일 내보내기 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}