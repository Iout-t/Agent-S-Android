"""HTTP adapter for the existing Python automation core.

Keep planning, tool execution, and device integrations in the Python core. This
module is only the stable boundary consumed by the Kotlin Android client.
"""
from __future__ import annotations

from flask import Flask, jsonify, request
from uuid import uuid4

app = Flask(__name__)

@app.get("/health")
def health():
    return jsonify({"status": "ok", "service": "agentx-python-core"})

@app.post("/v1/runs")
def create_run():
    payload = request.get_json(silent=True) or {}
    instruction = str(payload.get("instruction", "")).strip()
    if not instruction:
        return jsonify({"error": "instruction is required"}), 400
    # Delegate this request to the retained Python agent planner/executor.
    # Replace `dispatch_to_core` with the existing core entry point when it is
    # available; the transport contract does not need to change.
    return jsonify({"id": str(uuid4()), "status": "accepted", "instruction": instruction}), 202
