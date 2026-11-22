import java.util.Date; 
import java.util.ArrayList;
import java.util.List;

public abstract class Usuario {
    private static List<Usuario> usuarios = new ArrayList<>();

    private String nome;
    private Date dataNascimento;
    private String cpf;
    private String email;
    private String userName;
    private String senha;
    private boolean ativo;

    protected Usuario(String nome, Date dataNascimento, String cpf, String email, String userName, String senha) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
        this.email = email;
        this.userName = userName;
        this.senha = senha;
        this.ativo = true;
        usuarios.add(this);
    }

    public void verUsuario() {
        System.out.println("Nome: " + this.nome);
        System.out.println("Data de Nascimento: " + this.dataNascimento);
        System.out.println("CPF: " + this.cpf);
        System.out.println("Email: " + this.email);
        System.out.println("Username: " + this.userName);
        System.out.println("Ativo: " + (this.ativo ? "Sim" : "Não"));
    }

    public static void desativarUsuario(Usuario usuario) {
        if (usuario != null) {
            usuario.setAtivo(false);
        }    
    }

    public static boolean autenticar(String userName, String senha) {
        for (Usuario usuario : usuarios) {
            if (usuario.getUserName().equals(userName) && usuario.getSenha().equals(senha)) {
                return true;
            }
        }
        return false;
    }

    public void logout() {
        System.out.println("Sessão encerrada para o usuário: " + this.userName);   
    }

    public static List<Usuario> listarUsuarios() {
        for (Usuario usuario : usuarios) {
            usuario.verUsuario();
            System.out.println("------------------------");
        }
        return new ArrayList<>(usuarios);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Date getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(Date dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return "Usuario [nome=" + nome + ", dataNascimento=" + dataNascimento + ", cpf=" + cpf + ", email=" + email + ", userName=" + userName + ", ativo=" + ativo + "]";
    }

}