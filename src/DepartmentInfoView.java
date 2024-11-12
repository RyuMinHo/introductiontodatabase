import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ItemListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class DepartmentInfoView extends JFrame {
    private JTable departmentTable;
    private Main mainFrame;
    private JTextField searchField;
    private JComboBox<String> searchCriteriaComboBox;
    private JComboBox<String> yearComparisonComboBox;
    private JButton addDepartmentButton;
    private JButton editDepartmentButton;
    private JButton deleteDepartmentButton;

    public DepartmentInfoView(Main mainFrame) {
        this.mainFrame = mainFrame;
        setTitle("부서 정보");
        setSize(800, 600);
        setLayout(new BorderLayout());

        initComponents();
        loadDepartmentData();

        setLocationRelativeTo(mainFrame);
        setVisible(true);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        searchCriteriaComboBox = new JComboBox<>(new String[]{"선택", "부서 이름", "부서 번호", "관리자 SSN", "관리자 시작 날짜"});
        yearComparisonComboBox = new JComboBox<>(new String[]{"이전", "이후"});
        yearComparisonComboBox.setVisible(false);

        searchField = new JTextField(20);
        JButton searchButton = new JButton("검색");

        topPanel.add(new JLabel("검색 기준:"));
        topPanel.add(searchCriteriaComboBox);
        topPanel.add(yearComparisonComboBox);
        topPanel.add(searchField);
        topPanel.add(searchButton);

        departmentTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(departmentTable);

        addDepartmentButton = new JButton("부서 추가");
        editDepartmentButton = new JButton("부서 수정");
        deleteDepartmentButton = new JButton("부서 삭제");

        bottomPanel.add(addDepartmentButton);
        bottomPanel.add(editDepartmentButton);
        bottomPanel.add(deleteDepartmentButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> performSearch());
        searchCriteriaComboBox.addActionListener(e -> {
            String selected = (String) searchCriteriaComboBox.getSelectedItem();
            yearComparisonComboBox.setVisible("관리자 시작 날짜".equals(selected));
        });

        addDepartmentButton.addActionListener(e -> {
            ManageDepartment manageDepartment = new ManageDepartment(mainFrame, this);
            manageDepartment.setupAddMode();
            manageDepartment.setVisible(true);
        });
        editDepartmentButton.addActionListener(e -> {
            ManageDepartment manageDepartment = new ManageDepartment(mainFrame, this);
            manageDepartment.setupEditMode();
            manageDepartment.setVisible(true);
        });
        deleteDepartmentButton.addActionListener(e -> {
            ManageDepartment manageDepartment = new ManageDepartment(mainFrame, this);
            manageDepartment.setupDeleteMode();
            manageDepartment.setVisible(true);
        });
    }

    private void loadDepartmentData() {
        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            String query = "SELECT * FROM DEPARTMENT";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                DefaultTableModel model = new DefaultTableModel();

                for (int i = 1; i <= columnCount; i++) {
                    model.addColumn(metaData.getColumnName(i));
                }

                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    model.addRow(row);
                }

                departmentTable.setModel(model);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "부서 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void performSearch() {
        String searchCriteria = (String) searchCriteriaComboBox.getSelectedItem();
        String searchText = searchField.getText().toLowerCase();

        if ("선택".equals(searchCriteria) || searchText.isEmpty()) {
            loadDepartmentData();
            return;
        }

        try (Connection connection = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            String query = buildSearchQuery(searchCriteria, searchText);
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                setQueryParameters(pstmt, searchCriteria, searchText);
                try (ResultSet rs = pstmt.executeQuery()) {
                    displaySearchResults(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "검색 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String buildSearchQuery(String searchCriteria, String searchText) {
        String baseQuery = "SELECT * FROM DEPARTMENT WHERE ";
        switch (searchCriteria) {
            case "부서 이름":
                return baseQuery + "Dname LIKE ?";
            case "부서 번호":
                return baseQuery + "Dnumber = ?";
            case "관리자 SSN":
                return baseQuery + "Mgr_ssn LIKE ?";
            case "관리자 시작 날짜":
                String comparison = yearComparisonComboBox.getSelectedItem().equals("이전") ? "<=" : ">";
                return baseQuery + "YEAR(Mgr_start_date) " + comparison + " ?";
            default:
                return "SELECT * FROM DEPARTMENT";
        }
    }

    private void setQueryParameters(PreparedStatement pstmt, String searchCriteria, String searchText) throws SQLException {
        switch (searchCriteria) {
            case "부서 이름":
            case "관리자 SSN":
                pstmt.setString(1, "%" + searchText + "%");
                break;
            case "부서 번호":
                pstmt.setInt(1, Integer.parseInt(searchText));
                break;
            case "관리자 시작 날짜":
                pstmt.setInt(1, Integer.parseInt(searchText));
                break;
        }
    }

    private void displaySearchResults(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        DefaultTableModel model = new DefaultTableModel();

        for (int i = 1; i <= columnCount; i++) {
            model.addColumn(metaData.getColumnName(i));
        }

        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i - 1] = rs.getObject(i);
            }
            model.addRow(row);
        }

        departmentTable.setModel(model);
    }

    public void refreshTable() {
        loadDepartmentData();
    }
}