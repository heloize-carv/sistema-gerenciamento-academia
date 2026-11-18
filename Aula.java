import java.util.ArrayList;
import java.util.List;

public class Aula {

    private static int totalAulas = 0;
    private String horario;
    private String descricao;

    private Funcionario instrutor;
    private List<Membro> alunos;

    private static List<Aula> listaAulas = new ArrayList<>();

    public Aula(String horario, String descricao, Funcionario instrutor, List<Membro> alunos) {
        if (instrutor == null || instrutor.getCargo() != Cargo.INSTRUTOR) {
            throw new IllegalArgumentException("A aula deve ser ministrada por um funcionário com cargo de INSTRUTOR.");
        }
        this.horario = horario;
        this.descricao = descricao;
        this.instrutor = instrutor;
        this.alunos = (alunos != null) ? new ArrayList<>(alunos) : new ArrayList<>();
        totalAulas++;
        listaAulas.add(this);
    }

    public void verAula() {
        System.out.println("--- Dados da Aula ---");
        System.out.println("Horário: " + horario);
        System.out.println("Descrição: " + descricao);
        System.out.println("Instrutor: " + instrutor.getNome());
        System.out.println("Quantidade de inscritos: " + alunos.size());
    }

    public static Aula criarAula(String horario, String descricao, Funcionario instrutor, List<Membro> alunos) {
        return new Aula(horario, descricao, instrutor, alunos);
    }

    public static void editarAula(String horario, String descricao, Funcionario instrutor, List<Membro> alunos) {
        if (instrutor == null || instrutor.getCargo() != Cargo.INSTRUTOR) {
            throw new IllegalArgumentException("A aula deve ser ministrada por um funcionário com cargo de INSTRUTOR.");
        }
        for (Aula aula : listaAulas) {
            if (aula.horario != null && aula.horario.equals(horario)) {
                aula.descricao = descricao;
                aula.instrutor = instrutor;
                aula.alunos = (alunos != null) ? new ArrayList<>(alunos) : new ArrayList<>();
                return;
            }
        }
    }

    public static void excluirAula(Aula aula) {
        if (aula != null && listaAulas.remove(aula)) {
            totalAulas--;
        }
    }

    public static List<Aula> listarAulas() {
        return new ArrayList<>(listaAulas);
    }

    public static List<Membro> listarInscritos() {
        List<Membro> inscritos = new ArrayList<>();
        for (Aula aula : listaAulas) {
            for (Membro m : aula.alunos) {
                if (!inscritos.contains(m)) {
                    inscritos.add(m);
                }
            }
        }
        return inscritos;
    }

    public static int getTotalAulas() {
        return totalAulas;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        if (horario != null && !horario.isBlank()) {
            this.horario = horario;
        }
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        if (descricao != null && !descricao.isBlank()) {
            this.descricao = descricao;
        }
    }

    public Funcionario getInstrutor() {
        return instrutor;
    }

    public void setInstrutor(Funcionario instrutor) {
        if (instrutor == null || instrutor.getCargo() != Cargo.INSTRUTOR) {
            throw new IllegalArgumentException("A aula deve ser ministrada por um funcionário com cargo de INSTRUTOR.");
        }
        this.instrutor = instrutor;
    }

    public List<Membro> getAlunos() {
        return new ArrayList<>(alunos);
    }

    @Override
    public String toString() {
        return "Aula [horario=" + horario + ", descricao=" + descricao
                + ", instrutor=" + instrutor.getNome()
                + ", inscritos=" + alunos.size() + "]";
    }
}
