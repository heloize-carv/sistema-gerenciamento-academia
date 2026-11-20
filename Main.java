import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== SISTEMA DE GERENCIAMENTO DE ACADEMIA ===\n");

        Funcionario instrutor = gerenciarFuncionarios();
        Plano plano = gerenciarPlanos();
        Membro membro = gerenciarMembros();
        gerenciarAssinaturas(plano, membro);
        gerenciarAulas(instrutor, membro);
        testarAcesso(membro);
    }

    private static Funcionario gerenciarFuncionarios() {
        System.out.println("--- Funcionários ---");
        Funcionario.criarFuncionario("Carlos Mendes", data(15, 4, 1985), "111.222.333-44",
                "carlos@academia.com", "carlos.gerente", "admin123", Cargo.GERENTE);
        Funcionario.criarFuncionario("Ana Souza", data(22, 8, 1995), "222.333.444-55",
                "ana@academia.com", "ana.recep", "recep123", Cargo.RECEPCIONISTA);
        Funcionario instrutor = Funcionario.criarFuncionario("Rafael Costa", data(10, 11, 1990),
                "333.444.555-66", "rafael@academia.com", "rafael.inst", "inst123", Cargo.INSTRUTOR);

        for (Funcionario f : Funcionario.listarFuncionarios()) {
            f.verFuncionario();
        }
        System.out.println();
        return instrutor;
    }

    private static Plano gerenciarPlanos() {
        System.out.println("--- Planos ---");
        Plano basico = Plano.criarPlano("Musculação e Cárdio", 99.90f, "Mensal sem fidelidade");
        Plano black = Plano.criarPlano("Acesso total + Aulas coletivas", 149.90f, "Anual");

        black.editarPlano("Acesso total + Aulas coletivas + Avaliação Física", 159.90f, "Fidelidade 12 meses", true);
        basico.editarPlano("Musculação e Cárdio", 99.90f, "Mensal sem fidelidade", false);

        System.out.println("Planos cadastrados: " + Plano.getNumPlanos());
        for (Plano p : Plano.listarPlanosAtivo()) {
            p.verPlano();
        }
        System.out.println();
        return black;
    }

    private static Membro gerenciarMembros() {
        System.out.println("--- Membros ---");
        Membro membro = Membro.criarMembro("Lucas Oliveira", data(5, 3, 2000), "444.555.666-77",
                "lucas@email.com", "lucas.aluno", "123456", "Rua das Flores, 120", "(84) 99999-1111");
        membro.verMembro();
        System.out.println();
        return membro;
    }

    private static void gerenciarAssinaturas(Plano plano, Membro membro) {
        System.out.println("--- Assinaturas ---");
        Assinatura assinatura = Assinatura.criarAssinatura(plano, membro, daqui(-5), daqui(365));
        Assinatura.atualizarSituacoes();
        System.out.println("Pagamentos atrasados: " + Assinatura.listarPagamentoAtrasados().size());
        assinatura.verAssinatura();
        assinatura.registrarPagamento();
        assinatura.verAssinatura();
        System.out.println();
    }

    private static void gerenciarAulas(Funcionario instrutor, Membro membro) {
        System.out.println("--- Aulas ---");
        List<Membro> alunos = new ArrayList<>();
        alunos.add(membro);

        Aula aula = Aula.criarAula("Segunda 18h", "Treino funcional", instrutor, alunos);
        aula.verAula();
        System.out.println("Aulas cadastradas: " + Aula.listarAulas().size());
        System.out.println();
    }

    private static void testarAcesso(Membro membro) {
        System.out.println("--- Acesso ---");
        System.out.println("Login correto: " + membro.autenticar("lucas.aluno", "123456"));
        System.out.println("Login com senha errada: " + membro.autenticar("lucas.aluno", "000000"));
        membro.desativarUsuario(membro);
        System.out.println("Login após suspensão: " + membro.autenticar("lucas.aluno", "123456"));
        System.out.println("Membros suspensos: " + Membro.listarMembrosSuspensos().size());
    }

    private static Date data(int dia, int mes, int ano) {
        return new GregorianCalendar(ano, mes - 1, dia).getTime();
    }

    private static Date daqui(int dias) {
        return new Date(System.currentTimeMillis() + dias * 86400000L);
    }
}
