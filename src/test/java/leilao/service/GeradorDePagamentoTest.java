package leilao.service;

import br.com.adrianoba.leilao.dao.PagamentoDao;
import br.com.adrianoba.leilao.model.Lance;
import br.com.adrianoba.leilao.model.Leilao;
import br.com.adrianoba.leilao.model.Pagamento;
import br.com.adrianoba.leilao.model.Usuario;

import br.com.adrianoba.leilao.service.GeradorDePagamento;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.*;


public class GeradorDePagamentoTest {

    private GeradorDePagamento gerador;
    @Mock
    private PagamentoDao pagamentoDao;

    @Captor
    private ArgumentCaptor<Pagamento> captor;
    @Mock
    private Clock clock;


    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        this.gerador = new GeradorDePagamento(pagamentoDao, clock);
    }

    @Test
    void deveriaCriarPagamentoParaVencedorDoLeilao(){
        Leilao leilao = leilao();
        Lance vencedor = leilao.getLanceVencedor();

        LocalDate data = LocalDate.of(2023, 12, 12);

        Instant instant = data.atStartOfDay(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant()).thenReturn(instant);
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        gerador.gerarPagamento(vencedor);

        Mockito.verify(pagamentoDao).salvar(captor.capture());

        Pagamento pagamento = captor.getValue();
        Assert.assertEquals(LocalDate.now().plusDays(1), pagamento.getVencimento());
        Assert.assertEquals(pagamento.getValor(), vencedor.getValor());
        Assert.assertFalse(pagamento.getPago());
        Assert.assertEquals(vencedor.getUsuario(), pagamento.getUsuario());
        Assert.assertEquals(leilao, pagamento.getLeilao());
    }

    private Leilao leilao() {
        Leilao leilao = new Leilao("Celular",
                new BigDecimal("500"),
                new Usuario("Fulano"));

        Lance lance = new Lance(new Usuario("Ciclano"),
                new BigDecimal("900"));

        leilao.propoe(lance);
        leilao.setLanceVencedor(lance);

        return leilao;

    }
}
