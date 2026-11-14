import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Funcionario extends Usuario {
  
    private static int numeroTotal = 0;
    private Cargo cargo;
    private static List<Funcionario> listaFuncionarios = new ArrayList<>();

    public Funcionario(String nome, Date dataNascimento, String cpf, String email, String userName, String senha, Cargo cargo) {
        super(nome, dataNascimento, cpf, email, userName, senha);
        this.cargo = cargo;
        numeroTotal++;
        listaFuncionarios.add(this);
    }

    public void verFuncionario() {
        System.out.println("--- Dados do Funcionário ---");
        super.verUsuario();
        System.out.println("Cargo: " + (cargo != null ? cargo : "Não definido"));
    }

    public static Funcionario criarFuncionario(String nome, Date dataNascimento, String cpf,
                                               String email, String userName, String senha, Cargo cargo) {
        return new Funcionario(nome, dataNascimento, cpf, email, userName, senha, cargo);
    }

    public static void editarFuncionario(Funcionario funcionario, String nome, Date dataNascimento,
                                         String cpf, String email, String userName, String senha,
                                         boolean ativo, Cargo cargo) {
        if (funcionario != null) {
            funcionario.setNome(nome);
            funcionario.setDataNascimento(dataNascimento);
            funcionario.setCpf(cpf);
            funcionario.setEmail(email);
            funcionario.setUserName(userName);
            funcionario.setSenha(senha);
            funcionario.setAtivo(ativo);
            funcionario.setCargo(cargo);
        }
    }

    public static List<Funcionario> listarFuncionarios() {
        return listaFuncionarios;
    }

    public static int getNumeroTotal() {
        return numeroTotal;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }
}
