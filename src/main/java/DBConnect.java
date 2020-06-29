import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

class DBConnect {

    private Connection conn;

    /**
     * коннект с базой
     */
    void connectUDWH() throws SQLException, ClassNotFoundException {
        
    }

    /**
     * очистка таблицы
     */
    void deleteData() throws SQLException {
        Statement statement = null;
        try {
            statement = conn.createStatement();
            statement.executeUpdate("delete from RT_COST_CARS");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        statement.close();
    }

    /**
     * пишем в базу
     */
    void insertData(String mark, String model, String year, Integer min_cost, Integer avg_cost, Integer max_cost, String region) throws SQLException {
        Statement statement = conn.createStatement();
        statement.executeUpdate(
        "insert into RT_COST_CARS(mark, model, year, min_cost, avg_cost, max_cost, region)" +
            " values('" + mark + "','" + model + "','" + year + "','" + min_cost + "','" + avg_cost + "','" + max_cost + "','" + region + "')"
        );
        statement.close();
    }
}
