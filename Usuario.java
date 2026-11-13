import java.util.ArrayList;

public abstract class Usuario {
    public String nome;
    public int dataNascimento;
    protected String cpf;
    private String email;
    protected  String userName;
    protected  String senha;
    public boolean ativo;

    public Usuario(String nome, int dataNascimento, String cpf, String email, String userName, String senha, boolean ativo) {
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
        this.email = email;
        this.userName = userName;
        this.senha = senha;
        this.ativo = ativo;
    }

    public void verUsuario() {
        System.out.println("Nome: " + this.nome);
        System.out.println("Data de Nascimento: " + this.dataNascimento);
        System.out.println("CPF: " + this.cpf);
        System.out.println("Email: " + this.email);
        System.out.println("Username: " + this.userName);
        System.out.println("Ativo: " + this.ativo);
    }

    public void desativarUsuario() {
        this.ativo = false;
    }

    public boolean autenticar(String userName, String senha) {
        if (this.userName.equals(userName) && this.senha.equals(senha)) {
            return true;
        } else {
            return false;
        }
    }

    public void logout() {
        this.ativo = false;
    }

    public ArrayList<Usuario> listarUsuarios() {
        ArrayList<Usuario> usuarios = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            System.out.println("Nome: " + usuario.nome);
            System.out.println("Data de Nascimento: " + usuario.dataNascimento);
            System.out.println("CPF: " + usuario.cpf);
            System.out.println("Email: " + usuario.email);
            System.out.println("Username: " + usuario.userName);
            System.out.println("Ativo: " + usuario.ativo);
            System.out.println("------------------------");
        }
        return usuarios;
    }

    @Override
    public String toString() {
        return "Usuario [nome=" + nome + ", dataNascimento=" + dataNascimento + ", cpf=" + cpf + ", email=" + email
                + ", userName=" + userName + ", ativo=" + ativo + "]";
    }

}