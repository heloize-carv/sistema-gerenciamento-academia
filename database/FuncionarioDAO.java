import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    public void salvar(Funcionario f) throws SQLException {
        String sqlUsuario = """
            INSERT INTO usuario (nome, data_nascimento, cpf, email, username, senha, ativo, tipo_usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'FUNCIONARIO')
        """;
        String sqlFunc = "INSERT INTO funcionario (cpf, cargo) VALUES (?, ?)";

        try (Connection conn = ConexaoSupabase.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario);
                 PreparedStatement psFunc = conn.prepareStatement(sqlFunc)) {

                psUser.setString(1, f.getNome());
                psUser.setDate(2, new java.sql.Date(f.getDataNascimento().getTime()));
                psUser.setString(3, f.getCpf());
                psUser.setString(4, f.getEmail());
                psUser.setString(5, f.getUserName());
                psUser.setString(6, f.getSenha());
                psUser.setBoolean(7, f.isAtivo());
                psUser.executeUpdate();

                psFunc.setString(1, f.getCpf());
                psFunc.setString(2, f.getCargo().name());
                psFunc.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void atualizarPorCpf(String cpfAtual, Funcionario f) throws SQLException {
        String sqlUsuario = """
            UPDATE usuario SET nome = ?, data_nascimento = ?, cpf = ?, email = ?,
            username = ?, senha = ?, ativo = ? WHERE cpf = ?
        """;
        String sqlFunc = "UPDATE funcionario SET cargo = ? WHERE cpf = ?";

        try (Connection conn = ConexaoSupabase.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario);
                 PreparedStatement psFunc = conn.prepareStatement(sqlFunc)) {

                psUser.setString(1, f.getNome());
                psUser.setDate(2, new java.sql.Date(f.getDataNascimento().getTime()));
                psUser.setString(3, f.getCpf());
                psUser.setString(4, f.getEmail());
                psUser.setString(5, f.getUserName());
                psUser.setString(6, f.getSenha());
                psUser.setBoolean(7, f.isAtivo());
                psUser.setString(8, cpfAtual);
                psUser.executeUpdate();

                psFunc.setString(1, f.getCargo().name());
                psFunc.setString(2, f.getCpf());
                psFunc.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Funcionario> carregarTodos() throws SQLException {
        List<Funcionario> lista = new ArrayList<>();
        String sql = """
            SELECT u.*, f.cargo FROM usuario u
            INNER JOIN funcionario f ON u.cpf = f.cpf
            ORDER BY u.nome
        """;

        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Funcionario f = Funcionario.criarFuncionario(
                    rs.getString("nome"),
                    new java.util.Date(rs.getDate("data_nascimento").getTime()),
                    rs.getString("cpf"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("senha"),
                    Cargo.valueOf(rs.getString("cargo"))
                );
                f.setAtivo(rs.getBoolean("ativo"));
                lista.add(f);
            }
        }
        return lista;
    }

    public static Funcionario buscarEmMemoria(String cpf) {
        for (Funcionario f : Funcionario.listarFuncionarios()) {
            if (f.getCpf().equals(cpf)) {
                return f;
            }
        }
        return null;
    }

    public int contarTotalFuncionarios() throws SQLException {
        String sql = "SELECT COUNT(*) FROM funcionario";
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}