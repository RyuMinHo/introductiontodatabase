import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ERDViewer extends JFrame {
    private List<Table> tables; // 테이블 목록 저장 목적 리스트

    public ERDViewer() {
        setTitle("ERD Viewer");
        setSize(1400, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        tables = new ArrayList<>();

        loadDatabaseSchema(); // 데이터베이스 스키마 로드

        ERDPanel erdPanel = new ERDPanel(); // ERD를 그릴 패널 생성
        add(erdPanel); // 패널을 프레임에 추가
    }

    // 데이터베이스 스키마를 로드하는 함수
    private void loadDatabaseSchema() {
        try (Connection conn = DriverManager.getConnection(Main.DB_URL, Main.DB_USER, Main.DB_PASSWORD)) {
            DatabaseMetaData metaData = conn.getMetaData();

            // COMPANY 스키마의 테이블만 로드
            ResultSet rs = metaData.getTables(null, "COMPANY", "%", new String[]{"TABLE"});
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");

                // 중복된 테이블 발생 박지 목적
                if (findTable(tableName) != null) {
                    continue; // 이미 추가된 테이블은 건너뜀
                }

                // sys_config 테이블 생성 방지 목적
                if (tableName.toLowerCase().contains("sys")) {
                    continue;
                }

                Table table = new Table(tableName);

                // Column 정보 로드
                ResultSet columns = metaData.getColumns(null, "COMPANY", tableName, null);
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String dataType = columns.getString("TYPE_NAME");

                    // 중복된 Column 생성 방지 목적
                    if (!table.hasColumn(columnName)) {
                        boolean isPrimaryKey = isPrimaryKey(metaData, tableName, columnName);
                        table.addColumn(columnName, dataType, isPrimaryKey); // 컬럼 추가
                    }
                }

                tables.add(table); // 테이블 리스트에 추가
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스 스키마를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // Column이 프라이머리 키인지 확인하는 함수
    private boolean isPrimaryKey(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
        ResultSet pkRs = metaData.getPrimaryKeys(null, "COMPANY", tableName);
        while (pkRs.next()) {
            if (columnName.equals(pkRs.getString("COLUMN_NAME"))) {
                return true;
            }
        }
        return false;
    }

    private class ERDPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            int x = 50;
            int y = 50;
            int width = 200; // 테이블 너비 설정
            int heightPerRow = 20; // 각 행의 높이 설정

            // 테이블 디테일
            for (Table table : tables) {
                int tableHeight = heightPerRow * (table.columns.size() + 1);

                g2d.setColor(Color.BLACK); // 테두리 색상
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawRect(x, y, width, tableHeight); // 테두리 그리기
                g2d.drawString(table.name, x + 5, y + 15); // 테이블 이름 출력

                // 테이블의 위치와 크기 저장
                table.x = x;
                table.y = y;
                table.width = width;
                table.height = tableHeight;

                // Column 그리기
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                int columnY = y + heightPerRow;
                for (Column column : table.columns) {
                    if (column.isPrimaryKey) { // 프라이머리 키는 빨간색으로 표시하고 PK 추가
                        g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 12));
                        g2d.setColor(Color.RED);
                        g2d.drawString(column.name + " (" + column.dataType + ") PK", x + 5, columnY + 15);
                    } else { // 프라이머리키가 아니면 검은색으로 표시
                        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                        g2d.setColor(Color.BLACK);
                        g2d.drawString(column.name + " (" + column.dataType + ")", x + 5, columnY + 15);
                    }
                    columnY += heightPerRow; // 다음 Column의 Y 위치 계산
                }

                x += width + 50; // 다음 테이블의 X 위치로 이동
                if (x > getWidth() - width) { // 화면 너비를 넘어가면 다음 행으로 이동
                    x = 50;
                    y += tableHeight + 50;
                }
            }
        }
    }

    // 주어진 테이블 이름에 해당하는 Table 객체를 찾는 함수
    private Table findTable(String tableName) {
        for (Table table : tables) {
            if (table.name.equals(tableName)) {
                return table;
            }
        }
        return null;
    }

    private static class Table {
        String name;
        List<Column> columns;
        int x, y;
        int width, height;

        Table(String name) {
            this.name = name;
            this.columns = new ArrayList<>();
        }

        // 새로운 Column을 해당 테이블에 추가
        void addColumn(String name, String dataType, boolean isPrimaryKey) {
            columns.add(new Column(name, dataType, isPrimaryKey));
        }

        // 해당 이름의 Column이 이미 존재하는지 확인
        boolean hasColumn(String columnName) {
            for (Column column : columns) {
                if (column.name.equals(columnName)) {
                    return true; // 중복된 컬럼 존재 여부 확인
                }
            }
            return false;
        }
    }

    // Column 내용
    private static class Column {
        String name;
        String dataType;
        boolean isPrimaryKey;

        Column(String name, String dataType, boolean isPrimaryKey) {
            this.name = name;
            this.dataType = dataType;
            this.isPrimaryKey = isPrimaryKey;
        }
    }
}