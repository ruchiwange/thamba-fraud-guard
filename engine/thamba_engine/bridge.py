"""Thin wrappers for the Android app, which passes and receives plain text."""

import json

from .engine import on_app, on_link, on_payment, on_sms


def on_sms_json(sender, text, on_call, caller_known):
    return json.dumps(on_sms(str(sender), str(text), bool(on_call), bool(caller_known)))


def on_link_json(url):
    return json.dumps(on_link(str(url)))


def on_app_json(package, installer, permissions_csv, is_system):
    """permissions_csv is the app's requested permissions joined with commas."""
    permissions = [p for p in str(permissions_csv).split(",") if p]
    return json.dumps(on_app(str(package), str(installer), permissions, bool(is_system)))


def on_payment_json(foreground_package, on_call, caller_known):
    return json.dumps(on_payment(str(foreground_package), bool(on_call), bool(caller_known)))
