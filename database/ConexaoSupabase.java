import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoSupabase {

private static final String HOST = "aws-0-us-east-1.pooler.supabase.com";
private static final String PORT = "5432";
private static final String DATABASE = "postgres";
private static final String USER = "postgres.sqeswkzqdbafhktvoqcw";
private static final String PASSWORD = "#PooAcademiadasmaravilhas2026";

    private static final String URL = String.format(
        "jdbc:postgresql://%s:%s/%s?sslmode=require", HOST, PORT, DATABASE
    );

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}