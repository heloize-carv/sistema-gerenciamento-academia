import java.sql.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class AulaDAO {

    private static final Map<Aula, String> CHAVES = new IdentityHashMap<>();

    public void salvar(Aula aula) throws SQLException {
        String sqlAula = "INSERT INTO aula (horario, descricao, instrutor_cpf) VALUES (?, ?, ?)";

        try (Connection conn = ConexaoSupabase.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psAula = conn.prepareStatement(sqlAula)) {
                psAula.setString(1, aula.getHorario());
                psAula.setString(2, aula.getDescricao());
                psAula.setString(3, aula.getInstrutor().getCpf());
                psAula.executeUpdate();

                inserirAlunos(conn, aula.getHorario(), aula.getAlunos());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        CHAVES.put(aula, aula.getHorario());
    }

    public void atualizar(Aula aula) throws SQLException {
        String horarioAtual = CHAVES.get(aula);
        if (horarioAtual == null) {
            throw new SQLException("Aula não sincronizada com o banco.");
        }

        String sqlAula = "UPDATE aula SET horario = ?, descricao = ?, instrutor_cpf = ? WHERE horario = ?";
        String sqlLimpar = "DELETE FROM aula_membro WHERE aula_horario = ?";

        try (Connection conn = ConexaoSupabase.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psAula = conn.prepareStatement(sqlAula);
                 PreparedStatement psLimpar = conn.prepareStatement(sqlLimpar)) {

                psAula.setString(1, aula.getHorario());
                psAula.setString(2, aula.getDescricao());
                psAula.setString(3, aula.getInstrutor().getCpf());
                psAula.setString(4, horarioAtual);
                psAula.executeUpdate();

                psLimpar.setString(1, aula.getHorario());
                psLimpar.executeUpdate();

                inserirAlunos(conn, aula.getHorario(), aula.getAlunos());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        CHAVES.put(aula, aula.getHorario());
    }

    private void inserirAlunos(Connection conn, String horario, List<Membro> alunos) throws SQLException {
        String sql = "INSERT INTO aula_membro (aula_horario, membro_cpf) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Membro m : alunos) {
                ps.setString(1, horario);
                ps.setString(2, m.getCpf());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Aula> carregarTodas() throws SQLException {
        List<Aula> aulas = new ArrayList<>();
        String sql = "SELECT horario, descricao, instrutor_cpf FROM aula ORDER BY horario";

        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String horario = rs.getString("horario");
                Funcionario instrutor = FuncionarioDAO.buscarEmMemoria(rs.getString("instrutor_cpf"));
                if (instrutor == null) {
                    continue;
                }
                List<Membro> alunos = buscarAlunosDaAula(conn, horario);
                try {
                    Aula aula = new Aula(horario, rs.getString("descricao"), instrutor, alunos);
                    CHAVES.put(aula, horario);
                    aulas.add(aula);
                } catch (IllegalArgumentException e) {
                    continue;
                }
            }
        }
        return aulas;
    }

    private List<Membro> buscarAlunosDaAula(Connection conn, String horario) throws SQLException {
        List<Membro> alunos = new ArrayList<>();
        String sql = "SELECT membro_cpf FROM aula_membro WHERE aula_horario = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, horario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Membro m = MembroDAO.buscarEmMemoria(rs.getString("membro_cpf"));
                    if (m != null) {
                        alunos.add(m);
                    }
                }
            }
        }
        return alunos;
    }

    public void excluir(Aula aula) throws SQLException {
        String horario = CHAVES.get(aula);
        if (horario == null) {
            return;
        }
        String sql = "DELETE FROM aula WHERE horario = ?";
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, horario);
            ps.executeUpdate();
        }
        CHAVES.remove(aula);
    }
}