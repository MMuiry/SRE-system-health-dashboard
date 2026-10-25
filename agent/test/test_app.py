from app import app, collect_stats


def test_health_returns_up():
    response = app.test_client().get("/health")
    assert response.status_code == 200, "/health should return HTTP 200"
    assert response.get_json()["status"] == "UP", "/health should report UP"


def test_system_reports_metrics():
    data = app.test_client().get("/system").get_json()
    for key in ["machine", "cpu_percent", "memory_percent", "disk_percent", "timestamp"]:
        assert key in data, f"/system is missing '{key}'"
    assert 0 <= data["cpu_percent"] <= 100, "cpu_percent should be between 0 and 100"


def test_machine_name_comes_from_env(monkeypatch):
    monkeypatch.setenv("MACHINE_NAME", "test-machine")
    assert collect_stats()["machine"] == "test-machine", "machine should come from MACHINE_NAME"