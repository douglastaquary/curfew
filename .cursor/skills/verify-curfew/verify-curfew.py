#!/usr/bin/env python3
"""Sobe a Curfew, dirige as telas e guarda a prova. A saída é JSON. Não imprime segredo."""

import argparse
import json
import os
import signal
import subprocess
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).resolve().parent
RUN = HERE / "run"
ARTIFACTS = HERE / "artifacts"
STATE = RUN / "state.json"
LOG = RUN / "quarkus.log"
PORT = 8473
URL = f"http://127.0.0.1:{PORT}"
SECRET_MARKERS = ("ghp_", "github_pat_", "AKIA")
MARKERS = (
    "Revogar",
    "Nenhum acesso esquecido",
    "Lendo GitHub e AWS",
    "Nada foi fechado",
    "Leitura ainda não ligada",
    "Acompanhamento",
    "Passagem da noite",
    "Executar a passagem",
    "Trancar e pedir um humano",
    "Chave com prazo",
    "menos de 24 horas",
)
STRIPPED = (
    "CURFEW_GITHUB_TOKEN",
    "CURFEW_AWS_ACCESS_KEY_ID",
    "CURFEW_AWS_SECRET_ACCESS_KEY",
    "CURFEW_CLOSE",
    "CURFEW_ISSUE",
)


def main():
    parser = argparse.ArgumentParser(
        description="Dirige a Curfew em http://127.0.0.1:8473. Cada comando devolve JSON."
    )
    sub = parser.add_subparsers(dest="command", required=True)

    up = sub.add_parser("up", help="Sobe ./mvnw quarkus:dev nesta execução.")
    up.add_argument("--dry-run", action="store_true", help="Mostra o comando e não sobe nada.")

    down = sub.add_parser("down", help="Encerra só o processo que up desta execução subiu.")
    down.add_argument("--dry-run", action="store_true", help="Mostra o pid e não encerra.")

    sub.add_parser("doctor", help="Diz se a instância desta execução responde na fila.")

    get = sub.add_parser("get", help="Lê uma rota e devolve status e h1, sem o HTML.")
    get.add_argument("path")

    sub.add_parser("log", help="Lê o fluxo da varredura até o fim.")

    shot = sub.add_parser("screenshot", help="Grava um print da rota em artifacts/.")
    shot.add_argument("path")

    sub.add_parser("prove", help="Sobe, confere a varredura, grava o print e encerra.")

    plan = sub.add_parser("plan", help="Confere se cada item do plano tem prova no ar.")
    plan.add_argument("file")

    args = parser.parse_args()
    try:
        if args.command == "up":
            emit(cmd_up(args.dry_run))
        elif args.command == "down":
            emit(cmd_down(args.dry_run))
        elif args.command == "doctor":
            emit(cmd_doctor())
        elif args.command == "get":
            emit(cmd_get(args.path))
        elif args.command == "log":
            emit(cmd_log())
        elif args.command == "screenshot":
            emit(cmd_screenshot(args.path))
        elif args.command == "prove":
            emit(cmd_prove())
        elif args.command == "plan":
            emit(cmd_plan(args.file))
    except SystemExit:
        raise
    except Exception as exception:
        detail = str(exception)
        if any(marker in detail for marker in SECRET_MARKERS):
            detail = "detalhe omitido"
        fail(
            f"O comando não completou ({type(exception).__name__}: {detail}).",
            "Leia a mensagem e rode doctor. Não feche acesso.",
        )


