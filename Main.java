import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

public class Main {

    private static final Scanner in = new Scanner(System.in);
    private static final SimpleDateFormat FORMATO = new SimpleDateFormat("dd/MM/yyyy");
    private static final long DIA = 86400000L;

    private static final FuncionarioDAO funcionarioDAO = new FuncionarioDAO();
    private static final MembroDAO membroDAO = new MembroDAO();
    private static final PlanoDAO planoDAO = new PlanoDAO();
    private static final AssinaturaDAO assinaturaDAO = new AssinaturaDAO();
    private static final AulaDAO aulaDAO = new AulaDAO();

    private interface AcaoBanco {
        void executar() throws SQLException;
    }

    private static void persistir(AcaoBanco acao) {
        try {
            acao.executar();
        } catch (SQLException e) {
            System.out.println("Erro ao gravar no banco de dados: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        FORMATO.setLenient(false);
        if (!carregar()) {
            return;
        }
        garantirGerenteInicial();
        System.out.println("=== SISTEMA DE GERENCIAMENTO DE ACADEMIA ===");
        boolean rodando = true;
        while (rodando) {
            System.out.println("\n1. Entrar");
            System.out.println("0. Sair");
            int op = lerInt("Opção: ");
            if (op == 1) {
                login();
            } else if (op == 0) {
                rodando = false;
            } else {
                System.out.println("Opção inválida.");
            }
        }
        System.out.println("Até logo!");
    }

    private static void garantirGerenteInicial() {
        if (Funcionario.listarFuncionarios().isEmpty()) {
            Funcionario admin = Funcionario.criarFuncionario("Administrador", data(1, 1, 1990), "000.000.000-00",
                    "admin@academia.com", "admin", "admin123", Cargo.GERENTE);
            persistir(() -> funcionarioDAO.salvar(admin));
            System.out.println("Primeiro acesso: gerente criado (username: admin / senha: admin123).");
        }
    }

    private static void login() {
        String userName = lerTexto("Username: ");
        String senha = lerTexto("Senha: ");
        Usuario usuario = buscarUsuario(userName);
        if (usuario == null) {
            System.out.println("Credenciais inválidas.");
            return;
        }
        if (!usuario.isAtivo()) {
            System.out.println("Acesso negado: conta desativada.");
            return;
        }
        if (!usuario.autenticar(userName, senha)) {
            System.out.println("Credenciais inválidas.");
            return;
        }
        if (usuario instanceof Membro) {
            Membro membro = (Membro) usuario;
            atualizarSituacoes();
            if (temAssinaturaVencida(membro) && Assinatura.listarAssinaturasAtivasPorMembro(membro).isEmpty()) {
                System.out.println("Acesso negado: assinatura vencida. Procure a recepção.");
                return;
            }
            System.out.println("\nBem-vindo(a), " + membro.getNome() + "!");
            menuMembro(membro);
        } else {
            Funcionario funcionario = (Funcionario) usuario;
            System.out.println("\nBem-vindo(a), " + funcionario.getNome() + " (" + funcionario.getCargo() + ")!");
            switch (funcionario.getCargo()) {
                case GERENTE:
                    menuGerente(funcionario);
                    break;
                case RECEPCIONISTA:
                    menuRecepcionista(funcionario);
                    break;
                case INSTRUTOR:
                    menuInstrutor(funcionario);
                    break;
                default:
                    System.out.println("Cargo não definido. Procure um gerente.");
            }
        }
        usuario.logout();
    }

    private static void menuGerente(Funcionario logado) {
        boolean sair = false;
        while (!sair) {
            System.out.println("\n--- Menu do Gerente ---");
            System.out.println("1. Cadastrar funcionário");
            System.out.println("2. Editar funcionário / atribuir cargo");
            System.out.println("3. Listar funcionários");
            System.out.println("4. Desativar / reativar funcionário");
            System.out.println("5. Editar membro");
            System.out.println("6. Listar membros");
            System.out.println("7. Criar plano");
            System.out.println("8. Editar plano / disponibilidade");
            System.out.println("9. Excluir plano");
            System.out.println("10. Listar planos");
            System.out.println("0. Logout");
            switch (lerInt("Opção: ")) {
                case 1: cadastrarFuncionario(); break;
                case 2: editarFuncionario(logado); break;
                case 3: listarFuncionarios(); break;
                case 4: alternarStatusFuncionario(logado); break;
                case 5: editarMembroDados(); break;
                case 6: listarMembros(); break;
                case 7: criarPlano(); break;
                case 8: editarPlano(); break;
                case 9: excluirPlano(); break;
                case 10: listarPlanos(); break;
                case 0: sair = true; break;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    private static void menuRecepcionista(Funcionario logado) {
        boolean sair = false;
        while (!sair) {
            System.out.println("\n--- Menu do Recepcionista ---");
            System.out.println("1. Cadastrar membro");
            System.out.println("2. Ver membro");
            System.out.println("3. Listar membros");
            System.out.println("4. Listar membros suspensos");
            System.out.println("5. Suspender membro");
            System.out.println("6. Reativar membro");
            System.out.println("7. Criar assinatura");
            System.out.println("8. Editar datas da assinatura");
            System.out.println("9. Registrar pagamento");
            System.out.println("10. Cancelar assinatura");
            System.out.println("11. Excluir assinatura");
            System.out.println("12. Listar assinaturas");
            System.out.println("13. Listar assinaturas ativas");
            System.out.println("14. Listar pagamentos atrasados");
            System.out.println("15. Atualizar situações das assinaturas");
            System.out.println("16. Inscrever membro em aula");
            System.out.println("17. Remover inscrição de aula");
            System.out.println("18. Listar aulas");
            System.out.println("19. Listar planos disponíveis");
            System.out.println("0. Logout");
            switch (lerInt("Opção: ")) {
                case 1: cadastrarMembro(); break;
                case 2: verMembro(); break;
                case 3: listarMembros(); break;
                case 4: listarMembrosSuspensos(); break;
                case 5: suspenderMembro(logado); break;
                case 6: reativarMembro(); break;
                case 7: criarAssinatura(); break;
                case 8: editarAssinatura(); break;
                case 9: registrarPagamento(); break;
                case 10: cancelarAssinaturaRecepcao(); break;
                case 11: excluirAssinatura(); break;
                case 12: listarAssinaturas(Assinatura.ListarAssinaturas()); break;
                case 13: listarAssinaturas(Assinatura.listarAssinaturasAtivas()); break;
                case 14: listarAssinaturas(Assinatura.listarPagamentoAtrasados()); break;
                case 15: atualizarSituacoes(); System.out.println("Situações atualizadas."); break;
                case 16: inscreverEmAula(); break;
                case 17: removerInscricao(); break;
                case 18: listarAulas(Aula.listarAulas()); break;
                case 19: listarPlanosDisponiveis(); break;
                case 0: sair = true; break;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    private static void menuInstrutor(Funcionario instrutor) {
        boolean sair = false;
        while (!sair) {
            System.out.println("\n--- Menu do Instrutor ---");
            System.out.println("1. Criar aula");
            System.out.println("2. Editar descrição de aula");
            System.out.println("3. Excluir aula");
            System.out.println("4. Listar minhas aulas");
            System.out.println("5. Ver inscritos de uma aula");
            System.out.println("6. Listar todos os membros inscritos em aulas");
            System.out.println("0. Logout");
            switch (lerInt("Opção: ")) {
                case 1: criarAula(instrutor); break;
                case 2: editarAula(instrutor); break;
                case 3: excluirAula(instrutor); break;
                case 4: listarAulas(aulasDoInstrutor(instrutor)); break;
                case 5: verInscritos(instrutor); break;
                case 6: listarTodosInscritos(); break;
                case 0: sair = true; break;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    private static void menuMembro(Membro membro) {
        boolean sair = false;
        while (!sair) {
            System.out.println("\n--- Menu do Membro ---");
            System.out.println("1. Realizar check-in");
            System.out.println("2. Minhas aulas");
            System.out.println("3. Minha assinatura e plano");
            System.out.println("4. Planos disponíveis");
            System.out.println("5. Cancelar minha assinatura atual");
            System.out.println("6. Solicitar exclusão do meu cadastro");
            System.out.println("0. Logout");
            switch (lerInt("Opção: ")) {
                case 1: realizarCheckIn(membro); break;
                case 2: minhasAulas(membro); break;
                case 3: minhaAssinatura(membro); break;
                case 4: listarPlanosDisponiveis(); break;
                case 5: cancelarMinhaAssinatura(membro); break;
                case 6: sair = solicitarExclusao(membro); break;
                case 0: sair = true; break;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    private static void cadastrarFuncionario() {
        String nome = lerTexto("Nome: ");
        Date nascimento = lerData("Data de nascimento (dd/MM/aaaa)", null);
        String cpf = lerTexto("CPF: ");
        if (cpfExiste(cpf)) {
            System.out.println("Já existe um usuário com esse CPF.");
            return;
        }
        String email = lerTexto("Email: ");
        String userName = lerTexto("Username: ");
        if (buscarUsuario(userName) != null) {
            System.out.println("Username já utilizado.");
            return;
        }
        String senha = lerTexto("Senha: ");
        Cargo cargo = lerCargo(null);
        Funcionario novo = Funcionario.criarFuncionario(nome, nascimento, cpf, email, userName, senha, cargo);
        persistir(() -> funcionarioDAO.salvar(novo));
        System.out.println("Funcionário cadastrado com sucesso.");
    }

    private static void editarFuncionario(Funcionario logado) {
        Funcionario f = escolher(Funcionario.listarFuncionarios(),
                x -> x.getNome() + " - " + x.getCargo() + (x.isAtivo() ? "" : " (desativado)"));
        if (f == null) {
            return;
        }
        final String cpfAnterior = f.getCpf();
        String nome = lerOuManter("Nome", f.getNome());
        Date nascimento = lerData("Data de nascimento", f.getDataNascimento());
        String cpf = lerOuManter("CPF", f.getCpf());
        String email = lerOuManter("Email", f.getEmail());
        String userName = lerOuManter("Username", f.getUserName());
        System.out.print("Nova senha [Enter mantém a atual]: ");
        String novaSenha = in.nextLine().trim();
        String senha = novaSenha.isEmpty() ? f.getSenha() : limpar(novaSenha);
        Cargo cargo = lerCargo(f.getCargo());
        if (!cpf.equals(f.getCpf()) && cpfExiste(cpf)) {
            System.out.println("Já existe um usuário com esse CPF.");
            return;
        }
        if (!userName.equals(f.getUserName()) && buscarUsuario(userName) != null) {
            System.out.println("Username já utilizado.");
            return;
        }
        if (f == logado && cargo != Cargo.GERENTE) {
            System.out.println("Você não pode remover o seu próprio cargo de gerente.");
            return;
        }
        Funcionario.editarFuncionario(f, nome, nascimento, cpf, email, userName, senha, f.isAtivo(), cargo);
        persistir(() -> funcionarioDAO.atualizarPorCpf(cpfAnterior, f));
        System.out.println("Funcionário editado com sucesso.");
    }

    private static void listarFuncionarios() {
        System.out.println("\n--- Funcionários ---");
        for (Funcionario f : Funcionario.listarFuncionarios()) {
            f.verFuncionario();
            System.out.println("----------------------");
        }
        System.out.println("Total: " + Funcionario.getNumeroTotal());
    }

    private static void alternarStatusFuncionario(Funcionario logado) {
        Funcionario f = escolher(Funcionario.listarFuncionarios(),
                x -> x.getNome() + " - " + x.getCargo() + (x.isAtivo() ? " (ativo)" : " (desativado)"));
        if (f == null) {
            return;
        }
        if (f == logado) {
            System.out.println("Você não pode desativar a própria conta.");
            return;
        }
        if (f.isAtivo()) {
            logado.desativarUsuario(f);
            System.out.println("Funcionário desativado.");
        } else {
            f.setAtivo(true);
            System.out.println("Funcionário reativado.");
        }
        persistir(() -> funcionarioDAO.atualizarPorCpf(f.getCpf(), f));
    }

    private static void editarMembroDados() {
        Membro m = escolherMembro(Membro.listarMembros());
        if (m == null) {
            return;
        }
        final String cpfAnterior = m.getCpf();
        String nome = lerOuManter("Nome", m.getNome());
        Date nascimento = lerData("Data de nascimento", m.getDataNascimento());
        String cpf = lerOuManter("CPF", m.getCpf());
        String email = lerOuManter("Email", m.getEmail());
        String userName = lerOuManter("Username", m.getUserName());
        System.out.print("Nova senha [Enter mantém a atual]: ");
        String novaSenha = in.nextLine().trim();
        String senha = novaSenha.isEmpty() ? m.getSenha() : limpar(novaSenha);
        String endereco = lerOuManter("Endereço", m.getEndereco());
        String telefone = lerOuManter("Telefone", m.getTelefone());
        if (!cpf.equals(m.getCpf()) && cpfExiste(cpf)) {
            System.out.println("Já existe um usuário com esse CPF.");
            return;
        }
        if (!userName.equals(m.getUserName()) && buscarUsuario(userName) != null) {
            System.out.println("Username já utilizado.");
            return;
        }
        Membro.editarMembro(m, nome, nascimento, cpf, email, userName, senha, m.isAtivo(), endereco, telefone);
        persistir(() -> membroDAO.atualizarPorCpf(cpfAnterior, m));
    }

    private static void criarPlano() {
        String beneficios = lerTexto("Benefícios: ");
        float valor = lerFloat("Valor mensal: ");
        String regras = lerTexto("Regras de vigência: ");
        if (planoDuplicado(beneficios, valor, regras, null)) {
            System.out.println("Já existe um plano com esses mesmos dados.");
            return;
        }
        Plano novo = Plano.criarPlano(beneficios, valor, regras);
        persistir(() -> planoDAO.salvar(novo));
        System.out.println("Plano criado com sucesso.");
    }

    private static void editarPlano() {
        Plano p = escolher(Plano.listarPlanos(), Main::descPlano);
        if (p == null) {
            return;
        }
        String beneficios = lerOuManter("Benefícios", p.getBeneficios());
        System.out.print("Valor mensal [" + p.getValorMensal() + "]: ");
        String textoValor = in.nextLine().trim();
        float valor = p.getValorMensal();
        if (!textoValor.isEmpty()) {
            try {
                valor = Float.parseFloat(textoValor.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido, mantendo o atual.");
            }
        }
        String regras = lerOuManter("Regras de vigência", p.getRegrasVigencia());
        boolean disponivel = lerSimNao("Disponível?", p.isDisponivel());
        if (planoDuplicado(beneficios, valor, regras, p)) {
            System.out.println("Já existe um plano com esses mesmos dados.");
            return;
        }
        p.editarPlano(beneficios, valor, regras, disponivel);
        persistir(() -> planoDAO.atualizar(p));
        System.out.println("Plano editado com sucesso.");
    }

    private static void excluirPlano() {
        Plano p = escolher(Plano.listarPlanos(), Main::descPlano);
        if (p == null) {
            return;
        }
        for (Assinatura a : Assinatura.ListarAssinaturas()) {
            if (a.getPlano() == p) {
                System.out.println("Não é possível excluir: existem assinaturas vinculadas a este plano. Marque-o como indisponível.");
                return;
            }
        }
        Plano.excluirPlano(p);
        persistir(() -> planoDAO.excluir(p));
        System.out.println("Plano excluído.");
    }

    private static void listarPlanos() {
        System.out.println("\n--- Planos (" + Plano.getNumPlanos() + ") ---");
        int i = 1;
        for (Plano p : Plano.listarPlanos()) {
            System.out.println("Plano " + i++);
            p.verPlano();
            System.out.println("----------------------");
        }
    }

    private static void listarPlanosDisponiveis() {
        System.out.println("\n--- Planos disponíveis ---");
        int i = 1;
        for (Plano p : Plano.listarPlanosAtivo()) {
            System.out.println("Plano " + i++);
            p.verPlano();
            System.out.println("----------------------");
        }
    }

    private static void cadastrarMembro() {
        String nome = lerTexto("Nome: ");
        Date nascimento = lerData("Data de nascimento (dd/MM/aaaa)", null);
        String cpf = lerTexto("CPF: ");
        if (cpfExiste(cpf)) {
            System.out.println("Já existe um usuário com esse CPF.");
            return;
        }
        String email = lerTexto("Email: ");
        String userName = lerTexto("Username: ");
        if (buscarUsuario(userName) != null) {
            System.out.println("Username já utilizado.");
            return;
        }
        String senha = lerTexto("Senha: ");
        String endereco = lerTexto("Endereço: ");
        String telefone = lerTexto("Telefone: ");
        Membro novo = Membro.criarMembro(nome, nascimento, cpf, email, userName, senha, endereco, telefone);
        persistir(() -> membroDAO.salvar(novo));
    }

    private static void verMembro() {
        Membro m = escolherMembro(Membro.listarMembros());
        if (m != null) {
            m.verMembro();
        }
    }

    private static void listarMembros() {
        System.out.println("\n--- Membros ---");
        for (Membro m : Membro.listarMembros()) {
            m.verMembro();
            System.out.println("----------------------");
        }
        System.out.println("Total: " + Membro.listarMembros().size());
    }

    private static void listarMembrosSuspensos() {
        System.out.println("\n--- Membros suspensos ---");
        for (Membro m : Membro.listarMembrosSuspensos()) {
            m.verMembro();
            System.out.println("----------------------");
        }
        System.out.println("Total: " + Membro.listarMembrosSuspensos().size());
    }

    private static void suspenderMembro(Funcionario logado) {
        List<Membro> ativos = new ArrayList<>();
        for (Membro m : Membro.listarMembros()) {
            if (m.isAtivo()) {
                ativos.add(m);
            }
        }
        Membro m = escolherMembro(ativos);
        if (m != null) {
            logado.desativarUsuario(m);
            persistir(() -> membroDAO.atualizarPorCpf(m.getCpf(), m));
            System.out.println("Membro suspenso.");
        }
    }

    private static void reativarMembro() {
        Membro m = escolherMembro(Membro.listarMembrosSuspensos());
        if (m != null) {
            m.setAtivo(true);
            persistir(() -> membroDAO.atualizarPorCpf(m.getCpf(), m));
            System.out.println("Membro reativado.");
        }
    }

    private static void criarAssinatura() {
        List<Membro> ativos = new ArrayList<>();
        for (Membro m : Membro.listarMembros()) {
            if (m.isAtivo()) {
                ativos.add(m);
            }
        }
        Membro membro = escolherMembro(ativos);
        if (membro == null) {
            return;
        }
        if (!Assinatura.listarAssinaturasAtivasPorMembro(membro).isEmpty()) {
            System.out.println("O membro já possui uma assinatura ativa. Cancele-a antes de trocar de plano.");
            return;
        }
        System.out.println("Escolha o plano:");
        Plano plano = escolher(Plano.listarPlanosAtivo(), Main::descPlano);
        if (plano == null) {
            return;
        }
        Date hoje = new Date();
        Date pagamento = lerData("Data limite de pagamento", new Date(hoje.getTime() + 30 * DIA));
        Date termino = lerData("Data de término", new Date(hoje.getTime() + 365 * DIA));
        Assinatura nova = Assinatura.criarAssinatura(plano, membro, pagamento, termino);
        persistir(() -> assinaturaDAO.salvar(nova));
    }

    private static void editarAssinatura() {
        Assinatura a = escolher(Assinatura.ListarAssinaturas(), Main::descAssinatura);
        if (a == null) {
            return;
        }
        Date pagamento = lerData("Data limite de pagamento", a.getDataPagamento());
        Date termino = lerData("Data de término", a.getDataTermino());
        Assinatura.editarAssinatura(a, a.getDataAssinatura(), pagamento, termino,
                a.getDataPagamentoReal(), a.isPago(), a.getSituacao());
        persistir(() -> assinaturaDAO.atualizar(a));
    }

    private static void registrarPagamento() {
        List<Assinatura> pendentes = new ArrayList<>();
        for (Assinatura a : Assinatura.ListarAssinaturas()) {
            if (!a.isPago() && a.getSituacao() != Situacao.CANCELADA) {
                pendentes.add(a);
            }
        }
        Assinatura a = escolher(pendentes, Main::descAssinatura);
        if (a != null) {
            a.registrarPagamento();
            persistir(() -> assinaturaDAO.registrarPagamento(a));
        }
    }

    private static void cancelarAssinaturaRecepcao() {
        List<Assinatura> naoCanceladas = new ArrayList<>();
        for (Assinatura a : Assinatura.ListarAssinaturas()) {
            if (a.getSituacao() != Situacao.CANCELADA) {
                naoCanceladas.add(a);
            }
        }
        Assinatura a = escolher(naoCanceladas, Main::descAssinatura);
        if (a != null && lerSimNao("Confirmar cancelamento?", false)) {
            cancelar(a);
        }
    }

    private static void excluirAssinatura() {
        Assinatura a = escolher(Assinatura.ListarAssinaturas(), Main::descAssinatura);
        if (a != null && lerSimNao("Confirmar exclusão?", false)) {
            Assinatura.excluirAssinatura(a);
            persistir(() -> assinaturaDAO.excluir(a));
        }
    }

    private static void listarAssinaturas(List<Assinatura> lista) {
        System.out.println("\n--- Assinaturas ---");
        for (Assinatura a : lista) {
            a.verAssinatura();
            System.out.println("----------------------");
        }
        System.out.println("Total: " + lista.size());
    }

    private static void cancelar(Assinatura a) {
        Assinatura.editarAssinatura(a, a.getDataAssinatura(), a.getDataPagamento(), new Date(),
                a.getDataPagamentoReal(), a.isPago(), Situacao.CANCELADA);
        persistir(() -> assinaturaDAO.cancelarAssinatura(a));
        System.out.println("Assinatura cancelada.");
    }

    private static void inscreverEmAula() {
        Aula aula = escolher(Aula.listarAulas(), Main::descAula);
        if (aula == null) {
            return;
        }
        List<Membro> ativos = new ArrayList<>();
        for (Membro m : Membro.listarMembros()) {
            if (m.isAtivo() && !aula.getAlunos().contains(m)) {
                ativos.add(m);
            }
        }
        Membro membro = escolherMembro(ativos);
        if (membro == null) {
            return;
        }
        if (Assinatura.listarAssinaturasAtivasPorMembro(membro).isEmpty()) {
            System.out.println("O membro precisa de uma assinatura ativa para se inscrever em aulas.");
            return;
        }
        List<Membro> alunos = aula.getAlunos();
        alunos.add(membro);
        Aula.editarAula(aula.getHorario(), aula.getDescricao(), aula.getInstrutor(), alunos);
        persistir(() -> aulaDAO.atualizar(aula));
        System.out.println("Inscrição realizada.");
    }

    private static void removerInscricao() {
        Aula aula = escolher(Aula.listarAulas(), Main::descAula);
        if (aula == null) {
            return;
        }
        Membro membro = escolherMembro(aula.getAlunos());
        if (membro == null) {
            return;
        }
        List<Membro> alunos = aula.getAlunos();
        alunos.remove(membro);
        Aula.editarAula(aula.getHorario(), aula.getDescricao(), aula.getInstrutor(), alunos);
        persistir(() -> aulaDAO.atualizar(aula));
        System.out.println("Inscrição removida.");
    }

    private static void listarAulas(List<Aula> lista) {
        System.out.println("\n--- Aulas ---");
        for (Aula a : lista) {
            a.verAula();
            System.out.println("----------------------");
        }
        System.out.println("Total: " + lista.size());
    }

    private static List<Aula> aulasDoInstrutor(Funcionario instrutor) {
        List<Aula> minhas = new ArrayList<>();
        for (Aula a : Aula.listarAulas()) {
            if (a.getInstrutor() == instrutor) {
                minhas.add(a);
            }
        }
        return minhas;
    }

    private static void criarAula(Funcionario instrutor) {
        String horario = lerTexto("Horário (ex: Segunda 18h): ");
        for (Aula a : Aula.listarAulas()) {
            if (a.getHorario().equalsIgnoreCase(horario)) {
                System.out.println("Já existe uma aula nesse horário.");
                return;
            }
        }
        String descricao = lerTexto("Descrição: ");
        Aula nova = Aula.criarAula(horario, descricao, instrutor, new ArrayList<>());
        persistir(() -> aulaDAO.salvar(nova));
        System.out.println("Aula criada com sucesso.");
    }

    private static void editarAula(Funcionario instrutor) {
        Aula aula = escolher(aulasDoInstrutor(instrutor), Main::descAula);
        if (aula == null) {
            return;
        }
        String descricao = lerOuManter("Descrição", aula.getDescricao());
        Aula.editarAula(aula.getHorario(), descricao, instrutor, aula.getAlunos());
        persistir(() -> aulaDAO.atualizar(aula));
        System.out.println("Aula editada com sucesso.");
    }

    private static void excluirAula(Funcionario instrutor) {
        Aula aula = escolher(aulasDoInstrutor(instrutor), Main::descAula);
        if (aula != null && lerSimNao("Confirmar exclusão?", false)) {
            Aula.excluirAula(aula);
            persistir(() -> aulaDAO.excluir(aula));
            System.out.println("Aula excluída.");
        }
    }

    private static void verInscritos(Funcionario instrutor) {
        Aula aula = escolher(aulasDoInstrutor(instrutor), Main::descAula);
        if (aula == null) {
            return;
        }
        aula.verAula();
        for (Membro m : aula.getAlunos()) {
            System.out.println(" - " + m.getNome());
        }
    }

    private static void listarTodosInscritos() {
        System.out.println("\n--- Membros inscritos em aulas ---");
        for (Membro m : Aula.listarInscritos()) {
            System.out.println(" - " + m.getNome());
        }
    }

    private static void realizarCheckIn(Membro membro) {
        atualizarSituacoes();
        if (Assinatura.listarAssinaturasAtivasPorMembro(membro).isEmpty()) {
            System.out.println("Acesso negado: assinatura inexistente ou vencida.");
            return;
        }
        membro.realizarCheckin();
    }

    private static void minhasAulas(Membro membro) {
        System.out.println("\n--- Minhas aulas ---");
        boolean encontrou = false;
        for (Aula a : Aula.listarAulas()) {
            if (a.getAlunos().contains(membro)) {
                System.out.println(a.getHorario() + " - " + a.getDescricao() + " (Instrutor: " + a.getInstrutor().getNome() + ")");
                encontrou = true;
            }
        }
        if (!encontrou) {
            System.out.println("Você não está inscrito em nenhuma aula.");
        }
    }

    private static void minhaAssinatura(Membro membro) {
        atualizarSituacoes();
        List<Assinatura> ativas = Assinatura.listarAssinaturasAtivasPorMembro(membro);
        if (ativas.isEmpty()) {
            System.out.println("Você não possui assinatura ativa.");
            return;
        }
        for (Assinatura a : ativas) {
            a.verAssinatura();
            System.out.println("- Plano -");
            a.getPlano().verPlano();
        }
    }

    private static void cancelarMinhaAssinatura(Membro membro) {
        Assinatura a = escolher(Assinatura.listarAssinaturasAtivasPorMembro(membro), Main::descAssinatura);
        if (a != null && lerSimNao("Confirmar cancelamento?", false)) {
            cancelar(a);
        }
    }

    private static boolean solicitarExclusao(Membro membro) {
        if (!lerSimNao("Isso cancelará suas assinaturas e desativará sua conta. Confirmar?", false)) {
            return false;
        }
        for (Assinatura a : Assinatura.listarAssinaturasAtivasPorMembro(membro)) {
            cancelar(a);
        }
        membro.desativarUsuario(membro);
        persistir(() -> membroDAO.atualizarPorCpf(membro.getCpf(), membro));
        System.out.println("Cadastro desativado.");
        return true;
    }

    private static boolean temAssinaturaVencida(Membro membro) {
        for (Assinatura a : Assinatura.ListarAssinaturas()) {
            if (a.getMembro() == membro && a.getSituacao() == Situacao.VENCIDA) {
                return true;
            }
        }
        return false;
    }
}
