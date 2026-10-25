import json
import os
import platform
import socket
import threading
import time

import psutil
from flask import Flask, jsonify

app = Flask(__name__)


KAFKA_SERVERS = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "")
KAFKA_TOPIC = os.getenv("KAFKA_METRICS_TOPIC", "system-metrics")
PUBLISH_INTERVAL = int(os.getenv("PUBLISH_INTERVAL_SECONDS", "5"))

def machine_name():
    return os.getenv("MACHINE_NAME", socket.gethostname())
def to_mb(n):
    return round(n / 1024 / 1024)

def to_gb(n):
    return round(n / 1024 / 1024 / 1024, 1)

def collect_stats():
    memory = psutil.virtual_memory()
    disk = psutil.disk_usage(os.path.abspath(os.sep))
    return {
        "machine": machine_name(),
        "os": f"{platform.system()} {platform.release()}",
        "cpu_count": psutil.cpu_count(),
        "cpu_percent": psutil.cpu_percent(interval=0.5),
        "memory_total_mb": to_mb(memory.total),
        "memory_used_mb": to_mb(memory.used),
        "memory_percent": memory.percent,
        "disk_total_gb": to_gb(disk.total),
        "disk_used_gb": to_gb(disk.used),
        "disk_percent": disk.percent,
        "uptime_seconds": int(time.time() - psutil.boot_time()),
        "timestamp": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
    }

@app.get("/health")
def health():
    return jsonify(status="UP")

@app.get("/system")
def system():
    return jsonify(collect_stats())

def on_delivery(err, msg):
    if err is not None:
        print(f"Message not sent: {err}", flush=True)

def publish_forever():
    from confluent_kafka import Producer

    producer = Producer({"bootstrap.servers": KAFKA_SERVERS})
    while True:
        try:
            stats = collect_stats()
            producer.produce(KAFKA_TOPIC, key=stats["machine"], value=json.dumps(stats),
                             on_delivery=on_delivery)
            producer.poll(0)
        except Exception as ex:
            print(f"Could not publish metrics: {ex}", flush=True)
        time.sleep(PUBLISH_INTERVAL)

def start_publisher():
    if KAFKA_SERVERS:
        threading.Thread(target=publish_forever, daemon=True).start()
        print(f"Publishing metrics to '{KAFKA_TOPIC}' every {PUBLISH_INTERVAL} seconds", flush=True)
    else:
        print("KAFKA_BOOTSTRAP_SERVERS not set - metrics publishing is off", flush=True)

start_publisher()

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=int(os.getenv("PORT", "5000")))