def cmd_up(dry_run):
    command = ["./mvnw", "quarkus:dev", "-Dquarkus.http.host=127.0.0.1"]
    if dry_run:
        return {"ok": True, "dryRun": True, "command": command, "url": URL, "secret": False}
    if STATE.exists():
        state = read_state()
        if pid_alive(state.get("pid")):
            fail(
                "Esta execução já subiu uma instância.",
                "Rode doctor. Se ela não servir, rode down e depois up.",
            )
    if listeners(PORT):
        fail(
            "A porta 8473 já tem outro processo.",
            "Não encerre pelo nome. Pare o processo que você subiu, ou rode down se o state for desta execução.",
        )
    RUN.mkdir(parents=True, exist_ok=True)
    log = LOG.open("w", encoding="utf-8")
    env = os.environ.copy()
    for name in STRIPPED:
        env.pop(name, None)
    process = subprocess.Popen(
        command,
        cwd=ROOT,
        env=env,
        stdout=log,
        stderr=subprocess.STDOUT,
        start_new_session=True,
    )
    state = {"pid": process.pid, "port": PORT, "url": URL}
    STATE.write_text(json.dumps(state), encoding="utf-8")
    deadline = time.time() + 120
    while time.time() < deadline:
        if process.poll() is not None:
            try:
                os.killpg(process.pid, signal.SIGTERM)
            except ProcessLookupError:
                pass
            fail("A Curfew encerrou antes de responder.", "Leia .cursor/skills/verify-curfew/run/quarkus.log.")
        status, _body = fetch("/", timeout=2)
        if status == 200:
            return {"ok": True, "pid": process.pid, "url": URL, "secret": False}
        time.sleep(1)
    os.killpg(process.pid, signal.SIGTERM)
    fail("A Curfew não respondeu em 120 segundos.", "Leia .cursor/skills/verify-curfew/run/quarkus.log e rode down.")


def cmd_down(dry_run):
    if not STATE.exists():
        return {"ok": True, "stopped": False, "reason": "Nenhuma instância desta execução."}
    state = read_state()
    pid = state.get("pid")
    if dry_run:
        return {"ok": True, "dryRun": True, "pid": pid}
    if pid_alive(pid):
        try:
            os.killpg(pid, signal.SIGTERM)
        except ProcessLookupError:
            pass
        for _ in range(20):
            if not pid_alive(pid):
                break
            time.sleep(0.25)
        if pid_alive(pid):
            try:
                os.killpg(pid, signal.SIGKILL)
            except ProcessLookupError:
                pass
    if STATE.exists():
        STATE.unlink()
    return {"ok": True, "stopped": True, "pid": pid, "artifacts": str(ARTIFACTS)}


def cmd_doctor():
    state = require_state()
    pid = state.get("pid")
    if not pid_alive(pid):
        fail("O processo desta execução não está no ar.", "Rode up de novo.")
    owners = listener_pids(PORT)
    tree = descendants(pid)
    if not owners or not owners.intersection(tree):
        fail(
            "A porta 8473 não pertence a esta execução.",
            "Não dirija essa porta. Rode down se o state for seu e depois up.",
        )
    status, body = fetch("/")
    if status != 200 or "<h1>Fila</h1>" not in body:
        fail("A fila não respondeu.", "Rode down e depois up.")
    refuse_secret(body)
    return {"ok": True, "pid": pid, "url": URL, "h1": "Fila", "secret": False}


def cmd_get(path):
    cmd_doctor()
    status, body = fetch(path)
    refuse_secret(body)
    if status not in (200, 404):
        fail(f"A rota {path} respondeu {status}.", "Confira o caminho. Nada foi fechado.")
    return {
        "ok": True,
        "path": path,
        "status": status,
        "h1": h1(body),
        "markers": [marker for marker in MARKERS if marker in body],
        "secret": False,
    }


def cmd_log():
    cmd_doctor()
    try:
        with urllib.request.urlopen(URL + "/varredura/eventos", timeout=20) as response:
            raw = response.read().decode("utf-8", "replace")
    except urllib.error.URLError:
        fail("O fluxo da varredura não completou.", "Nada foi fechado. Rode doctor.")
    refuse_secret(raw)
    events = parse_sse(raw)
    done = next((item["data"] for item in events if item["event"] == "done"), "")
    data = "\n".join(item["data"] for item in events)
    ok = (
        done == "0 0 0 0"
        and "Início da leitura" in data
        and "Nada foi fechado" in data
        and "Revogar" not in data
    )
    if not ok:
        fail("A varredura sem conta não fechou em 0 0 0 0.", "A instância de verificação não leva token nem chave.")
    return {
        "ok": True,
        "done": done,
        "inicio": True,
        "revogar": True,
        "nadaFechado": True,
        "secret": False,
    }


