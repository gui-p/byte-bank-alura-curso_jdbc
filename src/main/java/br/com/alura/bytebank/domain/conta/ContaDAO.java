package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {

    private final Connection conn;
    ContaDAO(Connection connnection){
        this.conn = connnection;
    }

    public void salvar(DadosAberturaConta dadosDaConta){
        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), cliente, BigDecimal.ZERO, true);

        String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email, esta_ativa)"
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try{
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setInt(1, conta.getNumero());
            preparedStatement.setBigDecimal(2, conta.getSaldo());
            preparedStatement.setString(3, cliente.getNome());
            preparedStatement.setString(4, cliente.getCpf());
            preparedStatement.setString(5, cliente.getEmail());
            preparedStatement.setBoolean(6, conta.getEstaAtiva());
            preparedStatement.execute();
            preparedStatement.close();
            conn.close();
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    public Set<Conta> listar(){
        Set<Conta> contas = new HashSet<>();

        String sql = "SELECT * FROM conta WHERE esta_ativa = TRUE";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()){
                Integer numero = resultSet.getInt(1);
                BigDecimal saldo = resultSet.getBigDecimal(2);
                String nome = resultSet.getString(3);
                String cpf = resultSet.getString(4);
                String email = resultSet.getString(5);
                boolean atividade = resultSet.getBoolean(6);
                Cliente titular = new Cliente(new DadosCadastroCliente(nome, cpf, email));
                Conta conta = new Conta(numero, titular, saldo, atividade);
                contas.add(conta);
            }
            resultSet.close();
            ps.close();
            conn.close();

        }catch (SQLException e){
            throw new RuntimeException(e.getMessage());
        }
        return contas;
    }

    public Conta recuperarConta(Integer numero){
        Conta conta = null;
        String sql = "SELECT * FROM conta WHERE numero = ? AND esta_ativa = TRUE";
        try {

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, numero);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Integer numeroDaConta = rs.getInt(1);
                BigDecimal saldo = rs.getBigDecimal(2);
                String nome = rs.getString(3);
                String cpf = rs.getString(4);
                String email = rs.getString(5);
                boolean atividade = rs.getBoolean(6);
                Cliente titular = new Cliente(new DadosCadastroCliente(nome, cpf, email));
                conta = new Conta(numeroDaConta, titular, saldo, atividade);
            }
            rs.close();
            ps.close();
            conn.close();

        }catch (SQLException e){
            throw new RuntimeException(e.getMessage());
        }
        return conta;
    }

    public void alterar(Integer numero, BigDecimal valor){
        PreparedStatement ps;
        String sql = "UPDATE conta SET saldo = ? WHERE numero = ?";

        try{
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            ps.setBigDecimal(1, valor);
            ps.setInt(2, numero);
            ps.execute();
            conn.commit();
            ps.close();
            conn.close();
        }catch (SQLException e){
            try {
                conn.rollback();
            }catch (SQLException e2){
                throw new RuntimeException(e2.getMessage());
            }
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deletar(Integer numeroDaConta){
        PreparedStatement ps;
        String sql = "DELETE FROM conta WHERE numero = ?";

        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, numeroDaConta);
            ps.execute();
            ps.close();
            conn.close();
        }catch (SQLException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void alterarLogico(Integer numeroDaConta){
        PreparedStatement ps;
        String sql = "UPDATE conta SET esta_ativa = FALSE WHERE numero = ?";

        try{
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            ps.setInt(1, numeroDaConta);
            ps.execute();
            conn.commit();
            ps.close();
            conn.close();
        }catch (SQLException e){
            try {
                conn.rollback();
            }catch (SQLException e2){
                throw new RuntimeException(e2.getMessage());
            }
            throw new RuntimeException(e.getMessage());
        }
    }
}
