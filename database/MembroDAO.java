import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembroDAO {

    public void salvar(Membro m) throws SQLException {
        String sqlUsuario = """
            INSERT INTO usuario (nome, data_nascimento, cpf, email, username, senha, ativo, tipo_usuario)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'MEMBRO')
        """;
        String sqlMembro = "INSERT INTO membro (cpf, endereco, telefone) VALUES (?, ?, ?)";

        try (Connection conn = ConexaoSupabase.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario);
                 PreparedStatement psMembro = conn.prepareStatement(sqlMembro)) {

                psUser.setString(1, m.getNome());
                psUser.setDate(2, new java.sql.Date(m.getDataNascimento().getTime()));
                psUser.setString(3, m.getCpf());
                psUser.setString(4, m.getEmail());
                psUser.setString(5, m.getUserName());
                psUser.setString(6, m.getSenha());
                psUser.setBoolean(7, m.isAtivo());
                psUser.executeUpdate();

                psMembro.setString(1, m.getCpf());
                psMembro.setString(2, m.getEndereco());
                psMembro.setString(3, m.getTelefone());
                psMembro.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void atualizarPorCpf(String cpfAtual, Membro m) throws SQLException {
        String sqlUsuario = """
            UPDATE usuario SET nome = ?, data_nascimento = ?, cpf = ?, email = ?,
            username = ?, senha = ?, ativo = ? WHERE cpf = ?
        """;
        String sqlMembro = "UPDATE membro SET endereco = ?, telefone = ? WHERE cpf = ?";

        try (Connection conn = ConexaoSupabase.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psUser = conn.prepareStatement(sqlUsuario);
                 PreparedStatement psMembro = conn.prepareStatement(sqlMembro)) {

                psUser.setString(1, m.getNome());
                psUser.setDate(2, new java.sql.Date(m.getDataNascimento().getTime()));
                psUser.setString(3, m.getCpf());
                psUser.setString(4, m.getEmail());
                psUser.setString(5, m.getUserName());
                psUser.setString(6, m.getSenha());
                psUser.setBoolean(7, m.isAtivo());
                psUser.setString(8, cpfAtual);
                psUser.executeUpdate();

                psMembro.setString(1, m.getEndereco());
                psMembro.setString(2, m.getTelefone());
                psMembro.setString(3, m.getCpf());
                psMembro.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public List<Membro> carregarTodos() throws SQLException {
        List<Membro> lista = new ArrayList<>();
        String sql = """
            SELECT u.*, m.endereco, m.telefone FROM usuario u
            INNER JOIN membro m ON u.cpf = m.cpf
            ORDER BY u.nome
        """;

        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Membro m = Membro.criarMembro(
                    rs.getString("nome"),
                    new java.util.Date(rs.getDate("data_nascimento").getTime()),
                    rs.getString("cpf"),
                    rs.getString("email"),
                    rs.getString("username"),
                    rs.getString("senha"),
                    rs.getString("endereco"),
                    rs.getString("telefone")
                );
                m.setAtivo(rs.getBoolean("ativo"));
                lista.add(m);
            }
        }
        return lista;
    }

    public static Membro buscarEmMemoria(String cpf) {
        for (Membro m : Membro.listarMembros()) {
            if (m.getCpf().equals(cpf)) {
                return m;
            }
        }
        return null;
    }

    public int contarMembrosAtivos() throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM usuario u
            INNER JOIN membro m ON u.cpf = m.cpf
            WHERE u.ativo = TRUE
        """;
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}