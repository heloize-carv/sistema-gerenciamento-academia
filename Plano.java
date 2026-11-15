import java.util.ArrayList;
import java.util.List;

public class Plano {
    private static int numPlanos = 0;
    private static List<Plano> planos = new ArrayList<>();
    private String beneficios;
    private float valorMensal;
    private String regrasVigencia;
    private boolean disponivel;

    private Plano(String beneficios, float valorMensal, String regrasVigencia) {
        this.beneficios = beneficios;
        this.valorMensal = valorMensal;
        this.regrasVigencia = regrasVigencia;
        this.disponivel = true;
        numPlanos++;
        planos.add(this);
    }

    public void verPlano() {
        System.out.println("Benefícios: " + beneficios);
        System.out.println("Valor Mensal: " + valorMensal);
        System.out.println("Regras de Vigência: " + regrasVigencia);
        System.out.println("Disponível: " + disponivel);
    }

    public static Plano criarPlano(String beneficios, float valorMensal, String regrasVigencia) {
        return new Plano(beneficios, valorMensal, regrasVigencia);
    }

    public void editarPlano(String beneficios, float valorMensal, String regrasVigencia, boolean disponivel) {
        this.beneficios = beneficios;
        this.valorMensal = valorMensal;
        this.regrasVigencia = regrasVigencia;
        this.disponivel = disponivel;
    }

    public static void excluirPlano(Plano plano) {
        if (planos.remove(plano)) {
            numPlanos--;
        }
    }

    public static List<Plano> listarPlanos() {
        return new ArrayList<>(planos);
    }

    public static List<Plano> listarPlanosAtivo() {
        List<Plano> ativos = new ArrayList<>();
        for (Plano plano : planos) {
            if (plano.disponivel) {
                ativos.add(plano);
            }
        }
        return ativos;
    }

    public static int getNumPlanos() {
        return numPlanos;
    }

    public String getBeneficios() {
        return beneficios;
    }

    public float getValorMensal() {
        return valorMensal;
    }

    public String getRegrasVigencia() {
        return regrasVigencia;
    }

    public boolean isDisponivel() {
        return disponivel;
    }
}
