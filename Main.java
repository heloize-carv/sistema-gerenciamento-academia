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
}
