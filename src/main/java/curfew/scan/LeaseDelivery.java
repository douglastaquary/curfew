package curfew.scan;

import java.time.Instant;
import java.util.Optional;

/**
 * A fila guarda a validade. O segredo da sessão só existe no resultado desta chamada.
 */
public final class LeaseDelivery {

    public Result deliver(
            LeaseBook book,
            SessionMint mint,
            boolean armed,
            String task,
            String system,
            int hours,
            Instant now) {
        if (hours >= 24) {
            return Result.bad("A chave precisa acabar em menos de 24 horas.");
        }
        if (hours < 1) {
            return Result.bad("A chave não foi entregue.");
        }
        if (armed && "AWS".equals(system)) {
            if (book.size() >= LeaseBook.CAP || !LeaseTerm.safeTask(task)) {
                return Result.bad("A chave não foi entregue.");
            }
            Minted minted = call(mint, hours);
            if (minted == null) {
                return Result.bad("A conta AWS não entregou a sessão.");
            }
            Optional<TaskLease> lease = book.issue(task, system, hours, now, minted.tail());
            if (lease.isEmpty()) {
                return Result.bad("A chave não foi entregue.");
            }
            return Result.once(lease.get(), minted);
        }
        if (armed) {
            return Result.bad("A chave real deste corte é a sessão AWS.");
        }
        Optional<TaskLease> lease = book.issue(task, system, hours, now);
        if (lease.isEmpty()) {
            return Result.bad("A chave não foi entregue.");
        }
        return Result.local(lease.get());
    }

    private static Minted call(SessionMint mint, int hours) {
        if (mint == null) {
            return null;
        }
        try {
            return mint.mint(hours).orElse(null);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public static final class Result {
        private final int status;
        private final String message;
        private final TaskLease lease;
        private final Minted minted;

        private Result(int status, String message, TaskLease lease, Minted minted) {
            this.status = status;
            this.message = message;
            this.lease = lease;
            this.minted = minted;
        }

        static Result bad(String message) {
            return new Result(400, message, null, null);
        }

        static Result local(TaskLease lease) {
            return new Result(200, "", lease, null);
        }

        static Result once(TaskLease lease, Minted minted) {
            return new Result(200, "", lease, minted);
        }

        public int status() {
            return status;
        }

        public String message() {
            return message;
        }

        public TaskLease lease() {
            return lease;
        }

        public Minted minted() {
            return minted;
        }

        public boolean calledAccount() {
            return minted != null;
        }

        @Override
        public String toString() {
            return "Result{status=" + status + ", conta=" + (calledAccount() ? "sim" : "nao") + "}";
        }
    }
}
