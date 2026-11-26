import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Assinatura {
    private static int numeroAtivas = 0;
    private static List <Assinatura> listaAssinaturas = new ArrayList<>();
    private Date dataAssinatura;
    private Date dataPagamento;
    private Date dataTermino;
    private Date dataPagamentoReal;
    private boolean pago;
    private Situacao situacao;
    private Plano plano;
    private Membro membro;

    public Assinatura(Plano plano, Membro membro, Date dataPagamento, Date dataTermino) {
        this.plano=plano;
        this.membro=membro;
        this.dataPagamento = dataPagamento;
        this.dataTermino=dataTermino;
        this.dataAssinatura = new Date();
        this.pago = false;
        this.situacao = Situacao.ATIVA;
    }
    public void verAssinatura(){
        System.out.println ("- Detalhes de Assinatura -");
        System.out.println ("Membro: " + this.membro != null ? this.membro.getNome() : "Inexistente");
        System.out.println ("Data de Assinatura: " + this.dataAssinatura);
        System.out.println ("Data de Pagamento: " + this.dataPagamento);
        System.out.println ("PAGO: " + (this.pago ? "sim" : "não"));
        System.out.println ("Situação: " + this.situacao);
    }
    public void registrarPagamento(){
        this.pago = true;
        this.dataPagamentoReal = new Date();
        this.situacao=Situacao.ATIVA;
        System.out.println ("Pagamento Registrado.");
    }
    public void verificarVencimento(){
        Date hoje = new Date();
        if (!this.pago && hoje.after(this.dataPagamento) && this.situacao != Situacao.CANCELADA){
            this.situacao= Situacao.VENCIDA;
        }
    }

    public static Assinatura criarAssinatura  (Plano plano, Membro membro, Date dataPagamento, Date dataTermino){
        Assinatura novaAssinatura = new Assinatura(plano,membro, dataPagamento, dataTermino);
        listaAssinaturas.add(novaAssinatura);
        numeroAtivas++;
        System.out.println ("Assinatura criada com sucesso!");
        return novaAssinatura;
    }
    public static void editarAssinatura (Assinatura assinatura, Date dataAssinatura, Date dataPagamento, Date dataTermino, Date dataPagamentoReal, boolean pago, Situacao situacao){
        if (assinatura!= null){
            assinatura.dataAssinatura = dataAssinatura;
            assinatura.dataPagamento = dataPagamento;
            assinatura. dataTermino=dataTermino;
            assinatura.dataPagamentoReal=dataPagamentoReal;
            assinatura.pago=pago;
            assinatura.situacao=situacao;
            System.out.println ("Assinatura editada com sucesso.");
        }
    }
    public static void excluirAssinatura(Assinatura assinatura){
        if (assinatura != null && listaAssinaturas.remove(assinatura)){
            if(assinatura.situacao == Situacao.ATIVA){
                numeroAtivas--;
            }
            System.out.println ("Assinatura Excluída.");
        }
    }
    public static List <Assinatura> listarAssinaturas (){
        return listaAssinaturas;
    }
    public static List <Assinatura> listarPagamentoAtrasados(){
        List<Assinatura> atrasadas = new ArrayList<>();
        Date hoje = new Date();
        for (Assinatura a : listaAssinaturas){
            if (!a.pago && hoje.after (a.dataPagamento)){
                atrasadas.add(a);
            }
        }
        return atrasadas;
    }
    public static List <Assinatura> listarPagamentoAtrasadoPorMembro(Membro membro){
        List <Assinatura> atrasadasMembro = new ArrayList<>();
        Date hoje = new Date ();
        for (Assinatura a: listaAssinaturas){
            if (a.membro != null && a.membro.equals (membro) && !a.pago && hoje.after(a.dataPagamento)){
                atrasadasMembro.add(a);
            }
        }
        return atrasadasMembro;
    }
    public static List <Assinatura> listarAssinaturasAtivas(){
        List <Assinatura> ativas = new ArrayList<>();
        for (Assinatura a : listaAssinaturas){
            if (a.situacao == Situacao.ATIVA){
                ativas.add(a);
            }
        }
        return ativas;
    }
    public static List<Assinatura> listarAssinaturasAtivasPorMembro(Membro membro) {
        List<Assinatura> ativasMembro = new ArrayList<>();
        for (Assinatura a : listaAssinaturas) {
            if (a.membro != null && a.membro.equals(membro) && a.situacao == Situacao.ATIVA) {
                ativasMembro.add(a);
            }
        }
        return ativasMembro;
    }

    public static void atualizarSituacoes() {
        for (Assinatura a : listaAssinaturas) {
            a.verificarVencimento();
        }
        System.out.println("Situações atualizadas.");
    }
    public static int getNumeroAtivas() {
        return numeroAtivas;
    }

    public Date getDataAssinatura() {
        return dataAssinatura;
    }

    public void setDataAssinatura(Date dataAssinatura) {
        this.dataAssinatura = dataAssinatura;
    }

    public Date getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(Date dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public Date getDataTermino() {
        return dataTermino;
    }

    public void setDataTermino(Date dataTermino) {
        this.dataTermino = dataTermino;
    }

    public Date getDataPagamentoReal() {
        return dataPagamentoReal;
    }

    public void setDataPagamentoReal(Date dataPagamentoReal) {
        this.dataPagamentoReal = dataPagamentoReal;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public Situacao getSituacao() {
        return situacao;
    }
    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }

    public Membro getMembro() {
        return membro;
    }

    public void setMembro(Membro membro) {
        this.membro = membro;
    }
}
