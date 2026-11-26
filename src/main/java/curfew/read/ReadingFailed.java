package curfew.read;

/**
 * A leitura parou. A mensagem não carrega token, chave ou corpo da resposta.
 */
public final class ReadingFailed extends RuntimeException {

    public ReadingFailed(String message) {
        super(message);
    }
}