def cmd_screenshot(path):
    cmd_doctor()
    browser = find_browser()
    if browser is None:
        fail("Não há Chrome nem Chromium neste ambiente.", "Instale um dos dois e rode screenshot de novo.")
    ARTIFACTS.mkdir(parents=True, exist_ok=True)
    target = ARTIFACTS / (slug(path) + ".png")
    command = [
        "timeout",
        "25",
        browser,
        "--headless=new",
        "--disable-gpu",
        "--no-sandbox",
        "--hide-scrollbars",
        "--window-size=1280,1600",
        "--virtual-time-budget=12000",
        f"--screenshot={target}",
        URL + path,
    ]
    subprocess.run(command, cwd=ROOT, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    if not target.exists() or target.stat().st_size < 1000:
        fail("O print não foi gravado.", "Rode screenshot de novo. Não apague artifacts/ no down.")
    return {"ok": True, "path": str(target.relative_to(ROOT)), "bytes": target.stat().st_size, "secret": False}


def cmd_plan(path):
    file = Path(path)
    if not file.is_file():
        fail("O plano não está nesse caminho.", "Passe o arquivo markdown. Não deixe o plano no repositório depois do corte.")
    text = file.read_text(encoding="utf-8")
    lowered = text.lower()
    if "sam deploy" in lowered or "fechar acesso" in lowered:
        fail(
            "O plano pede deploy ou fechamento de acesso.",
            "Tire esse passo. Este corte não publica na AWS e não fecha acesso.",
        )
    sections = []
    title = None
    body = []
    for line in text.splitlines():
        if line.startswith("## "):
            if title is not None:
                sections.append((title, body))
            title = line[3:].strip()
            body = []
        elif title is not None:
            body.append(line)
    if title is not None:
        sections.append((title, body))
    if not sections:
        fail("O plano não tem item.", "Cada mudança precisa de um título ## e de uma linha - Prova:.")
    weak = []
    for name, lines in sections:
        proof = next((line for line in lines if line.startswith("- Prova:")), "")
        live = "verify-curfew.py" in proof
        unit_only = "./mvnw test" in proof and "sem tela" in proof
        if not (live or unit_only):
            weak.append(name or "(sem título)")
    if weak:
        fail(
            "Estes itens não têm prova no ar: " + ", ".join(weak) + ".",
            "Use verify-curfew.py na linha - Prova:. Se o item não mexe em tela, escreva ./mvnw test e sem tela.",
        )
    return {"ok": True, "items": len(sections), "secret": False}


def cmd_prove():
    try:
        cmd_up(False)
        queue = cmd_get("/")
        if "Chave com prazo" not in queue["markers"] or "menos de 24 horas" not in queue["markers"]:
            fail(
                "A fila não mostra a chave com prazo.",
                "A prova só lê. Não envie o formulário da chave.",
            )
        page = cmd_get("/varredura")
        log = cmd_log()
        shot = cmd_screenshot("/varredura")
        lease = cmd_screenshot("/")
    finally:
        cmd_down(False)
    if not Path(ROOT, shot["path"]).exists() or not Path(ROOT, lease["path"]).exists():
        fail("O down apagou a prova.", "A prova fica em artifacts/ e sobrevive ao down.")
    return {
        "ok": True,
        "h1": page["h1"],
        "done": log["done"],
        "proof": shot["path"],
        "lease": lease["path"],
        "secret": False,
    }


def fetch(path, timeout=10):
    try:
        with urllib.request.urlopen(URL + path, timeout=timeout) as response:
            return response.status, response.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as exception:
        return exception.code, exception.read().decode("utf-8", "replace")
    except (urllib.error.URLError, TimeoutError):
        return 0, ""


def parse_sse(raw):
    events = []
    event = "message"
    data = []
    for line in raw.splitlines():
        if line.startswith("event:"):
            event = line.split(":", 1)[1].strip()
        elif line.startswith("data:"):
            data.append(line.split(":", 1)[1].strip())
        elif line == "":
            if data:
                events.append({"event": event, "data": "\n".join(data)})
            event = "message"
            data = []
    if data:
        events.append({"event": event, "data": "\n".join(data)})
    return events


def h1(body):
    start = body.find("<h1>")
    end = body.find("</h1>")
    if start < 0 or end < start:
        return ""
    return body[start + 4 : end].strip()


def refuse_secret(body):
    if any(marker in body for marker in SECRET_MARKERS):
        fail("A resposta contém valor de segredo. O valor não foi impresso.", "Pare esta leitura. Nada foi fechado.")


def require_state():
    if not STATE.exists():
        fail("Não há instância desta execução.", "Rode up. Não use uma Curfew que já estava na porta.")
    return read_state()


def read_state():
    return json.loads(STATE.read_text(encoding="utf-8"))


def pid_alive(pid):
    if not isinstance(pid, int):
        return False
    try:
        os.kill(pid, 0)
    except OSError:
        return False
    return True


def descendants(pid):
    parent = {}
    for entry in os.listdir("/proc"):
        if not entry.isdigit():
            continue
        try:
            stat = Path(f"/proc/{entry}/stat").read_text(encoding="utf-8")
        except OSError:
            continue
        cut = stat.rfind(")")
        if cut < 0:
            continue
        parts = stat[cut + 2 :].split()
        if len(parts) > 1:
            parent[int(entry)] = int(parts[1])
    found = {pid}
    growing = True
    while growing:
        growing = False
        for child, owner in parent.items():
            if owner in found and child not in found:
                found.add(child)
                growing = True
    return found


def listeners(port):
    return listener_pids(port)


def listener_pids(port):
    needle = f":{port:04X}"
    inodes = set()
    for name in ("tcp", "tcp6"):
        path = Path(f"/proc/net/{name}")
        if not path.exists():
            continue
        for line in path.read_text(encoding="utf-8").splitlines()[1:]:
            fields = line.split()
            if len(fields) < 10:
                continue
            if fields[1].upper().endswith(needle) and fields[3] == "0A":
                inodes.add(fields[9])
    pids = set()
    for entry in os.listdir("/proc"):
        if not entry.isdigit():
            continue
        folder = Path(f"/proc/{entry}/fd")
        try:
            links = list(folder.iterdir())
        except OSError:
            continue
        for link in links:
            try:
                target = os.readlink(link)
            except OSError:
                continue
            if target.startswith("socket:[") and target[8:-1] in inodes:
                pids.add(int(entry))
    return pids


def find_browser():
    candidates = [
        "/usr/bin/google-chrome",
        "/usr/bin/google-chrome-stable",
        "/usr/local/bin/google-chrome",
        "/opt/google/chrome/chrome",
        "/usr/bin/chromium",
        "/usr/bin/chromium-browser",
    ]
    for candidate in candidates:
        if Path(candidate).is_file() and os.access(candidate, os.X_OK):
            return candidate
    return None


def slug(path):
    text = path.strip("/") or "fila"
    cleaned = "".join(character if character.isalnum() else "-" for character in text)
    while "--" in cleaned:
        cleaned = cleaned.replace("--", "-")
    return cleaned.strip("-") or "fila"


def emit(payload):
    json.dump(payload, sys.stdout, ensure_ascii=False)
    sys.stdout.write("\n")
    if not payload.get("ok", False):
        sys.exit(1)


def fail(error, next_step):
    emit({"ok": False, "error": error, "next": next_step, "secret": False})


if __name__ == "__main__":
    main()
