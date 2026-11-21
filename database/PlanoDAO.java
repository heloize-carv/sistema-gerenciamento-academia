import java.sql.*;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class PlanoDAO {

    private static final Map<Plano, Object[]> CHAVES = new IdentityHashMap<>();

    public void salvar(Plano p) throws SQLException {
        String sql = """
            INSERT INTO plano (beneficios, valor_mensal, regras_vigencia, disponivel)
            VALUES (?, ?, ?, ?)
        """;
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getBeneficios());
            ps.setFloat(2, p.getValorMensal());
            ps.setString(3, p.getRegrasVigencia());
            ps.setBoolean(4, p.isDisponivel());
            ps.executeUpdate();
        }
        CHAVES.put(p, chaveAtual(p));
    }

    public void atualizar(Plano p) throws SQLException {
        Object[] chave = CHAVES.get(p);
        if (chave == null) {
            throw new SQLException("Plano não sincronizado com o banco.");
        }
        String sql = """
            UPDATE plano SET beneficios = ?, valor_mensal = ?, regras_vigencia = ?, disponivel = ?
            WHERE beneficios = ? AND valor_mensal = ? AND regras_vigencia = ?
        """;
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getBeneficios());
            ps.setFloat(2, p.getValorMensal());
            ps.setString(3, p.getRegrasVigencia());
            ps.setBoolean(4, p.isDisponivel());
            ps.setString(5, (String) chave[0]);
            ps.setFloat(6, (Float) chave[1]);
            ps.setString(7, (String) chave[2]);
            ps.executeUpdate();
        }
        CHAVES.put(p, chaveAtual(p));
    }

    public void excluir(Plano p) throws SQLException {
        Object[] chave = CHAVES.get(p);
        if (chave == null) {
            return;
        }
        String sql = "DELETE FROM plano WHERE beneficios = ? AND valor_mensal = ? AND regras_vigencia = ?";
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, (String) chave[0]);
            ps.setFloat(2, (Float) chave[1]);
            ps.setString(3, (String) chave[2]);
            ps.executeUpdate();
        }
        CHAVES.remove(p);
    }

    public List<Plano> carregarTodos() throws SQLException {
        List<Plano> lista = new ArrayList<>();
        String sql = "SELECT beneficios, valor_mensal, regras_vigencia, disponivel FROM plano ORDER BY beneficios, valor_mensal";
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String beneficios = rs.getString("beneficios");
                float valor = rs.getFloat("valor_mensal");
                String regras = rs.getString("regras_vigencia");
                Plano p = Plano.criarPlano(beneficios, valor, regras);
                p.editarPlano(beneficios, valor, regras, rs.getBoolean("disponivel"));
                CHAVES.put(p, chaveAtual(p));
                lista.add(p);
            }
        }
        return lista;
    }

    public Plano buscarPorChave(String beneficios, float valorMensal, String regrasVigencia) {
        for (Map.Entry<Plano, Object[]> e : CHAVES.entrySet()) {
            Object[] k = e.getValue();
            if (k[0].equals(beneficios) && Float.compare((Float) k[1], valorMensal) == 0 && k[2].equals(regrasVigencia)) {
                return e.getKey();
            }
        }
        return null;
    }

    public int contarPlanos() throws SQLException {
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM plano");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Object[] chaveAtual(Plano p) {
        return new Object[]{p.getBeneficios(), p.getValorMensal(), p.getRegrasVigencia()};
    }
}