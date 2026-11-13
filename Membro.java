import java.util.ArrayList;
import java.util.List;
public class Membro extends Usuario {
   private static int numeroAtivos =0;
   private String endereco;
   private String telefone;
   private static List<Membro> listaDeMembros = new ArrayList<>();

    public Membro(String nome, int dataNascimento, String cpf, String email, String userName, String senha, boolean ativo, String endereco, String telefone) {
        super(nome, dataNascimento, cpf, email, userName, senha, ativo);
        this.endereco=endereco;
        this. telefone=telefone;
    }
    public void verMembro (){
        System.out.println (" - Dados do Membro - ");
        super.verUsuario();
        System.out.println ("Endereço: " + this.endereco);
        System.out.println ("Telefone: " + this.telefone);
    }
    public boolean realizarCheckin(){
        if(this.ativo){
            System.out.println ("Check-in realizado com sucesso. Membro: " + this.nome);
            return true;
        } else {
            System.out.println ("Falha no Check-in. Membro Suspenso/inativo");
            return false;
        }
    }
    public static Membro criarMembro (String nome, int dataNascimento, String cpf, String email, String userName, String senha, String endereco, String telefone){
        Membro novoMembro = new Membro(nome,dataNascimento,cpf,email,userName,senha,true, endereco,telefone);
        listaDeMembros.add(novoMembro);
        numeroAtivos++;
        System.out.println ("Membro criado com sucesso!");
        return novoMembro;
    }
    public static void editarMembro (Membro membro,String nome, int dataNascimento, String cpf, String email, String userName, String senha, boolean ativo, String endereco, String telefone){
        if (membro != null){
            membro.nome=nome;
            membro.dataNascimento=dataNascimento;
            membro.ativo=ativo;
            membro.endereco = endereco;
            membro.telefone = telefone;
            System.out.println ("Dados do membro editados com sucesso.");
        }
    }
    public static List<Membro> listarMembros(){
        return listaDeMembros;
    }
    public static List<Membro> listarMembrosSuspensos(){
        List<Membro> membrosSuspensos = new ArrayList<>();
        for (Membro m : listaDeMembros){
            if (!m.ativo){
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
}
