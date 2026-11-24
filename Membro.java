import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class Membro extends Usuario {
   private static int numeroAtivos =0;
   private static List<Membro> listaDeMembros = new ArrayList<>();
   private String endereco;
   private String telefone;

    public Membro(String nome, Date dataNascimento, String cpf, String email, String userName, String senha, String endereco, String telefone) {
        super(nome, dataNascimento, cpf, email, userName, senha);
        this.endereco=endereco;
        this.telefone=telefone;
    }
    public void verMembro (){
        System.out.println (" - Dados do Membro - ");
        super.verUsuario();
        System.out.println ("Endereço: " + this.endereco);
        System.out.println ("Telefone: " + this.telefone);
    }
    public boolean realizarCheckin(){
        if(this.isAtivo()){
            System.out.println ("Check-in realizado com sucesso. Membro: " + this.getNome());
            return true;
        } else {
            System.out.println ("Falha no Check-in: Membro inativo");
            return false;
        }
    }
    public static Membro criarMembro (String nome, Date dataNascimento, String cpf, String email, String userName, String senha, String endereco, String telefone){
        Membro novoMembro = new Membro(nome,dataNascimento,cpf,email,userName,senha, endereco,telefone);
        listaDeMembros.add(novoMembro);
        numeroAtivos++;
        System.out.println ("Membro criado com sucesso!");
        return novoMembro;
    }
    public static void editarMembro (Membro membro,String nome, Date dataNascimento, String cpf, String email, String userName, String senha, boolean ativo, String endereco, String telefone){
        if (membro != null){
            membro.setNome(nome);
            membro.setDataNascimento(dataNascimento);
            membro.setCpf(cpf);
            membro.setEmail(email);
            membro.setUserName(userName);
            membro.setSenha(senha);
            membro.setAtivo(ativo);
            membro.setEndereco(endereco);
            membro.setTelefone(telefone);
            System.out.println ("Dados do membro editados com sucesso.");
        }
    }
    public static List<Membro> listarMembros(){
        return listaDeMembros;
    }
    public static List<Membro> listarMembrosSuspensos(){
        List<Membro> membrosSuspensos = new ArrayList<>();
        for (Membro m : listaDeMembros){
            if (!m.isAtivo()){
                membrosSuspensos.add(m);
            }
        }
        return membrosSuspensos;
    }
    public String getEndereco() {
        return endereco;
    }
    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
    public String getTelefone() {
        return telefone;
    }
    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
    public static int getNumeroAtivos(){return numeroAtivos;}
    @Override
    public String toString() {
        return "Membro [" + super.toString() + ", endereco=" + endereco + ", telefone=" + telefone + "]";
    }
}
