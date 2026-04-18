#!/usr/bin/env python3
import argparse
import math
import random
import time
import urllib.request
import urllib.error
import json

BASE_URL = "http://localhost:8080"
TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI3NDNkNzA4Ny05OWJmLTQ5ZmMtYjk2OC05MmI4MGZjZTg0MzgiLCJ1c2VybmFtZSI6InRlc3QtdXNlcm5hbWUiLCJyb2xlIjoiQURNSU4iLCJpYXQiOjE3NzY1MjkxNzUsImV4cCI6MTc3NjYxNTU3NX0.5gswqHFozdCB8QWeI1tDiIjLJ6UtNxWI7dCr2ZmKs0uGHka_tf9ZQeA9hwl4mBi2"
SOURCE_ID = "b2e7c5a5-28c6-4538-925b-51a35bfb4b01"
INTERVALS = 5
SERVERS = ["web-01", "web-02", "db-01", "worker-01"]


def build_metrics(server: str, tick: int) -> list:
    is_spike = random.random() < 0.20

    cpu_base = 30 + 20 * math.sin(tick / 30) + random.uniform(-5, 5)
    cpu = min(98, cpu_base + (50 if is_spike else 0))

    ram_base = 55 + 10 * math.sin(tick / 60 + 1) + random.uniform(-3, 3)
    ram = min(97, ram_base + (30 if is_spike else 0))

    disk_io = max(0, 20 + random.gauss(0, 15))
    net_in = max(0, random.expovariate(1 / 500))
    net_out = max(0, random.expovariate(1 / 300))
    req_per_sec = max(0, 120 + random.gauss(0, 30) + (200 if is_spike else 0))
    error_rate = random.uniform(0, 0.5) + (5 if is_spike else 0)

    return [
        {"name": "cpu_usage_percent", "value": round(cpu, 2), "labels": {"host": server}},
        {"name": "memory_usage_percent", "value": round(ram, 2), "labels": {"host": server}},
        {"name": "disk_io_mbps", "value": round(disk_io, 2), "labels": {"host": server}},
        {"name": "network_in_kbps", "value": round(net_in, 2), "labels": {"host": server}},
        {"name": "network_out_kbps", "value": round(net_out, 2), "labels": {"host": server}},
        {"name": "requests_per_second", "value": round(req_per_sec, 2), "labels": {"host": server}},
        {"name": "error_rate_percent", "value": round(error_rate, 2), "labels": {"host": server}},
    ]


def push(token: str, source_id: str, server: str, tick: int) -> bool:
    payload = {
        "sourceId": source_id,
        "sourceName": server,
        "timestamp": int(time.time() * 1000),
        "metrics": build_metrics(server, tick),
    }
    data = json.dumps(payload).encode()
    req = urllib.request.Request(
        f"{BASE_URL}/api/metrics/push",
        data=data,
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {token}",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            return resp.status == 200
    except urllib.error.HTTPError as e:
        print(f"  [ERROR] {e.code} {e.reason}")
        return False
    except Exception as e:
        print(f"  [ERROR] {e}")
        return False


def main():
    print("Starting generating metrics")

    tick = 0
    while True:
        tick += 1
        ok_count = 0
        summary = []
        for server in SERVERS:
            metrics = build_metrics(server, tick)
            cpu = next(m["value"] for m in metrics if m["name"] == "cpu_usage_percent")
            ok = push(TOKEN, SOURCE_ID, server, tick)
            ok_count += ok
            spike_marker = " [SPIKE]" if cpu > 80 else ""
            summary.append(f"{server}: cpu={cpu}%{spike_marker}")

        ts = time.strftime("%H:%M:%S")
        status = "OK" if ok_count == len(SERVERS) else f"{ok_count}/{len(SERVERS)} OK"
        print(f"[{ts}] tick={tick} {status} | {' | '.join(summary)}")
        time.sleep(INTERVALS)


if __name__ == "__main__":
    main()
