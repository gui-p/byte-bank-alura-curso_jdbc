package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Set;

public class ContaService {


    public Set<Conta> listarContasAbertas() {
        Connection conn = ConnectionFactory.recuperaConexao();
        return new ContaDAO(conn).listar();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta)  {

        Connection conn = ConnectionFactory.recuperaConexao();
        new ContaDAO(conn).salvar(dadosDaConta);
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        if (!conta.getEstaAtiva()){
            throw new RegraDeNegocioException("Conta não está ativa");
        }

        BigDecimal novoValor = conta.getSaldo().subtract(valor);
        this.alterarValor(conta.getNumero(), novoValor);
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        var conta = buscarContaPorNumero(numeroDaConta);

        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }

        if (!conta.getEstaAtiva()){
            throw new RegraDeNegocioException("Conta não está ativa");
        }

        BigDecimal novoValor = conta.getSaldo().add(valor);
        this.alterarValor(conta.getNumero(), novoValor);
    }

    public void realizarTransferencia(Integer numeroDaContaOrigem, Integer numeroDaContaDestino, BigDecimal valor){
        this.realizarSaque(numeroDaContaOrigem, valor);
        this.realizarDeposito(numeroDaContaDestino, valor);
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = ConnectionFactory.recuperaConexao();
        new ContaDAO(conn).deletar(conta.getNumero());
    }

    public void encerrarLogico(Integer numeroDaConta){
        var conta = buscarContaPorNumero(numeroDaConta);
        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection conn = ConnectionFactory.recuperaConexao();
        new ContaDAO(conn).alterarLogico(conta.getNumero());
    }

    public Conta buscarContaPorNumero(Integer numero) {
        Connection conn = ConnectionFactory.recuperaConexao();
        Conta c = new ContaDAO(conn).recuperarConta(numero);
        if (c == null) throw new RegraDeNegocioException("Não existe conta cadastrada com esse número!");
        return c;
    }

    private void alterarValor(Integer numeroConta, BigDecimal novoValor){
        Connection conn = ConnectionFactory.recuperaConexao();
        new ContaDAO(conn).alterar(numeroConta, novoValor);
    }
}
