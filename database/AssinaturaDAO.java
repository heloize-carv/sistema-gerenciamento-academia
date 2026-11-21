import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class AssinaturaDAO {

    private static final Map<Assinatura, Timestamp> CHAVES = new IdentityHashMap<>();
    private final PlanoDAO planoDAO = new PlanoDAO();

    public void salvar(Assinatura a) throws SQLException {
        String sql = """
            INSERT INTO assinatura (membro_cpf, plano_beneficios, plano_valor_mensal, plano_regras_vigencia,
                                    data_assinatura, data_pagamento, data_termino, pago, data_pagamento_real, situacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        Timestamp dataAssinatura = new Timestamp(a.getDataAssinatura().getTime());
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, a.getMembro().getCpf());
            ps.setString(2, a.getPlano().getBeneficios());
            ps.setFloat(3, a.getPlano().getValorMensal());
            ps.setString(4, a.getPlano().getRegrasVigencia());
            ps.setTimestamp(5, dataAssinatura);
            ps.setDate(6, new java.sql.Date(a.getDataPagamento().getTime()));
            definirData(ps, 7, a.getDataTermino());
            ps.setBoolean(8, a.isPago());
            definirData(ps, 9, a.getDataPagamentoReal());
            ps.setString(10, a.getSituacao().name());
            ps.executeUpdate();
        }
        CHAVES.put(a, dataAssinatura);
    }

    public void atualizar(Assinatura a) throws SQLException {
        Timestamp chave = chaveObrigatoria(a);
        String sql = """
            UPDATE assinatura
            SET data_assinatura = ?, data_pagamento = ?, data_termino = ?, data_pagamento_real = ?,
                pago = ?, situacao = ?
            WHERE membro_cpf = ? AND data_assinatura = ?
        """;
        Timestamp nova = new Timestamp(a.getDataAssinatura().getTime());
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, nova);
            ps.setDate(2, new java.sql.Date(a.getDataPagamento().getTime()));
            definirData(ps, 3, a.getDataTermino());
            definirData(ps, 4, a.getDataPagamentoReal());
            ps.setBoolean(5, a.isPago());
            ps.setString(6, a.getSituacao().name());
            ps.setString(7, a.getMembro().getCpf());
            ps.setTimestamp(8, chave);
            ps.executeUpdate();
        }
        CHAVES.put(a, nova);
    }

    public void registrarPagamento(Assinatura a) throws SQLException {
        Timestamp chave = chaveObrigatoria(a);
        String sql = """
            UPDATE assinatura
            SET pago = TRUE,
                data_pagamento_real = CURRENT_DATE,
                situacao = CASE WHEN situacao = 'VENCIDA' THEN 'ATIVA' ELSE situacao END
            WHERE membro_cpf = ? AND data_assinatura = ?
        """;
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getMembro().getCpf());
            ps.setTimestamp(2, chave);
            ps.executeUpdate();
        }
    }

    public void cancelarAssinatura(Assinatura a) throws SQLException {
        Timestamp chave = chaveObrigatoria(a);
        String sql = """
            UPDATE assinatura SET situacao = ?, data_termino = CURRENT_DATE
            WHERE membro_cpf = ? AND data_assinatura = ?
        """;
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Situacao.CANCELADA.name());
            ps.setString(2, a.getMembro().getCpf());
            ps.setTimestamp(3, chave);
            ps.executeUpdate();
        }
    }

    public void excluir(Assinatura a) throws SQLException {
        Timestamp chave = CHAVES.get(a);
        if (chave == null) {
            return;
        }
        String sql = "DELETE FROM assinatura WHERE membro_cpf = ? AND data_assinatura = ?";
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getMembro().getCpf());
            ps.setTimestamp(2, chave);
            ps.executeUpdate();
        }
        CHAVES.remove(a);
    }

    public List<Assinatura> carregarTodas() throws SQLException {
        List<Assinatura> lista = new ArrayList<>();
        String sql = "SELECT * FROM assinatura ORDER BY data_assinatura";
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Plano plano = planoDAO.buscarPorChave(rs.getString("plano_beneficios"),
                        rs.getFloat("plano_valor_mensal"), rs.getString("plano_regras_vigencia"));
                Membro membro = MembroDAO.buscarEmMemoria(rs.getString("membro_cpf"));
                if (plano == null || membro == null) {
                    continue;
                }
                Timestamp dataAssinatura = rs.getTimestamp("data_assinatura");
                Date dataPagamento = paraData(rs.getDate("data_pagamento"));
                Date dataTermino = paraData(rs.getDate("data_termino"));
                Assinatura a = Assinatura.criarAssinatura(plano, membro, dataPagamento, dataTermino);
                Assinatura.editarAssinatura(a, new Date(dataAssinatura.getTime()), dataPagamento, dataTermino,
                        paraData(rs.getDate("data_pagamento_real")), rs.getBoolean("pago"),
                        Situacao.valueOf(rs.getString("situacao")));
                CHAVES.put(a, dataAssinatura);
                lista.add(a);
            }
        }
        return lista;
    }

    public int atualizarSituacoesVencidas() throws SQLException {
        String sql = """
            UPDATE assinatura
            SET situacao = ?
            WHERE situacao = ? AND pago = FALSE AND data_pagamento < CURRENT_DATE
        """;
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Situacao.VENCIDA.name());
            ps.setString(2, Situacao.ATIVA.name());
            return ps.executeUpdate();
        }
    }

    public int contarAssinaturasAtivas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM assinatura WHERE situacao = 'ATIVA'";
        try (Connection conn = ConexaoSupabase.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Timestamp chaveObrigatoria(Assinatura a) throws SQLException {
        Timestamp chave = CHAVES.get(a);
        if (chave == null) {
            throw new SQLException("Assinatura não sincronizada com o banco.");
        }
        return chave;
    }

    private void definirData(PreparedStatement ps, int indice, Date data) throws SQLException {
        if (data != null) {
            ps.setDate(indice, new java.sql.Date(data.getTime()));
        } else {
            ps.setNull(indice, Types.DATE);
        }
    }

    private Date paraData(java.sql.Date data) {
        return data == null ? null : new Date(data.getTime());
    }
}